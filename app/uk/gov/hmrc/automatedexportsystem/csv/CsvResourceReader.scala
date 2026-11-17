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

package uk.gov.hmrc.automatedexportsystem.csv

import org.apache.pekko.stream.IOResult
import org.apache.pekko.stream.connectors.csv.scaladsl.CsvParsing
import org.apache.pekko.stream.scaladsl.{Source, StreamConverters}
import org.apache.pekko.util.ByteString
import play.api.Environment
import uk.gov.hmrc.automatedexportsystem.errors.CsvResourceReaderError

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class CsvResourceReader @Inject() (env: Environment):
  def read(csv: CsvResource): Either[CsvResourceReaderError, Source[List[ByteString], Future[IOResult]]] =
    val csvPath: String = csv.path

    val resource =
      env
        .resourceAsStream(csvPath)
        .toRight(CsvResourceReaderError.CsvResourceNotFound(csvPath))

    resource
      .map(csv =>
        StreamConverters
          .fromInputStream(() => csv)
          .via(CsvParsing.lineScanner())
      )
