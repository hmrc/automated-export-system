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

package uk.gov.hmrc.automatedexportsystem.controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Singleton
class IE507StubController @Inject() (
  val controllerComponents: ControllerComponents
) extends BaseController {

  private val RequiredHeaders = Seq(
    "x-forwarded-host",
    "x-correlation-id",
    "date",
    "content-type",
    "accept",
    "authorization",
    "x-message-type"
  )

  def submit: Action[AnyContent] =
    Action { request =>

      val hasMissingRequiredHeaders =
        RequiredHeaders.exists(request.headers.get(_).isEmpty)

      if (hasMissingRequiredHeaders)
        BadRequest("Missing required headers")
      else {

        val correlationId =
          request.headers.get("x-correlation-id").getOrElse("")

        NoContent.withHeaders(
          "x-correlation-id" -> correlationId,
          "date" -> DateTimeFormatter.RFC_1123_DATE_TIME
            .format(ZonedDateTime.now())
        )
      }
    }
}
