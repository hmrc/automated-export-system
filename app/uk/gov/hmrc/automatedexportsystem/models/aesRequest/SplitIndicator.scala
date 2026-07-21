package uk.gov.hmrc.automatedexportsystem.models.aesRequest

import play.api.libs.json.{Format, Json}

final case class SplitIndicator(value: Boolean) extends AnyVal

object SplitIndicator:
  given mongoFormat: Format[SplitIndicator] = Json.valueFormat[SplitIndicator]
