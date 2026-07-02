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

package uk.gov.hmrc.automatedexportsystem.parsers

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class CodeListParserSpec extends AnyWordSpec with Matchers {

  "CodeListParser" should {

    "parse XML into a sequence of CodeList values" in {}

    "parse the name from each XML item" in {}

    "parse the description when present" in {}

    "return None for description when missing" in {}

    "parse the start date when present" in {}

    "return None for start date when missing" in {}

    "parse the end date when present" in {}

    "return None for end date when missing" in {}

    "return an empty sequence when there are no items" in {}

    "fail when given invalid XML" in {}
  }
}
