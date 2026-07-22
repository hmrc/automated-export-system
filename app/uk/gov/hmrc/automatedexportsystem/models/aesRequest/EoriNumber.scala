package uk.gov.hmrc.automatedexportsystem.models.aesRequest

import play.api.libs.json.{Format, Json}

final case class EoriNumber(value: String) extends AnyVal

object EoriNumber:
  given mongoFormat: Format[EoriNumber] = Json.valueFormat[EoriNumber]
