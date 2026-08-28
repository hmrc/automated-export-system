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
import org.apache.pekko.util.ByteString
import org.scalatest.EitherValues
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers
import play.api.mvc.Results.Status
import play.api.mvc.{AnyContent, AnyContentAsEmpty, Request, Result}
import play.api.test.{DefaultAwaitTimeout, FakeRequest, Helpers}
import uk.gov.hmrc.automatedexportsystem.controllers.actions.request.ValidatedXmlRequest
import uk.gov.hmrc.automatedexportsystem.models.IE507.EoriNumber

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.Elem

class AesIE507ActionRefinerSpec extends AnyFreeSpecLike, Matchers, EitherValues, DefaultAwaitTimeout:
  given ec: ExecutionContext = ExecutionContext.global

  val aesIE507ActionRefiner: AesIE507ActionRefiner = AesIE507ActionRefiner()

  object TestData:
    val successfulBlock: Request[AnyContent] => Future[Result] =
      _ => Future.successful(Status(Helpers.NO_CONTENT))

    val eoriNumber: EoriNumber = EoriNumber("eoriNumber")

    val aesIE507MessageValidXml: Elem =
      <Message>
        <ExportOperation>
          <type>1</type>
          <MRN>mrn</MRN>
          <discrepanciesExist>1</discrepanciesExist>
          <splitIndicator>1</splitIndicator>
        </ExportOperation>
        <CustomsOfficeOfExitActual>
          <referenceNumber>referenceNumber</referenceNumber>
        </CustomsOfficeOfExitActual>
        <GoodsShipment>
          <Consignment>
            <referenceNumberUCR>referenceNumberUcr</referenceNumberUCR>
            <LocationOfGoods>
              <typeOfLocation>typeOfLocation</typeOfLocation>
              <qualifierOfIdentification>qualifierOfIdentification</qualifierOfIdentification>
            </LocationOfGoods>
          </Consignment>
        </GoodsShipment>
      </Message>
    end aesIE507MessageValidXml

    val aesIE507MessageInvalidXml: Elem =
      <Message>
        <ExportOperation>
          <MRN>mrn</MRN>
          <discrepanciesExist>3</discrepanciesExist>
          <splitIndicator>1</splitIndicator>
        </ExportOperation>
        <GoodsShipment>
          <Consignment>
            <referenceNumberUCR>referenceNumberUcr</referenceNumberUCR>
            <LocationOfGoods>
            </LocationOfGoods>
          </Consignment>
        </GoodsShipment>
      </Message>

  "AesIE507ActionRefiner" - {

    ".invokeBlock" - {

      "should return a Result" - {

        "when xml deserialization succeeds" in {
          val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(Helpers.GET, "/dummy/path")

          val validatedXmlRequest: ValidatedXmlRequest[AnyContent] =
            ValidatedXmlRequest(TestData.aesIE507MessageValidXml, request, TestData.eoriNumber)

          val result: Future[Result] =
            aesIE507ActionRefiner.invokeBlock(validatedXmlRequest, TestData.successfulBlock)

          Helpers.status(result)         shouldBe Helpers.NO_CONTENT
          Helpers.contentAsBytes(result) shouldBe ByteString.empty
        }

        "when xml deserialization fails" in {
          val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(Helpers.GET, "/dummy/path")

          val validatedXmlRequest: ValidatedXmlRequest[AnyContent] =
            ValidatedXmlRequest(TestData.aesIE507MessageInvalidXml, request, TestData.eoriNumber)

          val result: Future[Result] =
            aesIE507ActionRefiner.invokeBlock(validatedXmlRequest, TestData.successfulBlock)

          val xmlFailedReadErrorResponseXml: Elem =
            <errorResponse>
              <status>422</status>
              <code>UNPROCESSABLE_ENTITY</code>
              <message>XML failed deserialization</message>
              <errors>
                <error>
                  <path>/ExportOperation/type</path>
                  <message>Element is missing</message>
                </error>
                <error>
                  <path>/ExportOperation/discrepanciesExist</path>
                  <message>Failed to parse '3' to Boolean</message>
                </error>
                <error>
                  <path>/CustomsOfficeOfExitActual</path>
                  <message>Element is missing</message>
                </error>
                <error>
                  <path>/GoodsShipment/Consignment/LocationOfGoods/typeOfLocation</path>
                  <message>Element is missing</message>
                </error>
                <error>
                  <path>/GoodsShipment/Consignment/LocationOfGoods/qualifierOfIdentification</path>
                  <message>Element is missing</message>
                </error>
              </errors>
            </errorResponse>

          val resultContent: String = Helpers.contentAsString(result)
          val resultXml:     Elem   = XmlOps.loadXmlFromString(resultContent).value

          Helpers.status(result)      shouldBe Helpers.UNPROCESSABLE_ENTITY
          Helpers.contentType(result) shouldBe Some(Helpers.XML)
          XmlOps.normalize(resultXml) shouldBe XmlOps.normalize(xmlFailedReadErrorResponseXml)
        }
      }
    }
  }
