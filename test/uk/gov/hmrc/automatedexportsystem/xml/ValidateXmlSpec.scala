package uk.gov.hmrc.automatedexportsystem.xml

import helpers.BaseSpec
import uk.gov.hmrc.automatedexportsystem.xml.models.{InvalidFormatNameError, InvalidLengthPhoneNumberError, MissingNameError, ValidationErrors}

import scala.io.Source
import scala.xml.{NodeSeq, XML}

class ValidateXmlSpec extends BaseSpec {
  val path = "xsd/contact.xsd"
  val mapper = XmlErrorCodeMapper.fromXsd(path)

  "ValidateXmlAgainstSchema" should {
    "should return true" in {
      val result = XmlSchemaValidator.validate(validXml.toString, path)
      result shouldBe empty
    }

    "should return invalid format error" in {
      val result =XmlSchemaValidator.validate(invalidFormatXml.toString, path)
      val code = ValidationErrors.fromValidationCode(mapper.toError(result.head).code)
      code shouldBe InvalidFormatNameError
    }

    "should return max occurance exceeded error" in {
      val result = XmlSchemaValidator.validate(tooManyXml.toString, path)
      val code = ValidationErrors.fromValidationCode(mapper.toError(result.head).code)
      code shouldBe InvalidLengthPhoneNumberError
    }

    "should return missing property error" in {
      val result = XmlSchemaValidator.validate(missingPropertyXml.toString, path)
      val code = ValidationErrors.fromValidationCode(mapper.toError(result.head).code)
      code shouldBe MissingNameError
    }
  }

  private def loadXmlResource(path: String): NodeSeq = {
    val stream = Option(getClass.getResourceAsStream(path))
      .getOrElse(throw new IllegalArgumentException(s"Missing XML test resource: $path"))
    val xmlString = Source.fromInputStream(stream, "UTF-8").mkString
    stream.close()
    XML.loadString(xmlString)
  }

  private lazy val validXml:           NodeSeq = loadXmlResource("samples/valid.xml")
  private lazy val invalidFormatXml:   NodeSeq = loadXmlResource("samples/invalid-format.xml")
  private lazy val missingPropertyXml: NodeSeq = loadXmlResource("samples/missing-property.xml")
  private lazy val tooManyXml:         NodeSeq = loadXmlResource("samples/too-many.xml")
}
