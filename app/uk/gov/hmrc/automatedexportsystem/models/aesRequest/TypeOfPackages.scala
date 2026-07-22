package uk.gov.hmrc.automatedexportsystem.models.aesRequest

import play.api.libs.json.{Format, Json}

final case class TypeOfPackages(value: String) extends AnyVal

object TypeOfPackages:
  given mongoFormat: Format[TypeOfPackages] = Json.valueFormat[TypeOfPackages]
