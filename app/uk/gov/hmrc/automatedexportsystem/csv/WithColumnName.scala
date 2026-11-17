package uk.gov.hmrc.automatedexportsystem.csv

trait WithColumnName:
  def columnName: String

object WithColumnName:
  object StringCaseInsensitiveOrdering extends Ordering[String]:
    def compare(x: String, y: String): Int =
      val xLength: Int = x.length
      val yLength: Int = y.length

      if xLength < yLength then -1
      else if xLength > yLength then 1
      else x.compareToIgnoreCase(y)

  given ordering: Ordering[WithColumnName] =
    (x, y) => StringCaseInsensitiveOrdering.compare(x.columnName, y.columnName)
