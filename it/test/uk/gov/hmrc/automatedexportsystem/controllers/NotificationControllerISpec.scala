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

import cats.data.NonEmptyList
import helpers.XmlOps
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import play.api.inject
import play.api.inject.Binding
import play.api.mvc.{AnyContentAsXml, Result}
import play.api.test.Helpers.*
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.automatedexportsystem.generators.MongoAesIE507MessageGenerator
import uk.gov.hmrc.automatedexportsystem.helpers.BaseISpec
import uk.gov.hmrc.automatedexportsystem.models.IE507.{CorrelationId, EoriNumber, ExportOperationType, Mrn}
import uk.gov.hmrc.automatedexportsystem.models.mongo.write.MongoAesIE507Message
import uk.gov.hmrc.automatedexportsystem.models.notification.{NotificationEvent, NotificationEventStatus}
import uk.gov.hmrc.automatedexportsystem.repositories.AesIE507RepositoryImpl

import java.time.{Clock, Instant}
import scala.concurrent.Future
import scala.xml.{Elem, XML as Xml}

class NotificationControllerISpec extends BaseISpec with MongoAesIE507MessageGenerator:
  object TestData:
    val endpoint = "/automated-export-system/notification"

    val instant:       Instant       = Instant.parse("2026-09-25T00:00:00.000Z")
    val eoriNumber:    EoriNumber    = EoriNumber("GB123456789000")
    val mrn:           Mrn           = Mrn("25GB1234567890ABCDE")
    val correlationId: CorrelationId = CorrelationId("correlationId")

    val validPayload: Elem =
      <notification>
        <correlationId>{correlationId.value}</correlationId>
        <eori>{eoriNumber.value}</eori>
        <mrn>{mrn.value}</mrn>
        <dateCreated>2026-08-12T10:15:30</dateCreated>
        <status>1</status>
      </notification>

    val invalidPayload =
      """<not-notification>
        |      <status>1</status>
        |    </notification>""".stripMargin

    val invalidXmlPayload =
      <notification>
        <status>1</status>
      </notification>
  end TestData

  val aesIE507Repository: AesIE507RepositoryImpl =
    app.injector.instanceOf[AesIE507RepositoryImpl]

  lazy val clock: Clock = mock[Clock]

  override def bindingOverrides: Seq[Binding[_]] =
    super.bindingOverrides ++ Seq(
      inject.bind[Clock].toInstance(clock)
    )

  override def beforeEach(): Unit =
    super.beforeEach()
    await(aesIE507Repository.collection.drop().head())

  "POST /notification" - {

    "return 204 when authorization header is valid and payload is valid" - {

      "when there is a submission found with that eori and mrn" - {

        "where the most recent notification event with that correlation id is pending" in {
          when(clock.instant()).thenReturn(TestData.instant.plusMillis(1))

          val generatedMongoAesIE507Message: MongoAesIE507Message =
            arbitrary[MongoAesIE507Message].sample.value

          val notificationEvent: NotificationEvent =
            NotificationEvent(
              correlationId = TestData.correlationId,
              dateCreated = TestData.instant,
              dateUpdated = TestData.instant,
              isPending = true,
              status = NotificationEventStatus.Awaiting,
              errors = None
            )

          val mongoAesIE507Message: MongoAesIE507Message =
            generatedMongoAesIE507Message.copy(
              eoriNumber = TestData.eoriNumber,
              exportOperation = generatedMongoAesIE507Message.exportOperation.copy(
                exportOperationType = ExportOperationType.Standard,
                mrn = TestData.mrn
              ),
              metadata = NonEmptyList.one(notificationEvent)
            )

          await(
            aesIE507Repository.collection
              .insertOne(mongoAesIE507Message)
              .head()
          )

          val request = FakeRequest(Helpers.POST, TestData.endpoint)
            .withHeaders("Authorization" -> "some-token")
            .withXmlBody(TestData.validPayload)

          val result = Helpers.route(app, request).value

          Helpers.status(result) shouldBe Helpers.NO_CONTENT

          val updatedNotificationEvent: NotificationEvent =
            notificationEvent.copy(
              dateUpdated = TestData.instant.plusMillis(1),
              isPending = false,
              status = NotificationEventStatus.Accepted
            )

          val mongoAesIe507MessageAfterUpdate: MongoAesIE507Message =
            mongoAesIE507Message.copy(metadata = NonEmptyList.of(updatedNotificationEvent))

          val updatedMongoAesIE507Message: MongoAesIE507Message =
            aesIE507Repository
              .getMessageByNotification(
                TestData.eoriNumber,
                TestData.mrn,
                TestData.correlationId
              )
              .value
              .futureValue
              .value

          updatedMongoAesIE507Message shouldBe mongoAesIe507MessageAfterUpdate
        }

        "where the most recent notification event with that correlation id is diverted" in {
          when(clock.instant()).thenReturn(TestData.instant.plusMillis(1))

          val generatedMongoAesIE507Message: MongoAesIE507Message =
            arbitrary[MongoAesIE507Message].sample.value

          val divertedNotificationEvent: NotificationEvent =
            NotificationEvent(
              correlationId = TestData.correlationId,
              dateCreated = TestData.instant,
              dateUpdated = TestData.instant,
              isPending = false,
              status = NotificationEventStatus.Awaiting,
              errors = None
            )

          val mongoAesIE507Message: MongoAesIE507Message =
            generatedMongoAesIE507Message.copy(
              eoriNumber = TestData.eoriNumber,
              exportOperation = generatedMongoAesIE507Message.exportOperation.copy(
                exportOperationType = ExportOperationType.Standard,
                mrn = TestData.mrn
              ),
              metadata = NonEmptyList.one(divertedNotificationEvent)
            )

          await(
            aesIE507Repository.collection
              .insertOne(mongoAesIE507Message)
              .head()
          )

          val request: FakeRequest[AnyContentAsXml] = FakeRequest(Helpers.POST, TestData.endpoint)
            .withHeaders("Authorization" -> "some-token")
            .withXmlBody(TestData.validPayload)

          val result: Future[Result] = Helpers.route(app, request).value

          Helpers.status(result) shouldBe Helpers.NO_CONTENT

          val pushedNotificationEvent: NotificationEvent =
            NotificationEvent(
              correlationId = TestData.correlationId,
              dateCreated = TestData.instant.plusMillis(1),
              dateUpdated = TestData.instant.plusMillis(1),
              isPending = false,
              status = NotificationEventStatus.Accepted,
              errors = None
            )

          val mongoAesIe507MessageAfterPush: MongoAesIE507Message =
            mongoAesIE507Message.copy(metadata =
              NonEmptyList.of(
                pushedNotificationEvent,
                divertedNotificationEvent
              )
            )

          val updatedMongoAesIE507Message: MongoAesIE507Message =
            aesIE507Repository
              .getMessageByNotification(
                TestData.eoriNumber,
                TestData.mrn,
                TestData.correlationId
              )
              .value
              .futureValue
              .value

          updatedMongoAesIE507Message shouldBe mongoAesIe507MessageAfterPush
        }
      }
    }

    "return 401 when authorization header is invalid" in {
      val request = FakeRequest(Helpers.POST, TestData.endpoint)
        .withHeaders("Authorization" -> "invalid-token")
        .withXmlBody(TestData.validPayload)

      val result = Helpers.route(app, request).value
      Helpers.status(result)      shouldBe Helpers.UNAUTHORIZED
      Helpers.contentType(result) shouldBe Some("application/xml")
      val resultXml = Xml.loadString(Helpers.contentAsString(result))
      (resultXml \ "code").text.trim shouldBe "UNAUTHORIZED"
    }

    "return 415 when authorization header is valid and payload is missing" in {
      val request = FakeRequest(Helpers.POST, TestData.endpoint)
        .withHeaders("Authorization" -> "some-token")

      val result = Helpers.route(app, request).value
      Helpers.status(result)      shouldBe Helpers.UNSUPPORTED_MEDIA_TYPE
      Helpers.contentType(result) shouldBe Some("application/xml")
      val resultXml = Xml.loadString(Helpers.contentAsString(result))
      (resultXml \ "code").text shouldBe "UNSUPPORTED_MEDIA_TYPE"
    }

    "return 422 when authorization header is valid and payload is invalid" in {
      val request = FakeRequest(Helpers.POST, TestData.endpoint)
        .withHeaders("Authorization" -> "some-token")
        .withBody(TestData.invalidPayload)

      val result = Helpers.route(app, request).value
      Helpers.status(result)      shouldBe Helpers.UNSUPPORTED_MEDIA_TYPE
      Helpers.contentType(result) shouldBe Some("application/xml")
      val resultXml = Xml.loadString(Helpers.contentAsString(result))
      (resultXml \ "code").text shouldBe "UNSUPPORTED_MEDIA_TYPE"
    }

    "return 422 when authorization header is valid and payload is invalid xml" in {
      val request = FakeRequest(Helpers.POST, TestData.endpoint)
        .withHeaders("Authorization" -> "some-token")
        .withBody(TestData.invalidXmlPayload)

      val result = Helpers.route(app, request).value
      Helpers.status(result)      shouldBe Helpers.UNPROCESSABLE_ENTITY
      Helpers.contentType(result) shouldBe Some("application/xml")
      val resultXml = Xml.loadString(Helpers.contentAsString(result))
      (resultXml \ "code").text shouldBe "UNPROCESSABLE_ENTITY"
    }

    "should return a 500 response when authorization header is valid and payload is valid" - {

      "when there is a submission found with that eori and mrn" - {

        "where the most recent notification event with that correlation id is not pending" in {
          when(clock.instant()).thenReturn(TestData.instant.plusMillis(1))

          val generatedMongoAesIE507Message: MongoAesIE507Message =
            arbitrary[MongoAesIE507Message].sample.value

          val notificationEvent: NotificationEvent =
            NotificationEvent(
              correlationId = TestData.correlationId,
              dateCreated = TestData.instant,
              dateUpdated = TestData.instant,
              isPending = false,
              status = NotificationEventStatus.Cancelled,
              errors = None
            )

          val mongoAesIE507Message: MongoAesIE507Message =
            generatedMongoAesIE507Message.copy(
              eoriNumber = TestData.eoriNumber,
              exportOperation = generatedMongoAesIE507Message.exportOperation.copy(
                exportOperationType = ExportOperationType.Standard,
                mrn = TestData.mrn
              ),
              metadata = NonEmptyList.one(notificationEvent)
            )

          await(
            aesIE507Repository.collection
              .insertOne(mongoAesIE507Message)
              .head()
          )

          val request: FakeRequest[AnyContentAsXml] = FakeRequest(Helpers.POST, TestData.endpoint)
            .withHeaders("Authorization" -> "some-token")
            .withXmlBody(TestData.validPayload)

          val errorMessage: String =
            s"Submission not found. EORI: ${TestData.eoriNumber.value}, " +
              s"MRN: ${TestData.mrn.value}, correlationId: ${TestData.correlationId.value}"

          val updateNotificationFailureXml: Elem =
            <errorResponse>
              <status>404</status>
              <code>NOT_FOUND</code>
              <message>{errorMessage}</message>
            </errorResponse>

          val result: Future[Result] = Helpers.route(app, request).value

          val resultContent: String = Helpers.contentAsString(result)
          val resultXml:     Elem   = XmlOps.loadXmlFromString(resultContent).value

          Helpers.status(result)      shouldBe Helpers.NOT_FOUND
          Helpers.contentType(result) shouldBe Some(Helpers.XML)
          XmlOps.normalize(resultXml) shouldBe XmlOps.normalize(updateNotificationFailureXml)
        }
      }
    }
  }
