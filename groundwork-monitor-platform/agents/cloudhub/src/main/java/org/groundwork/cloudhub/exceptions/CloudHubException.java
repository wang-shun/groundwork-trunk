package org.groundwork.cloudhub.exceptions;

public class CloudHubException extends RuntimeException {

    private String additional;

    public CloudHubException() {
        super();
    }

    public CloudHubException(String msg) {
        super(msg);
    }

    public CloudHubException(Throwable nested) {
        super(nested);
    }

    public CloudHubException(String msg, Throwable nested) {
        super(msg, nested);
    }

    public CloudHubException(String msg, String additional, Throwable nested) {
        super(msg, nested);
        this.additional = additional;
    }

    public CloudHubException(String msg, String additional) {
        super(msg);
        this.additional = additional;
    }

    public String getAdditional() {
        return additional;
    }
}
