package uk.gov.hmrc.automatedexportsystem.models.aesRequest

final case class ActiveBorderTransportMeans(
  typeOfIdentification: Option[TypeOfIdentification],
  identificationNumber: Option[IdentificationNumber],
  nationality:          Option[Nationality]
)
