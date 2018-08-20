package org.groundwork.agents.monitor;

import java.util.List;
import java.util.concurrent.Future;

public class CollectorResult {

    public static enum Status {
        BatchSuccess,
        BatchInterrupted,
        BatchTimeout,
        BatchFailure,
        BatchRunning,
        BatchInit
    };

    private Status status;
    private long executionTime;

    private List<Future<MonitorAgentResult>> results;

    public CollectorResult(List<Future<MonitorAgentResult>> results) {
        this.results = results;
        this.status = Status.BatchInit;
        this.executionTime = 0;
    }

    public List<Future<MonitorAgentResult>> getResults() {
        return results;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }
}
