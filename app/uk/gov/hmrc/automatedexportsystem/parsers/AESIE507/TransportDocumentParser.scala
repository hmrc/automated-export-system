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

package uk.gov.hmrc.automatedexportsystem.parsers.AESIE507

import cats.data.NonEmptyList
import uk.gov.hmrc.automatedexportsystem.models.IE507.{ReferenceNumber, SequenceNumber, TransportDocument, TransportDocumentType}
import uk.gov.hmrc.automatedexportsystem.parsers.AESIE507.Helpers.{parseOptionalInt, sequence, textOptChild}

import scala.xml.Node

object TransportDocumentParser {

  def parseTransportDocuments(n: Node): Either[String, Option[NonEmptyList[TransportDocument]]] = {
    val nodes = (n \ Tags.TransportDocument).toList
    sequence(nodes.map(parseTransportDocumentNode)).map(NonEmptyList.fromList)
  }

  private def parseTransportDocumentNode(n: Node): Either[String, TransportDocument] =
    for {
      sequenceNumber <- parseOptionalInt(textOptChild(n, Tags.SequenceNumber), Tags.SequenceNumber).map(_.map(SequenceNumber.apply))
      documentType   <- parseOptionalInt(textOptChild(n, Tags.Type), Tags.Type).map(_.map(TransportDocumentType.apply))
    } yield TransportDocument(
      sequenceNumber = sequenceNumber,
      transportDocumentType = documentType,
      referenceNumber = textOptChild(n, Tags.ReferenceNumber).map(ReferenceNumber.apply)
    )

}
