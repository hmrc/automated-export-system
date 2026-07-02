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
import org.scalatest.prop.{TableDrivenPropertyChecks, TableFor1, TableFor2}
import uk.gov.hmrc.automatedexportsystem.models.codelists.CodeList

import java.time.{Clock, Instant, ZoneOffset}

class CodeListSpec extends AnyFreeSpecLike with Matchers with TableDrivenPropertyChecks {
  private val clock: Clock =
    Clock.fixed(Instant.parse("2026-06-23T09:00:00Z"), ZoneOffset.UTC)

  private val codeListValues: Seq[CodeList] =
    Seq(
      CodeList.CL060,
      CodeList.CL347,
      CodeList.CL165,
      CodeList.CL018,
      CodeList.CL094
    )

  "CodeList" - {
    ".values" - {
      "should return the list of concrete code lists" in {
        CodeList.values shouldBe codeListValues
      }
    }

    ".valueOf" - {
      val codeListNamesWithValuesTable: TableFor2[String, CodeList] =
        Table(
          ("name", "codeList"),
          codeListValues.map(codeList => (codeList.name, codeList))*
        )

      "should return the correct code list object" in
        forAll(codeListNamesWithValuesTable) { case (name, codeList) =>
          CodeList.valueOf(name) shouldBe Some(codeList)
        }
    }
  }

  // can't really test more than this since our code lists don't have any dates, so I think this
  // should suffice for now
  "CodeList" - {
    val codeListValuesTable: TableFor1[CodeList] =
      Table("name", codeListValues*)

    "should be valid when no dates are defined" in
      forAll(codeListValuesTable) { codeList =>
        codeList.isValid(clock) shouldBe true
      }
  }
}
