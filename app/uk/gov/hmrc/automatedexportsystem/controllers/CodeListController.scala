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