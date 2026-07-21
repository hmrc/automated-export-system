package uk.gov.hmrc.automatedexportsystem.models.aesRequest

import play.api.libs.json.{Format, Json}

final case class UnLocode(value: String) extends AnyVal

object UnLocode:
  given mongoFormat: Format[UnLocode] = Json.valueFormat[UnLocode]
