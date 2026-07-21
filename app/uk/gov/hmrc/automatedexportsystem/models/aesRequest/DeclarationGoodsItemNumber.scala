package uk.gov.hmrc.automatedexportsystem.models.aesRequest

import play.api.libs.json.{Format, Json}

final case class DeclarationGoodsItemNumber(value: Int) extends AnyVal

object DeclarationGoodsItemNumber:
  given mongoFormat: Format[DeclarationGoodsItemNumber] = Json.valueFormat[DeclarationGoodsItemNumber]
