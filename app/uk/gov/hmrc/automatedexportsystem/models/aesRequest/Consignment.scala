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

import cats.data.NonEmptyList
import play.api.libs.json.{Format, Json}

final case class Consignment(
  modeOfTransportAtBorder:    Option[ModeOfTransportAtBorder],
  referenceNumberUCR:         ReferenceNumberUcr,
  parentUcrId:                Option[ParentUcrId],
  transportEquipment:         Option[NonEmptyList[TransportEquipment]],
  seal:                       Option[NonEmptyList[Seal]],
  goodsReference:             Option[NonEmptyList[GoodsReference]],
  locationOfGoods:            LocationOfGoods,
  activeBorderTransportMeans: Option[ActiveBorderTransportMeans],
  transportDocument:          Option[NonEmptyList[TransportDocument]]
)

object Consignment:
  import uk.gov.hmrc.automatedexportsystem.models.formats.NonEmptyListFormat.nonEmptyListFormat
  given mongoFormat: Format[Consignment] = Json.format[Consignment]
