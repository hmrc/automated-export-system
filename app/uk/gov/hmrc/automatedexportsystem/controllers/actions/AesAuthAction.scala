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

package uk.gov.hmrc.automatedexportsystem.controllers.actions
import org.apache.pekko.stream.Materializer
import play.api.Logging
import play.api.libs.streams.Accumulator
import play.api.mvc.*
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions, Enrolment, Enrolments}
import uk.gov.hmrc.automatedexportsystem.controllers.actions.request.AesAuthAttr
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendHeaderCarrierProvider

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import scala.xml.NodeSeq

class AesAuthAction @Inject() (
  val authConnector: AuthConnector
)(implicit ec: ExecutionContext, materializer: Materializer)
    extends AuthorisedFunctions
    with BackendHeaderCarrierProvider
    with Results
    with Logging {

  private object AuthConstants {
    val AuthorizationHeader = "Authorization"
    val EnrolmentKey        = "HMRC-CUS-ORG"
    val EoriIdentifierKey   = "EORINumber"
  }

  def apply(next: Action[NodeSeq]): EssentialAction =
    EssentialAction { requestHeader =>
      if (!hasAuthorizationHeader(requestHeader)) {
        Accumulator.done(unauthorisedResult)
      } else {
        implicit val headerCarrier: HeaderCarrier = hc(requestHeader)

        Accumulator.flatten {
          authoriseAndExtractEori(requestHeader).map {
            case Right(eori) =>
              next(requestHeader.addAttr(AesAuthAttr.Eori, eori))
            case Left(result) =>
              Accumulator.done(result)
          }
        }
      }
    }

  private def hasAuthorizationHeader(requestHeader: RequestHeader): Boolean =
    requestHeader.headers
      .get(AuthConstants.AuthorizationHeader)
      .exists(_.trim.nonEmpty)

  private def authoriseAndExtractEori(
    requestHeader: RequestHeader
  )(implicit hc: HeaderCarrier): Future[Either[Result, String]] =
    authorised(Enrolment(AuthConstants.EnrolmentKey))
      .retrieve(Retrievals.allEnrolments) { enrolments =>
        Future.successful {
          extractEori(enrolments) match {
            case Some(eori) => Right(eori)
            case None       =>
              logger.warn(s"EORI missing for authorised request [path=${requestHeader.path}]")
              Left(unauthorisedResult)
          }
        }
      }
      .recover { case NonFatal(e) =>
        logger.warn(
          s"Authorisation failed [path=${requestHeader.path}, message=${e.getMessage}]",
          e
        )
        Left(unauthorisedResult)
      }

  private def extractEori(enrolments: Enrolments): Option[String] =
    enrolments
      .getEnrolment(AuthConstants.EnrolmentKey)
      .flatMap(_.getIdentifier(AuthConstants.EoriIdentifierKey))
      .map(_.value)

  private def unauthorisedResult: Result = Unauthorized
}
