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

package uk.gov.hmrc.automatedexportsystem.parsers

import cats.data.NonEmptyList
import uk.gov.hmrc.automatedexportsystem.models.aesIE507.{ActiveBorderTransportMeans, AdditionalIdentifier, AuthorisationNumber, Commodity, Consignment, ContainerIdentificationNumber, CustomsOfficeOfExitActual, DeclarationGoodsItemNumber, DiscrepanciesExist, ExportOperation, ExportOperationType, GoodsItem, GoodsReference, GoodsShipment, GrossMass, IdentificationNumber, LocationOfGoods, ModeOfTransportAtBorder, Mrn, Nationality, NetMass, NumberOfPackages, NumberOfSeals, Packaging, ParentUcrId, QualifierOfIdentification, ReferenceNumber, ReferenceNumberUcr, Seal, SealIdentifier, SequenceNumber, ShippingMarks, SplitIndicator, SubmissionId, TransportDocument, TransportDocumentType, TransportEquipment, TypeOfIdentification, TypeOfLocation, TypeOfPackages, UnLocode}
import uk.gov.hmrc.automatedexportsystem.models.request.SubmissionRequest

import java.util.UUID
import scala.util.Try
import scala.xml.{Node, NodeSeq}

object SubmissionRequestParser {

  private object Tags {
    val SubmissionId       = "SubmissionId"
    val ExportOperation    = "ExportOperation"
    val MRN                = "MRN"
    val DiscrepanciesExist = "discrepanciesExist"
    val SplitIndicator     = "splitIndicator"

    val CustomsOfficeOfExitActual = "CustomsOfficeOfExitActual"
    val ReferenceNumber           = "referenceNumber"

    val GoodsShipment           = "GoodsShipment"
    val Consignment             = "Consignment"
    val ModeOfTransportAtBorder = "modeOfTransportAtBorder"
    val ReferenceNumberUCR      = "referenceNumberUCR"
    val ParentUCRID             = "parentUCRID"

    val LocationOfGoods           = "LocationOfGoods"
    val TypeOfLocation            = "typeOfLocation"
    val QualifierOfIdentification = "qualifierOfIdentification"
    val AuthorisationNumber       = "authorisationNumber"
    val AdditionalIdentifier      = "additionalIdentifier"
    val UNLocode                  = "UNLocode"

    val TransportEquipment            = "TransportEquipment"
    val NumberOfSeals                 = "numberOfSeals"
    val ContainerIdentificationNumber = "containerIdentificationNumber"

    val Seal       = "Seal"
    val Identifier = "identifier"

    val GoodsReference             = "GoodsReference"
    val DeclarationGoodsItemNumber = "declarationGoodsItemNumber"

    val ActiveBorderTransportMeans = "ActiveBorderTransportMeans"
    val TypeOfIdentification       = "typeOfIdentification"
    val IdentificationNumber       = "identificationNumber"
    val Nationality                = "nationality"

    val TransportDocument = "TransportDocument"
    val SequenceNumber    = "sequenceNumber"
    val Type              = "type"

    val GoodsItem      = "GoodsItem"
    val Commodity      = "Commodity"
    val GoodsMeasure   = "GoodsMeasure"
    val GrossMass      = "grossMass"
    val NetMass        = "netMass"
    val Packaging      = "Packaging"
    val TypeOfPackages = "typeOfPackages"
    val ShippingMarks  = "shippingMarks"
  }

  def fromXml(xml: NodeSeq): Either[String, SubmissionRequest] =
    for {
      submissionId <- parseOptionalSubmissionId(textOptDeep(xml, Tags.SubmissionId))
      exportOpNode <- req((xml \\ Tags.ExportOperation).headOption, Tags.ExportOperation)
      exportOp     <- parseExportOperation(exportOpNode)

      officeNode <- req((xml \\ Tags.CustomsOfficeOfExitActual).headOption, Tags.CustomsOfficeOfExitActual)
      office     <- parseCustomsOfficeOfExitActual(officeNode)

      shipmentNode <- req((xml \\ Tags.GoodsShipment).headOption, Tags.GoodsShipment)
      shipment     <- parseGoodsShipment(shipmentNode)
    } yield SubmissionRequest(
      submissionId = submissionId,
      exportOperation = exportOp,
      customsOfficeOfExitActual = office,
      goodsShipment = Some(shipment)
    )

