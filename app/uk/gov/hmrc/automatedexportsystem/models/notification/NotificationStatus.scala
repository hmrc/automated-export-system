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

package uk.gov.hmrc.automatedexportsystem.models.notification

import cats.implicits.catsSyntaxOption
import uk.gov.hmrc.automatedexportsystem.errors.XmlReaderError
import uk.gov.hmrc.automatedexportsystem.xml.XmlReader

enum NotificationStatus(val status: Int):
  case Accepted extends NotificationStatus(1)
  case Rejected extends NotificationStatus(2)
  case Diversion extends NotificationStatus(5)

object NotificationStatus:
  given notificationStatusXmlReader: XmlReader[NotificationStatus] =
    XmlReader.intReader.flatMapResult { (value, path) =>
      NotificationStatus.values
        .find(_.status == value)
        .toValidNel(
          XmlReaderError.ParseError(
            path.toString,
            s"Failed to parse '$value' to NotificationStatus"
          )
        )
    }
