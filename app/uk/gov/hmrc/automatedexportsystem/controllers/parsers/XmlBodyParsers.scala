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

package uk.gov.hmrc.automatedexportsystem.controllers.parsers

import cats.syntax.either.*
import play.api.mvc.{BodyParser, PlayBodyParsers}
import uk.gov.hmrc.automatedexportsystem.errors.RequestError
import uk.gov.hmrc.automatedexportsystem.models.responses.AesErrorResponse.toErrorResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import scala.xml.NodeSeq

@Singleton
class XmlBodyParsers @Inject() (parsers: PlayBodyParsers):
  def utf8: BodyParser[NodeSeq] =
    parsers.using(request =>
      request.mediaType
        .toRight(RequestError.MissingContentTypeHeader)
        .flatMap(_ =>
          Either.cond(
            request.charset.forall(_.equalsIgnoreCase("utf-8")),
            parsers.xml,
            RequestError.ContentTypeNotUtf8Error
          )
        )
        .valueOr(error => parsers.error(Future.successful(error.toErrorResponse.toResult)))
    )
