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

package uk.gov.hmrc.automatedexportsystem.xml

import cats.data.NonEmptyList
import cats.syntax.either.*
import org.xml.sax.{ErrorHandler, InputSource, SAXParseException}
import uk.gov.hmrc.automatedexportsystem.errors.SchemaError.{SchemaNotFoundError, SchemaParseError}
import uk.gov.hmrc.automatedexportsystem.errors.{SchemaError, XmlFailedValidationError, XmlSchemaValidationError}

import java.io.StringReader
import javax.xml.XMLConstants
import javax.xml.parsers.SAXParserFactory
import javax.xml.transform.Source
import javax.xml.transform.sax.SAXSource
import javax.xml.validation.{Schema, SchemaFactory, Validator}
import scala.collection.mutable.ArrayBuffer
import scala.xml.{InputSource, NodeSeq}

final class XsdValidator private (schema: Schema):
  def validate(xml: NodeSeq): Either[XmlFailedValidationError, Unit] =
    val xmlSource: Source = XsdValidator.xmlToSaxSource(xml)
    validate(xmlSource)

  private def validate(xml: Source): Either[XmlFailedValidationError, Unit] =
    val errorHandler: XsdValidator.XsdErrorHandler = XsdValidator.XsdErrorHandler()
    val validator:    Validator                    = getValidator(errorHandler)

    val thrown: Option[SAXParseException] =
      Either.catchOnly[SAXParseException](validator.validate(xml)).left.toOption

    val allErrors: List[SAXParseException] =
      (errorHandler.getErrors ++ thrown.toList)
        .distinctBy(e => (e.getLineNumber, e.getColumnNumber, e.getMessage))

    NonEmptyList
      .fromList(allErrors)
      .map(saxeNel => XmlFailedValidationError(saxeNel.map(XmlSchemaValidationError.fromSaxe)))
      .toLeft(())

  private def getValidator(errorHandler: ErrorHandler): Validator =
    val validator: Validator = schema.newValidator()
    validator.setErrorHandler(errorHandler)
    validator
end XsdValidator

object XsdValidator:
  private class XsdErrorHandler extends ErrorHandler:
    private lazy val errorBuffer: ArrayBuffer[SAXParseException] = ArrayBuffer.empty

    def warning(exception: SAXParseException): Unit =
      errorBuffer.addOne(exception)

    def error(exception: SAXParseException): Unit =
      errorBuffer.addOne(exception)

    def fatalError(exception: SAXParseException): Unit =
      errorBuffer.addOne(exception)
      throw exception

    def getErrors: List[SAXParseException] = errorBuffer.toList
  end XsdErrorHandler

  private def xmlToSaxSource(xml: NodeSeq): Source =
    val spf = SAXParserFactory.newInstance()
    spf.setNamespaceAware(true)
    spf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)

    val xmlReader   = spf.newSAXParser().getXMLReader
    val inputSource = InputSource(new StringReader(xml.toString))
    SAXSource(xmlReader, inputSource)

  def fromXsdPath(path: String): Either[SchemaError, XsdValidator] =
    Either
      .catchNonFatal {
        val schemaFactory: SchemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
        schemaFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false)
        val schema: Schema = schemaFactory.newSchema(getClass.getResource(path))
        XsdValidator(schema)
      }
      .leftMap {
        case _:    NullPointerException => SchemaNotFoundError(xsdPath = path)
        case saxe: SAXParseException    => SchemaParseError(SchemaError.XsdStructureError.fromSaxe(saxe))
      }
