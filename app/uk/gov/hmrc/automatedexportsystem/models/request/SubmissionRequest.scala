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

package uk.gov.hmrc.automatedexportsystem.models.request

import uk.gov.hmrc.automatedexportsystem.models.IE507.{CustomsOfficeOfExitActual, EoriNumber, ExportOperation, GoodsShipment, SubmissionId}
import uk.gov.hmrc.automatedexportsystem.models.mongo.write.MongoAesIE507Message
import uk.gov.hmrc.automatedexportsystem.models.IE507.ExportOperationType
import java.time.Instant
import java.util.UUID
import play.api.Logging

case class SubmissionRequest(
  submissionId:              Option[SubmissionId],
  exportOperation:           ExportOperation,
  customsOfficeOfExitActual: CustomsOfficeOfExitActual,
  goodsShipment:             Option[GoodsShipment]
) extends Logging {
  def toMongoMessage(
    operationType: ExportOperationType,
    eoriNumber:    EoriNumber
  ): MongoAesIE507Message = {
    logger.info(
      s"Converting SubmissionRequest to MongoAesIE507Message with submissionId: ${submissionId.getOrElse("None")}, eoriNumber: $eoriNumber, operationType: $operationType"
    )
    val now = Instant.now()
    MongoAesIE507Message(
      submissionId = submissionId.getOrElse(SubmissionId(UUID.randomUUID())),
      eoriNumber = eoriNumber,
      createdAt = now,
      updatedAt = now,
      exportOperation = exportOperation.copy(exportOperationType = operationType),
      customsOfficeOfExitActual = customsOfficeOfExitActual,
      goodsShipment = goodsShipment
    )
  }
}
sealed trait SubmissionResult

object SubmissionResult {
  case object Created extends SubmissionResult
  case object Updated extends SubmissionResult
  case object Awaiting extends SubmissionResult
}
