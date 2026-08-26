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

package uk.gov.hmrc.automatedexportsystem.controllers

import play.api.test.FakeRequest
import test.uk.gov.hmrc.automatedexportsystem.helpers.BaseISpec

import scala.xml.{Elem, XML as Xml}

class NotificationControllerISpec extends BaseISpec:

  private val endpoint = "/automated-export-system/notification"
  val validPayload: Elem =
    <notification>
      <correlationId>8f3c2a19-7d2b-4b74-a9f0-123456789012</correlationId>
      <eori>GB123456789000</eori>
      <mrn>25GB1234567890ABCDE</mrn>
      <dateCreated>2026-08-12T10:15:30Z</dateCreated>
      <status>1</status>
    </notification>

  val invalidPayload = """<not-notification>
                         |      <status>1</status>
                         |    </notification>""".stripMargin

  val invalidXmlPayload =
    """<notification>
      |      <status>1</status>
      |    </notification>""".stripMargin

  "POST /notification" - {

    "return 204 when authorization header is valid and payload is valid" in {
      val request = FakeRequest(POST, endpoint)
        .withHeaders("Authorization" -> "some-token")
        .withXmlBody(validPayload)

      val result = route(app, request).value
      status(result) shouldBe NO_CONTENT
    }

    "return 401 when authorization header is invalid" in {
      val request = FakeRequest(POST, endpoint)
        .withHeaders("Authorization" -> "invalid-token")
        .withXmlBody(validPayload)

      val result = route(app, request).value
      status(result)      shouldBe UNAUTHORIZED
      contentType(result) shouldBe Some("application/xml")
      val resultXml = Xml.loadString(contentAsString(result))
      (resultXml \ "code").text.trim shouldBe "UNAUTHORIZED"
    }

    "return 400 when authorization header is valid and payload is missing" in {
      val request = FakeRequest(POST, endpoint)
        .withHeaders("Authorization" -> "some-token")

      val result = route(app, request).value
      status(result)      shouldBe BAD_REQUEST
      contentType(result) shouldBe Some("application/xml")
      val resultXml = Xml.loadString(contentAsString(result))
      (resultXml \ "code").text shouldBe "BAD_REQUEST"
    }
    "return 400 when authorization header is valid and payload is invalid" in {
      val request = FakeRequest(POST, endpoint)
        .withHeaders("Authorization" -> "some-token")
        .withBody(invalidPayload)

      val result = route(app, request).value
      status(result)      shouldBe BAD_REQUEST
      contentType(result) shouldBe Some("application/xml")
      val resultXml = Xml.loadString(contentAsString(result))
      (resultXml \ "code").text shouldBe "BAD_REQUEST"
    }

    "return 400 when authorization header is valid and payload is invalid xml" in {
      val request = FakeRequest(POST, endpoint)
        .withHeaders("Authorization" -> "some-token")
        .withBody(invalidXmlPayload)

      val result = route(app, request).value
      status(result)      shouldBe BAD_REQUEST
      contentType(result) shouldBe Some("application/xml")
      val resultXml = Xml.loadString(contentAsString(result))
      (resultXml \ "code").text shouldBe "INVALID_NOTIFICATION"
    }
  }
