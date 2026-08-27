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

package uk.gov.hmrc.automatedexportsystem.connectors.httpreads

import cats.data.NonEmptyList
import org.scalatest.EitherValues
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers
import uk.gov.hmrc.automatedexportsystem.errors.{ConnectorError, ResponseCode, XmlReaderError}
import uk.gov.hmrc.automatedexportsystem.xml.{XmlPath, XmlReader}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

class XmlBasedHttpReadsSpec extends AnyFreeSpecLike, Matchers, EitherValues:
  object TestData:
    case class SuccessModel(a: String)

    object SuccessModel:
      given successModelXmlReader: XmlReader[SuccessModel] =
        XmlReader.nonEmptyReader { (xml, path) =>
          (XmlPath \ "a")
            .read[String](xml, path)
            .map(SuccessModel.apply)
        }

    case class ErrorModel(b: String)

    object ErrorModel:
      given errorModelXmlReader: XmlReader[ErrorModel] =
        XmlReader.nonEmptyReader { (xml, path) =>
          (XmlPath \ "b")
            .read[String](xml, path)
            .map(ErrorModel.apply)
        }
  end TestData

  val xmlBasedHttpReads: XmlBasedHttpReads[TestData.ErrorModel, TestData.SuccessModel] =
    XmlBasedHttpReads(getClass)

  "XmLBasedHttpReads" - {

    "should be able to read a successful status http response body" in {
      val httpReads: HttpReads[Either[ConnectorError, Either[TestData.ErrorModel, TestData.SuccessModel]]] =
        xmlBasedHttpReads.httpReads

      val responseBody: String = "<xml><a>success</a></xml>"

      val httpResponse: HttpResponse = HttpResponse(200, responseBody)

      val result: TestData.SuccessModel =
        httpReads.read("POST", "/dummy/path", httpResponse).value.value

      result shouldBe TestData.SuccessModel("success")
    }

    "should be able to read an error status http response body" in {
      val httpReads: HttpReads[Either[ConnectorError, Either[TestData.ErrorModel, TestData.SuccessModel]]] =
        xmlBasedHttpReads.httpReads

      val responseBody: String = "<xml><b>failure</b></xml>"

      val httpResponse: HttpResponse = HttpResponse(500, responseBody)

      val result: TestData.ErrorModel =
        httpReads.read("POST", "/dummy/path", httpResponse).value.left.value

      result shouldBe TestData.ErrorModel("failure")
    }

    "should return a connector error" - {

      "when the http response body is not valid XML" in {
        val httpReads: HttpReads[Either[ConnectorError, Either[TestData.ErrorModel, TestData.SuccessModel]]] =
          xmlBasedHttpReads.httpReads

        val responseBody: String = "not valid xml"

        val httpResponse: HttpResponse = HttpResponse(123, responseBody)

        val result: ConnectorError =
          httpReads.read("GET", "/dummy/path", httpResponse).left.value

        result.message      shouldBe "Error on GET request to /dummy/path. Response body was not valid XML"
        result.responseCode shouldBe ResponseCode.BadGateway
        result.exception.foreach(_.getMessage shouldBe "Content is not allowed in prolog.")
      }

      "when the http response body failed to be deserialized" - {

        "when the response status is successful" in {
          val httpReads: HttpReads[Either[ConnectorError, Either[TestData.ErrorModel, TestData.SuccessModel]]] =
            xmlBasedHttpReads.httpReads

          val responseBody: String = "<xml></xml>"

          val httpResponse: HttpResponse = HttpResponse(222, responseBody)

          val result: ConnectorError =
            httpReads.read("GET", "/dummy/path", httpResponse).left.value

          result shouldBe ConnectorError.ResponseBodyXmlReadError(
            "GET",
            "/dummy/path",
            NonEmptyList.one(
              XmlReaderError.Missing("/a")
            )
          )
        }

        "when the http response status is unsuccessful" in {
          val httpReads: HttpReads[Either[ConnectorError, Either[TestData.ErrorModel, TestData.SuccessModel]]] =
            xmlBasedHttpReads.httpReads

          val responseBody: String = ""

          val httpResponse: HttpResponse = HttpResponse(433, responseBody)

          val result: ConnectorError =
            httpReads.read("PUT", "/dummy/path", httpResponse).left.value

          result shouldBe ConnectorError.ResponseBodyXmlReadError(
            "PUT",
            "/dummy/path",
            NonEmptyList.one(
              XmlReaderError.Missing("/")
            )
          )
        }
      }

      "when the http response status is unexpected" in {
        val httpReads: HttpReads[Either[ConnectorError, Either[TestData.ErrorModel, TestData.SuccessModel]]] =
          xmlBasedHttpReads.httpReads

        val responseBody: String = "<xml></xml>"

        val httpResponse: HttpResponse = HttpResponse(321, responseBody)

        val result: ConnectorError =
          httpReads.read("GET", "/dummy/path", httpResponse).left.value

        result shouldBe ConnectorError.UnexpectedStatusError("GET", "/dummy/path", 321)
      }
    }
  }
