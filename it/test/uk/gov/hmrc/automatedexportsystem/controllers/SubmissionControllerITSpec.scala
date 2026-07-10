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

import helpers.XmlOps
import org.apache.pekko.util.ByteString
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatest.{EitherValues, OptionValues}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.{HttpVerbs, MimeTypes, Status as StatusValues}
import play.api.mvc.Result
import play.api.test.{DefaultAwaitTimeout, FakeRequest, Helpers}

import scala.concurrent.Future
import scala.xml.{Elem, NodeSeq}

class SubmissionControllerITSpec extends AnyFreeSpecLike, Matchers, GuiceOneAppPerSuite, EitherValues, OptionValues, DefaultAwaitTimeout:
  "SubmissionController" - {
    "should process an incoming POST request to the /message endpoint" - {
      "and return a 202 response" - {
        "when the request contains a IE507 XML body with all optional elements" in {
          val requestXml: Elem = XmlOps.loadXmlFromPath("/testdata/aesIE507RequestValid.xml").value

          val request: FakeRequest[NodeSeq] = FakeRequest(HttpVerbs.POST, "/automated-export-system/message")
            .withBody(requestXml)

          val result: Future[Result] = Helpers.route(app, request).value

          Helpers.status(result)         shouldBe StatusValues.ACCEPTED
          Helpers.contentType(result)    shouldBe None
          Helpers.contentAsBytes(result) shouldBe ByteString.empty
        }

        "when the request contains a IE507 XML body without optional elements" in {
          val requestXml: Elem = XmlOps.loadXmlFromPath("/testdata/aesIE507RequestValidNoOptionals.xml").value

          val request: FakeRequest[NodeSeq] = FakeRequest(HttpVerbs.POST, "/automated-export-system/message")
            .withBody(requestXml)

          val result: Future[Result] = Helpers.route(app, request).value

          Helpers.status(result)         shouldBe StatusValues.ACCEPTED
          Helpers.contentType(result)    shouldBe None
          Helpers.contentAsBytes(result) shouldBe ByteString.empty
        }
      }

      "and return a 400 response" - {
        "when the request contains a valid XML body that doesn't pass IE507 request schema validation" - {
          "due to missing required elements" in {
            val requestXml: Elem = XmlOps.loadXmlFromPath("/testdata/aesIE507RequestInvalidMissingRequired.xml").value

            val request: FakeRequest[NodeSeq] = FakeRequest(HttpVerbs.POST, "/automated-export-system/message")
              .withBody(requestXml)

            val xmlFailedValidationErrorResponseXml: Elem =
              <errorResponse>
                <status>400</status>
                <code>BAD_REQUEST</code>
                <message>XML failed schema validation</message>
                <errors>
                  <error>
                    <line>7</line>
                    <column>14</column>
                    <message>cvc-complex-type.2.4.b: The content of element 'Header' is not complete. One of '{{messageType}}' is expected.</message>
                  </error>
                  <error>
                    <line>13</line>
                    <column>27</column>
                    <message>cvc-complex-type.2.4.b: The content of element 'ExportOperation' is not complete. One of '{{splitIndicator}}' is expected.</message>
                  </error>
                  <error>
                    <line>15</line>
                    <column>37</column>
                    <message>cvc-complex-type.2.4.b: The content of element 'CustomsOfficeOfExitActual' is not complete. One of '{{referenceNumber}}' is expected.</message>
                  </error>
                </errors>
              </errorResponse>

            val result:        Future[Result] = Helpers.route(app, request).value
            val resultContent: String         = Helpers.contentAsString(result)
            val resultXml:     Elem           = XmlOps.loadXmlFromString(resultContent).value

            Helpers.status(result)               shouldBe StatusValues.BAD_REQUEST
            Helpers.contentType(result)          shouldBe Some(MimeTypes.XML)
            XmlOps.normalize(resultXml).toString shouldBe XmlOps.normalize(xmlFailedValidationErrorResponseXml).toString
          }

          "due to elements not matching the required patterns" in {
            val requestXml: Elem = XmlOps.loadXmlFromPath("/testdata/aesIE507RequestInvalidBadPatterns.xml").value

            val request: FakeRequest[NodeSeq] = FakeRequest(HttpVerbs.POST, "/automated-export-system/message")
              .withBody(requestXml)

            val xmlFailedValidationErrorResponseXml: Elem =
              <errorResponse>
                <status>400</status>
                <code>BAD_REQUEST</code>
                <message>XML failed schema validation</message>
                <errors>
                  <error>
                    <line>5</line>
                    <column>68</column>
                    <message>cvc-pattern-valid: Value '2026-01-01' is not facet-valid with respect to pattern '\d{{4}}-\d{{2}}-\d{{2}}T\d{{2}}:\d{{2}}:\d{{2}}' for type 'UK_PreparationDateAndTimeContentType'.</message>
                  </error>
                  <error>
                    <line>5</line>
                    <column>68</column>
                    <message>cvc-type.3.1.3: The value '2026-01-01' of element 'preparationDateAndTime' is not valid.</message>
                  </error>
                  <error>
                    <line>7</line>
                    <column>41</column>
                    <message>cvc-pattern-valid: Value 'CC507' is not facet-valid with respect to pattern 'CC507C' for type 'UK_messageCode507'.</message>
                  </error>
                  <error>
                    <line>7</line>
                    <column>41</column>
                    <message>cvc-type.3.1.3: The value 'CC507' of element 'messageType' is not valid.</message>
                  </error>
                  <error>
                    <line>12</line>
                    <column>40</column>
                    <message>cvc-pattern-valid: Value '45ABABCDEFGH0789' is not facet-valid with respect to pattern '([2][4-9]|[3-9][0-9])[A-Z]{{2}}[A-Z0-9]{{12}}[A-E][0-9]' for type 'UK_MRNType'.</message>
                  </error>
                  <error>
                    <line>12</line>
                    <column>40</column>
                    <message>cvc-type.3.1.3: The value '45ABABCDEFGH0789' of element 'MRN' is not valid.</message>
                  </error>
                  <error>
                    <line>17</line>
                    <column>55</column>
                    <message>cvc-pattern-valid: Value 'EFX0K19' is not facet-valid with respect to pattern '[A-Z]{{2}}[A-Z0-9]{{6}}' for type 'UK_ReferenceNumberType'.</message>
                  </error>
                  <error>
                    <line>17</line>
                    <column>55</column>
                    <message>cvc-type.3.1.3: The value 'EFX0K19' of element 'referenceNumber' is not valid.</message>
                  </error>
                </errors>
              </errorResponse>

            val result:        Future[Result] = Helpers.route(app, request).value
            val resultContent: String         = Helpers.contentAsString(result)
            val resultXml:     Elem           = XmlOps.loadXmlFromString(resultContent).value

            Helpers.status(result)               shouldBe StatusValues.BAD_REQUEST
            Helpers.contentType(result)          shouldBe Some(MimeTypes.XML)
            XmlOps.normalize(resultXml).toString shouldBe XmlOps.normalize(xmlFailedValidationErrorResponseXml).toString
          }
        }
      }
    }
  }