  private def parseOptionalSubmissionId(raw: Option[String]): Either[String, Option[SubmissionId]] =
    parseOptionalUuid(raw).map(_.map(SubmissionId.apply))

  private def parseExportOperation(n: Node): Either[String, ExportOperation] =
    for {
      mrn           <- req(textOptChild(n, Tags.MRN), Tags.MRN).map(Mrn.apply)
      discrepancies <- req(textOptChild(n, Tags.DiscrepanciesExist), Tags.DiscrepanciesExist)
                         .flatMap(parseBoolean)
                         .map(DiscrepanciesExist.apply)
      split <- req(textOptChild(n, Tags.SplitIndicator), Tags.SplitIndicator)
                 .flatMap(parseBoolean)
                 .map(SplitIndicator.apply)
    } yield ExportOperation(
      mrn = mrn,
      discrepanciesExist = discrepancies,
      splitIndicator = split,
      exportOperationType = ExportOperationType.Awaiting
    )

  private def parseCustomsOfficeOfExitActual(n: Node): Either[String, CustomsOfficeOfExitActual] =
    req(textOptChild(n, Tags.ReferenceNumber), Tags.ReferenceNumber)
      .map(v => CustomsOfficeOfExitActual(ReferenceNumber(v)))

  private def parseGoodsShipment(n: Node): Either[String, GoodsShipment] =
    for {
      consignmentNode <- req((n \ Tags.Consignment).headOption, Tags.Consignment)
      consignment     <- parseConsignment(consignmentNode)
      goodsItems      <- parseGoodsItems(n)
    } yield GoodsShipment(
      consignment = consignment,
      goodsItem = goodsItems
    )

  private def parseConsignment(n: Node): Either[String, Consignment] =
    for {
      referenceNumberUcr <- req(textOptChild(n, Tags.ReferenceNumberUCR), Tags.ReferenceNumberUCR).map(ReferenceNumberUcr.apply)

      modeOfTransportAtBorder <- parseOptionalInt(textOptChild(n, Tags.ModeOfTransportAtBorder), Tags.ModeOfTransportAtBorder)
                                   .map(_.map(ModeOfTransportAtBorder.apply))

      locationNode    <- req((n \ Tags.LocationOfGoods).headOption, Tags.LocationOfGoods)
      locationOfGoods <- parseLocationOfGoods(locationNode)

      transportEquipment <- parseTransportEquipment(n)
      seals              <- parseSeals(n)
      goodsReferences    <- parseGoodsReferences(n)
      borderMeans        <- parseActiveBorderTransportMeans(n)
      transportDocs      <- parseTransportDocuments(n)
    } yield Consignment(
      modeOfTransportAtBorder = modeOfTransportAtBorder,
      referenceNumberUCR = referenceNumberUcr,
      parentUcrId = textOptChild(n, Tags.ParentUCRID).map(ParentUcrId.apply),
      transportEquipment = transportEquipment,
      seal = seals,
      goodsReference = goodsReferences,
      locationOfGoods = locationOfGoods,
      activeBorderTransportMeans = borderMeans,
      transportDocument = transportDocs
    )

  private def parseLocationOfGoods(n: Node): Either[String, LocationOfGoods] =
    for {
      typeOfLocation <- req(textOptChild(n, Tags.TypeOfLocation), Tags.TypeOfLocation).map(TypeOfLocation.apply)
      qualifier      <- req(textOptChild(n, Tags.QualifierOfIdentification), Tags.QualifierOfIdentification).map(QualifierOfIdentification.apply)
    } yield LocationOfGoods(
      typeOfLocation = typeOfLocation,
      qualifierOfIdentification = qualifier,
      authorisationNumber = textOptChild(n, Tags.AuthorisationNumber).map(AuthorisationNumber.apply),
      additionalIdentifier = textOptChild(n, Tags.AdditionalIdentifier).map(AdditionalIdentifier.apply),
      unLocode = textOptChild(n, Tags.UNLocode).map(UnLocode.apply)
    )

