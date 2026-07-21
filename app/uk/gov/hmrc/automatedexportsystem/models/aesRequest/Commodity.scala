package uk.gov.hmrc.automatedexportsystem.models.aesRequest

import play.api.libs.json.{Format, Json}

final case class Commodity(grossMass: GrossMass, netMass: NetMass)

object Commodity:
  given mongoFormat: Format[Commodity] = Json.format[Commodity]
