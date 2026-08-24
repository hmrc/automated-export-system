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

import cats.implicits.catsSyntaxTuple4Semigroupal
import play.api.libs.json.*
import uk.gov.hmrc.automatedexportsystem.xml.XmlWriter.toXml
import uk.gov.hmrc.automatedexportsystem.xml.{XmlPath, XmlReader, XmlRootTag, XmlWriter}

import scala.xml.NodeSeq

case class ExportOperation(
  exportOperationType: ExportOperationType,
  mrn:                 Mrn,
  discrepanciesExist:  DiscrepanciesExist,
  splitIndicator:      SplitIndicator
)

object ExportOperation:
  given mongoFormat: Format[ExportOperation] = Json.format[ExportOperation]

  given exportOperationTag: XmlRootTag[ExportOperation] = XmlRootTag("ExportOperation")

  given exportOperationXmlWriter: XmlWriter[ExportOperation] =
    (o, label) =>
      val children: NodeSeq =
        o.exportOperationType.toXml("type")
          ++ o.mrn.toXml("MRN")
          ++ o.discrepanciesExist.toXml("discrepanciesExist")
          ++ o.splitIndicator.toXml("splitIndicator")

      XmlWriter.elem(label, children)

  given exportOperationXmlReader: XmlReader[ExportOperation] =
    XmlReader.nonEmptyReader { (xml, path) =>
      (
        (XmlPath \ "type").read[ExportOperationType](xml, path),
        (XmlPath \ "MRN").read[Mrn](xml, path),
        (XmlPath \ "discrepanciesExist").read[DiscrepanciesExist](xml, path),
        (XmlPath \ "splitIndicator").read[SplitIndicator](xml, path)
      ).mapN(ExportOperation.apply)
    }
