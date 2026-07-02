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

package uk.gov.hmrc.automatedexportsystem.services

import uk.gov.hmrc.automatedexportsystem.errors.{AesError, SchemaError}
import uk.gov.hmrc.automatedexportsystem.xml.{XsdPath, XsdValidator}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

sealed abstract class XmlValidationService(xsdPath: XsdPath)(using ExecutionContext):
  private lazy val xsdValidator: Either[SchemaError, XsdValidator] =
    XsdValidator.fromXsdPath(xsdPath.path)

  def validate(xml: NodeSeq): Future[Either[AesError, Unit]] =
    Future(xsdValidator.flatMap(v => v.validate(xml)))

@Singleton
class IE507XmlValidationService @Inject() ()(using ec: ExecutionContext) extends XmlValidationService(XsdPath.XsdIE507Path)
