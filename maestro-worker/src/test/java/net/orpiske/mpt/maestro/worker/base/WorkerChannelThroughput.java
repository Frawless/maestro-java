/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.orpiske.mpt.maestro.worker.base;

import net.orpiske.mpt.common.content.ContentStrategy;
import net.orpiske.mpt.common.worker.MaestroSenderWorker;
import net.orpiske.mpt.common.worker.MaestroWorker;
import net.orpiske.mpt.common.worker.WorkerOptions;
import net.orpiske.mpt.maestro.worker.jms.JMSSenderWorker;
import net.orpiske.mpt.maestro.worker.jms.SenderClient;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Stream;

public class WorkerChannelThroughput {

    private enum DummySenderClient implements SenderClient {
        Instance;

        @Override
        public void sendMessages() throws Exception {

        }

        @Override
        public void setContentStrategy(ContentStrategy contentStrategy) {

        }

        @Override
        public void start() throws Exception {

        }

        @Override
        public void stop() {

        }

        @Override
        public void setUrl(String s) {

        }
    }

    public static void main(String[] args) throws InterruptedException {
        final int capacity = 128 * 1024;
        final File reportFolder = new File("./");
        final int workers = 2;
        final long rate = 100;
        final Thread[] workerThreads = new Thread[workers];
        final MaestroWorker[] maestroWorkers = new MaestroWorker[workers];
        final WorkerOptions workerOptions = new WorkerOptions();
        workerOptions.setDuration(Long.toString(Long.MAX_VALUE));
        workerOptions.setMessageSize("0");
        workerOptions.setRate(Long.toString(rate));
        for (int i = 0; i < workers; i++) {
            final int workerIndex = i;
            final MaestroSenderWorker worker = new JMSSenderWorker(() -> DummySenderClient.Instance, capacity);
            worker.setWorkerOptions(workerOptions);
            maestroWorkers[workerIndex] = worker;
            workerThreads[i] = new Thread(worker);
            workerThreads[i].setDaemon(true);
            workerThreads[i].setName("worker-" + workerIndex);
        }
        System.out.println("Estimated footprint of buffering is: " + (Stream.of(maestroWorkers).mapToLong(w -> w.workerChannel().footprintInBytes()).sum() / 1024) + " KB");
        final WorkerChannelWriter channelWriter = new WorkerChannelWriter(reportFolder, Arrays.asList(maestroWorkers));
        final Thread writerThread = new Thread(channelWriter);
        writerThread.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Stream.of(maestroWorkers).forEach(w -> w.stop());
            Stream.of(workerThreads).forEach(workerThread -> {
                try {
                    workerThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            writerThread.interrupt();
            try {
                writerThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println("FINISHED WRITES");
            }
        }));
        final Thread reporterThread = new Thread(() -> {
            final StringBuilder report = new StringBuilder();
            final long[] lastMessageCount = new long[workers];
            long lastCheck = System.currentTimeMillis();
            final long[] lastMissedCount = new long[workers];
            //print reports
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    continue;
                }
                report.setLength(0);
                long now = System.currentTimeMillis();
                final long intervalLength = now - lastCheck;
                report.append(" - ").append(intervalLength).append(" ms");
                for (int i = 0; i < workers; i++) {
                    final long transmitted = maestroWorkers[i].messageCount();
                    final long missed = maestroWorkers[i].workerChannel().missedSamples();
                    final long transmissedInterval = transmitted - lastMessageCount[i];
                    final long missedInInterval = missed - lastMissedCount[i];
                    report.append(" - [").append(i).append("]\t").append(transmissedInterval).append(" missed= ").append(missedInInterval);
                    lastMessageCount[i] = transmitted;
                    lastMissedCount[i] = missed;
                }
                System.out.println(report);
                lastCheck = now;
            }
        });
        reporterThread.start();
        Stream.of(workerThreads).forEach(Thread::start);
    }
}