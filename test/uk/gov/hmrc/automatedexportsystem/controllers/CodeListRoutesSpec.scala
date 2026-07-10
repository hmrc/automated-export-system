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

import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*

class CodeListRoutesSpec extends AnyWordSpec with Matchers with OptionValues {

  "the application routes" should {
    "serve /codelists/messagetype" in {
      val app = new GuiceApplicationBuilder().build()

      running(app) {
        val request      = FakeRequest(GET, "/automated-export-system/codelists/messagetype")
        val resultFuture = route(app, request).value

        status(resultFuture)      shouldBe 200
        contentType(resultFuture) shouldBe Some("application/xml")
      }
    }

    "serve /codelists/customsofficeexit" in {
      val app = new GuiceApplicationBuilder().build()

      running(app) {
        val request      = FakeRequest(GET, "/automated-export-system/codelists/customsofficeexit")
        val resultFuture = route(app, request).value

        status(resultFuture)      shouldBe 200
        contentType(resultFuture) shouldBe Some("application/xml")
      }
    }

    "serve /codelists/nationality" in {
      val app = new GuiceApplicationBuilder().build()

      running(app) {
        val request      = FakeRequest(GET, "/automated-export-system/codelists/nationality")
        val resultFuture = route(app, request).value

        status(resultFuture)      shouldBe 200
        contentType(resultFuture) shouldBe Some("application/xml")
      }
    }

    "serve /codelists/transportmode" in {
      val app = new GuiceApplicationBuilder().build()

      running(app) {
        val request      = FakeRequest(GET, "/automated-export-system/codelists/transportmode")
        val resultFuture = route(app, request).value

        status(resultFuture)      shouldBe 200
        contentType(resultFuture) shouldBe Some("application/xml")
      }
    }

    "serve /codelists/typeoflocation" in {
      val app = new GuiceApplicationBuilder().build()

      running(app) {
        val request      = FakeRequest(GET, "/automated-export-system/codelists/typeoflocation")
        val resultFuture = route(app, request).value

        status(resultFuture)      shouldBe 200
        contentType(resultFuture) shouldBe Some("application/xml")
      }
    }
  }
}
