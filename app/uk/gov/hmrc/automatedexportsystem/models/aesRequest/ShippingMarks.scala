package uk.gov.hmrc.automatedexportsystem.models.aesRequest

import play.api.libs.json.{Format, Json}

final case class ShippingMarks(value: String) extends AnyVal

object ShippingMarks:
  given mongoFormat: Format[ShippingMarks] = Json.valueFormat[ShippingMarks]
