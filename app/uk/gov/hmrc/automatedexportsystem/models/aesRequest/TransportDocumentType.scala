package uk.gov.hmrc.automatedexportsystem.models.aesRequest

import play.api.libs.json.{Format, Json}

case class TransportDocumentType(value: Int) extends AnyVal

object TransportDocumentType:
  given mongoFormat: Format[TransportDocumentType] = Json.valueFormat[TransportDocumentType]
