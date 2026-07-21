package uk.gov.hmrc.automatedexportsystem.models.aesRequest

import play.api.libs.json.{Format, Json}

final case class AuthorisationNumber(value: String) extends AnyVal

object AuthorisationNumber:
  given mongoFormat: Format[AuthorisationNumber] = Json.valueFormat[AuthorisationNumber]
