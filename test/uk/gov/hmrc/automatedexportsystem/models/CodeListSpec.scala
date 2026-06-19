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

package uk.gov.hmrc.automatedexportsystem.models.codelist

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import java.time.LocalDateTime

class CodeListSpec extends AnyWordSpec with Matchers {

  object TestCodeList extends CodeList {
    override val name: String = "Test"
    override val description: Option[String] = None
    override val startDate: Option[LocalDateTime] = None
    override val endDate: Option[LocalDateTime] = None
  }

  "CodeList" should {
    val now = LocalDateTime.now()
    
    "be valid when no dates are defined" in {
      TestCodeList.isValid shouldBe true
    }

    "return true when startDate is in the past and endDate is in the future" in {
      object ValidCodeList extends CodeList {
        override val name: String = "Valid"
        override val description: Option[String] = None
        override val startDate: Option[LocalDateTime] = Some(now.minusDays(1))
        override val endDate: Option[LocalDateTime] = Some(now.plusDays(1))
      }

      ValidCodeList.isValid shouldBe true
    }

    "return false when startDate is in the future" in {
      object InvalidStartDate extends CodeList {
        override val name: String = "Invalid"
        override val description: Option[String] = None
        override val startDate: Option[LocalDateTime] = Some(now.plusDays(1))
        override val endDate: Option[LocalDateTime] = None
      }

      InvalidStartDate.isValid shouldBe false
    }

    "return false when endDate is in the past" in {
      object InvalidEndDate extends CodeList {
        override val name: String = "Invalid"
        override val description: Option[String] = None
        override val startDate: Option[LocalDateTime] = None
        override val endDate: Option[LocalDateTime] = Some(now.minusDays(1))
      }

      InvalidEndDate.isValid shouldBe false
    }
  }
}

