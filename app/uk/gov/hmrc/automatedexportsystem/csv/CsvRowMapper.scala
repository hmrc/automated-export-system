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

import cats.data.{NonEmptyList, NonEmptySet, Validated, ValidatedNel}
import cats.implicits.{catsSyntaxTuple5Semigroupal, catsSyntaxValidatedId, toTraverseOps}
import uk.gov.hmrc
import uk.gov.hmrc.automatedexportsystem
import uk.gov.hmrc.automatedexportsystem.errors.CsvMapperError
import uk.gov.hmrc.automatedexportsystem.models.codelists.*
import uk.gov.hmrc.automatedexportsystem.parsers.StringParser
import uk.gov.hmrc.automatedexportsystem.parsers.StringParser.parse

import javax.inject.{Inject, Singleton}
import scala.collection.immutable.{SortedMap, SortedSet}

sealed abstract class CsvRowMapper[T](val headerRow: NonEmptySet[WithColumnName]):
  def mapRow(row: Map[String, String]): ValidatedNel[CsvMapperError, T]

  protected def parseValue[U: StringParser](verifiedRow: VerifiedRow, column: String): ValidatedNel[CsvMapperError, U] =
    verifiedRow
      .get(column)
      .parse[U]
      .leftMap(nel => nel.map(error => CsvMapperError.ParseError(error.message, column)))

  protected final case class VerifiedRow(row: SortedMap[String, String]):
    def get(column: String): String = row(column)

@Singleton
class GlcCodeListRowMapper @Inject() extends CsvRowMapper[GlcCodeListEntry](GlcCodeListRowMapper.headers):
  def mapRow(row: Map[String, String]): ValidatedNel[CsvMapperError, GlcCodeListEntry] = {
    given stringCaseInsensitiveOrdering: Ordering[String] =
      WithColumnName.StringCaseInsensitiveOrdering

    val missingColumns: Validated[NonEmptyList[CsvMapperError], VerifiedRow] =
      headerRow
        .map(_.columnName)
        .toSortedSet
        .diff(SortedSet.from(row.keySet))
        .toList
        .traverse(col => CsvMapperError.MissingColumn(col).invalidNel)
        .map(_ => VerifiedRow(SortedMap.from(row)))

    missingColumns.andThen(verifiedRow =>
      (
        parseValue[GlcLocationCode](verifiedRow, GlcLocationCode.columnName),
        parseValue[GlcLocation](verifiedRow, GlcLocation.columnName),
        parseValue[GlcDescription](verifiedRow, GlcDescription.columnName),
        parseValue[GlcOfficeOfExitName](verifiedRow, GlcOfficeOfExitName.columnName),
        parseValue[GlcOfficeOfExit](verifiedRow, GlcOfficeOfExit.columnName)
      ).mapN(GlcCodeListEntry.apply)
    )
  }

object GlcCodeListRowMapper:
  val headers: NonEmptySet[WithColumnName] =
    NonEmptySet.fromSetUnsafe(
      SortedSet(
        GlcLocationCode,
        GlcLocation,
        GlcDescription,
        GlcOfficeOfExitName,
        GlcOfficeOfExit
      )
    )
