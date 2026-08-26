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
import uk.gov.hmrc.automatedexportsystem.controllers.actions.{ValidatedNotificationRequestAction, XmlNotificationPayloadActionRefiner}
import uk.gov.hmrc.automatedexportsystem.helpers.{AllMocks, BaseSpec}

import scala.xml.Elem

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
    val xmlNotificationPayloadActionRefiner = new XmlNotificationPayloadActionRefiner()

    val controller =
      new NotificationController(controllerComponents, notificationAction, xmlNotificationPayloadActionRefiner)

    val fakeRequest = FakeRequest(POST, "notification")

    val validPayload: Elem =
      <notification>
        <correlationId>8f3c2a19-7d2b-4b74-a9f0-123456789012</correlationId>
        <eori>GB123456789000</eori>
        <mrn>25GB1234567890ABCDE</mrn>
        <dateCreated>2026-08-12T10:15:30Z</dateCreated>
        <status>1</status>
      </notification>

  val invalidPayload: Elem =
    <someXml>data</someXml>

  "notification" - {
    "authorization header is valid and payload is valid" in new Setup {
      val request = fakeRequest
        .withHeaders("Authorization" -> "some-token")
        .withXmlBody(validPayload)

      val result = controller.notification(request)
      Helpers.status(result) shouldBe NO_CONTENT
    }

    "authorization header is invalid" in new Setup {
      val request = fakeRequest
        .withHeaders("Authorization" -> "invalid-token")
        .withXmlBody(validPayload)
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
        .withXmlBody(invalidPayload)
      val result = controller.notification(request)
      Helpers.status(result) shouldBe BAD_REQUEST
    }
  }
