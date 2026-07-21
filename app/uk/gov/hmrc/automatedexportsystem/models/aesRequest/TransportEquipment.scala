package uk.gov.hmrc.automatedexportsystem.models.aesRequest

final case class TransportEquipment(
  sequenceNumber:                Option[SequenceNumber],
  containerIdentificationNumber: Option[ContainerIdentificationNumber],
  numberOfSeals:                 Option[NumberOfSeals]
)
