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

        "when the request contains a valid AES IE507 XML body with all optional elements" in {
          val requestXml: Elem = XmlOps.loadXmlFromPath("/testdata/aesIE507RequestValid.xml").value

          val request: FakeRequest[NodeSeq] = FakeRequest(HttpVerbs.POST, "/automated-export-system/message")
            .withBody(requestXml)

          val result: Future[Result] = Helpers.route(app, request).value

          Helpers.status(result)         shouldBe StatusValues.ACCEPTED
          Helpers.contentType(result)    shouldBe None
          Helpers.contentAsBytes(result) shouldBe ByteString.empty
        }

        "when the request contains an valid AES IE507 XML body without optional elements" in {
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

        "when the request contains a valid XML body that doesn't pass AES IE507 request schema validation" - {

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
                    <line>4</line>
                    <column>26</column>
                    <message>cvc-complex-type.2.4.a: Invalid content was found starting with element 'ExportOperation'. One of '{{status}}' is expected.</message>
                  </error>
                  <error>
                    <line>6</line>
                    <column>33</column>
                    <message>cvc-complex-type.2.4.a: Invalid content was found starting with element 'discrepanciesExist'. One of '{{MRN}}' is expected.</message>
                  </error>
                  <error>
                    <line>15</line>
                    <column>34</column>
                    <message>cvc-complex-type.2.4.a: Invalid content was found starting with element 'LocationOfGoods'. One of '{{referenceNumberUCR}}' is expected.</message>
                  </error>
                  <error>
                    <line>17</line>
                    <column>35</column>
                    <message>cvc-complex-type.2.4.b: The content of element 'LocationOfGoods' is not complete. One of '{{qualifierOfIdentification}}' is expected.</message>
                  </error>
                  <error>
                    <line>33</line>
                    <column>34</column>
                    <message>cvc-complex-type.2.4.a: Invalid content was found starting with element 'netMass'. One of '{{grossMass}}' is expected.</message>
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
                    <line>3</line>
                    <column>24</column>
                    <message>cvc-pattern-valid: Value '' is not facet-valid with respect to pattern '.{{1,35}}' for type 'UK_AlphaNumeric35Type'.</message>
                  </error>
                  <error>
                    <line>3</line>
                    <column>24</column>
                    <message>cvc-type.3.1.3: The value '' of element 'submissionId' is not valid.</message>
                  </error>
                  <error>
                    <line>4</line>
                    <column>18</column>
                    <message>cvc-pattern-valid: Value '' is not facet-valid with respect to pattern '.{{1,35}}' for type 'UK_AlphaNumeric35Type'.</message>
                  </error>
                  <error>
                    <line>4</line>
                    <column>18</column>
                    <message>cvc-type.3.1.3: The value '' of element 'status' is not valid.</message>
                  </error>
                  <error>
                    <line>6</line>
                    <column>20</column>
                    <message>cvc-pattern-valid: Value '' is not facet-valid with respect to pattern '[1-3]{{1}}' for type 'UK_OneToThreeType'.</message>
                  </error>
                  <error>
                    <line>6</line>
                    <column>20</column>
                    <message>cvc-type.3.1.3: The value '' of element 'type' is not valid.</message>
                  </error>
                  <error>
                    <line>7</line>
                    <column>19</column>
                    <message>cvc-pattern-valid: Value '' is not facet-valid with respect to pattern '([2][4-9]|[3-9][0-9])[A-Z]{{2}}[A-Z0-9]{{12}}[A-E][0-9]' for type 'UK_MRNType'.</message>
                  </error>
                  <error>
                    <line>7</line>
                    <column>19</column>
                    <message>cvc-type.3.1.3: The value '' of element 'MRN' is not valid.</message>
                  </error>
                  <error>
                    <line>8</line>
                    <column>34</column>
                    <message>cvc-enumeration-valid: Value '' is not facet-valid with respect to enumeration '[0, 1]'. It must be a value from the enumeration.</message>
                  </error>
                  <error>
                    <line>8</line>
                    <column>34</column>
                    <message>cvc-type.3.1.3: The value '' of element 'discrepanciesExist' is not valid.</message>
                  </error>
                  <error>
                    <line>9</line>
                    <column>30</column>
                    <message>cvc-enumeration-valid: Value '' is not facet-valid with respect to enumeration '[0, 1]'. It must be a value from the enumeration.</message>
                  </error>
                  <error>
                    <line>9</line>
                    <column>30</column>
                    <message>cvc-type.3.1.3: The value '' of element 'splitIndicator' is not valid.</message>
                  </error>
                  <error>
                    <line>12</line>
                    <column>31</column>
                    <message>cvc-pattern-valid: Value '' is not facet-valid with respect to pattern '[A-Z]{{2}}[A-Z0-9]{{6}}' for type 'UK_ReferenceNumberType'.</message>
                  </error>
                  <error>
                    <line>12</line>
                    <column>31</column>
                    <message>cvc-type.3.1.3: The value '' of element 'referenceNumber' is not valid.</message>
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
