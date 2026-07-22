package uk.gov.hmrc.automatedexportsystem.models.aesRequest

import play.api.libs.json.{Format, Json}

case class CustomsOfficeOfExitActual(referenceNumber: ReferenceNumber)

object CustomsOfficeOfExitActual:
  given mongoFormat: Format[CustomsOfficeOfExitActual] = Json.format[CustomsOfficeOfExitActual]
