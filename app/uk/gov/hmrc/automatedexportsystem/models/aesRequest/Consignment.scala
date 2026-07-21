package uk.gov.hmrc.automatedexportsystem.models.aesRequest

final case class Consignment(
  modeOfTransportAtBorder:    Option[ModeOfTransportAtBorder],
  referenceNumberUCR:         ReferenceNumberUcr,
  parentUcrId:                Option[ParentUcrId],
  transportEquipment:         List[TransportEquipment],
  seal:                       List[Seal],
  goodsReference:             List[GoodsReference],
  locationOfGoods:            LocationOfGoods,
  activeBorderTransportMeans: Option[ActiveBorderTransportMeans],
  transportDocument:          List[TransportDocument]
)
