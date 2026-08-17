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

package uk.gov.hmrc.automatedexportsystem.models.aesIE507

import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.automatedexportsystem.xml.XmlWriter.toXml
import uk.gov.hmrc.automatedexportsystem.xml.{XmlRootTag, XmlWriter}

import scala.xml.NodeSeq

final case class Packaging(
  sequenceNumber:   Option[SequenceNumber],
  typeOfPackages:   Option[TypeOfPackages],
  numberOfPackages: Option[NumberOfPackages],
  shippingMarks:    Option[ShippingMarks]
)

object Packaging:
  given mongoFormat: Format[Packaging] = Json.format[Packaging]

  given packagingTag: XmlRootTag[Packaging] = XmlRootTag("Packaging")

  given packagingXmlWriter: XmlWriter[Packaging] =
    (o, label) =>
      val children: NodeSeq =
        o.sequenceNumber.toXml("sequenceNumber")
          ++ o.typeOfPackages.toXml("typeOfPackages")
          ++ o.numberOfPackages.toXml("numberOfPackages")
          ++ o.shippingMarks.toXml("shippingMarks")

      XmlWriter.elem(label, children)
