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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.*
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.automatedexportsystem.config.AppConfig
import uk.gov.hmrc.automatedexportsystem.helpers.{AllMocks, BaseSpec}

import scala.concurrent.Future
import scala.xml.NodeSeq

class AesAuthActionSpec extends BaseSpec with AllMocks {

  trait Setup {
    implicit val appConfig: AppConfig = mockAppConfig
    val authenticatedAction =
      new AesAuthAction(authConnector = mockAuthConnector)
  }
  "AesAuthAction" - {

    "must execute the supplied body and return the request content successfully" in new Setup {
      val eori                = "some-eori"
      val enrolmentIdentifier = EnrolmentIdentifier("EORINumber", eori)
      val enrolments          = Enrolments(Set(Enrolment("HMRC-CUS-ORG", Seq(enrolmentIdentifier), "active")))

      when(mockAuthConnector.authorise[Enrolments](any(), any())(any(), any()))
        .thenReturn(Future.successful(enrolments))

      val action = stubControllerComponents().actionBuilder(stubControllerComponents().parsers.xml) { (_: Request[NodeSeq]) =>
        Results.Ok(Json.obj("EORINumber" -> eori))
      }

      val request = FakeRequest(POST, "/dummy-uri")
        .withHeaders(play.api.http.HeaderNames.AUTHORIZATION -> "Bearer valid-token")
        .withXmlBody(<root></root>)

      val essentialAction: EssentialAction = authenticatedAction.apply(action)
      val result:          Future[Result]  = call(essentialAction, request)

      status(result) shouldBe OK
      val json:      JsValue = contentAsJson(result)
      val eoriValue: String  = (json \ "EORINumber").as[String]

      eoriValue shouldBe eori
    }

    "must return a 401 if a bearer token is not provided" in new Setup {
      val enrolmentIdentifier = EnrolmentIdentifier("EORINumber", "*")
      val enrolments          = Enrolments(Set(Enrolment("HMRC-CUS-ORG", Seq(enrolmentIdentifier), "active")))

      when(mockAuthConnector.authorise[Enrolments](any(), any())(any(), any()))
        .thenReturn(Future.successful(enrolments))

      val action = stubControllerComponents().actionBuilder(stubControllerComponents().parsers.xml) { (_: Request[NodeSeq]) =>
        Results.Ok(Json.obj("EORINumber" -> "*"))
      }

      val request = FakeRequest(POST, "/dummy-uri")
        .withXmlBody(<root></root>)

      val essentialAction: EssentialAction = authenticatedAction.apply(action)
      val result:          Future[Result]  = call(essentialAction, request)

      status(result) shouldBe UNAUTHORIZED
      verify(mockAuthConnector, times(0)).authorise[Enrolments](any(), any())(any(), any())

    }
    "must return a 401 if the required enrolment is missing" in new Setup {
      val eori                = "some-eori"
      val enrolmentIdentifier = EnrolmentIdentifier("INVALIDNumber", eori)
      val enrolments          = Enrolments(Set(Enrolment("HMRC-INVALID", Seq(enrolmentIdentifier), "active")))

      when(mockAuthConnector.authorise[Enrolments](any(), any())(any(), any()))
        .thenReturn(Future.successful(enrolments))

      val action = stubControllerComponents().actionBuilder(stubControllerComponents().parsers.xml) { (_: Request[NodeSeq]) =>
        Results.Ok(Json.obj("EORINumber" -> eori))
      }

      val request = FakeRequest(POST, "/dummy-uri")
        .withHeaders(play.api.http.HeaderNames.AUTHORIZATION -> "Bearer valid-token")
        .withXmlBody(<root></root>)

      val essentialAction: EssentialAction = authenticatedAction.apply(action)
      val result:          Future[Result]  = call(essentialAction, request)

      status(result) shouldBe UNAUTHORIZED
    }
  }

}
