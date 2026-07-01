package uk.gov.hmrc.automatedexportsystem.connectors

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.http.HeaderCarrier

class CodeListConnectorSpec extends AnyWordSpec with Matchers {

  given HeaderCarrier = HeaderCarrier()

  "CodeListConnector" should {

    "call the message types endpoint" in {

    }

    "call the type of locations endpoint" in {

    }

    "call the nationalities endpoint" in {

    }

    "call the transport modes endpoint" in {

    }

    "call the customs office exits endpoint" in {

    }

    "return the response body as a String" in {

    }

    "fail when the downstream service returns an error" in {

    }
  }
}