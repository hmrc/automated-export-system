package uk.gov.hmrc.automatedexportsystem.models.aesRequest

final case class GoodsItem(
  declarationGoodsItemNumber: Option[DeclarationGoodsItemNumber],
  commodity:                  Commodity,
  packaging:                  List[Packaging]
)
