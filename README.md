Libre Metrics Service
=====================

This project is being released as partial fullfillment of NSF award number ARC
0946625 and NASA award number NNX10AE07A.  This is non-working software and has
not been previously distributed.  All of the code references to internal
networks and servers have been removed throughout the project.  Any potential
user will have to modify the source for their environemnt in order to use this
code.  License information is provided in the LICENSE.txt file.

## About

Libre Metrics is a reusable, repurposabe RESTful web service to record usage of
services.  It exposes a RESTful interface to allow many types of clients to
record usages.  It can easily be used to allow you to generate and record
metrics for interactions and usages of web interfaces.


## Metrics API

In the example URLs below, the example.com domain is used. 

Also, anyplace that has `{xxx}` is a placeholder that should be populated with
specific information, as described in more detail for each service.

For services that have Query String parameters, simply append
`?{param}={value}&{param}={value}` filling in param and value as appropriate
for the service.

### Metrics Recording

To record a metric using this service send a POST request to the following URL:

http://example.com/libre/services/metrics/projects/{project}/services/{service}/instances/{instance}

* `{project}` = The name of the project/sponsor (eg. Libre)
* `{service}` = The name of the actual service (eg. PICBadge)
* `{instance}` = The name of the host instance (eg. Prod)

The content of the POST request is XML representing the data sample to record.
Make sure to indicate `Content-Type: application/xml` in the request header!

