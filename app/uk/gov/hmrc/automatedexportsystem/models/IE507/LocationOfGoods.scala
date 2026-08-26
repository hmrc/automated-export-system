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

import cats.implicits.catsSyntaxTuple5Semigroupal
import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.automatedexportsystem.xml.XmlWriter.toXml
import uk.gov.hmrc.automatedexportsystem.xml.{XmlPath, XmlReader, XmlRootTag, XmlWriter}

import scala.xml.NodeSeq

final case class LocationOfGoods(
  typeOfLocation:            TypeOfLocation,
  qualifierOfIdentification: QualifierOfIdentification,
  authorisationNumber:       Option[AuthorisationNumber],
  additionalIdentifier:      Option[AdditionalIdentifier],
  unLocode:                  Option[UnLocode]
)

object LocationOfGoods:
  given mongoFormat: Format[LocationOfGoods] = Json.format[LocationOfGoods]

  given locationOfGoodsTag: XmlRootTag[LocationOfGoods] = XmlRootTag("LocationOfGoods")

  given locationOfGoodsXmlWriter: XmlWriter[LocationOfGoods] =
    (o, label) =>
      val children: NodeSeq =
        o.typeOfLocation.toXml("typeOfLocation")
          ++ o.qualifierOfIdentification.toXml("qualifierOfIdentification")
          ++ o.authorisationNumber.toXml("authorisationNumber")
          ++ o.additionalIdentifier.toXml("additionalIdentifier")
          ++ o.unLocode.toXml("UNLocode")

      XmlWriter.elem(label, children)

  given locationOfGoodsXmlReader: XmlReader[LocationOfGoods] =
    XmlReader.nonEmptyReader { (xml, path) =>
      (
        (XmlPath \ "typeOfLocation").read[TypeOfLocation](xml, path),
        (XmlPath \ "qualifierOfIdentification").read[QualifierOfIdentification](xml, path),
        (XmlPath \ "authorisationNumber").read[Option[AuthorisationNumber]](xml, path),
        (XmlPath \ "additionalIdentifier").read[Option[AdditionalIdentifier]](xml, path),
        (XmlPath \ "UNLocode").read[Option[UnLocode]](xml, path)
      ).mapN(LocationOfGoods.apply)
    }
