/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.automatedexportsystem.repositories

import cats.data.{EitherT, NonEmptyList}
import com.google.inject.ImplementedBy
import com.mongodb.client.model.{IndexModel, IndexOptions, Sorts}
import com.mongodb.{MongoNotPrimaryException, MongoSocketException, MongoTimeoutException}
import org.apache.pekko.pattern.RetrySupport
import org.bson.codecs.Codec
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.*
import org.mongodb.scala.{Document, MongoException, bson}
import play.api.Logging
import uk.gov.hmrc.automatedexportsystem.config.AppConfig
import uk.gov.hmrc.automatedexportsystem.errors.MongoError
import uk.gov.hmrc.automatedexportsystem.models.aesIE507.{EoriNumber, ExportOperationType, SubmissionId}
import uk.gov.hmrc.automatedexportsystem.models.mongo.read.MongoAesIE507MessageSummary
import uk.gov.hmrc.automatedexportsystem.models.mongo.write.MongoAesIE507Message
import uk.gov.hmrc.automatedexportsystem.models.mongo.{MongoAesIE507MessageProjections, UpdateStatus}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}

import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

@ImplementedBy(classOf[AesIE507RepositoryImpl])
trait AesIE507Repository:
  def getMessages(eori: EoriNumber): EitherT[Future, MongoError, NonEmptyList[MongoAesIE507MessageSummary]]

  def getMessage(eori: EoriNumber, submissionId: SubmissionId): EitherT[Future, MongoError, MongoAesIE507Message]

  def submit(submission: MongoAesIE507Message): EitherT[Future, MongoError, Boolean]

  def cancel(eori: EoriNumber, submissionId: SubmissionId, updatedAt: Instant): EitherT[Future, MongoError, UpdateStatus]

