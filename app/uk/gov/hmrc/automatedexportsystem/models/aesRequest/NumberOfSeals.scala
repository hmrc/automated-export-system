package uk.gov.hmrc.automatedexportsystem.models.aesRequest

import play.api.libs.json.{Format, Json}

final case class NumberOfSeals(value: Int) extends AnyVal

object NumberOfSeals:
  given mongoFormat: Format[NumberOfSeals] = Json.valueFormat[NumberOfSeals]
