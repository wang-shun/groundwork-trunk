package com.groundwork.downtime;

import java.util.Date;

public class DowntimeMaintenanceWindow {

    public enum MaintenanceStatus {
        Pending,
        Active,
        Expired,
        None
    }

    private MaintenanceStatus status;
    private Float percentage;
    private String message;
    private Date startDate;
    private Date endDate;

    public DowntimeMaintenanceWindow(DowntimeMaintenanceWindow.MaintenanceStatus status, Float percentage, String message) {
        this.status = status;
        this.percentage = percentage;
        this.message = message;
    }

    public DowntimeMaintenanceWindow(DowntimeMaintenanceWindow.MaintenanceStatus status, Float percentage, String message, Date startDate, Date endDate) {
        this.status = status;
        this.percentage = percentage;
        this.message = message;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public MaintenanceStatus getStatus() {
        return status;
    }

    public void setStatus(MaintenanceStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Float getPercentage() {
        return percentage;
    }

    public void setPercentage(Float percentage) {
        this.percentage = percentage;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
