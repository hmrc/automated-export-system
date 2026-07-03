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

@Singleton
class CodeListController @Inject() (
  val controllerComponents: ControllerComponents
) extends BaseController {

  private def loadXml(fileName: String) =
    scala.xml.XML.loadFile(
      s"conf/codelists/$fileName"
    )

  def messageTypes: Action[AnyContent] = Action {
    Ok(loadXml("message-type.xml"))
      .as("application/xml")
  }

  def typeOfLocations: Action[AnyContent] = Action {
    Ok(loadXml("type-of-location.xml"))
      .as("application/xml")
  }

  def nationalities: Action[AnyContent] = Action {
    Ok(loadXml("nationality.xml"))
      .as("application/xml")
  }

  def transportModes: Action[AnyContent] = Action {
    Ok(loadXml("transport-mode.xml"))
      .as("application/xml")
  }

  def customsOfficeExits: Action[AnyContent] = Action {
    Ok(loadXml("customs-office-exit.xml"))
      .as("application/xml")
  }
}
