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

package uk.gov.hmrc.automatedexportsystem.services

import cats.data.NonEmptyList
import helpers.EitherTFutureOps.{toEitherTLeft, toEitherTRight}
import org.mockito.Mockito.when
import org.scalatest.EitherValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.automatedexportsystem.config.AppConfig
import uk.gov.hmrc.automatedexportsystem.errors.MongoError
import uk.gov.hmrc.automatedexportsystem.models.IE507.{CorrelationId, EoriNumber, Mrn}
import uk.gov.hmrc.automatedexportsystem.models.mongo.SingleUpdateStatus
import uk.gov.hmrc.automatedexportsystem.models.mongo.read.MongoAesIE507MessageUnresolvedNotifications
import uk.gov.hmrc.automatedexportsystem.models.notification.{NotificationEvent, NotificationEventStatus}
import uk.gov.hmrc.automatedexportsystem.repositories.AesIE507Repository

import java.time.Instant
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationLong

class UnresolvedNotificationsServiceSpec extends AnyFreeSpecLike, Matchers, EitherValues, ScalaFutures, MockitoSugar:
  object TestData:
    val eoriNumber1: EoriNumber = EoriNumber("eoriNumber1")
    val eoriNumber2: EoriNumber = EoriNumber("eoriNumber2")

    val mrn1: Mrn = Mrn("mrn1")
    val mrn2: Mrn = Mrn("mrn2")

    val correlationId1: CorrelationId = CorrelationId("correlationId1")
    val correlationId2: CorrelationId = CorrelationId("correlationId2")

    val instant: Instant = Instant.parse("2026-10-09T00:00:00Z")

    val maxPendingTimeInMillis: Long = 1

    val notificationEvent1: NotificationEvent =
      NotificationEvent(
        correlationId = correlationId1,
        dateCreated = instant,
        dateUpdated = instant,
        isPending = true,
        status = NotificationEventStatus.Awaiting,
        errors = None
      )

    val notificationEvent2: NotificationEvent =
      NotificationEvent(
        correlationId = correlationId2,
        dateCreated = instant,
        dateUpdated = instant,
        isPending = true,
        status = NotificationEventStatus.Awaiting,
        errors = None
      )

    val mongoAesIE507MessageUnresolvedNotifications1: MongoAesIE507MessageUnresolvedNotifications =
      MongoAesIE507MessageUnresolvedNotifications(
        eoriNumber = eoriNumber1,
        mrn = mrn1,
        unresolvedEvents = NonEmptyList.of(
          notificationEvent1,
          notificationEvent2
        )
      )

    val mongoAesIE507MessageUnresolvedNotifications2: MongoAesIE507MessageUnresolvedNotifications =
      MongoAesIE507MessageUnresolvedNotifications(
        eoriNumber = eoriNumber2,
        mrn = mrn2,
        unresolvedEvents = NonEmptyList.of(
          notificationEvent1,
          notificationEvent2
        )
      )

  end TestData

  trait Setup:
    given ec: ExecutionContext = ExecutionContext.global

    val aesIE507Repository: AesIE507Repository = mock

    val appConfig: AppConfig = mock

    when(appConfig.unresolvedNotificationsDocumentMaxPendingTime).thenReturn(TestData.maxPendingTimeInMillis.milli)

    val unresolvedNotificationsService: UnresolvedNotificationsService =
      UnresolvedNotificationsService(aesIE507Repository, appConfig)

  "UnresolvedNotificationsService" - {

    ".updateUnresolvedNotifications" - {

      "should update all pending notification events last updated before the specified time" - {

        "and return a non-empty list of the updated notification events" - {

          "when all update operations are successful" in new Setup {
            when(
              aesIE507Repository
                .getUnresolvedNotificationsMessages(
                  TestData.instant.minusMillis(TestData.maxPendingTimeInMillis)
                )
            )
              .thenReturn(
                NonEmptyList
                  .of(
                    TestData.mongoAesIE507MessageUnresolvedNotifications1,
                    TestData.mongoAesIE507MessageUnresolvedNotifications2
                  )
                  .toEitherTRight
              )

            when(
              aesIE507Repository.updateNotification(
                TestData.eoriNumber1,
                TestData.mrn1,
                TestData.correlationId1,
                updatedAt = TestData.instant,
                status = NotificationEventStatus.Awaiting,
                errors = None
              )
            ).thenReturn(SingleUpdateStatus.Updated("update").toEitherTRight)

            when(
              aesIE507Repository.updateNotification(
                TestData.eoriNumber1,
                TestData.mrn1,
                TestData.correlationId2,
                updatedAt = TestData.instant,
                status = NotificationEventStatus.Awaiting,
                errors = None
              )
            ).thenReturn(SingleUpdateStatus.Updated("update").toEitherTRight)

            when(
              aesIE507Repository.updateNotification(
                TestData.eoriNumber2,
                TestData.mrn2,
                TestData.correlationId1,
                updatedAt = TestData.instant,
                status = NotificationEventStatus.Awaiting,
                errors = None
              )
            ).thenReturn(SingleUpdateStatus.Updated("update").toEitherTRight)

            when(
              aesIE507Repository.updateNotification(
                TestData.eoriNumber2,
                TestData.mrn2,
                TestData.correlationId2,
                updatedAt = TestData.instant,
                status = NotificationEventStatus.Awaiting,
                errors = None
              )
            ).thenReturn(SingleUpdateStatus.Updated("update").toEitherTRight)

            val result: List[NotificationEvent] =
              unresolvedNotificationsService
                .updateUnresolvedNotifications(TestData.instant)
                .value
                .futureValue
                .value

            result shouldBe List(
              TestData.notificationEvent1,
              TestData.notificationEvent2,
              TestData.notificationEvent1,
              TestData.notificationEvent2
            )
          }

          "when only some update operations are successful" in new Setup {
            when(
              aesIE507Repository
                .getUnresolvedNotificationsMessages(
                  TestData.instant.minusMillis(TestData.maxPendingTimeInMillis)
                )
            )
              .thenReturn(
                NonEmptyList
                  .of(
                    TestData.mongoAesIE507MessageUnresolvedNotifications1,
                    TestData.mongoAesIE507MessageUnresolvedNotifications2
                  )
                  .toEitherTRight
              )

            when(
              aesIE507Repository.updateNotification(
                TestData.eoriNumber1,
                TestData.mrn1,
                TestData.correlationId1,
                updatedAt = TestData.instant,
                status = NotificationEventStatus.Awaiting,
                errors = None
              )
            ).thenReturn(SingleUpdateStatus.Updated("update").toEitherTRight)

            when(
              aesIE507Repository.updateNotification(
                TestData.eoriNumber1,
                TestData.mrn1,
                TestData.correlationId2,
                updatedAt = TestData.instant,
                status = NotificationEventStatus.Awaiting,
                errors = None
              )
            ).thenReturn(MongoError.DocumentNotFound("").toEitherTLeft)

            when(
              aesIE507Repository.updateNotification(
                TestData.eoriNumber2,
                TestData.mrn2,
                TestData.correlationId1,
                updatedAt = TestData.instant,
                status = NotificationEventStatus.Awaiting,
                errors = None
              )
            ).thenReturn(MongoError.DocumentNotFound("").toEitherTLeft)

            when(
              aesIE507Repository.updateNotification(
                TestData.eoriNumber2,
                TestData.mrn2,
                TestData.correlationId2,
                updatedAt = TestData.instant,
                status = NotificationEventStatus.Awaiting,
                errors = None
              )
            ).thenReturn(SingleUpdateStatus.Updated("update").toEitherTRight)

            val result: List[NotificationEvent] =
              unresolvedNotificationsService
                .updateUnresolvedNotifications(TestData.instant)
                .value
                .futureValue
                .value

            result shouldBe List(TestData.notificationEvent1, TestData.notificationEvent2)
          }
        }

        "and return an empty list when no update operation is successful" in new Setup {
          when(
            aesIE507Repository
              .getUnresolvedNotificationsMessages(
                TestData.instant.minusMillis(TestData.maxPendingTimeInMillis)
              )
          )
            .thenReturn(
              NonEmptyList
                .of(
                  TestData.mongoAesIE507MessageUnresolvedNotifications1,
                  TestData.mongoAesIE507MessageUnresolvedNotifications2
                )
                .toEitherTRight
            )

          when(
            aesIE507Repository.updateNotification(
              TestData.eoriNumber1,
              TestData.mrn1,
              TestData.correlationId1,
              updatedAt = TestData.instant,
              status = NotificationEventStatus.Awaiting,
              errors = None
            )
          ).thenReturn(MongoError.DocumentNotFound("").toEitherTLeft)

          when(
            aesIE507Repository.updateNotification(
              TestData.eoriNumber1,
              TestData.mrn1,
              TestData.correlationId2,
              updatedAt = TestData.instant,
              status = NotificationEventStatus.Awaiting,
              errors = None
            )
          ).thenReturn(MongoError.DocumentNotFound("").toEitherTLeft)

          when(
            aesIE507Repository.updateNotification(
              TestData.eoriNumber2,
              TestData.mrn2,
              TestData.correlationId1,
              updatedAt = TestData.instant,
              status = NotificationEventStatus.Awaiting,
              errors = None
            )
          ).thenReturn(MongoError.DocumentNotFound("").toEitherTLeft)

          when(
            aesIE507Repository.updateNotification(
              TestData.eoriNumber2,
              TestData.mrn2,
              TestData.correlationId2,
              updatedAt = TestData.instant,
              status = NotificationEventStatus.Awaiting,
              errors = None
            )
          ).thenReturn(MongoError.DocumentNotFound("").toEitherTLeft)

          val result: List[NotificationEvent] =
            unresolvedNotificationsService
              .updateUnresolvedNotifications(TestData.instant)
              .value
              .futureValue
              .value

          result shouldBe Nil
        }
      }

      "should return a MongoError" - {

        "when there are no unresolved notifications messages found" in new Setup {
          when(
            aesIE507Repository
              .getUnresolvedNotificationsMessages(
                TestData.instant.minusMillis(TestData.maxPendingTimeInMillis)
              )
          ).thenReturn(MongoError.DocumentNotFound("").toEitherTLeft)

          val result: MongoError =
            unresolvedNotificationsService
              .updateUnresolvedNotifications(TestData.instant)
              .value
              .futureValue
              .left
              .value

          result shouldBe MongoError.DocumentNotFound("")
        }
      }
    }
  }
