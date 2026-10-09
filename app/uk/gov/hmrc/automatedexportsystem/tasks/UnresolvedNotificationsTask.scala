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

package uk.gov.hmrc.automatedexportsystem.tasks

import org.apache.pekko.actor.ActorSystem
import play.api.Logging
import uk.gov.hmrc.automatedexportsystem.config.AppConfig
import uk.gov.hmrc.automatedexportsystem.errors.MongoError
import uk.gov.hmrc.automatedexportsystem.services.UnresolvedNotificationsService

import java.time.{Clock, Instant, ZoneOffset}
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

class UnresolvedNotificationsTask @Inject() (
  unresolvedNotificationsService: UnresolvedNotificationsService,
  actorSystem:                    ActorSystem,
  appConfig:                      AppConfig,
  clock:                          Clock
)(using ExecutionContext)
    extends Logging:
  private val isTaskRunning: AtomicBoolean = AtomicBoolean(false)

  actorSystem.scheduler.scheduleAtFixedRate(
    initialDelay = 0.millis,
    interval = appConfig.unresolvedNotificationsSchedulerInterval
  ) { () =>
    if isTaskRunning.compareAndSet(false, true) then
      executeTask(Instant.now(clock))
        .onComplete(_ => isTaskRunning.set(false))
  }

  private def executeTask(startTime: Instant): Future[Unit] =
    logger.info(
      s"Scheduled task has been STARTED. " +
        s"startTime: ${startTime.atZone(ZoneOffset.UTC)}"
    )

    unresolvedNotificationsService
      .updateUnresolvedNotifications(startTime)
      .value
      .map {
        case Left(mongoError) =>
          mongoError match
            case MongoError.DocumentNotFound(_) =>
              logger.info(
                "Scheduled task has been COMPLETED. There were no unresolved notifications detected"
              )
            case me =>
              logger.warn(s"Scheduled task has FAILED due to: $me")
        case Right(resolvedNotificationEvents) =>
          val numberOfResolvedNotificationEvents: Int = resolvedNotificationEvents.length

          val message: String =
            s"Scheduled task has been COMPLETED. " +
              (if numberOfResolvedNotificationEvents > 0
               then s"numberOfNotificationsResolved: $numberOfResolvedNotificationEvents"
               else "There were no unresolved notifications remaining after initial detection")

          logger.info(message)
      }
  end executeTask
end UnresolvedNotificationsTask
