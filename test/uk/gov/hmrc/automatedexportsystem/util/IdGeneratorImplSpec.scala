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

package uk.gov.hmrc.automatedexportsystem.util

import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers

import java.util.UUID

class IdGeneratorImplSpec extends AnyFreeSpecLike, Matchers:
  val uuid: UUID = UUID.fromString("6fb33641-6dc7-4a4f-adef-06238c13a317")

  val idGenerator: IdGenerator = Mockito.spy(IdGeneratorImpl())

  when(idGenerator.generate).thenReturn(uuid)

  "IdGenerator" - {

    ".generateNoHyphen" - {

      "should return a 32 char hyphen-less UUID string" in {
        val id: String = idGenerator.generateNoHyphen

        id shouldBe "6fb336416dc74a4fadef06238c13a317"
      }
    }
  }
