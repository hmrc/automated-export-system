package uk.gov.hmrc.automatedexportsystem.models.aesRequest

import play.api.libs.json.{Format, Json}

final case class ContainerIdentificationNumber(value: Int) extends AnyVal

object ContainerIdentificationNumber:
  given mongoFormat: Format[ContainerIdentificationNumber] = Json.valueFormat[ContainerIdentificationNumber]
