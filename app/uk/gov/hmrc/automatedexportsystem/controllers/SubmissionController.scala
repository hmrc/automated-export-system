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

import cats.Functor
import cats.data.EitherT
import cats.syntax.bifunctor.toBifunctorOps
import play.api.Logging
import play.api.mvc.{Action, AnyContent, ControllerComponents, EssentialAction}
import uk.gov.hmrc.automatedexportsystem.controllers.actions.*
import uk.gov.hmrc.automatedexportsystem.controllers.parsers.XmlBodyParsers
import uk.gov.hmrc.automatedexportsystem.errors.{AesError, ResponseCode}
import uk.gov.hmrc.automatedexportsystem.models.IE507.aes.{AesIE507Message, SubmissionId}
import uk.gov.hmrc.automatedexportsystem.models.IE507.{CorrelationId, EoriNumber, ExportOperationType}
import uk.gov.hmrc.automatedexportsystem.models.eis.EisErrorResponse
import uk.gov.hmrc.automatedexportsystem.models.mongo.SingleUpdateStatus
import uk.gov.hmrc.automatedexportsystem.models.responses.AesErrorResponse.toErrorResponse
import uk.gov.hmrc.automatedexportsystem.services.{AesIE507XmlValidationService, EisService, SubmissionService}
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
  xmlPayloadActionRefiner:    AesXmlPayloadActionRefiner,
  xmlValidationActionRefiner: XmlValidationActionRefiner[AesIE507XmlValidationService],
  aesIE507ActionRefiner:      AesIE507ActionRefiner,
  xmlBodyParsers:             XmlBodyParsers,
  submissionService:          SubmissionService,
  eisService:                 EisService
) extends BackendController(cc)
    with Logging:

  import SubmissionController.eitherTAesErrorWiden
  import writeables.NodeSeqFormattedWriteables.writeableOfFormattedNodeSeq

  import scala.language.implicitConversions

  given ec: ExecutionContext = cc.executionContext

  private lazy val messageXmlValidatedAction: Action[NodeSeq] =
    val composed = Action(xmlBodyParsers.utf8)
      .andThen(aesAuthRequestRefiner)
      .andThen(xmlPayloadActionRefiner)
      .andThen(xmlValidationActionRefiner)
      .andThen(aesIE507ActionRefiner)

    composed.async { implicit request =>
      val aesIE507Message: AesIE507Message = request.message
      val eoriNumber:      EoriNumber      = request.eori
      val correlationId:   CorrelationId   = request.correlationId

      val result: EitherT[Future, AesError, Either[EisErrorResponse, Unit]] =
        submissionService
          .submitMessage(
            aesIE507Message,
            ExportOperationType.Standard,
            eoriNumber,
            correlationId
          )
          .flatMap { _ =>
            eisService.submitMessage(
              aesIE507Message,
              eoriNumber,
              correlationId
            )
          }

      result.fold(
        error => error.toErrorResponse.toResult,
        _.fold(
          err => Status(err.errorCode)(err.toXmlRoot),
          _ => Status(ResponseCode.Accepted.status)
        )
      )
    }
  end messageXmlValidatedAction

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
        val submissionId: SubmissionId = SubmissionId(id)

        submissionService
          .getSubmission(aesAuthRequest.eori, submissionId)
          .fold(
            error => error.toErrorResponse.toResult,
            submission => Status(ResponseCode.Ok.status)(submission.toXmlRoot)
          )
      )

  def submission(id: UUID): EssentialAction =
    aesAuthEssentialAction(submissionByEoriAndSubmissionIdAction(id))

  private def cancelBySubmissionIdAction(id: UUID) =
    Action
      .andThen(aesAuthRequestRefiner)
      .async { implicit aesAuthRequest =>
        val eoriNumber:    EoriNumber    = aesAuthRequest.eori
        val correlationId: CorrelationId = aesAuthRequest.correlationId
        val submissionId:  SubmissionId  = SubmissionId(id)

        val result: EitherT[Future, AesError, Either[EisErrorResponse, Unit]] =
          for
            cancellationMessage <- submissionService.getCancellationMessage(eoriNumber, submissionId).leftMap(error => error: AesError)
            cancellationStatus  <- submissionService.cancelSubmission(eoriNumber, submissionId, correlationId).leftMap(error => error: AesError)
            eisResult           <- (cancellationStatus match
                           case SingleUpdateStatus.Updated(_) =>
                             eisService
                               .submitMessage(cancellationMessage, eoriNumber, correlationId)
                               .leftMap(error => error: AesError)
                           case _ =>
                             EitherT.rightT[Future, AesError](Right(()): Either[EisErrorResponse, Unit])
                         ): EitherT[Future, AesError, Either[EisErrorResponse, Unit]]
          yield eisResult

        result.fold(
          error => error.toErrorResponse.toResult,
          _.fold(
            error => Status(error.errorCode)(error.toXmlRoot),
            _ => Status(ResponseCode.NoContent.status)
          )
        )
      }

  def cancel(id: UUID): EssentialAction =
    aesAuthEssentialAction(cancelBySubmissionIdAction(id))

object SubmissionController:
  given eitherTAesErrorWiden[F[_]: Functor, A <: AesError, B]: Conversion[EitherT[F, A, B], EitherT[F, AesError, B]] =
    et => et.leftWiden
