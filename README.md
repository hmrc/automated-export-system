
# automated-export-system

This is a protected microservices that allows third parties to interact with the AES system. 

It allows users to:
* Create an IE507A submission for goods passing through Northern Ireland
* Amend an IE507A submission
* Cancel and IE507 submission

* Retrieve a list of all submissions made by the user
* Retrieve the details of a single submission by submission ID

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").

### Running locally
By default the service runs on port 5002

## Using service manager 2
Start the service and its dependencies in service manager 2 by running `sm2 --start AUTOMATED_EXPORT_SYSTEM_ALL`

## Using sbt
For local development, use `sbt run`. If it is already running in sm2, then that needs to be stopped.

## Test only endpoints 
To use locally, `sbt run -Dapplication.router=testOnlyDoNotUseInAppConf.Routes`
To use with sm2, add this line `-Dapplication.router=testOnlyDoNotUseInAppConf.Routes` to the microservices profile


### Running all tests and coverage report
To run all tests and generate a coverage report, use this command
`sbt scalafmt test:scalafmt it:test::scalafmt coverage test it/test coverageReport`

### Endpoints
## Headers required are `Authorization: Bearer <token>` and `Content-Type: application/xml

## Submissions
`POST /message`: This is the submission from the UI to CREATE, AMEND or CANCEL an IE507 submission
```<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <aes:Submission xmlns:aes="http://ecs.dgtaxud.ec">
        <submissionId>700d2c74-f313-4dfb-89d3-0f9ad1bb9377</submissionId>
        <ExportOperation>
            <type>1</type>
            <MRN>26GB0000X6524786A9</MRN>
            <discrepanciesExist>0</discrepanciesExist>
            <splitIndicator>0</splitIndicator>
        </ExportOperation>
        <CustomsOfficeOfExitActual>
            <referenceNumber>IEARK100</referenceNumber>
        </CustomsOfficeOfExitActual>
        <GoodsShipment>
            <Consignment>
                <modeOfTransportAtTheBorder>1</modeOfTransportAtTheBorder>
                <referenceNumberUCR>6GB536187624189-S458</referenceNumberUCR>
                <parentUCRID>GB/ABC-12345</parentUCRID>
                <TransportEquipment>
                    <sequenceNumber>1</sequenceNumber>
                    <containerIdentificationNumber>CONT1234567890123</containerIdentificationNumber>
                    <numberOfSeals>2</numberOfSeals>
                    <Seal>
                        <sequenceNumber>1</sequenceNumber>
                        <identifier>SEAL123</identifier>
                    </Seal>
                    <Seal>
                        <sequenceNumber>2</sequenceNumber>
                        <identifier>SEAL124</identifier>
                    </Seal>
                    <GoodsReference>
                        <sequenceNumber>1</sequenceNumber>
                        <declarationGoodsItemNumber>1</declarationGoodsItemNumber>
                    </GoodsReference>
                    <GoodsReference>
                        <sequenceNumber>2</sequenceNumber>
                        <declarationGoodsItemNumber>10</declarationGoodsItemNumber>
                    </GoodsReference>
                    <GoodsReference>
                        <sequenceNumber>3</sequenceNumber>
                        <declarationGoodsItemNumber>11</declarationGoodsItemNumber>
                    </GoodsReference>
                    <GoodsReference>
                        <sequenceNumber>4</sequenceNumber>
                        <declarationGoodsItemNumber>12</declarationGoodsItemNumber>
                    </GoodsReference>
                </TransportEquipment>
                <LocationOfGoods>
                    <typeOfLocation>A</typeOfLocation>
                    <qualifierOfIdentification>B</qualifierOfIdentification>
                    <authorisationNumber>AUTH12345</authorisationNumber>
                    <additionalIdentifier>AD01</additionalIdentifier>
                    <UNLocode>UNLOCODE123</UNLocode>
                </LocationOfGoods>
                <ActiveBorderTransportMeans>
                    <typeOfIdentification>20</typeOfIdentification>
                    <identificationNumber>IDNUMBER123</identificationNumber>
                    <nationality>GB</nationality>
                </ActiveBorderTransportMeans>
                <TransportDocument>
                    <sequenceNumber>1</sequenceNumber>
                    <type>2</type>
                    <referenceNumber>REF123</referenceNumber>
                </TransportDocument>
                <TransportDocument>
                    <sequenceNumber>2</sequenceNumber>
                    <type>2</type>
                    <referenceNumber>REF124</referenceNumber>
                </TransportDocument>
            </Consignment>
            <GoodsItem>
                <declarationGoodsItemNumber>2</declarationGoodsItemNumber>
                <referenceNumberUCR>4AA09AZ(-//)</referenceNumberUCR>
                <Commodity>
                    <GoodsMeasure>
                        <grossMass>1000.500000</grossMass>
                        <netMass>900.500000</netMass>
                    </GoodsMeasure>
                </Commodity>
                <Packaging>
                    <sequenceNumber>1</sequenceNumber>
                    <typeOfPackages>PA</typeOfPackages>
                    <numberOfPackages>10</numberOfPackages>
                    <shippingMarks>MARKS123</shippingMarks>
                </Packaging>
                <Packaging>
                    <sequenceNumber>2</sequenceNumber>
                    <typeOfPackages>PA</typeOfPackages>
                    <numberOfPackages>10</numberOfPackages>
                    <shippingMarks>MARKS1234</shippingMarks>
                </Packaging>
            </GoodsItem>
            <GoodsItem>
                <declarationGoodsItemNumber>1</declarationGoodsItemNumber>
                <Commodity>
                    <GoodsMeasure>
                        <grossMass>1000.500000</grossMass>
                        <netMass>900.500000</netMass>
                    </GoodsMeasure>
                </Commodity>
                <Packaging>
                    <sequenceNumber>1</sequenceNumber>
                    <typeOfPackages>PA</typeOfPackages>
                    <numberOfPackages>10</numberOfPackages>
                    <shippingMarks>MARKS123</shippingMarks>
                </Packaging>
                <Packaging>
                    <sequenceNumber>2</sequenceNumber>
                    <typeOfPackages>PA</typeOfPackages>
                    <numberOfPackages>10</numberOfPackages>
                    <shippingMarks>MARKS1234</shippingMarks>
                </Packaging>
            </GoodsItem>
        </GoodsShipment>
    </aes:Submission>```

