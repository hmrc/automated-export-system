package codeList

import java.time.LocalDateTime

trait CodeList {
  def name: String
  def description: Option[String]
  def startDate: Option[LocalDateTime]
  def endDate: Option[LocalDateTime]
}

//add function to check if valid