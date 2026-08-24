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

import cats.data.{EitherT, NonEmptyList}
import helpers.EitherTFutureOps.{toEitherTLeft, toEitherTRight}
import helpers.XmlOps
import org.apache.pekko.util.ByteString
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.EitherValues
import play.api.http.{HttpVerbs, MimeTypes, Status as StatusValues}
import play.api.mvc.*
import play.api.test.Helpers.writeableOf_AnyContentAsEmpty
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.automatedexportsystem.controllers.SubmissionController
import uk.gov.hmrc.automatedexportsystem.controllers.actions.request.AesAuthAttr
import uk.gov.hmrc.automatedexportsystem.controllers.actions.{AesAuthAction, AesAuthRequestRefiner, XmlPayloadActionRefiner, XmlValidationActionRefiner}
import uk.gov.hmrc.automatedexportsystem.controllers.parsers.XmlBodyParsers
import uk.gov.hmrc.automatedexportsystem.errors.*
import uk.gov.hmrc.automatedexportsystem.helpers.{AllMocks, BaseSpec}
import uk.gov.hmrc.automatedexportsystem.models.IE507.*
import uk.gov.hmrc.automatedexportsystem.models.IE507.aes.SubmissionId
import uk.gov.hmrc.automatedexportsystem.models.mongo.{SubmissionResult, UpdateStatus}
import uk.gov.hmrc.automatedexportsystem.models.responses.{Submission, SubmissionSummary, SubmissionSummaryList}
import uk.gov.hmrc.automatedexportsystem.services.{AesIE507XmlValidationService, SubmissionService}
import uk.gov.hmrc.automatedexportsystem.util.IdGenerator

import java.time.LocalDateTime
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.{Elem, NodeSeq, XML}

