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

import org.scalatest.EitherValues
import org.scalatest.EitherValues.*
import uk.gov.hmrc.automatedexportsystem.helpers.BaseSpec
import uk.gov.hmrc.automatedexportsystem.models.IE507.TypeOfIdentification

import scala.xml.XML

class ConsignmentParserSpec extends BaseSpec {

  "parseConsignment" - {
    "parse a valid consignment with all fields present" in {
      val xml =
        XML.loadString(
          """
              |<Consignment>
              |  <modeOfTransportAtTheBorder>1</modeOfTransportAtTheBorder>
              |  <referenceNumberUCR>6GB536187624189-S458</referenceNumberUCR>
              |  <parentUCRID>GB/ABC-12345</parentUCRID>
              |
              |  <TransportEquipment>
              |    <sequenceNumber>1</sequenceNumber>
              |    <containerIdentificationNumber>CONT1234567890123</containerIdentificationNumber>
              |    <numberOfSeals>2</numberOfSeals>
              |  </TransportEquipment>
              |
              |  <Seal>
              |    <sequenceNumber>1</sequenceNumber>
              |    <identifier>SEAL123</identifier>
              |  </Seal>
              |
              |  <GoodsReference>
              |    <sequenceNumber>1</sequenceNumber>
              |    <declarationGoodsItemNumber>1</declarationGoodsItemNumber>
              |  </GoodsReference>
              |
              |  <LocationOfGoods>
              |    <typeOfLocation>A</typeOfLocation>
              |    <qualifierOfIdentification>B</qualifierOfIdentification>
              |    <authorisationNumber>AUTH12345</authorisationNumber>
              |    <additionalIdentifier>AD01</additionalIdentifier>
              |    <UNLocode>UNLOCODE123</UNLocode>
              |  </LocationOfGoods>
              |
              |  <ActiveBorderTransportMeans>
              |    <typeOfIdentification>20</typeOfIdentification>
              |    <identificationNumber>IDNUMBER123</identificationNumber>
              |    <nationality>GB</nationality>
              |  </ActiveBorderTransportMeans>
              |
              |  <TransportDocument>
              |    <sequenceNumber>1</sequenceNumber>
              |    <type>2</type>
              |    <referenceNumber>REF123</referenceNumber>
              |  </TransportDocument>
              |</Consignment>
              |""".stripMargin
        )

      val result = ConsignmentParser.parseConsignment(xml)

      result.isRight shouldBe true
      val parsed = result.value

      parsed.modeOfTransportAtTheBorder.map(_.value) shouldBe Some(1)
      parsed.referenceNumberUCR.value                shouldBe "6GB536187624189-S458"
      parsed.parentUcrId.map(_.value)                shouldBe Some("GB/ABC-12345")

      parsed.transportEquipment.map(_.length) shouldBe Some(1)
      parsed.seal.map(_.length)               shouldBe Some(1)
      parsed.goodsReference.map(_.length)     shouldBe Some(1)
      parsed.transportDocument.map(_.length)  shouldBe Some(1)

      parsed.activeBorderTransportMeans.map(_.typeOfIdentification.value) shouldBe Some(TypeOfIdentification("20"))
      parsed.locationOfGoods.typeOfLocation.value                         shouldBe "A"
    }

    "return populated model when only required fields are present" in {
      val xml =
        XML.loadString(
          """
            |<Consignment>
            |  <referenceNumberUCR>6GB536187624189-S458</referenceNumberUCR>
            |  <LocationOfGoods>
            |    <typeOfLocation>A</typeOfLocation>
            |    <qualifierOfIdentification>B</qualifierOfIdentification>
            |  </LocationOfGoods>
            |</Consignment>
            |""".stripMargin
        )

      val result = ConsignmentParser.parseConsignment(xml)

      result.isRight shouldBe true
      val parsed = result.value

      parsed.referenceNumberUCR.value                        shouldBe "6GB536187624189-S458"
      parsed.locationOfGoods.typeOfLocation.value            shouldBe "A"
      parsed.locationOfGoods.qualifierOfIdentification.value shouldBe "B"
    }

    "return no model, but error msg when required fields are missing" in {
      val xml =
        XML.loadString(
          """
            |<Consignment>
            |  <LocationOfGoods>
            |    <qualifierOfIdentification>B</qualifierOfIdentification>
            |  </LocationOfGoods>
            |</Consignment>
            |""".stripMargin
        )

      val result = ConsignmentParser.parseConsignment(xml)

      result.isLeft                 shouldBe true
      result.left.value.toLowerCase() should include("missing required field: referencenumberucr")
    }

  }
}
