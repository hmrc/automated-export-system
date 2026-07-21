package uk.gov.hmrc.automatedexportsystem.models.aesRequest

import play.api.libs.json.{Format, Json}

final case class NumberOfPackages(value: Int) extends AnyVal

object NumberOfPackages:
  given mongoFormat: Format[NumberOfPackages] = Json.valueFormat[NumberOfPackages]