Maestro: Using
============

Maestro tests are written in Groovy. By default, it already comes with a library of tests 
for single and multi host testing. Additional tests can be written by using the provided 
test API. 

Preparing for the Test Execution
----

The test parameters are exported as environment variables.  It is done this way to facilitate
the integration with continuous integration systems such as Jenkins. As such, the first step 
required for any test is to export the appropriate variables.

The variables to be exported vary if the software under test is deployed in single or in multi
node topologies. This different topologies are referenced as singlepoint and multipoint within
Maestro. Additionally, the variables may vary according to the test - and custom tests may have
their own variables. The default set of tests provided with Maestro is documented in greater
detail in further sections of this document.

Overall, the base set of variables for the tests are: 

**Singlepoint Test Variables**: 

| Variable Name    | Default Value       | Description          |
|-------------------|---------------------|----------------------|
| `MAESTRO_BROKER` | `null` | The URL for the Maestro broker |
| `SEND_RECEIVE_URL` | `null` | The URL for the SUT (ie.: ```amqp://testhost/queue```) with options (see below) |
| `MESSAGE_SIZE` | `null` | The [message size](MessageSize.md) |
| `TEST_DURATION` | `null` | The [test duration](TestDuration.md) |
| `LOG_LEVEL` | `null` | Optional log level (see below) |
| `MANAGEMENT_INTERFACE` | `null` | The URL for the [management interface](Inspectors.md) |
| `INSPECTOR_NAME` | `null` | The name of the [inspector](Inspectors.md) |
| `DOWNLOADER_NAME` | `null` | The report download method (see below) |


**Multipoint Test Variables**: 

| Variable Name    | Default Value       | Description          |
|-------------------|---------------------|----------------------|
| `MAESTRO_BROKER` | `null` | The URL for the Maestro broker |
| `SEND_URL` | `null` | The URL for sending data to the SUT (ie.: ```amqp://testhost/queue```) with options (see below) |
| `RECEIVE_URL` | `null` | The URL for receiving data from the SUT (ie.: ```amqp://testhost/queue```) with options (see below) |
| `MESSAGE_SIZE` | `null` | The [message size](MessageSize.md) |
| `TEST_DURATION` | `null` | The [test duration](TestDuration.md) |
| `LOG_LEVEL` | `null` | Optional log level (see below) |
| `MANAGEMENT_INTERFACE` | `null` | The URL for the [management interface](Inspectors.md) |
| `INSPECTOR_NAME` | `null` | The name of the [inspector](Inspectors.md) |
| `DOWNLOADER_NAME` | `null` | The report download method (see below) |


Default Tests
----

Maestro comes with a set of default tests. Those tests are provided along with the
Maestro client package and can be found in the script directory of the client install.
The most relevant tests are:

* Single Point
  * FixedRateTest.groovy: a test can can send data at a fixed rate.
  * IncrementalTest.groovy: a test that keeps increasing the rate if the test is successful.
  * FairIncrementalTest.groovy: a test that keeps increasing the rate in a **fair** way if the test is successful.

* Multi Point
  * FixedRateTest.groovy: a test can can send data at a fixed rate.
  * IncrementalTest.groovy: a test that keeps increasing the rate if the test is successful.
  * FairIncrementalTest.groovy: a test that keeps increasing the rate in a **fair** way if the test is successful.

The different between the incremental tests and the fair incremental test is that the first 
do not take into consideration the number of connections per worker, whereas the second will
balance the rate on the SUT according to the number of connections per worker.

**Log Level**

Log level can be adjusted by setting the LOG_LEVEL variable to one of the following values:

* trace
* debug
* info
* warn

The default log level is "info".

**Report Downloaders**

After the test is complete, Maestro Client downloads the files for processing them and creating the reports. There are
two possible ways for the client to do this: 

* Direct method via HTTP: in this method, the client downloads the files directly from every single peer on the cluster. The client does
not need to know the peer addresses prior to the test. During the initial phases of the test, the client uses the Get 
command from the Maestro protocol to request the Data Server address of every peer on the test cluster.
* Broker method via MQTT: in this method, the client requests the peers on the test cluster to push the logs into the 
Maestro broker into a specific topic for logs. Then, the client downloads those files one at a time. 

Choosing the correct method depends on the topology and specifics of your test network. Overall:

* Direct method via HTTP
  * Is ideal if the client and the peers are on the same network
  * The peers have valid addresses that can be accessed via the client
  * The tests are high scale running for a very long time (ie: several hours), thus, generating files that are several 
  gigabytes in size 
  * Is ideal if the Maestro Broker is JVM-based and runs on a constrained memory environment (ie.: < 1Gb)
* Broker method via MQTT
  * Is ideal if the client and the peers are on different networks with the broker on the edge
  * Is ideal if the addresses of the peers are unknown

Configuring the Report Download Method

* Direct method via HTTP
  * No configuration needed. It is the default method
  * Used whenever the environment variable DOWNLOADER_NAME is null or "default"
