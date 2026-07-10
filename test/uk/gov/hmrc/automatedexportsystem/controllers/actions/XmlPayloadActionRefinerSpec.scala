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

import helpers.XmlOps
import org.scalatest.EitherValues
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers
import play.api.http.{HttpVerbs, MimeTypes, Status as StatusValues}
import play.api.mvc.*
import play.api.mvc.Results.Status
import play.api.test.{DefaultAwaitTimeout, FakeRequest, Helpers}

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.{Elem, NodeSeq}

class XmlPayloadActionRefinerSpec extends AnyFreeSpecLike, Matchers, EitherValues, DefaultAwaitTimeout:
  given ec: ExecutionContext = ExecutionContext.global

  val xmlPayloadActionRefiner: XmlPayloadActionRefiner = XmlPayloadActionRefiner()

  "XmlPayloadActionRefiner" - {

    ".invokeBlock" - {

      "should return a Result" - {
        val successfulBlockNodeSeq: Request[NodeSeq] => Future[Result] = _ =>
          Future.successful(
            Status(StatusValues.OK)
          )

        val successfulBlockAnyContent: Request[AnyContent] => Future[Result] = _ =>
          Future.successful(
            Status(StatusValues.OK)
          )

        "when the body of the request is XML (NodeSeq)" in {
          val xml: Elem =
            <element>I'm XML</element>

          val request: FakeRequest[NodeSeq] = FakeRequest
            .apply(
              HttpVerbs.GET,
              "/dummy/path"
            )
            .withBody(xml)

          val result: Future[Result] = xmlPayloadActionRefiner.invokeBlock(request, successfulBlockNodeSeq)

          Helpers.status(result) shouldBe StatusValues.OK
        }

        "when the body of the request is XML (AnyContent)" in {
          val xml: Elem =
            <element>I'm XML</element>

          val request: FakeRequest[AnyContentAsXml] = FakeRequest
            .apply(
              HttpVerbs.GET,
              "/dummy/path"
            )
            .withXmlBody(xml)

          val result: Future[Result] = xmlPayloadActionRefiner.invokeBlock(request, successfulBlockAnyContent)

          Helpers.status(result) shouldBe StatusValues.OK
        }

        "when the body of the request is not XML" in {
          val text: String = "<element>I'm XML in disguise</element>"

          val request: FakeRequest[AnyContentAsText] = FakeRequest
            .apply(
              HttpVerbs.GET,
              "/dummy/path"
            )
            .withTextBody(text)

          val result: Future[Result] = xmlPayloadActionRefiner.invokeBlock(request, successfulBlockAnyContent)

          val expectedXmlBodyErrorResponseXml: Elem =
            <errorResponse>
              <status>415</status>
              <code>UNSUPPORTED_MEDIA_TYPE</code>
              <message>The body of the request is not valid XML</message>
            </errorResponse>

          val resultContent: String = Helpers.contentAsString(result)
          val resultXml:     Elem   = XmlOps.loadXml(resultContent).value

          Helpers.status(result)               shouldBe StatusValues.UNSUPPORTED_MEDIA_TYPE
          Helpers.contentType(result)          shouldBe Some(MimeTypes.XML)
          XmlOps.normalize(resultXml).toString shouldBe XmlOps.normalize(expectedXmlBodyErrorResponseXml).toString
        }
      }
    }
  }
