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

import uk.gov.hmrc.automatedexportsystem.helpers.BaseSpec
import org.scalatest.EitherValues
import org.scalatest.EitherValues.*
import scala.xml.XML

class LocationOfGoodsParserSpec extends BaseSpec {
  "parseLocationOfGoods" - {
    "return Right when required fields are present (and optional absent)" in {
      val xml: scala.xml.Elem =
        XML.loadString(
          """
            |<LocationOfGoods>
            |  <typeOfLocation>1</typeOfLocation>
            |  <qualifierOfIdentification>1</qualifierOfIdentification>
            |</LocationOfGoods>
            |""".stripMargin
        )

      val result = LocationOfGoodsParser.parseLocationOfGoods(xml)

      result.isRight shouldBe true
      val parsed = result.value

      parsed.typeOfLocation.value            shouldBe "1"
      parsed.qualifierOfIdentification.value shouldBe "1"
      parsed.authorisationNumber             shouldBe None
      parsed.additionalIdentifier            shouldBe None
      parsed.unLocode                        shouldBe None
    }

    "return Right when all fields are present" in {
      val xml: scala.xml.Elem =
        XML.loadString(
          """
            |<LocationOfGoods>
            |  <typeOfLocation>1</typeOfLocation>
            |  <qualifierOfIdentification>1</qualifierOfIdentification>
            |  <authorisationNumber>AUTH123</authorisationNumber>
            |  <additionalIdentifier>ADD456</additionalIdentifier>
            |  <UNLocode>GBLON</UNLocode>
            |</LocationOfGoods>
            |""".stripMargin
        )

      val result = LocationOfGoodsParser.parseLocationOfGoods(xml)

      result.isRight shouldBe true
      val parsed = result.value

      parsed.typeOfLocation.value              shouldBe "1"
      parsed.qualifierOfIdentification.value   shouldBe "1"
      parsed.authorisationNumber.map(_.value)  shouldBe Some("AUTH123")
      parsed.additionalIdentifier.map(_.value) shouldBe Some("ADD456")
      parsed.unLocode.map(_.value)             shouldBe Some("GBLON")
    }

    "return Left when typeOfLocation is missing" in {
      val xml: scala.xml.Elem =
        XML.loadString(
          """
            |<LocationOfGoods>
            |  <qualifierOfIdentification>1</qualifierOfIdentification>
            |</LocationOfGoods>
            |""".stripMargin
        )

      val result = LocationOfGoodsParser.parseLocationOfGoods(xml)

      result.isLeft                 shouldBe true
      result.left.value.toLowerCase shouldBe "missing required field: typeoflocation"
    }

    "return Left when qualifierOfIdentification is missing" in {
      val xml: scala.xml.Elem =
        XML.loadString(
          """
            |<LocationOfGoods>
            |  <typeOfLocation>1</typeOfLocation>
            |</LocationOfGoods>
            |""".stripMargin
        )

      val result = LocationOfGoodsParser.parseLocationOfGoods(xml)

      result.isLeft                 shouldBe true
      result.left.value.toLowerCase shouldBe "missing required field: qualifierofidentification"
    }
  }
}
