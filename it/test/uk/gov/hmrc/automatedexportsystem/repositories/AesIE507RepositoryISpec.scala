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
import org.mongodb.scala.ObservableFuture
import org.mongodb.scala.model.{Filters, Indexes}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.EitherValues
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import uk.gov.hmrc.automatedexportsystem.config.AppConfig
import uk.gov.hmrc.automatedexportsystem.errors.MongoError
import uk.gov.hmrc.automatedexportsystem.generators.MongoAesIE507MessageGenerator
import uk.gov.hmrc.automatedexportsystem.models.IE507.aes.SubmissionId
import uk.gov.hmrc.automatedexportsystem.models.IE507.{EoriNumber, ExportOperationType}
import uk.gov.hmrc.automatedexportsystem.models.mongo.UpdateStatus
import uk.gov.hmrc.automatedexportsystem.models.mongo.read.MongoAesIE507MessageSummary
import uk.gov.hmrc.automatedexportsystem.models.mongo.write.MongoAesIE507Message
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.time.Instant
import java.util.UUID
import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext

class AesIE507RepositoryISpec
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
    val instant: Instant = Instant.parse("2026-08-17T00:00:00.000Z")

    val submissionId: SubmissionId = SubmissionId(UUID.fromString("6fb33641-6dc7-4a4f-adef-06238c13a317"))

    val eoriNumber: EoriNumber = EoriNumber("eoriNumber")

    def mongoAesIE507MessageSummary(mongoAesIE507Message: MongoAesIE507Message) =
      MongoAesIE507MessageSummary(
        submissionId = mongoAesIE507Message.submissionId,
        exportOperation = mongoAesIE507Message.exportOperation,
        customsOfficeOfExitActual = mongoAesIE507Message.customsOfficeOfExitActual,
        ducr = mongoAesIE507Message.goodsShipment.map(_.consignment.referenceNumberUCR),
        updatedAt = mongoAesIE507Message.updatedAt
      )

    extension (mongoAesIE507MessageGen: Gen[MongoAesIE507Message])
      def withEori(eoriNumber: EoriNumber): Gen[MongoAesIE507Message] =
        mongoAesIE507MessageGen.map(_.copy(eoriNumber = eoriNumber))

      def withSubmissionId(submissionId: SubmissionId): Gen[MongoAesIE507Message] =
        mongoAesIE507MessageGen.map(_.copy(submissionId = submissionId))

  "AesIE507Repository" - {
    import helpers.GenHelpers.*

    "should have the expected TTL associated with the updatedAt index" in {
      val ttlIndex = repository.indexes.find(_.getKeys == Indexes.ascending("updatedAt")).head
      ttlIndex.getOptions.getExpireAfter(TimeUnit.SECONDS) shouldBe appConfig.documentTtl
    }

    "should be able to insert and retrieve documents" in
      forAll { (message: MongoAesIE507Message) =>
        insert(message).futureValue

        find(
          Filters.and(
            Filters.eq("eoriNumber", message.eoriNumber.value),
            Filters.eq("submissionId", message.submissionId.value.toString)
          )
        ).futureValue shouldBe Seq(message)
      }

    "submit should upsert on same submissionId and keep one document" in
      forAll { (message1: MongoAesIE507Message) =>
        val message2 = message1.copy(updatedAt = message1.updatedAt.plusSeconds(30))

        repository.submit(message1).value.futureValue shouldBe Right(true)
        repository.submit(message2).value.futureValue shouldBe Right(true)

        val docs = repository.collection
          .find(Filters.eq("submissionId", message1.submissionId.value.toString))
          .toFuture()
          .futureValue

        docs.size shouldBe 1
        docs.head shouldBe message2
      }

    ".getMessages" - {

      "should return all documents with the given eori from the collection" - {

        "when there is only one document in the collection with that eori" in {
          val mongoAesIE507MessagesDifferentEori: Seq[MongoAesIE507Message] =
            Seq.fill(2)(arbitrary[MongoAesIE507Message].sample).flatten

          val mongoAesIE507MessagesMatchingEori: Seq[MongoAesIE507Message] =
            Seq.fill(1)(arbitrary[MongoAesIE507Message].withEori(TestData.eoriNumber).sample).flatten

          val mongoAesIE507Messages: Seq[MongoAesIE507Message] =
            mongoAesIE507MessagesDifferentEori ++ mongoAesIE507MessagesMatchingEori

          val mongoAesIE507MessageSummaries: Seq[MongoAesIE507MessageSummary] =
            mongoAesIE507MessagesMatchingEori.map(TestData.mongoAesIE507MessageSummary)

          repository.collection.insertMany(mongoAesIE507Messages).head().futureValue

          val result: NonEmptyList[MongoAesIE507MessageSummary] =
            repository.getMessages(TestData.eoriNumber).value.futureValue.value

          result.length shouldBe 1
          result.toList shouldBe mongoAesIE507MessageSummaries
        }

        "when there are multiple documents in the collection with that eori" in {
          val mongoAesIE507MessagesDifferentEori: Seq[MongoAesIE507Message] =
            Seq.fill(2)(arbitrary[MongoAesIE507Message].sample).flatten

          val mongoAesIE507MessagesMatchingEori: Seq[MongoAesIE507Message] =
            Seq.fill(2)(arbitrary[MongoAesIE507Message].withEori(TestData.eoriNumber).sample).flatten

          val mongoAesIE507Messages: Seq[MongoAesIE507Message] =
            mongoAesIE507MessagesDifferentEori ++ mongoAesIE507MessagesMatchingEori

          val mongoAesIE507MessageSummaries: Seq[MongoAesIE507MessageSummary] =
            mongoAesIE507MessagesMatchingEori.map(TestData.mongoAesIE507MessageSummary)

          repository.collection.insertMany(mongoAesIE507Messages).head().futureValue

          val result: NonEmptyList[MongoAesIE507MessageSummary] =
            repository.getMessages(TestData.eoriNumber).value.futureValue.value

          result.length shouldBe 2
          result.toList   should contain theSameElementsAs mongoAesIE507MessageSummaries
        }
      }

      "should return a MongoError" - {

        "when there are no documents in the collection with that eori" in {
          val mongoAesIE507MessagesDifferentEori: Seq[MongoAesIE507Message] =
            Seq.fill(3)(arbitrary[MongoAesIE507Message].sample).flatten

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
            Seq.fill(2)(arbitrary[MongoAesIE507Message].sample).flatten

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
            Seq.fill(3)(arbitrary[MongoAesIE507Message].sample).flatten

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
            Seq.fill(3)(arbitrary[MongoAesIE507Message].withEori(TestData.eoriNumber).sample).flatten

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

    ".cancel" - {

      "should set the document's ExportOperationType to Cancel" - {

        "when there is a document in the collection with that eori and submissionId" - {

          "and ExportOperationType is not Cancel" in {
            val mongoAesIE507MessagesDifferentEoriAndId: Seq[MongoAesIE507Message] =
              Seq.fill(2)(arbitrary[MongoAesIE507Message].sample).flatten

            val mongoAesIE507MessagesMatchingEoriAndId: Seq[MongoAesIE507Message] =
              Seq
                .fill(1)(
                  arbitrary[MongoAesIE507Message]
                    .withEori(TestData.eoriNumber)
                    .withSubmissionId(TestData.submissionId)
                    .withExportOperationType(ExportOperationType.Standard)
                    .sample
                )
                .flatten

            val mongoAesIE507Messages: Seq[MongoAesIE507Message] =
              mongoAesIE507MessagesDifferentEoriAndId ++ mongoAesIE507MessagesMatchingEoriAndId

            val mongoAesIE507MessagesMatchingEoriAndIdCancelled: Seq[MongoAesIE507Message] =
              mongoAesIE507MessagesMatchingEoriAndId.map(m =>
                m.copy(
                  exportOperation = m.exportOperation.copy(exportOperationType = ExportOperationType.Cancel),
                  updatedAt = TestData.instant
                )
              )

            repository.collection.insertMany(mongoAesIE507Messages).head().futureValue

            val updateStatus: UpdateStatus =
              repository.cancel(TestData.eoriNumber, TestData.submissionId, TestData.instant).value.futureValue.value

            updateStatus shouldBe UpdateStatus.Updated("cancel", 1, 1)

            val result: Seq[MongoAesIE507Message] =
              find(
                Filters.and(
                  Filters.eq("eoriNumber", TestData.eoriNumber.value),
                  Filters.eq("submissionId", TestData.submissionId.value.toString)
                )
              ).futureValue

            result shouldBe mongoAesIE507MessagesMatchingEoriAndIdCancelled
          }

          "and ExportOperationType is Cancel" in {
            val mongoAesIE507MessagesDifferentEoriAndId: Seq[MongoAesIE507Message] =
              Seq.fill(2)(arbitrary[MongoAesIE507Message].sample).flatten

            val mongoAesIE507MessagesMatchingEoriAndId: Seq[MongoAesIE507Message] =
              Seq
                .fill(1)(
                  arbitrary[MongoAesIE507Message]
                    .withSubmissionId(TestData.submissionId)
                    .withEori(TestData.eoriNumber)
                    .withExportOperationType(ExportOperationType.Cancel)
                    .sample
                )
                .flatten

            val mongoAesIE507Messages: Seq[MongoAesIE507Message] =
              mongoAesIE507MessagesDifferentEoriAndId ++ mongoAesIE507MessagesMatchingEoriAndId

            repository.collection.insertMany(mongoAesIE507Messages).head().futureValue

            val updateStatus: UpdateStatus =
              repository.cancel(TestData.eoriNumber, TestData.submissionId, TestData.instant).value.futureValue.value

            updateStatus shouldBe UpdateStatus.AlreadyUpToDate("cancel", 1)

            val result: Seq[MongoAesIE507Message] =
              find(Filters.eq("submissionId", TestData.submissionId.value.toString)).futureValue

            result shouldBe mongoAesIE507MessagesMatchingEoriAndId
          }
        }
      }

      "should return a MongoError" - {

        "when there is no document in the collection with that submissionId" in {
          val mongoAesIE507MessagesDifferentId: Seq[MongoAesIE507Message] =
            Seq.fill(3)(arbitrary[MongoAesIE507Message].sample).flatten

          repository.collection.insertMany(mongoAesIE507MessagesDifferentId).head().futureValue

          val result: MongoError =
            repository.cancel(TestData.eoriNumber, TestData.submissionId, TestData.instant).value.futureValue.left.value

          result shouldBe MongoError.DocumentNotFound(
            s"No document found for submissionId: ${TestData.submissionId.value}"
          )
        }
      }
    }
  }
