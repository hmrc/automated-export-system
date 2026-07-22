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

import cats.data.EitherT
import com.google.inject.ImplementedBy
import com.mongodb.client.model.{IndexModel, IndexOptions}
import org.apache.pekko.pattern.RetrySupport
import org.mongodb.scala.AggregateObservable
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.{Filters, Indexes}
import play.api.Logging
import uk.gov.hmrc.automatedexportsystem.config.AppConfig
import uk.gov.hmrc.automatedexportsystem.errors.MongoError
import uk.gov.hmrc.automatedexportsystem.models.aesRequest.{EoriNumber, SubmissionId}
import uk.gov.hmrc.automatedexportsystem.models.mongo.write.MongoAesIE507Message
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag
import scala.util.control.NonFatal

@ImplementedBy(classOf[AesIE507RepositoryImpl])
trait AesIE507Repository:
  def getMessages(eori: EoriNumber): EitherT[Future, MongoError, Seq[MongoAesIE507Message]]

  def getMessage(eori: EoriNumber, submissionId: SubmissionId): EitherT[Future, MongoError, MongoAesIE507Message]

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
          Indexes.ascending("updatedAt"),
          IndexOptions().expireAfter(appConfig.documentTtl, TimeUnit.SECONDS)
        ),
        IndexModel(
          Indexes.compoundIndex(
            Indexes.ascending("eoriNumber"),
            Indexes.ascending("submissionId")
          )
        )
      )
    ),
      AesIE507Repository,
      Logging:
  def getMessages(eori: EoriNumber): EitherT[Future, MongoError, Seq[MongoAesIE507Message]] =
    val filter: Bson = Filters.eq("eoriNumber", eori.value)

    val pipeline: Seq[Bson] = Seq(filter)

    retryPipeline(pipeline)((obs: AggregateObservable[MongoAesIE507Message]) =>
      obs
        .toFuture()
        .map {
          case seq if seq.isEmpty =>
            Left(MongoError.DocumentNotFound(s"No documents found for EORI: ${eori.value}"))
          case seq => Right(seq)
        }
        .recover { case NonFatal(ex) =>
          Left(MongoError.UnexpectedError(ex))
        }
    )

  def getMessage(eori: EoriNumber, submissionId: SubmissionId): EitherT[Future, MongoError, MongoAesIE507Message] =
    val filter: Bson = Filters.and(
      Filters.eq("eoriNumber", eori.value),
      Filters.eq("submissionId", submissionId.value)
    )

    val pipeline: Seq[Bson] = Seq(filter)

    retryPipeline(pipeline)((obs: AggregateObservable[MongoAesIE507Message]) =>
      obs
        .headOption()
        .map(opt =>
          opt.toRight(
            MongoError.DocumentNotFound(
              s"No document found for EORI: ${eori.value} " +
                s"and submissionId: ${submissionId.value}"
            )
          )
        )
    )

  private def retryPipeline[T: ClassTag, R](
    pipeline: => Seq[Bson]
  )(transform: AggregateObservable[T] => Future[Either[MongoError, R]]): EitherT[Future, MongoError, R] = {
    def func(): Future[Either[MongoError, R]] =
      transform(collection.aggregate[T](pipeline))

    EitherT(
      RetrySupport.retry(
        attempt = func,
        attempts = appConfig.mongoRetryAttempts
      )
    )
  }
