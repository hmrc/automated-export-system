package uk.gov.hmrc.automatedexportsystem.models.aesRequest

final case class TransportDocument(
  sequenceNumber:        Option[SequenceNumber],
  transportDocumentType: Option[TransportDocumentType],
  referenceNumber:       Option[ReferenceNumber]
)
