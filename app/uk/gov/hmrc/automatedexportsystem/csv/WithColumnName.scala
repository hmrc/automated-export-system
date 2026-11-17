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

trait WithColumnName:
  def columnName: String

object WithColumnName:
  object StringCaseInsensitiveOrdering extends Ordering[String]:
    def compare(x: String, y: String): Int =
      val xLength: Int = x.length
      val yLength: Int = y.length

      if xLength < yLength then -1
      else if xLength > yLength then 1
      else x.compareToIgnoreCase(y)

  given ordering: Ordering[WithColumnName] =
    (x, y) => StringCaseInsensitiveOrdering.compare(x.columnName, y.columnName)
