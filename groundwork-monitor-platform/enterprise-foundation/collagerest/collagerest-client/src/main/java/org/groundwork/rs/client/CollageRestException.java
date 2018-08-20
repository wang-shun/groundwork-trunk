package org.groundwork.rs.client;

public class CollageRestException extends RuntimeException {

    int status = 200;

    public CollageRestException() {
        super();
    }
    public CollageRestException(int status) {
        super();
        this.status = status;
    }

    public CollageRestException(String msg) {
        super(msg);
    }

    public CollageRestException(String msg, int status) {
        super(msg);
        this.status = status;
    }

    public CollageRestException(Throwable nested, int status) {
        super(nested);
        this.status = status;
    }
    public CollageRestException(Throwable nested) {
        super(nested);
    }

    public CollageRestException(String msg, Throwable nested, int status) {
        super(msg, nested);
        this.status = status;
    }
    public CollageRestException(String msg, Throwable nested) {
        super(msg, nested);
    }

    public int getStatus() {
        return status;
    }

}
