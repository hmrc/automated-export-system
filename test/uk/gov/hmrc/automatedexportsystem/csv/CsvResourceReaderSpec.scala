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

package uk.gov.hmrc.automatedexportsystem.csv

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.apache.pekko.stream.scaladsl.Sink
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, EitherValues}
import org.scalatestplus.mockito.MockitoSugar
import play.api.Environment
import uk.gov.hmrc.automatedexportsystem.errors.CsvResourceReaderError

import java.io.{ByteArrayInputStream, InputStream}
import java.nio.charset.StandardCharsets
import scala.concurrent.duration.DurationInt

class CsvResourceReaderSpec extends AnyFreeSpecLike, Matchers, EitherValues, ScalaFutures, MockitoSugar, BeforeAndAfterAll:
  override given patienceConfig: PatienceConfig = PatienceConfig(1500.millis, 15.millis)

  val actorSystem: ActorSystem = ActorSystem("CsvResourceReaderSpec")

  given mat: Materializer = Materializer(actorSystem)

  val env: Environment = mock

  val csvResourceReader: CsvResourceReader = CsvResourceReader(env)

  override protected def afterAll(): Unit =
    actorSystem.terminate().futureValue
    super.afterAll()

  "CsvReader" - {

    ".read" - {

      "should return a Source" - {

        "when the csv file exists" in {
          val row1: List[String] = List("h1", "h2", "h3")
          val row2: List[String] = List("11", "12", "13")
          val row3: List[String] = List("21", "22", "23")

          val csvSeq: Seq[List[String]] = Seq(row1, row2, row3)

          val csvInputStream: InputStream =
            ByteArrayInputStream(
              csvSeq
                .map(_.mkString(","))
                .mkString("\n")
                .getBytes(StandardCharsets.UTF_8)
            )

          when(env.resourceAsStream("test.csv")).thenReturn(Some(csvInputStream))

          val result: Seq[List[String]] =
            csvResourceReader
              .read(CsvResource.Custom("test.csv"))
              .value
              .map(_.map(_.utf8String))
              .runWith(Sink.seq)
              .futureValue

          result shouldBe csvSeq
        }
      }

      "should return an error" - {

        "when the CSV file doesn't exist" in {
          when(env.resourceAsStream("test.csv")).thenReturn(None)

          val result: CsvResourceReaderError =
            csvResourceReader
              .read(CsvResource.Custom("test.csv"))
              .left
              .value

          result shouldBe CsvResourceReaderError.CsvResourceNotFound("test.csv")
        }
      }
    }
  }
