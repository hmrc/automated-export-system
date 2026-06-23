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

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.FakeRequest
import play.api.test.Helpers._

class CodeListControllerISpec extends PlaySpec with GuiceOneAppPerSuite {

  private val baseUrl = "/automated-export-system/codelists"

  "CodeListController" should {

    "return XML for message type endpoint" in {
      val request = FakeRequest(GET, s"$baseUrl/messagetype")

      val result = route(app, request).get

      status(result) mustBe OK
      contentType(result) mustBe Some("application/xml")
      contentAsString(result) must include("<codeList>")
    }

    "return XML for all endpoints" in {
      val endpoints = Seq(
        "messagetype",
        "typeoflocation",
        "nationality",
        "transportmode",
        "customsofficeexit"
      )


      endpoints.foreach { endpoint =>
        val request = FakeRequest(GET, s"$baseUrl/$endpoint")
        val result = route(app, request).get

        status(result) mustBe OK
        contentType(result) mustBe Some("application/xml")
        contentAsString(result) must not be empty
      }
    }

    "return 404 for an unknown code list endpoint" in {
      val request = FakeRequest(GET, s"$baseUrl/invalid")
      val result = route(app, request).get

      status(result) mustBe NOT_FOUND
    }
  }
}

