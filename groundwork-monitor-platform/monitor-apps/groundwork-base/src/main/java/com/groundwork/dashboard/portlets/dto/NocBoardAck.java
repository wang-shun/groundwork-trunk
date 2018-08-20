package com.groundwork.dashboard.portlets.dto;

public class NocBoardAck {

    protected boolean ackBool;
    protected String acknowledger;
    protected String acknowledgeComment;
    protected String host;
    protected String service;

    public NocBoardAck() {}

    public boolean isAckBool() {
        return ackBool;
    }

    public void setAckBool(boolean ackBool) {
        this.ackBool = ackBool;
    }

    public String getAcknowledger() {
        return acknowledger;
    }

    public void setAcknowledger(String acknowledger) {
        this.acknowledger = acknowledger;
    }

    public String getAcknowledgeComment() {
        return acknowledgeComment;
    }

    public void setAcknowledgeComment(String acknowledgeComment) {
        this.acknowledgeComment = acknowledgeComment;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }
}
