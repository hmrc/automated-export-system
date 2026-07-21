package uk.gov.hmrc.automatedexportsystem.models.aesRequest

import play.api.libs.json.{Format, Json}

case class SubmissionId(value: String) extends AnyVal

object SubmissionId:
  given mongoFormat: Format[SubmissionId] = Json.valueFormat[SubmissionId]
