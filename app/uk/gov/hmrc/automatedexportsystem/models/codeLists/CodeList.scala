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

package uk.gov.hmrc.automatedexportsystem.models.codelists

import java.time.LocalDateTime
import java.time.Clock

sealed class CodeList(
  val name: String,
  val description: Option[String],
  val startDate: Option[LocalDateTime],
  val endDate: Option[LocalDateTime]
):
  def isValid(clock: Clock): Boolean =
    val now = LocalDateTime.now(clock)

    startDate.forall(
      date => !date.isAfter(now)
    ) &&
    endDate.forall(
      date => !date.isBefore(now)
    )

case object CL060 extends CodeList("MessageType", None, None, None)

case object CL347 extends CodeList("TypeOfLocation", None, None, None)

case object CL165 extends CodeList("Nationality", None, None, None)

case object CL018 extends CodeList("TransportMode", None, None, None)

case object CL094 extends CodeList("CustomsOfficeExit", None, None, None)