  private def parseTransportEquipment(n: Node): Either[String, Option[NonEmptyList[TransportEquipment]]] = {
    val nodes = (n \ Tags.TransportEquipment).toList
    sequence(nodes.map(parseTransportEquipmentNode)).map(NonEmptyList.fromList)
  }

  private def parseTransportEquipmentNode(n: Node): Either[String, TransportEquipment] =
    for {
      sequenceNumber <- parseOptionalInt(textOptChild(n, Tags.SequenceNumber), Tags.SequenceNumber).map(_.map(SequenceNumber.apply))
      numberOfSeals  <- parseOptionalInt(textOptChild(n, Tags.NumberOfSeals), Tags.NumberOfSeals).map(_.map(NumberOfSeals.apply))
    } yield TransportEquipment(
      sequenceNumber = sequenceNumber,
      containerIdentificationNumber = textOptChild(n, Tags.ContainerIdentificationNumber)
        .map(_.trim)
        .filter(_.nonEmpty)
        .map(ContainerIdentificationNumber.apply),
      numberOfSeals = numberOfSeals
    )

  private def parseSeals(n: Node): Either[String, Option[NonEmptyList[Seal]]] = {
    val nodes = (n \ Tags.Seal).toList
    sequence(nodes.map(parseSealNode)).map(NonEmptyList.fromList)
  }

  private def parseSealNode(n: Node): Either[String, Seal] =
    for {
      sequenceNumber <- parseOptionalInt(textOptChild(n, Tags.SequenceNumber), Tags.SequenceNumber).map(_.map(SequenceNumber.apply))
    } yield Seal(
      sequenceNumber = sequenceNumber,
      sealIdentifier = textOptChild(n, Tags.Identifier).map(SealIdentifier.apply)
    )

  private def parseGoodsReferences(n: Node): Either[String, Option[NonEmptyList[GoodsReference]]] = {
    val nodes = (n \ Tags.GoodsReference).toList
    sequence(nodes.map(parseGoodsReferenceNode)).map(NonEmptyList.fromList)
  }

  private def parseGoodsReferenceNode(n: Node): Either[String, GoodsReference] =
    for {
      sequenceNumber <- parseOptionalInt(textOptChild(n, Tags.SequenceNumber), Tags.SequenceNumber).map(_.map(SequenceNumber.apply))
      declarationNo  <- parseOptionalInt(textOptChild(n, Tags.DeclarationGoodsItemNumber), Tags.DeclarationGoodsItemNumber)
                         .map(_.map(DeclarationGoodsItemNumber.apply))
    } yield GoodsReference(
      sequenceNumber = sequenceNumber,
      declarationGoodsItemNumber = declarationNo
    )

  private def parseActiveBorderTransportMeans(n: Node): Either[String, Option[ActiveBorderTransportMeans]] = {
    val maybeNode = (n \ Tags.ActiveBorderTransportMeans).headOption
    Right(
      maybeNode.map { m =>
        ActiveBorderTransportMeans(
          typeOfIdentification = textOptChild(m, Tags.TypeOfIdentification).map(TypeOfIdentification.apply),
          identificationNumber = textOptChild(m, Tags.IdentificationNumber).map(IdentificationNumber.apply),
          nationality = textOptChild(m, Tags.Nationality).map(Nationality.apply)
        )
      }
    )
  }

  private def parseTransportDocuments(n: Node): Either[String, Option[NonEmptyList[TransportDocument]]] = {
    val nodes = (n \ Tags.TransportDocument).toList
    sequence(nodes.map(parseTransportDocumentNode)).map(NonEmptyList.fromList)
  }

  private def parseTransportDocumentNode(n: Node): Either[String, TransportDocument] =
    for {
      sequenceNumber <- parseOptionalInt(textOptChild(n, Tags.SequenceNumber), Tags.SequenceNumber).map(_.map(SequenceNumber.apply))
      documentType   <- parseOptionalInt(textOptChild(n, Tags.Type), Tags.Type).map(_.map(TransportDocumentType.apply))
    } yield TransportDocument(
      sequenceNumber = sequenceNumber,
      transportDocumentType = documentType,
      referenceNumber = textOptChild(n, Tags.ReferenceNumber).map(ReferenceNumber.apply)
    )

