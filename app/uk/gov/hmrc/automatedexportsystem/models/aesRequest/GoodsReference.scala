package uk.gov.hmrc.automatedexportsystem.models.aesRequest

import play.api.libs.json.{Format, Json}

final case class GoodsReference(
  sequenceNumber:             Option[SequenceNumber],
  declarationGoodsItemNumber: Option[DeclarationGoodsItemNumber]
)

object GoodsReference:
  given mongoFormat: Format[GoodsReference] = Json.format[GoodsReference]