`POST /notification`: This is the async response from EIS/CDS
```<?xml version="1.0" encoding="UTF-8" ?>
<notification>
  <correlationId>1c9a77e2-1111-4f22-9abc-abcdef123456</correlationId>
  <eori>GB987654321000</eori>
  <mrn>25GB0987654321ZYXWV</mrn>
  <dateCreated>2026-08-12T10:16:10Z</dateCreated>
  <status>2</status>
  <errors>
    <error>
      <code>INVALID_SEQUENCE_NUMBER</code>
      <description>Invalid goods item number</description>
      <path>/Declaration/GoodsShipment/GoodsItem[3]</path>
    </error>
    <error>
      <code>WRONG_MRN</code>
      <description>MRN is incorrect</description>
      <originalValue>GB12</originalValue>
    </error>
  </errors>
</notification>```

`GET /submissions`: List of all submissions for the logged in user
```<?xml version="1.0" encoding="UTF-8"?>
<Submissions>
  <Submission>

    <submissionId>123e4567-e89b-12d3-a456-426614174000</submissionId>
    <mrn>MRN1234567890ABCDEF</mrn>
    <ducr>DUCR1234567890XYZ</ducr>
    <officeOfExitCode>GB000123</officeOfExitCode>
    <preparationDateAndTime>2026-07-07T10:15:30</preparationDateAndTime>
    <status>Accepted</status>
  </Submission>
  <Submission>

    <submissionId>123e4567-e89b-12d3-a456-426614174123</submissionId>
    <mrn>A1B2C3D4E5F6G7H8I9J0</mrn>
    <ducr>DUCR-SECOND-0001</ducr>
    <officeOfExitCode>GB000456</officeOfExitCode>
    <preparationDateAndTime>2026-07-07T11:45:00</preparationDateAndTime>
    <status>Rejected</status>
  </Submission>
</Submissions>```

`GET /submission/:submissionId`

```<?xml version="1.0" encoding="utf-8"?>
<Submission>
    <submissionId>635a2504-aecc-4469-a704-df13257b740a</submissionId>
    <status>1</status>
    <ExportOperation>
        <type>1</type>
        <MRN>26GB408875704648A9</MRN>
        <discrepanciesExist>0</discrepanciesExist>
        <splitIndicator>0</splitIndicator>
    </ExportOperation>
    <CustomsOfficeOfExitActual>
        <referenceNumber>XI000142</referenceNumber>
    </CustomsOfficeOfExitActual>
    <GoodsShipment>
        <Consignment>
            <referenceNumberUCR>5GB000000000000-12345</referenceNumberUCR>
            <parentUCRID>GB/000000000000-12345</parentUCRID>
            <LocationOfGoods>
                <typeOfLocation>A</typeOfLocation>
                <qualifierOfIdentification>Y</qualifierOfIdentification>
                <authorisationNumber>AUTH12345</authorisationNumber>
                <additionalIdentifier>AD01</additionalIdentifier>
                <UNLocode>UN123</UNLocode>
            </LocationOfGoods>
        </Consignment>
    </GoodsShipment>
    <updatedAt>2026-10-23T10:04:02.518</updatedAt>
</Submission>```

## Code lists

`GET  /codelists/nationality`
```<codeList>
    <item>
        <name>AD</name>
        <description>Andorra</description>
        <startDate>2025-11-01T00:00:00</startDate>
    </item>
    <item>
        <name>AE</name>
        <description>United Arab Emirates</description>
        <startDate>2025-11-01T00:00:00</startDate>
    </item>
    <item>
        <name>AF</name>
        <description>Afghanistan</description>
        <startDate>2025-11-01T00:00:00</startDate>
    </item>
    <item>
    ...```
    
`GET  /codelists/transportmode`
```<codeList>
    <item>
        <name>1</name>
        <description>Sea transport</description>
        <startDate>2023-03-04T00:00:00</startDate>
    </item>
    <item>
        <name>2</name>
        <description>Rail transport</description>
        <startDate>2025-04-24T00:00:00</startDate>
    </item>
    <item>
        <name>3</name>
        <description>Road transport</description>
        <startDate>2025-04-24T00:00:00</startDate>
    </item>
    <item>
        <name>4</name>
        <description>Air transport</description>
        <startDate>2025-04-24T00:00:00</startDate>
    </item>
    ...
    ```
    
`GET  /codelists/typeoflocation`
```<codeList>
    <item>
        <name>A</name>
        <description>Designated location</description>
        <startDate>2025-11-01T00:00:00</startDate>
    </item>
    <item>
        <name>B</name>
        <description>Authorised place</description>
        <startDate>2025-11-01T00:00:00</startDate>
    </item>
    <item>
        <name>C</name>
        <description>Approved place</description>
        <startDate>2025-11-01T00:00:00</startDate>
    </item>
    <item>
        <name>D</name>
        <description>Other</description>
        <startDate>2025-11-01T00:00:00</startDate>
    </item>
</codeList>```