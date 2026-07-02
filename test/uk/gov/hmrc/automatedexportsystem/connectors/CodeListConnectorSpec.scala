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

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import com.github.tomakehurst.wiremock.http.Fault
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier

class CodeListConnectorSpec extends AnyWordSpec with Matchers with ScalaFutures with BeforeAndAfterAll {

  given HeaderCarrier = HeaderCarrier()

  private val wireMockServer = new WireMockServer(options().dynamicPort())

  override def beforeAll(): Unit = {
    super.beforeAll()
    wireMockServer.start()
    configureFor("localhost", wireMockServer.port())
  }

  override def afterAll(): Unit = {
    wireMockServer.stop()
    super.afterAll()
  }

  private lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.automated-export-system.protocol" -> "http",
        "microservice.services.automated-export-system.host"     -> "localhost",
        "microservice.services.automated-export-system.port"     -> wireMockServer.port()
      )
      .build()

  private lazy val connector: CodeListConnector =
    app.injector.instanceOf[CodeListConnector]

  "CodeListConnector" should {

    "call the message types endpoint" in {
      wireMockServer.stubFor(
        get(urlEqualTo("/automated-export-system/codelists/messagetype"))
          .willReturn(
            ok("response")
          )
      )

      connector.getMessageTypes().futureValue shouldBe "response"
    }

    "call the type of locations endpoint" in {
      wireMockServer.stubFor(
        get(urlEqualTo("/automated-export-system/codelists/typeoflocation"))
          .willReturn(
            ok("response")
          )
      )

      connector.getTypeOfLocations().futureValue shouldBe "response"
    }

    "call the nationalities endpoint" in {
      wireMockServer.stubFor(
        get(urlEqualTo("/automated-export-system/codelists/nationality"))
          .willReturn(
            ok("response")
          )
      )

      connector.getNationalities().futureValue shouldBe "response"
    }

    "call the transport modes endpoint" in {
      wireMockServer.stubFor(
        get(urlEqualTo("/automated-export-system/codelists/transportmode"))
          .willReturn(
            ok("response")
          )
      )

      connector.getTransportModes().futureValue shouldBe "response"
    }

    "call the customs office exits endpoint" in {
      wireMockServer.stubFor(
        get(urlEqualTo("/automated-export-system/codelists/customsofficeexit"))
          .willReturn(
            ok("response")
          )
      )

      connector.getCustomsOfficeExits().futureValue shouldBe "response"
    }

    "return the response body as a String" in {
      val xml =
        """<codeList>
          |  <code>
          |    <name>IE815</name>
          |  </code>
          |</codeList>""".stripMargin

      wireMockServer.stubFor(
        get(urlEqualTo("/automated-export-system/codelists/messagetype"))
          .willReturn(
            ok(xml)
          )
      )

      connector.getMessageTypes().futureValue shouldBe xml
    }

    "fail when the downstream service returns an error" in {
      wireMockServer.stubFor(
        get(urlEqualTo("/automated-export-system/codelists/messagetype"))
          .willReturn(
            aResponse()
              .withFault(Fault.CONNECTION_RESET_BY_PEER)
          )
      )

      connector.getMessageTypes().failed.futureValue
    }
  }
}
