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
import play.api.mvc.Result
import play.api.mvc.Results.Status
import uk.gov.hmrc.automatedexportsystem.errors.*
import uk.gov.hmrc.automatedexportsystem.models.responses.AesErrorResponse.AesErrorResponseValidationError

import scala.xml.*

final case class AesErrorResponse(status: Int, code: String, message: String, errors: Option[NonEmptyList[AesErrorResponseValidationError]]):
  def toXml: Elem =
    val errorNodes: Option[Seq[Node]] =
      errors.map(nel =>
        nel
          .map(err => <error>
              <line>{err.line}</line>
              <column>{err.column}</column>
              <message>{err.message}</message>
            </error>)
          .toList
      )

    val xml: Elem =
      <errorResponse>
        <status>{status}</status>
        <code>{code}</code>
        <message>{message}</message>
        {
        errorNodes.fold(NodeSeq.Empty)(nodes => <errors>
            {nodes}
          </errors>)
      }
      </errorResponse>

    xml
  end toXml

  def toResult: Result = Status(status)(toXml)
end AesErrorResponse

object AesErrorResponse:
  final case class AesErrorResponseValidationError(line: Int, column: Int, message: String)

  object AesErrorResponseValidationError:
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
        case _: SchemaError =>
          AesErrorResponse(responseCode.status, responseCode.code, errorMessage, None)
        case XmlFailedValidationError(errors) =>
          AesErrorResponse(
            responseCode.status,
            responseCode.code,
            errorMessage,
            Some(errors.map(AesErrorResponseValidationError.fromXmlSchemaValidationError))
          )
        case _: RequestError =>
          AesErrorResponse(responseCode.status, responseCode.code, errorMessage, None)
        case _: XmlReaderError =>
          AesErrorResponse(responseCode.status, responseCode.code, errorMessage, None)
        case _: MongoError =>
          AesErrorResponse(responseCode.status, responseCode.code, errorMessage, None)
end AesErrorResponse
