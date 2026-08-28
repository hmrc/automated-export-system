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

import uk.gov.hmrc.automatedexportsystem.helpers.BaseSpec

import scala.xml.XML

class GoodsShipmentParserSpec extends BaseSpec {

  "parseGoodsShipment" - {

    "return Left when GoodsShipment node is missing" in {
      val xml = XML.loadString("<Submission><GoodsShipment></GoodsShipment></Submission>")

      val result = GoodsShipmentParser.parseGoodsShipmentOpt(xml)

      result.isLeft                 shouldBe true
      result.left.value.toLowerCase shouldBe "missing required field: consignment"
    }

    "return Left when Consignment empty" in {
      val xml: scala.xml.Elem =
        XML.loadString(
          """
            |<Submission>
            |  <GoodsShipment>
            |    <Consignment></Consignment>
            |    <GoodsItem></GoodsItem>
            |  </GoodsShipment>
            |</Submission>
            |""".stripMargin
        )
      val result = GoodsShipmentParser.parseGoodsShipmentOpt(xml)

      result.isLeft                 shouldBe true
      result.left.value.toLowerCase shouldBe "missing required field: referencenumberucr"
    }

    "return Right with valid consignment" in {
      val xml: scala.xml.Elem =
        XML.loadString(
          """
            | <Submission>
            |  <GoodsShipment>
            |   <Consignment>
            |    <referenceNumberUCR>6GB536187624189-S458</referenceNumberUCR>
            |    <LocationOfGoods>
            |       <typeOfLocation>1</typeOfLocation>
            |       <qualifierOfIdentification>1</qualifierOfIdentification>
            |    </LocationOfGoods>
            |   </Consignment>
            |  </GoodsShipment>
            |  </Submission>
            |""".stripMargin
        )

      val result = GoodsShipmentParser.parseGoodsShipmentOpt(xml)
      result.isRight shouldBe true

      val shipmentOpt = result.value
      shipmentOpt.isDefined shouldBe true

      val shipment = shipmentOpt.value
      shipment.consignment.referenceNumberUCR.value                        shouldBe "6GB536187624189-S458"
      shipment.consignment.locationOfGoods.typeOfLocation.value            shouldBe "1"
      shipment.consignment.locationOfGoods.qualifierOfIdentification.value shouldBe "1"
      shipment.goodsItem                                                   shouldBe None
    }

    "return Right when GoodsItem is valid" in {
      val xml: scala.xml.Elem =
        XML.loadString(
          """
            | <Submission>
            |  <GoodsShipment>
            |   <Consignment>
            |    <referenceNumberUCR>6GB536187624189-S458</referenceNumberUCR>
            |    <LocationOfGoods>
            |       <typeOfLocation>1</typeOfLocation>
            |       <qualifierOfIdentification>1</qualifierOfIdentification>
            |    </LocationOfGoods>
            |   </Consignment>
            |   <GoodsItem>
            |     <Commodity>
            |     <GoodsMeasure>
            |       <grossMass>12.1</grossMass>
            |       <netMass>11.0</netMass>
            |       </GoodsMeasure>
            |     </Commodity>
            |   </GoodsItem>
            |  </GoodsShipment>
            |  </Submission>
            |""".stripMargin
        )

      val result = GoodsShipmentParser.parseGoodsShipmentOpt(xml)
      result.isRight shouldBe true

      val shipmentOpt = result.value
      shipmentOpt.isDefined shouldBe true

      val shipment = shipmentOpt.value

      shipment.goodsItem.isDefined    shouldBe true
      shipment.goodsItem.value.length shouldBe 1

      val item = shipment.goodsItem.value.head
      item.commodity.goodsMeasure.grossMass.value shouldBe BigDecimal("12.1")
      item.commodity.goodsMeasure.netMass.value   shouldBe BigDecimal("11.0")
      item.declarationGoodsItemNumber             shouldBe None
      item.referenceNumberUcr                     shouldBe None
    }
  }
}
