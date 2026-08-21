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

import uk.gov.hmrc.automatedexportsystem.models.IE507.{AdditionalIdentifier, AuthorisationNumber, LocationOfGoods, QualifierOfIdentification, TypeOfLocation, UnLocode}
import uk.gov.hmrc.automatedexportsystem.parsers.AESIE507.Helpers.{req, textOptChild}

import scala.xml.Node

object LocationOfGoodsParser {
  def parseLocationOfGoods(n: Node): Either[String, LocationOfGoods] =
    for {
      typeOfLocation <- req(textOptChild(n, Tags.TypeOfLocation), Tags.TypeOfLocation).map(TypeOfLocation.apply)
      qualifier      <- req(textOptChild(n, Tags.QualifierOfIdentification), Tags.QualifierOfIdentification).map(QualifierOfIdentification.apply)
    } yield LocationOfGoods(
      typeOfLocation = typeOfLocation,
      qualifierOfIdentification = qualifier,
      authorisationNumber = textOptChild(n, Tags.AuthorisationNumber).map(AuthorisationNumber.apply),
      additionalIdentifier = textOptChild(n, Tags.AdditionalIdentifier).map(AdditionalIdentifier.apply),
      unLocode = textOptChild(n, Tags.UNLocode).map(UnLocode.apply)
    )

}
