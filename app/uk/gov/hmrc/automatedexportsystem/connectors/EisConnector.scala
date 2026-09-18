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

package uk.gov.hmrc.automatedexportsystem.connectors

import cats.data.EitherT
import jakarta.inject.Inject
import play.api.Logger
import play.api.libs.ws.WSBodyWritables.writeableOf_NodeSeq
import uk.gov.hmrc.automatedexportsystem.connectors.httpreads.XmlBasedHttpReads
import uk.gov.hmrc.automatedexportsystem.errors.ConnectorError
import uk.gov.hmrc.automatedexportsystem.models.eis.{EisErrorResponse, EisIE507Request}
import uk.gov.hmrc.automatedexportsystem.xml.RootedXmlWriter.toXmlRoot
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, StringContextOps}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.net.URL
import javax.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

@Singleton
class EisConnector @Inject() (
  httpClient:     HttpClientV2,
  servicesConfig: ServicesConfig
)(using protected val ec: ExecutionContext):
  private val logger: Logger = Logger(getClass)

  private lazy val submitXmlBasedHttpReads: XmlBasedHttpReads[EisErrorResponse, Unit] =
    XmlBasedHttpReads[EisErrorResponse, Unit](getClass)

  def submitMessage(
    eisIE507Request: EisIE507Request
  )(using hc: HeaderCarrier): EitherT[Future, ConnectorError, Either[EisErrorResponse, Unit]] =
    val submitUrl: URL = url"${servicesConfig.baseUrl("eis")}/cds/aesIE507Request/v1"

    given httpReads: HttpReads[Either[ConnectorError, Either[EisErrorResponse, Unit]]] =
      submitXmlBasedHttpReads.httpReads

    EitherT(
      httpClient
        .post(submitUrl)
        .setHeader(eisIE507Request.headers.normalizedHeaders*)
        .withBody(eisIE507Request.message.toXmlRoot)
        .execute
        .recover { case NonFatal(t) =>
          logger.error(s"Error encountered on POST request to $submitUrl", t)

          Left(ConnectorError.UnexpectedError("POST", submitUrl.toString, t))
        }
    )
