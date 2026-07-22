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

package uk.gov.hmrc.automatedexportsystem.models.aesRequest

import play.api.libs.json.*

enum ExportOperationType(val status: Int):
  case Standard extends ExportOperationType(1)
  case Amend extends ExportOperationType(2)
  case Cancel extends ExportOperationType(3)

object ExportOperationType:
  given mongoFormat: Format[ExportOperationType] = Format(
    Reads.IntReads.flatMapResult(value =>
      ExportOperationType.values
        .find(_.status == value)
        .fold(JsError("error.expected.exportoperationtype"))(JsSuccess(_))
    ),
    Writes.IntWrites.contramap(exportOperationType => exportOperationType.status)
  )
