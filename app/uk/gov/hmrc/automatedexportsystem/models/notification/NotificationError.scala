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

import cats.implicits.catsSyntaxTuple4Semigroupal
import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.automatedexportsystem.xml.XmlWriter.toXml
import uk.gov.hmrc.automatedexportsystem.xml.{XmlPath, XmlReader, XmlRootTag, XmlWriter}

import scala.xml.NodeSeq

final case class NotificationError(
  code:          String,
  description:   String,
  path:          Option[String],
  originalValue: Option[String]
)

object NotificationError:
  given mongoFormat: Format[NotificationError] = Json.format[NotificationError]

  given notificationErrorTag: XmlRootTag[NotificationError] = XmlRootTag("error")

  given notificationErrorXmlReader: XmlReader[NotificationError] =
    XmlReader.nonEmptyReader { (xml, path) =>
      (
        (XmlPath \ "code").read[String](xml, path),
        (XmlPath \ "description").read[String](xml, path),
        (XmlPath \ "path").read[Option[String]](xml, path),
        (XmlPath \ "originalValue").read[Option[String]](xml, path)
      ).mapN(NotificationError.apply)
    }

  given notificationErrorXmlWriter: XmlWriter[NotificationError] =
    (o, label) =>
      val children: NodeSeq =
        o.code.toXml("code")
          ++ o.description.toXml("description")
          ++ o.path.toXml("path")
          ++ o.originalValue.toXml("originalValue")

      XmlWriter.elem(label, children)
