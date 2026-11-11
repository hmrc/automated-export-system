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

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.{Materializer, SystemMaterializer}
import org.apache.pekko.stream.scaladsl.{Sink, Source}
import org.apache.pekko.util.ByteString
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.automatedexportsystem.models.codelists.CodeListEntry

import java.time.LocalDate
import java.time.format.DateTimeParseException
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.*
import scala.io.Source as IoSource
import scala.util.Using

class CodeListCsvParserSpec
  extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with BeforeAndAfterAll {

  private val system = ActorSystem("CodeListCsvParserSpec")

  private given Materializer =
    SystemMaterializer(system).materializer

  private val header =
    "Entity Name,Code List Entry Code,State,Start Date,Description"

  private def parse(csv: String): Future[Seq[CodeListEntry]] =
    Source
      .single(ByteString(csv))
      .via(CodeListCsvParser.flow("TransportMode"))
      .runWith(Sink.seq)

  override def afterAll(): Unit =
    try {
      Await.result(system.terminate(), 10.seconds)
      ()
    } finally {
      super.afterAll()
    }

  "CodeListCsvParser" should {

    "parse the eight transport modes from the bundled CSV" in {
      val csv = Using.resource(
        IoSource.fromResource("codelists/transport-mode.csv")
      )(_.mkString)

      val entries = parse(csv).futureValue

      entries.map(_.key) shouldBe Seq(
        "1", "2", "3", "4", "5", "7", "8", "9"
      )

      entries.head shouldBe CodeListEntry(
        key = "1",
        value = "Sea transport",
        state = "valid",
        startDate = LocalDate.of(2023, 3, 4)
      )
    }

    "trim the code and other field values" in {
      val csv =
        s"""$header
           |TransportMode, TransportMode = 1  , valid ,04-03-2023, Sea transport
           |""".stripMargin

      val entry = parse(csv).futureValue.head

      entry.key shouldBe "1"
      entry.value shouldBe "Sea transport"
      entry.state shouldBe "valid"
    }

    "handle a quoted description containing a comma" in {
      val csv =
        s"""$header
           |TransportMode,TransportMode = 1,valid,04-03-2023,"Sea transport, including ferries"
           |""".stripMargin

      parse(csv).futureValue.head.value shouldBe
        "Sea transport, including ferries"
    }

    "retain invalid and future-dated entries for service filtering" in {
      val csv =
        s"""$header
           |TransportMode,TransportMode = 1,invalid,04-03-2023,Sea transport
           |TransportMode,TransportMode = 2,valid,01-01-2099,Rail transport
           |""".stripMargin

      val entries = parse(csv).futureValue

      entries should have size 2
      entries.head.state shouldBe "invalid"
      entries(1).startDate shouldBe LocalDate.of(2099, 1, 1)
    }

    "parse an optional end date" in {
      val csv =
        s"""$header,End Date
           |TransportMode,TransportMode = 1,valid,04-03-2023,Sea transport,31-12-2026
           |""".stripMargin

      parse(csv).futureValue.head.endDate shouldBe
        Some(LocalDate.of(2026, 12, 31))
    }

    "reject a missing required column" in {
      val csv =
        """Entity Name,Code List Entry Code,Start Date,Description
          |TransportMode,TransportMode = 1,04-03-2023,Sea transport
          |""".stripMargin

      parse(csv).failed.futureValue.getMessage should include("State")
    }

    "reject an empty required value" in {
      val csv =
        s"""$header
           |TransportMode,TransportMode = 1,valid,,Sea transport
           |""".stripMargin

      parse(csv).failed.futureValue.getMessage should include("Start Date")
    }

    "reject an impossible date" in {
      val csv =
        s"""$header
           |TransportMode,TransportMode = 1,valid,31-02-2026,Sea transport
           |""".stripMargin

      parse(csv).failed.futureValue shouldBe a[DateTimeParseException]
    }

    "reject an unexpected entity" in {
      val csv =
        s"""$header
           |Nationality,Nationality = GB,valid,04-03-2023,British
           |""".stripMargin

      parse(csv).failed.futureValue.getMessage should include(
        "Expected CSV entity 'TransportMode'"
      )
    }

    "reject a malformed code" in {
      val csv =
        s"""$header
           |TransportMode,TransportMode = ,valid,04-03-2023,Sea transport
           |""".stripMargin

      parse(csv).failed.futureValue.getMessage should include(
        "Invalid code list entry code"
      )
    }
  }
}