/*
 *  Copyright 2017 Otavio R. Piske <angusyoung@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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

package org.maestro.tests.incremental;

import org.apache.commons.configuration.AbstractConfiguration;
import org.maestro.client.Maestro;
import org.maestro.client.notes.GetResponse;
import org.maestro.common.ConfigurationWrapper;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.reports.downloaders.ReportsDownloader;
import org.maestro.tests.AbstractTestExecutor;
import org.maestro.tests.DownloadProcessor;
import org.maestro.tests.callbacks.LogRequesterCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;

/**
 * An executor that runs the test ever incrementing the rate and parallel connections as defined by the profile
 */
public class IncrementalTestExecutor extends AbstractTestExecutor {
    private static final Logger logger = LoggerFactory.getLogger(IncrementalTestExecutor.class);
    private static final AbstractConfiguration config = ConfigurationWrapper.getConfig();

    private final IncrementalTestProfile testProfile;

    private static final long coolDownPeriod;
    private final DownloadProcessor downloadProcessor;

    static {
        coolDownPeriod = config.getLong("test.incremental.cooldown.period", 1) * 1000;
    }

    /**
     * Constructor
     * @param maestro a Maestro client instance
     * @param reportsDownloader the reports downloader in use for the test
     * @param testProfile the test profile in use for the test
     */
    public IncrementalTestExecutor(final Maestro maestro, final ReportsDownloader reportsDownloader,
                                   final IncrementalTestProfile testProfile) {
        super(maestro, reportsDownloader);

        this.testProfile = testProfile;

        downloadProcessor = new DownloadProcessor(reportsDownloader);
        getMaestro().getCollector().addCallback(new LogRequesterCallback(this, downloadProcessor));
    }

    private long getTimeout() {
        return getTimeout(testProfile);
    }

    private String phaseName() {
        return "run";
    }

    private boolean runTest(int testNumber) {
        logger.info("Starting test execution {}", testNumber);

        try {
            // Clean up the topic
            getMaestro().clear();

            int numPeers = peerCount(testProfile);
            if (numPeers == 0) {
                logger.error("There are not enough peers to run the test");

                return false;
            }

            List<? extends MaestroNote> dataServers = getMaestro().getDataServer().get();
            dataServers.stream()
                    .filter(note -> note instanceof GetResponse)
                    .forEach(note -> downloadProcessor.addDataServer((GetResponse) note));


            getReportsDownloader().getOrganizer().getTracker().setCurrentTest(testNumber);
            testProfile.apply(getMaestro());

            try {
                startServices(testProfile);

                testStart();


                long timeout = getTimeout();
                logger.info("The test {} has started and will timeout after {} seconds", phaseName(), timeout);
                List<? extends MaestroNote> results = getMaestro()
                        .waitForNotifications(numPeers)
                        .get(timeout, TimeUnit.SECONDS);

                long failed = results.stream()
                        .filter(this::isTestFailed)
                        .count();

                if (failed > 0) {
                    logger.info("Test {} completed unsuccessfully", phaseName());
                    return false;
                }

                logger.info("Test {} completed successfully", phaseName());
                return true;
            }
            finally {
                drain();
            }
        }
        catch (TimeoutException te) {
            logger.warn("Timed out waiting for the test notifications");
        }
        catch (Exception e) {
            logger.error("Error: {}", e.getMessage(), e);
        }
        finally {
            testStop();

            try {
                stopServices();
            }
            catch (MaestroException e) {
                if (e.getCause() instanceof TimeoutException) {
                    logger.warn("Timed out waiting for a stop response");
                }
                else {
                    logger.warn(e.getMessage());
                }
            }
        }

        return false;
    }


    public boolean run() {
        int testNumber = 0;
        boolean successful;

        do {
            successful = runTest(testNumber);
            if (!successful) {
                break;
            }

            testProfile.increment();
            if (testProfile.isOverCeiling()) {
                break;
            }
            
            testNumber++;
        } while (true);

        logger.error("Test execution failed");
        return successful;
    }

    @Override
    public long getCoolDownPeriod() {
        return coolDownPeriod;
    }
}
