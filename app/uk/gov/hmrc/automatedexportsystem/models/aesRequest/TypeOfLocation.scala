package uk.gov.hmrc.automatedexportsystem.models.aesRequest

import play.api.libs.json.{Format, Json}

final case class TypeOfLocation(value: String) extends AnyVal

object TypeOfLocation:
  given mongoFormat: Format[TypeOfLocation] = Json.valueFormat[TypeOfLocation]
