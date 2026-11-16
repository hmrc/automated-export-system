package uk.gov.hmrc.automatedexportsystem.parsers

import cats.data.{NonEmptyList, Validated}
import org.scalatest.EitherValues
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.{TableDrivenPropertyChecks, TableFor1, TableFor2}
import uk.gov.hmrc.automatedexportsystem.errors.StringParserError
import uk.gov.hmrc.automatedexportsystem.parsers.StringParser

class StringParserSpec extends AnyFreeSpecLike, Matchers, EitherValues, TableDrivenPropertyChecks:
  object TestData:
    val unitSuccessfulStringParser: StringParser[Unit] =
      _ => Validated.validNel(())

    val unitUnsuccessfulStringParser: StringParser[Unit] =
      s => Validated.invalidNel(StringParserError.ParseError(s"Failed to parse `${s.trim}` to Unit"))

  "StringParser" - {

    "should parse a String" - {

      "to a String" - {
        val stringParser: StringParser[String] = summon

        "successfully" in {
          val stringTable: TableFor2[String, String] =
            Table(
              "string"    -> "value",
              "   abc"    -> "abc",
              "   123   " -> "123"
            )

          forAll(stringTable) { (string, value) =>
            stringParser.parse(string).toEither.value shouldBe value
          }
        }

        "unsuccessfully" in {
          val stringTable: TableFor2[String, StringParserError] =
            Table(
              "string" -> "value",
              ""       -> StringParserError.EmptyString
            )

          forAll(stringTable) { (string, value) =>
            stringParser.parse(string).toEither.left.value shouldBe NonEmptyList.one(value)
          }
        }
      }

      "to an Int" - {
        val intParser: StringParser[Int] = summon

        "successfully" in {
          val intTable: TableFor2[String, Int] =
            Table(
              "string"   -> "value",
              "123"      -> 123,
              "   123  " -> 123
            )

          forAll(intTable) { (string, value) =>
            intParser.parse(string).toEither.value shouldBe value
          }
        }

        "unsuccessfully" in {
          val intTable: TableFor2[String, StringParserError] =
            Table(
              "string"   -> "value",
              ""         -> StringParserError.EmptyString,
              "   12e  " -> StringParserError.ParseError("Failed to parse `12e` to Int")
            )

          forAll(intTable) { (string, value) =>
            intParser.parse(string).toEither.left.value shouldBe NonEmptyList.one(value)
          }
        }
      }

      "to a Boolean" - {
        val booleanParser: StringParser[Boolean] = summon

        "successfully" in {
          val booleanTable: TableFor2[String, Boolean] =
            Table(
              "string"     -> "value",
              "true"       -> true,
              "   false  " -> false,
              "  1"        -> true,
              "0   "       -> false
            )

          forAll(booleanTable) { (string, value) =>
            booleanParser.parse(string).toEither.value shouldBe value
          }
        }

        "unsuccessfully" in {
          val booleanTable: TableFor2[String, StringParserError] =
            Table(
              "string" -> "value",
              ""       -> StringParserError.EmptyString,
              "   2 "  -> StringParserError.ParseError("Failed to parse `2` to Boolean")
            )

          forAll(booleanTable) { (string, value) =>
            booleanParser.parse(string).toEither.left.value shouldBe NonEmptyList.one(value)
          }
        }
      }
    }

    ".nonEmptyParser" - {

      "should return a StringParser that parses elements" - {
        val nonEmptyParser: StringParser[Unit] = StringParser.nonEmptyParser(TestData.unitSuccessfulStringParser)

        "successfully" - {

          "when not empty" in {
            val testTable: TableFor1[String] =
              Table(
                "string",
                "not empty",
                "  not empty  "
              )

            forAll(testTable) { string =>
              nonEmptyParser.parse(string).toEither.value shouldBe ()
            }
          }
        }

        "unsuccessfully" - {

          "when empty" in {
            val testTable: TableFor1[String] =
              Table(
                "string",
                "",
                "    "
              )

            forAll(testTable) { string =>
              nonEmptyParser.parse(string).toEither.left.value shouldBe NonEmptyList.one(
                StringParserError.EmptyString
              )
            }
          }
        }
      }
    }

    ".optionStringParser" - {

      "should return a StringParser that parses values" - {

        "successfully" - {
          val optionParser: StringParser[Option[Unit]] =
            StringParser.optionStringParser(using TestData.unitSuccessfulStringParser)

          "when not empty" in {
            val testTable: TableFor1[String] =
              Table(
                "string",
                "not empty",
                " not empty "
              )

            forAll(testTable) { string =>
              optionParser.parse(string).toEither.value shouldBe Some(())
            }
          }

          "when empty" in {
            val testTable: TableFor1[String] =
              Table(
                "string",
                "",
                "   "
              )

            forAll(testTable) { string =>
              optionParser.parse(string).toEither.value shouldBe None
            }
          }
        }

        "unsuccessfully" - {
          val optionParser: StringParser[Option[Unit]] =
            StringParser.optionStringParser(using TestData.unitUnsuccessfulStringParser)

          "when not empty" - {

            "and the underlying StringParser parses unsuccessfully" in {
              optionParser.parse("not unit").toEither.left.value shouldBe NonEmptyList.one(
                StringParserError.ParseError("Failed to parse `not unit` to Unit")
              )
            }
          }
        }
      }
    }
  }
