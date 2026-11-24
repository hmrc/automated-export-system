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
import uk.gov.hmrc.automatedexportsystem.models.codelists.CodeListEntry

import java.io.{ByteArrayInputStream, FileNotFoundException}
import java.nio.charset.StandardCharsets.UTF_8
import java.time.format.DateTimeParseException
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.*

class CsvCodeListLoaderSpec extends AnyWordSpec with Matchers with ScalaFutures with BeforeAndAfterAll {

  private val system = ActorSystem("CsvCodeListLoaderSpec")

  private given Materializer =
    SystemMaterializer(system).materializer

  private val header =
    "Entity Name,Code List Entry Code,State,Start Date,Description"

  private val codeLists: Seq[
    (String, String, String, String, CsvCodeListLoader => Future[Seq[CodeListEntry]])
  ] = Seq(
    (
      "transport-mode.csv",
      "TransportMode",
      "TransportMode",
      "1",
      _.transportModes
    ),
    (
      "transport-means.csv",
      "TypeOfIdentificationofMeansOfTransportActive",
      "TypeOfIdentificationofMeansOfTransportActiveCode",
      "10",
      _.transportMeans
    ),
    (
      "nationality.csv",
      "Nationality",
      "CountryCode",
      "CI",
      _.nationalities
    )
  )

  private def csv(
    entityName:    String,
    codeFieldName: String,
    code:          String
  ): String =
    s"""$header
       |$entityName,$codeFieldName = $code,valid,01-11-2025,Example description
       |""".stripMargin

  private def environmentWithResources(): Environment = {
    val environment = mock(classOf[Environment])

    codeLists.foreach { case (fileName, entityName, codeFieldName, code, _) =>
      val contents = csv(entityName, codeFieldName, code)

      when(environment.resourceAsStream(s"codelists/$fileName"))
        .thenAnswer(_ => Some(new ByteArrayInputStream(contents.getBytes(UTF_8))))
    }

    environment
  }

  override def afterAll(): Unit =
    try {
      Await.result(system.terminate(), 10.seconds)
      ()
    } finally super.afterAll()

  "CsvCodeListLoader" should
    codeLists.foreach { case (fileName, entityName, codeFieldName, code, loadEntries) =>

      s"load and reuse $fileName" in {
        val environment = environmentWithResources()
        val loader      = new CsvCodeListLoader(environment)

        val first  = loadEntries(loader).futureValue
        val second = loadEntries(loader).futureValue

        first.map(_.key) shouldBe Seq(code)
        first.head.value shouldBe "Example description"
        second           shouldBe first

        verify(environment, times(1))
          .resourceAsStream(s"codelists/$fileName")
      }

      s"fail when $fileName is missing" in {
        val environment = environmentWithResources()

        when(environment.resourceAsStream(s"codelists/$fileName"))
          .thenReturn(None)

        val loader = new CsvCodeListLoader(environment)

        val failure = loadEntries(loader).failed.futureValue

        failure          shouldBe a[FileNotFoundException]
        failure.getMessage should include(s"codelists/$fileName")
      }

      s"fail when $fileName contains malformed data" in {
        val environment  = environmentWithResources()
        val malformedCsv =
          csv(entityName, codeFieldName, code)
            .replace("01-11-2025", "not-a-date")

        when(environment.resourceAsStream(s"codelists/$fileName"))
          .thenAnswer(_ => Some(new ByteArrayInputStream(malformedCsv.getBytes(UTF_8))))

        val loader = new CsvCodeListLoader(environment)

        loadEntries(loader).failed.futureValue shouldBe
          a[DateTimeParseException]
      }
    }
}
