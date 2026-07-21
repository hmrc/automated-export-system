package uk.gov.hmrc.automatedexportsystem.models.aesRequest

import cats.data.NonEmptyList
import play.api.libs.json.{Format, Json}

final case class GoodsItem(
  declarationGoodsItemNumber: Option[DeclarationGoodsItemNumber],
  commodity:                  Commodity,
  packaging:                  Option[NonEmptyList[Packaging]]
)

object GoodsItem:
  import uk.gov.hmrc.automatedexportsystem.models.formats.NonEmptyListFormat.nonEmptyListFormat
  given mongoFormat: Format[GoodsItem] = Json.format[GoodsItem]
