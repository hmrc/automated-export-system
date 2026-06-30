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

package uk.gov.hmrc.automatedexportsystem.config

import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provides}
import play.api.{Configuration, Environment}
import play.api.libs.concurrent.PekkoGuiceSupport
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.Clock
import javax.inject.Singleton
import scala.annotation.unused

class GuiceModule(@unused environment: Environment, configuration: Configuration) extends AbstractModule with PekkoGuiceSupport {
  override def configure(): Unit = {}

  @Provides
  @Named("eisBearerToken")
  @Singleton
  def eisBearerTokenProvider(servicesConfig: ServicesConfig): String =
    servicesConfig.getString("microservice.services.eis.bearer-token")

  @Provides
  @Named("eisUrl")
  @Singleton
  def eisUrlProvider(servicesConfig: ServicesConfig): String =
    servicesConfig.baseUrl("eis")

  @Provides
  @Named("eisEnvironment")
  @Singleton
  def eisEnvironmentProvider(servicesConfig: ServicesConfig): String =
    servicesConfig.getString("microservice.services.eis.environment")

  @Provides
  @Singleton
  def clockProvider(): Clock =
    Clock.systemUTC()

}
