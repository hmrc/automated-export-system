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
end AesErrorResponse

object AesErrorResponse:
  final case class AesErrorResponseValidationError(line: Int, column: Int, message: String)

  object AesErrorResponseValidationError:
    def fromXmlSchemaValidationError(error: XmlSchemaValidationError): AesErrorResponseValidationError =
      AesErrorResponseValidationError(error.line, error.column, error.message)
end AesErrorResponse

extension (error: AesError)
  def toErrorResponse: AesErrorResponse =
    val statusCode:   StatusCode = error.statusCode
    val errorMessage: String     = error.message

    error match
      case _: SchemaError =>
        AesErrorResponse(statusCode.status, statusCode.code, errorMessage, None)
      case XmlFailedValidationError(errors) =>
        AesErrorResponse(
          statusCode.status,
          statusCode.code,
          errorMessage,
          Some(errors.map(AesErrorResponseValidationError.fromXmlSchemaValidationError))
        )
      case _: RequestError =>
        AesErrorResponse(statusCode.status, statusCode.code, errorMessage, None)
end extension
