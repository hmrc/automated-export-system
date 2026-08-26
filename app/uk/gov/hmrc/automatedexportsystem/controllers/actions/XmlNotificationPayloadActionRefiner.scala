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

import play.api.mvc.{ActionRefiner, Result}
import play.api.mvc.Results.BadRequest

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq
import play.api.mvc.AnyContentAsXml
import uk.gov.hmrc.automatedexportsystem.controllers.actions.request.NotificationPayloadRequest
import uk.gov.hmrc.automatedexportsystem.models.notification.AESDigitalNotification

class XmlNotificationPayloadActionRefiner @javax.inject.Inject() ()(using override protected val executionContext: ExecutionContext)
    extends ActionRefiner[ValidatedNotificationRequest, NotificationPayloadRequest]:

  override protected def refine[A](validatedRequest: ValidatedNotificationRequest[A]): Future[Either[Result, NotificationPayloadRequest[A]]] =
    Future.successful(
      try {
        val xml: NodeSeq = validatedRequest.body match
          case anyContentAsXml: AnyContentAsXml =>
            anyContentAsXml.xml
          case _ =>
            throw new IllegalArgumentException("Expected XML body")

        val notification = AESDigitalNotification.fromXml(xml)
        Right(NotificationPayloadRequest(notification, validatedRequest.request))
      } catch {
        case _: NumberFormatException =>
          Left(
            BadRequest(
              <error>
                <code>INVALID_STATUS</code>
                <message>Status must be a valid integer (1, 2, or 5)</message>
              </error>
            ).as("application/xml")
          )
        case _: Exception =>
          Left(
            BadRequest(
              <error>
                <code>INVALID_NOTIFICATION</code>
                <message>Failed to parse notification payload</message>
              </error>
            ).as("application/xml")
          )
      }
    )
