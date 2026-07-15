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

import cats.data.{EitherT, NonEmptyList}
import helpers.XmlOps
import org.scalactic.source.Position
import org.scalatest.EitherValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers
import uk.gov.hmrc.automatedexportsystem.errors.{AesError, XmlFailedValidationError, XmlSchemaValidationError}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.Elem

class XmlValidationServiceSpec extends AnyFreeSpecLike, Matchers, EitherValues, ScalaFutures:
  given ec: ExecutionContext = ExecutionContext.global

  override given patienceConfig: PatienceConfig = PatienceConfig(1500.millis, 15.millis)

  "EisIE507XmlValidationService" - {
    val xmlValidationService: XmlValidationService = EisIE507XmlValidationService()

    "should validate an XML document" - {

      "successfully" - {

        "when processing an IE507 document conforming to the XSD schema" in {
          val xml: Elem = XmlOps.loadXmlFromPath("/testdata/eisIE507RequestValid.xml").value

          val result: EitherT[Future, AesError, Unit] = xmlValidationService.validate(xml)

          result.value.futureValue.value shouldBe ()
        }

        "when processing an IE507 document conforming to the XSD schema, with no optionals" in {
          val xml: Elem = XmlOps.loadXmlFromPath("/testdata/eisIE507RequestValidNoOptionals.xml").value

          val result: EitherT[Future, AesError, Unit] = xmlValidationService.validate(xml)

          result.value.futureValue.value shouldBe ()
        }
      }

      "unsuccessfully" - {

        "when processing an IE507 document with missing required elements" in {
          val xml: Elem = XmlOps.loadXmlFromPath("/testdata/eisIE507RequestInvalidMissingRequired.xml").value

          val result: EitherT[Future, AesError, Unit] = xmlValidationService.validate(xml)

          result.value.futureValue.left.value shouldBe XmlFailedValidationError(
            NonEmptyList.of(
              XmlSchemaValidationError(
                7,
                14,
                "cvc-complex-type.2.4.b: The content of element 'Header' is not complete. One of '{messageType}' is expected."
              ),
              XmlSchemaValidationError(
                13,
                27,
                "cvc-complex-type.2.4.b: The content of element 'ExportOperation' is not complete. One of '{splitIndicator}' is expected."
              ),
              XmlSchemaValidationError(
                15,
                37,
                "cvc-complex-type.2.4.b: The content of element 'CustomsOfficeOfExitActual' is not complete. One of '{referenceNumber}' is expected."
              )
            )
          )
        }

        "when processing an IE507 document where elements don't match the patterns" in {
          val xml: Elem = XmlOps.loadXmlFromPath("/testdata/eisIE507RequestInvalidBadPatterns.xml").value

          val result: EitherT[Future, AesError, Unit] = xmlValidationService.validate(xml)

          result.value.futureValue.left.value shouldBe XmlFailedValidationError(
            NonEmptyList.of(
              XmlSchemaValidationError(
                5,
                68,
                raw"cvc-pattern-valid: Value '2026-01-01' is not facet-valid with respect to pattern '\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}' for type 'UK_PreparationDateAndTimeContentType'."
              ),
              XmlSchemaValidationError(5, 68, "cvc-type.3.1.3: The value '2026-01-01' of element 'preparationDateAndTime' is not valid."),
              XmlSchemaValidationError(
                7,
                41,
                "cvc-pattern-valid: Value 'CC507' is not facet-valid with respect to pattern 'CC507C' for type 'UK_messageCode507'."
              ),
              XmlSchemaValidationError(7, 41, "cvc-type.3.1.3: The value 'CC507' of element 'messageType' is not valid."),
              XmlSchemaValidationError(
                12,
                40,
                "cvc-pattern-valid: Value '45ABABCDEFGH0789' is not facet-valid with respect to pattern '([2][4-9]|[3-9][0-9])[A-Z]{2}[A-Z0-9]{12}[A-E][0-9]' for type 'UK_MRNType'."
              ),
              XmlSchemaValidationError(12, 40, "cvc-type.3.1.3: The value '45ABABCDEFGH0789' of element 'MRN' is not valid."),
              XmlSchemaValidationError(
                17,
                55,
                "cvc-pattern-valid: Value 'EFX0K19' is not facet-valid with respect to pattern '[A-Z]{2}[A-Z0-9]{6}' for type 'UK_ReferenceNumberType'."
              ),
              XmlSchemaValidationError(17, 55, "cvc-type.3.1.3: The value 'EFX0K19' of element 'referenceNumber' is not valid.")
            )
          )
        }

      }
    }
  }

  "AesIE507XmlValidationService" - {
    val xmlValidationService: XmlValidationService = AesIE507XmlValidationService()

    "should validate an XML document" - {

      "successfully" - {

        "when processing an IE507 document conforming to the XSD schema" in {
          val xml: Elem = XmlOps.loadXmlFromPath("/testdata/aesIE507RequestValid.xml").value

          val result: EitherT[Future, AesError, Unit] = xmlValidationService.validate(xml)

          result.value.futureValue.value shouldBe ()
        }

        "when processing an IE507 document conforming to the XSD schema, with no optionals" in {
          val xml: Elem = XmlOps.loadXmlFromPath("/testdata/aesIE507RequestValidNoOptionals.xml").value

          val result: EitherT[Future, AesError, Unit] = xmlValidationService.validate(xml)

          result.value.futureValue.value shouldBe ()
        }
      }

      "unsuccessfully" - {

        "when processing an IE507 document with missing required elements" in {
          val xml: Elem = XmlOps.loadXmlFromPath("/testdata/aesIE507RequestInvalidMissingRequired.xml").value

          val result: EitherT[Future, AesError, Unit] = xmlValidationService.validate(xml)

          result.value.futureValue.left.value shouldBe XmlFailedValidationError(
            NonEmptyList.of(
              XmlSchemaValidationError(
                4,
                26,
                "cvc-complex-type.2.4.a: Invalid content was found starting with element 'ExportOperation'. One of '{status}' is expected."
              ),
              XmlSchemaValidationError(
                6,
                33,
                "cvc-complex-type.2.4.a: Invalid content was found starting with element 'discrepanciesExist'. One of '{MRN}' is expected."
              ),
              XmlSchemaValidationError(
                15,
                34,
                "cvc-complex-type.2.4.a: Invalid content was found starting with element 'LocationOfGoods'. One of '{referenceNumberUCR}' is expected."
              ),
              XmlSchemaValidationError(
                17,
                35,
                "cvc-complex-type.2.4.b: The content of element 'LocationOfGoods' is not complete. One of '{qualifierOfIdentification}' is expected."
              ),
              XmlSchemaValidationError(
                33,
                34,
                "cvc-complex-type.2.4.a: Invalid content was found starting with element 'netMass'. One of '{grossMass}' is expected."
              )
            )
          )
        }

        "when processing an IE507 document where elements don't match the patterns" in {
          val xml: Elem = XmlOps.loadXmlFromPath("/testdata/aesIE507RequestInvalidBadPatterns.xml").value

          val result: EitherT[Future, AesError, Unit] = xmlValidationService.validate(xml)

          result.value.futureValue.left.value shouldBe XmlFailedValidationError(
            NonEmptyList.of(
              XmlSchemaValidationError(
                3,
                24,
                "cvc-pattern-valid: Value '' is not facet-valid with respect to pattern '.{1,35}' for type 'UK_AlphaNumeric35Type'."
              ),
              XmlSchemaValidationError(3, 24, "cvc-type.3.1.3: The value '' of element 'submissionId' is not valid."),
              XmlSchemaValidationError(
                4,
                18,
                "cvc-pattern-valid: Value '' is not facet-valid with respect to pattern '.{1,35}' for type 'UK_AlphaNumeric35Type'."
              ),
              XmlSchemaValidationError(4, 18, "cvc-type.3.1.3: The value '' of element 'status' is not valid."),
              XmlSchemaValidationError(
                6,
                20,
                "cvc-pattern-valid: Value '' is not facet-valid with respect to pattern '[1-3]{1}' for type 'UK_OneToThreeType'."
              ),
              XmlSchemaValidationError(6, 20, "cvc-type.3.1.3: The value '' of element 'type' is not valid."),
              XmlSchemaValidationError(
                7,
                19,
                "cvc-pattern-valid: Value '' is not facet-valid with respect to pattern '([2][4-9]|[3-9][0-9])[A-Z]{2}[A-Z0-9]{12}[A-E][0-9]' for type 'UK_MRNType'."
              ),
              XmlSchemaValidationError(7, 19, "cvc-type.3.1.3: The value '' of element 'MRN' is not valid."),
              XmlSchemaValidationError(
                8,
                34,
                "cvc-enumeration-valid: Value '' is not facet-valid with respect to enumeration '[0, 1]'. It must be a value from the enumeration."
              ),
              XmlSchemaValidationError(8, 34, "cvc-type.3.1.3: The value '' of element 'discrepanciesExist' is not valid."),
              XmlSchemaValidationError(
                9,
                30,
                "cvc-enumeration-valid: Value '' is not facet-valid with respect to enumeration '[0, 1]'. It must be a value from the enumeration."
              ),
              XmlSchemaValidationError(9, 30, "cvc-type.3.1.3: The value '' of element 'splitIndicator' is not valid."),
              XmlSchemaValidationError(
                12,
                31,
                "cvc-pattern-valid: Value '' is not facet-valid with respect to pattern '[A-Z]{2}[A-Z0-9]{6}' for type 'UK_ReferenceNumberType'."
              ),
              XmlSchemaValidationError(12, 31, "cvc-type.3.1.3: The value '' of element 'referenceNumber' is not valid.")
            )
          )
        }

      }
    }
  }
