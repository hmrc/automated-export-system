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

package test.uk.gov.hmrc.automatedexportsystem.helpers

import com.github.tomakehurst.wiremock.client.WireMock
import org.scalatest.concurrent.{Eventually, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatest.*
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.http.{Status, *}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Results
import play.api.test.*
import uk.gov.hmrc.http.test.WireMockSupport

trait BaseISpec
    extends AnyFreeSpecLike
    with BeforeAndAfterAll
    with GuiceOneAppPerSuite
    with Matchers
    with Inspectors
    with ScalaFutures
    with DefaultAwaitTimeout
    with Writeables
    with FutureAwaits
    with EssentialActionCaller
    with RouteInvokers
    with LoneElement
    with Inside
    with OptionValues
    with Results
    with Status
    with HeaderNames
    with MimeTypes
    with HttpProtocol
    with HttpVerbs
    with ResultExtractors
    with WireMockSupport
    with Eventually {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      "microservice.services.auth.host" -> "localhost",
      "microservice.services.auth.port" -> wireMockPort,
      "metrics.enabled"                 -> "false"
    )
    .build()
  override def beforeEach(): Unit = {
    super.beforeEach()
    WireMock.reset()
  }
}
