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

package uk.gov.hmrc.automatedexportsystem.models.IE507

import cats.data.NonEmptyList
import cats.implicits.catsSyntaxTuple9Semigroupal
import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.automatedexportsystem.xml.RootedXmlWriter.toXmlRoot
import uk.gov.hmrc.automatedexportsystem.xml.XmlWriter.toXml
import uk.gov.hmrc.automatedexportsystem.xml.{XmlPath, XmlReader, XmlRootTag, XmlWriter}

import scala.xml.NodeSeq

final case class Consignment(
  modeOfTransportAtTheBorder: Option[ModeOfTransportAtTheBorder],
  referenceNumberUCR:         ReferenceNumberUcr,
  parentUcrId:                Option[ParentUcrId],
  transportEquipment:         Option[NonEmptyList[TransportEquipment]],
  seal:                       Option[NonEmptyList[Seal]],
  goodsReference:             Option[NonEmptyList[GoodsReference]],
  locationOfGoods:            LocationOfGoods,
  activeBorderTransportMeans: Option[ActiveBorderTransportMeans],
  transportDocument:          Option[NonEmptyList[TransportDocument]]
)

object Consignment:
  import uk.gov.hmrc.automatedexportsystem.models.formats.NonEmptyListFormat.nonEmptyListFormat

  given mongoFormat: Format[Consignment] = Json.format[Consignment]

  given consignmentTag: XmlRootTag[Consignment] = XmlRootTag("Consignment")

  given consignmentXmlWriter: XmlWriter[Consignment] =
    (o, label) =>
      val children: NodeSeq =
        o.modeOfTransportAtTheBorder.toXml("modeOfTransportAtTheBorder")
          ++ o.referenceNumberUCR.toXml("referenceNumberUCR")
          ++ o.parentUcrId.toXml("parentUCRID")
          ++ o.transportEquipment.toXmlRoot
          ++ o.seal.toXmlRoot
          ++ o.goodsReference.toXmlRoot
          ++ o.locationOfGoods.toXmlRoot
          ++ o.activeBorderTransportMeans.toXmlRoot
          ++ o.transportDocument.toXmlRoot

      XmlWriter.elem(label, children)

  given consignmentXmlReader: XmlReader[Consignment] =
    XmlReader.nonEmptyReader { (xml, path) =>
      (
        (XmlPath \ "modeOfTransportAtTheBorder").read[Option[ModeOfTransportAtTheBorder]](xml, path),
        (XmlPath \ "referenceNumberUCR").read[ReferenceNumberUcr](xml, path),
        (XmlPath \ "parentUCRID").read[Option[ParentUcrId]](xml, path),
        XmlPath.readRoot[Option[NonEmptyList[TransportEquipment]]](xml, path),
        XmlPath.readRoot[Option[NonEmptyList[Seal]]](xml, path),
        XmlPath.readRoot[Option[NonEmptyList[GoodsReference]]](xml, path),
        XmlPath.readRoot[LocationOfGoods](xml, path),
        XmlPath.readRoot[Option[ActiveBorderTransportMeans]](xml, path),
        XmlPath.readRoot[Option[NonEmptyList[TransportDocument]]](xml, path)
      ).mapN(Consignment.apply)
    }
