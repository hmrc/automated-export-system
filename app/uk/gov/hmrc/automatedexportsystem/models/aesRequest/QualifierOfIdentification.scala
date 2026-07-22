package uk.gov.hmrc.automatedexportsystem.models.aesRequest

import play.api.libs.json.{Format, Json}

final case class QualifierOfIdentification(value: String) extends AnyVal

object QualifierOfIdentification:
  given mongoFormat: Format[QualifierOfIdentification] = Json.valueFormat[QualifierOfIdentification]
