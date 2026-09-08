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

import cats.syntax.either.catsSyntaxEither
import play.api.Logging
import play.api.mvc.*
import uk.gov.hmrc.automatedexportsystem.config.AppConfig
import uk.gov.hmrc.automatedexportsystem.errors.RequestError
import uk.gov.hmrc.automatedexportsystem.models.responses.AesErrorResponse.toErrorResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
case class ValidatedNotificationRequest[A](request: Request[A]) extends WrappedRequest[A](request)

@Singleton
class ValidatedNotificationRequestAction @Inject() (
  bodyParsers: BodyParsers.Default,
  appConfig:   AppConfig
)(implicit ec: ExecutionContext)
    extends ActionBuilder[ValidatedNotificationRequest, AnyContent]
    with ActionRefiner[Request, ValidatedNotificationRequest]
    with Logging:

  override def parser: BodyParser[AnyContent] = bodyParsers

  override protected def executionContext: ExecutionContext = ec

  override def refine[A](
    request: Request[A]
  ): Future[Either[Result, ValidatedNotificationRequest[A]]] =
    Future.successful(
      request.headers
        .get("Authorization")
        .toRight {
          logUnauthorizedError("Authorization header is missing")

          RequestError.MissingAuthorizationHeader
        }
        .flatMap(token =>
          val notificationBearerToken: String = appConfig.notificationToken

          if token != notificationBearerToken then
            logUnauthorizedError(s"Authorization Bearer token is invalid: $token")

            Left(RequestError.InvalidAuthorizationToken)
          else Right(ValidatedNotificationRequest(request))
        )
        .leftMap(_.toErrorResponse.toResult)
    )

  private def logUnauthorizedError(context: String): Unit =
    val ctx: String =
      if context.trim.isEmpty then ""
      else s" ${context.trim}"

    logger.warn(s"Unauthorized request:$ctx")
