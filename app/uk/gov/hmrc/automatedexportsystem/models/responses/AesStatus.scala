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

package uk.gov.hmrc.automatedexportsystem.models.responses

import uk.gov.hmrc.automatedexportsystem.xml.XmlWriter
import uk.gov.hmrc.automatedexportsystem.xml.XmlWriter.toXml
import play.api.libs.json.*

import scala.xml.NodeSeq

sealed trait AesStatus { def value: Int }

object AesStatus {

  case object Created extends AesStatus { val value = 1 }
  case object Amended extends AesStatus { val value = 2 }
  case object Cancelled extends AesStatus { val value = 3 }
  case object Awaiting extends AesStatus { val value = 4 }
  case object Rejected extends AesStatus { val value = 5 }

  private val byValue: Map[Int, AesStatus] =
    List(Created, Amended, Cancelled, Awaiting, Rejected).map(s => s.value -> s).toMap

  given Format[AesStatus] = Format(
    Reads {
      case JsNumber(n) if n.isValidInt =>
        byValue
          .get(n.toInt)
          .map(JsSuccess(_))
          .getOrElse(JsError(s"Unknown AesStatus value: $n"))
      case JsString(s) =>
        s match
          case "Created"   => JsSuccess(Created)
          case "Amended"   => JsSuccess(Amended)
          case "Cancelled" => JsSuccess(Cancelled)
          case "Awaiting"  => JsSuccess(Awaiting)
          case "Rejected"  => JsSuccess(Rejected)
          case _           => JsError(s"Unknown AesStatus string: $s")
      case other => JsError(s"Expected AesStatus as number or string, got: $other")
    },
    Writes(status => JsNumber(status.value))
  )
}

object AesStatusXmlWriters:
  given aesStatusXmlWriter: XmlWriter[AesStatus] with
    override def write(status: AesStatus, label: String): NodeSeq =
      val value = status match
        case AesStatus.Created   => "1"
        case AesStatus.Amended   => "2"
        case AesStatus.Cancelled => "3"
        case AesStatus.Awaiting  => "4"
        case AesStatus.Rejected  => "5"
      value.toXml(label)
