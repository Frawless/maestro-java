/*
 * Copyright 2018 Otavio R. Piske <angusyoung@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.maestro.tests.incremental.singlepoint;

import org.maestro.client.Maestro;
import org.maestro.common.client.exceptions.NotEnoughRepliesException;
import org.maestro.tests.SinglePointProfile;
import org.maestro.tests.incremental.IncrementalTestProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.maestro.client.Maestro.set;

public class SimpleTestProfile extends IncrementalTestProfile implements SinglePointProfile {
    private static final Logger logger = LoggerFactory.getLogger(SimpleTestProfile.class);

    private String brokerURL;

    public String getBrokerURL() {
        return getSendReceiveURL();
    }

    public void setBrokerURL(String brokerURL) {
        setSendReceiveURL(brokerURL);
    }

    @Override
    public void setSendReceiveURL(final String url) {
        this.brokerURL = url;
    }

    @Override
    public String getSendReceiveURL() {
        return brokerURL;
    }

    public void apply(Maestro maestro) {
        logger.info("Setting broker to {}", getBrokerURL());
        set(maestro::setBroker, getBrokerURL());

        logger.info("Setting rate to {}", getRate());
        set(maestro::setRate, rate);

        logger.info("Rate increment value is {}", getRateIncrement());

        logger.info("Setting parallel count to {}", this.parallelCount);
        set(maestro::setParallelCount, this.parallelCount);

        logger.info("Parallel count increment value is {}", getParallelCountIncrement());

        logger.info("Setting duration to {}", getDuration());
        set(maestro::setDuration, this.getDuration().toString());

        logger.info("Setting fail-condition-latency to {}", getMaximumLatency());
        set(maestro::setFCL, getMaximumLatency());

        // Variable message messageSize
        logger.info("Setting message size to: {}", getMessageSize());
        set(maestro::setMessageSize, getMessageSize());

        if (getManagementInterface() != null) {
            if (getInspectorName() != null) {
                logger.info("Setting the management interface to {} using inspector {}", getManagementInterface(),
                        getInspectorName());
                try {
                    set(maestro::setManagementInterface, getManagementInterface());
                }
                catch (NotEnoughRepliesException ne) {
                    logger.warn("Apparently no inspector nodes are enabled on this cluster. Ignoring ...");
                }
            }
        }

        logger.info("Estimated time for test completion: {} seconds", getEstimatedCompletionTime());
    }
}
