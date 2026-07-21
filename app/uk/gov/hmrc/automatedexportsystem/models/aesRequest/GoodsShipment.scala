package uk.gov.hmrc.automatedexportsystem.models.aesRequest

final case class GoodsShipment(consignment: Consignment, goodsItem: List[GoodsItem])
