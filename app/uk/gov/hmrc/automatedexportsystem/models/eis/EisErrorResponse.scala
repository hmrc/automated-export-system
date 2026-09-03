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

package uk.gov.hmrc.automatedexportsystem.models.eis

import cats.implicits.catsSyntaxTuple6Semigroupal
import uk.gov.hmrc.automatedexportsystem.xml.XmlWriter.toXml
import uk.gov.hmrc.automatedexportsystem.xml.{XmlPath, XmlReader, XmlRootTag, XmlWriter}

import java.time.Instant
import scala.xml.{NamespaceBinding, NodeSeq, TopScope}

final case class EisErrorResponse(
  timestamp:         Instant,
  correlationId:     String,
  errorCode:         Int,
  errorMessage:      String,
  source:            String,
  sourceFaultDetail: SourceFaultDetail
)

object EisErrorResponse:
  private val eisErrorResponseScope: NamespaceBinding =
    NamespaceBinding(
      prefix = null,
      uri = "http://www.hmrc.gsi.gov.uk/eis",
      parent = TopScope
    )
  given eisErrorResponseTag: XmlRootTag[EisErrorResponse] = XmlRootTag("errorDetail")

  given eisErrorResponseXmlWriter: XmlWriter[EisErrorResponse] =
    (o, label) =>
      val children: NodeSeq =
        o.timestamp.toString.toXml("timestamp")
          ++ o.correlationId.toXml("correlationId")
          ++ o.errorCode.toXml("errorCode")
          ++ o.errorMessage.toXml("errorMessage")
          ++ o.source.toXml("source")
          ++ o.sourceFaultDetail.toXml("sourceFaultDetail")

      XmlWriter.elemWithScope(label, eisErrorResponseScope, children)

  given eisErrorResponseXmlReader: XmlReader[EisErrorResponse] =
    XmlReader.nonEmptyReader { (xml, path) =>
      (
        (XmlPath \ "timestamp").read[Instant](xml, path),
        (XmlPath \ "correlationId").read[String](xml, path),
        (XmlPath \ "errorCode").read[Int](xml, path),
        (XmlPath \ "errorMessage").read[String](xml, path),
        (XmlPath \ "source").read[String](xml, path),
        (XmlPath \ "sourceFaultDetail").read[SourceFaultDetail](xml, path)
      ).mapN(EisErrorResponse.apply)
    }

final case class SourceFaultDetail(details: Seq[String])

object SourceFaultDetail:
  given sourceFaultDetailXmlWriter: XmlWriter[SourceFaultDetail] =
    (o, label) => XmlWriter.elem(label, o.details.toList.toXml("detail"))

  given sourceFaultDetailXmlReader: XmlReader[SourceFaultDetail] =
    XmlReader.nonEmptyReader { (xml, path) =>
      (XmlPath \ "detail").read[List[String]](xml, path).map(SourceFaultDetail.apply)
    }
