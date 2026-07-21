package uk.gov.hmrc.automatedexportsystem.models.aesRequest

import play.api.libs.json.{Format, Json}

final case class NetMass(value: BigDecimal) extends AnyVal

object NetMass:
  given mongoFormat: Format[NetMass] = Json.valueFormat[NetMass]