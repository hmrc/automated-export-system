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

import org.apache.pekko.NotUsed
import org.apache.pekko.stream.connectors.csv.scaladsl.{CsvParsing, CsvToMap}
import org.apache.pekko.stream.scaladsl.Flow
import org.apache.pekko.util.ByteString
import uk.gov.hmrc.automatedexportsystem.models.codelists.CodeListEntry

import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.format.{DateTimeFormatter, ResolverStyle}

object CodeListCsvParser {

  private val dateFormat =
    DateTimeFormatter
      .ofPattern("dd-MM-uuuu")
      .withResolverStyle(ResolverStyle.STRICT)

  def flow(entityName: String): Flow[ByteString, CodeListEntry, NotUsed] =
    Flow[ByteString]
      .via(CsvParsing.lineScanner())
      .via(CsvToMap.toMapAsStrings(StandardCharsets.UTF_8))
      .map(row => toEntry(row, entityName))

  private def required(row: Map[String, String], column: String): String =
    row
      .get(column)
      .map(_.trim)
      .filter(_.nonEmpty)
      .getOrElse(
        throw new IllegalArgumentException(
          s"Missing or empty CSV column: $column"
        )
      )

  private def toEntry(
                       row: Map[String, String],
                       entityName: String
                     ): CodeListEntry = {
    val actualEntity = required(row, "Entity Name")

    require(
      actualEntity == entityName,
      s"Expected CSV entity '$entityName', found '$actualEntity'"
    )

    val rawCode = required(row, "Code List Entry Code")
    val parts   = rawCode.split("=", 2).map(_.trim)

    require(
      parts.length == 2 &&
        parts(0) == entityName &&
        parts(1).nonEmpty,
      s"Invalid code list entry code: $rawCode"
    )

    CodeListEntry(
      key = parts(1),
      value = required(row, "Description"),
      state = required(row, "State"),
      startDate = LocalDate.parse(
        required(row, "Start Date"),
        dateFormat
      ),
      endDate = row
        .get("End Date")
        .map(_.trim)
        .filter(_.nonEmpty)
        .map(value => LocalDate.parse(value, dateFormat))
    )
  }
}
