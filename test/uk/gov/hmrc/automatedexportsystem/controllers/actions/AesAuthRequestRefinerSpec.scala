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

package uk.gov.hmrc.automatedexportsystem.controllers.actions
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.FakeRequest
import uk.gov.hmrc.automatedexportsystem.controllers.actions.request.AesAuthAttr
import uk.gov.hmrc.automatedexportsystem.helpers.BaseSpec
import uk.gov.hmrc.automatedexportsystem.models.aesIE507.EoriNumber

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class AesAuthRequestRefinerSpec extends BaseSpec:
  private val refiner = new AesAuthRequestRefiner
  private val eori    = EoriNumber("GB123456789000")

  "AesAuthRequestRefiner.refine" - {

    "return Right(AesAuthRequest) when EORI attr is present" in {
      val request: Request[AnyContentAsEmpty.type] =
        FakeRequest("POST", "/")
          .addAttr(AesAuthAttr.Eori, eori.value)

      val result = Await.result(refiner.refine(request), 2.seconds)

      result match
        case Right(aesAuthRequest) =>
          aesAuthRequest.eori    shouldBe eori
          aesAuthRequest.request shouldBe request
        case Left(_) =>
          fail("Expected Right(AesAuthRequest) but got Left")
    }

    "return Left(Unauthorized) when EORI attr is missing" in {
      val request = FakeRequest("POST", "/")

      val result = Await.result(refiner.refine(request), 2.seconds)

      result                                   shouldBe a[Left[_, _]]
      result.left.toOption.value.header.status shouldBe 401
    }
  }
