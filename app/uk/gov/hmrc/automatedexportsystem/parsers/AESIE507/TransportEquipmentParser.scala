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
import uk.gov.hmrc.automatedexportsystem.models.IE507.{ContainerIdentificationNumber, NumberOfSeals, SequenceNumber, TransportEquipment}
import uk.gov.hmrc.automatedexportsystem.parsers.AESIE507.Helpers.*

import scala.xml.Node

object TransportEquipmentParser {
  def parseTransportEquipment(n: Node): Either[String, Option[NonEmptyList[TransportEquipment]]] = {
    val nodes = (n \ Tags.TransportEquipment).toList
    sequence(nodes.map(parseTransportEquipmentNode)).map(NonEmptyList.fromList)
  }

  private def parseTransportEquipmentNode(n: Node): Either[String, TransportEquipment] =
    for {
      sequenceNumber <- parseOptionalInt(textOptChild(n, Tags.SequenceNumber), Tags.SequenceNumber).map(_.map(SequenceNumber.apply))
      numberOfSeals  <- parseOptionalInt(textOptChild(n, Tags.NumberOfSeals), Tags.NumberOfSeals).map(_.map(NumberOfSeals.apply))
    } yield TransportEquipment(
      sequenceNumber = sequenceNumber,
      containerIdentificationNumber = textOptChild(n, Tags.ContainerIdentificationNumber)
        .map(_.trim)
        .filter(_.nonEmpty)
        .map(ContainerIdentificationNumber.apply),
      numberOfSeals = numberOfSeals
    )
}
