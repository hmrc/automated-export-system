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

import org.mongodb.scala.bson.BsonDocument
import org.scalatest.{BeforeAndAfterAll, TestSuite}
import org.scalatestplus.play.BaseOneAppPerSuite
import play.api.inject.ApplicationLifecycle
import uk.gov.hmrc.automatedexportsystem.repositories.AutomatedExportSystemRepository
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import org.mongodb.scala.SingleObservableFuture
import uk.gov.hmrc.automatedexportsystem.models.dbDocument.AutomatedExportSystemDocument

import scala.concurrent.duration.Duration.Inf
import scala.concurrent.{Await, Future}

trait CleanMongo extends BeforeAndAfterAll { this: TestSuite & BaseOneAppPerSuite =>

  override protected def beforeAll(): Unit = {
    super.beforeAll()

    val repository: PlayMongoRepository[AutomatedExportSystemDocument] =
      app.injector.instanceOf[AutomatedExportSystemRepository]
    val mongoComponent = app.injector.instanceOf[MongoComponent]

    Await.ready(
      mongoComponent.database
        .getCollection(repository.collectionName)
        .deleteMany(BsonDocument())
        .toFuture(),
      Inf
    )

    app.injector.instanceOf[ApplicationLifecycle].addStopHook { () =>
      mongoComponent.client.close()
      Future.unit
    }
  }
}
