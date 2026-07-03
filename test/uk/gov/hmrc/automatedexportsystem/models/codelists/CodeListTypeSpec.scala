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

package uk.gov.hmrc.automatedexportsystem.models.codelists

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class CodeListTypeSpec extends AnyWordSpec with Matchers {

  "CodeListType" should {

    "return CL060 for MessageType" in {
      CodeListType.MessageType.code shouldBe "CL060"
    }

    "return CL347 for TypeOfLocation" in {
      CodeListType.TypeOfLocation.code shouldBe "CL347"
    }

    "return CL165 for Nationality" in {
      CodeListType.Nationality.code shouldBe "CL165"
    }

    "return CL018 for TransportMode" in {
      CodeListType.TransportMode.code shouldBe "CL018"
    }

    "return CL094 for CustomsOfficeExit" in {
      CodeListType.CustomsOfficeExit.code shouldBe "CL094"
    }

    "allow each code list type to be treated as a CodeListType" in {
      val codeListTypes: Seq[CodeListType] = Seq(
        CodeListType.MessageType,
        CodeListType.TypeOfLocation,
        CodeListType.Nationality,
        CodeListType.TransportMode,
        CodeListType.CustomsOfficeExit
      )

      codeListTypes should have size 5

      codeListTypes.map(_.code) should contain allOf (
        "CL060",
        "CL347",
        "CL165",
        "CL018",
        "CL094"
      )
    }
  }
}
