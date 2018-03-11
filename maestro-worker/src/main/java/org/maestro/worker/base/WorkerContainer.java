package org.maestro.worker.base;

import org.maestro.common.client.MaestroReceiver;
import org.maestro.common.evaluators.Evaluator;
import org.maestro.common.evaluators.LatencyEvaluator;
import org.maestro.common.worker.LatencyStats;
import org.maestro.common.worker.MaestroWorker;
import org.maestro.common.worker.ThroughputStats;
import org.maestro.common.worker.WorkerOptions;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * This is a container class for multiple workerRuntimeInfos. It is responsible for
 * creating, starting and stopping multiple workerRuntimeInfos at once.
 */
public final class WorkerContainer {
    private static WorkerContainer instance;
    private WorkerOptions workerOptions;
    private final List<WorkerRuntimeInfo> workerRuntimeInfos = new ArrayList<>();

    private WorkerWatchdog workerWatchdog;
    private Thread watchDogThread;
    private final MaestroReceiver endpoint;
    private LocalDateTime startTime;
    private Evaluator<?> evaluator;

    private WorkerContainer(MaestroReceiver endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Gets and instance of the container
     * @return
     */
    public synchronized static WorkerContainer getInstance(MaestroReceiver endpoint) {
        if (instance == null) {
            instance = new WorkerContainer(endpoint);
        }

        return instance;
    }




    /**
     * Sets the worker options for the instance.
     * @param workerOptions the worker options to set
     */
    public void setWorkerOptions(WorkerOptions workerOptions) {
        this.workerOptions = workerOptions;
    }

    /**
     * Start the execution of the workers for a predefined class
     * @param clazz The class associated with the workers
     * @param onWorkersStopped callback that will be called when the workers will stop
     * @throws IllegalAccessException if unable to access the worker constructor
     * @throws InstantiationException if unable to instantiate the worker
     */
    public void start(final Class<MaestroWorker> clazz, Collection<? super MaestroWorker> workers,
                      Consumer<? super List<WorkerRuntimeInfo>> onWorkersStopped, Evaluator<?> evaluator)
            throws IllegalAccessException, InstantiationException {
        final int parallelCount = Integer.parseInt(workerOptions.getParallelCount());
        this.workerRuntimeInfos.clear();
        this.evaluator = evaluator;
        try {
            createAndStartWorkers(clazz, workerOptions, parallelCount, this.workerRuntimeInfos, onWorkersStopped, evaluator);
        } catch (Throwable t) {
            //interrupt any workers
            this.workerRuntimeInfos.forEach(info -> info.thread.interrupt());
            //cleanup
            this.workerRuntimeInfos.clear();
            throw t;
        }
        //the workers are started
        workers.addAll(this.workerRuntimeInfos.stream().map(info -> info.worker).collect(Collectors.toList()));
        startTime = LocalDateTime.now();
    }

    private void createAndStartWorkers(final Class<MaestroWorker> clazz, WorkerOptions workerOptions, int workers,
                                       List<WorkerRuntimeInfo> workerRuntimeInfos,
                                       final Consumer<? super List<WorkerRuntimeInfo>> onWorkersStopped,
                                       final Evaluator<?> evaluator) throws IllegalAccessException, InstantiationException {
        for (int i = 0; i < workers; i++) {
            final WorkerRuntimeInfo ri = new WorkerRuntimeInfo();
            ri.worker = clazz.newInstance();
            ri.worker.setWorkerOptions(workerOptions);
            ri.worker.setWorkerNumber(i);
            ri.thread = new Thread(ri.worker);
            ri.thread.start();
            workerRuntimeInfos.add(ri);
        }

        workerWatchdog = new WorkerWatchdog(workerRuntimeInfos, endpoint, onWorkersStopped, evaluator);

        watchDogThread = new Thread(workerWatchdog);
        watchDogThread.start();
    }

    public void stop() {
        for (WorkerRuntimeInfo ri : workerRuntimeInfos) {
            ri.worker.stop();
        }

        if (workerWatchdog != null) {
            workerWatchdog.setRunning(false);
        }
    }

    public boolean isTestInProgress() {
        if (watchDogThread == null) {
            return false;
        }


        if (workerWatchdog.isRunning()) {
            for (WorkerRuntimeInfo ri : workerRuntimeInfos) {
                // A worker should only be in "not running" state if it is being
                // shutdown
                if (!ri.worker.isRunning()) {
                    return false;
                }
            }
            return true;
        }


        return false;
    }

    public ThroughputStats throughputStats() {
        ThroughputStats ret = new ThroughputStats();

        long messageCount = 0;
        for (WorkerRuntimeInfo runtimeInfo : workerRuntimeInfos) {
            messageCount += runtimeInfo.worker.messageCount();
        }
        ret.setCount(messageCount);

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(startTime, now);
        ret.setDuration(duration);

        return ret;
    }

    public LatencyStats latencyStats() {
        if (evaluator instanceof LatencyEvaluator) {
            LatencyStats ret = new LatencyStats();
            
            LatencyEvaluator latencyEvaluator = (LatencyEvaluator) evaluator;

            LocalDateTime now = LocalDateTime.now();
            Duration duration = Duration.between(startTime, now);
            ret.setDuration(duration);

            ret.setLatency(latencyEvaluator.getMean());

            return ret;
        }

        return null;
    }
}
