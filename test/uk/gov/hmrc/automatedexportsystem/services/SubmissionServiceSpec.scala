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
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.EitherValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.automatedexportsystem.errors.{MongoError, SubmissionServiceError}
import uk.gov.hmrc.automatedexportsystem.generators.MongoAesIE507MessageGenerator
import uk.gov.hmrc.automatedexportsystem.models.aesIE507.EoriNumber
import uk.gov.hmrc.automatedexportsystem.models.mongo.write.MongoAesIE507Message
import uk.gov.hmrc.automatedexportsystem.models.responses.{SubmissionSummary, SubmissionSummaryList}
import uk.gov.hmrc.automatedexportsystem.repositories.AesIE507Repository

import scala.concurrent.{ExecutionContext, Future}

class SubmissionServiceSpec extends AnyFreeSpecLike, Matchers, EitherValues, ScalaFutures, MockitoSugar, MongoAesIE507MessageGenerator:
  given ec: ExecutionContext = ExecutionContext.global

  val aesIE507Repository: AesIE507Repository = mock[AesIE507Repository]

  val submissionService: SubmissionService = SubmissionService(aesIE507Repository)

  "SubmissionService" - {
    import helpers.GenHelpers.*

    val eoriNumber: EoriNumber = EoriNumber("eoriNumber")

    ".getSubmissions" - {

      "should return a list of submissions" - {

        "when no submission with the given EORI can be found in the mongodb collection" in {
          when(aesIE507Repository.getMessages(eoriNumber))
            .thenReturn(EitherT(Future.successful(Left(MongoError.DocumentNotFound("")))))

          val result: SubmissionSummaryList =
            submissionService.getSubmissions(eoriNumber).value.futureValue.value

          result.submissions shouldBe Nil
        }

        "when multiple submissions with the given EORI are found in the mongodb collection" in {
          val mongoAesIE507Messages: Seq[MongoAesIE507Message] =
            Seq.fill(5)(arbitrary[MongoAesIE507Message].withEori(eoriNumber).sample).flatten

          val submissionSummaryList: List[SubmissionSummary] =
            mongoAesIE507Messages.map(SubmissionSummary.fromMongoAesIE507Message).toList

          when(aesIE507Repository.getMessages(eoriNumber))
            .thenReturn(
              EitherT(
                Future.successful(
                  Right(
                    NonEmptyList.of(
                      mongoAesIE507Messages.head,
                      mongoAesIE507Messages.tail*
                    )
                  )
                )
              )
            )

          val result: SubmissionSummaryList =
            submissionService.getSubmissions(eoriNumber).value.futureValue.value

          result.submissions shouldBe submissionSummaryList
        }
      }

      "should return an error" - {

        "when the retrieval operation returns an unexpected error" in {
          when(aesIE507Repository.getMessages(eoriNumber))
            .thenReturn(EitherT(Future.successful(Left(MongoError.UnexpectedError(Exception())))))

          val error: SubmissionServiceError =
            SubmissionServiceError.SubmissionRetrieveFailure(
              s"Submission retrieval failed for EORI: $eoriNumber"
            )

          val result: SubmissionServiceError =
            submissionService.getSubmissions(eoriNumber).value.futureValue.left.value

          result shouldBe error
        }
      }
    }
  }
