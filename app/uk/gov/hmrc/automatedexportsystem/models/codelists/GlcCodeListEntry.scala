package uk.gov.hmrc.automatedexportsystem.models.codelists

import uk.gov.hmrc.automatedexportsystem.csv.WithColumnName
import uk.gov.hmrc.automatedexportsystem.parsers.StringParser

final case class GlcCodeListEntry(
  locationCode:     GlcLocationCode,
  location:         GlcLocation,
  description:      GlcDescription,
  officeOfExitName: GlcOfficeOfExitName,
  officeOfExit:     GlcOfficeOfExit
)

final case class GlcLocationCode(value: String) extends AnyVal

object GlcLocationCode extends WithColumnName:
  given glcLocationCodeStringParser: StringParser[GlcLocationCode] =
    StringParser.stringStringParser.map(GlcLocationCode.apply)

  val columnName: String = "location code"

final case class GlcLocation(value: String) extends AnyVal

object GlcLocation extends WithColumnName:
  given glcLocationStringParser: StringParser[GlcLocation] =
    StringParser.stringStringParser.map(GlcLocation.apply)

  val columnName: String = "location"

final case class GlcDescription(value: String) extends AnyVal

object GlcDescription extends WithColumnName:
  given glcDescriptionStringParser: StringParser[GlcDescription] =
    StringParser.stringStringParser.map(GlcDescription.apply)

  val columnName: String = "description"

final case class GlcOfficeOfExitName(value: String) extends AnyVal

object GlcOfficeOfExitName extends WithColumnName:
  given glcOfficeOfExitNameStringParser: StringParser[GlcOfficeOfExitName] =
    StringParser.stringStringParser.map(GlcOfficeOfExitName.apply)

  val columnName: String = "office of exit name"

final case class GlcOfficeOfExit(value: String) extends AnyVal

object GlcOfficeOfExit extends WithColumnName:
  given glcOfficeOfExitStringParser: StringParser[GlcOfficeOfExit] =
    StringParser.stringStringParser.map(GlcOfficeOfExit.apply)

  val columnName: String = "office of exit"
