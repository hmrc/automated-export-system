package uk.gov.hmrc.automatedexportsystem.models.aesRequest

import cats.data.NonEmptyList
import play.api.libs.json.{Format, Json}

final case class GoodsShipment(consignment: Consignment, goodsItem: Option[NonEmptyList[GoodsItem]])

object GoodsShipment:
  import uk.gov.hmrc.automatedexportsystem.models.formats.NonEmptyListFormat.nonEmptyListFormat
  given mongoFormat: Format[GoodsShipment] = Json.format[GoodsShipment]
