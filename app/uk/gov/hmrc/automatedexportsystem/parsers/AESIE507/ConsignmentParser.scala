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

package uk.gov.hmrc.automatedexportsystem.parsers.AESIE507

import uk.gov.hmrc.automatedexportsystem.models.aesIE507.{Consignment, ModeOfTransportAtBorder, ParentUcrId, ReferenceNumberUcr}
import uk.gov.hmrc.automatedexportsystem.parsers.AESIE507.Helpers.*
import uk.gov.hmrc.automatedexportsystem.parsers.AESIE507.{ActiveBorderTransportMeansParser, LocationOfGoodsParser, TransportEquipmentParser}

import scala.xml.Node

object ConsignmentParser {
  def parseConsignment(n: Node): Either[String, Consignment] =
    for {
      referenceNumberUcr <- req(textOptChild(n, Tags.ReferenceNumberUCR), Tags.ReferenceNumberUCR).map(ReferenceNumberUcr.apply)

      modeOfTransportAtBorder <- parseOptionalInt(textOptChild(n, Tags.ModeOfTransportAtBorder), Tags.ModeOfTransportAtBorder)
                                   .map(_.map(ModeOfTransportAtBorder.apply))

      locationNode    <- req((n \ Tags.LocationOfGoods).headOption, Tags.LocationOfGoods)
      locationOfGoods <- LocationOfGoodsParser.parseLocationOfGoods(locationNode)

      transportEquipment <- TransportEquipmentParser.parseTransportEquipment(n)
      seals              <- SealsParser.parseSeals(n)
      goodsReferences    <- GoodsReferenceParser.parseGoodsReferences(n)
      borderMeans        <- ActiveBorderTransportMeansParser.parseActiveBorderTransportMeans(n)
      transportDocs      <- TransportDocumentParser.parseTransportDocuments(n)
    } yield Consignment(
      modeOfTransportAtBorder = modeOfTransportAtBorder,
      referenceNumberUCR = referenceNumberUcr,
      parentUcrId = textOptChild(n, Tags.ParentUCRID).map(ParentUcrId.apply),
      transportEquipment = transportEquipment,
      seal = seals,
      goodsReference = goodsReferences,
      locationOfGoods = locationOfGoods,
      activeBorderTransportMeans = borderMeans,
      transportDocument = transportDocs
    )
}
