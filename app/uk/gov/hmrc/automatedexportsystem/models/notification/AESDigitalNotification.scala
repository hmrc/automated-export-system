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

package uk.gov.hmrc.automatedexportsystem.models.notification

import scala.xml.NodeSeq

final case class AESDigitalNotification(
  header: Header,
  body:   Body
)

final case class Header(
  messageSender:         String,
  messageRecipient:      String,
  preparationDateTime:   String,
  messageIdentification: String,
  messageType:           String,
  correlationIdentifier: String
)

final case class Body(
  messageCode: String,
  actionCode:  String,
  mrn:         String
)

object AESDigitalNotification {

  def fromXml(xml: NodeSeq): AESDigitalNotification = {
    val headerNode = xml \ "Header"
    val bodyNode   = xml \ "Body"

    AESDigitalNotification(
      header = Header(
        messageSender = (headerNode \ "messageSender").text.trim,
        messageRecipient = (headerNode \ "messageRecipient").text.trim,
        preparationDateTime = (headerNode \ "preparationDateTime").text.trim,
        messageIdentification = (headerNode \ "messageIdentification").text.trim,
        messageType = (headerNode \ "messageType").text.trim,
        correlationIdentifier = (headerNode \ "correlationIdentifier").text.trim
      ),
      body = Body(
        messageCode = (bodyNode \ "messageCode").text.trim,
        actionCode = (bodyNode \ "actionCode").text.trim,
        mrn = (bodyNode \ "MRN").text.trim
      )
    )
  }
}
