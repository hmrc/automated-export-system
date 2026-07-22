package uk.gov.hmrc.automatedexportsystem.models.aesRequest

import play.api.libs.json.{Format, Json}

final case class ModeOfTransportAtBorder(value: Int) extends AnyVal

object ModeOfTransportAtBorder:
  given mongoFormat: Format[ModeOfTransportAtBorder] = Json.valueFormat[ModeOfTransportAtBorder]
