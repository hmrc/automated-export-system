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

import org.mockito.Mockito.when
import org.mongodb.scala.SingleObservableFuture
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.time.{Seconds, Span}
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.mvc.ControllerComponents
import play.api.test.FakeRequest
import uk.gov.hmrc.automatedexportsystem.config.AppConfig
import uk.gov.hmrc.automatedexportsystem.controllers.test.TestController
import uk.gov.hmrc.automatedexportsystem.generators.MongoAesIE507MessageGenerator
import uk.gov.hmrc.automatedexportsystem.helpers.BaseISpec
import uk.gov.hmrc.automatedexportsystem.models.mongo.write.MongoAesIE507Message
import uk.gov.hmrc.automatedexportsystem.repositories.AesIE507RepositoryImpl
import uk.gov.hmrc.automatedexportsystem.services.test.TestService

import scala.concurrent.ExecutionContext

class TestControllerISpec extends BaseISpec, MongoAesIE507MessageGenerator:
  val repository: AesIE507RepositoryImpl = app.injector.instanceOf[AesIE507RepositoryImpl]

  override def beforeEach(): Unit =
    super.beforeEach()
    await(repository.collection.drop().head())

  trait Setup:
    val appConfig: AppConfig = mock[AppConfig]
    when(appConfig.replaceIndexes).thenReturn(true)

    val service    = new TestService(repository)
    val controller = new TestController(app.injector.instanceOf[ControllerComponents], service)

  given ec: ExecutionContext = ExecutionContext.global

  implicit val patience: PatienceConfig = PatienceConfig(timeout = scaled(Span(10, Seconds)))

  "TestController.deleteAll" - {
    "should return NoContent when deletion is successful" in new Setup {

      val testMessages = Gen.listOfN(3, arbitrary[MongoAesIE507Message]).sample.get
      testMessages.foreach { msg =>
        repository.submit(msg).value.futureValue shouldBe Right(true)
      }

      repository.collection.countDocuments().toFuture().futureValue shouldBe 3L

      val result = controller.deleteAll()(FakeRequest())

      status(result)                                                shouldBe NO_CONTENT
      repository.collection.countDocuments().toFuture().futureValue shouldBe 0L
    }

  }
