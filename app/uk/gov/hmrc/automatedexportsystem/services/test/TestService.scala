package uk.gov.hmrc.automatedexportsystem.services.test

import cats.data.EitherT
import com.google.inject.Singleton
import uk.gov.hmrc.automatedexportsystem.repositories.AesIE507Repository
import org.mongodb.scala.SingleObservableFuture
import org.mongodb.scala.model.Filters
import uk.gov.hmrc.automatedexportsystem.errors.MongoError
import uk.gov.hmrc.mdc.Mdc

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TestService @Inject() (aesIE507Repo:   AesIE507Repository)(implicit ec: ExecutionContext) :
  def deleteAll: EitherT[Future, MongoError, Unit] = {
    EitherT(
      Mdc.preservingMdc(
        aesIE507Repo.collection.deleteMany(Filters.empty()).toFuture()
      ).flatMap { _ =>
        Mdc.preservingMdc(aesIE507Repo.ensureIndexes()).map(_ => Right(()))
      }.recover { case e => Left(MongoError.UnexpectedError(e)) }
    )
  }
  