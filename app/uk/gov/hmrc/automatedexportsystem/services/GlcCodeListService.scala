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
import play.api.Logging
import uk.gov.hmrc.automatedexportsystem.config.AppConfig
import uk.gov.hmrc.automatedexportsystem.csv.CsvResource
import uk.gov.hmrc.automatedexportsystem.errors.CsvLoaderError
import uk.gov.hmrc.automatedexportsystem.models.codelists.GlcCodeListEntry

import javax.inject.{Inject, Singleton}
import scala.concurrent.{Await, Future}
import scala.util.control.NonFatal

@Singleton
class GlcCodeListService @Inject() (
  csvLoader: CsvLoader.GlcCodeListLoader,
  appConfig: AppConfig
) extends Logging:
  val glcEntries: NonEmptyList[GlcCodeListEntry] =
    try
      Await.result(loadGlcCodeList().value, appConfig.csvLoaderStartupLoadTimeInSecondsGlcCodeList) match
        case Left(errors) =>
          throw IllegalStateException(
            s"CsvLoader failed to load GLC code list. " +
              s"errors=[${errors.toList.mkString(", ")}]"
          )
        case Right(glcCodeListEntries) =>
          logger.info("GLC code list successfully loaded")

          glcCodeListEntries
    catch
      case NonFatal(t) =>
        throw RuntimeException("GlcCodeListService failed to initialize", t)

  private def loadGlcCodeList(): EitherT[Future, NonEmptyList[CsvLoaderError], NonEmptyList[GlcCodeListEntry]] =
    csvLoader.load(CsvResource.GlcCodeList)
