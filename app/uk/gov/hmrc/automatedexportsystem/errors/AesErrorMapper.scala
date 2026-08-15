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

package uk.gov.hmrc.automatedexportsystem.errors

class AesErrorMapper[T <: AesError, U](mappers: PartialFunction[T, U]):
  protected def withMapperAfter(mapper: PartialFunction[T, U]): PartialFunction[T, U] =
    mappers.orElse(mapper)

  protected def withMapperBefore(mapper: PartialFunction[T, U]): PartialFunction[T, U] =
    mapper.orElse(mappers)

  def apply(t: T): U =
    mappers.apply(t)
