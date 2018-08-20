package org.groundwork.rs.async;

import com.groundwork.collage.CollageFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.rs.dto.DtoAsyncSettings;
import org.groundwork.rs.tasks.RestRequestResult;
import org.groundwork.rs.tasks.RestRequestTask;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

public class AsyncRestProcessor {
    protected static Log log = LogFactory.getLog(AsyncRestProcessor.class);

    public static final String CONFIG_THREADS = "collagerest.threads";
    public static final String CONFIG_QUEUE_SIZE = "collagerest.queueSize";
    public static final String CONFIG_THROTTLE_THRESHOLD = "collagerest.throttleThreshold";
    public static final String CONFIG_THROTTLE_WAIT_MS = "collagerest.throttleWaitMs";

    public static final int DEFAULT_CONFIG_THREADS = 10;
    public static final int DEFAULT_CONFIG_QUEUE_SIZE = 1000;
    public static final int DEFAULT_CONFIG_THROTTLE_THRESHOLD = 500;
    public static final int DEFAULT_CONFIG_THROTTLE_WAIT_MS = 500;

    private int threadPoolSize = DEFAULT_CONFIG_THREADS;
    private int queueSize = DEFAULT_CONFIG_QUEUE_SIZE;
    private int throttleThreshold = DEFAULT_CONFIG_THROTTLE_THRESHOLD;
    private int throttleWaitMs = DEFAULT_CONFIG_THROTTLE_WAIT_MS;

    private PausableThreadPoolExecutor executor;

    private static AsyncRestProcessor singleton = null;

    public static final AsyncRestProcessor factory() {
        if (singleton == null) {
            singleton = new AsyncRestProcessor();
        }
        return singleton;
    }

    public AsyncRestProcessor() {
        loadProperties();
        executor = new PausableThreadPoolExecutor(threadPoolSize, threadPoolSize, 1, TimeUnit.SECONDS,
                new LinkedBlockingQueue(queueSize));
    }

    public DtoAsyncSettings getSettings() {
        return new DtoAsyncSettings(this.threadPoolSize, this.queueSize, this.throttleThreshold, this.throttleWaitMs);
    }

    public boolean setSettings(DtoAsyncSettings settings) {
        threadPoolSize = settings.getThreadPoolSize();
        queueSize = settings.getQueueSize();
        throttleThreshold = settings.getThrottleThreshold();
        throttleWaitMs = settings.getThrottleWaitMs();
        if (executor.getActiveCount() > 0) {
            return false;
        }
        executor.shutdownNow();
        executor = new PausableThreadPoolExecutor(threadPoolSize, threadPoolSize, 1, TimeUnit.SECONDS,
                new LinkedBlockingQueue(queueSize));
        return true;
    }

    public void shutdown() {

        executor.shutdown();
//        while (!executor.isTerminated()) {
//            executor.awaitTermination(1, TimeUnit.SECONDS);
//            log.debug(executor.getCompletedTaskCount() + " index requests processed).");
//        }
    }

    public Future<RestRequestResult> submitJob(RestRequestTask task) throws RejectedExecutionException {
        /// TODO: track results
        //List<Future<Map<String, Object>>> futures = new ArrayList<Future<Map<String, Object>>>(taskCount);

        if (executor.getQueue().remainingCapacity() < throttleThreshold) {
            try {
                if (log.isInfoEnabled()) {
                    log.info("-- throttling for queue/throttle value: " + throttleThreshold +
                            ", remaining queue capacity: " + executor.getQueue().remainingCapacity());
                    log.info("-- throttle sleep value(ms): " + throttleWaitMs);
                }
                Thread.sleep(throttleWaitMs);
            } catch (Exception e) {
                log.error("Async Queue Interrupted from Throttling ", e);
            }
        }
        Future<RestRequestResult> job = executor.submit(task);
        //futures.add(executor.submit(callable));
        if (log.isInfoEnabled()) {
            log.info("--- Async job submitted, active thread count: " + executor.getActiveCount() +
                    ", total pool size: " + executor.getPoolSize());
        }
        return job;

    }


//        for (Future<Map<String, Object>> future : futures) {
//            Map<String, Object> result = null;
//            try {
//                result = (Map<String, Object>) future.get(1, TimeUnit.SECONDS);
//            } catch (Exception e) {
//                future.cancel(true);
//            }
//        }

    private void loadProperties() {
        InputStream input = null;
        try {
            CollageFactory service = CollageFactory.getInstance();
            Properties properties = service.getFoundationProperties();
            String prop = properties.getProperty(CONFIG_THREADS, String.valueOf(DEFAULT_CONFIG_THREADS));
            this.threadPoolSize = Integer.parseInt(prop);
            prop = properties.getProperty(CONFIG_QUEUE_SIZE, String.valueOf(DEFAULT_CONFIG_QUEUE_SIZE));
            this.queueSize = Integer.parseInt(prop);
            prop = properties.getProperty(CONFIG_THROTTLE_THRESHOLD, String.valueOf(DEFAULT_CONFIG_THROTTLE_THRESHOLD));
            this.throttleThreshold = Integer.parseInt(prop);
            prop = properties.getProperty(CONFIG_THROTTLE_WAIT_MS, String.valueOf(DEFAULT_CONFIG_THROTTLE_WAIT_MS));
            this.throttleWaitMs = Integer.parseInt(prop);
            if (log.isInfoEnabled()) {
                log.info("--- rest async processor configured with " + threadPoolSize + " threads");
                log.info("\t\t queueSize: " + queueSize);
                log.info("\t\t throttleThreshold: " + throttleThreshold);
                log.info("\t\t throttleWaitMs: " + throttleWaitMs);
            }
        } catch (Exception e) {
            log.error("Failed to load collage factory properties ", e);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {}
            }
        }

    }

}


