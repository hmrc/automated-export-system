package uk.gov.hmrc.automatedexportsystem.models.aesRequest

import play.api.libs.json.{Format, Json}

final case class SequenceNumber(value: Int) extends AnyVal

object SequenceNumber:
  given mongoFormat: Format[SequenceNumber] = Json.valueFormat[SequenceNumber]
