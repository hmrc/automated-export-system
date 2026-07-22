package uk.gov.hmrc.automatedexportsystem.models.aesRequest

import play.api.libs.json.{Format, Json}

final case class ParentUcrId(value: String) extends AnyVal

object ParentUcrId:
  given mongoFormat: Format[ParentUcrId] = Json.valueFormat[ParentUcrId]
