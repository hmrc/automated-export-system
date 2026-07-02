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

import org.mockito.Mockito.{mock, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.automatedexportsystem.connectors.CodeListConnector
import uk.gov.hmrc.automatedexportsystem.models.codelists._
import uk.gov.hmrc.http.HeaderCarrier

import java.time.{Clock, Instant, ZoneOffset}
import scala.concurrent.{ExecutionContext, Future}

class CodeListServiceSpec extends AnyWordSpec with Matchers with ScalaFutures {

  given ExecutionContext = ExecutionContext.global
  given HeaderCarrier = HeaderCarrier()

  private val clock =
    Clock.fixed(
      Instant.parse("2026-07-02T12:00:00Z"),
      ZoneOffset.UTC
    )

  private def serviceWith(connector: CodeListConnector): CodeListService =
    new CodeListService(connector, clock)

  private val validXml =
    """<root>
      |  <item>
      |    <name>Valid Code</name>
      |    <description>Currently valid code</description>
      |    <startDate>2026-07-01T00:00:00</startDate>
      |    <endDate>2026-07-03T00:00:00</endDate>
      |  </item>
      |</root>""".stripMargin

  private val expiredXml =
    """<root>
      |  <item>
      |    <name>Expired Code</name>
      |    <description>Expired code</description>
      |    <startDate>2026-06-01T00:00:00</startDate>
      |    <endDate>2026-06-30T00:00:00</endDate>
      |  </item>
      |</root>""".stripMargin

  private val noEndDateXml =
    """<root>
      |  <item>
      |    <name>No End Date Code</name>
      |    <description>Still valid because it has no end date</description>
      |    <startDate>2026-07-01T00:00:00</startDate>
      |  </item>
      |</root>""".stripMargin

  private val mixedXml =
    """<root>
      |  <item>
      |    <name>Valid Code</name>
      |    <description>Currently valid code</description>
      |    <startDate>2026-07-01T00:00:00</startDate>
      |    <endDate>2026-07-03T00:00:00</endDate>
      |  </item>
      |  <item>
      |    <name>Expired Code</name>
      |    <description>Expired code</description>
      |    <startDate>2026-06-01T00:00:00</startDate>
      |    <endDate>2026-06-30T00:00:00</endDate>
      |  </item>
      |</root>""".stripMargin

  "CodeListService" should {

    "return a MessageTypeCodeList from message type XML" in {
      val connector = mock(classOf[CodeListConnector])

      when(connector.getMessageTypes())
        .thenReturn(Future.successful(validXml))

      val result =
        serviceWith(connector).getMessageTypes().futureValue

      result shouldBe a[MessageTypeCodeList]
      result.values should have size 1
      result.values.head.name shouldBe "Valid Code"
    }

    "return a TypeOfLocationCodeList from type of location XML" in {
      val connector = mock(classOf[CodeListConnector])

      when(connector.getTypeOfLocations())
        .thenReturn(Future.successful(validXml))

      val result =
        serviceWith(connector).getTypeOfLocations().futureValue

      result shouldBe a[TypeOfLocationCodeList]
      result.values should have size 1
      result.values.head.name shouldBe "Valid Code"
    }

    "return a NationalityCodeList from nationality XML" in {
      val connector = mock(classOf[CodeListConnector])

      when(connector.getNationalities())
        .thenReturn(Future.successful(validXml))

      val result =
        serviceWith(connector).getNationalities().futureValue

      result shouldBe a[NationalityCodeList]
      result.values should have size 1
      result.values.head.name shouldBe "Valid Code"
    }

    "return a TransportModeCodeList from transport mode XML" in {
      val connector = mock(classOf[CodeListConnector])

      when(connector.getTransportModes())
        .thenReturn(Future.successful(validXml))

      val result =
        serviceWith(connector).getTransportModes().futureValue

      result shouldBe a[TransportModeCodeList]
      result.values should have size 1
      result.values.head.name shouldBe "Valid Code"
    }

    "return a CustomsOfficeExitCodeList from customs office exit XML" in {
      val connector = mock(classOf[CodeListConnector])

      when(connector.getCustomsOfficeExits())
        .thenReturn(Future.successful(validXml))

      val result =
        serviceWith(connector).getCustomsOfficeExits().futureValue

      result shouldBe a[CustomsOfficeExitCodeList]
      result.values should have size 1
      result.values.head.name shouldBe "Valid Code"
    }

    "filter out expired code list values" in {
      val connector = mock(classOf[CodeListConnector])

      when(connector.getMessageTypes())
        .thenReturn(Future.successful(mixedXml))

      val result =
        serviceWith(connector).getMessageTypes().futureValue

      result.values should have size 1
      result.values.map(_.name) should contain("Valid Code")
      result.values.map(_.name) should not contain "Expired Code"
    }

    "keep currently valid code list values" in {
      val connector = mock(classOf[CodeListConnector])

      when(connector.getMessageTypes())
        .thenReturn(Future.successful(validXml))

      val result =
        serviceWith(connector).getMessageTypes().futureValue

      result.values should have size 1
      result.values.head.name shouldBe "Valid Code"
    }

    "keep code list values with no end date" in {
      val connector = mock(classOf[CodeListConnector])

      when(connector.getMessageTypes())
        .thenReturn(Future.successful(noEndDateXml))

      val result =
        serviceWith(connector).getMessageTypes().futureValue

      result.values should have size 1
      result.values.head.name shouldBe "No End Date Code"
    }

    "return an empty collection when no valid values are found" in {
      val connector = mock(classOf[CodeListConnector])

      when(connector.getMessageTypes())
        .thenReturn(Future.successful(expiredXml))

      val result =
        serviceWith(connector).getMessageTypes().futureValue

      result.values shouldBe empty
    }

    "propagate connector failures" in {
      val connector = mock(classOf[CodeListConnector])

      when(connector.getMessageTypes())
        .thenReturn(Future.failed(new RuntimeException("connector failed")))

      val result =
        serviceWith(connector).getMessageTypes().failed.futureValue

      result.getMessage shouldBe "connector failed"
    }
  }
}