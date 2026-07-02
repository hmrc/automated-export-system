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

package uk.gov.hmrc.automatedexportsystem.parsers

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.LocalDateTime
import scala.xml.parsing.FatalError

class CodeListParserSpec extends AnyWordSpec with Matchers {

  "CodeListParser" should {

    "parse XML into a sequence of CodeList values" in {
      val xml =
        """<root>
          |  <item>
          |    <name>IE815</name>
          |  </item>
          |  <item>
          |    <name>IE507</name>
          |  </item>
          |</root>""".stripMargin

      val result = CodeListParser.parse(xml)

      result should have size 2
    }

    "parse the name from each XML item" in {
      val xml =
        """<root>
          |  <item>
          |    <name>IE815</name>
          |  </item>
          |</root>""".stripMargin

      val result = CodeListParser.parse(xml)

      result.head.name shouldBe "IE815"
    }

    "parse the description when present" in {
      val xml =
        """<root>
          |  <item>
          |    <name>IE815</name>
          |    <description>Export declaration</description>
          |  </item>
          |</root>""".stripMargin

      val result = CodeListParser.parse(xml)

      result.head.description shouldBe Some("Export declaration")
    }

    "return None for description when missing" in {
      val xml =
        """<root>
          |  <item>
          |    <name>IE815</name>
          |  </item>
          |</root>""".stripMargin

      val result = CodeListParser.parse(xml)

      result.head.description shouldBe None
    }

    "parse the start date when present" in {
      val xml =
        """<root>
          |  <item>
          |    <name>IE815</name>
          |    <startDate>2026-01-01T00:00:00</startDate>
          |  </item>
          |</root>""".stripMargin

      val result = CodeListParser.parse(xml)

      result.head.startDate shouldBe Some(
        LocalDateTime.parse("2026-01-01T00:00:00")
      )
    }

    "return None for start date when missing" in {
      val xml =
        """<root>
          |  <item>
          |    <name>IE815</name>
          |  </item>
          |</root>""".stripMargin

      val result = CodeListParser.parse(xml)

      result.head.startDate shouldBe None
    }

    "parse the end date when present" in {
      val xml =
        """<root>
          |  <item>
          |    <name>IE815</name>
          |    <endDate>2026-12-31T23:59:59</endDate>
          |  </item>
          |</root>""".stripMargin

      val result = CodeListParser.parse(xml)

      result.head.endDate shouldBe Some(
        LocalDateTime.parse("2026-12-31T23:59:59")
      )
    }

    "return None for end date when missing" in {
      val xml =
        """<root>
          |  <item>
          |    <name>IE815</name>
          |  </item>
          |</root>""".stripMargin

      val result = CodeListParser.parse(xml)

      result.head.endDate shouldBe None
    }

    "return an empty sequence when there are no items" in {
      val xml =
        """<root>
          |</root>""".stripMargin

      CodeListParser.parse(xml) shouldBe empty
    }

    "fail when given invalid XML" in {
      val invalidXml =
        "<root><item><name>IE815</name>"

      assertThrows[FatalError] {
        CodeListParser.parse(invalidXml)
      }
    }
  }
}
