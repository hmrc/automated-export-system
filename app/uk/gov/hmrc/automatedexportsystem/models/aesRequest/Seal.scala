package uk.gov.hmrc.automatedexportsystem.models.aesRequest

import play.api.libs.json.{Format, Json}

final case class Seal(sequenceNumber: Option[SequenceNumber], sealIdentifier: Option[SealIdentifier])

object Seal:
  given mongoFormat: Format[Seal] = Json.format[Seal]
