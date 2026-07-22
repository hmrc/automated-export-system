package uk.gov.hmrc.automatedexportsystem.models.mongo.write

import play.api.libs.json.{Format, Json, OFormat}
import uk.gov.hmrc.automatedexportsystem.models.aesRequest.*

import java.time.Instant
import java.util.UUID

case class MongoAesIE507Message(
  _id:                       UUID,
  submissionId:              SubmissionId,
  eoriNumber:                EoriNumber,
  created:                   Instant,
  lastUpdated:               Instant,
  exportOperation:           ExportOperation,
  customsOfficeOfExitActual: CustomsOfficeOfExitActual,
  goodsShipment:             Option[GoodsShipment]
)

object MongoAesIE507Message:
  val mongoFormat: Format[MongoAesIE507Message] = Json.format[MongoAesIE507Message]
