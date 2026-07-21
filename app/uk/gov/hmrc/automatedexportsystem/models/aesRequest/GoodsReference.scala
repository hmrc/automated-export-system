package uk.gov.hmrc.automatedexportsystem.models.aesRequest

final case class GoodsReference(
  sequenceNumber:             Option[SequenceNumber],
  declarationGoodsItemNumber: Option[DeclarationGoodsItemNumber]
)
