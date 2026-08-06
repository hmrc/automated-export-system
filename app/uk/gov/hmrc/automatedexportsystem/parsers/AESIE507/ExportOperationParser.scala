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

import uk.gov.hmrc.automatedexportsystem.models.aesIE507.{DiscrepanciesExist, ExportOperation, ExportOperationType, Mrn, SplitIndicator}
import uk.gov.hmrc.automatedexportsystem.parsers.AESIE507.Helpers.{parseBoolean, req, textOptChild}

import scala.xml.Node

object ExportOperationParser {
  private def parseExportOperationType(raw: String): Either[String, ExportOperationType] =
    raw.toIntOption
      .toRight(s"Invalid ${Tags.ExportOperationType}: [$raw]")
      .flatMap { status =>
        ExportOperationType.values
          .find(_.status == status)
          .toRight(s"Invalid ${Tags.ExportOperationType}: [$status]")
      }

  def parseExportOperation(n: Node): Either[String, ExportOperation] =
    for {
      exportOperationType <- req(textOptChild(n, Tags.ExportOperationType), Tags.ExportOperationType)
                               .flatMap(parseExportOperationType)

      mrn <- req(textOptChild(n, Tags.MRN), Tags.MRN).map(Mrn.apply)

      discrepancies <- req(textOptChild(n, Tags.DiscrepanciesExist), Tags.DiscrepanciesExist)
                         .flatMap(parseBoolean)
                         .map(DiscrepanciesExist.apply)

      split <- req(textOptChild(n, Tags.SplitIndicator), Tags.SplitIndicator)
                 .flatMap(parseBoolean)
                 .map(SplitIndicator.apply)
    } yield ExportOperation(
      exportOperationType = exportOperationType,
      mrn = mrn,
      discrepanciesExist = discrepancies,
      splitIndicator = split
    )
}
