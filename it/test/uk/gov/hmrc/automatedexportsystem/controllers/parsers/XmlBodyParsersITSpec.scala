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

package uk.gov.hmrc.automatedexportsystem.controllers.parsers

import helpers.XmlOps
import org.apache.pekko.stream.Materializer
import org.apache.pekko.util.ByteString
import org.scalatest.EitherValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.{HttpVerbs, MimeTypes, Status as StatusValues}
import play.api.mvc.{AnyContent, Result}
import play.api.test.{DefaultAwaitTimeout, FakeRequest, Helpers}

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.{Elem, NodeSeq}

class XmlBodyParsersITSpec extends AnyFreeSpecLike, Matchers, GuiceOneAppPerSuite, EitherValues, DefaultAwaitTimeout, ScalaFutures:
  given ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  val xmlBodyParsers: XmlBodyParsers = app.injector.instanceOf[XmlBodyParsers]

  given materializer: Materializer = app.materializer

  "XmlBodyParsers" - {

    ".utf8" - {

      "should return a BodyParser" - {

        "that returns an Accumulator that will parse XML" - {

          "when applied to a Request that has the correct Content-Type" in {
            val xml: Elem =
              <xml>valid xml</xml>

            val request: FakeRequest[AnyContent] =
              FakeRequest(HttpVerbs.POST, "/dummy/path")
                .withHeaders("Content-Type" -> s"${MimeTypes.XML}; charset=utf-8")

            val result: NodeSeq =
              xmlBodyParsers.utf8
                .apply(request)
                .run(ByteString.apply(xml.toString, charset = "utf-8"))
                .map(_.value)
                .futureValue

            XmlOps.normalize(result).toString shouldBe XmlOps.normalize(xml).toString
          }
        }

        "that returns an Accumulator that always returns an error Result" - {

          "when applied to a Request that is missing the Content-Type header" in {
            val xml: Elem =
              <xml>valid xml</xml>

            val request: FakeRequest[AnyContent] =
              FakeRequest(HttpVerbs.POST, "/dummy/path")

            val result: Future[Result] = xmlBodyParsers.utf8
              .apply(request)
              .run(ByteString.apply(xml.toString, charset = "utf-8"))
              .map(_.left.value)

            val errorResponseXml: Elem =
              <errorResponse>
                <status>415</status>
                <code>UNSUPPORTED_MEDIA_TYPE</code>
                <message>Request Content-Type header is missing</message>
              </errorResponse>

            val resultContent: String = Helpers.contentAsString(result)
            val resultXml:     Elem   = XmlOps.loadXmlFromString(resultContent).value

            Helpers.status(result)               shouldBe StatusValues.UNSUPPORTED_MEDIA_TYPE
            Helpers.contentType(result)          shouldBe Some(MimeTypes.XML)
            XmlOps.normalize(resultXml).toString shouldBe XmlOps.normalize(errorResponseXml).toString
          }

          "when applied to a Request where the Content-Type charset parameter is not utf-8" in {
            val xml: Elem =
              <xml>valid xml</xml>

            val request: FakeRequest[AnyContent] =
              FakeRequest(HttpVerbs.POST, "/dummy/path")
                .withHeaders("Content-Type" -> s"${MimeTypes.XML}; charset=ascii")

            val result: Future[Result] = xmlBodyParsers.utf8
              .apply(request)
              .run(ByteString.apply(xml.toString, charset = "utf-8"))
              .map(_.left.value)

            val errorResponseXml: Elem =
              <errorResponse>
                <status>415</status>
                <code>UNSUPPORTED_MEDIA_TYPE</code>
                <message>Request Content-Type charset is not UTF-8</message>
              </errorResponse>

            val resultContent: String = Helpers.contentAsString(result)
            val resultXml:     Elem   = XmlOps.loadXmlFromString(resultContent).value

            Helpers.status(result)               shouldBe StatusValues.UNSUPPORTED_MEDIA_TYPE
            Helpers.contentType(result)          shouldBe Some(MimeTypes.XML)
            XmlOps.normalize(resultXml).toString shouldBe XmlOps.normalize(errorResponseXml).toString
          }

          "when applied to a Request where the Content-Type is not xml" in {
            val request: FakeRequest[AnyContent] =
              FakeRequest(HttpVerbs.POST, "/dummy/path")
                .withHeaders("Content-Type" -> s"${MimeTypes.JSON}; charset=utf-8")

            val result: Future[Result] = xmlBodyParsers.utf8.apply(request).run().map(_.left.value)

            val errorResponseXml: Elem =
              <errorResponse>
                <status>415</status>
                <code>UNSUPPORTED_MEDIA_TYPE</code>
                <message>Expecting xml body</message>
              </errorResponse>

            val resultContent: String = Helpers.contentAsString(result)
            val resultXml:     Elem   = XmlOps.loadXmlFromString(resultContent).value

            Helpers.status(result)               shouldBe StatusValues.UNSUPPORTED_MEDIA_TYPE
            Helpers.contentType(result)          shouldBe Some(MimeTypes.XML)
            XmlOps.normalize(resultXml).toString shouldBe XmlOps.normalize(errorResponseXml).toString
          }
        }
      }
    }
  }
