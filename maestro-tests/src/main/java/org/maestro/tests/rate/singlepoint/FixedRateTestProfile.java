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

package org.maestro.tests.rate.singlepoint;

import org.apache.commons.configuration.AbstractConfiguration;
import org.maestro.client.Maestro;
import org.maestro.common.ConfigurationWrapper;
import org.maestro.common.client.exceptions.NotEnoughRepliesException;
import org.maestro.common.duration.TestDuration;
import org.maestro.tests.AbstractTestProfile;
import org.maestro.tests.SinglePointProfile;
import org.maestro.tests.utils.CompletionTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.maestro.client.Maestro.set;

/**
 * A test profile for fixed rate tests
 */
public class FixedRateTestProfile extends AbstractTestProfile implements SinglePointProfile {
    private static final Logger logger = LoggerFactory.getLogger(FixedRateTestProfile.class);
    private static final AbstractConfiguration config = ConfigurationWrapper.getConfig();

    private int rate;
    protected int warmUpRate;
    private int parallelCount;
    private int warmUpParallelCount;
    private String brokerURL;

    private int maximumLatency = 0;
    private TestDuration duration;
    private String messageSize;

    private String extPointSource;
    private String extPointBranch;
    private String extPointCommand;

    public String getExtPointSource() {
        return extPointSource;
    }

    public void setExtPointSource(String extPointSource) {
        this.extPointSource = extPointSource;
    }

    public String getExtPointBranch() {
        return extPointBranch;
    }

    public void setExtPointBranch(String extPointBranch) {
        this.extPointBranch = extPointBranch;
    }

    public String getExtPointCommand() {
        return extPointCommand;
    }

    public void setExtPointCommand(String extPointCommand) {
        this.extPointCommand = extPointCommand;
    }

    public void setParallelCount(int parallelCount) {
        this.parallelCount = parallelCount;

        setWarmUpParallelCount(parallelCount);
    }

    private void setWarmUpParallelCount(int parallelCount) {
        final int ceilingWarmUpPc = config.getInt("warm-up.ceiling.parallel.count", 30);
        if (parallelCount > ceilingWarmUpPc) {
            warmUpParallelCount = ceilingWarmUpPc;
        }
        else {
            warmUpParallelCount = parallelCount;
        }
    }

    public int getParallelCount() {
        return parallelCount;
    }

    public void setRate(int rate) {
        double rateMultiplier = config.getDouble("warm-up.rate.percent", 30);

        this.rate = rate;
        double warmUpTmp = rate * (rateMultiplier / 100);

        if (warmUpTmp > Integer.MAX_VALUE) {
            warmUpRate = Integer.MAX_VALUE;
        }
        else {
            warmUpRate = (int) Math.round(warmUpTmp);
        }
    }

    public int getRate() {
        return rate;
    }


    public int getMaximumLatency() {
        return maximumLatency;
    }

    public void setMaximumLatency(int maximumLatency) {
        this.maximumLatency = maximumLatency;
    }

    public TestDuration getDuration() {
        return duration;
    }

    public void setDuration(final TestDuration duration) {
        this.duration = duration;
    }

    public String getMessageSize() {
        return messageSize;
    }

    public void setMessageSize(String messageSize) {
        this.messageSize = messageSize;
    }

    public String getBrokerURL() {
        return getSendReceiveURL();
    }

    public void setBrokerURL(final String brokerURL) {
        setSendReceiveURL(brokerURL);
    }

    @Override
    public void setSendReceiveURL(String url) {
        this.brokerURL = url;
    }

    @Override
    public String getSendReceiveURL() {
        return brokerURL;
    }

    @Override
    public long getEstimatedCompletionTime() {
        return getEstimatedCompletionTime(duration, getRate());
    }

    public long getWarmUpEstimatedCompletionTime() {
        return CompletionTime.estimate(getDuration().getWarmUpDuration(), warmUpRate);
    }

    protected void apply(final Maestro maestro, boolean warmUp) {
        logger.info("Setting endpoint URL to {}", getSendReceiveURL());
        set(maestro::setBroker, getSendReceiveURL());

        if (warmUp) {
            logger.info("Setting warm-up rate to {}", warmUpRate);
            set(maestro::setRate, warmUpRate);

            TestDuration warmUpDuration = getDuration().getWarmUpDuration();
            long balancedDuration = Math.round((double) warmUpDuration.getNumericDuration() / (double) getParallelCount());

            logger.info("Setting warm-up duration to {}", balancedDuration);
            set(maestro::setDuration, balancedDuration);

            logger.info("Setting warm-up parallel count to {}", this.warmUpParallelCount);
            set(maestro::setParallelCount, this.warmUpParallelCount);
        }
        else {
            logger.info("Setting test rate to {}", getRate());
            set(maestro::setRate, rate);

            logger.info("Setting test duration to {}", getDuration());
            set(maestro::setDuration, this.getDuration().toString());

            logger.info("Setting parallel count to {}", this.parallelCount);
            set(maestro::setParallelCount, this.parallelCount);
        }

        logger.info("Setting fail-condition-latency to {}", getMaximumLatency());
        set(maestro::setFCL, getMaximumLatency());

        logger.info("Setting message size to {}", getMessageSize());
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

        if (getExtPointSource() != null) {
            if (getExtPointBranch() != null) {
                logger.info("Setting the extension point source to {} using the {} branch", getExtPointSource(),
                        getExtPointBranch());
                set(maestro::sourceRequest, getExtPointSource(), getExtPointBranch());
            }
        }

        if (getExtPointCommand() != null) {
            logger.info("Setting command to Agent execution to {}", getExtPointCommand());
            set(maestro::userCommand, 0L, getExtPointCommand());
        }
    }

    @Override
    public void apply(final Maestro maestro) {
        logger.info("Applying test execution profile");
        apply(maestro, false);
        logger.info("Estimated time for test completion: {} seconds", getEstimatedCompletionTime());
    }

    public void warmUp(final Maestro maestro) {
        logger.info("Applying test warm-up profile");
        apply(maestro, true);
        logger.info("Estimated time for warm-up completion: {} seconds", getWarmUpEstimatedCompletionTime());
    }

    @Override
    public String toString() {
        return "FixedRateTestProfile{" +
                "rate=" + rate +
                ", parallelCount=" + parallelCount +
                ", maximumLatency=" + maximumLatency +
                ", duration=" + duration +
                ", messageSize='" + messageSize + '\'' +
                "} " + super.toString();
    }
}
