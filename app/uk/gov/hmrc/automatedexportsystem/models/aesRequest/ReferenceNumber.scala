package uk.gov.hmrc.automatedexportsystem.models.aesRequest

import play.api.libs.json.{Format, Json}

final case class ReferenceNumber(value: String) extends AnyVal

object ReferenceNumber:
  given mongoFormat: Format[ReferenceNumber] = Json.valueFormat[ReferenceNumber]
