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

package uk.gov.hmrc.automatedexportsystem.services

import uk.gov.hmrc.automatedexportsystem.models.IE507.EoriNumber
import uk.gov.hmrc.automatedexportsystem.models.IE507.aes.AesIE507Message
import uk.gov.hmrc.automatedexportsystem.models.IE507.eis.{EisIE507Body, EisIE507Header, EisIE507Message, MessageIdentification}
import uk.gov.hmrc.automatedexportsystem.models.eis.{EisIE507Request, EisIE507RequestHeaders}
import uk.gov.hmrc.automatedexportsystem.models.http.HttpHeader
import uk.gov.hmrc.automatedexportsystem.util.IdGenerator

import java.time.*
import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}

@Singleton
class EisIE507Factory @Inject() (clock: Clock, idGenerator: IdGenerator):
  def request(
    aesIE507Message:     AesIE507Message,
    eori:                EoriNumber,
    authorization:       HttpHeader.Authorization,
    maybeCorrelationId:  Option[HttpHeader.CorrelationId],
    maybeConversationId: Option[HttpHeader.ConversationId]
  ): EisIE507Request =
    val instantNow: Instant = Instant.now(clock)

    val correlationId: HttpHeader.CorrelationId =
      maybeCorrelationId.getOrElse(HttpHeader.CorrelationId(idGenerator.generate35Char))

    val conversationId: HttpHeader.ConversationId =
      maybeConversationId.getOrElse(HttpHeader.ConversationId(idGenerator.generate35Char))

    val dateHeader: HttpHeader.Date =
      HttpHeader.Date(
        DateTimeFormatter.RFC_1123_DATE_TIME
          .format(instantNow.atOffset(ZoneOffset.UTC))
      )

    val headers: EisIE507RequestHeaders =
      EisIE507RequestHeaders(correlationId, conversationId, authorization, dateHeader)

    val messageHeader: EisIE507Header =
      EisIE507Header(
        eori,
        LocalDateTime.ofInstant(instantNow, ZoneOffset.UTC),
        MessageIdentification(correlationId.value)
      )

    val messageBody: EisIE507Body = EisIE507Body.fromAesIE507Message(aesIE507Message)

    val message: EisIE507Message = EisIE507Message(messageHeader, messageBody)

    EisIE507Request(headers, message)
