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

import helpers.EitherTFutureOps.{toEitherTLeft, toEitherTRight}
import org.mockito.Mockito.when
import org.scalatest.EitherValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.automatedexportsystem.config.AppConfig
import uk.gov.hmrc.automatedexportsystem.connectors.EisConnector
import uk.gov.hmrc.automatedexportsystem.errors.{ConnectorError, EisServiceError}
import uk.gov.hmrc.automatedexportsystem.models.IE507.*
import uk.gov.hmrc.automatedexportsystem.models.IE507.aes.{AesIE507Message, SubmissionId}
import uk.gov.hmrc.automatedexportsystem.models.IE507.eis.*
import uk.gov.hmrc.automatedexportsystem.models.eis.{EisErrorResponse, EisIE507Request, EisIE507RequestHeaders, SourceFaultDetail}
import uk.gov.hmrc.automatedexportsystem.models.http.HttpHeader
import uk.gov.hmrc.http.HeaderCarrier

import java.time.{Instant, LocalDateTime}
import java.util.UUID
import scala.concurrent.ExecutionContext

class EisServiceSpec extends AnyFreeSpecLike, Matchers, ScalaFutures, EitherValues, MockitoSugar:
  object TestData:
    val id:             UUID          = UUID.fromString("6fb33641-6dc7-4a4f-adef-06238c13a317")
    val instant:        Instant       = Instant.parse("2026-08-29T00:00:00.000Z")
    val eoriNumber:     EoriNumber    = EoriNumber("eoriNumber")
    val correlationId:  String        = "correlationId"
    val conversationId: String        = "conversationId"
    val bearerToken:    String        = "Bearer token"
    val dateTime:       LocalDateTime = LocalDateTime.parse("2026-08-29T00:00:00")

    val correlationIdHeader:  HttpHeader.CorrelationId  = HttpHeader.CorrelationId(correlationId)
    val conversationIdHeader: HttpHeader.ConversationId = HttpHeader.ConversationId(conversationId)
    val authorizationHeader:  HttpHeader.Authorization  = HttpHeader.Authorization(bearerToken)
    val dateHeader:           HttpHeader.Date           = HttpHeader.Date("date")

    val aesIE507Message: AesIE507Message =
      AesIE507Message(
        submissionId = Some(SubmissionId(id)),
        exportOperation = ExportOperation(
          exportOperationType = ExportOperationType.Standard,
          mrn = Mrn("mrn"),
          discrepanciesExist = DiscrepanciesExist(false),
          splitIndicator = SplitIndicator(true)
        ),
        customsOfficeOfExitActual = CustomsOfficeOfExitActual(
          referenceNumber = ReferenceNumber("referenceNumber")
        ),
        goodsShipment = None
      )

    val eisIE507Request: EisIE507Request =
      EisIE507Request(
        headers = EisIE507RequestHeaders(
          correlationIdHeader,
          conversationIdHeader,
          authorizationHeader,
          dateHeader
        ),
        message = EisIE507Message(
          header = EisIE507Header(
            messageSender = MessageSender(eoriNumber.value),
            messageRecipient = MessageRecipient("NECA.XI"),
            preparationDateAndTime = dateTime,
            messageIdentification = MessageIdentification(correlationId),
            messageType = MessageType("CC507C")
          ),
          body = EisIE507Body(
            exportOperation = ExportOperation(
              exportOperationType = ExportOperationType.Standard,
              mrn = Mrn("mrn"),
              discrepanciesExist = DiscrepanciesExist(false),
              splitIndicator = SplitIndicator(true)
            ),
            customsOfficeOfExitActual = CustomsOfficeOfExitActual(
              referenceNumber = ReferenceNumber("referenceNumber")
            ),
            goodsShipment = None
          )
        )
      )

    val eisErrorResponse: EisErrorResponse =
      EisErrorResponse(
        timestamp = instant,
        correlationId = correlationId,
        errorCode = 123,
        errorMessage = "errorMessage",
        source = "source",
        sourceFaultDetail = SourceFaultDetail(Seq.empty)
      )
  end TestData

  given ec: ExecutionContext = ExecutionContext.global

  given hc: HeaderCarrier = HeaderCarrier()

  val eisConnector:    EisConnector    = mock[EisConnector]
  val eisIE507Factory: EisIE507Factory = mock[EisIE507Factory]
  val appConfig:       AppConfig       = mock[AppConfig]

  val eisService: EisService = EisService(eisConnector, eisIE507Factory, appConfig)

  "EisService" - {

    ".submitMessage" - {

      "should return nothing" - {

        "when EisConnector returns nothing" in {
          when(appConfig.eisToken).thenReturn(TestData.bearerToken)

          when(
            eisIE507Factory.request(
              TestData.aesIE507Message,
              TestData.eoriNumber,
              TestData.authorizationHeader,
              Some(TestData.correlationIdHeader),
              Some(TestData.conversationIdHeader)
            )
          ).thenReturn(TestData.eisIE507Request)

          when(eisConnector.submitMessage(TestData.eisIE507Request)(using hc))
            .thenReturn(Right(()).toEitherTRight[ConnectorError])

          val result: Either[EisErrorResponse, Unit] = eisService
            .submitMessage(
              TestData.aesIE507Message,
              TestData.eoriNumber,
              Some(TestData.correlationIdHeader),
              Some(TestData.conversationIdHeader)
            )
            .value
            .futureValue
            .value

          result shouldBe Right(())
        }
      }

      "should return an EisErrorResponse" - {

        "when EisConnector returns an EisErrorResponse" in {
          when(appConfig.eisToken).thenReturn(TestData.bearerToken)

          when(
            eisIE507Factory.request(
              TestData.aesIE507Message,
              TestData.eoriNumber,
              TestData.authorizationHeader,
              Some(TestData.correlationIdHeader),
              Some(TestData.conversationIdHeader)
            )
          ).thenReturn(TestData.eisIE507Request)

          when(eisConnector.submitMessage(TestData.eisIE507Request)(using hc))
            .thenReturn(Left(TestData.eisErrorResponse).toEitherTRight[ConnectorError])

          val result: Either[EisErrorResponse, Unit] = eisService
            .submitMessage(
              TestData.aesIE507Message,
              TestData.eoriNumber,
              Some(TestData.correlationIdHeader),
              Some(TestData.conversationIdHeader)
            )
            .value
            .futureValue
            .value

          result shouldBe Left(TestData.eisErrorResponse)
        }
      }

      "should return an EisServiceError" - {

        "when EisConnector returns a ConnectorError" in {
          when(appConfig.eisToken).thenReturn(TestData.bearerToken)

          when(
            eisIE507Factory.request(
              TestData.aesIE507Message,
              TestData.eoriNumber,
              TestData.authorizationHeader,
              Some(TestData.correlationIdHeader),
              Some(TestData.conversationIdHeader)
            )
          ).thenReturn(TestData.eisIE507Request)

          when(eisConnector.submitMessage(TestData.eisIE507Request)(using hc))
            .thenReturn(
              ConnectorError
                .UnexpectedError("GET", "/", Exception())
                .toEitherTLeft[Either[EisErrorResponse, Unit]]
            )

          val eisServiceError: EisServiceError =
            EisServiceError.SubmissionFailedError(
              s"Failed to submit IE507 message to EIS. EORI: ${TestData.eoriNumber.value}, " +
                s"submissionId: ${TestData.id}"
            )

          val result: EisServiceError = eisService
            .submitMessage(
              TestData.aesIE507Message,
              TestData.eoriNumber,
              Some(TestData.correlationIdHeader),
              Some(TestData.conversationIdHeader)
            )
            .value
            .futureValue
            .left
            .value

          result shouldBe eisServiceError
        }
      }
    }
  }
