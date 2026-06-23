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

import javax.inject._
import play.api.mvc._
import scala.io.Source

@Singleton
class CodeListController @Inject()(cc: ControllerComponents)
  extends AbstractController(cc) {

  private def loadXml(fileName: String): String = {
    val stream = getClass.getResourceAsStream(s"/codelists/$fileName") //Temporary placeholder to test endpoints with local files
    require(stream != null,s"fileName not found")

    Source.fromInputStream(stream).mkString
  }

  def messageType: Action[AnyContent] = Action {
    Ok(loadXml("messagetype.xml")).as("application/xml")
  }

  def typeoflocation: Action[AnyContent] = Action {
    Ok(loadXml("typeoflocation.xml")).as("application/xml")
  }

  def nationality: Action[AnyContent] = Action {
    Ok(loadXml("nationality.xml")).as("application/xml")
  }

  def transportmode: Action[AnyContent] = Action {
    Ok(loadXml("transportmode.xml")).as("application/xml")
  }

  def customsofficeexit: Action[AnyContent] = Action {
    Ok(loadXml("customsofficeexit.xml")).as("application/xml")
  }

  }
