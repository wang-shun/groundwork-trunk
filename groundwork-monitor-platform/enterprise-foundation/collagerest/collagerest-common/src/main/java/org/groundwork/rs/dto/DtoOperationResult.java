package org.groundwork.rs.dto;

import java.net.URI;

public class DtoOperationResult {

    public final static String SUCCESS = "success";
    public final static String FAILURE = "failure";
    public final static String WARNING = "warning";

    private String entity;
    private String status = FAILURE;
    private String message;
    private String location;

    public DtoOperationResult() {}

    public DtoOperationResult(String entity, String message) {
        this.entity = entity;
        this.message = message;
    }

    public DtoOperationResult(String entity, URI location) {
        this.entity = entity;
        this.location = location.toString();
    }

    public DtoOperationResult(String entity, URI location, String message) {
        this(entity, location);
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String toString() {
        return String.format("entity: %s, status: %s, message: %s ", entity, status, (message == null) ? "" : message);
    }
}