@Singleton
class AesIE507RepositoryImpl @Inject() (
  mongoComponent: MongoComponent,
  appConfig:      AppConfig
)(using protected val executionContext: ExecutionContext)
    extends PlayMongoRepository[MongoAesIE507Message](
      collectionName = "aes-ie507",
      mongoComponent = mongoComponent,
      domainFormat = MongoAesIE507Message.mongoFormat,
      replaceIndexes = appConfig.replaceIndexes,
      indexes = Seq(
        IndexModel(
          Indexes.ascending("submissionId"),
          IndexOptions()
            .name("submissionId_unique")
            .unique(true)
        ),
        IndexModel(
          Indexes.ascending("updatedAt"),
          IndexOptions().expireAfter(appConfig.documentTtl, TimeUnit.SECONDS)
        ),
        IndexModel(
          Indexes.compoundIndex(
            Indexes.ascending("eoriNumber"),
            Indexes.ascending("submissionId")
          )
        )
      ),
      extraCodecs = Seq(
        Codecs.playFormatCodec(MongoAesIE507MessageSummary.mongoFormat)
      )
    ),
      AesIE507Repository,
      Logging:

  def getMessages(eori: EoriNumber): EitherT[Future, MongoError, NonEmptyList[MongoAesIE507MessageSummary]] =
    val pipeline: Seq[Bson] = Seq(
      Aggregates.filter(Filters.eq("eoriNumber", eori.value)),
      Aggregates.project(MongoAesIE507MessageProjections.summaryProjection),
      Aggregates.sort(Sorts.descending("updatedAt"))
    )

    retryOperation("getMessages", Map("eori" -> eori.value)) {
      collection
        .aggregate[MongoAesIE507MessageSummary](pipeline)
        .toFuture()
        .map { messageSummaries =>
          NonEmptyList
            .fromList(messageSummaries.toList)
            .toRight(
              MongoError.DocumentNotFound(s"No documents found for EORI: ${eori.value}")
            )
        }
    }

  def getMessage(
    eori:         EoriNumber,
    submissionId: SubmissionId
  ): EitherT[Future, MongoError, MongoAesIE507Message] =
    retryOperation(
      operationName = "getMessage",
      context = Map("eoriNumber" -> eori.value, "submissionId" -> submissionId.value.toString)
    ) {
      collection
        .find(
          Filters.and(
            Filters.eq("eoriNumber", eori.value),
            Filters.eq("submissionId", submissionId.value.toString)
          )
        )
        .headOption()
        .map(
          _.toRight(
            MongoError.DocumentNotFound(
              s"No document found for EORI: ${eori.value} and submissionId: ${submissionId.value}"
            )
          )
        )
    }

  override def submit(submission: MongoAesIE507Message): EitherT[Future, MongoError, Boolean] =
    val sid: String = submission.submissionId.value.toString

    retryOperation(
      operationName = "submitUpsert",
      context = Map("submissionId" -> sid)
    ) {
      collection
        .replaceOne(
          Filters.eq("submissionId", sid),
          submission,
          ReplaceOptions().upsert(true)
        )
        .toFuture()
        .map(updateResult => Right(updateResult.wasAcknowledged()))
    }

  def cancel(eori: EoriNumber, submissionId: SubmissionId, updatedAt: Instant): EitherT[Future, MongoError, UpdateStatus] =
    val operationName: String = "cancel"

    val filter: Bson = Filters.and(
      Filters.eq("eoriNumber", eori.value),
      Filters.eq("submissionId", submissionId.value.toString)
    )

    val exportOperationTypeCancel: Int = ExportOperationType.Cancel.status

    val update: Seq[Bson] =
      Seq(
        Document(s"""{
          |  "$$set": {
          |    "updatedAt": {
          |      "$$cond": [
          |          { "$$ne": ["$$exportOperation.exportOperationType", $exportOperationTypeCancel] },
          |          { "$$date": { "$$numberLong": "${updatedAt.toEpochMilli}" } },
          |          "$$updatedAt"
          |      ]
          |    },
          |    "exportOperation.exportOperationType": $exportOperationTypeCancel
          |  }
          |}""".stripMargin)
      )

    retryOperation(
      operationName = operationName,
      context = Map("submissionId" -> submissionId.value.toString)
    ) {
      collection
        .updateOne(filter, update)
        .toFuture()
        .map(updateResult =>
          if !updateResult.wasAcknowledged() then
            Left(
              writeUnacknowledgedError(
                operationName,
                context = Map(
                  "eoriNumber"   -> eori.value,
                  "submissionId" -> submissionId.value.toString
                )
              )
            )
          else
            val matchedCount:  Long = updateResult.getMatchedCount
            val modifiedCount: Long = updateResult.getModifiedCount

            if matchedCount == 0 then Left(MongoError.DocumentNotFound(s"No document found for submissionId: ${submissionId.value}"))
            else if updateResult.getModifiedCount == 0 then Right(UpdateStatus.AlreadyUpToDate(operationName, matchedCount))
            else Right(UpdateStatus.Updated(operationName, matchedCount, modifiedCount))
        )
    }

  private def writeUnacknowledgedError(operation: String, context: Map[String, String]): MongoError =
    val contextString: String =
      if context.isEmpty then ""
      else context.map { case (k, v) => s"$k: $v" }.mkString(", ", ", ", "")

    logger.error(
      s"Write was unacknowledged when attempting '$operation' operation. " +
        s"write concern: ${collection.writeConcern}$contextString]"
    )

    MongoError.WriteUnacknowledgedError

  private def retryOperation[R](
    operationName: String,
    context:       Map[String, String]
  )(
    op: => Future[Either[MongoError, R]]
  ): EitherT[Future, MongoError, R] =
    def attempt(): Future[Either[MongoError, R]] =
      op.recover { case MongoNonRetryable(me) =>
        Left(MongoError.UnexpectedError(me))
      }

    EitherT(
      RetrySupport
        .retry(
          attempt = attempt,
          attempts = appConfig.mongoRetryAttempts
        )
        .recover { case NonFatal(ex) =>
          val ctx: String =
            if context.isEmpty then ""
            else context.map { case (k, v) => s"$k=$v" }.mkString(" ", " ", "")

          logger.error(
            s"$operationName failed after ${appConfig.mongoRetryAttempts + 1} attempts$ctx: " +
              s"${ex.getClass.getSimpleName}: ${ex.getMessage}"
          )

          Left(MongoError.UnexpectedError(ex))
        }
    )

  private object MongoNonRetryable:
    def unapply(t: Throwable): Option[Throwable] =
      t match
        case _:  MongoTimeoutException                                           => None
        case _:  MongoSocketException                                            => None
        case _:  MongoNotPrimaryException                                        => None
        case me: MongoException if me.hasErrorLabel("RetryableWriteError")       => None
        case me: MongoException if me.hasErrorLabel("TransientTransactionError") => None
        case _ => Some(t)
