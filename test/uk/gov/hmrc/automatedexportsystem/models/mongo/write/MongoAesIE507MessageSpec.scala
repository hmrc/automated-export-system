package uk.gov.hmrc.automatedexportsystem.models.mongo.write

import cats.data.NonEmptyList
import org.scalatest.EitherValues
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.automatedexportsystem.models.aesRequest.*

import java.time.Instant
import java.util.UUID

class MongoAesIE507MessageSpec extends AnyFreeSpecLike, Matchers, EitherValues:
  object TestData:
    val id: UUID = UUID.fromString("6fb33641-6dc7-4a4f-adef-06238c13a317")

    val instant: Instant = Instant.parse("2026-07-21T00:00:00.000Z")

    val mongoAesIE507MessageAllFields: MongoAesIE507Message =
      MongoAesIE507Message(
        _id = id,
        submissionId = SubmissionId("submissionId"),
        eoriNumber = EoriNumber("eoriNumber"),
        created = instant,
        lastUpdated = instant,
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
                    containerIdentificationNumber = Some(ContainerIdentificationNumber(1)),
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

    val mongoAesIE507MessageAllFieldsJson: JsValue =
      Json.parse("""
          |{
          |  "_id" : "6fb33641-6dc7-4a4f-adef-06238c13a317",
          |  "submissionId" : "submissionId",
          |  "eoriNumber" : "eoriNumber",
          |  "created" : "2026-07-21T00:00:00Z",
          |  "lastUpdated" : "2026-07-21T00:00:00Z",
          |  "exportOperation" : {
          |    "exportOperationType" : 1,
          |    "mrn" : "mrn",
          |    "discrepanciesExist" : false,
          |    "splitIndicator" : true
          |  },
          |  "customsOfficeOfExitActual" : {
          |    "referenceNumber" : "referenceNumber"
          |  },
          |  "goodsShipment" : {
          |    "consignment" : {
          |      "modeOfTransportAtBorder" : 1,
          |      "referenceNumberUCR" : "referenceNumberUcr",
          |      "parentUcrId" : "parentUcrId",
          |      "transportEquipment" : [ {
          |        "sequenceNumber" : 1,
          |        "containerIdentificationNumber" : 1,
          |        "numberOfSeals" : 1
          |      } ],
          |      "seal" : [ {
          |        "sequenceNumber" : 1,
          |        "sealIdentifier" : "sealIdentifier"
          |      } ],
          |      "goodsReference" : [ {
          |        "sequenceNumber" : 1,
          |        "declarationGoodsItemNumber" : 1
          |      } ],
          |      "locationOfGoods" : {
          |        "typeOfLocation" : "typeOfLocation",
          |        "qualifierOfIdentification" : "qualifierIdentification",
          |        "authorisationNumber" : "authorisationNumber",
          |        "additionalIdentifier" : "additionalIdentifier",
          |        "unLocode" : "unLocode"
          |      },
          |      "activeBorderTransportMeans" : {
          |        "typeOfIdentification" : "typeOfIdentification",
          |        "identificationNumber" : "identificationNumber",
          |        "nationality" : "nationality"
          |      },
          |      "transportDocument" : [ {
          |        "sequenceNumber" : 1,
          |        "transportDocumentType" : 1,
          |        "referenceNumber" : "referenceNumber"
          |      } ]
          |    },
          |    "goodsItem" : [ {
          |      "declarationGoodsItemNumber" : 1,
          |      "commodity" : {
          |        "grossMass" : 100.55,
          |        "netMass" : 80.45
          |      },
          |      "packaging" : [ {
          |        "sequenceNumber" : 1,
          |        "typeOfPackages" : "typeOfPackages",
          |        "numberOfPackages" : 1,
          |        "shippingMarks" : "shippingMarks"
          |      } ]
          |    } ]
          |  }
          |}
          |""".stripMargin)

    val mongoAesIE507MessageNoOptionalGoodsShipment: MongoAesIE507Message =
      MongoAesIE507Message(
        _id = id,
        submissionId = SubmissionId("submissionId"),
        eoriNumber = EoriNumber("eoriNumber"),
        created = instant,
        lastUpdated = instant,
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

    val mongoAesIE507MessageNoOptionalGoodsShipmentJson: JsValue =
      Json.parse("""
          |{
          |  "_id" : "6fb33641-6dc7-4a4f-adef-06238c13a317",
          |  "submissionId" : "submissionId",
          |  "eoriNumber" : "eoriNumber",
          |  "created" : "2026-07-21T00:00:00Z",
          |  "lastUpdated" : "2026-07-21T00:00:00Z",
          |  "exportOperation" : {
          |    "exportOperationType" : 1,
          |    "mrn" : "mrn",
          |    "discrepanciesExist" : false,
          |    "splitIndicator" : true
          |  },
          |  "customsOfficeOfExitActual" : {
          |    "referenceNumber" : "referenceNumber"
          |  }
          |}
          |""".stripMargin)

    val mongoAesIE507MessageNoObjOptionals: MongoAesIE507Message =
      MongoAesIE507Message(
        _id = id,
        submissionId = SubmissionId("submissionId"),
        eoriNumber = EoriNumber("eoriNumber"),
        created = instant,
        lastUpdated = instant,
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
              modeOfTransportAtBorder = None,
              referenceNumberUCR = ReferenceNumberUcr("referenceNumberUcr"),
              parentUcrId = None,
              transportEquipment = Some(
                NonEmptyList.one(
                  TransportEquipment(
                    sequenceNumber = None,
                    containerIdentificationNumber = None,
                    numberOfSeals = None
                  )
                )
              ),
              seal = Some(
                NonEmptyList.one(
                  Seal(
                    sequenceNumber = None,
                    sealIdentifier = None
                  )
                )
              ),
              goodsReference = Some(
                NonEmptyList.one(
                  GoodsReference(
                    sequenceNumber = None,
                    declarationGoodsItemNumber = None
                  )
                )
              ),
              locationOfGoods = LocationOfGoods(
                typeOfLocation = TypeOfLocation("typeOfLocation"),
                qualifierOfIdentification = QualifierOfIdentification("qualifierIdentification"),
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
                NonEmptyList.one(
                  TransportDocument(
                    sequenceNumber = None,
                    transportDocumentType = None,
                    referenceNumber = None
                  )
                )
              )
            ),
            goodsItem = Some(
              NonEmptyList.one(
                GoodsItem(
                  declarationGoodsItemNumber = None,
                  commodity = Commodity(
                    grossMass = GrossMass(100.55),
                    netMass = NetMass(80.45)
                  ),
                  packaging = Some(
                    NonEmptyList.one(
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

    val mongoAesIE507MessageNoObjOptionalsJson: JsValue =
      Json.parse("""
          |{
          |  "_id" : "6fb33641-6dc7-4a4f-adef-06238c13a317",
          |  "submissionId" : "submissionId",
          |  "eoriNumber" : "eoriNumber",
          |  "created" : "2026-07-21T00:00:00Z",
          |  "lastUpdated" : "2026-07-21T00:00:00Z",
          |  "exportOperation" : {
          |    "exportOperationType" : 1,
          |    "mrn" : "mrn",
          |    "discrepanciesExist" : false,
          |    "splitIndicator" : true
          |  },
          |  "customsOfficeOfExitActual" : {
          |    "referenceNumber" : "referenceNumber"
          |  },
          |  "goodsShipment" : {
          |    "consignment" : {
          |      "referenceNumberUCR" : "referenceNumberUcr",
          |      "transportEquipment" : [ { } ],
          |      "seal" : [ { } ],
          |      "goodsReference" : [ { } ],
          |      "locationOfGoods" : {
          |        "typeOfLocation" : "typeOfLocation",
          |        "qualifierOfIdentification" : "qualifierIdentification"
          |      },
          |      "activeBorderTransportMeans" : { },
          |      "transportDocument" : [ { } ]
          |    },
          |    "goodsItem" : [ {
          |      "commodity" : {
          |        "grossMass" : 100.55,
          |        "netMass" : 80.45
          |      },
          |      "packaging" : [ { } ]
          |    } ]
          |  }
          |}
          |""".stripMargin)

    val mongoAesIE507MessageEmptyLists: MongoAesIE507Message =
      MongoAesIE507Message(
        _id = id,
        submissionId = SubmissionId("submissionId"),
        eoriNumber = EoriNumber("eoriNumber"),
        created = instant,
        lastUpdated = instant,
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
              transportEquipment = None,
              seal = None,
              goodsReference = None,
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
              transportDocument = None
            ),
            goodsItem = None
          )
        )
      )

    val mongoAesIE507MessageEmptyListsJson: JsValue =
      Json.parse("""
          |{
          |  "_id" : "6fb33641-6dc7-4a4f-adef-06238c13a317",
          |  "submissionId" : "submissionId",
          |  "eoriNumber" : "eoriNumber",
          |  "created" : "2026-07-21T00:00:00Z",
          |  "lastUpdated" : "2026-07-21T00:00:00Z",
          |  "exportOperation" : {
          |    "exportOperationType" : 1,
          |    "mrn" : "mrn",
          |    "discrepanciesExist" : false,
          |    "splitIndicator" : true
          |  },
          |  "customsOfficeOfExitActual" : {
          |    "referenceNumber" : "referenceNumber"
          |  },
          |  "goodsShipment" : {
          |    "consignment" : {
          |      "modeOfTransportAtBorder" : 1,
          |      "referenceNumberUCR" : "referenceNumberUcr",
          |      "parentUcrId" : "parentUcrId",
          |      "locationOfGoods" : {
          |        "typeOfLocation" : "typeOfLocation",
          |        "qualifierOfIdentification" : "qualifierIdentification",
          |        "authorisationNumber" : "authorisationNumber",
          |        "additionalIdentifier" : "additionalIdentifier",
          |        "unLocode" : "unLocode"
          |      },
          |      "activeBorderTransportMeans" : {
          |        "typeOfIdentification" : "typeOfIdentification",
          |        "identificationNumber" : "identificationNumber",
          |        "nationality" : "nationality"
          |      }
          |    }
          |  }
          |}
          |""".stripMargin)

  end TestData

  "MongoAesIE507Message" - {

    ".mongoFormat" - {

      "when all optional fields are present" - {

        "should read a JsValue" - {

          "successfully" in {
            MongoAesIE507Message.mongoFormat
              .reads(TestData.mongoAesIE507MessageAllFieldsJson)
              .asEither
              .value shouldBe TestData.mongoAesIE507MessageAllFields
          }
        }

        "should write a JsValue" - {

          "successfully" in {
            MongoAesIE507Message.mongoFormat
              .writes(TestData.mongoAesIE507MessageAllFields) shouldBe
              TestData.mongoAesIE507MessageAllFieldsJson
          }
        }
      }

      "when all optional fields are missing" - {

        "should read a JsValue" - {

          "successfully" in {
            MongoAesIE507Message.mongoFormat
              .reads(TestData.mongoAesIE507MessageNoOptionalGoodsShipmentJson)
              .asEither
              .value shouldBe TestData.mongoAesIE507MessageNoOptionalGoodsShipment
          }
        }

        "should write a JsValue" - {

          "successfully" in {
            MongoAesIE507Message.mongoFormat
              .writes(TestData.mongoAesIE507MessageNoOptionalGoodsShipment) shouldBe
              TestData.mongoAesIE507MessageNoOptionalGoodsShipmentJson
          }
        }
      }

      "when some optional fields are missing" - {

        "should read a JsValue" - {

          "successfully" in {
            MongoAesIE507Message.mongoFormat
              .reads(TestData.mongoAesIE507MessageNoObjOptionalsJson)
              .asEither
              .value shouldBe TestData.mongoAesIE507MessageNoObjOptionals
          }
        }

        "should write a JsValue" - {

          "successfully" in {
            MongoAesIE507Message.mongoFormat
              .writes(TestData.mongoAesIE507MessageNoObjOptionals) shouldBe
              TestData.mongoAesIE507MessageNoObjOptionalsJson
          }
        }
      }

      "when list fields are missing" - {

        "should read a JsValue" - {

          "successfully" in {
            MongoAesIE507Message.mongoFormat
              .reads(TestData.mongoAesIE507MessageEmptyListsJson)
              .asEither
              .value shouldBe TestData.mongoAesIE507MessageEmptyLists
          }
        }

        "should write a JsValue" - {

          "successfully" in {
            MongoAesIE507Message.mongoFormat
              .writes(TestData.mongoAesIE507MessageNoObjOptionals) shouldBe
              TestData.mongoAesIE507MessageNoObjOptionalsJson
          }
        }
      }
    }
  }
