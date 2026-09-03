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
import uk.gov.hmrc.automatedexportsystem.config.AppConfig
import uk.gov.hmrc.automatedexportsystem.connectors.EisConnector
import uk.gov.hmrc.automatedexportsystem.errors.EisServiceError
import uk.gov.hmrc.automatedexportsystem.models.IE507.EoriNumber
import uk.gov.hmrc.automatedexportsystem.models.IE507.aes.AesIE507Message
import uk.gov.hmrc.automatedexportsystem.models.eis.{EisErrorResponse, EisIE507Request}
import uk.gov.hmrc.automatedexportsystem.models.http.HttpHeader
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EisService @Inject() (
  eisConnector:    EisConnector,
  eisIE507Factory: EisIE507Factory,
  appConfig:       AppConfig
)(using protected val ec: ExecutionContext):
  def submitMessage(
    aesIE507Message:     AesIE507Message,
    eoriNumber:          EoriNumber,
    maybeCorrelationId:  Option[HttpHeader.CorrelationId],
    maybeConversationId: Option[HttpHeader.ConversationId]
  )(using hc: HeaderCarrier): EitherT[Future, EisServiceError, Either[EisErrorResponse, Unit]] =
    val eisBearerToken: String = appConfig.eisToken

    val authorization: HttpHeader.Authorization = HttpHeader.Authorization(eisBearerToken)

    val eisIE507Request: EisIE507Request =
      eisIE507Factory.request(
        aesIE507Message,
        eoriNumber,
        authorization,
        maybeCorrelationId,
        maybeConversationId
      )

    eisConnector
      .submitMessage(eisIE507Request)
      .leftMap(_ =>
        val context: String =
          Seq(
            Some(s"EORI: ${eoriNumber.value}"),
            aesIE507Message.submissionId.map(submissionId => s"submissionId: ${submissionId.value}")
          ).flatten.mkString(", ")

        EisServiceError.SubmissionFailedError(s"Failed to submit IE507 message to EIS. $context")
      )
end EisService
