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

package uk.gov.hmrc.automatedexportsystem.models.IE507.eis

import uk.gov.hmrc.automatedexportsystem.models.IE507.EoriNumber
import uk.gov.hmrc.automatedexportsystem.xml.XmlWriter.toXml
import uk.gov.hmrc.automatedexportsystem.xml.{XmlRootTag, XmlWriter}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.xml.NodeSeq

final case class EisIE507Header(
  messageSender:          MessageSender,
  messageRecipient:       MessageRecipient,
  preparationDateAndTime: LocalDateTime,
  messageIdentification:  MessageIdentification,
  messageType:            MessageType
)

object EisIE507Header:
  given eisIE507HeaderTag: XmlRootTag[EisIE507Header] = XmlRootTag("Header")

  given eisIE507HeaderXmlWriter: XmlWriter[EisIE507Header] =
    (o, label) =>
      val children: NodeSeq =
        o.messageSender.toXml("messageSender")
          ++ o.messageRecipient.toXml("messageRecipient")
          ++ o.preparationDateAndTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).toXml("preparationDateAndTime")
          ++ o.messageIdentification.toXml("messageIdentification")
          ++ o.messageType.toXml("messageType")

      XmlWriter.elem(label, children)

  def apply(
    eoriNumber:             EoriNumber,
    preparationDateAndTime: LocalDateTime,
    messageIdentification:  MessageIdentification
  ): EisIE507Header =
    EisIE507Header(
      MessageSender(eoriNumber.value),
      MessageRecipient("NECA.XI"),
      preparationDateAndTime,
      messageIdentification,
      MessageType("CC507C")
    )
