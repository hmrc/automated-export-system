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
import org.scalatest.*
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.*
import uk.gov.hmrc.http.test.WireMockSupport

trait BaseISpec
    extends AnyFreeSpecLike
    with Matchers
    with GuiceOneAppPerSuite
    with DefaultAwaitTimeout
    with FutureAwaits
    with ScalaFutures
    with OptionValues
    with EitherValues
    with MockitoSugar
    with WireMockSupport:
  lazy val guiceApplicationBuilder: GuiceApplicationBuilder =
    GuiceApplicationBuilder()
      .configure(
        "microservice.services.auth.host" -> "localhost",
        "microservice.services.auth.port" -> wireMockPort,
        "metrics.enabled"                 -> "false"
      )

  override lazy val app: Application =
    guiceApplicationBuilder.build()

  override def beforeEach(): Unit = {
    super.beforeEach()
    WireMock.reset()
  }
