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

import uk.gov.hmrc.automatedexportsystem.models.aesIE507.*
import uk.gov.hmrc.automatedexportsystem.models.mongo.write.MongoAesIE507Message

import java.time.{LocalDateTime, ZoneOffset}

final case class SubmissionSummaryList(submissions: List[SubmissionSummary])

final case class SubmissionSummary(
  submissionId:     SubmissionId,
  mrn:              Mrn,
  ducr:             Option[ReferenceNumberUcr],
  officeOfExitCode: ReferenceNumber,
  updatedAt:        LocalDateTime,
  status:           ExportOperationType
)

object SubmissionSummary:
  def fromMongoAesIE507Message(message: MongoAesIE507Message): SubmissionSummary =
    SubmissionSummary(
      submissionId = message._id,
      mrn = message.exportOperation.mrn,
      ducr = message.goodsShipment.map(_.consignment.referenceNumberUCR),
      officeOfExitCode = message.customsOfficeOfExitActual.referenceNumber,
      updatedAt = LocalDateTime.ofInstant(message.updatedAt, ZoneOffset.UTC),
      status = message.exportOperation.exportOperationType
    )
