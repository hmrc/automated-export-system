package uk.gov.hmrc.automatedexportsystem.xml

import org.xml.sax.SAXParseException

final case class XmlValidationError(code: String, message: String, line: Int, column: Int)

class XmlErrorCodeMapper(propertyLookup: Map[String, String]) {

  private val ElementRegex          = "element '([^']+)'".r
  private val TypeRegex             = "type '([^']+)'".r
  private val MaxOccursElementRegex = "'([^']+)' can occur a (?:maximum|minimum) of".r
  private val ExpectedOneOfRegex    = "One of '\\{([^}]*)\\}' is expected".r
  private val ExpectedSingleRegex   = "'\\{([^}]*)\\}' is expected".r

  private def xsdCode(msg: String): String =
    msg.takeWhile(_ != ':')

  private def normalize(value: String): String =
    value
      .replaceAll("([a-z])([A-Z])", "$1_$2")
      .replaceAll("[^A-Za-z0-9_]", "_")
      .toUpperCase
  
  private def cleanTypeName(raw: String): String =
    raw
      .stripPrefix("#")
      .replaceAll("^AnonType_", "")
      .replaceAll("Contact$", "")

  private def localName(token: String): String =
    token.trim
      .split(",")
      .headOption
      .getOrElse(token.trim)
      .trim
      .split(":")
      .lastOption
      .getOrElse(token.trim)
  
  private def expectedElement(msg: String): Option[String] =
    ExpectedOneOfRegex.findFirstMatchIn(msg).map(m => localName(m.group(1)))
      .orElse(ExpectedSingleRegex.findFirstMatchIn(msg).map(m => localName(m.group(1))))
  
  private def inferredElementFromType(rawType: Option[String]): Option[String] =
    rawType
      .map(_.stripPrefix("#"))
      .flatMap { t =>
        "^AnonType_([A-Za-z0-9_]+)Contact$".r.findFirstMatchIn(t).map(_.group(1))
          .orElse("^AnonType_([A-Za-z0-9_]+)$".r.findFirstMatchIn(t).map(_.group(1)))
      }

  private def propertyName(msg: String, prefix: String): String = {
    val rawType: Option[String]   = TypeRegex.findFirstMatchIn(msg).map(_.group(1))
    val cleanType: Option[String] = rawType.map(cleanTypeName)

    val elementFromGeneral: Option[String] =
      ElementRegex.findFirstMatchIn(msg).map(_.group(1))
        .orElse(MaxOccursElementRegex.findFirstMatchIn(msg).map(_.group(1)))

    val elementFromExpected: Option[String] = expectedElement(msg)
    val elementFromType: Option[String]     = inferredElementFromType(rawType)

    // Need to check missing element first
    val chosenElement: Option[String] =
      if (prefix == "MISSING")
        elementFromExpected.orElse(elementFromGeneral).orElse(elementFromType)
      else
        elementFromGeneral.orElse(elementFromType).orElse(elementFromExpected)

    val typeKeys: List[String] =
      (rawType.toList.map(_.stripPrefix("#")) ++ cleanType.toList).distinct

    val elementKeys: List[String] =
      chosenElement.toList.distinct
    
    val annotationValue: Option[String] =
      elementKeys.view
        .map(e => propertyLookup.get(s"element:$e"))
        .collectFirst { case Some(v) => v }
        .orElse(
          typeKeys.view
            .map(t => propertyLookup.get(s"type:$t"))
            .collectFirst { case Some(v) => v }
        )

    annotationValue
      .orElse(cleanType)
      .orElse(chosenElement)
      .map(normalize)
      .getOrElse("FIELD")
  }

  def toError(e: SAXParseException): XmlValidationError = {
    val msg      = Option(e.getMessage).getOrElse("XML validation error")
    val lowerMsg = msg.toLowerCase
    val code     = xsdCode(msg)

    val prefix = code match {
      case "cvc-pattern-valid" | "cvc-maxLength-valid" |  "cvc-minLength-valid" =>
        "INVALID_FORMAT"
        
      case c if c.startsWith("cvc-complex-type.2.4.f") &&
        (lowerMsg.contains("can occur a maximum of") ||
          lowerMsg.contains("can occur a minimum of") ||
          lowerMsg.contains("limit was exceeded")) =>
        "INVALID_LENGTH"

      case c if c.startsWith("cvc-complex-type.2.4") &&
        (lowerMsg.contains("is expected") || lowerMsg.contains("not complete")) =>
        "MISSING"

      case _ =>
        "XML_VALIDATION_ERROR"
    }

    val prop = propertyName(msg, prefix)
    XmlValidationError(s"${prefix}_$prop", msg, e.getLineNumber, e.getColumnNumber)
  }
}

object XmlErrorCodeMapper {
  def fromXsd(xsdPath: String): XmlErrorCodeMapper =
    new XmlErrorCodeMapper(XsdPropertyLookup.fromResource(xsdPath))
}