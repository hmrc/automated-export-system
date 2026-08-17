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

import cats.data.NonEmptyList
import play.api.http.Writeable
import play.api.mvc.Result
import play.api.mvc.Results.Status
import uk.gov.hmrc.automatedexportsystem.errors.*
import uk.gov.hmrc.automatedexportsystem.models.responses.AesErrorResponse.AesErrorResponseValidationError
import uk.gov.hmrc.automatedexportsystem.xml.RootedXmlWriter.toXmlRoot
import uk.gov.hmrc.automatedexportsystem.xml.XmlWriter.toXml
import uk.gov.hmrc.automatedexportsystem.xml.{RootedXmlWriter, XmlRootTag, XmlWriter}

import scala.xml.*

final case class AesErrorResponse(status: Int, code: String, message: String, errors: Option[NonEmptyList[AesErrorResponseValidationError]]):
  private def self: AesErrorResponse = this

  def toResult(using Writeable[NodeSeq]): Result = Status(status)(self.toXmlRoot)

object AesErrorResponse:
  given aesErrorResponseTag: XmlRootTag[AesErrorResponse] = XmlRootTag("errorResponse")

  given aesErrorResponseXmlWriter: XmlWriter[AesErrorResponse] =
    (o, label) =>
      val children: NodeSeq =
        o.status.toXml("status")
          ++ o.code.toXml("code")
          ++ o.message.toXml("message")
          ++ XmlWriter.optElem("errors", o.errors.toXmlRoot)

      XmlWriter.elem(label, children)

  final case class AesErrorResponseValidationError(line: Int, column: Int, message: String)

  object AesErrorResponseValidationError:
    given aesErrorResponseValidationErrorTag: XmlRootTag[AesErrorResponseValidationError] =
      XmlRootTag("error")

    given aesErrorResponseValidationErrorXmlWriter: XmlWriter[AesErrorResponseValidationError] =
      (o, label) =>
        val children: NodeSeq =
          o.line.toXml("line")
            ++ o.column.toXml("column")
            ++ o.message.toXml("message")

        XmlWriter.elem(label, children)

    def fromXmlSchemaValidationError(error: XmlSchemaValidationError): AesErrorResponseValidationError =
      AesErrorResponseValidationError(error.line, error.column, error.message)

  def fromResponseCode(responseCode: ResponseCode, message: String): AesErrorResponse =
    fromStatusAndCode(responseCode.status, responseCode.code, message)

  def fromStatusAndCode(status: Int, code: String, message: String): AesErrorResponse =
    AesErrorResponse(status, code, message, None)

  extension (error: AesError)
    def toErrorResponse: AesErrorResponse =
      val responseCode: ResponseCode = error.responseCode
      val errorMessage: String       = error.message

      error match
        case XmlFailedValidationError(errors) =>
          AesErrorResponse(
            responseCode.status,
            responseCode.code,
            errorMessage,
            Some(errors.map(AesErrorResponseValidationError.fromXmlSchemaValidationError))
          )
        case _ => AesErrorResponse(responseCode.status, responseCode.code, errorMessage, None)
end AesErrorResponse
