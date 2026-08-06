/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.automatedexportsystem.parsers.AESIE507

import java.util.UUID
import scala.util.Try
import scala.xml.{Node, NodeSeq}

object Helpers {

  def textOptDeep(xml: NodeSeq, tag: String): Option[String] =
    (xml \\ tag).headOption.map(_.text.trim).filter(_.nonEmpty)

  def textOptChild(n: Node, tag: String): Option[String] =
    (n \ tag).headOption.map(_.text.trim).filter(_.nonEmpty)

  def req[A](opt: Option[A], field: String): Either[String, A] =
    opt.toRight(s"Missing required field: $field")

  def parseOptionalInt(raw: Option[String], field: String): Either[String, Option[Int]] =
    raw match {
      case None    => Right(None)
      case Some(v) =>
        Try(v.trim.toInt).toEither.left.map(_ => s"Invalid integer for $field: $v").map(Some(_))
    }

  def parseBigDecimal(s: String): Either[String, BigDecimal] =
    Try(BigDecimal(s.trim)).toEither.left.map(_ => s"Invalid decimal: $s")

  def parseBoolean(s: String): Either[String, Boolean] =
    s.trim.toLowerCase match {
      case "true" | "1"  => Right(true)
      case "false" | "0" => Right(false)
      case other         => Left(s"Invalid boolean: $other")
    }

  def parseOptionalUuid(raw: Option[String]): Either[String, Option[UUID]] =
    raw match {
      case None    => Right(None)
      case Some(v) =>
        Try(UUID.fromString(v.trim)).toEither.left.map(_ => s"Invalid UUID: $v").map(Some(_))
    }

  def sequence[A](xs: List[Either[String, A]]): Either[String, List[A]] =
    xs.foldRight(Right(Nil): Either[String, List[A]]) { (e, acc) =>
      for {
        x <- e
        a <- acc
      } yield x :: a
    }
}
