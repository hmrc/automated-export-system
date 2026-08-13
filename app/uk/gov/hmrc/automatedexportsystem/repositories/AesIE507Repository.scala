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
import com.mongodb.client.model.{IndexModel, IndexOptions, Projections, Sorts}
import com.mongodb.{MongoNotPrimaryException, MongoSocketException, MongoTimeoutException}
import org.apache.pekko.pattern.RetrySupport
import org.mongodb.scala.MongoException
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.{Aggregates, Filters, Indexes, ReplaceOptions}
import play.api.Logging
import uk.gov.hmrc.automatedexportsystem.config.AppConfig
import uk.gov.hmrc.automatedexportsystem.errors.MongoError
import uk.gov.hmrc.automatedexportsystem.models.aesIE507.{EoriNumber, SubmissionId}
import uk.gov.hmrc.automatedexportsystem.models.mongo.write.MongoAesIE507Message
import uk.gov.hmrc.automatedexportsystem.models.responses.SubmissionSummary
import uk.gov.hmrc.mongo.MongoComponent

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import org.bson.codecs.Codec
import play.api.libs.json.OFormat
import uk.gov.hmrc.automatedexportsystem.errors.MongoError.UnexpectedError
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}

@ImplementedBy(classOf[AesIE507RepositoryImpl])
trait AesIE507Repository:
  def getMessages(eori:  EoriNumber):                             EitherT[Future, MongoError, NonEmptyList[SubmissionSummary]]
  def getMessage(eori:   EoriNumber, submissionId: SubmissionId): EitherT[Future, MongoError, MongoAesIE507Message]
  def submit(submission: MongoAesIE507Message):                   EitherT[Future, MongoError, Boolean]

@Singleton
class AesIE507RepositoryImpl @Inject() (
  mongo:     MongoComponent,
  appConfig: AppConfig
)(using protected val executionContext: ExecutionContext)
    extends PlayMongoRepository[MongoAesIE507Message](
      collectionName = "aes-ie507",
      mongoComponent = mongo,
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
      extraCodecs = Seq[Codec[?]](
        Codecs.playFormatCodec[SubmissionSummary](summon[OFormat[SubmissionSummary]])
      )
    ),
      AesIE507Repository,
      Logging:

  override def getMessages(eori: EoriNumber): EitherT[Future, MongoError, NonEmptyList[SubmissionSummary]] =
    val pipeline: Seq[Bson] = Seq(
      Aggregates.filter(Filters.eq("eoriNumber", eori.value)),
      Aggregates.project(
        Projections.fields(
          Projections.computed("submissionId", "$submissionId"),
          Projections.computed("ducr", "$goodsShipment.consignment.referenceNumberUCR"),
          Projections.computed("mrn", "$exportOperation.mrn"),
          Projections.computed("officeOfExitCode", "$customsOfficeOfExitActual.referenceNumber"),
          Projections.computed("status", "$exportOperation.exportOperationType"),
          Projections.computed("updatedAt", "$updatedAt"),
          Projections.excludeId()
        )
      ),
      Aggregates.sort(Sorts.descending("lastUpdated"))
    )

    val op: Future[Either[MongoError, NonEmptyList[SubmissionSummary]]] =
      collection
        .aggregate[SubmissionSummary](pipeline)
        .toFuture()
        .map { summaries =>
          NonEmptyList
            .fromList(summaries.toList)
            .toRight(
              MongoError.DocumentNotFound(s"No documents found for EORI: ${eori.value}")
            )
        }

    retryPipeline("getMessages", Map("eori" -> eori.value))(op)

  override def getMessage(
    eori:         EoriNumber,
    submissionId: SubmissionId
  ): EitherT[Future, MongoError, MongoAesIE507Message] =
    val op: Future[Either[MongoError, MongoAesIE507Message]] =
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

    retryPipeline(
      operationName = "getMessage",
      context = Map("eoriNumber" -> eori.value, "submissionId" -> submissionId.value.toString)
    )(op)

  override def submit(submission: MongoAesIE507Message): EitherT[Future, MongoError, Boolean] =
    val sid = submission.submissionId.value.toString

    val op: Future[Either[MongoError, Boolean]] =
      collection
        .replaceOne(
          Filters.eq("submissionId", sid),
          submission,
          ReplaceOptions().upsert(true)
        )
        .toFuture()
        .map: wr =>
          Right[MongoError, Boolean](wr.wasAcknowledged())
        .recover:
          case e: com.mongodb.MongoWriteException if e.getError.getCode == 11000 =>
            Left(UnexpectedError(e))
          case e =>
            Left(UnexpectedError(e))

    retryPipeline(
      operationName = "submitUpsert",
      context = Map("submissionId" -> sid)
    )(op)

  private def retryPipeline[R](
    operationName: String,
    context:       Map[String, String]
  )(
    op: => Future[Either[MongoError, R]]
  ): EitherT[Future, MongoError, R] =
    def attempt(): Future[Either[MongoError, R]] =
      op.recover {
        case me: MongoException if !MongoRetryable.isRetryable(me) =>
          Left(MongoError.UnexpectedError(me))
      }

    EitherT(
      RetrySupport
        .retry(
          attempt = attempt,
          attempts = appConfig.mongoRetryAttempts
        )
        .recover { case NonFatal(ex) =>
          val ctx =
            if context.isEmpty then ""
            else context.map { case (k, v) => s"$k=$v" }.mkString(" ", " ", "")

          logger.error(
            s"$operationName failed after ${appConfig.mongoRetryAttempts + 1} attempts$ctx: " +
              s"${ex.getClass.getSimpleName}: ${ex.getMessage}"
          )
          Left(MongoError.UnexpectedError(ex))
        }
    )

  private object MongoRetryable:
    def isRetryable(t: Throwable): Boolean = t match
      case _:  MongoTimeoutException                                           => true
      case _:  MongoSocketException                                            => true
      case _:  MongoNotPrimaryException                                        => true
      case me: MongoException if me.hasErrorLabel("RetryableWriteError")       => true
      case me: MongoException if me.hasErrorLabel("TransientTransactionError") => true
      case _ => false
