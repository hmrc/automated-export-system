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

package uk.gov.hmrc.automatedexportsystem.connectors

import cats.data.NonEmptyList
import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock.*
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import play.api.test.Helpers
import uk.gov.hmrc.automatedexportsystem.errors.{ConnectorError, ResponseCode, XmlReaderError}
import uk.gov.hmrc.automatedexportsystem.helpers.BaseISpec
import uk.gov.hmrc.automatedexportsystem.models.IE507.*
import uk.gov.hmrc.automatedexportsystem.models.IE507.aes.AesIE507Message
import uk.gov.hmrc.automatedexportsystem.models.IE507.eis.*
import uk.gov.hmrc.automatedexportsystem.models.eis.{EisErrorResponse, EisIE507Request, EisIE507RequestHeaders, SourceFaultDetail}
import uk.gov.hmrc.automatedexportsystem.models.http.{CustomHeaderNames, HttpHeader}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.{Clock, Instant, LocalDateTime, ZoneOffset}
import scala.xml.Elem

class EisConnectorSpec extends BaseISpec with TableDrivenPropertyChecks:
  object TestData:
    val instant:         Instant       = Instant.parse("2026-08-29T00:00:00.000Z")
    val eoriNumber:      EoriNumber    = EoriNumber("eoriNumber")
    val correlationId:   String        = "correlationId"
    val conversationId:  String        = "conversationId"
    val dateTime:        LocalDateTime = LocalDateTime.parse("2026-08-29T00:00:00")
    val rfc1123DateTime: String        = "Sat, 29 Aug 2026 00:00:00 GMT"
    val bearerToken:     String        = "Bearer token"

    val correlationIdHeader:  HttpHeader.CorrelationId  = HttpHeader.CorrelationId(correlationId)
    val conversationIdHeader: HttpHeader.ConversationId = HttpHeader.ConversationId(conversationId)
    val authorizationHeader:  HttpHeader.Authorization  = HttpHeader.Authorization(bearerToken)
    val dateHeader:           HttpHeader.Date           = HttpHeader.Date(rfc1123DateTime)

    val aesIE507Message: AesIE507Message =
      AesIE507Message(
        submissionId = None,
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

    val eisIE507MessageXml: Elem =
      <n:CC507C xmlns:n="http://ecs.dgtaxud.ec">
        <Header>
          <messageSender>
            {eoriNumber.value}
          </messageSender>
          <messageRecipient>NECA.XI</messageRecipient>
          <preparationDateAndTime>2026-08-29T00:00:00</preparationDateAndTime>
          <messageIdentification>
            {correlationId}
          </messageIdentification>
          <messageType>CC507C</messageType>
        </Header>
        <Body>
          <ExportOperation>
            <type>1</type>
            <MRN>mrn</MRN>
            <discrepanciesExist>0</discrepanciesExist>
            <splitIndicator>1</splitIndicator>
          </ExportOperation>
          <CustomsOfficeOfExitActual>
            <referenceNumber>referenceNumber</referenceNumber>
          </CustomsOfficeOfExitActual>
        </Body>
      </n:CC507C>

    def eisErrorResponseXml(status: Int): Elem =
      <error>
        <timestamp>{instant}</timestamp>
        <correlationId>{correlationId}</correlationId>
        <errorCode>{status}</errorCode>
        <errorMessage>errorMessage</errorMessage>
        <source>source</source>
        <sourceFaultDetail>
          <detail>detail1</detail>
          <detail>detail2</detail>
          <detail>detail3</detail>
        </sourceFaultDetail>
      </error>

    val eisErrorResponseInvalidXml: Elem =
      <error>
        <timestamp>instant</timestamp>
        <correlationId>{correlationId}</correlationId>
        <errorCode>status</errorCode>
        <errorMessage>errorMessage</errorMessage>
        <sourceFaultDetail>
        </sourceFaultDetail>
      </error>
  end TestData

  override given patienceConfig: PatienceConfig = PatienceConfig(3000.millis, 15.millis)

  val clock: Clock = Clock.fixed(TestData.instant, ZoneOffset.UTC)

  val eisConnector: EisConnector = app.injector.instanceOf[EisConnector]

  given hc: HeaderCarrier = HeaderCarrier()

  def eisPostRequestMappingBuilder: MappingBuilder =
    post(urlEqualTo("/cds/aesIE507Request/v1"))
      .withHeader(CustomHeaderNames.X_CORRELATION_ID, equalTo(TestData.correlationId))
      .withHeader(CustomHeaderNames.X_CONVERSATION_ID, equalTo(TestData.conversationId))
      .withHeader(Helpers.X_FORWARDED_HOST, equalTo("automated-export-system"))
      .withHeader(CustomHeaderNames.X_MESSAGE_TYPE, equalTo("aesIE507Request"))
      .withHeader(Helpers.CONTENT_TYPE, equalTo(Helpers.XML))
      .withHeader(Helpers.ACCEPT, equalTo(Helpers.XML))
      .withHeader(Helpers.AUTHORIZATION, equalTo(TestData.bearerToken))
      .withHeader(Helpers.DATE, equalTo(TestData.rfc1123DateTime))
      .withRequestBody(equalToXml(TestData.eisIE507MessageXml.toString))

  "EisConnector" - {

    ".submitMessage" - {

      "should return nothing" - {

        "when the EIS endpoint returns a 204 response with no content" in {
          stubFor(
            eisPostRequestMappingBuilder
              .willReturn(
                aResponse()
                  .withStatus(Helpers.NO_CONTENT)
              )
          )

          val result: Either[EisErrorResponse, Unit] =
            eisConnector
              .submitMessage(TestData.eisIE507Request)
              .value
              .futureValue
              .value

          result shouldBe Right(())
        }
      }

      "should return an EisErrorResponse" - {

        "when the EIS endpoint returns a 400 response with an error body" in {
          stubFor(
            eisPostRequestMappingBuilder
              .willReturn(
                aResponse()
                  .withStatus(Helpers.BAD_REQUEST)
                  .withBody(TestData.eisErrorResponseXml(Helpers.BAD_REQUEST).toString)
              )
          )

          val result: Either[EisErrorResponse, Unit] =
            eisConnector
              .submitMessage(TestData.eisIE507Request)
              .value
              .futureValue
              .value

          val eisErrorResponse: EisErrorResponse =
            EisErrorResponse(
              timestamp = TestData.instant,
              correlationId = TestData.correlationId,
              errorCode = Helpers.BAD_REQUEST,
              errorMessage = "errorMessage",
              source = "source",
              sourceFaultDetail = SourceFaultDetail(details =
                Seq(
                  "detail1",
                  "detail2",
                  "detail3"
                )
              )
            )

          result shouldBe Left(eisErrorResponse)
        }

        "when the EIS endpoint returns a 500 response with an error body" in {
          stubFor(
            eisPostRequestMappingBuilder
              .willReturn(
                aResponse()
                  .withStatus(Helpers.INTERNAL_SERVER_ERROR)
                  .withBody(TestData.eisErrorResponseXml(Helpers.INTERNAL_SERVER_ERROR).toString)
              )
          )

          val result: Either[EisErrorResponse, Unit] =
            eisConnector
              .submitMessage(TestData.eisIE507Request)
              .value
              .futureValue
              .value

          val eisErrorResponse: EisErrorResponse =
            EisErrorResponse(
              timestamp = TestData.instant,
              correlationId = TestData.correlationId,
              errorCode = Helpers.INTERNAL_SERVER_ERROR,
              errorMessage = "errorMessage",
              source = "source",
              sourceFaultDetail = SourceFaultDetail(details =
                Seq(
                  "detail1",
                  "detail2",
                  "detail3"
                )
              )
            )

          result shouldBe Left(eisErrorResponse)
        }
      }

      "should return a ConnectorError" - {

        "when the EIS endpoint returns a response with an invalid XML body" in {
          stubFor(
            eisPostRequestMappingBuilder
              .willReturn(
                aResponse()
                  .withStatus(Helpers.IM_A_TEAPOT)
                  .withBody("not valid xml")
              )
          )

          val result: ConnectorError =
            eisConnector
              .submitMessage(TestData.eisIE507Request)
              .value
              .futureValue
              .left
              .value

          val connectorErrorMessage: String =
            "Error on POST request to http://localhost:6001/cds/aesIE507Request/v1. " +
              "Response body was not valid XML"

          result.message      shouldBe connectorErrorMessage
          result.responseCode shouldBe ResponseCode.BadGateway
          result.exception.foreach(_.getMessage shouldBe "Content is not allowed in prolog.")
        }

        "when the EIS endpoint returns a response for which the body failed to be deserialized" - {

          "when response status is successful" in {
            stubFor(
              eisPostRequestMappingBuilder
                .willReturn(
                  aResponse()
                    .withStatus(Helpers.OK)
                    .withBody("<xml></xml>")
                )
            )

            val result: ConnectorError =
              eisConnector
                .submitMessage(TestData.eisIE507Request)
                .value
                .futureValue
                .left
                .value

            val connectorError: ConnectorError =
              ConnectorError.ResponseBodyXmlReadError(
                Helpers.POST,
                "http://localhost:6001/cds/aesIE507Request/v1",
                NonEmptyList.one(
                  XmlReaderError.ParseError("/", "Expected empty XML")
                )
              )

            result shouldBe connectorError
          }

          "when response status is unsuccessful" in {
            stubFor(
              eisPostRequestMappingBuilder
                .willReturn(
                  aResponse()
                    .withStatus(Helpers.UNPROCESSABLE_ENTITY)
                    .withBody(TestData.eisErrorResponseInvalidXml.toString)
                )
            )

            val result: ConnectorError =
              eisConnector
                .submitMessage(TestData.eisIE507Request)
                .value
                .futureValue
                .left
                .value

            val connectorError: ConnectorError =
              ConnectorError.ResponseBodyXmlReadError(
                Helpers.POST,
                "http://localhost:6001/cds/aesIE507Request/v1",
                NonEmptyList.of(
                  XmlReaderError.ParseError("/timestamp", "Failed to parse 'instant' to Instant"),
                  XmlReaderError.ParseError("/errorCode", "Failed to parse 'status' to Int"),
                  XmlReaderError.Missing("/source")
                )
              )

            result shouldBe connectorError
          }
        }

        "when the EIS endpoint returns an unexpected status response" in {
          stubFor(
            eisPostRequestMappingBuilder
              .willReturn(
                aResponse()
                  .withStatus(Helpers.SWITCHING_PROTOCOLS)
                  .withBody("not valid")
              )
          )

          val result: ConnectorError =
            eisConnector
              .submitMessage(TestData.eisIE507Request)
              .value
              .futureValue
              .left
              .value

          val connectorError: ConnectorError =
            ConnectorError.UnexpectedStatusError(
              Helpers.POST,
              "http://localhost:6001/cds/aesIE507Request/v1",
              Helpers.SWITCHING_PROTOCOLS
            )

          result shouldBe connectorError
        }
      }
    }
  }
