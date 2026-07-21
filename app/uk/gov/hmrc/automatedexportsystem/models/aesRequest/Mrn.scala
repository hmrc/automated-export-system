package uk.gov.hmrc.automatedexportsystem.models.aesRequest

import play.api.libs.json.{Format, Json}

final case class Mrn(value: String) extends AnyVal

object Mrn:
  given mongoFormat: Format[Mrn] = Json.valueFormat[Mrn]
