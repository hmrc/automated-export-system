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

import cats.data.NonEmptyList
import org.scalatest.EitherValues
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers
import uk.gov.hmrc.automatedexportsystem.errors.CsvMapperError
import uk.gov.hmrc.automatedexportsystem.models.codelists.*

class CsvRowMapperSpec extends AnyFreeSpecLike, Matchers, EitherValues:
  object TestData:
    val validGlcRowMixedCaseColumns: Map[String, String] =
      Map(
        "Location code"       -> "locationCode",
        "Location"            -> "location",
        "Description"         -> "description",
        "Office of Exit Name" -> "office of exit name",
        "Office of Exit"      -> "office of exit"
      )

    val invalidGlcRowMissingAllColumns: Map[String, String] = Map.empty

    val invalidGlcRowMissingSomeColumns: Map[String, String] =
      Map(
        "location code"       -> "locationCode",
        "Office of Exit Name" -> "office of exit name"
      )

    val invalidGlcRowFailedParsingAllColumns: Map[String, String] =
      Map(
        "location code"       -> " ",
        "Location"            -> "  ",
        "Description"         -> "  ",
        "Office of Exit Name" -> " ",
        "Office of Exit"      -> "   "
      )

    val invalidGlcRowFailedParsingSomeColumns: Map[String, String] =
      Map(
        "location code"       -> "locationCode",
        "Location"            -> "  ",
        "Description"         -> " description  ",
        "Office of Exit Name" -> " office of exit name ",
        "Office of Exit"      -> "   "
      )

  "GlcCodeListRowMapper" - {
    val glcCodeListRowMapper: GlcCodeListRowMapper = GlcCodeListRowMapper()

    ".mapRow" - {

      "should return a GlcCodeListEntry" - {

        "when the row is valid" in {
          val result: GlcCodeListEntry =
            glcCodeListRowMapper.mapRow(TestData.validGlcRowMixedCaseColumns).toEither.value

          result shouldBe GlcCodeListEntry(
            locationCode = GlcLocationCode("locationCode"),
            location = GlcLocation("location"),
            description = GlcDescription("description"),
            officeOfExitName = GlcOfficeOfExitName("office of exit name"),
            officeOfExit = GlcOfficeOfExit("office of exit")
          )
        }

        "when the row is invalid" - {

          "due to missing all columns" in {
            val result: NonEmptyList[CsvMapperError] =
              glcCodeListRowMapper
                .mapRow(TestData.invalidGlcRowMissingAllColumns)
                .toEither
                .left
                .value

            result shouldBe NonEmptyList.of(
              CsvMapperError.MissingColumn("description"),
              CsvMapperError.MissingColumn("location"),
              CsvMapperError.MissingColumn("location code"),
              CsvMapperError.MissingColumn("office of exit"),
              CsvMapperError.MissingColumn("office of exit name")
            )
          }

          "due to missing some columns" in {
            val result: NonEmptyList[CsvMapperError] =
              glcCodeListRowMapper
                .mapRow(TestData.invalidGlcRowMissingSomeColumns)
                .toEither
                .left
                .value

            result shouldBe NonEmptyList.of(
              CsvMapperError.MissingColumn("description"),
              CsvMapperError.MissingColumn("location"),
              CsvMapperError.MissingColumn("office of exit")
            )
          }

          "due to parsing errors for all columns" in {
            val result: NonEmptyList[CsvMapperError] =
              glcCodeListRowMapper
                .mapRow(TestData.invalidGlcRowFailedParsingAllColumns)
                .toEither
                .left
                .value

            result shouldBe NonEmptyList.of(
              CsvMapperError.ParseError("Value is empty", "location code"),
              CsvMapperError.ParseError("Value is empty", "location"),
              CsvMapperError.ParseError("Value is empty", "description"),
              CsvMapperError.ParseError("Value is empty", "office of exit name"),
              CsvMapperError.ParseError("Value is empty", "office of exit")
            )
          }

          "due to parsing errors for some columns" in {
            val result: NonEmptyList[CsvMapperError] =
              glcCodeListRowMapper
                .mapRow(TestData.invalidGlcRowFailedParsingSomeColumns)
                .toEither
                .left
                .value

            result shouldBe NonEmptyList.of(
              CsvMapperError.ParseError("Value is empty", "location"),
              CsvMapperError.ParseError("Value is empty", "office of exit")
            )
          }
        }
      }
    }
  }
