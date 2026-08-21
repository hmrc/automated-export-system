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

package uk.gov.hmrc.automatedexportsystem.models.IE507.eis

import uk.gov.hmrc.automatedexportsystem.models.IE507.{CustomsOfficeOfExitActual, ExportOperation, GoodsShipment}
import uk.gov.hmrc.automatedexportsystem.xml.RootedXmlWriter.toXmlRoot
import uk.gov.hmrc.automatedexportsystem.xml.{XmlRootTag, XmlWriter}

import scala.xml.NodeSeq

final case class EisIE507Body(
  exportOperation:           ExportOperation,
  customsOfficeOfExitActual: CustomsOfficeOfExitActual,
  goodsShipment:             Option[GoodsShipment]
)

object EisIE507Body:
  given eisIE507BodyTag: XmlRootTag[EisIE507Body] = XmlRootTag("Body")

  given eisIE507BodyXmlWriter: XmlWriter[EisIE507Body] =
    (o, label) =>
      val children: NodeSeq =
        o.exportOperation.toXmlRoot
          ++ o.customsOfficeOfExitActual.toXmlRoot
          ++ o.goodsShipment.toXmlRoot

      XmlWriter.elem(label, children)
