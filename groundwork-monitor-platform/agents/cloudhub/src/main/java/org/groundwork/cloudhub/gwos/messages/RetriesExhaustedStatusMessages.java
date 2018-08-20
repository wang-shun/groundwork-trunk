package org.groundwork.cloudhub.gwos.messages;

public class RetriesExhaustedStatusMessages extends UnreachableStatusMessages implements UpdateStatusMessages {

    protected final static String RETRIES_EXHAUSTED = " (retries exhausted)";

    @Override
    public String getHostHypervisorMessage() {
        return super.getHostHypervisorMessage() + RETRIES_EXHAUSTED;
    }

    @Override
    public String getHostVmMessage() {
        return super.getHostVmMessage() + RETRIES_EXHAUSTED;
    }

    @Override
    public String getHostMonitorMessage() {
        return super.getHostMonitorMessage() + RETRIES_EXHAUSTED;
    }

    @Override
    public String getServiceHypervisorMessage() {
        return super.getServiceHypervisorMessage() + RETRIES_EXHAUSTED;
    }

    @Override
    public String getServiceVmMessage() {
        return super.getServiceVmMessage() + RETRIES_EXHAUSTED;
    }

    @Override
    public String getServiceMonitorMessage() {
        return super.getServiceMonitorMessage() + RETRIES_EXHAUSTED;
    }

    @Override
    public String getComment() {
        return super.getComment() + RETRIES_EXHAUSTED;
    }

    @Override
    public String getMonitorComment() {
        return super.getMonitorComment() + RETRIES_EXHAUSTED;
    }

}
