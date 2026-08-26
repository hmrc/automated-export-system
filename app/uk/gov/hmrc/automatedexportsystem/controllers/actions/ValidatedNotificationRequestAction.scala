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

import play.api.Logging
import play.api.mvc.{ActionBuilder, ActionRefiner, AnyContent, BodyParser, BodyParsers, Request, Result, Results, WrappedRequest}
import uk.gov.hmrc.automatedexportsystem.config.AppConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scala.xml.{NodeSeq, XML}

case class ValidatedNotificationRequest[A](request: Request[A]) extends WrappedRequest[A](request)

@Singleton
class ValidatedNotificationRequestAction @Inject() (
  bodyParsers: BodyParsers.Default,
  appConfig:   AppConfig
)(implicit ec: ExecutionContext)
    extends ActionBuilder[ValidatedNotificationRequest, AnyContent]
    with ActionRefiner[Request, ValidatedNotificationRequest]
    with Logging:

  private val expectedNotificationHeader: String = appConfig.notificationToken

  override def parser:                     BodyParser[AnyContent] = bodyParsers
  override protected def executionContext: ExecutionContext       = ec

  override def refine[A](
    request: Request[A]
  ): Future[Either[Result, ValidatedNotificationRequest[A]]] = {
    val maybeAuth = request.headers.get("Authorization")

    if (maybeAuth.forall(_ != expectedNotificationHeader)) {
      logger.warn(s"Unauthorized request. Authorization header present: ${maybeAuth.isDefined}")
      Future.successful(Left(unauthorised("Invalid Authorization header")))
    } else {
      val maybeXmlString = extractBodyAsString(request.body)

      maybeXmlString match {
        case Some(xmlString) if Try(XML.loadString(xmlString)).isSuccess =>
          Future.successful(Right(ValidatedNotificationRequest(request)))

        case Some(_) =>
          logger.error("Invalid XML payload received")
          Future.successful(Left(badRequest("Invalid XML payload")))

        case None =>
          logger.error("Missing request body")
          Future.successful(Left(badRequest("Request body is required")))
      }
    }
  }

  private def extractBodyAsString(any: Any): Option[String] =
    any match {
      case c: AnyContent =>
        c.asXml.map(_.toString).orElse(c.asText)
      case nodeSeq: NodeSeq =>
        Some(nodeSeq.toString)
      case _ => None
    }

  private def badRequest(message: String): Result =
    Results
      .BadRequest(
        <error>
              <code>BAD_REQUEST</code>
              <message>{message}</message>
            </error>
      )
      .as("application/xml")

  private def unauthorised(message: String): Result =
    Results
      .Unauthorized(
        <error>
              <code>UNAUTHORIZED</code>
              <message>{message}</message>
            </error>
      )
      .as("application/xml")
