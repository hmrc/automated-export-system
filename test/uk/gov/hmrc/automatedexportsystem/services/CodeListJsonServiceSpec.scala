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

package uk.gov.hmrc.automatedexportsystem.services

import org.mockito.Mockito.{mock, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.automatedexportsystem.models.codelists.CodeListEntry

import java.time.{Clock, Instant, LocalDate, ZoneOffset}
import scala.concurrent.{ExecutionContext, Future}

class CodeListJsonServiceSpec extends AnyWordSpec with Matchers with ScalaFutures {

  private given ExecutionContext = ExecutionContext.global

  private val today = LocalDate.of(2026, 11, 4)

  private val fixedClock =
    Clock.fixed(
      Instant.parse("2026-11-04T12:00:00Z"),
      ZoneOffset.UTC
    )

  private val codeLists: Seq[
    (
      String,
      CsvCodeListLoader => Future[Seq[CodeListEntry]],
      CodeListJsonService => Future[Seq[CodeListEntry]]
    )
  ] = Seq(
    ("transport modes", _.transportModes, _.getTransportModes()),
    ("transport means", _.transportMeans, _.getTransportMeans()),
    ("nationalities", _.nationalities, _.getNationalities())
  )

  private def entry(
    key:       String,
    state:     String = "valid",
    startDate: LocalDate = today.minusDays(1),
    endDate:   Option[LocalDate] = None
  ): CodeListEntry =
    CodeListEntry(
      key = key,
      value = s"Description $key",
      state = state,
      startDate = startDate,
      endDate = endDate
    )

  "CodeListJsonService" should
    codeLists.foreach { case (name, loadedEntries, getEntries) =>

      s"return only currently valid $name sorted by key" in {
        val loader = mock(classOf[CsvCodeListLoader])

        when(loadedEntries(loader)).thenReturn(
          Future.successful(
            Seq(
              entry("3"),
              entry("2", state = "invalid", startDate = today),
              entry("4", startDate = today.plusDays(1)),
              entry("5", endDate = Some(today.minusDays(1))),
              entry("1", startDate = today),
              entry("6", state = "invalid", startDate = today.minusDays(1)),
              entry("7", endDate = Some(today))
            )
          )
        )

        val service = new CodeListJsonService(loader, fixedClock)

        getEntries(service).futureValue.map(_.key) shouldBe
          Seq("1", "3", "7")
      }

      s"return an empty list when no $name are valid" in {
        val loader = mock(classOf[CsvCodeListLoader])

        when(loadedEntries(loader)).thenReturn(
          Future.successful(Seq(entry("1", state = "invalid")))
        )

        val service = new CodeListJsonService(loader, fixedClock)

        getEntries(service).futureValue shouldBe empty
      }

      s"re-evaluate $name validity when the date changes" in {
        val loader = mock(classOf[CsvCodeListLoader])
        val clock  = mock(classOf[Clock])

        when(loadedEntries(loader)).thenReturn(
          Future.successful(
            Seq(entry("1", startDate = today.plusDays(1)))
          )
        )

        when(clock.getZone).thenReturn(ZoneOffset.UTC)
        when(clock.instant()).thenReturn(
          Instant.parse("2026-11-04T12:00:00Z")
        )

        val service = new CodeListJsonService(loader, clock)

        getEntries(service).futureValue shouldBe empty

        when(clock.instant()).thenReturn(
          Instant.parse("2026-11-05T12:00:00Z")
        )

        getEntries(service).futureValue.map(_.key) shouldBe Seq("1")
      }

      s"propagate a loading failure for $name" in {
        val loader  = mock(classOf[CsvCodeListLoader])
        val failure = new IllegalArgumentException("Invalid CSV")

        when(loadedEntries(loader))
          .thenReturn(Future.failed(failure))

        val service = new CodeListJsonService(loader, fixedClock)

        getEntries(service).failed.futureValue shouldBe failure
      }
    }
}
