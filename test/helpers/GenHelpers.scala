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

package helpers

import org.scalacheck.Gen
import uk.gov.hmrc.automatedexportsystem.models.aesIE507.{EoriNumber, ExportOperationType, SubmissionId}
import uk.gov.hmrc.automatedexportsystem.models.mongo.write.MongoAesIE507Message

import java.time.Instant

trait GenHelpers:
  extension (mongoAesIE507MessageGen: Gen[MongoAesIE507Message])
    def withEori(eoriNumber: EoriNumber): Gen[MongoAesIE507Message] =
      mongoAesIE507MessageGen.map(_.copy(eoriNumber = eoriNumber))

    def withSubmissionId(submissionId: SubmissionId): Gen[MongoAesIE507Message] =
      mongoAesIE507MessageGen.map(_.copy(submissionId = submissionId))

    def withExportOperationType(exportOperationType: ExportOperationType): Gen[MongoAesIE507Message] =
      mongoAesIE507MessageGen.map(m => m.copy(exportOperation = m.exportOperation.copy(exportOperationType = exportOperationType)))

    def withUpdatedAt(updatedAt: Instant): Gen[MongoAesIE507Message] =
      mongoAesIE507MessageGen.map(_.copy(updatedAt = updatedAt))

object GenHelpers extends GenHelpers
