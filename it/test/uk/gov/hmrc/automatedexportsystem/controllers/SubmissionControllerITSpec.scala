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
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, stubFor, urlEqualTo}
import helpers.XmlOps
import org.apache.pekko.util.ByteString
import org.mockito.Mockito.when
import org.scalatest.EitherValues
import org.scalatest.EitherValues.convertEitherToValuable
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.{HeaderNames, HttpVerbs, MimeTypes, Status as StatusValues}
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.{FakeRequest, Helpers}
import play.api.{Application, inject}
import test.uk.gov.hmrc.automatedexportsystem.helpers.BaseISpec
import uk.gov.hmrc.automatedexportsystem.errors.MongoError
import uk.gov.hmrc.automatedexportsystem.models.aesIE507.*
import uk.gov.hmrc.automatedexportsystem.models.mongo.write.MongoAesIE507Message
import uk.gov.hmrc.automatedexportsystem.models.responses.{SubmissionSummary, SubmissionSummaryList}
import uk.gov.hmrc.automatedexportsystem.repositories.{AesIE507Repository, AesIE507RepositoryImpl}

import java.time.{Instant, LocalDateTime}
import java.util.UUID
import scala.concurrent.Future
import scala.xml.{Elem, NodeSeq}

class SubmissionControllerITSpec extends BaseISpec, MockitoSugar:
  val aesIE507Repository: AesIE507RepositoryImpl = app.injector.instanceOf[AesIE507RepositoryImpl]

  override def beforeEach(): Unit =
    super.beforeEach()
    await(aesIE507Repository.collection.drop().head())

  trait Setup {
    val eori: String = "GB123456789000"

    val authSuccessPayload: String =
      s"""{
        |  "allEnrolments": [
        |    {
        |      "key": "HMRC-CUS-ORG",
        |      "identifiers": [
        |        {
        |          "key": "EORINumber",
        |          "value": "$eori"
        |        }
        |      ],
        |      "state": "Activated"
        |    }
        |  ]
        |}""".stripMargin

    val id1: UUID = UUID.fromString("6fb33641-6dc7-4a4f-adef-06238c13a317")

    val id2: UUID = UUID.fromString("4b10d823-4585-4f1e-bea5-d4bbe4605d6e")

    val instant: Instant = Instant.parse("2026-08-03T00:00:00.000Z")

    val dateTime: LocalDateTime = LocalDateTime.parse("2026-08-03T00:00:00")

    val mongoAesIE507Message1: MongoAesIE507Message =
      MongoAesIE507Message(
        submissionId = SubmissionId(id1),
        eoriNumber = EoriNumber(eori),
        createdAt = instant,
        updatedAt = instant,
        exportOperation = ExportOperation(
          exportOperationType = ExportOperationType.Standard,
          mrn = Mrn("mrn"),
          discrepanciesExist = DiscrepanciesExist(false),
          splitIndicator = SplitIndicator(true)
        ),
        customsOfficeOfExitActual = CustomsOfficeOfExitActual(
          referenceNumber = ReferenceNumber("referenceNumber")
        ),
        goodsShipment = Some(
          GoodsShipment(
            consignment = Consignment(
              modeOfTransportAtBorder = Some(ModeOfTransportAtBorder(1)),
              referenceNumberUCR = ReferenceNumberUcr("referenceNumberUcr"),
              parentUcrId = Some(ParentUcrId("parentUcrId")),
              transportEquipment = Some(
                NonEmptyList.one(
                  TransportEquipment(
                    sequenceNumber = Some(SequenceNumber(1)),
                    containerIdentificationNumber = Some(ContainerIdentificationNumber("some-id")),
                    numberOfSeals = Some(NumberOfSeals(1))
                  )
                )
              ),
              seal = Some(
                NonEmptyList.one(
                  Seal(
                    sequenceNumber = Some(SequenceNumber(1)),
                    sealIdentifier = Some(SealIdentifier("sealIdentifier"))
                  )
                )
              ),
              goodsReference = Some(
                NonEmptyList.one(
                  GoodsReference(
                    sequenceNumber = Some(SequenceNumber(1)),
                    declarationGoodsItemNumber = Some(DeclarationGoodsItemNumber(1))
                  )
                )
              ),
              locationOfGoods = LocationOfGoods(
                typeOfLocation = TypeOfLocation("typeOfLocation"),
                qualifierOfIdentification = QualifierOfIdentification("qualifierIdentification"),
                authorisationNumber = Some(AuthorisationNumber("authorisationNumber")),
                additionalIdentifier = Some(AdditionalIdentifier("additionalIdentifier")),
                unLocode = Some(UnLocode("unLocode"))
              ),
              activeBorderTransportMeans = Some(
                ActiveBorderTransportMeans(
                  typeOfIdentification = Some(TypeOfIdentification("typeOfIdentification")),
                  identificationNumber = Some(IdentificationNumber("identificationNumber")),
                  nationality = Some(Nationality("nationality"))
                )
              ),
              transportDocument = Some(
                NonEmptyList.one(
                  TransportDocument(
                    sequenceNumber = Some(SequenceNumber(1)),
                    transportDocumentType = Some(TransportDocumentType(1)),
                    referenceNumber = Some(ReferenceNumber("referenceNumber"))
                  )
                )
              )
            ),
            goodsItem = Some(
              NonEmptyList.one(
                GoodsItem(
                  referenceNumberUcr = Some(ReferenceNumberUcr("ducr")),
                  declarationGoodsItemNumber = Some(DeclarationGoodsItemNumber(1)),
                  commodity = Commodity(
                    grossMass = GrossMass(100.55),
                    netMass = NetMass(80.45)
                  ),
                  packaging = Some(
                    NonEmptyList.one(
                      Packaging(
                        sequenceNumber = Some(SequenceNumber(1)),
                        typeOfPackages = Some(TypeOfPackages("typeOfPackages")),
                        numberOfPackages = Some(NumberOfPackages(1)),
                        shippingMarks = Some(ShippingMarks("shippingMarks"))
                      )
                    )
                  )
                )
              )
            )
          )
        )
      )

    val mongoAesIE507Message2: MongoAesIE507Message =
      MongoAesIE507Message(
        submissionId = SubmissionId(id2),
        eoriNumber = EoriNumber(eori),
        createdAt = instant,
        updatedAt = instant,
        exportOperation = ExportOperation(
          exportOperationType = ExportOperationType.Standard,
          mrn = Mrn("mrn"),
          discrepanciesExist = DiscrepanciesExist(false),
          splitIndicator = SplitIndicator(true)
        ),
        customsOfficeOfExitActual = CustomsOfficeOfExitActual(
          referenceNumber = ReferenceNumber("referenceNumber")
        ),
        goodsShipment = None
      )

    val submissionSummary1: SubmissionSummary =
      SubmissionSummary(
        submissionId = SubmissionId(id1),
        mrn = Mrn("mrn"),
        ducr = Some(ReferenceNumberUcr("referenceNumberUcr")),
        officeOfExitCode = ReferenceNumber("referenceNumber"),
        updatedAt = dateTime,
        status = ExportOperationType.Standard
      )

    val submissionSummary2: SubmissionSummary =
      SubmissionSummary(
        submissionId = SubmissionId(id2),
        mrn = Mrn("mrn"),
        ducr = None,
        officeOfExitCode = ReferenceNumber("referenceNumber"),
        updatedAt = dateTime,
        status = ExportOperationType.Standard
      )

    val submissionSummaryList: SubmissionSummaryList =
      SubmissionSummaryList(List(submissionSummary1, submissionSummary2))
  }

  "SubmissionController" - {

    "should handle an incoming POST request to the /message endpoint" - {

      "and return a 202 response" - {

        "when the request contains a valid AES IE507 XML body with all optional elements" in new Setup {
          val requestXml: Elem = XmlOps.loadXmlFromPath("/testdata/aesIE507RequestValid.xml").value

          stubFor(
            post(urlEqualTo("/auth/authorise"))
              .willReturn(
                aResponse()
                  .withStatus(200)
                  .withHeader("Content-Type", "application/json")
                  .withBody(authSuccessPayload)
              )
          )

          val request: FakeRequest[NodeSeq] = FakeRequest(HttpVerbs.POST, "/automated-export-system/message")
            .withHeaders(
              HeaderNames.AUTHORIZATION -> "Bearer valid-token-123",
              "X-Session-ID"            -> "some-session-id"
            )
            .withBody(requestXml)

          val result: Future[Result] = Helpers.route(app, request).value

          Helpers.status(result)         shouldBe StatusValues.ACCEPTED
          Helpers.contentType(result)    shouldBe None
          Helpers.contentAsBytes(result) shouldBe ByteString.empty
        }

        "when the request contains an valid AES IE507 XML body without optional elements" in new Setup {
          val requestXml: Elem = XmlOps.loadXmlFromPath("/testdata/aesIE507RequestValidNoOptionals.xml").value

          stubFor(
            post(urlEqualTo("/auth/authorise"))
              .willReturn(
                aResponse()
                  .withStatus(200)
                  .withHeader("Content-Type", "application/json")
                  .withBody(authSuccessPayload)
              )
          )

          val request: FakeRequest[NodeSeq] = FakeRequest(HttpVerbs.POST, "/automated-export-system/message")
            .withHeaders(
              HeaderNames.AUTHORIZATION -> "Bearer valid-token-123",
              "X-Session-ID"            -> "some-session-id"
            )
            .withBody(requestXml)

          val result: Future[Result] = Helpers.route(app, request).value

          Helpers.status(result)         shouldBe StatusValues.ACCEPTED
          Helpers.contentType(result)    shouldBe None
          Helpers.contentAsBytes(result) shouldBe ByteString.empty
        }
      }

      "and return a 400 response" - {

        "when the request contains a valid XML body that doesn't pass AES IE507 request schema validation" - {

          "due to missing required elements" in new Setup {
            val requestXml: Elem = XmlOps.loadXmlFromPath("/testdata/aesIE507RequestInvalidMissingRequired.xml").value
            stubFor(
              post(urlEqualTo("/auth/authorise"))
                .willReturn(
                  aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(authSuccessPayload)
                )
            )

            val request: FakeRequest[NodeSeq] = FakeRequest(HttpVerbs.POST, "/automated-export-system/message")
              .withHeaders(
                HeaderNames.AUTHORIZATION -> "Bearer valid-token-123",
                "X-Session-ID"            -> "some-session-id"
              )
              .withBody(requestXml)

            val xmlFailedValidationErrorResponseXml: Elem =
              <errorResponse>
                <status>400</status>
                <code>BAD_REQUEST</code>
                <message>XML failed schema validation</message>
                <errors>
                  <error>
                    <line>5</line>
                    <column>33</column>
                    <message>cvc-complex-type.2.4.a: Invalid content was found starting with element 'discrepanciesExist'. One of '{{MRN}}' is expected.</message>
                  </error>
                  <error>
                    <line>14</line>
                    <column>34</column>
                    <message>cvc-complex-type.2.4.a: Invalid content was found starting with element 'LocationOfGoods'. One of '{{referenceNumberUCR}}' is expected.</message>
                  </error>
                  <error>
                    <line>16</line>
                    <column>35</column>
                    <message>cvc-complex-type.2.4.b: The content of element 'LocationOfGoods' is not complete. One of '{{qualifierOfIdentification}}' is expected.</message>
                  </error>
                  <error>
                    <line>32</line>
                    <column>34</column>
                    <message>cvc-complex-type.2.4.a: Invalid content was found starting with element 'netMass'. One of '{{grossMass}}' is expected.</message>
                  </error>
                </errors>
              </errorResponse>

            val result:        Future[Result] = Helpers.route(app, request).value
            val resultContent: String         = Helpers.contentAsString(result)
            val resultXml:     Elem           = XmlOps.loadXmlFromString(resultContent).value

            Helpers.status(result)               shouldBe StatusValues.BAD_REQUEST
            Helpers.contentType(result)          shouldBe Some(MimeTypes.XML)
            XmlOps.normalize(resultXml).toString shouldBe XmlOps.normalize(xmlFailedValidationErrorResponseXml).toString
          }

          "due to elements not matching the required patterns" in new Setup {
            val requestXml: Elem = XmlOps.loadXmlFromPath("/testdata/aesIE507RequestInvalidBadPatterns.xml").value

            stubFor(
              post(urlEqualTo("/auth/authorise"))
                .willReturn(
                  aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(authSuccessPayload)
                )
            )

            val request: FakeRequest[NodeSeq] = FakeRequest(HttpVerbs.POST, "/automated-export-system/message")
              .withHeaders(
                HeaderNames.AUTHORIZATION -> "Bearer valid-token-123",
                "X-Session-ID"            -> "some-session-id"
              )
              .withBody(requestXml)

            val xmlFailedValidationErrorResponseXml: Elem =
              <errorResponse>
                <status>400</status>
                <code>BAD_REQUEST</code>
                <message>XML failed schema validation</message>
                <errors>
                  <error>
                    <line>2</line>
                    <column>24</column>
                    <message>cvc-pattern-valid: Value '' is not facet-valid with respect to pattern '.{{1,36}}' for type 'UK_AlphaNumeric36Type'.</message>
                  </error>
                  <error>
                    <line>2</line>
                    <column>24</column>
                    <message>cvc-type.3.1.3: The value '' of element 'submissionId' is not valid.</message>
                  </error>
                  <error>
                    <line>4</line>
                    <column>20</column>
                    <message>cvc-pattern-valid: Value '' is not facet-valid with respect to pattern '[1-3]{{1}}' for type 'UK_OneToThreeType'.</message>
                  </error>
                  <error>
                    <line>4</line>
                    <column>20</column>
                    <message>cvc-type.3.1.3: The value '' of element 'type' is not valid.</message>
                  </error>
                  <error>
                    <line>5</line>
                    <column>19</column>
                    <message>cvc-pattern-valid: Value '' is not facet-valid with respect to pattern '([2][4-9]|[3-9][0-9])[A-Z]{{2}}[A-Z0-9]{{12}}[A-E][0-9]' for type 'UK_MRNType'.</message>
                  </error>
                  <error>
                    <line>5</line>
                    <column>19</column>
                    <message>cvc-type.3.1.3: The value '' of element 'MRN' is not valid.</message>
                  </error>
                  <error>
                    <line>6</line>
                    <column>34</column>
                    <message>cvc-enumeration-valid: Value '' is not facet-valid with respect to enumeration '[0, 1]'. It must be a value from the enumeration.</message>
                  </error>
                  <error>
                    <line>6</line>
                    <column>34</column>
                    <message>cvc-type.3.1.3: The value '' of element 'discrepanciesExist' is not valid.</message>
                  </error>
                  <error>
                    <line>7</line>
                    <column>30</column>
                    <message>cvc-enumeration-valid: Value '' is not facet-valid with respect to enumeration '[0, 1]'. It must be a value from the enumeration.</message>
                  </error>
                  <error>
                    <line>7</line>
                    <column>30</column>
                    <message>cvc-type.3.1.3: The value '' of element 'splitIndicator' is not valid.</message>
                  </error>
                  <error>
                    <line>10</line>
                    <column>31</column>
                    <message>cvc-pattern-valid: Value '' is not facet-valid with respect to pattern '[A-Z]{{2}}[A-Z0-9]{{6}}' for type 'UK_ReferenceNumberType'.</message>
                  </error>
                  <error>
                    <line>10</line>
                    <column>31</column>
                    <message>cvc-type.3.1.3: The value '' of element 'referenceNumber' is not valid.</message>
                  </error>
                </errors>
              </errorResponse>

            val result:        Future[Result] = Helpers.route(app, request).value
            val resultContent: String         = Helpers.contentAsString(result)
            val resultXml:     Elem           = XmlOps.loadXmlFromString(resultContent).value

            Helpers.status(result)               shouldBe StatusValues.BAD_REQUEST
            Helpers.contentType(result)          shouldBe Some(MimeTypes.XML)
            XmlOps.normalize(resultXml).toString shouldBe XmlOps.normalize(xmlFailedValidationErrorResponseXml).toString
          }
        }
      }
    }

    "should handle an incoming GET request to the /submissions endpoint" - {

      "and return a 200 response" - {

        "when the request contains a valid EORI" - {

          "and there are submissions found with that EORI" in new Setup {
            stubFor(
              post(urlEqualTo("/auth/authorise"))
                .willReturn(
                  aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(authSuccessPayload)
                )
            )

            await(aesIE507Repository.collection.insertMany(Seq(mongoAesIE507Message1, mongoAesIE507Message2)).head())

            val request: FakeRequest[AnyContentAsEmpty.type] =
              FakeRequest(HttpVerbs.GET, "/automated-export-system/submissions")
                .withHeaders(
                  HeaderNames.AUTHORIZATION -> "Bearer valid-token-123",
                  "X-Session-ID"            -> "some-session-id"
                )

            val submissionSummaryListXml: Elem =
              <Submissions>
                <Submission>
                  <submissionId>
                    {id1}
                  </submissionId>
                  <mrn>mrn</mrn>
                  <ducr>referenceNumberUcr</ducr>
                  <officeOfExitCode>referenceNumber</officeOfExitCode>
                  <updatedAt>2026-08-03T00:00:00</updatedAt>
                  <status>1</status>
                </Submission>
                <Submission>
                  <submissionId>
                    {id2}
                  </submissionId>
                  <mrn>mrn</mrn>
                  <officeOfExitCode>referenceNumber</officeOfExitCode>
                  <updatedAt>2026-08-03T00:00:00</updatedAt>
                  <status>1</status>
                </Submission>
              </Submissions>

            val result:        Future[Result] = Helpers.route(app, request).value
            val resultContent: String         = Helpers.contentAsString(result)
            val resultXml:     Elem           = XmlOps.loadXmlFromString(resultContent).value

            Helpers.status(result)               shouldBe StatusValues.OK
            Helpers.contentType(result)          shouldBe Some(MimeTypes.XML)
            XmlOps.normalize(resultXml).toString shouldBe XmlOps.normalize(submissionSummaryListXml).toString
          }

          "and there are no submissions found with that EORI" in new Setup {
            stubFor(
              post(urlEqualTo("/auth/authorise"))
                .willReturn(
                  aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(authSuccessPayload)
                )
            )

            val request: FakeRequest[AnyContentAsEmpty.type] =
              FakeRequest(HttpVerbs.GET, "/automated-export-system/submissions")
                .withHeaders(
                  HeaderNames.AUTHORIZATION -> "Bearer valid-token-123",
                  "X-Session-ID"            -> "some-session-id"
                )

            val submissionSummaryListXml: Elem =
              <Submissions>
              </Submissions>

            val result:        Future[Result] = Helpers.route(app, request).value
            val resultContent: String         = Helpers.contentAsString(result)
            val resultXml:     Elem           = XmlOps.loadXmlFromString(resultContent).value

            Helpers.status(result)               shouldBe StatusValues.OK
            Helpers.contentType(result)          shouldBe Some(MimeTypes.XML)
            XmlOps.normalize(resultXml).toString shouldBe XmlOps.normalize(submissionSummaryListXml).toString
          }
        }
      }

      "and return a 500 response" - {

        "when the request contains a valid EORI" in new Setup {
          val aesIE507Repository: AesIE507Repository = mock[AesIE507Repository]

          val app: Application = guiceApplicationBuilder
            .overrides(
              inject.bind[AesIE507Repository].toInstance(aesIE507Repository)
            )
            .build()

          stubFor(
            post(urlEqualTo("/auth/authorise"))
              .willReturn(
                aResponse()
                  .withStatus(200)
                  .withHeader("Content-Type", "application/json")
                  .withBody(authSuccessPayload)
              )
          )

          val mongoUnexpectedError: MongoError = MongoError.UnexpectedError(RuntimeException("Unexpected error"))

          when(aesIE507Repository.getMessages(EoriNumber(eori)))
            .thenReturn(EitherT(Future.successful(Left(mongoUnexpectedError))))

          val request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(HttpVerbs.GET, "/automated-export-system/submissions")
              .withHeaders(
                HeaderNames.AUTHORIZATION -> "Bearer valid-token-123",
                "X-Session-ID"            -> "some-session-id"
              )

          val submissionRetrievalFailureXml: Elem =
            <errorResponse>
              <status>500</status>
              <code>INTERNAL_SERVER_ERROR</code>
              <message>Submission retrieval failed for EORI: GB123456789000</message>
            </errorResponse>

          Helpers.running(app) {
            val result:        Future[Result] = Helpers.route(app, request).value
            val resultContent: String         = Helpers.contentAsString(result)
            val resultXml:     Elem           = XmlOps.loadXmlFromString(resultContent).value

            Helpers.status(result)               shouldBe StatusValues.INTERNAL_SERVER_ERROR
            Helpers.contentType(result)          shouldBe Some(MimeTypes.XML)
            XmlOps.normalize(resultXml).toString shouldBe XmlOps.normalize(submissionRetrievalFailureXml).toString
          }
        }
      }
    }
  }
