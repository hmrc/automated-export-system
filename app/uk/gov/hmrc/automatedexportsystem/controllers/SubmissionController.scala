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

package uk.gov.hmrc.automatedexportsystem.controllers

import play.api.http.ContentTypes
import play.api.mvc.{Action, AnyContent, ControllerComponents, EssentialAction}
import uk.gov.hmrc.automatedexportsystem.controllers.actions.{AesAuthAction, AesAuthRequestRefiner, XmlPayloadActionRefiner, XmlValidationActionRefiner}
import uk.gov.hmrc.automatedexportsystem.controllers.parsers.XmlBodyParsers
import uk.gov.hmrc.automatedexportsystem.errors.ResponseCode
import uk.gov.hmrc.automatedexportsystem.models.aesIE507.ExportOperationType.Awaiting
import uk.gov.hmrc.automatedexportsystem.models.aesIE507.{EoriNumber, SubmissionId}
import uk.gov.hmrc.automatedexportsystem.models.responses.AesErrorResponse.toErrorResponse
import uk.gov.hmrc.automatedexportsystem.parsers.SubmissionRequestParser
import uk.gov.hmrc.automatedexportsystem.services.{AesIE507XmlValidationService, SubmissionService}
import uk.gov.hmrc.automatedexportsystem.xml.RootedXmlWriter.toXmlRoot
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

@Singleton
class SubmissionController @Inject() (
  cc:                         ControllerComponents,
  aesAuthEssentialAction:     AesAuthAction,
  aesAuthRequestRefiner:      AesAuthRequestRefiner,
  xmlPayloadActionRefiner:    XmlPayloadActionRefiner,
  xmlValidationActionRefiner: XmlValidationActionRefiner[AesIE507XmlValidationService],
  xmlBodyParsers:             XmlBodyParsers,
  submissionService:          SubmissionService
) extends BackendController(cc):
  import writeables.NodeSeqFormattedWriteables.writeableOfFormattedNodeSeq

  given ec: ExecutionContext = cc.executionContext

  private lazy val messageXmlValidatedAction: Action[NodeSeq] = {
    val composed = Action(xmlBodyParsers.utf8)
      .andThen(aesAuthRequestRefiner)
      .andThen(xmlPayloadActionRefiner)
      .andThen(xmlValidationActionRefiner)

    composed.async { request =>
      SubmissionRequestParser.fromXml(request.validatedXml) match {
        case Left(parseErr) =>
          val errorXml =
            <Error>
                <Code>INVALID_XML</Code>
                <Message>
                  {parseErr}
                </Message>
              </Error>

          Future.successful(BadRequest(errorXml).as(ContentTypes.XML))

        case Right(submissionRequest) =>
          submissionService.submitMessage(submissionRequest, Awaiting, request.eori).value.map {
            case Right(_) =>
              Accepted

            case Left(err) =>
              InternalServerError(err.toString)
          }
      }
    }
  }

  def message: EssentialAction =
    aesAuthEssentialAction(messageXmlValidatedAction)

  private lazy val submissionsByEoriAction: Action[AnyContent] =
    Action
      .andThen(aesAuthRequestRefiner)
      .async(aesAuthRequest =>
        submissionService
          .getSubmissions(aesAuthRequest.eori)
          .fold(
            error => error.toErrorResponse.toResult,
            submissionSummaryList => Status(ResponseCode.Ok.status)(submissionSummaryList.toXmlRoot)
          )
      )

  def submissions: EssentialAction =
    aesAuthEssentialAction(submissionsByEoriAction)

  private def submissionByEoriAndSubmissionIdAction(id: UUID): Action[AnyContent] =
    Action
      .andThen(aesAuthRequestRefiner)
      .async(aesAuthRequest =>
        val eoriNumber:   EoriNumber   = EoriNumber(aesAuthRequest.eori)
        val submissionId: SubmissionId = SubmissionId(id)

        submissionService
          .getSubmission(eoriNumber, submissionId)
          .fold(
            error => error.toErrorResponse.toResult,
            submission => Status(ResponseCode.Ok.status)(submission.toXmlRoot)
          )
      )

  def submission(id: UUID): EssentialAction =
    aesAuthEssentialAction(submissionByEoriAndSubmissionIdAction(id))
