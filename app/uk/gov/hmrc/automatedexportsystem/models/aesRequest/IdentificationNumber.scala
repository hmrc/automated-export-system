package uk.gov.hmrc.automatedexportsystem.models.aesRequest

import play.api.libs.json.{Format, Json}

final case class IdentificationNumber(value: String) extends AnyVal

object IdentificationNumber:
  given mongoRetry: Format[IdentificationNumber] = Json.valueFormat[IdentificationNumber]