Here is an example of a metric XML (note, the order of fields isn't important):

    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <sample>
      <service>
        <sponsor>Libre</sponsor>
        <serviceName>PICBadge</serviceName>
        <instance>Prod</instance>
      </service>
      <entryTs>2010-09-23T01:51:35.994-0600</entryTs>
      <ipAddress>128.138.135.105</ipAddress>
      <userAgent>Mozilla/5.0(X11;U;Linuxi686;en-US;rv:1.9.1.11)Gecko/20100714SUSE/3.5.11-0.1.1Firefox/3.5.11</userAgent>
      <sessionId>eda8a38e96f890876cc74438c4c6</sessionId>
      <metrics>
        <name>LicenseType</name>
        <value>cc0</value>
      <metrics>
      <metrics>
        <name>Something Else</name>
        <value>Blah</value>
      </metrics>
    </sample>

The "service" tag (and sub-tags) are optional, and are in fact ignored; The URL determines these values.

* `entryTs` - A Timestamp of when the event being recorded happened. This should be in ISO 8601 format.
* `ipAddress` - The User's IP address (or host name)
* `userAgent` - Information on the agent used by the User (such as a browser)
* `sessionId` - Optional; Can be none, or can be the ID for a previous session (the latter is useful if you wish to record more metrics for a single sample using different requests.
* `metrics` - There can be any number of these tags (with name/value subtags). Each represents a distinct metric to record for this sample.
  * `name` - The name of the metric being recorded. Reports can use this name to aggregate results
  * `value` - The value of the metric. Report aggregations will use distinct values to determine counts

#### Java Client

[Libre Metrics Client](https://github.com/nsidc/libre-metrics-client) is a Java
Client library that can be used to abstract the calling of the Metrics service
directly, making it easier to incorporate metrics reporting in other Java
projects.

### Metrics Reporting

#### Get Valid Reporting Fields

To get a list of valid fields that are available for a report, use the following URL:

http://example.com/libre/services/metrics/projects/{project}/services/{service}/instances/{instance}/fields

* `{project}` = The name of the project/sponsor (eg. Libre)
* `{service}` = The name of the actual service (eg. PICBadge)
* `{instance}` = The name of the host instance (eg. Prod)

Query Parameters:

* `start` - A date in yyyy-MM-dd format; if used, only fields for metrics recorded on or after this date will be shown
* `stop` - A date in yyyy-MM-dd format; if used, only fields for metrics recorded before this date will be shown (fields for metrics recorded ON that date only will not be shown)

The below is a sample response using http://example.com/libre/services/metrics/projects/Libre/services/PICBadge/instances/Prod/fields?start=2010-11-01&stop=2010-12-01

    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <fieldList>
      <field>users</field>
      <field>LicenseType</field>
    </fieldList>

The below is a sample query for a service called CollectionCaster: http://example.com/libre/services/metrics/projects/Libre/services/CollectionCaster/instances/Prod/reports/users

The actual fields returned will depend on what metrics were recorded. The users field, however, is always reported as it is a special case.

#### Get a Report for specified fields

To get a report for previous metrics, use the following URL:

http://example.com/libre/services/metrics/projects/{project}/services/{service}/instances/{instance}/reports/{field}...

* `{project}` = The name of the project/sponsor (eg. Libre)
* `{service}` = The name of the actual service (eg. PICBadge)
* `{instance}` = The name of the host instance (eg. Prod)
* `{field}` = The name of the field to report on. The `...` indicates you can have more than one field. Any number of fields can be listed, in any order. For example:
  * `.../reports/users`
  * `.../reports/users/LicenseType`
  * `.../reports/LicenseType/users`

Query Parameters:

* `start`: A date in yyyy-MM-dd format; if used, only metrics recorded on or after this date will be shown
* `stop`: A date in yyyy-MM-dd format; if used, only metrics recorded before this date will be shown (metrics recorded on this date will NOT be shown)

The resulting XML will have the following structure:

* `report` - root level tag
  * ATTRIBUTES:
    * `project` - the name of the project/sponsor (should match the project specified in the URL)
    * `service` - the name of the web service (should match the service specified in the URL)
    * `instance` - the name of the instance (should match the instance specified in the URL)
  * CHILD TAGS:
    * `reportFields` - a list of the fields reported, as specified in the URL
      * CHILD TAGS, field - The name of the field
    * parameters - a list of the query parameters used, as specified in the URL. If none were provided, this tag will not be present
      * CHILD TAGS, parameter - Information on the parameter
        * ATTRIBUTES, name - the name of the parameter
        * CHILD TEXT VALUE - The value of the parameter
    * rows - a list of all the "rows" of the report
      * CHILD TAGS, row - An individual row
        * ATTRIBUTES, count - The number of matching reported metrics for this row
        * CHILD TAGS, column - One or more columns which, combined, tell you what the count consists of
        * ATTRIBUTES
          * `name` - The name of the column. Will usually be the name of a metric. However, one row will always be a "total" row, and have only a "total" column.
          * `value` - The value of metric reported for this row. It's the value that will determine what the row's count means

The "user" field report will list "domain" as a field name. This domain is
based on the NASA specifications, such as ".gov", "unresolved", etc. Note that
because of this, if using the "user" field the total row's count may not add up
to the individual other rows, as some domains have overlap. For instance,
".nasa.gov" will contain all those coming from that host, but ".gov" will
contain ALL .gov counts, including those from ".nasa.gov".

The below is a sample response using  http://example.com/libre/services/metrics/projects/Libre/services/PICBadge/instances/Prod/reports/users?start=2010-11-01&stop=2010-12-01 (to get a report for November, 2010):

    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <report project="Libre" service="PICBadge" instance="Prod">
      <reportFields>
        <field>users</field>
      </reportFields>
      <parameters>
        <parameter name="start">2010-11-01</parameter>
        <parameter name="stop">2010-12-01</parameter>
      </parameters>
      <rows>
        <row count="18">
          <column name="domain" value="unresolved"/>
        </row>
        <row count="18">
          <column name="total" value="total"/>
        </row>
      </rows>
    </report>

In this report, there were 18 metrics recorded in Nov 2010. All 18 were from an unresolved domain.

Another example, for http://example.com/libre/services/metrics/projects/Libre/services/PICBadge/instances/Prod/reports/users/LicenseType?start=2010-11-01&stop=2010-12-01

    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <report project="Libre" service="PICBadge" instance="Prod">
      <reportFields>
        <field>users</field>
        <field>LicenseType</field>
      </reportFields>
      <parameters>
        <parameter name="start">2010-11-01</parameter>
        <parameter name="stop">2010-11-30</parameter>
      </parameters>
      <rows>
        <row count="9">
          <column name="LicenseType" value="pic_cc_zero"/>
          <column name="domain" value="unresolved"/>
        </row>
        <row count="18">
          <column name="total" value="total"/>
        </row>
        <row count="9">
          <column name="LicenseType" value="pic_cc_by"/>
          <column name="domain" value="unresolved"/>
        </row>
      </rows>
    </report>

In this report, you again have 18 total metrics for Nov 2010. This time, broken
out by license type and user combined, you get 9 of them for the CC0 license,
with an unknown domain; and the other 9 for the CCBY license with an unknown
domain.

The below is a sample response using http://example.com/libre/services/metrics/projects/searchlight/services/opensearch/instances/Prod/reports/users

    <report project="searchlight" service="opensearch" instance="Prod">
       <reportFields>
          <field>users</field>
       </reportFields>
       <rows>
          <row count="2">
             <column name="domain" value="non-US"/>
          </row>
          <row count="2">
             <column name="total" value="total"/>
          </row>
       </rows>
    </report>



## Compilation notes:

This package was created using maven 2.2.0 which has some compatibility issues
with the latest maven release. In case you run into these issues try with maven
2.2.0

The `net.sf.javainetlocator` package is not included but it is necessary to
build the client. It can be downloaded from:
http://javainetlocator.sourceforge.net/  


## Credit

This software was developed by the National Snow and Ice Data Center under NSF
award number ARC 0946625 and NASA award number NNX10AE07A.

