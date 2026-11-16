package uk.gov.hmrc.automatedexportsystem.parsers

import cats.data.ValidatedNel
import cats.implicits.{catsSyntaxOption, catsSyntaxValidatedId}
import uk.gov.hmrc.automatedexportsystem.errors.StringParserError

trait StringParser[T]:
  def parse(s: String): ValidatedNel[StringParserError, T]

  def map[U](f: T => U): StringParser[U] =
    s => parse(s).map(f)

  def flatMap[U](f: T => StringParser[U]): StringParser[U] =
    s => parse(s).andThen(t => f(t).parse(s))

object StringParser:
  extension (s: String)
    def parse[T](using parser: StringParser[T]): ValidatedNel[StringParserError, T] =
      parser.parse(s)

  private[parsers] def nonEmptyParser[T](parser: StringParser[T]): StringParser[T] =
    s =>
      val trimmed: String = s.trim

      if trimmed.isEmpty then StringParserError.EmptyString.invalidNel
      else parser.parse(trimmed)

  given stringStringParser: StringParser[String] =
    nonEmptyParser(s => s.validNel)

  given intStringParser: StringParser[Int] =
    nonEmptyParser { s =>
      s.toIntOption.toValidNel(StringParserError.ParseError(s"Failed to parse `$s` to Int"))
    }

  given booleanStringParser: StringParser[Boolean] =
    def fallback(s: String): Option[Boolean] =
      s match
        case "1" => Some(true)
        case "0" => Some(false)
        case _   => None

    nonEmptyParser { s =>
      s.toBooleanOption
        .orElse(fallback(s))
        .toValidNel(StringParserError.ParseError(s"Failed to parse `$s` to Boolean"))
    }

  given optionStringParser[T](using parser: StringParser[T]): StringParser[Option[T]] =
    s =>
      val trimmed: String = s.trim

      if trimmed.isEmpty then None.validNel
      else parser.parse(trimmed).map(Some.apply)
