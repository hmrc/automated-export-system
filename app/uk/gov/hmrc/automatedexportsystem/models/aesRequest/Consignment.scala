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
