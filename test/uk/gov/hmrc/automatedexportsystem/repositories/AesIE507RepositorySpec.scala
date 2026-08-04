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

import cats.data.NonEmptyList
import org.mockito.Mockito.when
import org.mongodb.scala.model.{Filters, Indexes}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.EitherValues
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import uk.gov.hmrc.automatedexportsystem.config.AppConfig
import uk.gov.hmrc.automatedexportsystem.errors.MongoError
import uk.gov.hmrc.automatedexportsystem.generators.MongoAesIE507MessageGenerator
import uk.gov.hmrc.automatedexportsystem.models.aesIE507.{EoriNumber, SubmissionId}
import uk.gov.hmrc.automatedexportsystem.models.mongo.write.MongoAesIE507Message
import uk.gov.hmrc.automatedexportsystem.models.response.SubmissionListItem
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.util.UUID
import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext

class AesIE507RepositorySpec
    extends AnyFreeSpecLike,
      Matchers,
      EitherValues,
      MockitoSugar,
      ScalaCheckDrivenPropertyChecks,
      MongoAesIE507MessageGenerator,
      DefaultPlayMongoRepositorySupport[MongoAesIE507Message]:
  given ec: ExecutionContext = ExecutionContext.global

  val appConfig: AppConfig = mock[AppConfig]

  when(appConfig.replaceIndexes).thenReturn(true)
  when(appConfig.documentTtl).thenReturn(1L)

  protected val repository: AesIE507RepositoryImpl = AesIE507RepositoryImpl(mongoComponent, appConfig)

  object TestData:
    val submissionId: SubmissionId = SubmissionId(UUID.fromString("6fb33641-6dc7-4a4f-adef-06238c13a317"))
    val eoriNumber:   EoriNumber   = EoriNumber("eoriNumber")

    extension (mongoAesIE507MessageGen: Gen[MongoAesIE507Message])
      def withEori(eoriNumber: EoriNumber): Gen[MongoAesIE507Message] =
        mongoAesIE507MessageGen.map(_.copy(eoriNumber = eoriNumber))

      def withSubmissionId(submissionId: SubmissionId): Gen[MongoAesIE507Message] =
        mongoAesIE507MessageGen.map(_.copy(submissionId = submissionId))

  "AesIE507Repository" - {
    import helpers.GenHelpers.*

    "should have the expected TTL associated with the updatedAt index" in {
      repository.indexes.head.getKeys shouldBe Indexes.ascending("updatedAt")

      repository.indexes.head.getOptions.getExpireAfter(TimeUnit.SECONDS) shouldBe 1L
    }

    "should be able to insert and retrieve documents" in
      forAll { (message: MongoAesIE507Message) =>
        insert(message).futureValue

        find(Filters.eq("submissionId", message.submissionId.value.toString)).futureValue shouldBe Seq(message)
      }

    ".getMessages" - {

      "should return all documents with the given eori from the collection" - {

        "when there is only one document in the collection with that eori" in {
          val mongoAesIE507MessagesDifferentEori: Seq[MongoAesIE507Message] =
            Seq.fill(5)(arbitrary[MongoAesIE507Message].sample).flatten

          val mongoAesIE507MessagesMatchingEori: Seq[MongoAesIE507Message] =
            Seq.fill(1)(arbitrary[MongoAesIE507Message].withEori(TestData.eoriNumber).sample).flatten

          val mongoAesIE507Messages: Seq[MongoAesIE507Message] =
            mongoAesIE507MessagesDifferentEori ++ mongoAesIE507MessagesMatchingEori

          repository.collection.insertMany(mongoAesIE507Messages).head().futureValue

          val messages: Seq[SubmissionListItem] =
            repository.getMessages(TestData.eoriNumber).value.futureValue.value

          messages.length shouldBe 1
          messages        shouldBe SubmissionListItem
        }

        "when there are multiple documents in the collection with that eori" in {
          val mongoAesIE507MessagesDifferentEori: Seq[MongoAesIE507Message] =
            Seq.fill(5)(arbitrary[MongoAesIE507Message].sample).flatten

          val mongoAesIE507MessagesMatchingEori: Seq[MongoAesIE507Message] =
            Seq.fill(5)(arbitrary[MongoAesIE507Message].withEori(TestData.eoriNumber).sample).flatten

          val mongoAesIE507Messages: Seq[MongoAesIE507Message] =
            mongoAesIE507MessagesDifferentEori ++ mongoAesIE507MessagesMatchingEori

          repository.collection.insertMany(mongoAesIE507Messages).head().futureValue

          val messages: Seq[SubmissionListItem] =
            repository.getMessages(TestData.eoriNumber).value.futureValue.value

          messages.length shouldBe 5
          messages.toList   should contain theSameElementsAs mongoAesIE507MessagesMatchingEori
        }
      }

      "should return a MongoError" - {

        "when there are no documents in the collection with that eori" in {
          val mongoAesIE507MessagesDifferentEori: Seq[MongoAesIE507Message] =
            Seq.fill(5)(arbitrary[MongoAesIE507Message].sample).flatten

          repository.collection.insertMany(mongoAesIE507MessagesDifferentEori).head().futureValue

          val result: MongoError =
            repository.getMessages(TestData.eoriNumber).value.futureValue.left.value

          val error: MongoError = MongoError.DocumentNotFound(s"No documents found for EORI: ${TestData.eoriNumber.value}")

          result shouldBe error
        }
      }
    }

    ".getMessage" - {

      "should return a single document with the given eori and id" - {

        "when there is a document in the collection with that eori and id" in {
          val mongoAesIE507MessagesDifferentEoriAndId: Seq[MongoAesIE507Message] =
            Seq.fill(10)(arbitrary[MongoAesIE507Message].sample).flatten

          val mongoAesIE507MessagesMatchingEoriAndId: Seq[MongoAesIE507Message] =
            Seq
              .fill(1)(
                arbitrary[MongoAesIE507Message]
                  .withEori(TestData.eoriNumber)
                  .withSubmissionId(TestData.submissionId)
                  .sample
              )
              .flatten

          val mongoAesIE507Messages: Seq[MongoAesIE507Message] =
            mongoAesIE507MessagesDifferentEoriAndId ++ mongoAesIE507MessagesMatchingEoriAndId

          repository.collection.insertMany(mongoAesIE507Messages).head().futureValue

          val result: MongoAesIE507Message =
            repository.getMessage(TestData.eoriNumber, TestData.submissionId).value.futureValue.value

          Seq(result) shouldBe mongoAesIE507MessagesMatchingEoriAndId
        }
      }

      "should return a MongoError" - {

        "when there are no documents in the collection with that eori and id" in {
          val mongoAesIE507MessagesDifferentEoriAndId: Seq[MongoAesIE507Message] =
            Seq.fill(10)(arbitrary[MongoAesIE507Message].sample).flatten

          repository.collection.insertMany(mongoAesIE507MessagesDifferentEoriAndId).head().futureValue

          val result: MongoError =
            repository.getMessage(TestData.eoriNumber, TestData.submissionId).value.futureValue.left.value

          result shouldBe MongoError.DocumentNotFound(
            s"No document found for EORI: ${TestData.eoriNumber.value} " +
              s"and submissionId: ${TestData.submissionId.value}"
          )
        }

        "when there are documents in the collection with that eori but different id" in {
          val mongoAesIE507MessagesDifferentId: Seq[MongoAesIE507Message] =
            Seq.fill(10)(arbitrary[MongoAesIE507Message].withEori(TestData.eoriNumber).sample).flatten

          repository.collection.insertMany(mongoAesIE507MessagesDifferentId).head().futureValue

          val result: MongoError =
            repository.getMessage(TestData.eoriNumber, TestData.submissionId).value.futureValue.left.value

          result shouldBe MongoError.DocumentNotFound(
            s"No document found for EORI: ${TestData.eoriNumber.value} " +
              s"and submissionId: ${TestData.submissionId.value}"
          )
        }

        "when there is a document in the collection with that id but different eori" in {
          val mongoAesIE507MessagesDifferentEori: Seq[MongoAesIE507Message] =
            Seq.fill(1)(arbitrary[MongoAesIE507Message].withSubmissionId(TestData.submissionId).sample).flatten

          repository.collection.insertMany(mongoAesIE507MessagesDifferentEori).head().futureValue

          val result: MongoError =
            repository.getMessage(TestData.eoriNumber, TestData.submissionId).value.futureValue.left.value

          result shouldBe MongoError.DocumentNotFound(
            s"No document found for EORI: ${TestData.eoriNumber.value} " +
              s"and submissionId: ${TestData.submissionId.value}"
          )
        }
      }
    }
  }
