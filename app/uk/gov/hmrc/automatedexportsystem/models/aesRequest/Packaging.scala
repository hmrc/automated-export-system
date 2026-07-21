package uk.gov.hmrc.automatedexportsystem.models.aesRequest

final case class Packaging(
  sequenceNumber:   Option[SequenceNumber],
  typeOfPackages:   Option[TypeOfPackages],
  numberOfPackages: Option[NumberOfPackages],
  shippingMarks:    Option[ShippingMarks]
)
