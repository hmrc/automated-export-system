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

import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.StringContextOps
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CodeListConnector @Inject() (
  http:   HttpClientV2,
  config: ServicesConfig
)(using ec: ExecutionContext) {

  private val baseUrl: String =
    config.baseUrl("automated-export-system")

  private def get(url: String)(using hc: HeaderCarrier): Future[String] =
    http
      .get(url"$url")
      .execute[HttpResponse]
      .map(_.body)

  // endpoints
  def getMessageTypes()(using hc: HeaderCarrier): Future[String] =
    get(s"$baseUrl/codelists/messagetype")

  def getTypeOfLocations()(using hc: HeaderCarrier): Future[String] =
    get(s"$baseUrl/codelists/typeoflocation")

  def getNationalities()(using hc: HeaderCarrier): Future[String] =
    get(s"$baseUrl/codelists/nationality")

  def getTransportModes()(using hc: HeaderCarrier): Future[String] =
    get(s"$baseUrl/codelists/transportmode")

  def getCustomsOfficeExits()(using hc: HeaderCarrier): Future[String] =
    get(s"$baseUrl/codelists/customsofficeexit")

}
