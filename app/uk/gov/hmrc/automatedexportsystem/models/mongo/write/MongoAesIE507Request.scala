package uk.gov.hmrc.automatedexportsystem.models.mongo.write

import uk.gov.hmrc.automatedexportsystem.models.aesRequest.*

import java.time.Instant

case class MongoAesIE507Request(
  submissionId:              SubmissionId,
  eoriNumber:                EoriNumber,
  lastUpdated:               Instant,
  exportOperation:           ExportOperation,
  customsOfficeOfExitActual: CustomsOfficeOfExitActual,
  goodsShipment:             Option[GoodsShipment]
)
