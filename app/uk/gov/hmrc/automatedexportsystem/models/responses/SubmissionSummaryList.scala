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
import uk.gov.hmrc.automatedexportsystem.models.aesIE507.*
import uk.gov.hmrc.automatedexportsystem.models.mongo.write.MongoAesIE507Message

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDateTime, ZoneOffset}
import scala.xml.{Elem, NodeSeq}

final case class SubmissionSummaryList(submissions: List[SubmissionSummary]):
  def toXml: Elem =
    val submissionSummaryListXml: List[Elem] = submissions.map(_.toXml)

    <Submissions>
      {submissionSummaryListXml}
    </Submissions>

final case class SubmissionSummary(
  submissionId:     SubmissionId,
  mrn:              Mrn,
  ducr:             Option[ReferenceNumberUcr],
  officeOfExitCode: ReferenceNumber,
  updatedAt:        LocalDateTime,
  status:           ExportOperationType
):
  def toXml: Elem =
    val maybeDucrXml: NodeSeq =
      ducr.map(ucr => <ducr>{ucr.value}</ducr>).getOrElse(NodeSeq.Empty)

    <Submission>
      <submissionId>{submissionId.value}</submissionId>
      <mrn>{mrn.value}</mrn>
      {maybeDucrXml}
      <officeOfExitCode>{officeOfExitCode.value}</officeOfExitCode>
      <updatedAt>{updatedAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}</updatedAt>
      <status>{status.status}</status>
    </Submission>

object SubmissionSummary:
  import LocalDateTimeFormat.given
  given OFormat[SubmissionSummary] = Json.format[SubmissionSummary]
  // implicit val format: OFormat[SubmissionSummary] = Json.format[SubmissionSummary]
  def fromMongoAesIE507Message(message: MongoAesIE507Message): SubmissionSummary =
    SubmissionSummary(
      submissionId = message.submissionId,
      mrn = message.exportOperation.mrn,
      ducr = message.goodsShipment.map(_.consignment.referenceNumberUCR),
      officeOfExitCode = message.customsOfficeOfExitActual.referenceNumber,
      updatedAt = LocalDateTime.ofInstant(message.updatedAt, ZoneOffset.UTC),
      status = message.exportOperation.exportOperationType
    )

object LocalDateTimeFormat:
  private val iso = DateTimeFormatter.ISO_LOCAL_DATE_TIME

  given Format[LocalDateTime] = new Format[LocalDateTime]:
    override def reads(json: JsValue): JsResult[LocalDateTime] =
      json match
        case JsString(value) =>
          JsSuccess(LocalDateTime.parse(value, iso))

        case JsObject(fields) if fields.contains("$date") =>
          fields("$date") match
            case JsString(isoInstant) =>
              JsSuccess(LocalDateTime.ofInstant(Instant.parse(isoInstant), ZoneOffset.UTC))

            case JsObject(longObj) if longObj.contains("$numberLong") =>
              longObj("$numberLong") match
                case JsString(epochMs) =>
                  JsSuccess(LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMs.toLong), ZoneOffset.UTC))
                case other =>
                  JsError(s"Unsupported $$numberLong payload for LocalDateTime: $other")
            case other =>
              JsError(s"Unsupported $$date payload for LocalDateTime: $other")

        case JsNumber(epochMs) =>
          JsSuccess(LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMs.toLong), ZoneOffset.UTC))

        case other =>
          JsError(s"Cannot parse LocalDateTime from: $other")

    override def writes(value: LocalDateTime): JsValue =
      JsString(value.format(iso))
