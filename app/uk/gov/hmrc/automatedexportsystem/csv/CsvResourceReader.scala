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
class CsvResourceReader @Inject()(env: Environment):
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
