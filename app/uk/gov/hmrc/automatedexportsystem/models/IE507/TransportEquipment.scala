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
import cats.implicits.catsSyntaxTuple5Semigroupal
import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.automatedexportsystem.xml.RootedXmlWriter.toXmlRoot
import uk.gov.hmrc.automatedexportsystem.xml.XmlWriter.toXml
import uk.gov.hmrc.automatedexportsystem.xml.{XmlPath, XmlReader, XmlRootTag, XmlWriter}

import scala.xml.NodeSeq

final case class TransportEquipment(
  sequenceNumber:                Option[SequenceNumber],
  containerIdentificationNumber: Option[ContainerIdentificationNumber],
  numberOfSeals:                 Option[NumberOfSeals],
  seal:                          Option[NonEmptyList[Seal]],
  goodsReference:                Option[NonEmptyList[GoodsReference]]
)

object TransportEquipment:
  import uk.gov.hmrc.automatedexportsystem.models.formats.NonEmptyListFormat.nonEmptyListFormat

  given mongoFormat: Format[TransportEquipment] = Json.format[TransportEquipment]

  given transportEquipmentTag: XmlRootTag[TransportEquipment] = XmlRootTag("TransportEquipment")

  given transportEquipmentXmlWriter: XmlWriter[TransportEquipment] =
    (o, label) =>
      val children: NodeSeq =
        o.sequenceNumber.toXml("sequenceNumber")
          ++ o.containerIdentificationNumber.toXml("containerIdentificationNumber")
          ++ o.numberOfSeals.toXml("numberOfSeals")
          ++ o.seal.toXmlRoot
          ++ o.goodsReference.toXmlRoot

      XmlWriter.elem(label, children)

  given transportEquipmentXmlReader: XmlReader[TransportEquipment] =
    XmlReader.nonEmptyReader { (xml, path) =>
      (
        (XmlPath \ "sequenceNumber").read[Option[SequenceNumber]](xml, path),
        (XmlPath \ "containerIdentificationNumber").read[Option[ContainerIdentificationNumber]](xml, path),
        (XmlPath \ "numberOfSeals").read[Option[NumberOfSeals]](xml, path),
        XmlPath.readRoot[Option[NonEmptyList[Seal]]](xml, path),
        XmlPath.readRoot[Option[NonEmptyList[GoodsReference]]](xml, path)
      ).mapN(TransportEquipment.apply)
    }
