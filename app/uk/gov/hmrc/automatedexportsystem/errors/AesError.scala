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

package uk.gov.hmrc.automatedexportsystem.errors

import cats.data.NonEmptyList
import uk.gov.hmrc.automatedexportsystem.errors.ResponseCode.*

import scala.xml.SAXParseException

sealed trait AesError:
  val message:      String
  val responseCode: ResponseCode
  val exception:    Option[Throwable]

enum SchemaError(val message: String, val responseCode: ResponseCode, val exception: Option[Throwable]) extends AesError:
  case SchemaNotFoundError(xsdPath: String) extends SchemaError(s"XSD Schema not found: $xsdPath", InternalServerError, None)
  case SchemaParseError(xsdError: SchemaError.XsdStructureError) extends SchemaError("XSD Schema could not be parsed", UnprocessableEntity, None)

object SchemaError:
  case class XsdStructureError(line: Int, column: Int, message: String)

  object XsdStructureError:
    def fromSaxe(saxe: SAXParseException): XsdStructureError =
      XsdStructureError(saxe.getLineNumber, saxe.getColumnNumber, saxe.getMessage)
end SchemaError

case class XmlSchemaValidationError(line: Int, column: Int, message: String)

object XmlSchemaValidationError:
  def fromSaxe(saxe: SAXParseException): XmlSchemaValidationError =
    XmlSchemaValidationError(saxe.getLineNumber, saxe.getColumnNumber, saxe.getMessage)

case class XmlFailedValidationError(errors: NonEmptyList[XmlSchemaValidationError]) extends AesError:
  val message:      String            = "XML failed schema validation"
  val responseCode: ResponseCode      = BadRequest
  val exception:    Option[Exception] = None

enum RequestError(val message: String, val responseCode: ResponseCode, val exception: Option[Throwable]) extends AesError:
  case ExpectedXmlBodyError extends RequestError("The body of the request is not valid XML", UnsupportedMediaType, None)

enum XmlReaderError(val path: String, val message: String) extends AesError:
  val responseCode: ResponseCode      = BadRequest
  val exception:    Option[Throwable] = None

  case MissingOrEmpty(override val path: String) extends XmlReaderError(path, "Element is empty or missing")
  case ParseError(override val path: String, override val message: String) extends XmlReaderError(path, message)

enum MongoError(val message: String, val responseCode: ResponseCode, val exception: Option[Throwable]) extends AesError:
  case DocumentNotFound(details: String) extends MongoError(s"Document not found: $details", BadRequest, None)
  case UnexpectedError(ex: Throwable) extends MongoError("Unexpected error encountered while performing DB query", InternalServerError, Some(ex))
