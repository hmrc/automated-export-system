package uk.gov.hmrc.automatedexportsystem.services.test

import com.google.inject.Singleton
import uk.gov.hmrc.automatedexportsystem.repositories.AesIE507Repository
import org.mongodb.scala.SingleObservableFuture
import org.mongodb.scala.model.Filters

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TestService @Inject() (aesIE507Repo:   AesIE507Repository)(implicit ec: ExecutionContext) :
  def deleteAll: Future[Unit] = {
    aesIE507Repo.collection.deleteMany(Filters.empty()).toFuture().flatMap { _ =>
      aesIE507Repo.ensureIndexes().map(_ => ())
    }
  }