class SubmissionControllerSpec extends BaseSpec, EitherValues, AllMocks:
  val controllerComponents: ControllerComponents = Helpers.stubControllerComponents(executionContext = ec)

  val xmlPayloadActionRefiner: XmlPayloadActionRefiner = XmlPayloadActionRefiner()

  val xmlValidationService: AesIE507XmlValidationService = mock[AesIE507XmlValidationService]

  val xmlValidationActionRefiner: XmlValidationActionRefiner[AesIE507XmlValidationService] =
    XmlValidationActionRefiner(xmlValidationService)

  val idGenerator: IdGenerator = mock[IdGenerator]

  val aesAuthAction: AesAuthAction =
    new AesAuthAction(mockAuthConnector, idGenerator)(ec, materializer):
      override def apply[T](next: Action[T]): EssentialAction =
        EssentialAction { rh =>
          next(rh.addAttr(AesAuthAttr.Eori, "GB123456789000"))
        }

  val aesAuthRequestRefiner: AesAuthRequestRefiner = new AesAuthRequestRefiner

  val xmlBodyParsers: XmlBodyParsers = XmlBodyParsers(controllerComponents.parsers)

  val submissionService: SubmissionService = mock[SubmissionService]

  val submissionController: SubmissionController =
    SubmissionController(
      controllerComponents,
      aesAuthAction,
      aesAuthRequestRefiner,
      xmlPayloadActionRefiner,
      xmlValidationActionRefiner,
      xmlBodyParsers,
      submissionService
    )

  object TestData:
    val id: UUID = UUID.fromString("6fb33641-6dc7-4a4f-adef-06238c13a317")

    val submissionId: SubmissionId = SubmissionId(id)

    val eoriNumber: EoriNumber = EoriNumber("GB123456789000")

    val dateTime: LocalDateTime = LocalDateTime.parse("2026-08-03T00:00:00")

    val submissionSummary1: SubmissionSummary =
      SubmissionSummary(
        submissionId = submissionId,
        mrn = Mrn("mrn"),
        ducr = Some(ReferenceNumberUcr("referenceNumberUcr")),
        officeOfExitCode = ReferenceNumber("referenceNumber"),
        updatedAt = dateTime,
        status = ExportOperationType.Standard
      )

    val submissionSummary2: SubmissionSummary =
      SubmissionSummary(
        submissionId = submissionId,
        mrn = Mrn("mrn"),
        ducr = None,
        officeOfExitCode = ReferenceNumber("referenceNumber"),
        updatedAt = dateTime,
        status = ExportOperationType.Standard
      )

    val submissionSummaryList: SubmissionSummaryList =
      SubmissionSummaryList(List(submissionSummary1, submissionSummary2))

    val submissionSummaryListEmpty: SubmissionSummaryList =
      SubmissionSummaryList(Nil)

    val submission: Submission =
      Submission(
        submissionId = submissionId,
        status = ExportOperationType.Standard,
        exportOperation = ExportOperation(
          exportOperationType = ExportOperationType.Standard,
          mrn = Mrn("mrn"),
          discrepanciesExist = DiscrepanciesExist(true),
          splitIndicator = SplitIndicator(true)
        ),
        customsOfficeOfExitActual = CustomsOfficeOfExitActual(
          referenceNumber = ReferenceNumber("referenceNumber")
        ),
        goodsShipment = None,
        updatedAt = dateTime
      )
  end TestData

  "SubmissionController" - {

    ".message" - {

      "should return an Action" - {

        "that returns a 202 Result" - {

          "when applied with a Request containing a valid XML body that passes IE507 request schema validation" in {
            val requestXml: Elem =
              <aes:Submission xmlns:aes="http://ecs.dgtaxud.ec">
                <ExportOperation>
                  <type>1</type>
                  <MRN>26GB0000X6524786A9</MRN>
                  <discrepanciesExist>0</discrepanciesExist>
                  <splitIndicator>0</splitIndicator>
                </ExportOperation>
                <CustomsOfficeOfExitActual>
                  <referenceNumber>GB000001</referenceNumber>
                </CustomsOfficeOfExitActual>
              </aes:Submission>

            val request: FakeRequest[NodeSeq] =
              FakeRequest(HttpVerbs.POST, "/dummy/path")
                .withHeaders("content-type" -> "application/xml")
                .withBody(requestXml)

            when(xmlValidationService.validate(requestXml)).thenReturn(EitherT(Future.successful(Right(()))))

            when(submissionService.submitMessage(any(), any(), any())).thenReturn(EitherT(Future.successful(Right(SubmissionResult.Created))))

            val result: Future[Result] = Helpers.call(submissionController.message, request)

            Helpers.status(result)         shouldBe StatusValues.ACCEPTED
            Helpers.contentType(result)    shouldBe None
            Helpers.contentAsBytes(result) shouldBe ByteString.empty
          }
        }

        "that returns a 500 Result" - {

          "when applied with a Request containing a valid XML body but XSD schema cannot be found" in {
            val requestXml: Elem =
              <element>I'm valid XML</element>

            val request: FakeRequest[NodeSeq] =
              FakeRequest()
                .withHeaders("content-type" -> "application/xml")
                .withBody(requestXml)

            val schemaError: SchemaError = SchemaError.SchemaNotFoundError("/schemas/dummy.xsd")

            when(xmlValidationService.validate(requestXml))
              .thenReturn(EitherT(Future.successful(Left(schemaError))))

            val result: Future[Result] = Helpers.call(submissionController.message, request)

            val schemaNotFoundErrorResponseXml: Elem =
              <errorResponse>
                <status>500</status>
                <code>INTERNAL_SERVER_ERROR</code>
                <message>XSD Schema not found: /schemas/dummy.xsd</message>
              </errorResponse>

            val resultContent: String = Helpers.contentAsString(result)
            val resultXml:     Elem   = XmlOps.loadXmlFromString(resultContent).value

            Helpers.status(result)      shouldBe StatusValues.INTERNAL_SERVER_ERROR
            Helpers.contentType(result) shouldBe Some(MimeTypes.XML)
            XmlOps.normalize(resultXml) shouldBe XmlOps.normalize(schemaNotFoundErrorResponseXml)
          }
        }

        "that returns a 422 Result" - {

          "when applied with a Request containing a valid XML body but XSD schema cannot be parsed" in {
            val requestXml: Elem =
              <element>I'm valid XML</element>

            val request: FakeRequest[NodeSeq] =
              FakeRequest()
                .withHeaders("content-type" -> "application/xml")
                .withBody(requestXml)

            val schemaError: SchemaError =
              SchemaError.SchemaParseError(SchemaError.XsdStructureError(1, 1, "Bad parse error"))

            when(xmlValidationService.validate(requestXml))
              .thenReturn(EitherT(Future.successful(Left(schemaError))))

            val result: Future[Result] = Helpers.call(submissionController.message, request)

            val schemaParseErrorResponseXml: Elem =
              <errorResponse>
                <status>422</status>
                <code>UNPROCESSABLE_ENTITY</code>
                <message>XSD Schema could not be parsed</message>
              </errorResponse>

            val resultContent: String = Helpers.contentAsString(result)
            val resultXml:     Elem   = XmlOps.loadXmlFromString(resultContent).value

            Helpers.status(result)      shouldBe StatusValues.UNPROCESSABLE_ENTITY
            Helpers.contentType(result) shouldBe Some(MimeTypes.XML)
            XmlOps.normalize(resultXml) shouldBe XmlOps.normalize(schemaParseErrorResponseXml)
          }
        }

        "that returns a 400 Result" - {

          "when applied with a Request containing a valid XML body that doesn't pass IE507 request schema validation" - {

            "due to an XmlSchemaValidationError" in {
              val requestXml: Elem =
                <element>I'm valid XML</element>

              val request: FakeRequest[NodeSeq] =
                FakeRequest()
                  .withHeaders("content-type" -> "application/xml")
                  .withBody(requestXml)

              val xmlFailedValidationError: XmlFailedValidationError =
                XmlFailedValidationError(
                  NonEmptyList.one(
                    XmlSchemaValidationError(1, 1, "Bad parse error")
                  )
                )

              when(xmlValidationService.validate(requestXml))
                .thenReturn(EitherT(Future.successful(Left(xmlFailedValidationError))))

              val result: Future[Result] = Helpers.call(submissionController.message, request)

              val xmlFailedValidationErrorResponseXml: Elem =
                <errorResponse>
                  <status>400</status>
                  <code>BAD_REQUEST</code>
                  <message>XML failed schema validation</message>
                  <errors>
                    <error>
                      <line>1</line>
                      <column>1</column>
                      <message>Bad parse error</message>
                    </error>
                  </errors>
                </errorResponse>

              val resultContent: String = Helpers.contentAsString(result)
              val resultXml:     Elem   = XmlOps.loadXmlFromString(resultContent).value

              Helpers.status(result)      shouldBe StatusValues.BAD_REQUEST
              Helpers.contentType(result) shouldBe Some(MimeTypes.XML)
              XmlOps.normalize(resultXml) shouldBe XmlOps.normalize(xmlFailedValidationErrorResponseXml)
            }

            "due to many XmlSchemaValidationError" in {
              val requestXml: Elem =
                <element>I'm valid XML</element>

              val request: FakeRequest[NodeSeq] =
                FakeRequest()
                  .withBody(requestXml)

              val xmlFailedValidationError: XmlFailedValidationError =
                XmlFailedValidationError(
                  NonEmptyList.of(
                    XmlSchemaValidationError(1, 1, "Bad parse error 1"),
                    XmlSchemaValidationError(2, 1, "Bad parse error 2"),
                    XmlSchemaValidationError(3, 1, "Bad parse error 3"),
                    XmlSchemaValidationError(4, 1, "Bad parse error 4"),
                    XmlSchemaValidationError(5, 1, "Bad parse error 5")
                  )
                )

              when(xmlValidationService.validate(requestXml))
                .thenReturn(EitherT(Future.successful(Left(xmlFailedValidationError))))

              val result: Future[Result] = Helpers.call(submissionController.message, request)

              val xmlFailedValidationErrorResponseXml: Elem =
                <errorResponse>
                  <status>400</status>
                  <code>BAD_REQUEST</code>
                  <message>XML failed schema validation</message>
                  <errors>
                    <error>
                      <line>1</line>
                      <column>1</column>
                      <message>Bad parse error 1</message>
                    </error>
                    <error>
                      <line>2</line>
                      <column>1</column>
                      <message>Bad parse error 2</message>
                    </error>
                    <error>
                      <line>3</line>
                      <column>1</column>
                      <message>Bad parse error 3</message>
                    </error>
                    <error>
                      <line>4</line>
                      <column>1</column>
                      <message>Bad parse error 4</message>
                    </error>
                    <error>
                      <line>5</line>
                      <column>1</column>
                      <message>Bad parse error 5</message>
                    </error>
                  </errors>
                </errorResponse>

              val resultContent: String = Helpers.contentAsString(result)
              val resultXml:     Elem   = XmlOps.loadXmlFromString(resultContent).value

              Helpers.status(result)      shouldBe StatusValues.BAD_REQUEST
              Helpers.contentType(result) shouldBe Some(MimeTypes.XML)
              XmlOps.normalize(resultXml) shouldBe XmlOps.normalize(xmlFailedValidationErrorResponseXml)
            }

            "due to parser error" in {
              val badXml: scala.xml.Elem =
                <Submission>
                 <not>bar</not>
               </Submission>
              when(xmlValidationService.validate(any[NodeSeq]))
                .thenReturn(EitherT.rightT[Future, AesError](()))

              val request: FakeRequest[NodeSeq] =
                FakeRequest(HttpVerbs.POST, "/dummy/path")
                  .withHeaders("Content-Type" -> "application/xml")
                  .withBody(badXml)

              val result: Future[Result] = Helpers.call(submissionController.message, request)

              Helpers.status(result) shouldBe BAD_REQUEST

              val bodyXml = XML.loadString(Helpers.contentAsString(result))
              (bodyXml \\ "Code").text.trim    shouldBe "INVALID_XML"
              (bodyXml \\ "Message").text.trim shouldBe "Missing required field: ExportOperation"
            }
          }
        }
      }
    }

    ".submissions" - {

      "should return an Action" - {

        "when applied with a request that contains a valid EORI" - {

          "that returns a 200 Result with a payload containing all the submissions" - {

            "when there are submissions found with that EORI" in {
              when(submissionService.getSubmissions(TestData.eoriNumber))
                .thenReturn(EitherT(Future.successful(Right(TestData.submissionSummaryList))))

              val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

              val result: Future[Result] = Helpers.call(submissionController.submissions, request)

              val submissionSummaryListXml: Elem =
                <Submissions>
                  <Submission>
                    <submissionId>
                      {TestData.submissionId.value}
                    </submissionId>
                    <mrn>mrn</mrn>
                    <ducr>referenceNumberUcr</ducr>
                    <officeOfExitCode>referenceNumber</officeOfExitCode>
                    <updatedAt>2026-08-03T00:00:00</updatedAt>
                    <status>1</status>
                  </Submission>
                  <Submission>
                    <submissionId>
                      {TestData.submissionId.value}
                    </submissionId>
                    <mrn>mrn</mrn>
                    <officeOfExitCode>referenceNumber</officeOfExitCode>
                    <updatedAt>2026-08-03T00:00:00</updatedAt>
                    <status>1</status>
                  </Submission>
                </Submissions>

              val resultContent: String = Helpers.contentAsString(result)
              val resultXml:     Elem   = XmlOps.loadXmlFromString(resultContent).value

              Helpers.status(result)      shouldBe StatusValues.OK
              Helpers.contentType(result) shouldBe Some(MimeTypes.XML)
              XmlOps.normalize(resultXml) shouldBe XmlOps.normalize(submissionSummaryListXml)
            }

            "when there are no submissions found with that EORI" in {
              when(submissionService.getSubmissions(TestData.eoriNumber))
                .thenReturn(EitherT(Future.successful(Right(TestData.submissionSummaryListEmpty))))

              val request: FakeRequest[AnyContentAsEmpty.type] =
                FakeRequest()

              val result: Future[Result] = Helpers.call(submissionController.submissions, request)

              val submissionSummaryListXml: Elem =
                <Submissions>
                </Submissions>

              val resultContent: String = Helpers.contentAsString(result)
              val resultXml:     Elem   = XmlOps.loadXmlFromString(resultContent).value

              Helpers.status(result)      shouldBe StatusValues.OK
              Helpers.contentType(result) shouldBe Some(MimeTypes.XML)
              XmlOps.normalize(resultXml) shouldBe XmlOps.normalize(submissionSummaryListXml)
            }
          }

          "that returns a 500 Result" - {

            "due to an unexpected error encountered while retrieving the submissions" in {
              when(submissionService.getSubmissions(TestData.eoriNumber))
                .thenReturn(
                  EitherT(
                    Future.successful(
                      Left(
                        SubmissionServiceError.SubmissionOperationFailure(
                          s"Submission retrieval failed for EORI: ${TestData.eoriNumber.value}"
                        )
                      )
                    )
                  )
                )

              val request: FakeRequest[AnyContentAsEmpty.type] =
                FakeRequest()

              val result: Future[Result] = Helpers.call(submissionController.submissions, request)

              val submissionRetrieveFailureXml: Elem =
                <errorResponse>
                    <status>500</status>
                    <code>INTERNAL_SERVER_ERROR</code>
                    <message>
                      Submission retrieval failed for EORI:
                      {TestData.eoriNumber.value}
                    </message>
                </errorResponse>

              val resultContent: String = Helpers.contentAsString(result)
              val resultXml:     Elem   = XmlOps.loadXmlFromString(resultContent).value

              Helpers.status(result)      shouldBe StatusValues.INTERNAL_SERVER_ERROR
              Helpers.contentType(result) shouldBe Some(MimeTypes.XML)
              XmlOps.normalize(resultXml) shouldBe XmlOps.normalize(submissionRetrieveFailureXml)
            }
          }
        }
      }
    }

    ".submission" - {

      "should return an Action" - {

        "when applied with a request that contains a valid EORI" - {

          "that returns a 200 Result with a payload containing a single submission" - {

            "when there is a submission found with that EORI and given submissionId" in {
              when(submissionService.getSubmission(TestData.eoriNumber, TestData.submissionId))
                .thenReturn(EitherT(Future.successful(Right(TestData.submission))))

              val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

              val result: Future[Result] =
                Helpers.call(submissionController.submission(TestData.submissionId.value), request)

              val submissionXml: Elem =
                <Submission>
                  <submissionId>
                    {TestData.submissionId.value}
                  </submissionId>
                  <status>1</status>
                  <ExportOperation>
                    <type>1</type>
                    <MRN>mrn</MRN>
                    <discrepanciesExist>1</discrepanciesExist>
                    <splitIndicator>1</splitIndicator>
                  </ExportOperation>
                  <CustomsOfficeOfExitActual>
                    <referenceNumber>referenceNumber</referenceNumber>
                  </CustomsOfficeOfExitActual>
                  <updatedAt>2026-08-03T00:00:00</updatedAt>
                </Submission>

              val resultContent: String = Helpers.contentAsString(result)
              val resultXml:     Elem   = XmlOps.loadXmlFromString(resultContent).value

              Helpers.status(result)      shouldBe StatusValues.OK
              Helpers.contentType(result) shouldBe Some(MimeTypes.XML)
              XmlOps.normalize(resultXml) shouldBe XmlOps.normalize(submissionXml)
            }

          }

          "that returns a 404 Result" - {

            "and there is no submission found with that EORI and given submissionId" in {
              val submissionServiceErrorMessage: String =
                s"Submission not found for EORI: ${TestData.eoriNumber.value} " +
                  s"and submissionId: ${TestData.submissionId.value}"

              when(submissionService.getSubmission(TestData.eoriNumber, TestData.submissionId))
                .thenReturn(
                  EitherT(
                    Future.successful(
                      Left(
                        SubmissionServiceError.SubmissionNotFound(submissionServiceErrorMessage)
                      )
                    )
                  )
                )

              val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

              val result: Future[Result] =
                Helpers.call(submissionController.submission(TestData.submissionId.value), request)

              val submissionNotFoundXml: Elem =
                <errorResponse>
                    <status>404</status>
                    <code>NOT_FOUND</code>
                    <message>
                      {submissionServiceErrorMessage}
                    </message>
                  </errorResponse>

              val resultContent: String = Helpers.contentAsString(result)
              val resultXml:     Elem   = XmlOps.loadXmlFromString(resultContent).value

              Helpers.status(result)      shouldBe StatusValues.NOT_FOUND
              Helpers.contentType(result) shouldBe Some(MimeTypes.XML)
              XmlOps.normalize(resultXml) shouldBe XmlOps.normalize(submissionNotFoundXml)
            }
          }

          "that returns a 500 Result" - {

            "due to an unexpected error encountered while retrieving the submissions" in {
              val submissionServiceErrorMessage: String =
                s"Submission retrieval failed for EORI: ${TestData.eoriNumber.value} " +
                  s"and submissionId: ${TestData.submissionId.value}"

              when(submissionService.getSubmission(TestData.eoriNumber, TestData.submissionId))
                .thenReturn(
                  EitherT(
                    Future.successful(
                      Left(
                        SubmissionServiceError.SubmissionOperationFailure(submissionServiceErrorMessage)
                      )
                    )
                  )
                )

              val request: FakeRequest[AnyContentAsEmpty.type] =
                FakeRequest()

              val result: Future[Result] = Helpers.call(submissionController.submission(TestData.submissionId.value), request)

              val submissionRetrieveFailureXml: Elem =
                <errorResponse>
                  <status>500</status>
                  <code>INTERNAL_SERVER_ERROR</code>
                  <message>
                    {submissionServiceErrorMessage}
                  </message>
                </errorResponse>

              val resultContent: String = Helpers.contentAsString(result)
              val resultXml:     Elem   = XmlOps.loadXmlFromString(resultContent).value

              Helpers.status(result)      shouldBe StatusValues.INTERNAL_SERVER_ERROR
              Helpers.contentType(result) shouldBe Some(MimeTypes.XML)
              XmlOps.normalize(resultXml) shouldBe XmlOps.normalize(submissionRetrieveFailureXml)
            }
          }
        }
      }
    }

    ".cancel" - {

      "should return an Action" - {

        "when applied with a request that contains a valid EORI" - {

          "that returns a 204 Result" - {

            "when there is a submission found with that EORI and submissionId" - {

              "and the submission is not cancelled yet" in {
                when(submissionService.cancelSubmission(TestData.eoriNumber, TestData.submissionId))
                  .thenReturn(UpdateStatus.Updated("cancel", 1, 1).toEitherTRight[SubmissionServiceError])

                val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

                val result: Future[Result] = Helpers.call(submissionController.cancel(TestData.id), request)

                Helpers.status(result)         shouldBe StatusValues.NO_CONTENT
                Helpers.contentType(result)    shouldBe None
                Helpers.contentAsBytes(result) shouldBe ByteString.empty
              }

              "and the submission is already cancelled" in {
                when(submissionService.cancelSubmission(TestData.eoriNumber, TestData.submissionId))
                  .thenReturn(UpdateStatus.AlreadyUpToDate("cancel", 1).toEitherTRight[SubmissionServiceError])

                val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

                val result: Future[Result] = Helpers.call(submissionController.cancel(TestData.id), request)

                Helpers.status(result)         shouldBe StatusValues.NO_CONTENT
                Helpers.contentType(result)    shouldBe None
                Helpers.contentAsBytes(result) shouldBe ByteString.empty
              }
            }
          }

          "that returns a 404 Result" - {

            "when there is no submission found with that EORI and submissionId" in {
              val error: SubmissionServiceError = SubmissionServiceError.SubmissionNotFound(
                s"Submission not found. EORI: ${TestData.eoriNumber.value}, submissionId: ${TestData.id}"
              )

              when(submissionService.cancelSubmission(TestData.eoriNumber, TestData.submissionId))
                .thenReturn(error.toEitherTLeft[UpdateStatus])

              val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

              val result: Future[Result] =
                Helpers.call(submissionController.cancel(TestData.id), request)

              val submissionUpdateFailureXml: Elem =
                <errorResponse>
                  <status>404</status>
                  <code>NOT_FOUND</code>
                  <message>{error.message}</message>
                </errorResponse>

              val resultContent: String = Helpers.contentAsString(result)
              val resultXml:     Elem   = XmlOps.loadXmlFromString(resultContent).value

              Helpers.status(result)      shouldBe StatusValues.NOT_FOUND
              Helpers.contentType(result) shouldBe Some(MimeTypes.XML)
              XmlOps.normalize(resultXml) shouldBe XmlOps.normalize(submissionUpdateFailureXml)
            }
          }

          "that returns a 500 Result" - {

            "due to an unexpected error encountered while retrieving the submissions" in {
              val error: SubmissionServiceError = SubmissionServiceError.SubmissionOperationFailure(
                s"Submission update failed. EORI: ${TestData.eoriNumber.value} " +
                  s"and submissionId: ${TestData.submissionId.value}"
              )

              when(submissionService.cancelSubmission(TestData.eoriNumber, TestData.submissionId))
                .thenReturn(error.toEitherTLeft[UpdateStatus])

              val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

              val result: Future[Result] =
                Helpers.call(submissionController.cancel(TestData.id), request)

              val submissionUpdateFailureXml: Elem =
                <errorResponse>
                  <status>500</status>
                  <code>INTERNAL_SERVER_ERROR</code>
                  <message>{error.message}</message>
                </errorResponse>

              val resultContent: String = Helpers.contentAsString(result)
              val resultXml:     Elem   = XmlOps.loadXmlFromString(resultContent).value

              Helpers.status(result)      shouldBe StatusValues.INTERNAL_SERVER_ERROR
              Helpers.contentType(result) shouldBe Some(MimeTypes.XML)
              XmlOps.normalize(resultXml) shouldBe XmlOps.normalize(submissionUpdateFailureXml)
            }
          }
        }
      }
    }
  }