  private def parseGoodsItems(n: Node): Either[String, Option[NonEmptyList[GoodsItem]]] = {
    val nodes = (n \\ Tags.GoodsItem).toList
    sequence(nodes.map(parseGoodsItemNode)).map(NonEmptyList.fromList)
  }

  private def parseGoodsItemNode(n: Node): Either[String, GoodsItem] =
    for {
      goodsMeasure  <- req((n \ Tags.Commodity \ Tags.GoodsMeasure).headOption, Tags.GoodsMeasure)
      gross         <- req(textOptChild(goodsMeasure, Tags.GrossMass), Tags.GrossMass).flatMap(parseBigDecimal).map(GrossMass.apply)
      net           <- req(textOptChild(goodsMeasure, Tags.NetMass), Tags.NetMass).flatMap(parseBigDecimal).map(NetMass.apply)
      packaging     <- parsePackaging(n)
      declarationNo <- parseOptionalInt(textOptChild(n, Tags.DeclarationGoodsItemNumber), Tags.DeclarationGoodsItemNumber)
                         .map(_.map(DeclarationGoodsItemNumber.apply))
    } yield GoodsItem(
      declarationGoodsItemNumber = declarationNo,
      referenceNumberUcr = textOptChild(n, Tags.ReferenceNumberUCR).map(_.trim).filter(_.nonEmpty).map(ReferenceNumberUcr.apply),
      commodity = Commodity(grossMass = gross, netMass = net),
      packaging = packaging
    )

  private def parsePackaging(n: Node): Either[String, Option[NonEmptyList[Packaging]]] = {
    val nodes = (n \ Tags.Packaging).toList
    sequence(nodes.map(parsePackagingNode)).map(NonEmptyList.fromList)
  }

  private def parsePackagingNode(n: Node): Either[String, Packaging] =
    for {
      sequenceNumber   <- parseOptionalInt(textOptChild(n, Tags.SequenceNumber), Tags.SequenceNumber).map(_.map(SequenceNumber.apply))
      numberOfPackages <- parseOptionalInt(textOptChild(n, "numberOfPackages"), "numberOfPackages").map(_.map(NumberOfPackages.apply))
    } yield Packaging(
      sequenceNumber = sequenceNumber,
      typeOfPackages = textOptChild(n, Tags.TypeOfPackages).map(TypeOfPackages.apply),
      numberOfPackages = numberOfPackages,
      shippingMarks = textOptChild(n, Tags.ShippingMarks).map(ShippingMarks.apply)
    )

  private def textOptDeep(xml: NodeSeq, tag: String): Option[String] =
    (xml \\ tag).headOption.map(_.text.trim).filter(_.nonEmpty)

  private def textOptChild(n: Node, tag: String): Option[String] =
    (n \ tag).headOption.map(_.text.trim).filter(_.nonEmpty)

  private def req[A](opt: Option[A], field: String): Either[String, A] =
    opt.toRight(s"Missing required field: $field")

  private def parseOptionalInt(raw: Option[String], field: String): Either[String, Option[Int]] =
    raw match {
      case None    => Right(None)
      case Some(v) =>
        Try(v.trim.toInt).toEither.left.map(_ => s"Invalid integer for $field: $v").map(Some(_))
    }

  private def parseBigDecimal(s: String): Either[String, BigDecimal] =
    Try(BigDecimal(s.trim)).toEither.left.map(_ => s"Invalid decimal: $s")

  private def parseBoolean(s: String): Either[String, Boolean] =
    s.trim.toLowerCase match {
      case "true" | "1"  => Right(true)
      case "false" | "0" => Right(false)
      case other         => Left(s"Invalid boolean: $other")
    }

  private def parseOptionalUuid(raw: Option[String]): Either[String, Option[UUID]] =
    raw match {
      case None    => Right(None)
      case Some(v) =>
        Try(UUID.fromString(v.trim)).toEither.left.map(_ => s"Invalid UUID: $v").map(Some(_))
    }

  private def sequence[A](xs: List[Either[String, A]]): Either[String, List[A]] =
    xs.foldRight(Right(Nil): Either[String, List[A]]) { (e, acc) =>
      for {
        x <- e
        a <- acc
      } yield x :: a
    }
}
