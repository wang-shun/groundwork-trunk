package org.groundwork.cloudhub.monitor;

public class ConnectorMonitorState {

    public final static int FORCE_MONITOR_SHUTDOWN = -1;

    private String lastHostState = "";
    private String lastServiceState = "";

    public ConnectorMonitorState() {
    }

    public ConnectorMonitorState(String lastHostState, String lastServiceState) {
        this.lastHostState = lastHostState;
        this.lastServiceState = lastServiceState;
    }

    public void clear() {
        this.lastHostState = this.lastServiceState = "";
    }

    public String getLastHostState() {
        return lastHostState;
    }

    public String getLastServiceState() {
        return lastServiceState;
    }


    public void setLastHostState(String lastHostState) {
        this.lastHostState = lastHostState;
    }

    public void setLastServiceState(String lastServiceState) {
        this.lastServiceState = lastServiceState;
    }

    @Override
    public String toString() {
        StringBuffer message = new StringBuffer();
        message.append("host-state: ");
        message.append(lastHostState);
        message.append(", service-state: ");
        message.append(lastServiceState);
        return message.toString();
    }
}
