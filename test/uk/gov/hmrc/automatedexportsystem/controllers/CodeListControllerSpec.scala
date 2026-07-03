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
import play.api.test.Helpers._
import play.api.test.Helpers.stubControllerComponents

class CodeListControllerSpec extends AnyWordSpec with Matchers {

  private val controller =
    new CodeListController(stubControllerComponents())

  "CodeListController" should {

    "return message types XML" in {
      val result = controller.messageTypes()(FakeRequest())

      status(result)        shouldBe OK
      contentAsString(result) should include("<codeList>")
    }

    "return type of locations XML" in {
      val result = controller.typeOfLocations()(FakeRequest())

      status(result)        shouldBe OK
      contentAsString(result) should include("<codeList>")
    }

    "return nationalities XML" in {
      val result = controller.nationalities()(FakeRequest())

      status(result)        shouldBe OK
      contentAsString(result) should include("<codeList>")
    }

    "return transport modes XML" in {
      val result = controller.transportModes()(FakeRequest())

      status(result)        shouldBe OK
      contentAsString(result) should include("<codeList>")
    }

    "return customs office exits XML" in {
      val result = controller.customsOfficeExits()(FakeRequest())

      status(result)        shouldBe OK
      contentAsString(result) should include("<codeList>")
    }

    "return content type application/xml" in {
      contentType(
        controller.messageTypes()(FakeRequest())
      ) shouldBe Some("application/xml")
    }

    "return OK for each code list endpoint" in {
      status(controller.messageTypes()(FakeRequest()))       shouldBe OK
      status(controller.typeOfLocations()(FakeRequest()))    shouldBe OK
      status(controller.nationalities()(FakeRequest()))      shouldBe OK
      status(controller.transportModes()(FakeRequest()))     shouldBe OK
      status(controller.customsOfficeExits()(FakeRequest())) shouldBe OK
    }
  }
}
