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
import play.api.libs.streams.Accumulator
import play.api.mvc.*
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions, Enrolment}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendHeaderCarrierProvider

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class AesAuthAction @Inject() (
  val authConnector: AuthConnector
)(implicit ec: ExecutionContext, materializer: Materializer)
    extends AuthorisedFunctions
    with BackendHeaderCarrierProvider
    with Results {

  def apply(next: Action[scala.xml.NodeSeq]): EssentialAction =
    EssentialAction { requestHeader =>
      val hasAuthHeader = requestHeader.headers.get("Authorization").exists(_.trim.nonEmpty)

      if (!hasAuthHeader) {
        Accumulator.done(Unauthorized)
      } else {
        implicit val headerCarrier: HeaderCarrier = hc(requestHeader)

        Accumulator.flatten {
          authorised(Enrolment("HMRC-CUS-ORG"))
            .retrieve(Retrievals.allEnrolments) { enrolments =>
              val maybeEori =
                enrolments
                  .getEnrolment("HMRC-CUS-ORG")
                  .flatMap(_.getIdentifier("EORINumber"))
                  .map(_.value)

              maybeEori match {
                case Some(_) =>
                  Future.successful(next(requestHeader))
                case None =>
                  Future.successful(Accumulator.done(Unauthorized))
              }
            }
            .recover { case NonFatal(_) =>
              Accumulator.done(Unauthorized)
            }
        }
      }
    }
}
