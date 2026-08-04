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

package uk.gov.hmrc.automatedexportsystem.models.responses

import helpers.XmlOps
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers
import uk.gov.hmrc.automatedexportsystem.models.aesIE507.*

import java.time.LocalDateTime
import java.util.UUID
import scala.xml.Elem

class SubmissionSummaryListSpec extends AnyFreeSpecLike, Matchers:
  object TestData:
    val id: UUID = UUID.fromString("6fb33641-6dc7-4a4f-adef-06238c13a317")

    val dateTime: LocalDateTime = LocalDateTime.parse("2026-07-31T00:00:00")

    val submissionSummary: SubmissionSummary =
      SubmissionSummary(
        submissionId = SubmissionId(id),
        mrn = Mrn("mrn"),
        ducr = Some(ReferenceNumberUcr("referenceNumberUcr")),
        officeOfExitCode = ReferenceNumber("referenceNumber"),
        updatedAt = dateTime,
        status = ExportOperationType.Standard
      )

    val submissionSummaryNoDucr: SubmissionSummary =
      SubmissionSummary(
        submissionId = SubmissionId(id),
        mrn = Mrn("mrn"),
        ducr = None,
        officeOfExitCode = ReferenceNumber("referenceNumber"),
        updatedAt = dateTime,
        status = ExportOperationType.Standard
      )

    val submissionSummaryList: SubmissionSummaryList =
      SubmissionSummaryList(List(submissionSummary, submissionSummaryNoDucr))

    val submissionSummaryListEmpty: SubmissionSummaryList = SubmissionSummaryList(Nil)

  "SubmissionSummary" - {

    ".toXml" - {

      "should render the submission into an XML document" - {

        "when the ducr is present" in {
          val xml: Elem =
            <Submission>
              <submissionId>{TestData.id}</submissionId>
              <mrn>mrn</mrn>
              <ducr>referenceNumberUcr</ducr>
              <officeOfExitCode>referenceNumber</officeOfExitCode>
              <updatedAt>2026-07-31T00:00:00</updatedAt>
              <status>1</status>
            </Submission>

          XmlOps.normalize(TestData.submissionSummary.toXml).toString shouldBe
            XmlOps.normalize(xml).toString
        }

        "when the ducr is not present" in {
          val xml: Elem =
            <Submission>
              <submissionId>
                {TestData.id}
              </submissionId>
              <mrn>mrn</mrn>
              <officeOfExitCode>referenceNumber</officeOfExitCode>
              <updatedAt>2026-07-31T00:00:00</updatedAt>
              <status>1</status>
            </Submission>

          XmlOps.normalize(TestData.submissionSummaryNoDucr.toXml).toString shouldBe
            XmlOps.normalize(xml).toString
        }
      }
    }
  }

  "SubmissionSummaryList" - {

    ".toXml" - {

      "should render the submission list into an XML document" - {

        "when the list contains multiple elements" in {
          val xml: Elem =
            <Submissions>
              <Submission>
                <submissionId>{TestData.id}</submissionId>
                <mrn>mrn</mrn>
                <ducr>referenceNumberUcr</ducr>
                <officeOfExitCode>referenceNumber</officeOfExitCode>
                <updatedAt>2026-07-31T00:00:00</updatedAt>
                <status>1</status>
              </Submission>
              <Submission>
                <submissionId>{TestData.id}</submissionId>
                <mrn>mrn</mrn>
                <officeOfExitCode>referenceNumber</officeOfExitCode>
                <updatedAt>2026-07-31T00:00:00</updatedAt>
                <status>1</status>
              </Submission>
            </Submissions>

          XmlOps.normalize(TestData.submissionSummaryList.toXml).toString shouldBe
            XmlOps.normalize(xml).toString
        }

        "when the list is empty" in {
          val xml: Elem =
            <Submissions>
            </Submissions>

          XmlOps.normalize(TestData.submissionSummaryListEmpty.toXml).toString shouldBe
            XmlOps.normalize(xml).toString
        }
      }
    }
  }
