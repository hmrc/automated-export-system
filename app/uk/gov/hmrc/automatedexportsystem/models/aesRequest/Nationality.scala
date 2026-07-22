package uk.gov.hmrc.automatedexportsystem.models.aesRequest

import play.api.libs.json.{Format, Json}

final case class Nationality(value: String) extends AnyVal

object Nationality:
  given mongoFormat: Format[Nationality] = Json.valueFormat[Nationality]
