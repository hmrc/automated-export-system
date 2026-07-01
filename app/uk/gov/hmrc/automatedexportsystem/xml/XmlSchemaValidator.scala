package uk.gov.hmrc.automatedexportsystem.xml

import java.io.StringReader
import javax.xml.XMLConstants
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory
import org.xml.sax.{ErrorHandler, SAXParseException}
 
object XmlSchemaValidator {

  def validate(xml: String, xsdPath: String): List[SAXParseException] = {
    val schemaUrl = Option(getClass.getResource(xsdPath))
      .getOrElse(throw new IllegalArgumentException(s"XSD not found: $xsdPath"))

    val schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(schemaUrl)
    val validator = schema.newValidator()

    val errors = scala.collection.mutable.ListBuffer.empty[SAXParseException]
    validator.setErrorHandler(new ErrorHandler {
      override def warning(exception: SAXParseException): Unit = ()
      override def error(exception: SAXParseException): Unit = errors += exception
      override def fatalError(exception: SAXParseException): Unit = errors += exception
    })

    validator.validate(new StreamSource(new StringReader(xml)))
    errors.toList
  }
}
