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

package uk.gov.hmrc.automatedexportsystem.controllers

import cats.data.{EitherT, NonEmptyList}
import helpers.XmlOps
import org.apache.pekko.util.ByteString
import org.mockito.ArgumentMatchers.eq as eqTo
import org.mockito.Mockito.when
import org.scalatest.EitherValues
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.{HttpVerbs, MimeTypes, Status as StatusValues}
import play.api.mvc.{ControllerComponents, Result}
import play.api.test.{DefaultAwaitTimeout, FakeRequest, Helpers}
import uk.gov.hmrc.automatedexportsystem.controllers.actions.{XmlPayloadActionRefiner, XmlValidationActionRefiner}
import uk.gov.hmrc.automatedexportsystem.errors.{SchemaError, XmlFailedValidationError, XmlSchemaValidationError}
import uk.gov.hmrc.automatedexportsystem.services.AesIE507XmlValidationService

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.{Elem, NodeSeq}

class SubmissionControllerSpec extends AnyFreeSpecLike, Matchers, EitherValues, DefaultAwaitTimeout, MockitoSugar:
  given ec: ExecutionContext = ExecutionContext.global

  val controllerComponents: ControllerComponents = Helpers.stubControllerComponents(executionContext = ec)

  val xmlPayloadActionRefiner: XmlPayloadActionRefiner = XmlPayloadActionRefiner()

  val xmlValidationService: AesIE507XmlValidationService = mock[AesIE507XmlValidationService]

  val xmlValidationActionRefiner: XmlValidationActionRefiner[AesIE507XmlValidationService] =
    XmlValidationActionRefiner(xmlValidationService)

  val submissionController: SubmissionController =
    SubmissionController(
      controllerComponents,
      xmlPayloadActionRefiner,
      xmlValidationActionRefiner
    )

  "SubmissionController" - {

    ".message" - {

      "should return an Action" - {

        "that returns a 202 Result" - {

          "when applied with a Request containing a valid XML body that passes IE507 request schema validation" in {
            val requestXml: Elem =
              <element>I'm valid XML</element>

            val request: FakeRequest[NodeSeq] =
              FakeRequest(HttpVerbs.POST, "/dummy/path")
                .withBody(requestXml)

            when(xmlValidationService.validate(eqTo(requestXml))).thenReturn(EitherT(Future.successful(Right(()))))

            val result: Future[Result] = submissionController.message.apply(request)

            Helpers.status(result)         shouldBe StatusValues.ACCEPTED
            Helpers.contentType(result)    shouldBe None
            Helpers.contentAsBytes(result) shouldBe ByteString.empty
          }
        }

        "that returns a 500 Result" - {

          "when applied with a Request containing a valid XML body but XSD schema cannot be found" in {
            val requestXml: Elem =
              <element>I'm valid XML</element>

            val request: FakeRequest[NodeSeq] =
              FakeRequest(HttpVerbs.POST, "/dummy/path")
                .withBody(requestXml)

            val schemaError: SchemaError = SchemaError.SchemaNotFoundError("/schemas/dummy.xsd")

            when(xmlValidationService.validate(eqTo(requestXml)))
              .thenReturn(EitherT(Future.successful(Left(schemaError))))

            val result: Future[Result] = submissionController.message.apply(request)

            val schemaNotFoundErrorResponseXml: Elem =
              <errorResponse>
                <status>500</status>
                <code>INTERNAL_SERVER_ERROR</code>
                <message>XSD Schema not found: /schemas/dummy.xsd</message>
              </errorResponse>

            val resultContent: String = Helpers.contentAsString(result)
            val resultXml:     Elem   = XmlOps.loadXmlFromString(resultContent).value

            Helpers.status(result)               shouldBe StatusValues.INTERNAL_SERVER_ERROR
            Helpers.contentType(result)          shouldBe Some(MimeTypes.XML)
            XmlOps.normalize(resultXml).toString shouldBe XmlOps.normalize(schemaNotFoundErrorResponseXml).toString
          }
        }

        "that returns a 422 Result" - {

          "when applied with a Request containing a valid XML body but XSD schema cannot be parsed" in {
            val requestXml: Elem =
              <element>I'm valid XML</element>

            val request: FakeRequest[NodeSeq] =
              FakeRequest(HttpVerbs.POST, "/dummy/path")
                .withBody(requestXml)

            val schemaError: SchemaError =
              SchemaError.SchemaParseError(SchemaError.XsdStructureError(1, 1, "Bad parse error"))

            when(xmlValidationService.validate(eqTo(requestXml)))
              .thenReturn(EitherT(Future.successful(Left(schemaError))))

            val result: Future[Result] = submissionController.message.apply(request)

            val schemaParseErrorResponseXml: Elem =
              <errorResponse>
                <status>422</status>
                <code>UNPROCESSABLE_ENTITY</code>
                <message>XSD Schema could not be parsed</message>
              </errorResponse>

            val resultContent: String = Helpers.contentAsString(result)
            val resultXml:     Elem   = XmlOps.loadXmlFromString(resultContent).value

            Helpers.status(result)               shouldBe StatusValues.UNPROCESSABLE_ENTITY
            Helpers.contentType(result)          shouldBe Some(MimeTypes.XML)
            XmlOps.normalize(resultXml).toString shouldBe XmlOps.normalize(schemaParseErrorResponseXml).toString
          }
        }

        "that returns a 400 Result" - {

          "when applied with a Request containing a valid XML body that doesn't pass IE507 request schema validation" - {

            "due to an XmlSchemaValidationError" in {
              val requestXml: Elem =
                <element>I'm valid XML</element>

              val request: FakeRequest[NodeSeq] =
                FakeRequest(HttpVerbs.POST, "/dummy/path")
                  .withBody(requestXml)

              val xmlFailedValidationError: XmlFailedValidationError =
                XmlFailedValidationError(
                  NonEmptyList.one(
                    XmlSchemaValidationError(1, 1, "Bad parse error")
                  )
                )

              when(xmlValidationService.validate(eqTo(requestXml)))
                .thenReturn(EitherT(Future.successful(Left(xmlFailedValidationError))))

              val result: Future[Result] = submissionController.message.apply(request)

              val xmlFailedValidationErrorResponseXml: Elem =
                <errorResponse>
                  <status>400</status>
                  <code>BAD_REQUEST</code>
                  <message>XML failed schema validation</message>
                  <errors>
                    <error>
                      <line>1</line>
                      <column>1</column>
                      <message>Bad parse error</message>
                    </error>
                  </errors>
                </errorResponse>

              val resultContent: String = Helpers.contentAsString(result)
              val resultXml:     Elem   = XmlOps.loadXmlFromString(resultContent).value

              Helpers.status(result)               shouldBe StatusValues.BAD_REQUEST
              Helpers.contentType(result)          shouldBe Some(MimeTypes.XML)
              XmlOps.normalize(resultXml).toString shouldBe XmlOps.normalize(xmlFailedValidationErrorResponseXml).toString
            }

            "due to many XmlSchemaValidationError" in {
              val requestXml: Elem =
                <element>I'm valid XML</element>

              val request: FakeRequest[NodeSeq] =
                FakeRequest(HttpVerbs.POST, "/dummy/path")
                  .withBody(requestXml)

              val xmlFailedValidationError: XmlFailedValidationError =
                XmlFailedValidationError(
                  NonEmptyList.of(
                    XmlSchemaValidationError(1, 1, "Bad parse error 1"),
                    XmlSchemaValidationError(2, 1, "Bad parse error 2"),
                    XmlSchemaValidationError(3, 1, "Bad parse error 3"),
                    XmlSchemaValidationError(4, 1, "Bad parse error 4"),
                    XmlSchemaValidationError(5, 1, "Bad parse error 5")
                  )
                )

              when(xmlValidationService.validate(eqTo(requestXml)))
                .thenReturn(EitherT(Future.successful(Left(xmlFailedValidationError))))

              val result: Future[Result] = submissionController.message.apply(request)

              val xmlFailedValidationErrorResponseXml: Elem =
                <errorResponse>
                  <status>400</status>
                  <code>BAD_REQUEST</code>
                  <message>XML failed schema validation</message>
                  <errors>
                    <error>
                      <line>1</line>
                      <column>1</column>
                      <message>Bad parse error 1</message>
                    </error>
                    <error>
                      <line>2</line>
                      <column>1</column>
                      <message>Bad parse error 2</message>
                    </error>
                    <error>
                      <line>3</line>
                      <column>1</column>
                      <message>Bad parse error 3</message>
                    </error>
                    <error>
                      <line>4</line>
                      <column>1</column>
                      <message>Bad parse error 4</message>
                    </error>
                    <error>
                      <line>5</line>
                      <column>1</column>
                      <message>Bad parse error 5</message>
                    </error>
                  </errors>
                </errorResponse>

              val resultContent: String = Helpers.contentAsString(result)
              val resultXml:     Elem   = XmlOps.loadXmlFromString(resultContent).value

              Helpers.status(result)               shouldBe StatusValues.BAD_REQUEST
              Helpers.contentType(result)          shouldBe Some(MimeTypes.XML)
              XmlOps.normalize(resultXml).toString shouldBe XmlOps.normalize(xmlFailedValidationErrorResponseXml).toString
            }
          }
        }
      }
    }
  }
