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
import cats.implicits.catsSyntaxValidatedId
import org.apache.pekko.NotUsed
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.scaladsl.Source
import org.apache.pekko.util.ByteString
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, EitherValues}
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.automatedexportsystem.csv.{CsvResource, CsvResourceReader, CsvRowMapper}
import uk.gov.hmrc.automatedexportsystem.errors.{CsvLoaderError, CsvMapperError, CsvResourceReaderError}

import scala.reflect.ClassTag

class CsvLoaderSpec extends AnyFreeSpecLike, Matchers, ScalaFutures, EitherValues, MockitoSugar, BeforeAndAfterAll:
  given actorSystem: ActorSystem = ActorSystem("CsvLoaderSpec")

  object TestData:
    final case class TestClass(a: String, b: Int)

    val testClassCsvResource: CsvResource = CsvResource.Custom("test.csv")

  trait Setup:
    val csvResourceReader: CsvResourceReader = mock

    val csvRowMapper: CsvRowMapper[TestData.TestClass] = mock

    val csvLoader: CsvLoader[TestData.TestClass, CsvRowMapper[TestData.TestClass]] =
      CsvLoader(csvRowMapper, csvResourceReader)
  end Setup

  override protected def afterAll(): Unit =
    actorSystem.terminate().futureValue
    super.afterAll()

  "CsvLoader" - {

    ".load" - {

      "should return a list of mapped rows" - {

        "when the CSV resource is successfully read and parsed" in new Setup {
          val source: Source[List[ByteString], NotUsed] = Source(
            Seq(
              List(ByteString("a"), ByteString("b")),
              List(ByteString("a1"), ByteString("1")),
              List(ByteString("a2"), ByteString("2")),
              List(ByteString("a3"), ByteString("3"))
            )
          )

          val tc1: TestData.TestClass = TestData.TestClass("a1", 1)
          val tc2: TestData.TestClass = TestData.TestClass("a2", 2)
          val tc3: TestData.TestClass = TestData.TestClass("a3", 3)

          when(csvResourceReader.read(CsvResource.Custom("test.csv")))
            .thenReturn(Right(source))

          when(csvRowMapper.mapRow(Map("a" -> "a1", "b" -> "1")))
            .thenReturn(tc1.validNel)

          when(csvRowMapper.mapRow(Map("a" -> "a2", "b" -> "2")))
            .thenReturn(tc2.validNel)

          when(csvRowMapper.mapRow(Map("a" -> "a3", "b" -> "3")))
            .thenReturn(tc3.validNel)

          val result: NonEmptyList[TestData.TestClass] =
            csvLoader.load(TestData.testClassCsvResource).value.futureValue.value

          result shouldBe NonEmptyList.of(tc1, tc2, tc3)
        }
      }

      "should return a NEL of CsvLoaderError" - {

        "when the CSV resource is not found" in new Setup {
          val csvResourceReaderError: CsvResourceReaderError =
            CsvResourceReaderError.CsvResourceNotFound("test.csv")

          when(csvResourceReader.read(CsvResource.Custom("test.csv")))
            .thenReturn(Left(csvResourceReaderError))

          val result: NonEmptyList[CsvLoaderError] =
            csvLoader.load(TestData.testClassCsvResource).value.futureValue.left.value

          result shouldBe NonEmptyList.one(CsvLoaderError.ReadFailed(csvResourceReaderError))
        }

        "when the CSV resource is empty" in new Setup {
          when(csvResourceReader.read(CsvResource.Custom("test.csv")))
            .thenReturn(Right(Source(Seq.empty)))

          val result: NonEmptyList[CsvLoaderError] =
            csvLoader.load(TestData.testClassCsvResource).value.futureValue.left.value

          result shouldBe NonEmptyList.one(
            CsvLoaderError.EmptyCsvResource(TestData.testClassCsvResource.path)
          )
        }

        "when the CSV resource cannot be mapped successfully" in new Setup {
          val source: Source[List[ByteString], NotUsed] = Source(
            Seq(
              List(ByteString("a"), ByteString("b")),
              List(ByteString("a1")),
              List(ByteString("a2"), ByteString("2")),
              List(ByteString("a3"), ByteString("3"))
            )
          )

          val csvMapperError1: CsvMapperError = CsvMapperError.MissingColumn("b")
          val csvMapperError2: CsvMapperError = CsvMapperError.ParseError("parse error 1", "a")
          val csvMapperError3: CsvMapperError = CsvMapperError.ParseError("parse error 2", "b")

          when(csvResourceReader.read(CsvResource.Custom("test.csv")))
            .thenReturn(Right(source))

          val testClassCT: ClassTag[TestData.TestClass] =
            ClassTag(classOf[TestData.TestClass])

          when(csvRowMapper.ct).thenReturn(testClassCT)

          when(csvRowMapper.mapRow(Map("a" -> "a1")))
            .thenReturn(csvMapperError1.invalidNel)

          when(csvRowMapper.mapRow(Map("a" -> "a2", "b" -> "2")))
            .thenReturn(csvMapperError2.invalidNel)

          when(csvRowMapper.mapRow(Map("a" -> "a3", "b" -> "3")))
            .thenReturn(csvMapperError3.invalidNel)

          val csvLoaderError1: CsvLoaderError =
            CsvLoaderError.MappingFailed(0, "TestClass", NonEmptyList.one(csvMapperError1))
          val csvLoaderError2: CsvLoaderError =
            CsvLoaderError.MappingFailed(1, "TestClass", NonEmptyList.one(csvMapperError2))
          val csvLoaderError3: CsvLoaderError =
            CsvLoaderError.MappingFailed(2, "TestClass", NonEmptyList.one(csvMapperError3))

          val result: NonEmptyList[CsvLoaderError] =
            csvLoader.load(TestData.testClassCsvResource).value.futureValue.left.value

          result shouldBe NonEmptyList.of(csvLoaderError1, csvLoaderError2, csvLoaderError3)
        }
      }
    }
  }
