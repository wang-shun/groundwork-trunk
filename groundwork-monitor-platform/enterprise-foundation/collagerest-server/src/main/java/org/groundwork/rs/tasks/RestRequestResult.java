package org.groundwork.rs.tasks;

import org.groundwork.rs.dto.DtoOperationResults;

public class RestRequestResult {

    private final RestRequestTask task;
    private final DtoOperationResults results;
    private boolean success = false;
    private boolean running = false;
    private long totalRunTime = 0;

    public RestRequestResult(DtoOperationResults results, RestRequestTask task, boolean success, long totalRunTime, boolean running) {
        this.results = results;
        this.task = task;
        this.success  = success;
        this.totalRunTime = totalRunTime;
        this.running = running;
    }

    public RestRequestTask getTask() {
        return task;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isSuccess() {
        return success;
    }

    public long getTotalRunTime() {
        return totalRunTime;
    }

}
