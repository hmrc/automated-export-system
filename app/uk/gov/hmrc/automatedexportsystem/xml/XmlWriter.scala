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

import cats.data.NonEmptyList

import scala.xml.*

trait XmlWriter[T]:
  def write(o: T, label: String): NodeSeq

  def contramap[U](f: U => T): XmlWriter[U] =
    (o, label) => write(f(o), label)

final class RootedXmlWriter[T](writer: XmlWriter[T], rootTag: XmlRootTag[T]):
  def write(o: T): NodeSeq =
    writer.write(o, rootTag.label)

final case class XmlRootTag[T](label: String) extends AnyVal

object XmlRootTag:
  given deriveContainerTag[T, U[_]](using tag: XmlRootTag[T]): XmlRootTag[U[T]] =
    XmlRootTag(tag.label)

object RootedXmlWriter:
  extension [T](o: T)
    def toXmlRoot(using writer: RootedXmlWriter[T]): NodeSeq =
      writer.write(o)

  given deriveWriter[T](using writer: XmlWriter[T], tag: XmlRootTag[T]): RootedXmlWriter[T] =
    RootedXmlWriter(writer, tag)

object XmlWriter:
  extension [T](o: T)
    def toXml(label: String)(using writer: XmlWriter[T]): NodeSeq =
      writer.write(o, label)

  def elem(label: String, children: NodeSeq): Elem =
    elemWithScope(label, TopScope, children)

  def elemWithScope(label: String, scope: NamespaceBinding, children: NodeSeq): Elem =
    Elem(scope.prefix, label, Null, scope, false, children*)

  def optElem(label: String, children: NodeSeq): NodeSeq =
    if children.isEmpty then NodeSeq.Empty
    else elem(label, children)

  private def emptyElem(label: String): Elem =
    elem(label, NodeSeq.Empty)

  given stringWriter: XmlWriter[String] =
    (o, label) =>
      val text: String = o.trim

      if text.isEmpty then emptyElem(label)
      else elem(label, Text(text))

  given intWriter: XmlWriter[Int] =
    (o, label) => elem(label, Text(o.toString))

  given bigDecimalWriter: XmlWriter[BigDecimal] =
    (o, label) => elem(label, Text(o.toString))

  given booleanWriter: XmlWriter[Boolean] =
    (o, label) => elem(label, Text(o.toString))

  given optionWriter[T](using writer: XmlWriter[T]): XmlWriter[Option[T]] =
    (o, label) => o.fold(NodeSeq.Empty)(writer.write(_, label))

  given listWriter[T](using writer: XmlWriter[T]): XmlWriter[List[T]] =
    (o, label) => o.flatMap(writer.write(_, label))

  given nonEmptyListWriter[T](using writer: XmlWriter[List[T]]): XmlWriter[NonEmptyList[T]] =
    writer.contramap(_.toList)

  def intBasedBooleanWriter(using writer: XmlWriter[Int]): XmlWriter[Boolean] =
    writer.contramap {
      case true => 1
      case _    => 0
    }
