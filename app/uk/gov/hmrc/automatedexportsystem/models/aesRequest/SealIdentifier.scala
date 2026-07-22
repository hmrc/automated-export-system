package uk.gov.hmrc.automatedexportsystem.models.aesRequest

import play.api.libs.json.{Format, Json}

final case class SealIdentifier(value: String) extends AnyVal

object SealIdentifier:
  given mongoFormat: Format[SealIdentifier] = Json.valueFormat[SealIdentifier]
