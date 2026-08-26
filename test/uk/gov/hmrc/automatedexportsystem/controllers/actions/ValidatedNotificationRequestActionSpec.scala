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
import org.mockito.Mockito.when
import play.api.http.HttpVerbs
import play.api.mvc.*
import play.api.test.FakeRequest
import play.api.test.Helpers.await
import uk.gov.hmrc.automatedexportsystem.helpers.{AllMocks, BaseSpec}

import scala.xml.Elem

class ValidatedNotificationRequestActionSpec extends BaseSpec with AllMocks:
  trait Setup:
    when(mockAppConfig.notificationToken).thenReturn("some-token")

    val playBodyParsers = play.api.mvc.PlayBodyParsers()
    val parser: BodyParsers.Default = new BodyParsers.Default(playBodyParsers)

    val action = new ValidatedNotificationRequestAction(
      parser,
      mockAppConfig
    )

  "ValidatedNotificationRequestAction.refine" - {
    "return success when a valid notification token and xml body is provided" in new Setup {
      val payload: Elem                     = <xml>some xml</xml>
      val request: Request[AnyContentAsXml] =
        FakeRequest(HttpVerbs.POST, "/")
          .withHeaders("Authorization" -> "some-token")
          .withXmlBody(payload)

      val result = await(action.refine(request))

      result shouldBe Right(ValidatedNotificationRequest(request))
    }

    "return 400 when a valid notification token but invalid xml payload provided" in new Setup {
      val request: Request[AnyContentAsText] =
        FakeRequest(HttpVerbs.POST, "/")
          .withHeaders("Authorization" -> "some-token")
          .withBody(AnyContentAsText("<xmlx>not xml</xml>"))

      val result = await(action.refine(request))
      result.left.toOption.value.header.status shouldBe BAD_REQUEST
    }

    "return 400 when a valid notification token is provided with no xml body" in new Setup {
      val request: Request[AnyContentAsEmpty.type] =
        FakeRequest(HttpVerbs.POST, "/")
          .withHeaders("Authorization" -> "some-token")

      val result = await(action.refine(request))

      result.left.toOption.value.header.status shouldBe BAD_REQUEST
    }

    "return 401 when an invalid notification token" in new Setup {
      val request: Request[AnyContentAsEmpty.type] =
        FakeRequest(HttpVerbs.POST, "/")
          .withHeaders("Authorization" -> "wrong-token")

      val result = await(action.refine(request))

      result.left.toOption.value.header.status shouldBe UNAUTHORIZED
    }

    "return 401 when no authorization header is provided" in new Setup {
      val request: Request[AnyContentAsEmpty.type] =
        FakeRequest(HttpVerbs.POST, "/")

      val result = await(action.refine(request))

      result.left.toOption.value.header.status shouldBe UNAUTHORIZED
    }

  }
