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

import cats.syntax.either.*
import play.api.Logger
import uk.gov.hmrc.automatedexportsystem.errors.ConnectorError
import uk.gov.hmrc.automatedexportsystem.xml.XmlReader
import uk.gov.hmrc.automatedexportsystem.xml.XmlReader.as
import uk.gov.hmrc.http.{HttpErrorFunctions, HttpReads}

import java.io.StringReader
import scala.util.Using
import scala.xml.{NodeSeq, XML}

class XmlBasedHttpReads[E, S](connectorClass: Class[_]):
  lazy val logger: Logger = Logger(getClass)

  def httpReads(using
    errorReader:   XmlReader[E],
    successReader: XmlReader[S]
  ): HttpReads[Either[ConnectorError, Either[E, S]]] =
    (method, url, response) =>
      val status: Int = response.status

      loadXmlString(response.body)
        .leftMap(t => responseBodyNotXmlError(method, url, t))
        .flatMap(xml =>
          if HttpErrorFunctions.is2xx(status) then
            readXmlOrError[S](xml, method, url)
              .map(Right.apply)
          else if HttpErrorFunctions.is4xx(status) || HttpErrorFunctions.is5xx(status) then
            readXmlOrError[E](xml, method, url)
              .map(Left.apply)
          else Left(unexpectedStatusError(method, url, status))
        )

  private def logConnectorError(
    method:    String,
    url:       String,
    details:   String,
    exception: Option[Throwable]
  ): Unit =
    val message: String = s"$method request to $url error in $connectorClass: $details"

    exception.fold(logger.error(message))(logger.error(message, _))

  private def loadXmlString(xml: String): Either[Throwable, NodeSeq] =
    if xml.trim.isEmpty then Right(NodeSeq.Empty)
    else Using(StringReader(xml))(XML.load).toEither

  private def readXmlOrError[T: XmlReader](
    xml:    NodeSeq,
    method: String,
    url:    String
  ): Either[ConnectorError, T] =
    xml
      .as[T]
      .leftMap(nel =>
        logConnectorError(method, url, "response body XML could not be deserialized", exception = None)

        ConnectorError.ResponseBodyXmlReadError(method, url, nel)
      )
      .toEither

  private def responseBodyNotXmlError(
    method:    String,
    url:       String,
    exception: Throwable
  ): ConnectorError =
    logConnectorError(method, url, "response body is not valid XML", Some(exception))

    ConnectorError.ResponseBodyNotXmlError(method, url, Some(exception))

  private def unexpectedStatusError(
    method: String,
    url:    String,
    status: Int
  ): ConnectorError =
    logConnectorError(method, url, s"response status code unexpected: $status", exception = None)

    ConnectorError.UnexpectedStatusError(method, url, status)
