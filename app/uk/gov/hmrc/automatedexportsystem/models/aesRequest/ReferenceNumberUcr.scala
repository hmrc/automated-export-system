package uk.gov.hmrc.automatedexportsystem.models.aesRequest

import play.api.libs.json.{Format, Json}

final case class ReferenceNumberUcr(value: String) extends AnyVal

object ReferenceNumberUcr:
  given mongoFormat: Format[ReferenceNumberUcr] = Json.valueFormat[ReferenceNumberUcr]
