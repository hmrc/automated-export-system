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

package uk.gov.hmrc.automatedexportsystem.xml

import cats.data.{NonEmptyList, Validated, ValidatedNel}
import cats.syntax.option.*
import cats.syntax.traverse.toTraverseOps
import uk.gov.hmrc.automatedexportsystem.errors.XmlReaderError

import scala.xml.NodeSeq

sealed case class XmlPath(path: List[String]):
  def \(key: String): XmlPath = XmlPath(path :+ key)

  private def ++(keys: List[String]): XmlPath = XmlPath(path ++ keys)

  def ++(path: XmlPath): XmlPath = this ++ path.path

  def read[T](xml: NodeSeq, path: XmlPath)(using reader: XmlReader[T]): ValidatedNel[XmlReaderError, T] =
    val node: NodeSeq = this.path.foldLeft(xml) { (node, key) =>
      node \ key
    }

    reader.read(node, path ++ this)

  override def toString: String = path.mkString("/", "/", "")

object XmlPath extends XmlPath(List.empty)

trait XmlReader[T]:
  def read(xml: NodeSeq, path: XmlPath): ValidatedNel[XmlReaderError, T]

  def map[U](f: T => U): XmlReader[U] =
    (xml, path) => read(xml, path).map(f)

  def flatMap[U](f: T => XmlReader[U]): XmlReader[U] =
    (xml, path) =>
      read(xml, path) match
        case Validated.Valid(a)       => f(a).read(xml, path)
        case i @ Validated.Invalid(_) => i

object XmlReader:
  extension (xml: NodeSeq)
    def as[T](using reader: XmlReader[T]): ValidatedNel[XmlReaderError, T] =
      reader.read(xml, XmlPath)

  def nonEmptyReader[T](reader: XmlReader[T]): XmlReader[T] =
    (xml, path) =>
      val text: String = xml.text.trim

      if xml.isEmpty || text.isEmpty then Validated.invalidNel(XmlReaderError.MissingOrEmpty(path.toString))
      else reader.read(xml, path)

  given stringReader: XmlReader[String] =
    nonEmptyReader((xml, _) => Validated.validNel(xml.text.trim))

  given intReader: XmlReader[Int] = nonEmptyReader { (xml, path) =>
    val text: String = xml.text.trim

    text.toIntOption.toValidNel(XmlReaderError.ParseError(path.toString, s"Failed to parse '$text' to Int"))
  }

  given booleanReader: XmlReader[Boolean] =
    def fallback(text: String): Option[Boolean] =
      text match
        case "1" => Some(true)
        case "0" => Some(false)
        case _   => None

    nonEmptyReader { (xml, path) =>
      val text: String = xml.text.trim

      text.toBooleanOption
        .orElse(fallback(text))
        .toValidNel(XmlReaderError.ParseError(path.toString, s"Failed to parse '$text' to Boolean"))
    }

  given optionReader[T](using reader: XmlReader[T]): XmlReader[Option[T]] =
    (xml, path) =>
      val text: String = xml.text.trim

      if xml.isEmpty || text.isEmpty then Validated.validNel(None)
      else reader.read(xml, path).map(Some(_))

  given listReader[T](using reader: XmlReader[T]): XmlReader[List[T]] =
    (xml, path) =>
      val seqResult: ValidatedNel[XmlReaderError, List[T]] =
        xml.zipWithIndex.toList.map { case (node, i) =>
          reader.read(node, path \ s"[$i]")
        }.sequence

      seqResult

  given nonEmptyListReader[T](using reader: XmlReader[List[T]]): XmlReader[NonEmptyList[T]] =
    reader.flatMap(l =>
      (_, path) =>
        l match
          case head :: next =>
            Validated.validNel(NonEmptyList(head, next))
          case Nil =>
            Validated.invalidNel(
              XmlReaderError.ParseError(path.toString, "Failed to parse empty list into NonEmptyList")
            )
    )
end XmlReader
