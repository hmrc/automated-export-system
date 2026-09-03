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

package uk.gov.hmrc.automatedexportsystem.models.responses

import cats.data.NonEmptyList
import helpers.XmlOps
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers
import uk.gov.hmrc.automatedexportsystem.models.responses.AesErrorResponseXmlValidationError
import uk.gov.hmrc.automatedexportsystem.xml.RootedXmlWriter.toXmlRoot

import scala.xml.*

class AesErrorResponseSpec extends AnyFreeSpecLike, Matchers:
  "AesErrorResponse" - {

    "should be able to be serialized to XML" - {

      "when there are no errors" in {
        val aesErrorResponse: AesErrorResponse =
          AesErrorResponse(400, "BAD_REQUEST", "Request was bad", errors = None)

        val xml: Elem =
          <errorResponse>
            <status>400</status>
            <code>BAD_REQUEST</code>
            <message>Request was bad</message>
          </errorResponse>

        XmlOps.normalize(aesErrorResponse.toXmlRoot) shouldBe XmlOps.normalize(xml)
      }

      "when there are errors" - {

        "AesErrorResponseXmlValidationError" - {

          "one error" in {
            val aesErrorResponse: AesErrorResponse =
              AesErrorResponse(
                400,
                "BAD_REQUEST",
                "Request was bad",
                Some(NonEmptyList.one(AesErrorResponseXmlValidationError(1, 1, "Bad parse error")))
              )

            val xml: Elem =
              <errorResponse>
                <status>400</status>
                <code>BAD_REQUEST</code>
                <message>Request was bad</message>
                <errors>
                  <error>
                    <line>1</line>
                    <column>1</column>
                    <message>Bad parse error</message>
                  </error>
                </errors>
              </errorResponse>

            XmlOps.normalize(aesErrorResponse.toXmlRoot) shouldBe XmlOps.normalize(xml)
          }

          "more than one error" in {
            val aesErrorResponse: AesErrorResponse =
              AesErrorResponse(
                400,
                "BAD_REQUEST",
                "Request was bad",
                Some(
                  NonEmptyList.of(
                    AesErrorResponseXmlValidationError(1, 1, "Bad parse error 1"),
                    AesErrorResponseXmlValidationError(2, 1, "Bad parse error 2"),
                    AesErrorResponseXmlValidationError(3, 1, "Bad parse error 3"),
                    AesErrorResponseXmlValidationError(4, 1, "Bad parse error 4"),
                    AesErrorResponseXmlValidationError(5, 1, "Bad parse error 5")
                  )
                )
              )

            val xml: Elem =
              <errorResponse>
                <status>400</status>
                <code>BAD_REQUEST</code>
                <message>Request was bad</message>
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

            XmlOps.normalize(aesErrorResponse.toXmlRoot) shouldBe XmlOps.normalize(xml)
          }
        }

        "AesErrorResponseXmlReadError" - {

          "one error" in {
            val aesErrorResponse: AesErrorResponse =
              AesErrorResponse(
                422,
                "UNPROCESSABLE_ENTITY",
                "Request was unprocessable",
                Some(NonEmptyList.one(AesErrorResponseXmlReadError("/path", "read error")))
              )

            val xml: Elem =
              <errorResponse>
                <status>422</status>
                <code>UNPROCESSABLE_ENTITY</code>
                <message>Request was unprocessable</message>
                <errors>
                  <error>
                    <path>/path</path>
                    <message>read error</message>
                  </error>
                </errors>
              </errorResponse>

            XmlOps.normalize(aesErrorResponse.toXmlRoot) shouldBe XmlOps.normalize(xml)
          }

          "more than one error" in {
            val aesErrorResponse: AesErrorResponse =
              AesErrorResponse(
                422,
                "UNPROCESSABLE_ENTITY",
                "Request was unprocessable",
                Some(
                  NonEmptyList.of(
                    AesErrorResponseXmlReadError("/path/a", "read error a"),
                    AesErrorResponseXmlReadError("/path/b", "read error b"),
                    AesErrorResponseXmlReadError("/path/c", "read error c"),
                    AesErrorResponseXmlReadError("/path/d", "read error d"),
                    AesErrorResponseXmlReadError("/path/e", "read error e")
                  )
                )
              )

            val xml: Elem =
              <errorResponse>
                <status>422</status>
                <code>UNPROCESSABLE_ENTITY</code>
                <message>Request was unprocessable</message>
                <errors>
                  <error>
                    <path>/path/a</path>
                    <message>read error a</message>
                  </error>
                  <error>
                    <path>/path/b</path>
                    <message>read error b</message>
                  </error>
                  <error>
                    <path>/path/c</path>
                    <message>read error c</message>
                  </error>
                  <error>
                    <path>/path/d</path>
                    <message>read error d</message>
                  </error>
                  <error>
                    <path>/path/e</path>
                    <message>read error e</message>
                  </error>
                </errors>
              </errorResponse>

            XmlOps.normalize(aesErrorResponse.toXmlRoot) shouldBe XmlOps.normalize(xml)
          }
        }
      }
    }
  }
