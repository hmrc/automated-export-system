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

package uk.gov.hmrc.automatedexportsystem.config

import helpers.XmlOps
import org.mockito.ArgumentMatchers.eq as eqTo
import org.mockito.Mockito.when
import org.scalatest.EitherValues
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.*
import play.api.http.{HttpVerbs, MimeTypes, Status as StatusValues}
import play.api.mvc.{AnyContent, Result}
import play.api.routing.Router
import play.api.test.{DefaultAwaitTimeout, FakeRequest, Helpers}
import uk.gov.hmrc.automatedexportsystem.errors.ResponseCode

import javax.inject.Provider
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.Elem

class AesErrorHandlerSpec extends AnyFreeSpecLike, Matchers, EitherValues, DefaultAwaitTimeout, MockitoSugar:
  given ec: ExecutionContext = ExecutionContext.global

  val environment:   Environment          = mock[Environment]
  val configuration: Configuration        = mock[Configuration]
  val sourceMapper:  OptionalSourceMapper = OptionalSourceMapper(None)
  val router:        Provider[Router]     = mock[Provider[Router]]

  "AesErrorHandler" - {
    val request: FakeRequest[AnyContent] = FakeRequest(HttpVerbs.DELETE, "/dummy/path")

    ".onClientError" - {
      when(environment.mode).thenReturn(Mode.Dev)
      when(configuration.getOptional[String](eqTo("play.editor"))(eqTo(ConfigLoader.stringLoader))).thenReturn(None)
      when(router.get()).thenReturn(Router.empty)

      val aesErrorHandler: AesErrorHandler =
        AesErrorHandler(environment, configuration, sourceMapper, router)

      "should return a Result" - {

        "when invoked with a 4xx status error" - {

          "BAD_REQUEST" in {
            val result: Future[Result] =
              aesErrorHandler.onClientError(request, ResponseCode.BadRequest.status, "Very bad request")

            val errorResponseXml: Elem =
              <errorResponse>
                <status>400</status>
                <code>BAD_REQUEST</code>
                <message>Very bad request</message>
              </errorResponse>

            val resultContent: String = Helpers.contentAsString(result)
            val resultXml:     Elem   = XmlOps.loadXmlFromString(resultContent).value

            Helpers.status(result)               shouldBe StatusValues.BAD_REQUEST
            Helpers.contentType(result)          shouldBe Some(MimeTypes.XML)
            XmlOps.normalize(resultXml).toString shouldBe XmlOps.normalize(errorResponseXml).toString
          }

          "FORBIDDEN" in {
            val result: Future[Result] =
              aesErrorHandler.onClientError(request, ResponseCode.Forbidden.status, "Very forbidden request")

            val errorResponseXml: Elem =
              <errorResponse>
                <status>403</status>
                <code>FORBIDDEN</code>
                <message>Very forbidden request</message>
              </errorResponse>

            val resultContent: String = Helpers.contentAsString(result)
            val resultXml:     Elem   = XmlOps.loadXmlFromString(resultContent).value

            Helpers.status(result)               shouldBe StatusValues.FORBIDDEN
            Helpers.contentType(result)          shouldBe Some(MimeTypes.XML)
            XmlOps.normalize(resultXml).toString shouldBe XmlOps.normalize(errorResponseXml).toString
          }

          "NOT_FOUND" in {
            val result: Future[Result] =
              aesErrorHandler.onClientError(request, ResponseCode.NotFound.status, "Very not found request")

            val errorResponseXml: Elem =
              <errorResponse>
                <status>404</status>
                <code>NOT_FOUND</code>
                <message>Very not found request</message>
              </errorResponse>

            val resultContent: String = Helpers.contentAsString(result)
            val resultXml:     Elem   = XmlOps.loadXmlFromString(resultContent).value

            Helpers.status(result)               shouldBe StatusValues.NOT_FOUND
            Helpers.contentType(result)          shouldBe Some(MimeTypes.XML)
            XmlOps.normalize(resultXml).toString shouldBe XmlOps.normalize(errorResponseXml).toString
          }

          "other 4xx error that corresponds to a known ResponseCode" in {
            val result: Future[Result] =
              aesErrorHandler.onClientError(request, ResponseCode.UnprocessableEntity.status, "Very unprocessable entity request")

            val errorResponseXml: Elem =
              <errorResponse>
                <status>422</status>
                <code>UNPROCESSABLE_ENTITY</code>
                <message>Very unprocessable entity request</message>
              </errorResponse>

            val resultContent: String = Helpers.contentAsString(result)
            val resultXml:     Elem   = XmlOps.loadXmlFromString(resultContent).value

            Helpers.status(result)               shouldBe StatusValues.UNPROCESSABLE_ENTITY
            Helpers.contentType(result)          shouldBe Some(MimeTypes.XML)
            XmlOps.normalize(resultXml).toString shouldBe XmlOps.normalize(errorResponseXml).toString
          }

          "other 4xx error not corresponding to any known ResponseCode" in {
            val result: Future[Result] =
              aesErrorHandler.onClientError(request, StatusValues.IM_A_TEAPOT, "Very I'm a teapot request")

            val errorResponseXml: Elem =
              <errorResponse>
                <status>418</status>
                <code>CLIENT_ERROR</code>
                <message>Very I'm a teapot request</message>
              </errorResponse>

            val resultContent: String = Helpers.contentAsString(result)
            val resultXml:     Elem   = XmlOps.loadXmlFromString(resultContent).value

            Helpers.status(result)               shouldBe StatusValues.IM_A_TEAPOT
            Helpers.contentType(result)          shouldBe Some(MimeTypes.XML)
            XmlOps.normalize(resultXml).toString shouldBe XmlOps.normalize(errorResponseXml).toString
          }
        }
      }
    }

    ".onServerError" - {

      "should return a Result" - {

        "when invoked with a 5xx status error" - {

          "when in non-prod mode" in {
            when(environment.mode).thenReturn(Mode.Dev)
            when(configuration.getOptional[String](eqTo("play.editor"))(eqTo(ConfigLoader.stringLoader))).thenReturn(None)
            when(router.get).thenReturn(Router.empty)

            val aesErrorHandler: AesErrorHandler =
              AesErrorHandler(environment, configuration, sourceMapper, router)

            val result: Future[Result] = aesErrorHandler.onServerError(request, Exception("Unexpected exception"))

            val errorResponseXml: Elem =
              <errorResponse>
                <status>500</status>
                <code>INTERNAL_SERVER_ERROR</code>
                <message>Execution exception[[Exception: Unexpected exception]]</message>
              </errorResponse>

            val resultContent: String = Helpers.contentAsString(result)
            val resultXml:     Elem   = XmlOps.loadXmlFromString(resultContent).value

            Helpers.status(result)               shouldBe StatusValues.INTERNAL_SERVER_ERROR
            Helpers.contentType(result)          shouldBe Some(MimeTypes.XML)
            XmlOps.normalize(resultXml).toString shouldBe XmlOps.normalize(errorResponseXml).toString
          }

          "when in prod mode" in {
            when(environment.mode).thenReturn(Mode.Prod)
            when(configuration.getOptional[String](eqTo("play.editor"))(eqTo(ConfigLoader.stringLoader))).thenReturn(None)
            when(router.get).thenReturn(Router.empty)

            val aesErrorHandler: AesErrorHandler =
              AesErrorHandler(environment, configuration, sourceMapper, router)

            val result: Future[Result] = aesErrorHandler.onServerError(request, Exception("Unexpected prod exception"))

            val errorResponseXml: Elem =
              <errorResponse>
                <status>500</status>
                <code>INTERNAL_SERVER_ERROR</code>
                <message>Unexpected exception[Exception: Unexpected prod exception]</message>
              </errorResponse>

            val resultContent: String = Helpers.contentAsString(result)
            val resultXml:     Elem   = XmlOps.loadXmlFromString(resultContent).value

            Helpers.status(result)               shouldBe StatusValues.INTERNAL_SERVER_ERROR
            Helpers.contentType(result)          shouldBe Some(MimeTypes.XML)
            XmlOps.normalize(resultXml).toString shouldBe XmlOps.normalize(errorResponseXml).toString
          }

          "when server error handling failed" in {
            when(environment.mode).thenReturn(Mode.Prod)
            when(configuration.getOptional[String](eqTo("play.editor"))(eqTo(ConfigLoader.stringLoader))).thenReturn(None)
            when(router.get).thenReturn(Router.empty)

            val aesErrorHandler: AesErrorHandler =
              AesErrorHandler(environment, configuration, sourceMapper, router)

            val result: Future[Result] = aesErrorHandler.onServerError(request, null)

            val errorResponseText: String =
              "Fatal error: error handler has failed handling a server error"

            val resultContent: String = Helpers.contentAsString(result)

            Helpers.status(result)      shouldBe StatusValues.INTERNAL_SERVER_ERROR
            Helpers.contentType(result) shouldBe Some(MimeTypes.TEXT)
            resultContent               shouldBe errorResponseText
          }
        }
      }
    }
  }
