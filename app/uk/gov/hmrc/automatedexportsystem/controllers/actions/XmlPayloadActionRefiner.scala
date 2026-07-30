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

import play.api.mvc.{ActionRefiner, AnyContentAsXml, Result}
import uk.gov.hmrc.automatedexportsystem.controllers.actions.request.AesAuthRequest
import uk.gov.hmrc.automatedexportsystem.errors.RequestError
import uk.gov.hmrc.automatedexportsystem.models.actions.XmlPayloadRequest
import uk.gov.hmrc.automatedexportsystem.models.responses.AesErrorResponse
import uk.gov.hmrc.automatedexportsystem.models.responses.AesErrorResponse.toErrorResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

@Singleton
class XmlPayloadActionRefiner @Inject() ()(using protected val executionContext: ExecutionContext)
    extends ActionRefiner[AesAuthRequest, XmlPayloadRequest]:

  protected def refine[A](authRequest: AesAuthRequest[A]): Future[Either[Result, XmlPayloadRequest[A]]] =
    Future.successful(
      authRequest.body match
        case xml: NodeSeq =>
          Right(XmlPayloadRequest(xml, authRequest.request, authRequest.eori))
        case anyContentAsXml: AnyContentAsXml =>
          Right(XmlPayloadRequest(anyContentAsXml.xml, authRequest.request, authRequest.eori))
        case _ =>
          val error:         RequestError     = RequestError.ExpectedXmlBodyError
          val errorResponse: AesErrorResponse = error.toErrorResponse

          Left(errorResponse.toResult)
    )
