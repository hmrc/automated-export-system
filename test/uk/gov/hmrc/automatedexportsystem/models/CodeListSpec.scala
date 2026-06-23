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

package uk.gov.hmrc.automatedexportsystem.models.coelistd

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import java.time.{Clock, Instant, LocalDateTime, ZoneOffset}
import uk.gov.hmrc.automatedexportsystem.models.codelists.CodeList

class CodeListSpec extends AnyWordSpec with Matchers {
  private val clock: Clock =
    Clock.fixed(Instant.parse("2026-06-23T09:00:00Z"), ZoneOffset.UTC)

  private val now: LocalDateTime =
    LocalDateTime.now(clock)

  "CodeList" should {

    "be valid when no dates are defined" in {
      val testCodeList = CodeList(
        name = "Test",
        description = None,
        startDate = None,
        endDate = None
      )
      testCodeList.isValid(clock) shouldBe true
    }

    "return true when startDate is in the past and endDate is in the future" in {
      val validCodeList = CodeList(
        name = "Valid",
        description = None,
        startDate = Some(now.minusDays(1)),
        endDate = Some(now.plusDays(1))
      )
      validCodeList.isValid(clock) shouldBe true
    }

    "return false when startDate is in the future" in {
      val invalidStartDate = CodeList(
        name = "Invalid",
        description = None,
        startDate = Some(now.plusDays(1)),
        endDate = None
      )
      invalidStartDate.isValid(clock) shouldBe false
    }

    "return false when endDate is in the past" in {
      val invalidEndDate = CodeList(
        name = "Invalid",
        description = None,
        startDate = None,
        endDate = Some(now.minusDays(1))
      )
      invalidEndDate.isValid(clock) shouldBe false
    }
  }
}
