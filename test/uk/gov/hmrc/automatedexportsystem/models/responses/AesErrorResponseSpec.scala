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
import uk.gov.hmrc.automatedexportsystem.models.responses.AesErrorResponse.AesErrorResponseValidationError

import scala.xml.*

class AesErrorResponseSpec extends AnyFreeSpecLike, Matchers:
  "AesErrorResponse" - {
    ".toXml" - {
      "should render the response into an XML document" - {
        "when there are no validation errors" in {
          val aesErrorResponse: AesErrorResponse =
            AesErrorResponse(400, "BAD_REQUEST", "Request was bad", None)

          val normalizedResponseXmlString: String = XmlOps.normalize(aesErrorResponse.toXml).toString

          val xml: Elem =
            <errorResponse>
              <status>400</status>
              <code>BAD_REQUEST</code>
              <message>Request was bad</message>
            </errorResponse>

          val normalizedXmlString: String = XmlOps.normalize(xml).toString

          normalizedResponseXmlString shouldBe normalizedXmlString
        }

        "when there are validation errors" - {
          "one validation error" in {
            val aesErrorResponse: AesErrorResponse =
              AesErrorResponse(
                400,
                "BAD_REQUEST",
                "Request was bad",
                Some(NonEmptyList.one(AesErrorResponseValidationError(1, 1, "Bad parse error")))
              )

            val normalizedResponseXmlString: String = XmlOps.normalize(aesErrorResponse.toXml).toString

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

            val normalizedXmlString: String = XmlOps.normalize(xml).toString

            normalizedResponseXmlString shouldBe normalizedXmlString
          }

          "more than one validation error" in {
            val aesErrorResponse: AesErrorResponse =
              AesErrorResponse(
                400,
                "BAD_REQUEST",
                "Request was bad",
                Some(
                  NonEmptyList.of(
                    AesErrorResponseValidationError(1, 1, "Bad parse error 1"),
                    AesErrorResponseValidationError(2, 1, "Bad parse error 2"),
                    AesErrorResponseValidationError(3, 1, "Bad parse error 3"),
                    AesErrorResponseValidationError(4, 1, "Bad parse error 4"),
                    AesErrorResponseValidationError(5, 1, "Bad parse error 5")
                  )
                )
              )

            val normalizedResponseXmlString: String = XmlOps.normalize(aesErrorResponse.toXml).toString

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

            val normalizedXmlString: String = XmlOps.normalize(xml).toString

            normalizedResponseXmlString shouldBe normalizedXmlString
          }
        }
      }
    }
  }
