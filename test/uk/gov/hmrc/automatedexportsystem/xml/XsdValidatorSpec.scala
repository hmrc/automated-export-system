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
import org.scalatest.EitherValues
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers
import uk.gov.hmrc.automatedexportsystem.errors.SchemaError.{SchemaNotFoundError, SchemaParseError}
import uk.gov.hmrc.automatedexportsystem.errors.{SchemaError, XmlFailedValidationError, XmlSchemaValidationError}

import scala.xml.Elem

class XsdValidatorSpec extends AnyFreeSpecLike, Matchers, EitherValues:
  "XsdValidator" - {

    ".fromXsdPath" - {

      "should return a schema error" - {

        "when failing to locate the XSD resource" in {
          val result: Either[SchemaError, XsdValidator] = XsdValidator.fromXsdPath("/schemas/test-schema-not-found.xsd")

          val error: SchemaNotFoundError = SchemaError.SchemaNotFoundError("/schemas/test-schema-not-found.xsd")

          result.left.value shouldBe error
        }

        "when failing to parse the XSD" in {
          val result: Either[SchemaError, XsdValidator] = XsdValidator.fromXsdPath("/schemas/test-schema-malformed.xsd")

          val error: SchemaParseError = SchemaError.SchemaParseError(
            SchemaError.XsdStructureError(1, 38, "The XML declaration must end with \"?>\".")
          )

          result.left.value shouldBe error
        }
      }
    }

    ".validate" - {
      val xsdValidator: XsdValidator = XsdValidator.fromXsdPath("/schemas/test-schema.xsd").value

      "should validate successfully" - {

        "a matching XML document with all optionals present" in {
          val xml: Elem =
            <note>
              <to initials="AB" kind="human">"you"</to>
              <from kind="alien" initials="CDEFG">"me"</from>
              <date>2077-01-01</date>
              <heading>"hello there"</heading>
              <body>"I've come in peace"</body>
              <id>69420</id>
            </note>

          val result: Either[XmlFailedValidationError, Unit] = xsdValidator.validate(xml)

          result.value shouldBe ()
        }

        "a matching XML document with all optionals missing" in {
          val xml: Elem =
            <note>
              <to kind="human">"you"</to>
              <from kind="alien">"me"</from>
              <date>2077-01-01</date>
              <heading>"hello there"</heading>
              <body>"I've come in peace"</body>
            </note>

          val result: Either[XmlFailedValidationError, Unit] = xsdValidator.validate(xml)

          result.value shouldBe ()
        }
      }

      "should fail to validate" - {

        "an XML document" - {

          "where an element has the wrong name" in {
            val xml: Elem =
              <note>
                <to kind="human">"you"</to>
                <from kind="alien">"me"</from>
                <time>2077-01-01</time>
                <heading>"hello there"</heading>
                <body>"I've come in peace"</body>
              </note>

            val result: Either[XmlFailedValidationError, Unit] = xsdValidator.validate(xml)

            val error: XmlFailedValidationError = XmlFailedValidationError(
              NonEmptyList.one(
                XmlSchemaValidationError(
                  4,
                  23,
                  "cvc-complex-type.2.4.a: Invalid content was found starting with element 'time'. One of '{date}' is expected."
                )
              )
            )

            result.left.value shouldBe error
          }

          "where a required element is missing" in {
            val xml: Elem =
              <note>
                <to kind="human">"you"</to>
                <from kind="alien">"me"</from>
                <heading>"hello there"</heading>
                <body>"I've come in peace"</body>
              </note>

            val result: Either[XmlFailedValidationError, Unit] = xsdValidator.validate(xml)

            val error: XmlFailedValidationError = XmlFailedValidationError(
              NonEmptyList.one(
                XmlSchemaValidationError(
                  4,
                  26,
                  "cvc-complex-type.2.4.a: Invalid content was found starting with element 'heading'. One of '{date}' is expected."
                )
              )
            )

            result.left.value shouldBe error
          }

          "where an element is invalid" in {
            val xml: Elem =
              <note>
                <to kind="human">"you"</to>
                <from kind="alien">"me"</from>
                <date>2077-01-01</date>
                <heading>"hello there"</heading>
                <body>"I've come in peace"</body>
                <id>-10231</id>
              </note>

            val result: Either[XmlFailedValidationError, Unit] = xsdValidator.validate(xml)

            val error: XmlFailedValidationError = XmlFailedValidationError(
              NonEmptyList.of(
                XmlSchemaValidationError(
                  7,
                  32,
                  "cvc-minInclusive-valid: Value '-10231' is not facet-valid with respect to minInclusive '1' for type 'positiveInteger'."
                ),
                XmlSchemaValidationError(7, 32, "cvc-type.3.1.3: The value '-10231' of element 'id' is not valid.")
              )
            )

            result.left.value shouldBe error
          }

          "where an element is duplicated" in {
            val xml: Elem =
              <note>
                <to kind="human">"you"</to>
                <from kind="alien">"me"</from>
                <date>2077-01-01</date>
                <heading>"hello there"</heading>
                <body>"I've come in peace"</body>
                <id>10231</id>
                <id>10</id>
              </note>

            val result: Either[XmlFailedValidationError, Unit] = xsdValidator.validate(xml)

            val error: XmlFailedValidationError = XmlFailedValidationError(
              NonEmptyList.one(
                XmlSchemaValidationError(
                  8,
                  21,
                  "cvc-complex-type.2.4.d: Invalid content was found starting with element 'id'. No child element is expected at this point."
                )
              )
            )

            result.left.value shouldBe error
          }

          "where there are multiple problems" in {
            val xml: Elem =
              <note>
                <time>2077-01-01</time>
                <to kind="rodent" rodent="capybara">"you"</to>
                <from initials ="ALIEN123" kind="alien">"me"</from>
                <heading>"hello there"</heading>
                <body>"I've come in peace"</body>
                <id>420</id>
                <id>69</id>
              </note>

            val result: Either[XmlFailedValidationError, Unit] = xsdValidator.validate(xml)

            val error: XmlFailedValidationError = XmlFailedValidationError(
              NonEmptyList.of(
                XmlSchemaValidationError(
                  2,
                  23,
                  "cvc-complex-type.2.4.a: Invalid content was found starting with element 'time'. One of '{to}' is expected."
                ),

                XmlSchemaValidationError(
                  3,
                  53,
                  "cvc-enumeration-valid: Value 'rodent' is not facet-valid with respect to enumeration '[human, alien]'. It must be a value from the enumeration."
                ),
                XmlSchemaValidationError(
                  3,
                  53,
                  "cvc-attribute.3: The value 'rodent' of attribute 'kind' on element 'to' is not valid with respect to its type, 'kind'."
                ),
                XmlSchemaValidationError(3, 53, "cvc-complex-type.3.2.2: Attribute 'rodent' is not allowed to appear in element 'to'."),
                XmlSchemaValidationError(
                  4,
                  56,
                  "cvc-pattern-valid: Value 'ALIEN123' is not facet-valid with respect to pattern '[A-Z]{2,5}' for type 'initials'."
                ),
                XmlSchemaValidationError(
                  4,
                  56,
                  "cvc-attribute.3: The value 'ALIEN123' of attribute 'initials' on element 'from' is not valid with respect to its type, 'initials'."
                )
              )
            )

            result.left.value shouldBe error
          }
        }
      }
    }
  }
