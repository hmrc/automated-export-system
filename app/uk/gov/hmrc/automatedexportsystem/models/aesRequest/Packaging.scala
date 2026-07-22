package uk.gov.hmrc.automatedexportsystem.models.aesRequest

import play.api.libs.json.{Format, Json}

final case class Packaging(
  sequenceNumber:   Option[SequenceNumber],
  typeOfPackages:   Option[TypeOfPackages],
  numberOfPackages: Option[NumberOfPackages],
  shippingMarks:    Option[ShippingMarks]
)

object Packaging:
  given mongoFormat: Format[Packaging] = Json.format[Packaging]