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
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsArray, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.*

import java.time.{Clock, Instant, ZoneOffset}

class CodeListJsonControllerSpec
  extends AnyWordSpec
    with Matchers
    with OptionValues {

  "GET /automated-export-system/codelists/transportMode" should {

    "return the bundled transport modes as JSON without authentication" in {
      val clock =
        Clock.fixed(
          Instant.parse("2026-11-04T12:00:00Z"),
          ZoneOffset.UTC
        )

      val app = new GuiceApplicationBuilder()
        .overrides(bind[Clock].toInstance(clock))
        .build()

      running(app) {
        val request = FakeRequest(
          GET,
          "/automated-export-system/codelists/transportMode"
        )

        val result = route(app, request).value

        status(result) shouldBe OK
        contentType(result) shouldBe Some("application/json")

        val entries = contentAsJson(result).as[JsArray].value

        entries.map(json => (json \ "key").as[String]) shouldBe
          Seq("1", "2", "3", "4", "5", "7", "8", "9")

        entries.head shouldBe Json.obj(
          "key" -> "1",
          "value" -> "Sea transport",
          "properties" -> Json.obj()
        )

        entries.foreach { json =>
          json.as[play.api.libs.json.JsObject].keys shouldBe
            Set("key", "value", "properties")
        }
      }
    }
  }
}
