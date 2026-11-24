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

package uk.gov.hmrc.automatedexportsystem.services

import cats.data.{EitherT, NonEmptyList}
import cats.implicits.{catsSyntaxValidatedId, toTraverseOps}
import org.apache.pekko.NotUsed
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.connectors.csv.scaladsl.CsvToMap
import org.apache.pekko.stream.scaladsl.{Sink, Source}
import org.apache.pekko.util.ByteString
import uk.gov.hmrc.automatedexportsystem.csv.{CsvResource, CsvResourceReader, CsvRowMapper, GlcCodeListRowMapper}
import uk.gov.hmrc.automatedexportsystem.errors.CsvLoaderError
import uk.gov.hmrc.automatedexportsystem.models.codelists.GlcCodeListEntry

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CsvLoader[U, T <: CsvRowMapper[U]] @Inject() (
  csvRowMapper:      T,
  csvResourceReader: CsvResourceReader
)(using actorSystem: ActorSystem):
  given ec: ExecutionContext = actorSystem.getDispatcher

  def load(csv: CsvResource): EitherT[Future, NonEmptyList[CsvLoaderError], NonEmptyList[U]] =
    val csvStream: EitherT[Future, NonEmptyList[CsvLoaderError], Source[List[ByteString], NotUsed]] =
      EitherT
        .fromEither[Future](csvResourceReader.read(csv))
        .leftMap(error => NonEmptyList.one(CsvLoaderError.ReadFailed(error)))

    val rows: EitherT[Future, NonEmptyList[CsvLoaderError], NonEmptyList[U]] =
      csvStream.flatMap(csvSource =>
        EitherT(
          csvSource
            .via(CsvToMap.toMapAsStrings())
            .zipWithIndex
            .map { case (row, idx) =>
              csvRowMapper
                .mapRow(row)
                .leftMap(errors =>
                  val expectedType: String = csvRowMapper.ct.runtimeClass.getSimpleName

                  NonEmptyList.one(
                    CsvLoaderError.MappingFailed(
                      idx,
                      expectedType,
                      errors
                    )
                  )
                )
            }
            .runWith(Sink.seq)
            .map(
              _.sequence
                .andThen(mappedRows =>
                  val mappedRowsList: List[U] = mappedRows.toList

                  NonEmptyList.fromList(mappedRowsList) match
                    case Some(nel) => nel.validNel
                    case None      => CsvLoaderError.EmptyCsvResource(csv.path).invalidNel
                )
                .toEither
            )
        )
      )

    rows
  end load

object CsvLoader:
  type GlcCodeListLoader = CsvLoader[GlcCodeListEntry, GlcCodeListRowMapper]
