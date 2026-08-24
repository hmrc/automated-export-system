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

package uk.gov.hmrc.automatedexportsystem.models.IE507.aes

import cats.data.NonEmptyList
import org.scalatest.EitherValues
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers
import uk.gov.hmrc.automatedexportsystem.errors.XmlReaderError
import uk.gov.hmrc.automatedexportsystem.models.IE507.*
import uk.gov.hmrc.automatedexportsystem.xml.XmlReader.as

import java.time.LocalDateTime
import java.util.UUID
import scala.xml.Elem

class AesIE507MessageSpec extends AnyFreeSpecLike, Matchers, EitherValues:
  object TestData:
    val id: UUID = UUID.fromString("6fb33641-6dc7-4a4f-adef-06238c13a317")

    val dateTime: LocalDateTime = LocalDateTime.parse("2026-08-21T00:00:00")

    object ValidAesIE507Xml:
      val allFields: Elem =
        <Message>
          <submissionId>
            {TestData.id}
          </submissionId>
          <ExportOperation>
            <type>1</type>
            <MRN>mrn</MRN>
            <discrepanciesExist>1</discrepanciesExist>
            <splitIndicator>1</splitIndicator>
          </ExportOperation>
          <CustomsOfficeOfExitActual>
            <referenceNumber>referenceNumber</referenceNumber>
          </CustomsOfficeOfExitActual>
          <GoodsShipment>
            <Consignment>
              <modeOfTransportAtTheBorder>1</modeOfTransportAtTheBorder>
              <referenceNumberUCR>referenceNumberUcr</referenceNumberUCR>
              <parentUCRID>parentUcrId</parentUCRID>
              <TransportEquipment>
                <sequenceNumber>1</sequenceNumber>
                <containerIdentificationNumber>1</containerIdentificationNumber>
                <numberOfSeals>1</numberOfSeals>
              </TransportEquipment>
              <TransportEquipment>
                <sequenceNumber>2</sequenceNumber>
                <containerIdentificationNumber>2</containerIdentificationNumber>
                <numberOfSeals>1</numberOfSeals>
              </TransportEquipment>
              <Seal>
                <sequenceNumber>1</sequenceNumber>
                <identifier>sealIdentifier1</identifier>
              </Seal>
              <Seal>
                <sequenceNumber>2</sequenceNumber>
                <identifier>sealIdentifier2</identifier>
              </Seal>
              <GoodsReference>
                <sequenceNumber>1</sequenceNumber>
                <declarationGoodsItemNumber>1</declarationGoodsItemNumber>
              </GoodsReference>
              <GoodsReference>
                <sequenceNumber>2</sequenceNumber>
                <declarationGoodsItemNumber>2</declarationGoodsItemNumber>
              </GoodsReference>
              <LocationOfGoods>
                <typeOfLocation>typeOfLocation</typeOfLocation>
                <qualifierOfIdentification>qualifierOfIdentification</qualifierOfIdentification>
                <authorisationNumber>authorisationNumber</authorisationNumber>
                <additionalIdentifier>additionalIdentifier</additionalIdentifier>
                <UNLocode>unLocode</UNLocode>
              </LocationOfGoods>
              <ActiveBorderTransportMeans>
                <typeOfIdentification>typeOfIdentification</typeOfIdentification>
                <identificationNumber>identificationNumber</identificationNumber>
                <nationality>nationality</nationality>
              </ActiveBorderTransportMeans>
              <TransportDocument>
                <sequenceNumber>1</sequenceNumber>
                <type>1</type>
                <referenceNumber>referenceNumber1</referenceNumber>
              </TransportDocument>
              <TransportDocument>
                <sequenceNumber>2</sequenceNumber>
                <type>2</type>
                <referenceNumber>referenceNumber2</referenceNumber>
              </TransportDocument>
            </Consignment>
            <GoodsItem>
              <declarationGoodsItemNumber>1</declarationGoodsItemNumber>
              <referenceNumberUCR>referenceNumberUcr</referenceNumberUCR>
              <Commodity>
                <grossMass>100.55</grossMass>
                <netMass>80.45</netMass>
              </Commodity>
              <Packaging>
                <sequenceNumber>1</sequenceNumber>
                <typeOfPackages>typeOfPackages</typeOfPackages>
                <numberOfPackages>1</numberOfPackages>
                <shippingMarks>shippingMarks</shippingMarks>
              </Packaging>
              <Packaging>
                <sequenceNumber>2</sequenceNumber>
                <typeOfPackages>typeOfPackages</typeOfPackages>
                <numberOfPackages>1</numberOfPackages>
                <shippingMarks>shippingMarks</shippingMarks>
              </Packaging>
            </GoodsItem>
            <GoodsItem>
              <declarationGoodsItemNumber>2</declarationGoodsItemNumber>
              <referenceNumberUCR>referenceNumberUcr</referenceNumberUCR>
              <Commodity>
                <grossMass>100.55</grossMass>
                <netMass>80.45</netMass>
              </Commodity>
              <Packaging>
                <sequenceNumber>3</sequenceNumber>
                <typeOfPackages>typeOfPackages</typeOfPackages>
                <numberOfPackages>1</numberOfPackages>
                <shippingMarks>shippingMarks</shippingMarks>
              </Packaging>
              <Packaging>
                <sequenceNumber>4</sequenceNumber>
                <typeOfPackages>typeOfPackages</typeOfPackages>
                <numberOfPackages>1</numberOfPackages>
                <shippingMarks>shippingMarks</shippingMarks>
              </Packaging>
            </GoodsItem>
          </GoodsShipment>
        </Message>
      end allFields

      val noNonRootOptionals: Elem =
        <Message>
          <ExportOperation>
            <type>1</type>
            <MRN>mrn</MRN>
            <discrepanciesExist>1</discrepanciesExist>
            <splitIndicator>1</splitIndicator>
          </ExportOperation>
          <CustomsOfficeOfExitActual>
            <referenceNumber>referenceNumber</referenceNumber>
          </CustomsOfficeOfExitActual>
          <GoodsShipment>
            <Consignment>
              <referenceNumberUCR>referenceNumberUcr</referenceNumberUCR>
              <TransportEquipment></TransportEquipment>
              <TransportEquipment></TransportEquipment>
              <Seal></Seal>
              <Seal></Seal>
              <GoodsReference></GoodsReference>
              <GoodsReference></GoodsReference>
              <LocationOfGoods>
                <typeOfLocation>typeOfLocation</typeOfLocation>
                <qualifierOfIdentification>qualifierOfIdentification</qualifierOfIdentification>
              </LocationOfGoods>
              <ActiveBorderTransportMeans></ActiveBorderTransportMeans>
              <TransportDocument></TransportDocument>
              <TransportDocument></TransportDocument>
            </Consignment>
            <GoodsItem>
              <Commodity>
                <grossMass>100.55</grossMass>
                <netMass>80.45</netMass>
              </Commodity>
              <Packaging></Packaging>
              <Packaging></Packaging>
            </GoodsItem>
            <GoodsItem>
              <Commodity>
                <grossMass>100.55</grossMass>
                <netMass>80.45</netMass>
              </Commodity>
              <Packaging></Packaging>
              <Packaging></Packaging>
            </GoodsItem>
          </GoodsShipment>
        </Message>
      end noNonRootOptionals

      val noGoodsShipmentChildrenOptionals: Elem =
        <Message>
          <ExportOperation>
            <type>1</type>
            <MRN>mrn</MRN>
            <discrepanciesExist>1</discrepanciesExist>
            <splitIndicator>1</splitIndicator>
          </ExportOperation>
          <CustomsOfficeOfExitActual>
            <referenceNumber>referenceNumber</referenceNumber>
          </CustomsOfficeOfExitActual>
          <GoodsShipment>
            <Consignment>
              <referenceNumberUCR>referenceNumberUcr</referenceNumberUCR>
              <LocationOfGoods>
                <typeOfLocation>typeOfLocation</typeOfLocation>
                <qualifierOfIdentification>qualifierOfIdentification</qualifierOfIdentification>
              </LocationOfGoods>
            </Consignment>
          </GoodsShipment>
        </Message>

      val noGoodsShipment: Elem =
        <Message>
          <ExportOperation>
            <type>1</type>
            <MRN>mrn</MRN>
            <discrepanciesExist>1</discrepanciesExist>
            <splitIndicator>1</splitIndicator>
          </ExportOperation>
          <CustomsOfficeOfExitActual>
            <referenceNumber>referenceNumber</referenceNumber>
          </CustomsOfficeOfExitActual>
        </Message>
    end ValidAesIE507Xml

    object InvalidAesIE507Xml:
      val missingNonRootRequiredFields: Elem =
        <Message>
          <ExportOperation>
          </ExportOperation>
          <CustomsOfficeOfExitActual>
          </CustomsOfficeOfExitActual>
          <GoodsShipment>
            <Consignment>
              <LocationOfGoods>
              </LocationOfGoods>
            </Consignment>
            <GoodsItem>
              <Commodity>
              </Commodity>
            </GoodsItem>
          </GoodsShipment>
        </Message>
      end missingNonRootRequiredFields

      val missingNonGoodsShipmentRootRequiredFields: Elem =
        <Message>
          <GoodsShipment>
            <Consignment>
            </Consignment>
            <GoodsItem>
            </GoodsItem>
          </GoodsShipment>
        </Message>

      val missingGoodsShipmentRootRequiredFields: Elem =
        <Message>
          <GoodsShipment>
          </GoodsShipment>
        </Message>

      val unparseableFields: Elem =
        <Message>
          <submissionId>not-uuid</submissionId>
          <ExportOperation>
            <type>one</type>
            <MRN>mrn</MRN>
            <discrepanciesExist>2</discrepanciesExist>
            <splitIndicator>-1</splitIndicator>
          </ExportOperation>
          <CustomsOfficeOfExitActual>
            <referenceNumber>referenceNumber</referenceNumber>
          </CustomsOfficeOfExitActual>
          <GoodsShipment>
            <Consignment>
              <modeOfTransportAtTheBorder>two</modeOfTransportAtTheBorder>
              <referenceNumberUCR>referenceNumberUcr</referenceNumberUCR>
              <parentUCRID>parentUcrId</parentUCRID>
              <TransportEquipment>
                <sequenceNumber>one</sequenceNumber>
                <containerIdentificationNumber>one</containerIdentificationNumber>
                <numberOfSeals>one</numberOfSeals>
              </TransportEquipment>
              <TransportEquipment>
                <sequenceNumber>two</sequenceNumber>
                <containerIdentificationNumber>two</containerIdentificationNumber>
                <numberOfSeals>one</numberOfSeals>
              </TransportEquipment>
              <Seal>
                <sequenceNumber>two</sequenceNumber>
                <identifier>sealIdentifier1</identifier>
              </Seal>
              <Seal>
                <sequenceNumber>two</sequenceNumber>
                <identifier>sealIdentifier2</identifier>
              </Seal>
              <GoodsReference>
                <sequenceNumber>one</sequenceNumber>
                <declarationGoodsItemNumber>one</declarationGoodsItemNumber>
              </GoodsReference>
              <GoodsReference>
                <sequenceNumber>two</sequenceNumber>
                <declarationGoodsItemNumber>two</declarationGoodsItemNumber>
              </GoodsReference>
              <LocationOfGoods>
                <typeOfLocation>typeOfLocation</typeOfLocation>
                <qualifierOfIdentification>qualifierOfIdentification</qualifierOfIdentification>
                <authorisationNumber>authorisationNumber</authorisationNumber>
                <additionalIdentifier>additionalIdentifier</additionalIdentifier>
                <UNLocode>unLocode</UNLocode>
              </LocationOfGoods>
              <ActiveBorderTransportMeans>
                <typeOfIdentification>typeOfIdentification</typeOfIdentification>
                <identificationNumber>identificationNumber</identificationNumber>
                <nationality>nationality</nationality>
              </ActiveBorderTransportMeans>
              <TransportDocument>
                <sequenceNumber>one</sequenceNumber>
                <type>one</type>
                <referenceNumber>referenceNumber1</referenceNumber>
              </TransportDocument>
              <TransportDocument>
                <sequenceNumber>two</sequenceNumber>
                <type>two</type>
                <referenceNumber>referenceNumber2</referenceNumber>
              </TransportDocument>
            </Consignment>
            <GoodsItem>
              <declarationGoodsItemNumber>one</declarationGoodsItemNumber>
              <referenceNumberUCR>referenceNumberUcr</referenceNumberUCR>
              <Commodity>
                <grossMass>hundred</grossMass>
                <netMass>eighty</netMass>
              </Commodity>
              <Packaging>
                <sequenceNumber></sequenceNumber>
                <typeOfPackages>typeOfPackages</typeOfPackages>
                <numberOfPackages></numberOfPackages>
                <shippingMarks>shippingMarks</shippingMarks>
              </Packaging>
              <Packaging>
                <sequenceNumber></sequenceNumber>
                <typeOfPackages>typeOfPackages</typeOfPackages>
                <numberOfPackages></numberOfPackages>
                <shippingMarks>shippingMarks</shippingMarks>
              </Packaging>
            </GoodsItem>
            <GoodsItem>
              <declarationGoodsItemNumber></declarationGoodsItemNumber>
              <referenceNumberUCR>referenceNumberUcr</referenceNumberUCR>
              <Commodity>
                <grossMass></grossMass>
                <netMass></netMass>
              </Commodity>
              <Packaging>
                <sequenceNumber></sequenceNumber>
                <typeOfPackages>typeOfPackages</typeOfPackages>
                <numberOfPackages></numberOfPackages>
                <shippingMarks>shippingMarks</shippingMarks>
              </Packaging>
              <Packaging>
                <sequenceNumber></sequenceNumber>
                <typeOfPackages>typeOfPackages</typeOfPackages>
                <numberOfPackages></numberOfPackages>
                <shippingMarks>shippingMarks</shippingMarks>
              </Packaging>
            </GoodsItem>
          </GoodsShipment>
        </Message>
      end unparseableFields
    end InvalidAesIE507Xml
  end TestData

  "AesIE507Message XmlReader" - {

    "should be able to deserialize XML into a model" - {

      "when all fields are present, and lists are populated with more than one element" in {
        val aesIE507Message: AesIE507Message =
          AesIE507Message(
            submissionId = Some(SubmissionId(TestData.id)),
            exportOperation = ExportOperation(
              exportOperationType = ExportOperationType.Standard,
              mrn = Mrn("mrn"),
              discrepanciesExist = DiscrepanciesExist(true),
              splitIndicator = SplitIndicator(true)
            ),
            customsOfficeOfExitActual = CustomsOfficeOfExitActual(
              referenceNumber = ReferenceNumber("referenceNumber")
            ),
            goodsShipment = Some(
              GoodsShipment(
                consignment = Consignment(
                  modeOfTransportAtTheBorder = Some(ModeOfTransportAtTheBorder(1)),
                  referenceNumberUCR = ReferenceNumberUcr("referenceNumberUcr"),
                  parentUcrId = Some(ParentUcrId("parentUcrId")),
                  transportEquipment = Some(
                    NonEmptyList.of(
                      TransportEquipment(
                        sequenceNumber = Some(SequenceNumber(1)),
                        containerIdentificationNumber = Some(ContainerIdentificationNumber("1")),
                        numberOfSeals = Some(NumberOfSeals(1))
                      ),
                      TransportEquipment(
                        sequenceNumber = Some(SequenceNumber(2)),
                        containerIdentificationNumber = Some(ContainerIdentificationNumber("2")),
                        numberOfSeals = Some(NumberOfSeals(1))
                      )
                    )
                  ),
                  seal = Some(
                    NonEmptyList.of(
                      Seal(
                        sequenceNumber = Some(SequenceNumber(1)),
                        sealIdentifier = Some(SealIdentifier("sealIdentifier1"))
                      ),
                      Seal(
                        sequenceNumber = Some(SequenceNumber(2)),
                        sealIdentifier = Some(SealIdentifier("sealIdentifier2"))
                      )
                    )
                  ),
                  goodsReference = Some(
                    NonEmptyList.of(
                      GoodsReference(
                        sequenceNumber = Some(SequenceNumber(1)),
                        declarationGoodsItemNumber = Some(DeclarationGoodsItemNumber(1))
                      ),
                      GoodsReference(
                        sequenceNumber = Some(SequenceNumber(2)),
                        declarationGoodsItemNumber = Some(DeclarationGoodsItemNumber(2))
                      )
                    )
                  ),
                  locationOfGoods = LocationOfGoods(
                    typeOfLocation = TypeOfLocation("typeOfLocation"),
                    qualifierOfIdentification = QualifierOfIdentification("qualifierOfIdentification"),
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
                    NonEmptyList.of(
                      TransportDocument(
                        sequenceNumber = Some(SequenceNumber(1)),
                        transportDocumentType = Some(TransportDocumentType(1)),
                        referenceNumber = Some(ReferenceNumber("referenceNumber1"))
                      ),
                      TransportDocument(
                        sequenceNumber = Some(SequenceNumber(2)),
                        transportDocumentType = Some(TransportDocumentType(2)),
                        referenceNumber = Some(ReferenceNumber("referenceNumber2"))
                      )
                    )
                  )
                ),
                goodsItem = Some(
                  NonEmptyList.of(
                    GoodsItem(
                      declarationGoodsItemNumber = Some(DeclarationGoodsItemNumber(1)),
                      commodity = Commodity(
                        grossMass = GrossMass(100.55),
                        netMass = NetMass(80.45)
                      ),
                      referenceNumberUcr = Some(ReferenceNumberUcr("referenceNumberUcr")),
                      packaging = Some(
                        NonEmptyList.of(
                          Packaging(
                            sequenceNumber = Some(SequenceNumber(1)),
                            typeOfPackages = Some(TypeOfPackages("typeOfPackages")),
                            numberOfPackages = Some(NumberOfPackages(1)),
                            shippingMarks = Some(ShippingMarks("shippingMarks"))
                          ),
                          Packaging(
                            sequenceNumber = Some(SequenceNumber(2)),
                            typeOfPackages = Some(TypeOfPackages("typeOfPackages")),
                            numberOfPackages = Some(NumberOfPackages(1)),
                            shippingMarks = Some(ShippingMarks("shippingMarks"))
                          )
                        )
                      )
                    ),
                    GoodsItem(
                      declarationGoodsItemNumber = Some(DeclarationGoodsItemNumber(2)),
                      commodity = Commodity(
                        grossMass = GrossMass(100.55),
                        netMass = NetMass(80.45)
                      ),
                      referenceNumberUcr = Some(ReferenceNumberUcr("referenceNumberUcr")),
                      packaging = Some(
                        NonEmptyList.of(
                          Packaging(
                            sequenceNumber = Some(SequenceNumber(3)),
                            typeOfPackages = Some(TypeOfPackages("typeOfPackages")),
                            numberOfPackages = Some(NumberOfPackages(1)),
                            shippingMarks = Some(ShippingMarks("shippingMarks"))
                          ),
                          Packaging(
                            sequenceNumber = Some(SequenceNumber(4)),
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
        end aesIE507Message

        TestData.ValidAesIE507Xml.allFields.as[AesIE507Message].toEither.value shouldBe aesIE507Message
      }

      "when non root optional fields are missing" in {
        val aesIE507Message: AesIE507Message =
          AesIE507Message(
            submissionId = None,
            exportOperation = ExportOperation(
              exportOperationType = ExportOperationType.Standard,
              mrn = Mrn("mrn"),
              discrepanciesExist = DiscrepanciesExist(true),
              splitIndicator = SplitIndicator(true)
            ),
            customsOfficeOfExitActual = CustomsOfficeOfExitActual(
              referenceNumber = ReferenceNumber("referenceNumber")
            ),
            goodsShipment = Some(
              GoodsShipment(
                consignment = Consignment(
                  modeOfTransportAtTheBorder = None,
                  referenceNumberUCR = ReferenceNumberUcr("referenceNumberUcr"),
                  parentUcrId = None,
                  transportEquipment = Some(
                    NonEmptyList.of(
                      TransportEquipment(
                        sequenceNumber = None,
                        containerIdentificationNumber = None,
                        numberOfSeals = None
                      ),
                      TransportEquipment(
                        sequenceNumber = None,
                        containerIdentificationNumber = None,
                        numberOfSeals = None
                      )
                    )
                  ),
                  seal = Some(
                    NonEmptyList.of(
                      Seal(
                        sequenceNumber = None,
                        sealIdentifier = None
                      ),
                      Seal(
                        sequenceNumber = None,
                        sealIdentifier = None
                      )
                    )
                  ),
                  goodsReference = Some(
                    NonEmptyList.of(
                      GoodsReference(
                        sequenceNumber = None,
                        declarationGoodsItemNumber = None
                      ),
                      GoodsReference(
                        sequenceNumber = None,
                        declarationGoodsItemNumber = None
                      )
                    )
                  ),
                  locationOfGoods = LocationOfGoods(
                    typeOfLocation = TypeOfLocation("typeOfLocation"),
                    qualifierOfIdentification = QualifierOfIdentification("qualifierOfIdentification"),
                    authorisationNumber = None,
                    additionalIdentifier = None,
                    unLocode = None
                  ),
                  activeBorderTransportMeans = Some(
                    ActiveBorderTransportMeans(
                      typeOfIdentification = None,
                      identificationNumber = None,
                      nationality = None
                    )
                  ),
                  transportDocument = Some(
                    NonEmptyList.of(
                      TransportDocument(
                        sequenceNumber = None,
                        transportDocumentType = None,
                        referenceNumber = None
                      ),
                      TransportDocument(
                        sequenceNumber = None,
                        transportDocumentType = None,
                        referenceNumber = None
                      )
                    )
                  )
                ),
                goodsItem = Some(
                  NonEmptyList.of(
                    GoodsItem(
                      declarationGoodsItemNumber = None,
                      commodity = Commodity(
                        grossMass = GrossMass(100.55),
                        netMass = NetMass(80.45)
                      ),
                      referenceNumberUcr = None,
                      packaging = Some(
                        NonEmptyList.of(
                          Packaging(
                            sequenceNumber = None,
                            typeOfPackages = None,
                            numberOfPackages = None,
                            shippingMarks = None
                          ),
                          Packaging(
                            sequenceNumber = None,
                            typeOfPackages = None,
                            numberOfPackages = None,
                            shippingMarks = None
                          )
                        )
                      )
                    ),
                    GoodsItem(
                      declarationGoodsItemNumber = None,
                      commodity = Commodity(
                        grossMass = GrossMass(100.55),
                        netMass = NetMass(80.45)
                      ),
                      referenceNumberUcr = None,
                      packaging = Some(
                        NonEmptyList.of(
                          Packaging(
                            sequenceNumber = None,
                            typeOfPackages = None,
                            numberOfPackages = None,
                            shippingMarks = None
                          ),
                          Packaging(
                            sequenceNumber = None,
                            typeOfPackages = None,
                            numberOfPackages = None,
                            shippingMarks = None
                          )
                        )
                      )
                    )
                  )
                )
              )
            )
          )
        end aesIE507Message

        TestData.ValidAesIE507Xml.noNonRootOptionals
          .as[AesIE507Message]
          .toEither
          .value shouldBe aesIE507Message
      }

      "when GoodsShipment children optional fields are missing" in {
        val aesIE507Message: AesIE507Message =
          AesIE507Message(
            submissionId = None,
            exportOperation = ExportOperation(
              exportOperationType = ExportOperationType.Standard,
              mrn = Mrn("mrn"),
              discrepanciesExist = DiscrepanciesExist(true),
              splitIndicator = SplitIndicator(true)
            ),
            customsOfficeOfExitActual = CustomsOfficeOfExitActual(
              referenceNumber = ReferenceNumber("referenceNumber")
            ),
            goodsShipment = Some(
              GoodsShipment(
                consignment = Consignment(
                  modeOfTransportAtTheBorder = None,
                  referenceNumberUCR = ReferenceNumberUcr("referenceNumberUcr"),
                  parentUcrId = None,
                  transportEquipment = None,
                  seal = None,
                  goodsReference = None,
                  locationOfGoods = LocationOfGoods(
                    typeOfLocation = TypeOfLocation("typeOfLocation"),
                    qualifierOfIdentification = QualifierOfIdentification("qualifierOfIdentification"),
                    authorisationNumber = None,
                    additionalIdentifier = None,
                    unLocode = None
                  ),
                  activeBorderTransportMeans = None,
                  transportDocument = None
                ),
                goodsItem = None
              )
            )
          )
        end aesIE507Message

        TestData.ValidAesIE507Xml.noGoodsShipmentChildrenOptionals
          .as[AesIE507Message]
          .toEither
          .value shouldBe aesIE507Message
      }

      "when GoodsShipment is missing" in {
        val aesIE507Message: AesIE507Message =
          AesIE507Message(
            submissionId = None,
            exportOperation = ExportOperation(
              exportOperationType = ExportOperationType.Standard,
              mrn = Mrn("mrn"),
              discrepanciesExist = DiscrepanciesExist(true),
              splitIndicator = SplitIndicator(true)
            ),
            customsOfficeOfExitActual = CustomsOfficeOfExitActual(
              referenceNumber = ReferenceNumber("referenceNumber")
            ),
            goodsShipment = None
          )
        end aesIE507Message

        TestData.ValidAesIE507Xml.noGoodsShipment
          .as[AesIE507Message]
          .toEither
          .value shouldBe aesIE507Message
      }
    }

    "should return any and all errors when attempting XML deserialization" - {

      "when non root required fields are missing" in {
        val errors: NonEmptyList[XmlReaderError] =
          NonEmptyList.of(
            XmlReaderError.Missing("/ExportOperation/type"),
            XmlReaderError.Missing("/ExportOperation/MRN"),
            XmlReaderError.Missing("/ExportOperation/discrepanciesExist"),
            XmlReaderError.Missing("/ExportOperation/splitIndicator"),
            XmlReaderError.Missing("/CustomsOfficeOfExitActual/referenceNumber"),
            XmlReaderError.Missing("/GoodsShipment/Consignment/referenceNumberUCR"),
            XmlReaderError.Missing("/GoodsShipment/Consignment/LocationOfGoods/typeOfLocation"),
            XmlReaderError.Missing("/GoodsShipment/Consignment/LocationOfGoods/qualifierOfIdentification"),
            XmlReaderError.Missing("/GoodsShipment/GoodsItem/[0]/Commodity/grossMass"),
            XmlReaderError.Missing("/GoodsShipment/GoodsItem/[0]/Commodity/netMass")
          )

        TestData.InvalidAesIE507Xml.missingNonRootRequiredFields
          .as[AesIE507Message]
          .toEither
          .left
          .value shouldBe errors
      }

      "when non GoodsShipment root required fields are missing" in {
        val errors: NonEmptyList[XmlReaderError] =
          NonEmptyList.of(
            XmlReaderError.Missing("/ExportOperation"),
            XmlReaderError.Missing("/CustomsOfficeOfExitActual"),
            XmlReaderError.Missing("/GoodsShipment/Consignment/referenceNumberUCR"),
            XmlReaderError.Missing("/GoodsShipment/Consignment/LocationOfGoods"),
            XmlReaderError.Missing("/GoodsShipment/GoodsItem/[0]/Commodity")
          )

        TestData.InvalidAesIE507Xml.missingNonGoodsShipmentRootRequiredFields
          .as[AesIE507Message]
          .toEither
          .left
          .value shouldBe errors
      }

      "when GoodsShipment root required fields are missing" in {
        val errors: NonEmptyList[XmlReaderError] =
          NonEmptyList.of(
            XmlReaderError.Missing("/ExportOperation"),
            XmlReaderError.Missing("/CustomsOfficeOfExitActual"),
            XmlReaderError.Missing("/GoodsShipment/Consignment")
          )

        TestData.InvalidAesIE507Xml.missingGoodsShipmentRootRequiredFields
          .as[AesIE507Message]
          .toEither
          .left
          .value shouldBe errors
      }

      "when various fields cannot be parsed" in {
        val errors: NonEmptyList[XmlReaderError] =
          NonEmptyList.of(
            XmlReaderError.ParseError("/submissionId", "Failed to parse 'not-uuid' to UUID"),
            XmlReaderError.ParseError("/ExportOperation/type", "Failed to parse 'one' to Int"),
            XmlReaderError.ParseError("/ExportOperation/discrepanciesExist", "Failed to parse '2' to Boolean"),
            XmlReaderError.ParseError("/ExportOperation/splitIndicator", "Failed to parse '-1' to Boolean"),
            XmlReaderError.ParseError("/GoodsShipment/Consignment/modeOfTransportAtTheBorder", "Failed to parse 'two' to Int"),
            XmlReaderError.ParseError("/GoodsShipment/Consignment/TransportEquipment/[0]/sequenceNumber", "Failed to parse 'one' to Int"),
            XmlReaderError.ParseError("/GoodsShipment/Consignment/TransportEquipment/[0]/numberOfSeals", "Failed to parse 'one' to Int"),
            XmlReaderError.ParseError("/GoodsShipment/Consignment/TransportEquipment/[1]/sequenceNumber", "Failed to parse 'two' to Int"),
            XmlReaderError.ParseError("/GoodsShipment/Consignment/TransportEquipment/[1]/numberOfSeals", "Failed to parse 'one' to Int"),
            XmlReaderError.ParseError("/GoodsShipment/Consignment/Seal/[0]/sequenceNumber", "Failed to parse 'two' to Int"),
            XmlReaderError.ParseError("/GoodsShipment/Consignment/Seal/[1]/sequenceNumber", "Failed to parse 'two' to Int"),
            XmlReaderError.ParseError("/GoodsShipment/Consignment/GoodsReference/[0]/sequenceNumber", "Failed to parse 'one' to Int"),
            XmlReaderError.ParseError("/GoodsShipment/Consignment/GoodsReference/[0]/declarationGoodsItemNumber", "Failed to parse 'one' to Int"),
            XmlReaderError.ParseError("/GoodsShipment/Consignment/GoodsReference/[1]/sequenceNumber", "Failed to parse 'two' to Int"),
            XmlReaderError.ParseError("/GoodsShipment/Consignment/GoodsReference/[1]/declarationGoodsItemNumber", "Failed to parse 'two' to Int"),
            XmlReaderError.ParseError("/GoodsShipment/Consignment/TransportDocument/[0]/sequenceNumber", "Failed to parse 'one' to Int"),
            XmlReaderError.ParseError("/GoodsShipment/Consignment/TransportDocument/[0]/type", "Failed to parse 'one' to Int"),
            XmlReaderError.ParseError("/GoodsShipment/Consignment/TransportDocument/[1]/sequenceNumber", "Failed to parse 'two' to Int"),
            XmlReaderError.ParseError("/GoodsShipment/Consignment/TransportDocument/[1]/type", "Failed to parse 'two' to Int"),
            XmlReaderError.ParseError("/GoodsShipment/GoodsItem/[0]/declarationGoodsItemNumber", "Failed to parse 'one' to Int"),
            XmlReaderError.ParseError("/GoodsShipment/GoodsItem/[0]/Commodity/grossMass", "Failed to parse `hundred` to BigDecimal"),
            XmlReaderError.ParseError("/GoodsShipment/GoodsItem/[0]/Commodity/netMass", "Failed to parse `eighty` to BigDecimal"),
            XmlReaderError.ParseError("/GoodsShipment/GoodsItem/[0]/Packaging/[0]/sequenceNumber", "Failed to parse '' to Int"),
            XmlReaderError.ParseError("/GoodsShipment/GoodsItem/[0]/Packaging/[0]/numberOfPackages", "Failed to parse '' to Int"),
            XmlReaderError.ParseError("/GoodsShipment/GoodsItem/[0]/Packaging/[1]/sequenceNumber", "Failed to parse '' to Int"),
            XmlReaderError.ParseError("/GoodsShipment/GoodsItem/[0]/Packaging/[1]/numberOfPackages", "Failed to parse '' to Int"),
            XmlReaderError.ParseError("/GoodsShipment/GoodsItem/[1]/declarationGoodsItemNumber", "Failed to parse '' to Int"),
            XmlReaderError.ParseError("/GoodsShipment/GoodsItem/[1]/Commodity/grossMass", "Failed to parse `` to BigDecimal"),
            XmlReaderError.ParseError("/GoodsShipment/GoodsItem/[1]/Commodity/netMass", "Failed to parse `` to BigDecimal"),
            XmlReaderError.ParseError("/GoodsShipment/GoodsItem/[1]/Packaging/[0]/sequenceNumber", "Failed to parse '' to Int"),
            XmlReaderError.ParseError("/GoodsShipment/GoodsItem/[1]/Packaging/[0]/numberOfPackages", "Failed to parse '' to Int"),
            XmlReaderError.ParseError("/GoodsShipment/GoodsItem/[1]/Packaging/[1]/sequenceNumber", "Failed to parse '' to Int"),
            XmlReaderError.ParseError("/GoodsShipment/GoodsItem/[1]/Packaging/[1]/numberOfPackages", "Failed to parse '' to Int")
          )

        TestData.InvalidAesIE507Xml.unparseableFields
          .as[AesIE507Message]
          .toEither
          .left
          .value shouldBe errors
      }
    }
  }
