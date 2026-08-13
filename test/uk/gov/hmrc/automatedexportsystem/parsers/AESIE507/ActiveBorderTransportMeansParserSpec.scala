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

class ActiveBorderTransportMeansParserSpec extends BaseSpec:

  "parseActiveBorderTransportMeans" - {

    "return Right(None) when ActiveBorderTransportMeans is absent" in {
      val xml =
        XML.loadString(
          """<GoodsShipment>
            |  <someOtherNode>value</someOtherNode>
            |</GoodsShipment>""".stripMargin
        )

      val result = ActiveBorderTransportMeansParser.parseActiveBorderTransportMeans(xml)

      result shouldBe Right(None)
    }

    "return populated model when all optional fields are present" in {
      val xml =
        XML.loadString(
          """<GoodsShipment>
            |  <ActiveBorderTransportMeans>
            |    <typeOfIdentification>10</typeOfIdentification>
            |    <identificationNumber>IMO1234567</identificationNumber>
            |    <nationality>GB</nationality>
            |  </ActiveBorderTransportMeans>
            |</GoodsShipment>""".stripMargin
        )

      val result = ActiveBorderTransportMeansParser.parseActiveBorderTransportMeans(xml)

      result.isRight shouldBe true
      val abtm = result.toOption.flatten.value
      abtm.typeOfIdentification.map(_.value) shouldBe Some("10")
      abtm.identificationNumber.map(_.value) shouldBe Some("IMO1234567")
      abtm.nationality.map(_.value)          shouldBe Some("GB")
    }

    "return model with None fields when child tags are missing" in {
      val xml =
        XML.loadString(
          """<GoodsShipment>
            |  <ActiveBorderTransportMeans/>
            |</GoodsShipment>""".stripMargin
        )

      val result = ActiveBorderTransportMeansParser.parseActiveBorderTransportMeans(xml)

      val abtm = result.toOption.flatten.value
      abtm.typeOfIdentification shouldBe None
      abtm.identificationNumber shouldBe None
      abtm.nationality          shouldBe None
    }

    "trim whitespace in text nodes" in {
      val xml =
        XML.loadString(
          """<GoodsShipment>
            |  <ActiveBorderTransportMeans>
            |    <identificationNumber>  IMO1234567  </identificationNumber>
            |  </ActiveBorderTransportMeans>
            |</GoodsShipment>""".stripMargin
        )

      val result = ActiveBorderTransportMeansParser.parseActiveBorderTransportMeans(xml)

      result.toOption.flatten.value.identificationNumber.map(_.value) shouldBe Some("IMO1234567")
    }
  }
