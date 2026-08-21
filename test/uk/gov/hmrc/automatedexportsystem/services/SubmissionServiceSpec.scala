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

package uk.gov.hmrc.automatedexportsystem.services

import cats.data.{EitherT, NonEmptyList}
import org.mockito.Mockito.when
import org.scalatest.EitherValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.automatedexportsystem.errors.{MongoError, SubmissionServiceError}
import uk.gov.hmrc.automatedexportsystem.models.IE507.*
import uk.gov.hmrc.automatedexportsystem.models.mongo.UpdateStatus
import uk.gov.hmrc.automatedexportsystem.models.mongo.read.MongoAesIE507MessageSummary
import uk.gov.hmrc.automatedexportsystem.models.mongo.write.MongoAesIE507Message
import uk.gov.hmrc.automatedexportsystem.models.responses.{Submission, SubmissionSummary, SubmissionSummaryList}
import uk.gov.hmrc.automatedexportsystem.repositories.AesIE507Repository

import java.time.*
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class SubmissionServiceSpec extends AnyFreeSpecLike, Matchers, EitherValues, ScalaFutures, MockitoSugar:
  given ec: ExecutionContext = ExecutionContext.global

  val aesIE507Repository: AesIE507Repository = mock[AesIE507Repository]

  val instant: Instant = Instant.parse("2026-08-12T00:00:00.000Z")

  val clock: Clock = Clock.fixed(instant, ZoneOffset.UTC)

  val submissionService: SubmissionService = SubmissionServiceImpl(aesIE507Repository, clock)

  object TestData:
    val eoriNumber: EoriNumber = EoriNumber("eoriNumber")

    val submissionId: SubmissionId =
      SubmissionId(UUID.fromString("6fb33641-6dc7-4a4f-adef-06238c13a317"))

    val instant: Instant = Instant.parse("2026-08-12T00:00:00.000Z")

    val dateTime: LocalDateTime = LocalDateTime.parse("2026-08-12T00:00:00")

    def mongoAesIE507Message(submissionId: SubmissionId, eoriNumber: EoriNumber): MongoAesIE507Message =
      MongoAesIE507Message(
        submissionId = submissionId,
        eoriNumber = eoriNumber,
        createdAt = instant,
        updatedAt = instant,
        exportOperation = ExportOperation(
          exportOperationType = ExportOperationType.Standard,
          mrn = Mrn("mrn"),
          discrepanciesExist = DiscrepanciesExist(true),
          splitIndicator = SplitIndicator(true)
        ),
        customsOfficeOfExitActual = CustomsOfficeOfExitActual(
          referenceNumber = ReferenceNumber("referenceNumber")
        ),
        goodsShipment = None
      )

    def mongoAesIE507MessageSummary(submissionId: SubmissionId): MongoAesIE507MessageSummary =
      MongoAesIE507MessageSummary(
        submissionId = submissionId,
        exportOperation = ExportOperation(
          exportOperationType = ExportOperationType.Standard,
          mrn = Mrn("mrn"),
          discrepanciesExist = DiscrepanciesExist(true),
          splitIndicator = SplitIndicator(true)
        ),
        customsOfficeOfExitActual = CustomsOfficeOfExitActual(
          referenceNumber = ReferenceNumber("referenceNumber")
        ),
        ducr = None,
        updatedAt = instant
      )

    def submissionSummary(submissionId: SubmissionId): SubmissionSummary =
      SubmissionSummary(
        submissionId = submissionId,
        mrn = Mrn("mrn"),
        ducr = None,
        officeOfExitCode = ReferenceNumber("referenceNumber"),
        updatedAt = dateTime,
        status = ExportOperationType.Standard
      )
  end TestData

  "SubmissionService" - {

    ".getSubmissions" - {

      "should return a list of submissions" - {

        "when no submission with the given EORI can be found in the mongodb collection" in {
          when(aesIE507Repository.getMessages(TestData.eoriNumber))
            .thenReturn(EitherT(Future.successful(Left(MongoError.DocumentNotFound("")))))

          val result: SubmissionSummaryList =
            submissionService.getSubmissions(TestData.eoriNumber).value.futureValue.value

          result.submissions shouldBe Nil
        }

        "when there are multiple submissions with the given EORI in the mongodb collection" in {
          val mongoAesIE507MessageSummaries: Seq[MongoAesIE507MessageSummary] =
            Seq.fill(3)(TestData.mongoAesIE507MessageSummary(TestData.submissionId))

          val submissionSummaryList: List[SubmissionSummary] =
            List.fill(3)(TestData.submissionSummary(TestData.submissionId))

          when(aesIE507Repository.getMessages(TestData.eoriNumber))
            .thenReturn(
              EitherT(
                Future.successful(
                  Right(
                    NonEmptyList.of(
                      mongoAesIE507MessageSummaries.head,
                      mongoAesIE507MessageSummaries.tail*
                    )
                  )
                )
              )
            )

          val result: SubmissionSummaryList =
            submissionService.getSubmissions(TestData.eoriNumber).value.futureValue.value

          result.submissions shouldBe submissionSummaryList
        }
      }

      "should return an error" - {

        "when the retrieval operation returns an unexpected error" in {
          when(aesIE507Repository.getMessages(TestData.eoriNumber))
            .thenReturn(EitherT(Future.successful(Left(MongoError.UnexpectedError(Exception())))))

          val error: SubmissionServiceError =
            SubmissionServiceError.SubmissionOperationFailure(
              s"Submission retrieval failed. EORI: ${TestData.eoriNumber.value}"
            )

          val result: SubmissionServiceError =
            submissionService.getSubmissions(TestData.eoriNumber).value.futureValue.left.value

          result shouldBe error
        }
      }
    }

    ".getSubmission" - {

      "should return a single submission" - {

        "when one submission with the given EORI and submissionId is found in the mongodb collection" in {
          val mongoAesIE507Messages: Seq[MongoAesIE507Message] =
            Seq(TestData.mongoAesIE507Message(TestData.submissionId, TestData.eoriNumber))

          when(aesIE507Repository.getMessage(TestData.eoriNumber, TestData.submissionId))
            .thenReturn(EitherT(Future.successful(Right(mongoAesIE507Messages.head))))

          val result: Submission =
            submissionService.getSubmission(TestData.eoriNumber, TestData.submissionId).value.futureValue.value

          result.submissionId shouldBe TestData.submissionId
        }
      }

      "should return an error" - {

        "when there are no submissions with the given EORI and submissionId found in the mongodb collection" in {
          when(aesIE507Repository.getMessage(TestData.eoriNumber, TestData.submissionId))
            .thenReturn(EitherT(Future.successful(Left(MongoError.DocumentNotFound("")))))

          val error: SubmissionServiceError =
            SubmissionServiceError.SubmissionNotFound(
              s"Submission not found. EORI: ${TestData.eoriNumber.value}, " +
                s"submissionId: ${TestData.submissionId.value.toString}"
            )

          val result: SubmissionServiceError =
            submissionService.getSubmission(TestData.eoriNumber, TestData.submissionId).value.futureValue.left.value

          result shouldBe error
        }

        "when the retrieval operation returns an unexpected error" in {
          when(aesIE507Repository.getMessage(TestData.eoriNumber, TestData.submissionId))
            .thenReturn(EitherT(Future.successful(Left(MongoError.UnexpectedError(Exception())))))

          val error: SubmissionServiceError =
            SubmissionServiceError.SubmissionOperationFailure(
              s"Submission retrieval failed. EORI: ${TestData.eoriNumber.value}, " +
                s"submissionId: ${TestData.submissionId.value.toString}"
            )

          val result: SubmissionServiceError =
            submissionService.getSubmission(TestData.eoriNumber, TestData.submissionId).value.futureValue.left.value

          result shouldBe error
        }
      }
    }

    ".cancelSubmission" - {

      "should cancel a submission" - {

        "when a submission with the given EORI and submissionId is found in the mongodb collection" - {

          "and the submission is not cancelled yet" in {
            when(aesIE507Repository.cancel(TestData.eoriNumber, TestData.submissionId, instant))
              .thenReturn(EitherT(Future.successful(Right(UpdateStatus.Updated("cancel", 1, 1)))))

            val result: UpdateStatus =
              submissionService.cancelSubmission(TestData.eoriNumber, TestData.submissionId).value.futureValue.value

            result shouldBe UpdateStatus.Updated("cancel", 1, 1)
          }

          "and the submission is already cancelled" in {
            when(aesIE507Repository.cancel(TestData.eoriNumber, TestData.submissionId, instant))
              .thenReturn(EitherT(Future.successful(Right(UpdateStatus.AlreadyUpToDate("cancel", 1)))))

            val result: UpdateStatus =
              submissionService.cancelSubmission(TestData.eoriNumber, TestData.submissionId).value.futureValue.value

            result shouldBe UpdateStatus.AlreadyUpToDate("cancel", 1)
          }
        }
      }

      "should return an error" - {

        "when there is no submission with the given submissionId found in the mongodb collection" in {
          when(aesIE507Repository.cancel(TestData.eoriNumber, TestData.submissionId, instant))
            .thenReturn(EitherT(Future.successful(Left(MongoError.DocumentNotFound("")))))

          val error: SubmissionServiceError =
            SubmissionServiceError.SubmissionNotFound(
              s"Submission not found. EORI: ${TestData.eoriNumber.value}, " +
                s"submissionId: ${TestData.submissionId.value}"
            )

          val result: SubmissionServiceError =
            submissionService.cancelSubmission(TestData.eoriNumber, TestData.submissionId).value.futureValue.left.value

          result shouldBe error
        }

        "when the update operation returns an unexpected error" in {
          when(aesIE507Repository.cancel(TestData.eoriNumber, TestData.submissionId, instant))
            .thenReturn(EitherT(Future.successful(Left(MongoError.UnexpectedError(Exception())))))

          val error: SubmissionServiceError =
            SubmissionServiceError.SubmissionOperationFailure(
              s"Submission update failed. EORI: ${TestData.eoriNumber.value}, " +
                s"submissionId: ${TestData.submissionId.value}"
            )

          val result: SubmissionServiceError =
            submissionService.cancelSubmission(TestData.eoriNumber, TestData.submissionId).value.futureValue.left.value

          result shouldBe error
        }
      }
    }
  }
