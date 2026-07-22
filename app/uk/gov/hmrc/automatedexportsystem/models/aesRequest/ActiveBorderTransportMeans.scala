package uk.gov.hmrc.automatedexportsystem.models.aesRequest

import play.api.libs.json.{Format, Json}

final case class ActiveBorderTransportMeans(
  typeOfIdentification: Option[TypeOfIdentification],
  identificationNumber: Option[IdentificationNumber],
  nationality:          Option[Nationality]
)

object ActiveBorderTransportMeans:
  given mongoFormat: Format[ActiveBorderTransportMeans] = Json.format[ActiveBorderTransportMeans]
