package uk.gov.hmrc.automatedexportsystem.models.aesRequest

import play.api.libs.json.{Format, Json}

final case class LocationOfGoods(
  typeOfLocation:            TypeOfLocation,
  qualifierOfIdentification: QualifierOfIdentification,
  authorisationNumber:       Option[AuthorisationNumber],
  additionalIdentifier:      Option[AdditionalIdentifier],
  unLocode:                  Option[UnLocode]
)

object LocationOfGoods:
  given mongoFormat: Format[LocationOfGoods] = Json.format[LocationOfGoods]