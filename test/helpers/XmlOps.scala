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

package helpers

import java.io.StringReader
import scala.util.Using
import scala.xml.{Elem, Node, Text, XML}

/** Needed for XML normalization prior to assertion. ScalaTest will fail to acknowledge that 2 identical XMLs are equal, most likely due to the
  * complex nature of the [[scala.xml.Node]] class. The solution is to normalize the XMLs and compare their string equivalents.
  */
object XmlOps:
  private def trimKeepOneSpace(str: String): Seq[Node] =
    val stringBuilder: StringBuilder = StringBuilder()

    str.trim.foreach(c =>
      if !c.isSpaceChar
        || stringBuilder.nonEmpty && !stringBuilder.last.isSpaceChar
      then stringBuilder.append(c)
    )

    stringBuilder.result() match
      case ""  => Seq.empty
      case res => Seq(Text(res))

  private def trimSpaces(n: Node): Seq[Node] =
    n match
      case Text(text) => trimKeepOneSpace(text)
      case _          => Seq(n)

  def normalize(n: Node): Node =
    n match
      case Elem(str, str1, data, binding, child*) =>
        // technically to keep the same order of nodes, we need to foldRight, but it doesn't matter
        // because 2 identical XMLs will look the same after normalization regardless
        val childrenCombinedText: Seq[Node] = child.foldLeft(List.empty[Node]) {
          case (Text(textAcc) :: nodes, Text(textNode)) =>
            Text((textNode + textAcc).trim) :: nodes
          case (acc, node) => node :: acc
        }

        val children = childrenCombinedText.flatMap(trimSpaces).map(normalize)

        Elem(str, str1, data, binding, false, children*)
      case _ => n

  def loadXml(xmlString: String): Either[Throwable, Elem] =
    Using(StringReader(xmlString))(reader => XML.load(reader)).toEither
end XmlOps
