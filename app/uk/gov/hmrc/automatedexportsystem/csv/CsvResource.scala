package uk.gov.hmrc.automatedexportsystem.csv

enum CsvResource(val path: String):
  case GlcCodeList extends CsvResource("codelists/goods-location-code.csv")
  case Custom(override val path: String) extends CsvResource(path)
