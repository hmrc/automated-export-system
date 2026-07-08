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

package uk.gov.hmrc.automatedexportsystem.controllers.actions

import cats.data.{EitherT, NonEmptyList}
import helpers.XmlOps
import org.apache.pekko.util.ByteString
import org.mockito.ArgumentMatchers.eq as eqTo
import org.mockito.Mockito.*
import org.scalactic.source.Position
import org.scalatest.EitherValues
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.{HttpVerbs, MimeTypes, Status as StatusValues}
import play.api.mvc.Results.Status
import play.api.mvc.{AnyContent, Request, Result}
import play.api.test.{DefaultAwaitTimeout, FakeRequest, Helpers}
import uk.gov.hmrc.automatedexportsystem.errors.{SchemaError, XmlFailedValidationError, XmlSchemaValidationError}
import uk.gov.hmrc.automatedexportsystem.models.actions.XmlPayloadRequest
import uk.gov.hmrc.automatedexportsystem.services.XmlValidationService

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.Elem

class XmlValidationActionRefinerSpec extends AnyFreeSpecLike, Matchers, EitherValues, DefaultAwaitTimeout, MockitoSugar:
  given ec: ExecutionContext = ExecutionContext.global

  val xmlValidationService: XmlValidationService = mock[XmlValidationService]

  val xmlValidationActionRefiner: XmlValidationActionRefiner[XmlValidationService] =
    XmlValidationActionRefiner(xmlValidationService)

  "XmlValidationActionRefiner" - {
    ".invokeBlock" - {
      "should return a Result" - {
        val successfulBlock: Request[AnyContent] => Future[Result] = _ =>
          Future.successful(
            Status(StatusValues.OK)
          )

        "when XML validation succeeds" in {
          val xml: Elem =
            <element>validate me</element>

          val request: FakeRequest[AnyContent] = FakeRequest.apply(
            HttpVerbs.POST,
            "/dummy/path"
          )

          val xmlPayloadRequest: XmlPayloadRequest[AnyContent] = XmlPayloadRequest(xml, request)

          when(xmlValidationService.validate(eqTo(xml))).thenReturn(EitherT(Future.successful(Right(()))))

          val result: Future[Result] = xmlValidationActionRefiner.invokeBlock(xmlPayloadRequest, successfulBlock)

          Helpers.status(result)         shouldBe StatusValues.OK
          Helpers.contentAsBytes(result) shouldBe ByteString.empty
        }

        "when XML validation fails" - {
          "due to a SchemaNotFoundError" in {
            val xml: Elem =
              <element>validate me</element>

            val request: FakeRequest[AnyContent] = FakeRequest.apply(
              HttpVerbs.POST,
              "/dummy/path"
            )

            val xmlPayloadRequest: XmlPayloadRequest[AnyContent] = XmlPayloadRequest(xml, request)

            val schemaError: SchemaError = SchemaError.SchemaNotFoundError("/schemas/dummy.xsd")

            when(xmlValidationService.validate(eqTo(xml))).thenReturn(EitherT(Future.successful(Left(schemaError))))

            val result: Future[Result] = xmlValidationActionRefiner.invokeBlock(xmlPayloadRequest, successfulBlock)

            val schemaNotFoundErrorResponseXml: Elem =
              <errorResponse>
                <status>500</status>
                <code>INTERNAL_SERVER_ERROR</code>
                <message>XSD Schema not found: /schemas/dummy.xsd</message>
              </errorResponse>

            val resultContent: String = Helpers.contentAsString(result)
            val resultXml:     Elem   = XmlOps.loadXml(resultContent).value

            Helpers.status(result)               shouldBe StatusValues.INTERNAL_SERVER_ERROR
            Helpers.contentType(result)          shouldBe Some(MimeTypes.XML)
            XmlOps.normalize(resultXml).toString shouldBe XmlOps.normalize(schemaNotFoundErrorResponseXml).toString
          }

          "due to a SchemaParseError" in {
            val xml: Elem =
              <element>validate me</element>

            val request: FakeRequest[AnyContent] = FakeRequest.apply(
              HttpVerbs.POST,
              "/dummy/path"
            )

            val xmlPayloadRequest: XmlPayloadRequest[AnyContent] = XmlPayloadRequest(xml, request)

            val schemaError: SchemaError = SchemaError.SchemaParseError(SchemaError.XsdStructureError(1, 1, "Bad parse error"))

            when(xmlValidationService.validate(eqTo(xml))).thenReturn(EitherT(Future.successful(Left(schemaError))))

            val result: Future[Result] = xmlValidationActionRefiner.invokeBlock(xmlPayloadRequest, successfulBlock)

            val schemaNotFoundErrorResponseXml: Elem =
              <errorResponse>
                <status>422</status>
                <code>UNPROCESSABLE_ENTITY</code>
                <message>XSD Schema could not be parsed</message>
              </errorResponse>

            val resultContent: String = Helpers.contentAsString(result)
            val resultXml:     Elem   = XmlOps.loadXml(resultContent).value

            Helpers.status(result)               shouldBe StatusValues.UNPROCESSABLE_ENTITY
            Helpers.contentType(result)          shouldBe Some(MimeTypes.XML)
            XmlOps.normalize(resultXml).toString shouldBe XmlOps.normalize(schemaNotFoundErrorResponseXml).toString
          }

          "due to an XmlSchemaValidationError" in {
            val xml: Elem =
              <element>validate me</element>

            val request: FakeRequest[AnyContent] = FakeRequest.apply(
              HttpVerbs.POST,
              "/dummy/path"
            )

            val xmlPayloadRequest: XmlPayloadRequest[AnyContent] = XmlPayloadRequest(xml, request)

            val xmlFailedValidationError: XmlFailedValidationError =
              XmlFailedValidationError(
                NonEmptyList.one(
                  XmlSchemaValidationError(1, 1, "Bad parse error")
                )
              )

            when(xmlValidationService.validate(eqTo(xml))).thenReturn(
              EitherT(Future.successful(Left(xmlFailedValidationError)))
            )

            val result: Future[Result] = xmlValidationActionRefiner.invokeBlock(xmlPayloadRequest, successfulBlock)

            val schemaNotFoundErrorResponseXml: Elem =
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
            val resultXml:     Elem   = XmlOps.loadXml(resultContent).value

            Helpers.status(result)               shouldBe StatusValues.BAD_REQUEST
            Helpers.contentType(result)          shouldBe Some(MimeTypes.XML)
            XmlOps.normalize(resultXml).toString shouldBe XmlOps.normalize(schemaNotFoundErrorResponseXml).toString
          }

          "due to many XmlSchemaValidationError" in {
            val xml: Elem =
              <element>validate me</element>

            val request: FakeRequest[AnyContent] = FakeRequest.apply(
              HttpVerbs.POST,
              "/dummy/path"
            )

            val xmlPayloadRequest: XmlPayloadRequest[AnyContent] = XmlPayloadRequest(xml, request)

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

            when(xmlValidationService.validate(eqTo(xml))).thenReturn(
              EitherT(Future.successful(Left(xmlFailedValidationError)))
            )

            val result: Future[Result] = xmlValidationActionRefiner.invokeBlock(xmlPayloadRequest, successfulBlock)

            val schemaNotFoundErrorResponseXml: Elem =
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
            val resultXml:     Elem   = XmlOps.loadXml(resultContent).value

            Helpers.status(result)               shouldBe StatusValues.BAD_REQUEST
            Helpers.contentType(result)          shouldBe Some(MimeTypes.XML)
            XmlOps.normalize(resultXml).toString shouldBe XmlOps.normalize(schemaNotFoundErrorResponseXml).toString
          }
        }
      }
    }
  }
