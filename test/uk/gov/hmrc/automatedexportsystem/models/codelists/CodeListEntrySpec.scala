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
import play.api.libs.json.Json

import java.time.{Clock, Instant, LocalDate, ZoneOffset}

class CodeListEntrySpec extends AnyWordSpec with Matchers {

  private val clock =
    Clock.fixed(
      Instant.parse("2026-11-04T12:00:00Z"),
      ZoneOffset.UTC
    )

  private val today = LocalDate.of(2026, 11, 4)

  private val entry = CodeListEntry(
    key = "1",
    value = "Sea transport",
    state = "valid",
    startDate = today.minusDays(1)
  )

  "CodeListEntry.isValid" should {

    "include a valid entry whose start date is in the past" in {
      entry.isValid(clock) shouldBe true
    }

    "include an entry starting today" in {
      entry.copy(startDate = today).isValid(clock) shouldBe true
    }

    "exclude an entry starting in the future" in {
      entry.copy(startDate = today.plusDays(1)).isValid(clock) shouldBe false
    }

    "exclude an invalid entry" in {
      entry.copy(state = "invalid").isValid(clock) shouldBe false
    }

    "ignore case and surrounding spaces in the state" in {
      entry.copy(state = " VALID ").isValid(clock) shouldBe true
    }

    "exclude an expired entry" in {
      entry.copy(endDate = Some(today.minusDays(1))).isValid(clock) shouldBe false
    }

    "include an entry on its last valid day" in {
      entry.copy(endDate = Some(today)).isValid(clock) shouldBe true
    }

    "include an entry with a future end date" in {
      entry.copy(endDate = Some(today.plusDays(1))).isValid(clock) shouldBe true
    }
  }

  "CodeListEntry JSON writer" should {

    "return only key, value and properties" in {
      Json.toJson(entry) shouldBe Json.obj(
        "key" -> "1",
        "value" -> "Sea transport",
        "properties" -> Json.obj()
      )
    }
  }
}