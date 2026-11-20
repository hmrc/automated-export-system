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

import cats.data.NonEmptyList
import com.google.inject.{AbstractModule, CreationException, TypeLiteral}
import helpers.EitherTFutureOps.toEitherTLeft
import org.mockito.Mockito.when
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.automatedexportsystem.csv.CsvResource
import uk.gov.hmrc.automatedexportsystem.errors.{CsvLoaderError, CsvMapperError}

class GlcCodeListServiceISpec extends AnyFreeSpecLike, Matchers, MockitoSugar:
  trait Setup:
    val glcCodeListLoader: CsvLoader.GlcCodeListLoader = mock

  "GlcCodeListService" - {

    "should fail fast at startup" - {

      "when the CsvLoader returns unsuccessfully" in new Setup {
        when(glcCodeListLoader.load(CsvResource.GlcCodeList))
          .thenReturn(
            NonEmptyList
              .of(
                CsvLoaderError.MappingFailed(
                  0,
                  "expectedType",
                  NonEmptyList.one(
                    CsvMapperError.MissingColumn("missingColumn")
                  )
                )
              )
              .toEitherTLeft
          )

        lazy val app: Application = GuiceApplicationBuilder()
          .overrides(new AbstractModule {
            override def configure(): Unit =
              bind(new TypeLiteral[CsvLoader.GlcCodeListLoader]() {}).toInstance(glcCodeListLoader)
          })
          .build()

        val exception: CreationException = intercept(app)

        exception.getCause.getMessage          shouldBe "GlcCodeListService failed to initialize"
        exception.getCause.getCause.getMessage shouldBe "CsvLoader failed to load GLC code list. " +
          "errors=[MappingFailed(0,expectedType,NonEmptyList(MissingColumn(missingColumn)))]"
      }
    }
  }
