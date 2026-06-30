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

import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers

import java.time.{Clock, Instant, LocalDateTime, ZoneOffset}

class CodeListSpec extends AnyFreeSpecLike with Matchers {

  private val clock: Clock =
    Clock.fixed(
      Instant.parse("2026-06-23T09:00:00Z"),
      ZoneOffset.UTC
    )

  private val now: LocalDateTime =
    LocalDateTime.now(clock)

  "CodeList.isValid" - {

    "should be valid when no dates are supplied" in {

      val codeList =
        CodeList(
          name = "CD001B",
          description = Some("Export Declaration"),
          startDate = None,
          endDate = None
        )

      codeList.isValid(clock) shouldBe true
    }

    "should be valid when current date falls between start and end date" in {

      val codeList =
        CodeList(
          name = "CD001B",
          description = Some("Export Declaration"),
          startDate = Some(now.minusDays(1)),
          endDate = Some(now.plusDays(1))
        )

      codeList.isValid(clock) shouldBe true
    }

    "should be invalid when start date is in the future" in {

      val codeList =
        CodeList(
          name = "CD001B",
          description = Some("Export Declaration"),
          startDate = Some(now.plusDays(1)),
          endDate = None
        )

      codeList.isValid(clock) shouldBe false
    }

    "should be invalid when end date is in the past" in {

      val codeList =
        CodeList(
          name = "CD001B",
          description = Some("Export Declaration"),
          startDate = None,
          endDate = Some(now.minusDays(1))
        )

      codeList.isValid(clock) shouldBe false
    }
  }
}
