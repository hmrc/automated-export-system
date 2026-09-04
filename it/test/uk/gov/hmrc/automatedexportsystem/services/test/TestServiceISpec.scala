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

package uk.gov.hmrc.automatedexportsystem.services.test

import uk.gov.hmrc.automatedexportsystem.config.AppConfig
import uk.gov.hmrc.automatedexportsystem.helpers.BaseISpec
import org.mockito.Mockito.when
import org.mongodb.scala.SingleObservableFuture
import org.scalacheck.Gen
import uk.gov.hmrc.automatedexportsystem.generators.MongoAesIE507MessageGenerator
import uk.gov.hmrc.automatedexportsystem.models.mongo.write.MongoAesIE507Message
import uk.gov.hmrc.automatedexportsystem.repositories.AesIE507Repository
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.concurrent.IntegrationPatience
import org.scalatest.time.{Seconds, Span}

import scala.concurrent.ExecutionContext

class TestServiceISpec extends BaseISpec with MongoAesIE507MessageGenerator with IntegrationPatience:

  given ec:      ExecutionContext = ExecutionContext.global
  val appConfig: AppConfig        = mock[AppConfig]
  when(appConfig.replaceIndexes).thenReturn(true)
  implicit val patience: PatienceConfig = PatienceConfig(timeout = scaled(Span(10, Seconds)))

  extension (mongoAesIE507MessageGen: Gen[MongoAesIE507Message])
    def getMessage: Gen[MongoAesIE507Message] =
      mongoAesIE507MessageGen

  "TestService.deleteAll" - {
    "delete all records and recreate indexes" in {

      val repository = app.injector.instanceOf[AesIE507Repository]
      val service    = new TestService(repository)

      val testMessages = Gen.listOfN(3, arbitrary[MongoAesIE507Message]).sample.get
      testMessages.foreach { msg =>
        repository.submit(msg).value.futureValue shouldBe a[Right[_, _]]
      }

      repository.collection.countDocuments().toFuture().futureValue shouldBe 3L
      service.deleteAll.value.futureValue                           shouldBe Right(())

      repository.collection.countDocuments().toFuture().futureValue shouldBe 0L

      val indexInfo = repository.ensureIndexes().futureValue
      indexInfo should not be empty
    }
  }
