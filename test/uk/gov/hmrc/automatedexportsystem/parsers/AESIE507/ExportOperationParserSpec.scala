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
import uk.gov.hmrc.automatedexportsystem.models.IE507.ExportOperation
import uk.gov.hmrc.automatedexportsystem.models.IE507.ExportOperationType.Standard

import scala.xml.XML

class ExportOperationParserSpec extends BaseSpec {
  "parseExportOperation" - {

    "parse valid ExportOperation XML" in {
      val xml =
        XML.loadString(
          """
            |<ExportOperation>
            |  <type>1</type>
            |  <MRN>23GB12345678901234</MRN>
            |  <discrepanciesExist>0</discrepanciesExist>
            |  <splitIndicator>0</splitIndicator>
            |</ExportOperation>
            |""".stripMargin
        )

      val result = ExportOperationParser.parseExportOperation(xml)

      result.isRight shouldBe true
      val parsed: ExportOperation = result.value

      parsed.exportOperationType      shouldBe Standard
      parsed.mrn.value                shouldBe "23GB12345678901234"
      parsed.discrepanciesExist.value shouldBe false
      parsed.splitIndicator.value     shouldBe false
    }

    "return Awaiting when exportOperationType is missing" in {
      val xml =
        XML.loadString(
          """
            |<ExportOperation>
            |  <MRN>23GB12345678901234</MRN>
            |  <discrepanciesExist>0</discrepanciesExist>
            |  <splitIndicator>0</splitIndicator>
            |</ExportOperation>
            |""".stripMargin
        )

      val result = ExportOperationParser.parseExportOperation(xml)

      result.isLeft               shouldBe true
      result.left.value.toLowerCase should include("missing required field: type")
    }

    "return Left when MRN is missing" in {
      val xml =
        XML.loadString(
          """
            |<ExportOperation>
            |  <type>1</type>
            |  <discrepanciesExist>0</discrepanciesExist>
            |  <splitIndicator>0</splitIndicator>
            |</ExportOperation>
            |""".stripMargin
        )

      val result = ExportOperationParser.parseExportOperation(xml)

      result.isLeft               shouldBe true
      result.left.value.toLowerCase should include("missing required field: mrn")
    }
  }
}
