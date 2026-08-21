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
import uk.gov.hmrc.automatedexportsystem.models.IE507.{NumberOfPackages, Packaging, SequenceNumber, ShippingMarks, TypeOfPackages}
import uk.gov.hmrc.automatedexportsystem.parsers.AESIE507.Helpers.{parseOptionalInt, sequence, textOptChild}

import scala.xml.Node

object PackagingParser {

  def parsePackaging(n: Node): Either[String, Option[NonEmptyList[Packaging]]] = {
    val nodes = (n \ Tags.Packaging).toList
    sequence(nodes.map(parsePackagingNode)).map(NonEmptyList.fromList)
  }

  private def parsePackagingNode(n: Node): Either[String, Packaging] =
    for {
      sequenceNumber   <- parseOptionalInt(textOptChild(n, Tags.SequenceNumber), Tags.SequenceNumber).map(_.map(SequenceNumber.apply))
      numberOfPackages <- parseOptionalInt(textOptChild(n, Tags.NumberOfPackages), Tags.NumberOfPackages).map(_.map(NumberOfPackages.apply))
    } yield Packaging(
      sequenceNumber = sequenceNumber,
      typeOfPackages = textOptChild(n, Tags.TypeOfPackages).map(TypeOfPackages.apply),
      numberOfPackages = numberOfPackages,
      shippingMarks = textOptChild(n, Tags.ShippingMarks).map(ShippingMarks.apply)
    )

}
