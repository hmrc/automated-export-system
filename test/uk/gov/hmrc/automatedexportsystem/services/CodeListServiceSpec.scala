package uk.gov.hmrc.automatedexportsystem.services

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.http.HeaderCarrier

class CodeListServiceSpec extends AnyWordSpec with Matchers {

  given HeaderCarrier = HeaderCarrier()

  "CodeListService" should {

    "return a MessageTypeCodeList from message type XML" in {

    }

    "return a TypeOfLocationCodeList from type of location XML" in {

    }

    "return a NationalityCodeList from nationality XML" in {

    }

    "return a TransportModeCodeList from transport mode XML" in {

    }

    "return a CustomsOfficeExitCodeList from customs office exit XML" in {

    }

    "filter out expired code list values" in {

    }

    "keep currently valid code list values" in {

    }

    "keep code list values with no end date" in {

    }

    "return an empty collection when no valid values are found" in {

    }

    "propagate connector failures" in {

    }
  }
}