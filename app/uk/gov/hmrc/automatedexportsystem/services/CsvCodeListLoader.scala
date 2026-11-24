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

import org.apache.pekko.stream.Materializer
import org.apache.pekko.stream.scaladsl.{Sink, StreamConverters}
import play.api.Environment
import uk.gov.hmrc.automatedexportsystem.models.codelists.CodeListEntry
import uk.gov.hmrc.automatedexportsystem.parsers.CodeListCsvParser

import java.io.FileNotFoundException
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class CsvCodeListLoader @Inject() (
  environment: Environment
)(using Materializer) {

  val transportModes: Future[Seq[CodeListEntry]] =
    load(
      "transport-mode.csv",
      "TransportMode",
      "TransportMode"
    )

  val transportMeans: Future[Seq[CodeListEntry]] =
    load(
      "transport-means.csv",
      "TypeOfIdentificationofMeansOfTransportActive",
      "TypeOfIdentificationofMeansOfTransportActiveCode"
    )

  val nationalities: Future[Seq[CodeListEntry]] =
    load(
      "nationality.csv",
      "Nationality",
      "CountryCode"
    )

  private def load(
    fileName:      String,
    entityName:    String,
    codeFieldName: String
  ): Future[Seq[CodeListEntry]] = {
    val resourcePath = s"codelists/$fileName"

    StreamConverters
      .fromInputStream(() =>
        environment
          .resourceAsStream(resourcePath)
          .getOrElse(
            throw new FileNotFoundException(
              s"Missing resource: $resourcePath"
            )
          )
      )
      .via(CodeListCsvParser.flow(entityName, codeFieldName))
      .runWith(Sink.seq)
  }
}
