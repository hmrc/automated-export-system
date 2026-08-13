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

package uk.gov.hmrc.automatedexportsystem.parsers

import org.scalatest.EitherValues.*
import uk.gov.hmrc.automatedexportsystem.helpers.BaseSpec
import uk.gov.hmrc.automatedexportsystem.models.aesIE507.ExportOperationType

import scala.xml.XML

class SubmissionRequestParserSpec extends BaseSpec {

  "SubmissionRequestParser.fromXml" - {

    "return Right with all sections parsed when XML is valid" in {
      val xml: scala.xml.Elem =
        XML.loadString(
          """
              |<Submission>
              |  <submissionId>123e4567-e89b-12d3-a456-426614174000</submissionId>
              |
              |  <ExportOperation>
              |    <type>1</type>
              |    <MRN>23GB12345678901234</MRN>
              |    <discrepanciesExist>0</discrepanciesExist>
              |    <splitIndicator>0</splitIndicator>
              |  </ExportOperation>
              |
              |  <CustomsOfficeOfExitActual>
              |    <referenceNumber>GB000001</referenceNumber>
              |  </CustomsOfficeOfExitActual>
              |
              |  <GoodsShipment>
              |    <Consignment>
              |      <referenceNumberUCR>6GB536187624189-S458</referenceNumberUCR>
              |      <LocationOfGoods>
              |        <typeOfLocation>1</typeOfLocation>
              |        <qualifierOfIdentification>1</qualifierOfIdentification>
              |      </LocationOfGoods>
              |    </Consignment>
              |    <GoodsItem>
              |      <Commodity>
              |        <GoodsMeasure>
              |          <grossMass>12.1</grossMass>
              |          <netMass>11.0</netMass>
              |        </GoodsMeasure>
              |      </Commodity>
              |      <Packaging>
              |        <sequenceNumber>1</sequenceNumber>
              |        <typeOfPackages>BX</typeOfPackages>
              |      </Packaging>
              |    </GoodsItem>
              |  </GoodsShipment>
              |</Submission>
              |""".stripMargin
        )

      val result = SubmissionRequestParser.fromXml(xml)

      result.isRight shouldBe true
      val parsed = result.value

      parsed.submissionId.map(_.value.toString)       shouldBe Some("123e4567-e89b-12d3-a456-426614174000")
      parsed.exportOperation.exportOperationType      shouldBe ExportOperationType.Standard
      parsed.exportOperation.mrn.value                shouldBe "23GB12345678901234"
      parsed.exportOperation.discrepanciesExist.value shouldBe false
      parsed.exportOperation.splitIndicator.value     shouldBe false

      parsed.customsOfficeOfExitActual.referenceNumber.value shouldBe "GB000001"

      parsed.goodsShipment.isDefined shouldBe true
      val shipment = parsed.goodsShipment.value
      shipment.consignment.referenceNumberUCR.value shouldBe "6GB536187624189-S458"
      shipment.goodsItem.isDefined                  shouldBe true
      shipment.goodsItem.value.length               shouldBe 1
    }

    "return Right with submissionId None when submissionId is absent" in {
      val xml: scala.xml.Elem =
        XML.loadString(
          """
              |<Submission>
              |  <ExportOperation>
              |    <type>1</type>
              |    <MRN>23GB12345678901234</MRN>
              |    <discrepanciesExist>0</discrepanciesExist>
              |    <splitIndicator>0</splitIndicator>
              |  </ExportOperation>
              |  <CustomsOfficeOfExitActual>
              |    <referenceNumber>GB000001</referenceNumber>
              |  </CustomsOfficeOfExitActual>
              |</Submission>
              |""".stripMargin
        )

      val result = SubmissionRequestParser.fromXml(xml)

      result.isRight            shouldBe true
      result.value.submissionId shouldBe None
    }

    "return Left when ExportOperation is missing" in {
      val xml: scala.xml.Elem =
        XML.loadString(
          """
              |<Submission>
              |  <CustomsOfficeOfExitActual>
              |    <referenceNumber>GB000001</referenceNumber>
              |  </CustomsOfficeOfExitActual>
              |</Submission>
              |""".stripMargin
        )

      val result = SubmissionRequestParser.fromXml(xml)

      result.isLeft                 shouldBe true
      result.left.value.toLowerCase shouldBe "missing required field: exportoperation"
    }

    "return Left when CustomsOfficeOfExitActual is missing" in {
      val xml: scala.xml.Elem =
        XML.loadString(
          """
              |<Submission>
              |  <ExportOperation>
              |    <exportOperationType>1</exportOperationType>
              |    <MRN>23GB12345678901234</MRN>
              |    <discrepanciesExist>0</discrepanciesExist>
              |    <splitIndicator>0</splitIndicator>
              |  </ExportOperation>
              |</Submission>
              |""".stripMargin
        )

      val result = SubmissionRequestParser.fromXml(xml)

      result.isLeft                 shouldBe true
      result.left.value.toLowerCase shouldBe "missing required field: type"
    }

    "return Left when CustomsOfficeOfExitActual.referenceNumber is missing" in {
      val xml: scala.xml.Elem =
        XML.loadString(
          """
              |<Submission>
              |  <ExportOperation>
              |    <type>1</type>
              |    <MRN>23GB12345678901234</MRN>
              |    <discrepanciesExist>0</discrepanciesExist>
              |    <splitIndicator>0</splitIndicator>
              |  </ExportOperation>
              |  <CustomsOfficeOfExitActual>
              |  </CustomsOfficeOfExitActual>
              |</Submission>
              |""".stripMargin
        )

      val result = SubmissionRequestParser.fromXml(xml)

      result.isLeft                 shouldBe true
      result.left.value.toLowerCase shouldBe "missing required field: referencenumber"
    }

    "return Right with happy path when GoodsShipment is absent" in {
      val xml: scala.xml.Elem =
        XML.loadString(
          """
              |<Submission>
              |  <ExportOperation>
              |    <type>1</type>
              |    <MRN>23GB12345678901234</MRN>
              |    <discrepanciesExist>0</discrepanciesExist>
              |    <splitIndicator>0</splitIndicator>
              |  </ExportOperation>
              |  <CustomsOfficeOfExitActual>
              |    <referenceNumber>GB000001</referenceNumber>
              |  </CustomsOfficeOfExitActual>
              |</Submission>
              |""".stripMargin
        )

      val result = SubmissionRequestParser.fromXml(xml)

      result.isRight             shouldBe true
      result.value.goodsShipment shouldBe None
    }
  }
}
