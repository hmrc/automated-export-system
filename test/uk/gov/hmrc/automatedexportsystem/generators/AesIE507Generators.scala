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

package uk.gov.hmrc.automatedexportsystem.generators

import cats.data.NonEmptyList
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import uk.gov.hmrc.automatedexportsystem.models.IE507.*
import uk.gov.hmrc.automatedexportsystem.models.IE507.aes.SubmissionId
import uk.gov.hmrc.automatedexportsystem.models.mongo.write.MongoAesIE507Message

import java.util.UUID

trait AesIE507Generators extends BaseGenerators:
  given submissionIdArb: Arbitrary[SubmissionId] =
    Arbitrary {
      arbitrary[UUID].map(SubmissionId.apply)
    }

  given eoriNumberArb: Arbitrary[EoriNumber] =
    Arbitrary {
      Gen.asciiStr.map(EoriNumber.apply)
    }

  given exportOperationTypeArb: Arbitrary[ExportOperationType] =
    Arbitrary {
      Gen.oneOf(ExportOperationType.values.toSeq)
    }

  given mrnArb: Arbitrary[Mrn] =
    Arbitrary {
      Gen.asciiStr.map(Mrn.apply)
    }

  given discrepanciesExistArb: Arbitrary[DiscrepanciesExist] =
    Arbitrary {
      arbitrary[Boolean].map(DiscrepanciesExist.apply)
    }

  given splitIndicatorArb: Arbitrary[SplitIndicator] =
    Arbitrary {
      arbitrary[Boolean].map(SplitIndicator.apply)
    }

  given exportOperationArb: Arbitrary[ExportOperation] =
    Arbitrary {
      for
        exportOperationType <- arbitrary[ExportOperationType]
        mrn                 <- arbitrary[Mrn]
        discrepanciesExist  <- arbitrary[DiscrepanciesExist]
        splitIndicator      <- arbitrary[SplitIndicator]
      yield ExportOperation(exportOperationType, mrn, discrepanciesExist, splitIndicator)
    }

  given referenceNumberArb: Arbitrary[ReferenceNumber] =
    Arbitrary {
      Gen.asciiStr.map(ReferenceNumber.apply)
    }

  given customsOfficeOfExitActualArb: Arbitrary[CustomsOfficeOfExitActual] =
    Arbitrary {
      arbitrary[ReferenceNumber].map(CustomsOfficeOfExitActual.apply)
    }

  given modeOfTransportAtTheBorderArb: Arbitrary[ModeOfTransportAtTheBorder] =
    Arbitrary {
      arbitrary[Int].map(ModeOfTransportAtTheBorder.apply)
    }

  given referenceNumberUcrArb: Arbitrary[ReferenceNumberUcr] =
    Arbitrary {
      Gen.asciiStr.map(ReferenceNumberUcr.apply)
    }

  given parentUcrIdArb: Arbitrary[ParentUcrId] =
    Arbitrary {
      Gen.asciiStr.map(ParentUcrId.apply)
    }

  given sequenceNumberArb: Arbitrary[SequenceNumber] =
    Arbitrary {
      arbitrary[Int].map(SequenceNumber.apply)
    }

  given containerIdentificationNumberArb: Arbitrary[ContainerIdentificationNumber] =
    Arbitrary {
      arbitrary[String].map(ContainerIdentificationNumber.apply)
    }

  given numberOfSealsArb: Arbitrary[NumberOfSeals] =
    Arbitrary {
      arbitrary[Int].map(NumberOfSeals.apply)
    }

  given transportEquipmentArb: Arbitrary[TransportEquipment] =
    Arbitrary {
      for
        sequenceNumber                <- arbitrary[Option[SequenceNumber]]
        containerIdentificationNumber <- arbitrary[Option[ContainerIdentificationNumber]]
        numberOfSeals                 <- arbitrary[Option[NumberOfSeals]]
      yield TransportEquipment(sequenceNumber, containerIdentificationNumber, numberOfSeals)
    }

  given sealIdentifierArb: Arbitrary[SealIdentifier] =
    Arbitrary {
      Gen.asciiStr.map(SealIdentifier.apply)
    }

  given sealArb: Arbitrary[Seal] =
    Arbitrary {
      for
        sequenceNumber <- arbitrary[Option[SequenceNumber]]
        sealIdentifier <- arbitrary[Option[SealIdentifier]]
      yield Seal(sequenceNumber, sealIdentifier)
    }

  given declarationGoodsItemNumberArb: Arbitrary[DeclarationGoodsItemNumber] =
    Arbitrary {
      arbitrary[Int].map(DeclarationGoodsItemNumber.apply)
    }

  given goodsReferenceArb: Arbitrary[GoodsReference] =
    Arbitrary {
      for
        sequenceNumber             <- arbitrary[Option[SequenceNumber]]
        declarationGoodsItemNumber <- arbitrary[Option[DeclarationGoodsItemNumber]]
      yield GoodsReference(sequenceNumber, declarationGoodsItemNumber)
    }

  given typeOfLocationArb: Arbitrary[TypeOfLocation] =
    Arbitrary {
      Gen.asciiStr.map(TypeOfLocation.apply)
    }

  given qualifierOfIdentificationArb: Arbitrary[QualifierOfIdentification] =
    Arbitrary {
      Gen.asciiStr.map(QualifierOfIdentification.apply)
    }

  given authorisationNumberArb: Arbitrary[AuthorisationNumber] =
    Arbitrary {
      Gen.asciiStr.map(AuthorisationNumber.apply)
    }

  given additionalIdentifierArb: Arbitrary[AdditionalIdentifier] =
    Arbitrary {
      Gen.asciiStr.map(AdditionalIdentifier.apply)
    }

  given unLocodeArb: Arbitrary[UnLocode] =
    Arbitrary {
      Gen.asciiStr.map(UnLocode.apply)
    }

  given locationOfGoodsArb: Arbitrary[LocationOfGoods] =
    Arbitrary {
      for
        typeOfLocation            <- arbitrary[TypeOfLocation]
        qualifierOfIdentification <- arbitrary[QualifierOfIdentification]
        authorisationNumber       <- arbitrary[Option[AuthorisationNumber]]
        additionalIdentifier      <- arbitrary[Option[AdditionalIdentifier]]
        unLocode                  <- arbitrary[Option[UnLocode]]
      yield LocationOfGoods(typeOfLocation, qualifierOfIdentification, authorisationNumber, additionalIdentifier, unLocode)
    }

  given typeOfIdentificationArb: Arbitrary[TypeOfIdentification] =
    Arbitrary {
      Gen.asciiStr.map(TypeOfIdentification.apply)
    }

  given identificationNumberArb: Arbitrary[IdentificationNumber] =
    Arbitrary {
      Gen.asciiStr.map(IdentificationNumber.apply)
    }

  given nationalityArb: Arbitrary[Nationality] =
    Arbitrary {
      Gen.asciiStr.map(Nationality.apply)
    }

  given activeBorderTransportMeansArb: Arbitrary[ActiveBorderTransportMeans] =
    Arbitrary {
      for
        typeOfIdentification <- arbitrary[Option[TypeOfIdentification]]
        identificationNumber <- arbitrary[Option[IdentificationNumber]]
        nationality          <- arbitrary[Option[Nationality]]
      yield ActiveBorderTransportMeans(typeOfIdentification, identificationNumber, nationality)
    }

  given transportDocumentTypeArb: Arbitrary[TransportDocumentType] =
    Arbitrary {
      arbitrary[Int].map(TransportDocumentType.apply)
    }

  given transportDocumentArb: Arbitrary[TransportDocument] =
    Arbitrary {
      for
        sequenceNumber        <- arbitrary[Option[SequenceNumber]]
        transportDocumentType <- arbitrary[Option[TransportDocumentType]]
        referenceNumber       <- arbitrary[Option[ReferenceNumber]]
      yield TransportDocument(sequenceNumber, transportDocumentType, referenceNumber)
    }

  given consignmentArb: Arbitrary[Consignment] =
    Arbitrary {
      for
        modeOfTransportAtTheBorder <- arbitrary[Option[ModeOfTransportAtTheBorder]]
        referenceNumberUCR         <- arbitrary[ReferenceNumberUcr]
        parentUcrId                <- arbitrary[Option[ParentUcrId]]
        transportEquipment         <- arbitrary[Option[NonEmptyList[TransportEquipment]]]
        seal                       <- arbitrary[Option[NonEmptyList[Seal]]]
        goodsReference             <- arbitrary[Option[NonEmptyList[GoodsReference]]]
        locationOfGoods            <- arbitrary[LocationOfGoods]
        activeBorderTransportMeans <- arbitrary[Option[ActiveBorderTransportMeans]]
        transportDocument          <- arbitrary[Option[NonEmptyList[TransportDocument]]]
      yield Consignment(
        modeOfTransportAtTheBorder,
        referenceNumberUCR,
        parentUcrId,
        transportEquipment,
        seal,
        goodsReference,
        locationOfGoods,
        activeBorderTransportMeans,
        transportDocument
      )
    }

  given grossMassArb: Arbitrary[GrossMass] =
    Arbitrary {
      arbitrary[Double].map(d => GrossMass.apply(BigDecimal(d)))
    }

  given netMassArb: Arbitrary[NetMass] =
    Arbitrary {
      arbitrary[Double].map(d => NetMass.apply(BigDecimal(d)))
    }

  given commodityArb: Arbitrary[Commodity] =
    Arbitrary {
      for
        grossMass <- arbitrary[GrossMass]
        netMass   <- arbitrary[NetMass]
      yield Commodity(grossMass, netMass)
    }

  given typeOfPackagesArb: Arbitrary[TypeOfPackages] =
    Arbitrary {
      Gen.asciiStr.map(TypeOfPackages.apply)
    }

  given numberOfPackagesArb: Arbitrary[NumberOfPackages] =
    Arbitrary {
      arbitrary[Int].map(NumberOfPackages.apply)
    }

  given shippingMarksArb: Arbitrary[ShippingMarks] =
    Arbitrary {
      Gen.asciiStr.map(ShippingMarks.apply)
    }

  given packagingArb: Arbitrary[Packaging] =
    Arbitrary {
      for
        sequenceNumber   <- arbitrary[Option[SequenceNumber]]
        typeOfPackages   <- arbitrary[Option[TypeOfPackages]]
        numberOfPackages <- arbitrary[Option[NumberOfPackages]]
        shippingMarks    <- arbitrary[Option[ShippingMarks]]
      yield Packaging(sequenceNumber, typeOfPackages, numberOfPackages, shippingMarks)
    }

  given goodsItemArb: Arbitrary[GoodsItem] =
    Arbitrary {
      for
        declarationGoodsItemNumber <- arbitrary[Option[DeclarationGoodsItemNumber]]
        referenceNumberUCR         <- arbitrary[Option[ReferenceNumberUcr]]
        commodity                  <- arbitrary[Commodity]
        packaging                  <- arbitrary[Option[NonEmptyList[Packaging]]]
      yield GoodsItem(declarationGoodsItemNumber, referenceNumberUCR, commodity, packaging)
    }

  given goodsShipmentArb: Arbitrary[GoodsShipment] =
    Arbitrary {
      for
        consignment <- arbitrary[Consignment]
        goodsItem   <- arbitrary[Option[NonEmptyList[GoodsItem]]]
      yield GoodsShipment(consignment, goodsItem)
    }

trait MongoAesIE507MessageGenerator extends AesIE507Generators:
  given mongoAesIE507Arb: Arbitrary[MongoAesIE507Message] =
    Arbitrary {
      for
        _id                       <- arbitrary[SubmissionId]
        eoriNumber                <- arbitrary[EoriNumber]
        (createdAt, updatedAt)    <- chronologicalInstantsArb.arbitrary
        exportOperation           <- arbitrary[ExportOperation]
        customsOfficeOfExitActual <- arbitrary[CustomsOfficeOfExitActual]
        goodsShipment             <- arbitrary[Option[GoodsShipment]]
      yield MongoAesIE507Message(_id, eoriNumber, createdAt, updatedAt, exportOperation, customsOfficeOfExitActual, goodsShipment)
    }
