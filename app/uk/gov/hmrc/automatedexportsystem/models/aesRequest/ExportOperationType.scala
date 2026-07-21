package uk.gov.hmrc.automatedexportsystem.models.aesRequest

import play.api.libs.json.*

enum ExportOperationType(val status: Int):
  case Standard extends ExportOperationType(1)
  case Amend extends ExportOperationType(2)
  case Cancel extends ExportOperationType(3)

object ExportOperationType:
  given mongoFormat: Format[ExportOperationType] = Format(
    Reads.IntReads.flatMapResult(value =>
      ExportOperationType.values
        .find(_.status == value)
        .fold(JsError("error.expected.exportoperationtype"))(JsSuccess(_))
    ),
    Writes.IntWrites.contramap(exportOperationType => exportOperationType.status)
  )
