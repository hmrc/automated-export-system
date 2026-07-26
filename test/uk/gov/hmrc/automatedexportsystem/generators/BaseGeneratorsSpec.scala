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

package uk.gov.hmrc.automatedexportsystem.generators

import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class BaseGeneratorsSpec extends AnyFreeSpecLike, Matchers, ScalaCheckDrivenPropertyChecks, BaseGenerators:
  "BaseGenerators" - {

    ".chronologicalInstantsArb" - {

      "should generate a tuple of 2 Instants where the second is not before the first" in
        forAll(chronologicalInstantsArb.arbitrary) { case (instant1, instant2) =>
          !instant2.isBefore(instant1) shouldBe true
        }
    }

    ".nonEmptyListArb" - {

      "should generate a NonEmptyList" in {
        noException shouldBe thrownBy {
          forAll(nonEmptyListArb[Unit].arbitrary) { _ =>
            true
          }
        }
      }
    }
  }
