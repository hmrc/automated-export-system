package uk.gov.hmrc.automatedexportsystem.models.aesRequest

import play.api.libs.json.{Format, Json}

final case class TypeOfIdentification(value: String) extends AnyVal

object TypeOfIdentification:
  given mongoFormat: Format[TypeOfIdentification] = Json.valueFormat[TypeOfIdentification]
