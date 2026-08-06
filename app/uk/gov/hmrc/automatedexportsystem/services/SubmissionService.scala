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
import cats.data.EitherT
import jakarta.inject.Singleton
import uk.gov.hmrc.automatedexportsystem.errors.{MongoError, SubmissionServiceError}
import uk.gov.hmrc.automatedexportsystem.models.aesIE507.{EoriNumber, ExportOperationType}
import uk.gov.hmrc.automatedexportsystem.models.request.{SubmissionRequest, SubmissionResult}
import uk.gov.hmrc.automatedexportsystem.models.responses.SubmissionSummaryList
import uk.gov.hmrc.automatedexportsystem.repositories.AesIE507Repository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait SubmissionService {
  def submitMessage(
    request:             SubmissionRequest,
    exportOperationType: ExportOperationType,
    eoriNumber:          EoriNumber
  ): EitherT[Future, MongoError, SubmissionResult]

  def getSubmissions(eoriNumber: EoriNumber): EitherT[Future, SubmissionServiceError, SubmissionSummaryList]
}

@Singleton
class SubmissionServiceImpl @Inject() (aesIE507Repository: AesIE507Repository)(using ExecutionContext) extends SubmissionService:
  override def getSubmissions(eoriNumber: EoriNumber): EitherT[Future, SubmissionServiceError, SubmissionSummaryList] =
    val submissionSummaryListResult: EitherT[Future, MongoError, SubmissionSummaryList] =
      aesIE507Repository
        .getMessages(eoriNumber)
        .map(messageNel => SubmissionSummaryList(messageNel.toList))

    submissionSummaryListResult.leftFlatMap {
      case MongoError.DocumentNotFound(_) =>
        EitherT(Future.successful(Right(SubmissionSummaryList(Nil))))
      case MongoError.UnexpectedError(_) =>
        EitherT(
          Future.successful(
            Left(
              SubmissionServiceError.SubmissionRetrieveFailure(
                s"Submission retrieval failed for EORI: ${eoriNumber.value}"
              )
            )
          )
        )
    }
  end getSubmissions

  override def submitMessage(
    request:             SubmissionRequest,
    exportOperationType: ExportOperationType,
    eoriNumber:          EoriNumber
  ): EitherT[Future, MongoError, SubmissionResult] =
    aesIE507Repository
      .submit(request.toMongoMessage(exportOperationType, eoriNumber))
      .map(created => if (created) SubmissionResult.Created else SubmissionResult.Updated)
