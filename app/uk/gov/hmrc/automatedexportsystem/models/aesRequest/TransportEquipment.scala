package uk.gov.hmrc.automatedexportsystem.models.aesRequest

import play.api.libs.json.{Format, Json}

final case class TransportEquipment(
  sequenceNumber:                Option[SequenceNumber],
  containerIdentificationNumber: Option[ContainerIdentificationNumber],
  numberOfSeals:                 Option[NumberOfSeals]
)

object TransportEquipment:
  given mongoFormat: Format[TransportEquipment] = Json.format[TransportEquipment]