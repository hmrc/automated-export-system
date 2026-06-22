package helpers

import org.mockito.Mockito.reset
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.automatedexportsystem.config.AppConfig

trait AllMocks extends MockitoSugar with BeforeAndAfterEach {
  me: org.scalatest.Suite =>
  val mockAppConfig: AppConfig = mock[AppConfig]

  abstract override protected def beforeEach(): Unit = {
    super.beforeEach()

    Seq[AnyRef](
      mockAppConfig
    ).foreach(reset(_))
  }
}
