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

import uk.gov.hmrc.automatedexportsystem.models.IE507.*
import uk.gov.hmrc.automatedexportsystem.models.request.SubmissionRequest
import uk.gov.hmrc.automatedexportsystem.parsers.AESIE507.Helpers.*
import uk.gov.hmrc.automatedexportsystem.parsers.AESIE507.{ExportOperationParser, GoodsShipmentParser, Tags}

import java.util.UUID
import scala.xml.{Node, NodeSeq}

object SubmissionRequestParser {

  def fromXml(xml: NodeSeq): Either[String, SubmissionRequest] =
    for {
      submissionId <- parseOptionalSubmissionId(textOptDeep(xml, Tags.SubmissionId))
      exportOpNode <- req((xml \\ Tags.ExportOperation).headOption, Tags.ExportOperation)
      exportOp     <- ExportOperationParser.parseExportOperation(exportOpNode)

      officeNode <- req((xml \\ Tags.CustomsOfficeOfExitActual).headOption, Tags.CustomsOfficeOfExitActual)
      office     <- parseCustomsOfficeOfExitActual(officeNode)

      shipment <- GoodsShipmentParser.parseGoodsShipmentOpt(xml.head)
    } yield SubmissionRequest(
      submissionId = submissionId,
      exportOperation = exportOp,
      customsOfficeOfExitActual = office,
      goodsShipment = shipment
    )

  private def parseOptionalSubmissionId(raw: Option[String]): Either[String, Option[SubmissionId]] =
    parseOptionalUuid(raw).map(_.map(SubmissionId.apply))

  private def parseCustomsOfficeOfExitActual(n: Node): Either[String, CustomsOfficeOfExitActual] =
    req(textOptChild(n, Tags.ReferenceNumber), Tags.ReferenceNumber)
      .map(v => CustomsOfficeOfExitActual(ReferenceNumber(v)))

}
