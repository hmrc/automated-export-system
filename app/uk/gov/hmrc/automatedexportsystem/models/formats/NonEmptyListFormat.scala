package uk.gov.hmrc.automatedexportsystem.models.formats

import cats.data.NonEmptyList
import play.api.libs.json.*

object NonEmptyListFormat:
  given nonEmptyListReads[T: Reads]: Reads[NonEmptyList[T]] =
    Reads.list[T].flatMapResult {
      case head :: next => JsSuccess(NonEmptyList(head, next))
      case Nil          => JsError("error.expected.nonemptylist")
    }

  given nonEmptyListWrites[T: Writes]: Writes[NonEmptyList[T]] =
    Writes.list[T].contramap(_.toList)

  given nonEmptyListFormat[T: Format]: Format[NonEmptyList[T]] =
    Format(nonEmptyListReads, nonEmptyListWrites)
