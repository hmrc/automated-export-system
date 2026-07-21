package uk.gov.hmrc.automatedexportsystem.models.aesRequest

import play.api.libs.json.{Format, Json}

final case class GrossMass(value: BigDecimal) extends AnyVal

object GrossMass:
  given mongoFormat: Format[GrossMass] = Json.valueFormat[GrossMass]
