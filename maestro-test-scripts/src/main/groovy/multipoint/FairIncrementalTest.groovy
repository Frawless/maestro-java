/*
 *  Copyright 2017 Otavio R. Piske <angusyoung@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this normalizedFile except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package multipoint

import org.maestro.client.Maestro
import org.maestro.client.exchange.MaestroTopics
import org.maestro.reports.downloaders.DownloaderBuilder
import org.maestro.reports.downloaders.ReportsDownloader
import org.maestro.tests.MultiPointProfile
import org.maestro.tests.incremental.IncrementalTestExecutor
import org.maestro.tests.incremental.IncrementalTestProfile
import org.maestro.tests.incremental.multipoint.SimpleTestProfile
import org.maestro.common.LogConfigurator
import org.maestro.common.content.MessageSize
import org.maestro.common.duration.TestDurationBuilder
import org.maestro.tests.utils.ManagementInterface


maestroURL = System.getenv("MAESTRO_BROKER")
if (maestroURL == null) {
    println "Error: the maestro broker was not given"

    System.exit(1)
}

sendURL = System.getenv("SEND_URL")
if (sendURL == null) {
    println "Error: the send URL was not given"

    System.exit(1)
}

receiveURL = System.getenv("RECEIVE_URL")
if (receiveURL == null) {
    println "Error: the receive URL was not given"

    System.exit(1)
}

messageSizeStr = System.getenv("MESSAGE_SIZE")
if (messageSizeStr == null) {
    println "Error: the message size was not given"

    System.exit(1)
}
if (messageSizeStr.startsWith("~")) {
    println "Error: fair incremental test requires a fixed message size"

    System.exit(1)
}

messageSize = Long.parseLong(messageSizeStr)

duration = System.getenv("TEST_DURATION")
if (duration == null) {
    println "Error: the test duration was not given"

    System.exit(1)
}

combinedRateStr = System.getenv("COMBINED_INITIAL_RATE")
if (combinedRateStr == null) {
    println "Error: the combined rate was not given"

    System.exit(1)
}
combinedRate = Integer.parseInt(combinedRateStr)

combinedCeilingRateStr = System.getenv("COMBINED_CEILING_RATE")
if (combinedCeilingRateStr == null) {
    println "Error: the combined ceiling rate was not given"

    System.exit(1)
}
combinedCeilingRate = Integer.parseInt(combinedCeilingRateStr)

initialParallelCountStr = System.getenv("INITIAL_PARALLEL_COUNT")
if (initialParallelCountStr == null) {
    println "Error: the test parallel count was not given"

    System.exit(1)
}
initialParallelCount = Integer.parseInt(initialParallelCountStr)

ceilingParallelCountStr = System.getenv("CEILING_PARALLEL_COUNT")
if (ceilingParallelCountStr == null) {
    println "Error: the test parallel count was not given"

    System.exit(1)
}
ceilingParallelCount = Integer.parseInt(ceilingParallelCountStr)


parallelCountIncrementStr = System.getenv("PARALLEL_COUNT_INCREMENT")
if (parallelCountIncrementStr == null) {
    println "Error: the test parallel count was not given"

    System.exit(1)
}
parallelCountIncrement = Integer.parseInt(parallelCountIncrementStr)

stepsStr = System.getenv("STEPS")
if (stepsStr == null) {
    println "Error: the number of test steps were not given"

    System.exit(1)
}
steps = Integer.parseInt(stepsStr)

maxLatency = System.getenv("MAXIMUM_LATENCY")
if (maxLatency == null) {
    println "Error: the maximum acceptable latency (FCL) was not given"

    System.exit(1)
}

logLevel = System.getenv("LOG_LEVEL")
LogConfigurator.configureLogLevel(logLevel)

managementInterface = System.getenv("MANAGEMENT_INTERFACE");
inspectorName = System.getenv("INSPECTOR_NAME");
downloaderName = System.getenv("DOWNLOADER_NAME");


rate = (combinedRate / initialParallelCount ) * (1 - (Math.log10(messageSize.doubleValue())) / 10)
println "Calculated base rate $rate"

ceilingRate = (combinedCeilingRate / initialParallelCount ) * (1 - (Math.log10(messageSize.doubleValue())) / 10)
println "Calculated ceiling rate $ceilingRate"

rateIncrement = (ceilingRate - rate) / steps
println "Calculated rate increment $rateIncrement"


println "Connecting to " + maestroURL
maestro = new Maestro(maestroURL)

ReportsDownloader reportsDownloader = DownloaderBuilder.build(downloaderName, maestro, args[0])

IncrementalTestProfile testProfile = new SimpleTestProfile();

testProfile.addEndPoint(new MultiPointProfile.EndPoint("sender", MaestroTopics.SENDER_DAEMONS, sendURL))
testProfile.addEndPoint(new MultiPointProfile.EndPoint("receiver", MaestroTopics.RECEIVER_DAEMONS, receiveURL))

testProfile.setDuration(TestDurationBuilder.build(duration))
testProfile.setMessageSize(MessageSize.fixed(messageSize))
testProfile.setInitialRate(rate.intValue());
testProfile.setCeilingRate(ceilingRate.intValue())
testProfile.setRateIncrement(rateIncrement.intValue())
testProfile.setInitialParallelCount(initialParallelCount)
testProfile.setCeilingParallelCount(ceilingParallelCount)
testProfile.setParallelCountIncrement(parallelCountIncrement)

maxLatencyStr = System.getenv("MAXIMUM_LATENCY");
if (maxLatencyStr != null) {
    int maxLatency = Integer.parseInt(maxLatencyStr)
    testProfile.setMaximumLatency(maxLatency)
}

ManagementInterface.setupInterface(managementInterface, inspectorName, testProfile)
ManagementInterface.setupResolver(inspectorName, reportsDownloader)

IncrementalTestExecutor testExecutor = new IncrementalTestExecutor(maestro, reportsDownloader, testProfile)

boolean ret = testExecutor.run();

reportsDownloader.waitForComplete();
maestro.stop()

if (!ret) {
    System.exit(1)
}

System.exit(0)


