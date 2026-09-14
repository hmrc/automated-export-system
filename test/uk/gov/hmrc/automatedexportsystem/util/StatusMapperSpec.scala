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

package uk.gov.hmrc.automatedexportsystem.util

import uk.gov.hmrc.automatedexportsystem.helpers.BaseSpec
import uk.gov.hmrc.automatedexportsystem.models.IE507.ExportOperationType.{Amend, Cancel, Standard}
import uk.gov.hmrc.automatedexportsystem.models.notification.NotificationStatus
import uk.gov.hmrc.automatedexportsystem.models.notification.NotificationStatus.{Accepted, Diversion}
import uk.gov.hmrc.automatedexportsystem.models.responses.AesStatus
import uk.gov.hmrc.automatedexportsystem.models.responses.AesStatus.{Amended, Cancelled}
import uk.gov.hmrc.automatedexportsystem.utils.StatusMapper

class StatusMapperSpec extends BaseSpec:
  "StatusMapper.currentStatus" - {

    "return Awaiting when latest is None" in {
      StatusMapper.currentStatus(Standard, None) shouldBe AesStatus.Awaiting
      StatusMapper.currentStatus(Amend, None)    shouldBe AesStatus.Awaiting
      StatusMapper.currentStatus(Cancel, None)   shouldBe AesStatus.Awaiting
    }

    "return Awaiting when latest is Diversion" in {
      StatusMapper.currentStatus(Standard, Some(Diversion)) shouldBe AesStatus.Awaiting
      StatusMapper.currentStatus(Amend, Some(Diversion))    shouldBe AesStatus.Awaiting
      StatusMapper.currentStatus(Cancel, Some(Diversion))   shouldBe AesStatus.Awaiting
    }

    "map Accepted + Standard to Created" in {
      StatusMapper.currentStatus(Standard, Some(Accepted)) shouldBe AesStatus.Created
    }

    "map Accepted + Amend to Amended" in {
      StatusMapper.currentStatus(Amend, Some(Accepted)) shouldBe Amended
    }

    "map Accepted + Cancel to Cancelled" in {
      StatusMapper.currentStatus(Cancel, Some(Accepted)) shouldBe Cancelled
    }

    "return Rejected for any other status" in {
      StatusMapper.currentStatus(Standard, Some(NotificationStatus.Rejected)) shouldBe AesStatus.Rejected
      StatusMapper.currentStatus(Amend, Some(NotificationStatus.Rejected))    shouldBe AesStatus.Rejected
      StatusMapper.currentStatus(Cancel, Some(NotificationStatus.Rejected))   shouldBe AesStatus.Rejected
    }
  }
