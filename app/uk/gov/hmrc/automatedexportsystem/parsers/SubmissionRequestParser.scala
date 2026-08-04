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

  def fromXml(xml: NodeSeq): Either[String, SubmissionRequest] =
    for {
      submissionId <- parseOptionalSubmissionId(textOpt(xml, "SubmissionId"))
      exportOp     <- parseExportOperation(xml)
      office       <- parseCustomsOfficeOfExitActual(xml)
      shipment     <- parseGoodsShipment(xml)
    } yield SubmissionRequest(
      submissionId = submissionId,
      exportOperation = exportOp,
      customsOfficeOfExitActual = office,
      goodsShipment = Some(shipment)
    )

  // ---------------- top-level ----------------

  private def parseOptionalSubmissionId(raw: Option[String]): Either[String, Option[SubmissionId]] =
    parseOptionalUuid(raw).map(_.map(SubmissionId.apply))

  private def parseExportOperation(xml: NodeSeq): Either[String, ExportOperation] =
    for {
      mrn           <- req(textOpt(xml, "MRN"), "MRN").map(Mrn.apply)
      discrepancies <- req(textOpt(xml, "discrepanciesExist"), "iiscrepanciesExist")
                         .flatMap(parseBoolean)
                         .map(DiscrepanciesExist.apply)
      split <- req(textOpt(xml, "splitIndicator"), "splitIndicator")
                 .flatMap(parseBoolean)
                 .map(SplitIndicator.apply)
    } yield ExportOperation(
      mrn = mrn,
      discrepanciesExist = discrepancies,
      splitIndicator = split,
      exportOperationType = ExportOperationType.Awaiting // set default; controller/service can override
    )

  private def parseCustomsOfficeOfExitActual(xml: NodeSeq): Either[String, CustomsOfficeOfExitActual] =
    req(textOpt(xml, "referenceNumber"), "ReferenceNumber")
      .map(v => CustomsOfficeOfExitActual(ReferenceNumber(v)))

  private def parseGoodsShipment(xml: NodeSeq): Either[String, GoodsShipment] =
    for {
      consignment <- parseConsignment(xml)
      goodsItems  <- parseGoodsItems(xml)
    } yield GoodsShipment(
      consignment = consignment,
      goodsItem = goodsItems
    )

  // ---------------- consignment ----------------

  private def parseConsignment(xml: NodeSeq): Either[String, Consignment] =
    for {
      referenceNumberUcr <- req(textOpt(xml, "referenceNumberUCR"), "ReferenceNumberUCR").map(ReferenceNumberUcr.apply)
      locationOfGoods    <- parseLocationOfGoods(xml)
      transportEquipment <- parseTransportEquipment(xml)
      seals              <- parseSeals(xml)
      goodsReferences    <- parseGoodsReferences(xml)
      borderMeans        <- parseActiveBorderTransportMeans(xml)
      transportDocs      <- parseTransportDocuments(xml)
    } yield {
      val modeOfTransportAtBorder =
        textOpt(xml, "modeOfTransportAtBorder")
          .map(_.trim)
          .filter(_.nonEmpty)
          .flatMap(toIntOpt)
          .map(ModeOfTransportAtBorder.apply)
      Consignment(
        modeOfTransportAtBorder = modeOfTransportAtBorder,
        referenceNumberUCR = referenceNumberUcr,
        parentUcrId = textOpt(xml, "parentUCRID").map(ParentUcrId.apply),
        transportEquipment = transportEquipment,
        seal = seals,
        goodsReference = goodsReferences,
        locationOfGoods = locationOfGoods,
        activeBorderTransportMeans = borderMeans,
        transportDocument = transportDocs
      )
    }

  private def parseLocationOfGoods(xml: NodeSeq): Either[String, LocationOfGoods] =
    for {
      typeOfLocation <- req(textOpt(xml, "typeOfLocation"), "TypeOfLocation").map(TypeOfLocation.apply)
      qualifier      <- req(textOpt(xml, "qualifierOfIdentification"), "QualifierOfIdentification").map(QualifierOfIdentification.apply)
    } yield LocationOfGoods(
      typeOfLocation = typeOfLocation,
      qualifierOfIdentification = qualifier,
      authorisationNumber = textOpt(xml, "authorisationNumber").map(AuthorisationNumber.apply),
      additionalIdentifier = textOpt(xml, "additionalIdentifier").map(AdditionalIdentifier.apply),
      unLocode = textOpt(xml, "UNLocode").map(UnLocode.apply)
    )

  // ---------------- requested parsers ----------------

  private def parseTransportEquipment(xml: NodeSeq): Either[String, Option[NonEmptyList[TransportEquipment]]] = {
    val nodes = (xml \\ "TransportEquipment").toList
    sequence(nodes.map(parseTransportEquipmentNode)).map(xs => NonEmptyList.fromList(xs))
  }

  private def parseTransportEquipmentNode(n: Node): Either[String, TransportEquipment] =
    Right(
      TransportEquipment(
        sequenceNumber = textOpt(n, "sequenceNumber")
          .flatMap(toIntOpt)
          .map(SequenceNumber.apply),
        containerIdentificationNumber = textOpt(n, "containerIdentificationNumber")
          .map(_.trim)
          .filter(_.nonEmpty)
          .map(ContainerIdentificationNumber.apply),
        numberOfSeals = textOpt(n, "numberOfSeals")
          .flatMap(toIntOpt)
          .map(NumberOfSeals.apply)
      )
    )

  private def parseSeals(xml: NodeSeq): Either[String, Option[NonEmptyList[Seal]]] = {
    val nodes = (xml \\ "Seal").toList
    sequence(nodes.map(parseSealNode)).map(xs => NonEmptyList.fromList(xs))
  }

  private def parseSealNode(n: Node): Either[String, Seal] =
    Right(
      Seal(
        sequenceNumber = textOpt(n, "sequenceNumber")
          .flatMap(toIntOpt)
          .map(SequenceNumber.apply),
        sealIdentifier = textOpt(n, "identifier")
          .map(SealIdentifier.apply)
      )
    )

  private def parseGoodsReferences(xml: NodeSeq): Either[String, Option[NonEmptyList[GoodsReference]]] = {
    val nodes = (xml \\ "GoodsReference").toList
    sequence(nodes.map(parseGoodsReferenceNode)).map(xs => NonEmptyList.fromList(xs))
  }

  private def parseGoodsReferenceNode(n: Node): Either[String, GoodsReference] =
    Right(
      GoodsReference(
        sequenceNumber = textOpt(n, "sequenceNumber")
          .flatMap(toIntOpt)
          .map(SequenceNumber.apply),
        declarationGoodsItemNumber = textOpt(n, "declarationGoodsItemNumber")
          .flatMap(toIntOpt)
          .map(DeclarationGoodsItemNumber.apply)
      )
    )

  // ---------------- remaining optional sections ----------------

  private def parseActiveBorderTransportMeans(xml: NodeSeq): Either[String, Option[ActiveBorderTransportMeans]] = {
    val maybeNode = (xml \\ "ActiveBorderTransportMeans").headOption
    Right(
      maybeNode.map { n =>
        ActiveBorderTransportMeans(
          typeOfIdentification = textOpt(n, "typeOfIdentification").map(TypeOfIdentification.apply),
          identificationNumber = textOpt(n, "identificationNumber").map(IdentificationNumber.apply),
          nationality = textOpt(n, "nationality").map(Nationality.apply)
        )
      }
    )
  }

  private def parseTransportDocuments(xml: NodeSeq): Either[String, Option[NonEmptyList[TransportDocument]]] = {
    val nodes = (xml \\ "TransportDocument").toList
    sequence(nodes.map(parseTransportDocumentNode)).map(xs => NonEmptyList.fromList(xs))
  }

  private def parseTransportDocumentNode(node: Node): Either[String, TransportDocument] = {
    val sequenceNumber: Option[SequenceNumber] =
      textOpt(node, "sequenceNumber")
        .flatMap(s => Try(s.toInt).toOption)
        .map(SequenceNumber.apply)

    val documentType: Option[TransportDocumentType] =
      textOpt(node, "type")
        .flatMap(s => Try(s.toInt).toOption)
        .map(TransportDocumentType.apply)

    val referenceNumber: Option[ReferenceNumber] =
      textOpt(node, "referenceNumber")
        .map(ReferenceNumber.apply)

    Right(
      TransportDocument(
        sequenceNumber = sequenceNumber,
        transportDocumentType = documentType,
        referenceNumber = referenceNumber
      )
    )
  }
  private def parseGoodsItems(xml: NodeSeq): Either[String, Option[NonEmptyList[GoodsItem]]] = {
    val nodes = (xml \\ "GoodsItem").toList
    sequence(nodes.map(parseGoodsItemNode)).map(xs => NonEmptyList.fromList(xs))
  }

  private def parseGoodsItemNode(n: Node): Either[String, GoodsItem] =
    for {
      goodsMeasure <- req((n \ "Commodity" \ "GoodsMeasure").headOption, "GoodsMeasure")
      gross        <- req(textOpt(goodsMeasure, "grossMass"), "grossMass").flatMap(parseBigDecimal).map(GrossMass.apply)
      net          <- req(textOpt(goodsMeasure, "netMass"), "netMass").flatMap(parseBigDecimal).map(NetMass.apply)
      packaging    <- parsePackaging(n)
    } yield GoodsItem(
      declarationGoodsItemNumber = textOpt(n, "declarationGoodsItemNumber").flatMap(toIntOpt).map(DeclarationGoodsItemNumber.apply),
      referenceNumberUcr = textOpt(n, "referenceNumberUCR").map(_.trim).filter(_.nonEmpty).map(ReferenceNumberUcr.apply),
      commodity = Commodity(grossMass = gross, netMass = net),
      packaging = packaging
    )

  private def parsePackaging(n: Node): Either[String, Option[NonEmptyList[Packaging]]] = {
    val nodes = (n \\ "Packaging").toList
    sequence(nodes.map(parsePackagingNode)).map(xs => NonEmptyList.fromList(xs))
  }

  private def parsePackagingNode(n: Node): Either[String, Packaging] =
    Right(
      Packaging(
        sequenceNumber = textOpt(n, "sequenceNumber").flatMap(toIntOpt).map(SequenceNumber.apply),
        typeOfPackages = textOpt(n, "typeOfPackages").map(TypeOfPackages.apply),
        numberOfPackages = textOpt(n, "numberOfPackages").flatMap(toIntOpt).map(NumberOfPackages.apply),
        shippingMarks = textOpt(n, "shippingMarks").map(ShippingMarks.apply)
      )
    )

  // ---------------- helpers ----------------

  private def textOpt(xml: NodeSeq, tag: String): Option[String] =
    (xml \\ tag).headOption.map(_.text.trim).filter(_.nonEmpty)

  private def textOpt(n: Node, tag: String): Option[String] =
    (n \ tag).headOption.map(_.text.trim).filter(_.nonEmpty)

  private def req[A](opt: Option[A], field: String): Either[String, A] =
    opt.toRight(s"Missing required field: $field")

  private def toIntOpt(s: String): Option[Int] =
    Try(s.toInt).toOption

  private def parseBigDecimal(s: String): Either[String, BigDecimal] =
    Try(BigDecimal(s)).toEither.left.map(_ => s"Invalid decimal: $s")

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
        Try(UUID.fromString(v)).toEither.left.map(_ => s"Invalid UUID: $v").map(Some(_))
    }

  private def sequence[A](xs: List[Either[String, A]]): Either[String, List[A]] =
    xs.foldRight(Right(Nil): Either[String, List[A]]) { (e, acc) =>
      for {
        x <- e
        a <- acc
      } yield x :: a
    }
}
