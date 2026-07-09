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

import play.api.http.DefaultHttpErrorHandler
import play.api.mvc.{RequestHeader, Result}
import play.api.routing.Router
import play.api.{Configuration, Environment, OptionalSourceMapper, UsefulException}
import uk.gov.hmrc.automatedexportsystem.errors.ResponseCode
import uk.gov.hmrc.automatedexportsystem.models.responses.AesErrorResponse

import javax.inject.{Inject, Provider, Singleton}
import scala.concurrent.Future

@Singleton
class AesErrorHandler @Inject() (environment: Environment, configuration: Configuration, sourceMapper: OptionalSourceMapper, router: Provider[Router])
    extends DefaultHttpErrorHandler(environment, configuration, sourceMapper, router):
  override protected def onBadRequest(request: RequestHeader, message: String): Future[Result] =
    Future.successful(AesErrorResponse.fromResponseCode(ResponseCode.BadRequest, message).toResult)

  override protected def onForbidden(request: RequestHeader, message: String): Future[Result] =
    Future.successful(AesErrorResponse.fromResponseCode(ResponseCode.Forbidden, message).toResult)

  override protected def onNotFound(request: RequestHeader, message: String): Future[Result] =
    Future.successful(AesErrorResponse.fromResponseCode(ResponseCode.NotFound, message).toResult)

  override protected def onOtherClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] =
    val maybeResponseCode: Option[ResponseCode] = ResponseCode.values.find(_.status == statusCode)

    Future.successful(
      maybeResponseCode
        .map(responseCode => AesErrorResponse.fromResponseCode(responseCode, message))
        .getOrElse(AesErrorResponse.fromStatusAndCode(statusCode, "CLIENT_ERROR", message))
        .toResult
    )

  override protected def onDevServerError(request: RequestHeader, exception: UsefulException): Future[Result] =
    Future.successful(AesErrorResponse.fromResponseCode(ResponseCode.InternalServerError, exception.getMessage).toResult)

  override protected def onProdServerError(request: RequestHeader, exception: UsefulException): Future[Result] =
    onDevServerError(request, exception)

  override protected def fatalErrorMessage(request: RequestHeader, exception: Throwable): String =
    "Fatal error: error handler has failed handling a server error"
