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

package uk.gov.hmrc.automatedexportsystem.controllers.actions

import play.api.mvc.{ActionRefiner, Result}
import uk.gov.hmrc.automatedexportsystem.models.actions.{ValidatedXmlRequest, XmlPayloadRequest}
import uk.gov.hmrc.automatedexportsystem.models.responses.AesErrorResponse
import uk.gov.hmrc.automatedexportsystem.models.responses.AesErrorResponse.toErrorResponse
import uk.gov.hmrc.automatedexportsystem.services.XmlValidationService

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

@Singleton
class XmlValidationActionRefiner[T <: XmlValidationService] @Inject() (xmlValidationService: T)(using
  protected val executionContext: ExecutionContext
) extends ActionRefiner[XmlPayloadRequest, ValidatedXmlRequest]:
  protected def refine[A](request: XmlPayloadRequest[A]): Future[Either[Result, ValidatedXmlRequest[A]]] =
    val xml: NodeSeq = request.xml

    val validationOutcome: Future[Either[Result, ValidatedXmlRequest[A]]] =
      xmlValidationService
        .validate(xml)
        .bimap(
          error => {
            val errorResponse: AesErrorResponse = error.toErrorResponse

            errorResponse.toResult
          },
          _ => ValidatedXmlRequest(xml, request)
        )
        .value

    validationOutcome
