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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.api.test.Helpers.stubControllerComponents

class IE507StubControllerSpec extends AnyWordSpec with Matchers {

  private val controller =
    new IE507StubController(
      stubControllerComponents()
    )

  private val validRequest =
    FakeRequest(
      POST,
      "/cds/aesIE507Request/v1"
    ).withHeaders(
      "x-forwarded-host" -> "automated-export-system",
      "x-correlation-id" -> "12345",
      "date" -> "Mon, 13 Jul 2026 12:00:00 GMT",
      "content-type" -> "application/xml",
      "accept" -> "application/xml",
      "authorization" -> "Bearer test-token",
      "x-message-type" -> "aesIE507Request"
    )

  "IE507StubController" should {

    "return NoContent when all required headers are supplied" in {

      val result =
        controller.submit()(validRequest)

      status(result) shouldBe NO_CONTENT
    }

    "return x-correlation-id header in the response" in {

      val result =
        controller.submit()(validRequest)

      header("x-correlation-id", result) shouldBe Some("12345")
    }

    "return date header in the response" in {

      val result =
        controller.submit()(validRequest)

      header("date", result).isDefined shouldBe true
    }

    "return BadRequest when x-correlation-id is missing" in {

      val request =
        FakeRequest(
          POST,
          "/cds/aesIE507Request/v1"
        ).withHeaders(
          "x-forwarded-host" -> "automated-export-system",
          "date" -> "Mon, 13 Jul 2026 12:00:00 GMT",
          "content-type" -> "application/xml",
          "accept" -> "application/xml",
          "authorization" -> "Bearer test-token",
          "x-message-type" -> "aesIE507Request"
        )

      val result =
        controller.submit()(request)

      status(result) shouldBe BAD_REQUEST
    }

    "return BadRequest when authorization is missing" in {

      val request =
        FakeRequest(
          POST,
          "/cds/aesIE507Request/v1"
        ).withHeaders(
          "x-forwarded-host" -> "automated-export-system",
          "x-correlation-id" -> "12345",
          "date" -> "Mon, 13 Jul 2026 12:00:00 GMT",
          "content-type" -> "application/xml",
          "accept" -> "application/xml",
          "x-message-type" -> "aesIE507Request"
        )

      val result =
        controller.submit()(request)

      status(result) shouldBe BAD_REQUEST
    }

    "return BadRequest when x-message-type is missing" in {

      val request =
        FakeRequest(
          POST,
          "/cds/aesIE507Request/v1"
        ).withHeaders(
          "x-forwarded-host" -> "automated-export-system",
          "x-correlation-id" -> "12345",
          "date" -> "Mon, 13 Jul 2026 12:00:00 GMT",
          "content-type" -> "application/xml",
          "accept" -> "application/xml",
          "authorization" -> "Bearer test-token"
        )

      val result =
        controller.submit()(request)

      status(result) shouldBe BAD_REQUEST
    }
  }
}

