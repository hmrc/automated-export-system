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

package uk.gov.hmrc.automatedexportsystem.models.http

import play.api.http.HeaderNames
import uk.gov.hmrc.automatedexportsystem.helpers.BaseSpec

import java.time.{Clock, Instant, ZoneOffset}

class HttpHeaderSpec extends BaseSpec:

  "HttpHeader.date" - {
    "format the current time as UTC with a two-digit day" in {
      given Clock =
        Clock.fixed(
          Instant.parse("2026-10-01T12:34:56Z"),
          ZoneOffset.UTC
        )

      val header = HttpHeader.date

      header.name       shouldBe HeaderNames.DATE
      header.value      shouldBe "Thu, 01 Oct 2026 12:34:56 UTC"
      header.normalized shouldBe (HeaderNames.DATE, "Thu, 01 Oct 2026 12:34:56 UTC")
    }
  }
