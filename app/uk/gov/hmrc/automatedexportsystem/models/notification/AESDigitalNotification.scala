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

import scala.xml.NodeSeq

final case class AESDigitalNotification(
  correlationId:      String,
  eori:               String,
  mrn:                String,
  dateCreated:        String,
  status:             Int,
  notificationErrors: Option[Seq[NotificationError]] = None
)

object AESDigitalNotification:

  def fromXml(xml: NodeSeq): AESDigitalNotification = {
    val notificationErrors = (xml \\ "notificationErrors").headOption
      .map { errorsNode =>
        (errorsNode \\ "notificationError").map { errorNode =>
          NotificationError(
            code = (errorNode \\ "code").text.trim,
            description = (errorNode \\ "description").text.trim,
            path = {
              val pathText = (errorNode \\ "path").text.trim
              if (pathText.isEmpty) None else Some(pathText)
            },
            originalValue = {
              val valueText = (errorNode \\ "originalValue").text.trim
              if (valueText.isEmpty) None else Some(valueText)
            }
          )
        }
      }

    AESDigitalNotification(
      correlationId = (xml \\ "correlationId").text.trim,
      eori = (xml \\ "eori").text.trim,
      mrn = (xml \\ "mrn").text.trim,
      dateCreated = (xml \\ "dateCreated").text.trim,
      status = (xml \\ "status").text.trim.toInt,
      notificationErrors = notificationErrors.map(_.toSeq)
    )
  }