* Broker method via MQTT
  * Used whenever the environment variable DOWNLOADER_NAME is set to "broker"
  * Optional client configuration is available on ```maestro-cli.properties```.
  * Optional worker configuration is available on ```maestro-worker.properties```.

Fixed Rate Test Variables
----

| Variable Name    | Default Value       | Description          |
|-------------------|---------------------|----------------------|
| `RATE` | `null` | The rate or 0 for unbounded | 
| `PARALLEL_COUNT` | `null` | The number of connections per worker |
| `MAXIMUM_LATENCY` | `null` | Optional maximum latency |
| `WARM_UP` | `null` | Whether to run a short warm-up before the actual test (set to true for the warm-up) |


Incremental Test Variables
----

| Variable Name    | Default Value       | Description          |
|-------------------|---------------------|----------------------|
| `INITIAL_RATE` | `null` | The initial rate for the test (> 0) |
| `CEILING_RATE` | `null` | The maximum rate for the test |
| `RATE_INCREMENT` | `null` | How much to increment the rate after every iteration |
| `INITIAL_PARALLEL_COUNT` | `null` | The initial number of connections per worker for the test (> 0) |
| `CEILING_PARALLEL_COUNT` | `null` | The maximum number of connections per worker for the test |
| `PARALLEL_COUNT_INCREMENT` | `null` | How much to increment the number of connections per worker after every test iteration |
| `MAXIMUM_LATENCY` | `null` | Mandatory maximum latency |


Fair Incremental Test Variables
----

| Variable Name    | Default Value       | Description          |
|-------------------|---------------------|----------------------|
| `COMBINED_INITIAL_RATE` | `null` | The combined initial rate for the test (> 0) |
| `COMBINED_CEILING_RATE` | `null` | The combined maximum rate for the test |
| `INITIAL_PARALLEL_COUNT` | `null` | The initial number of connections per worker for the test (> 0) |
| `CEILING_PARALLEL_COUNT` | `null` | The maximum number of connections per worker for the test |
| `PARALLEL_COUNT_INCREMENT` | `null` | How much to increment the number of connections per worker after every test iteration |
| `STEPS` | `null` | Mandatory maximum latency |


Usage - Runtime Parameters and Message Customization
----

The following parameters can be set for the JMS worker/client:

| Parameter Name    | Default Value       | Description          |
|-------------------|---------------------|----------------------|
| `protocol` | `AMQP` | The underlying messaging protocol to use (one of AMQP, OPENWIRE, ARTEMIS, RABBITAMQP) |
| `type` | queue | Destination type ('queue' or 'topic') |
| `ttl` | 5000 | Time to live. |
| `durable` | true | Durable flag for the message |
| `priority` | null | Message priority |
| `limitDestinations` | `1` | Distributes the load in a fixed number of queues (<= number of connections) |
| `sessionMode` | `1` | JMS Session mode (1 = Auto ACK, 2 = client ACK, 3 = Dups OK, 0 = session transacted) |


Running Default Tests
----

Once the test parameters have been adequately set by exporting the test variables. The test can be 
run with one of the following commands:

```
maestro-cli exec -s ../scripts/singlepoint/IncrementalTest.groovy -d /path/to/save/reports
```

Some test might need to be run directly via Groovy. This is the case for the tests
that require specific dependencies for processing their data. For example, to run
the Quiver test:

```
cd /path/to/scripts/singlepoint/ && groovy QuiverTest.groovy /path/to/save/reports
```

Generating the Reports
----

After a test is completed, performance reports can be generated using the following command line:

```
maestro-cli report -l info -d /path/to/save/reports
```

Please consider checking the help with the --help option, since some test behaviors and parameters can
be adjusted (ie.: warm-up).

Using as Library
----

**Note**: this is not important unless you are developing Maestro performance tests.


To use this project as library on your project you have to add my personal 
[bintray](https://bintray.com/orpiske/libs-release/) repository to the pom.xml
file:

```
<repositories>
    <repository>
        <id>orpiske-repo</id>
        <url>https://dl.bintray.com/orpiske/libs-release</url>
    </repository>
</repositories>
```

Then, the library can be referenced as: 
```
<dependency>
    <groupId>org.maestro</groupId>
    <artifactId>maestro-java</artifactId>
    <version>1.2.0</version>
</dependency>
```

There are multiple components and it is possible to choose only the desired one: 

* maestro-client: provides the API for interacting with the cluster 
* maestro-common: common code used all over the project
* maestro-contrib: code from external sources
* maestro-exporter: monitoring component
* maestro-reports: reporting code
* maestro-tests: a basic test API on top of Maestro Client
* maestro-worker: backend code that executes the tests (using the one of the workers in maestro-workers)
* maestro-inspector: backend code that inspects the SUTs (using the one of the inspectors in maestro-inspectors)

The API documentation (javadoc) is available [here](http://www.orpiske.net/files/javadoc/maestro-java-1.3/apidocs/index.html). 
Additional project documentation is available [here](http://www.orpiske.net/files/javadoc/maestro-java-1.3/). 

**Note**: replace version with the latest available version you wish to use.




