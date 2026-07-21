package uk.gov.hmrc.automatedexportsystem.models.aesRequest

import play.api.libs.json.*

case class ExportOperation(
  exportOperationType: ExportOperationType,
  mrn:                 Mrn,
  discrepanciesExist:  DiscrepanciesExist,
  splitIndicator:      SplitIndicator
)

object ExportOperation:
  given mongoFormat: Format[ExportOperation] = Json.format[ExportOperation]
