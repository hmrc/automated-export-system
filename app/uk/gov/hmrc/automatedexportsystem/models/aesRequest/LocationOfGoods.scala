package uk.gov.hmrc.automatedexportsystem.models.aesRequest

final case class LocationOfGoods(
  typeOfLocation:            TypeOfLocation,
  qualifierOfIdentification: QualifierOfIdentification,
  authorisationNumber:       Option[AuthorisationNumber],
  additionalIdentifier:      Option[AdditionalIdentifier],
  unLocode:                  Option[UnLocode]
)
