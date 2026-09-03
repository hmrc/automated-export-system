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

import org.mockito.Mockito.when
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.automatedexportsystem.models.IE507.*
import uk.gov.hmrc.automatedexportsystem.models.IE507.aes.AesIE507Message
import uk.gov.hmrc.automatedexportsystem.models.IE507.eis.{EisIE507Body, EisIE507Header, EisIE507Message, MessageIdentification}
import uk.gov.hmrc.automatedexportsystem.models.eis.{EisIE507Request, EisIE507RequestHeaders}
import uk.gov.hmrc.automatedexportsystem.models.http.HttpHeader
import uk.gov.hmrc.automatedexportsystem.util.IdGenerator

import java.time.{Clock, Instant, LocalDateTime, ZoneOffset}

class EisIE507FactorySpec extends AnyFreeSpecLike, Matchers, MockitoSugar:
  val instant: Instant = Instant.parse("2026-08-24T00:00:00.000Z")
  val clock:   Clock   = Clock.fixed(instant, ZoneOffset.UTC)

  val idGenerator: IdGenerator = mock[IdGenerator]

  val eisIE507Factory: EisIE507Factory = EisIE507Factory(clock, idGenerator)

  object TestData:
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

    val eoriNumber:     EoriNumber    = EoriNumber("eoriNumber")
    val correlationId:  String        = "correlationId"
    val conversationId: String        = "conversationId"
    val dateTime:       LocalDateTime = LocalDateTime.parse("2026-08-24T00:00:00")

    val authorizationHeader:  HttpHeader.Authorization  = HttpHeader.Authorization("Bearer token")
    val correlationIdHeader:  HttpHeader.CorrelationId  = HttpHeader.CorrelationId(correlationId)
    val conversationIdHeader: HttpHeader.ConversationId = HttpHeader.ConversationId(conversationId)
    val dateHeader:           HttpHeader.Date           = HttpHeader.Date("Mon, 24 Aug 2026 00:00:00 GMT")
  end TestData

  "EisIE507Factory" - {

    ".request" - {

      "should return an EisIE507Request" - {

        "when correlationId and conversationId are provided" in {
          val eisIE507Request: EisIE507Request =
            EisIE507Request(
              headers = EisIE507RequestHeaders(
                correlationId = TestData.correlationIdHeader,
                conversationId = TestData.conversationIdHeader,
                authorization = TestData.authorizationHeader,
                date = TestData.dateHeader
              ),
              message = EisIE507Message(
                header = EisIE507Header(
                  eoriNumber = TestData.eoriNumber,
                  preparationDateAndTime = TestData.dateTime,
                  messageIdentification = MessageIdentification(TestData.correlationId)
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

          val result: EisIE507Request = eisIE507Factory.request(
            TestData.aesIE507Message,
            TestData.eoriNumber,
            TestData.authorizationHeader,
            Some(TestData.correlationIdHeader),
            Some(TestData.conversationIdHeader)
          )

          result shouldBe eisIE507Request
        }

        "when correlationId and conversationId are not provided (should generate new ones)" in {
          val eisIE507Request: EisIE507Request =
            EisIE507Request(
              headers = EisIE507RequestHeaders(
                correlationId = HttpHeader.CorrelationId("generated-correlation-id"),
                conversationId = HttpHeader.ConversationId("generated-conversation-id"),
                authorization = TestData.authorizationHeader,
                date = TestData.dateHeader
              ),
              message = EisIE507Message(
                header = EisIE507Header(
                  eoriNumber = TestData.eoriNumber,
                  preparationDateAndTime = TestData.dateTime,
                  messageIdentification = MessageIdentification("generated-correlation-id")
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

          when(idGenerator.generate35Char)
            .thenReturn("generated-correlation-id", "generated-conversation-id")

          val result: EisIE507Request = eisIE507Factory.request(
            TestData.aesIE507Message,
            TestData.eoriNumber,
            TestData.authorizationHeader,
            maybeCorrelationId = None,
            maybeConversationId = None
          )

          result shouldBe eisIE507Request
        }
      }
    }
  }
