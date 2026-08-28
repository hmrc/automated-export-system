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

package uk.gov.hmrc.automatedexportsystem.models.IE507

import cats.syntax.apply.catsSyntaxTuple2Semigroupal
import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.automatedexportsystem.xml.XmlWriter.toXml
import uk.gov.hmrc.automatedexportsystem.xml.{XmlPath, XmlReader, XmlRootTag, XmlWriter}

import scala.xml.NodeSeq

final case class GoodsMeasure(grossMass: GrossMass, netMass: NetMass)

object GoodsMeasure:
  given mongoFormat: Format[GoodsMeasure] = Json.format[GoodsMeasure]

  given goodsMeasureTag: XmlRootTag[GoodsMeasure] = XmlRootTag("GoodsMeasure")
  
  given goodsMeasureXmlWriter: XmlWriter[GoodsMeasure] =
    (o, label) =>
      val children: NodeSeq =
        o.grossMass.toXml("grossMass")
          ++ o.netMass.toXml("netMass")

      XmlWriter.elem(label, children)

  given goodsMeasureXmlReader: XmlReader[GoodsMeasure] =
    XmlReader.nonEmptyReader { (xml, path) =>
      (
        (XmlPath \ "grossMass").read[GrossMass](xml, path),
        (XmlPath \ "netMass").read[NetMass](xml, path)
      ).mapN(GoodsMeasure.apply)
    }
