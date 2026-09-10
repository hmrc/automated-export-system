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

package uk.gov.hmrc.automatedexportsystem.models.mongo.write

import play.api.libs.json.*

enum NotificationEventStatus(val status: Int):
  case Awaiting extends NotificationEventStatus(0)
  case Accepted extends NotificationEventStatus(1)
  case Amended extends NotificationEventStatus(2)
  case Cancelled extends NotificationEventStatus(3)
  case Rejected extends NotificationEventStatus(4)

object NotificationEventStatus:
  given mongoFormat: Format[NotificationEventStatus] =
    Format(
      Reads.IntReads.flatMapResult(value =>
        NotificationEventStatus.values
          .find(_.status == value)
          .fold(JsError("error.expected.notificationeventstatus"))(JsSuccess(_))
      ),
      Writes.IntWrites.contramap(_.status)
    )
