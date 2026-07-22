package uk.gov.hmrc.automatedexportsystem.models.aesRequest

import play.api.libs.json.{Format, Json}

final case class TransportDocument(
  sequenceNumber:        Option[SequenceNumber],
  transportDocumentType: Option[TransportDocumentType],
  referenceNumber:       Option[ReferenceNumber]
)

object TransportDocument:
  given mongoFormat: Format[TransportDocument] = Json.format[TransportDocument]
