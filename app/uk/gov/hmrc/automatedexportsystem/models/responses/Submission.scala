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
import uk.gov.hmrc.automatedexportsystem.xml.RootedXmlWriter.toXmlRoot
import uk.gov.hmrc.automatedexportsystem.xml.XmlWriter.toXml
import uk.gov.hmrc.automatedexportsystem.xml.{XmlRootTag, XmlWriter}

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneOffset}
import scala.xml.NodeSeq

final case class Submission(
  submissionId:              SubmissionId,
  exportOperation:           ExportOperation,
  customsOfficeOfExitActual: CustomsOfficeOfExitActual,
  goodsShipment:             Option[GoodsShipment],
  updatedAt:                 LocalDateTime
)

object Submission:
  given submissionTag: XmlRootTag[Submission] = XmlRootTag("Submission")

  given submissionXmlWriter: XmlWriter[Submission] =
    (o, label) =>
      val children: NodeSeq =
        o.submissionId.toXml("submissionId")
          ++ o.exportOperation.toXmlRoot
          ++ o.customsOfficeOfExitActual.toXmlRoot
          ++ o.goodsShipment.toXmlRoot
          ++ o.updatedAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).toXml("updatedAt")

      XmlWriter.elem(label, children)

  def fromMongoAesIE507Message(message: MongoAesIE507Message): Submission =
    Submission(
      submissionId = message.submissionId,
      exportOperation = message.exportOperation,
      customsOfficeOfExitActual = message.customsOfficeOfExitActual,
      goodsShipment = message.goodsShipment,
      updatedAt = LocalDateTime.ofInstant(message.updatedAt, ZoneOffset.UTC)
    )
