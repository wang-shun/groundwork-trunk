package com.groundwork.downtime;

public class DowntimeException extends RuntimeException {
    private String additional;

    public DowntimeException() {
        super();
    }

    public DowntimeException(String msg) {
        super(msg);
    }

    public DowntimeException(Throwable nested) {
        super(nested);
    }

    public DowntimeException(String msg, Throwable nested) {
        super(msg, nested);
    }

    public DowntimeException(String msg, String additional, Throwable nested) {
        super(msg, nested);
        this.additional = additional;
    }

    public DowntimeException(String msg, String additional) {
        super(msg);
        this.additional = additional;
    }

    public String getAdditional() {
        return additional;
    }

}
