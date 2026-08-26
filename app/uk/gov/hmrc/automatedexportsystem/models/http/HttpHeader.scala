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

package uk.gov.hmrc.automatedexportsystem.models.http

import play.api.http.HeaderNames

enum HttpHeader(val name: String, val value: String):
  def normalized: (String, String) = (name, value)

  case CorrelationId(override val value: String) extends HttpHeader(CustomHeaderNames.X_CORRELATION_ID, value)
  case ConversationId(override val value: String) extends HttpHeader(CustomHeaderNames.X_CONVERSATION_ID, value)
  case MessageType(override val value: String) extends HttpHeader(CustomHeaderNames.X_MESSAGE_TYPE, value)
  case ForwardedHost(override val value: String) extends HttpHeader(HeaderNames.X_FORWARDED_HOST, value)
  case ContentType(override val value: String) extends HttpHeader(HeaderNames.CONTENT_TYPE, value)
  case Accept(override val value: String) extends HttpHeader(HeaderNames.ACCEPT, value)
  case Authorization(override val value: String) extends HttpHeader(HeaderNames.AUTHORIZATION, value)
  case Date(override val value: String) extends HttpHeader(HeaderNames.DATE, value)
