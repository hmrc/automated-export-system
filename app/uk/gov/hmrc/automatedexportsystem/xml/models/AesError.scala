package uk.gov.hmrc.automatedexportsystem.xml.models

trait AesError {
  def code: String
}

case object InvalidFormatNameError extends AesError {
  override def code: String = "INVALID_FORMAT_PERSON"
}

case object MissingNameError extends AesError {
  override def code: String = "MISSING_PERSON"
}

case object InvalidLengthPhoneNumberError extends AesError {
  override def code: String = "INVALID_LENGTH_MOBILE"
}

case object UnknownError extends AesError {
  override def code: String = "UNKNOWN_XML_ERROR"
}

object ValidationErrors {

  val values: List[AesError] = List(
    InvalidFormatNameError,
    MissingNameError,
    InvalidLengthPhoneNumberError
  )

  private val byCode: Map[String, AesError] = values.map(e => e.code -> e).toMap
  def fromValidationCode(code: String): AesError =
    byCode.getOrElse(code, UnknownError)
}