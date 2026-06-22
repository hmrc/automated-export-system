package uk.gov.hmrc.automatedexportsystem.controllers

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.FakeRequest
import play.api.test.Helpers._

class CodeListControllerISpec extends PlaySpec with GuiceOneAppPerSuite {

  private val baseUrl = "/automated-export-system/codelists"

  "CodeListController" should {

    "return XML for message type endpoint" in {
      val request = FakeRequest(GET, s"$baseUrl/messagetype")

      val result = route(app, request).get

      status(result) mustBe OK
      contentType(result) mustBe Some("application/xml")
      contentAsString(result) must include("<codeList>")
    }

    "return XML for all endpoints" in {
      val endpoints = Seq(
        "messagetype",
        "typeoflocation",
        "nationality",
        "transportmode",
        "customsofficeexit"
      )


      endpoints.foreach { endpoint =>
        val request = FakeRequest(GET, s"$baseUrl/$endpoint")
        val result = route(app, request).get

        status(result) mustBe OK
        contentType(result) mustBe Some("application/xml")
        contentAsString(result) must not be empty
      }
    }

    "return 404 for an unknown code list endpoint" in {
      val request = FakeRequest(GET, s"$baseUrl/invalid")
      val result = route(app, request).get

      status(result) mustBe NOT_FOUND
    }

    "endpoint must return consistent data" in {
      val request = FakeRequest(GET, s"$baseUrl/messagetype")
      val result1 = route(app, request).get
      val result2 = route(app, request).get

      contentAsString(result1) mustBe contentAsString(result2)
    }

    "wrong HTTP method should return 405" in {
      val request = FakeRequest(POST, s"$baseUrl/messagetype")
      val result = route(app, request).get

      status(result) mustBe METHOD_NOT_ALLOWED
    }

    "content is valid XML" in {
      val request = FakeRequest(GET, s"$baseUrl/messagetype")
      val result = route(app, request).get

      val xmlString = contentAsString(result)
      noException should be thrownBy scala.xml.XML.loadString(xmlString)
    }

    "case must be handled correctly" in {
      val request = FakeRequest(GET, s"$baseUrl/MessageType")
      val result = route(app, request).get

      status(result) mustBe NOT_FOUND
    }

    "trailing slash should return 404" in {
      val request = FakeRequest(GET, s"$baseUrl/messagetype/")
      val result = route(app, request).get

      status(result) mustBe NOT_FOUND
    }
  }
}

