package uk.gov.hmrc.automatedexportsystem.models.aesRequest

case class ExportOperation(
  exportOperationType: ExportOperationType,
  mrn:                 Mrn,
  discrepanciesExist:  DiscrepanciesExist,
  splitIndicator:      SplitIndicator
)

enum ExportOperationType(val status: Int):
  case Standard extends ExportOperationType(1)
  case Amend extends ExportOperationType(2)
  case Cancel extends ExportOperationType(3)
