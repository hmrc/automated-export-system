package uk.gov.hmrc.automatedexportsystem.xml

import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.{XPathConstants, XPathFactory}
import org.w3c.dom.{Element, NodeList}

object XsdPropertyLookup {
  private val AppInfoSource = "urn:hmrc:error-key"

  def fromResource(xsdPath: String): Map[String, String] = {
    val is = Option(getClass.getResourceAsStream(xsdPath))
      .getOrElse(throw new IllegalArgumentException(s"XSD not found: $xsdPath"))

    try {
      val dbf = DocumentBuilderFactory.newInstance()
      dbf.setNamespaceAware(true)
      val doc   = dbf.newDocumentBuilder().parse(is)
      val xpath = XPathFactory.newInstance().newXPath()

      val expr =
        s"//*[local-name()='element' or local-name()='complexType' or local-name()='simpleType']" +
          s"[@name and *[local-name()='annotation']/*[local-name()='appinfo' and @source='$AppInfoSource']]"

      val nodes = xpath.evaluate(expr, doc, XPathConstants.NODESET).asInstanceOf[NodeList]

      (0 until nodes.getLength).flatMap { i =>
        val node  = nodes.item(i).asInstanceOf[Element]
        val kind  = node.getLocalName
        val name  = node.getAttribute("name").trim
        val value = xpath.evaluate(
          s"normalize-space(*[local-name()='annotation']/*[local-name()='appinfo' and @source='$AppInfoSource'][1]/text())",
          node
        ).trim
 
        if (name.nonEmpty && value.nonEmpty) {
          val key = kind match {
            case "element"                 => s"element:$name"
            case "complexType" | "simpleType" => s"type:$name"
            case _ => ""
          }
          if (key.nonEmpty) Some(key -> value) else None
        } else None
      }.toMap
    } finally is.close()
  }
}
