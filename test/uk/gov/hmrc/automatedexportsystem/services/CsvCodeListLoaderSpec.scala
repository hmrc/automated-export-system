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

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.{Materializer, SystemMaterializer}
import org.mockito.Mockito.{mock, times, verify, when}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.Environment

import java.io.{ByteArrayInputStream, FileNotFoundException}
import java.nio.charset.StandardCharsets.UTF_8
import scala.concurrent.Await
import scala.concurrent.duration.*

class CsvCodeListLoaderSpec
  extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with BeforeAndAfterAll {

  private val system = ActorSystem("CsvCodeListLoaderSpec")

  private given Materializer =
    SystemMaterializer(system).materializer

  private val resource = "codelists/transport-mode.csv"

  private val csv =
    """Entity Name,Code List Entry Code,State,Start Date,Description
      |TransportMode,TransportMode = 1,valid,04-03-2023,Sea transport
      |""".stripMargin

  override def afterAll(): Unit =
    try {
      Await.result(system.terminate(), 10.seconds)
      ()
    } finally {
      super.afterAll()
    }

  "CsvCodeListLoader" should {

    "load and reuse the parsed resource" in {
      val environment = mock(classOf[Environment])

      when(environment.resourceAsStream(resource))
        .thenAnswer(_ =>
          Some(new ByteArrayInputStream(csv.getBytes(UTF_8)))
        )

      val loader = new CsvCodeListLoader(environment)

      val first = loader.transportModes.futureValue
      val second = loader.transportModes.futureValue

      first.map(_.key) shouldBe Seq("1")
      first.head.value shouldBe "Sea transport"
      second shouldBe first

      verify(environment, times(1)).resourceAsStream(resource)
    }

    "fail when the resource is missing" in {
      val environment = mock(classOf[Environment])

      when(environment.resourceAsStream(resource))
        .thenReturn(None)

      val loader = new CsvCodeListLoader(environment)

      loader.transportModes.failed.futureValue shouldBe
        a[FileNotFoundException]
    }

    "fail when the resource contains malformed data" in {
      val environment = mock(classOf[Environment])
      val malformedCsv = csv.replace("04-03-2023", "not-a-date")

      when(environment.resourceAsStream(resource))
        .thenAnswer(_ =>
          Some(new ByteArrayInputStream(malformedCsv.getBytes(UTF_8)))
        )

      val loader = new CsvCodeListLoader(environment)

      loader.transportModes.failed.futureValue shouldBe
        a[java.time.format.DateTimeParseException]
    }
  }
}
