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

import cats.data.{EitherT, NonEmptyList}
import cats.syntax.all.toTraverseOps
import play.api.Logging
import uk.gov.hmrc.automatedexportsystem.config.AppConfig
import uk.gov.hmrc.automatedexportsystem.errors.MongoError
import uk.gov.hmrc.automatedexportsystem.models.mongo.read.MongoAesIE507MessageUnresolvedNotifications
import uk.gov.hmrc.automatedexportsystem.models.notification.{NotificationEvent, NotificationEventStatus}
import uk.gov.hmrc.automatedexportsystem.repositories.AesIE507Repository

import java.time.{Instant, ZoneOffset}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UnresolvedNotificationsService @Inject() (
  aesIE507Repository: AesIE507Repository,
  appConfig:          AppConfig
)(using ExecutionContext)
    extends Logging:
  def updateUnresolvedNotifications(executionTime: Instant): EitherT[Future, MongoError, List[NotificationEvent]] =
    val maxPendingTimeMillis: Long =
      appConfig.unresolvedNotificationsDocumentMaxPendingTime.toMillis

    val updatedBefore: Instant = executionTime.minusMillis(maxPendingTimeMillis)

    val unresolvedNotificationsMessages: EitherT[Future, MongoError, NonEmptyList[MongoAesIE507MessageUnresolvedNotifications]] =
      aesIE507Repository.getUnresolvedNotificationsMessages(updatedBefore)

    unresolvedNotificationsMessages.semiflatMap(unresolvedMessages =>
      val updatedEvents: NonEmptyList[Future[Either[MongoError, NotificationEvent]]] =
        unresolvedMessages.flatMap(unresolvedMessage =>
          unresolvedMessage.unresolvedEvents
            .map(unresolvedEvent =>
              aesIE507Repository
                .updateNotification(
                  unresolvedMessage.eoriNumber,
                  unresolvedMessage.mrn,
                  unresolvedEvent.correlationId,
                  updatedAt = executionTime,
                  status = NotificationEventStatus.Awaiting,
                  errors = None
                )
                .map(_ =>
                  logUpdatedUnresolvedNotificationEvent(unresolvedMessage, unresolvedEvent)

                  unresolvedEvent
                )
                .value
            )
        )

      updatedEvents.sequence.map(events => events.collect { case Right(value) => value })
    )
  end updateUnresolvedNotifications

  private def logUpdatedUnresolvedNotificationEvent(
    unresolvedMessage: MongoAesIE507MessageUnresolvedNotifications,
    unresolvedEvent:   NotificationEvent
  ): Unit =
    val context: String =
      Map(
        "eoriNumber"      -> unresolvedMessage.eoriNumber.value,
        "mrn"             -> unresolvedMessage.mrn.value,
        "correlationId"   -> unresolvedEvent.correlationId.value,
        "lastDateUpdated" -> unresolvedEvent.dateUpdated.atZone(ZoneOffset.UTC)
      ).map { case (k, v) => s"$k: $v" }.mkString(", ")

    logger.info(s"Unresolved notification event has been resolved. $context")
