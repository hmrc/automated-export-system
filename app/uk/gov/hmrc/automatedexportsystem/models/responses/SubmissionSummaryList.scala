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

package uk.gov.hmrc.automatedexportsystem.models.responses

import play.api.libs.json.*
import uk.gov.hmrc.automatedexportsystem.models.IE507.*
import uk.gov.hmrc.automatedexportsystem.models.mongo.read.MongoAesIE507MessageSummary
import uk.gov.hmrc.automatedexportsystem.xml.RootedXmlWriter.toXmlRoot
import uk.gov.hmrc.automatedexportsystem.xml.XmlWriter.toXml
import uk.gov.hmrc.automatedexportsystem.xml.{XmlRootTag, XmlWriter}

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneOffset}
import scala.xml.NodeSeq

final case class SubmissionSummaryList(submissions: List[SubmissionSummary])

object SubmissionSummaryList:
  given submissionSummaryListTag: XmlRootTag[SubmissionSummaryList] = XmlRootTag("Submissions")

  given submissionSummaryListXmlWriter: XmlWriter[SubmissionSummaryList] =
    (o, label) => XmlWriter.elem(label, o.submissions.toXmlRoot)

final case class SubmissionSummary(
  submissionId:     SubmissionId,
  mrn:              Mrn,
  ducr:             Option[ReferenceNumberUcr],
  officeOfExitCode: ReferenceNumber,
  updatedAt:        LocalDateTime,
  status:           ExportOperationType
)

object SubmissionSummary:

  given format: OFormat[SubmissionSummary] = Json.format[SubmissionSummary]

  given submissionSummaryTag: XmlRootTag[SubmissionSummary] = XmlRootTag("Submission")

  given submissionSummaryXmlWriter: XmlWriter[SubmissionSummary] =
    (o, label) =>
      val children: NodeSeq =
        o.submissionId.toXml("submissionId")
          ++ o.mrn.toXml("mrn")
          ++ o.ducr.toXml("ducr")
          ++ o.officeOfExitCode.toXml("officeOfExitCode")
          ++ o.updatedAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).toXml("updatedAt")
          ++ o.status.toXml("status")

      XmlWriter.elem(label, children)

  def fromMongoAesIE507MessageSummary(message: MongoAesIE507MessageSummary): SubmissionSummary =
    SubmissionSummary(
      submissionId = message.submissionId,
      mrn = message.exportOperation.mrn,
      ducr = message.ducr,
      officeOfExitCode = message.customsOfficeOfExitActual.referenceNumber,
      updatedAt = LocalDateTime.ofInstant(message.updatedAt, ZoneOffset.UTC),
      status = message.exportOperation.exportOperationType
    )
