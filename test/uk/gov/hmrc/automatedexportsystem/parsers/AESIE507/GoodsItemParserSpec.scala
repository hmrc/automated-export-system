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

import org.scalatest.EitherValues
import org.scalatest.EitherValues.*
import scala.xml.XML

class GoodsItemParserSpec extends BaseSpec {
  "parseGoodsItem" - {
    "return Right(None) when no GoodsItem nodes exist" in {
      val xml =
        XML.loadString(
          """
            |<Submission>
            |  <Foo/>
            |</Submission>
            |""".stripMargin
        )

      GoodsItemsParser.parseGoodsItems(xml) shouldBe Right(None)
    }

    "return Right(Some(nonEmptyList)) when GoodsItem is valid" in {
      val xml =
        XML.loadString(
          """
            |<Submission>
            |  <GoodsItem>
            |    <Commodity>
            |      <GoodsMeasure>
            |        <grossMass>12.1</grossMass>
            |        <netMass>11.0</netMass>
            |      </GoodsMeasure>
            |    </Commodity>
            |    <Packaging>
            |      <sequenceNumber>1</sequenceNumber>
            |      <typeOfPackages>BX</typeOfPackages>
            |    </Packaging>
            |    <declarationGoodsItemNumber>1</declarationGoodsItemNumber>
            |    <referenceNumberUCR>UCR-123</referenceNumberUCR>
            |  </GoodsItem>
            |</Submission>
            |""".stripMargin
        )

      val result = GoodsItemsParser.parseGoodsItems(xml)

      result.isRight shouldBe true
      val items = result.value.value
      items.length shouldBe 1

      val item = items.head
      item.commodity.grossMass.value               shouldBe BigDecimal("12.1")
      item.commodity.netMass.value                 shouldBe BigDecimal("11.0")
      item.declarationGoodsItemNumber.map(_.value) shouldBe Some(1)
      item.referenceNumberUcr.map(_.value)         shouldBe Some("UCR-123")
    }

    "return Left when GoodsMeasure is missing" in {
      val xml =
        XML.loadString(
          """
            |<Submission>
            |  <GoodsItem>
            |    <Commodity/>
            |    <Packaging>
            |      <sequenceNumber>1</sequenceNumber>
            |      <typeOfPackages>BX</typeOfPackages>
            |    </Packaging>
            |  </GoodsItem>
            |</Submission>
            |""".stripMargin
        )

      val result = GoodsItemsParser.parseGoodsItems(xml)

      result.isLeft                 shouldBe true
      result.left.value.toLowerCase shouldBe "missing required field: goodsmeasure"
    }

    "return Left when grossMass is not numeric" in {
      val xml =
        XML.loadString(
          """
            |<Submission>
            |  <GoodsItem>
            |    <Commodity>
            |      <GoodsMeasure>
            |        <grossMass>abc</grossMass>
            |        <netMass>11.0</netMass>
            |      </GoodsMeasure>
            |    </Commodity>
            |    <Packaging>
            |      <sequenceNumber>1</sequenceNumber>
            |      <typeOfPackages>BX</typeOfPackages>
            |    </Packaging>
            |  </GoodsItem>
            |</Submission>
            |""".stripMargin
        )

      val result = GoodsItemsParser.parseGoodsItems(xml)

      result.isLeft                 shouldBe true
      result.left.value.toLowerCase shouldBe "invalid decimal: abc"
    }

    "return Left when netMass is not numeric" in {
      val xml =
        XML.loadString(
          """
            |<Submission>
            |  <GoodsItem>
            |    <Commodity>
            |      <GoodsMeasure>
            |        <grossMass>12.1</grossMass>
            |        <netMass>xyz</netMass>
            |      </GoodsMeasure>
            |    </Commodity>
            |    <Packaging>
            |      <sequenceNumber>1</sequenceNumber>
            |      <typeOfPackages>BX</typeOfPackages>
            |    </Packaging>
            |  </GoodsItem>
            |</Submission>
            |""".stripMargin
        )

      val result = GoodsItemsParser.parseGoodsItems(xml)

      result.isLeft                 shouldBe true
      result.left.value.toLowerCase shouldBe "invalid decimal: xyz"
    }

    "return Left when declarationGoodsItemNumber is invalid" in {
      val xml =
        XML.loadString(
          """
            |<Submission>
            |  <GoodsItem>
            |    <Commodity>
            |      <GoodsMeasure>
            |        <grossMass>12.1</grossMass>
            |        <netMass>11.0</netMass>
            |      </GoodsMeasure>
            |    </Commodity>
            |    <Packaging>
            |      <sequenceNumber>1</sequenceNumber>
            |      <typeOfPackages>BX</typeOfPackages>
            |    </Packaging>
            |    <declarationGoodsItemNumber>abc</declarationGoodsItemNumber>
            |  </GoodsItem>
            |</Submission>
            |""".stripMargin
        )

      val result = GoodsItemsParser.parseGoodsItems(xml)

      result.isLeft                 shouldBe true
      result.left.value.toLowerCase shouldBe "invalid integer for declarationgoodsitemnumber: abc"
    }
  }
}
