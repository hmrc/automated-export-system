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

import play.api.libs.json.*

import java.time.Instant

object MongoFormats:
  given mongoDateInstantReads: Reads[Instant] =
    (JsPath \ "$date" \ "$numberLong")
      .read[String]
      .flatMapResult(s =>
        s.toLongOption match {
          case Some(long) => JsSuccess(Instant.ofEpochMilli(long))
          case None       => JsError("error.expected.long")
        }
      )

  given mongoDateInstantWrites: Writes[Instant] =
    (o: Instant) =>
      Json.obj(
        "$date" -> Json.obj(
          "$numberLong" -> JsString(o.toEpochMilli.toString)
        )
      )

  given mongoDateInstantFormat: Format[Instant] =
    Format(mongoDateInstantReads, mongoDateInstantWrites)
