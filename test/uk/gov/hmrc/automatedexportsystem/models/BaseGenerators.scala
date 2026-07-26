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

package uk.gov.hmrc.automatedexportsystem.models

import cats.data.NonEmptyList
import org.scalacheck.{Arbitrary, Gen}

import java.time.Instant

trait BaseGenerators:
  given chronologicalInstantsArb: Arbitrary[(Instant, Instant)] =
    val minOffset: Long = 0L
    // realistic ~10 years offset
    val maxOffset: Long = 315360000L

    Arbitrary {
      for
        seconds <- Gen.choose(Instant.EPOCH.getEpochSecond, Instant.MAX.getEpochSecond - maxOffset)
        instant1 = Instant.ofEpochSecond(seconds)
        offset <- Gen.choose(minOffset, maxOffset)
        instant2 = instant1.plusSeconds(offset)
      yield (instant1, instant2)
    }

  given nonEmptyListArb[T](using arb: Arbitrary[T]): Arbitrary[NonEmptyList[T]] =
    Arbitrary {
      Gen.nonEmptyListOf(arb.arbitrary).map(NonEmptyList.fromListUnsafe)
    }
