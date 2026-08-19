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

import scala.xml.XML as Xml

class NotificationControllerISpec extends BaseISpec:

  private val endpoint = "/automated-export-system/notification"

  "POST /notification" - {

    "return 204 when authorization header is valid and payload is valid" in {
      val request = FakeRequest(POST, endpoint)
        .withHeaders("Authorization" -> "some-token")
        .withTextBody("<valid>xml</valid>")

      val result = route(app, request).value
      status(result) shouldBe NO_CONTENT
    }

    "return 401 when authorization header is invalid" in {
      val request = FakeRequest(POST, endpoint)
        .withHeaders("Authorization" -> "invalid-token")
        .withTextBody("<valid>xml</valid>")

      val result = route(app, request).value
      status(result)      shouldBe UNAUTHORIZED
      contentType(result) shouldBe Some("application/xml")
      val resultXml = Xml.loadString(contentAsString(result))
      (resultXml \ "code").text.trim shouldBe "unauthorized"
    }

    "return 400 when authorization header is valid and payload is missing" in {
      val request = FakeRequest(POST, endpoint)
        .withHeaders("Authorization" -> "some-token")

      val result = route(app, request).value
      status(result)      shouldBe BAD_REQUEST
      contentType(result) shouldBe Some("application/xml")
      val resultXml = Xml.loadString(contentAsString(result))
      (resultXml \ "code").text shouldBe "bad_request"
    }
    "return 400 when authorization header is valid and payload is invalid xml" in {
      val request = FakeRequest(POST, endpoint)
        .withHeaders("Authorization" -> "some-token")
        .withTextBody("<valid>xml</invalid>")

      val result = route(app, request).value
      status(result)      shouldBe BAD_REQUEST
      contentType(result) shouldBe Some("application/xml")
      val resultXml = Xml.loadString(contentAsString(result))
      (resultXml \ "code").text shouldBe "bad_request"
    }
  }
