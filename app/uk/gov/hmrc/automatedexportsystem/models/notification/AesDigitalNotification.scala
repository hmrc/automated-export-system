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

import cats.data.NonEmptyList
import cats.implicits.catsSyntaxTuple6Semigroupal
import uk.gov.hmrc.automatedexportsystem.xml.{XmlPath, XmlReader, XmlRootTag}

import java.time.LocalDateTime

final case class AesDigitalNotification(
  correlationId:      String,
  eori:               String,
  mrn:                String,
  dateCreated:        LocalDateTime,
  status:             NotificationStatus,
  notificationErrors: Option[NonEmptyList[NotificationError]]
)

object AesDigitalNotification:
  given aesDigitalNotificationTag: XmlRootTag[AesDigitalNotification] = XmlRootTag("notification")

  given aesDigitalNotificationXmlReader: XmlReader[AesDigitalNotification] =
    XmlReader.nonEmptyReader { (xml, path) =>
      (
        (XmlPath \ "correlationId").read[String](xml, path),
        (XmlPath \ "eori").read[String](xml, path),
        (XmlPath \ "mrn").read[String](xml, path),
        (XmlPath \ "dateCreated").read[LocalDateTime](xml, path),
        (XmlPath \ "status").read[NotificationStatus](xml, path),
        (XmlPath \ "errors").readRoot[Option[NonEmptyList[NotificationError]]](xml, path)
      ).mapN(AesDigitalNotification.apply)
    }
