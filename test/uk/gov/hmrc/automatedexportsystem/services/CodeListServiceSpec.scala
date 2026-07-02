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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.http.HeaderCarrier

class CodeListServiceSpec extends AnyWordSpec with Matchers {

  given HeaderCarrier = HeaderCarrier()

  "CodeListService" should {

    "return a MessageTypeCodeList from message type XML" in {}

    "return a TypeOfLocationCodeList from type of location XML" in {}

    "return a NationalityCodeList from nationality XML" in {}

    "return a TransportModeCodeList from transport mode XML" in {}

    "return a CustomsOfficeExitCodeList from customs office exit XML" in {}

    "filter out expired code list values" in {}

    "keep currently valid code list values" in {}

    "keep code list values with no end date" in {}

    "return an empty collection when no valid values are found" in {}

    "propagate connector failures" in {}
  }
}
