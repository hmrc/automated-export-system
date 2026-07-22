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
