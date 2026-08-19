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

package uk.gov.hmrc.automatedexportsystem.controllers

import org.mockito.Mockito.when
import play.api.mvc.*
import play.api.test.Helpers.POST
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.automatedexportsystem.controllers.actions.ValidatedNotificationRequestAction
import uk.gov.hmrc.automatedexportsystem.helpers.{AllMocks, BaseSpec}

class NotificationControllerSpec extends BaseSpec, AllMocks:
  trait Setup:
    implicit val controllerComponents: ControllerComponents = Helpers.stubControllerComponents(executionContext = ec)

    val playBodyParsers = play.api.mvc.PlayBodyParsers()
    val parser: BodyParsers.Default = new BodyParsers.Default(playBodyParsers)

    when(mockAppConfig.notificationToken).thenReturn("some-token")
    val notificationAction = new ValidatedNotificationRequestAction(
      parser,
      mockAppConfig
    )

    val controller =
      new NotificationController(controllerComponents, notificationAction)

    val fakeRequest = FakeRequest(POST, "notification")

  "notification" - {
    "authorization header is valid and payload is valid" in new Setup {
      val request = fakeRequest
        .withHeaders("Authorization" -> "some-token")
        .withTextBody("<valid>xml</valid>")
      val result = controller.notification(request)
      Helpers.status(result) shouldBe NO_CONTENT
    }

    "authorization header is invalid" in new Setup {
      val request = fakeRequest
        .withHeaders("Authorization" -> "invalid-token")
        .withTextBody("<valid>xml</valid>")
      val result = controller.notification(request)
      Helpers.status(result) shouldBe UNAUTHORIZED
    }

    "authorization header is valid and payload is missing" in new Setup {
      val request = fakeRequest
        .withHeaders("Authorization" -> "some-token")
      val result = controller.notification(request)
      Helpers.status(result) shouldBe BAD_REQUEST
    }

    "authorization header is valid and payload is invalid" in new Setup {
      val request = fakeRequest
        .withHeaders("Authorization" -> "some-token")
        .withTextBody("<valid>xml</invalid>")
      val result = controller.notification(request)
      Helpers.status(result) shouldBe BAD_REQUEST
    }
  }
