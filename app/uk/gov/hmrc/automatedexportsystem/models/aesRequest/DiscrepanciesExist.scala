package uk.gov.hmrc.automatedexportsystem.models.aesRequest

import play.api.libs.json.{Format, Json}

final case class DiscrepanciesExist(value: Boolean) extends AnyVal

object DiscrepanciesExist:
  given mongoFormat: Format[DiscrepanciesExist] = Json.valueFormat[DiscrepanciesExist]
