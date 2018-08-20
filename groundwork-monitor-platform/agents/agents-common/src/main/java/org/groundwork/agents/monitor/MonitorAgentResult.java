package org.groundwork.agents.monitor;

public class MonitorAgentResult {
    private final MonitorAgent job;
    private boolean success = false;
    private boolean running = false;
    private long totalRunTime = 0;

    public MonitorAgentResult(MonitorAgent job, boolean success, long totalRunTime, boolean running) {
        this.job = job;
        this.success  = success;
        this.totalRunTime = totalRunTime;
        this.running = running;
    }

    public MonitorAgent getJob() {
        return job;
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
