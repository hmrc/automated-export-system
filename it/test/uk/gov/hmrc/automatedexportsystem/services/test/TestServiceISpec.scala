package uk.gov.hmrc.automatedexportsystem.services.test

import org.scalatestplus.mockito.MockitoSugar.mock
import uk.gov.hmrc.automatedexportsystem.config.AppConfig
import uk.gov.hmrc.automatedexportsystem.helpers.BaseISpec
import org.mockito.Mockito.when
import org.mongodb.scala.SingleObservableFuture
import org.scalacheck.Gen
import uk.gov.hmrc.automatedexportsystem.generators.MongoAesIE507MessageGenerator
import uk.gov.hmrc.automatedexportsystem.models.mongo.write.MongoAesIE507Message
import uk.gov.hmrc.automatedexportsystem.repositories.AesIE507Repository
import org.scalacheck.Arbitrary.arbitrary
import scala.concurrent.ExecutionContext

class TestServiceISpec extends BaseISpec with MongoAesIE507MessageGenerator:

  given ec: ExecutionContext = ExecutionContext.global
  val appConfig: AppConfig = mock[AppConfig]
  when(appConfig.replaceIndexes).thenReturn(true)

  extension (mongoAesIE507MessageGen: Gen[MongoAesIE507Message])
    def getMessage: Gen[MongoAesIE507Message] =
      mongoAesIE507MessageGen

  "TestService.deleteAll" - {
    "delete all records and recreate indexes" in {

      val repository = app.injector.instanceOf[AesIE507Repository]
      val service = new TestService(repository)

      val testMessages = Gen.listOfN(3, arbitrary[MongoAesIE507Message]).sample.get
      testMessages.foreach { msg =>
        repository.submit(msg).value.futureValue shouldBe Right(true)
      }


      repository.collection.countDocuments().toFuture().futureValue shouldBe 3L
      service.deleteAll.futureValue

      repository.collection.countDocuments().toFuture().futureValue shouldBe 0L

      val indexInfo = repository.ensureIndexes().futureValue
      indexInfo should not be empty
    }
  }
