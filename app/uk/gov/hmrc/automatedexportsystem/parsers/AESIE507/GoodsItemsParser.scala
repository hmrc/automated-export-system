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
import uk.gov.hmrc.automatedexportsystem.models.IE507.*
import uk.gov.hmrc.automatedexportsystem.parsers.AESIE507.Helpers.*

import scala.xml.Node

object GoodsItemsParser {
  def parseGoodsItems(n: Node): Either[String, Option[NonEmptyList[GoodsItem]]] = {
    val nodes = (n \\ Tags.GoodsItem).toList
    sequence(nodes.map(parseGoodsItemNode)).map(NonEmptyList.fromList)
  }

  private def parseGoodsItemNode(n: Node): Either[String, GoodsItem] =
    for {
      goodsMeasure  <- req((n \ Tags.Commodity \ Tags.GoodsMeasure).headOption, Tags.GoodsMeasure)
      gross         <- req(textOptChild(goodsMeasure, Tags.GrossMass), Tags.GrossMass).flatMap(parseBigDecimal).map(GrossMass.apply)
      net           <- req(textOptChild(goodsMeasure, Tags.NetMass), Tags.NetMass).flatMap(parseBigDecimal).map(NetMass.apply)
      packaging     <- PackagingParser.parsePackaging(n)
      declarationNo <- parseOptionalInt(textOptChild(n, Tags.DeclarationGoodsItemNumber), Tags.DeclarationGoodsItemNumber)
                         .map(_.map(DeclarationGoodsItemNumber.apply))
    } yield GoodsItem(
      declarationGoodsItemNumber = declarationNo,
      referenceNumberUcr = textOptChild(n, Tags.ReferenceNumberUCR).map(_.trim).filter(_.nonEmpty).map(ReferenceNumberUcr.apply),
      commodity = Commodity(GoodsMeasure(gross, net)),
      packaging = packaging
    )

}
