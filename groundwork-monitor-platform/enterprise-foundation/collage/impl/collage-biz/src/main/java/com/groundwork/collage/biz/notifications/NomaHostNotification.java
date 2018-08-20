package com.groundwork.collage.biz.notifications;

import com.groundwork.collage.CollageStatus;

public class NomaHostNotification {

    private String hostState;
    private String hostName;
    private String hostGroupNames;
    private String notificationType;
    private String hostAddress;
    private String hostOutput;
    private String checkDateTime;
    private String hostNotificationId;
    private String notificationAuthOrAlias;
    private String notificationComment;
    private String notificationRecipients;

    public NomaHostNotification() {}

    public NomaHostNotification(String hostName, String device, String hostState, String groupNames, String message, String checkDateTime) {
        this.hostName = hostName;
        this.hostAddress = device;
        this.hostState = hostState;
        this.hostGroupNames = groupNames;
        this.hostOutput = message;
        this.checkDateTime = checkDateTime;
        this.notificationType = (CollageStatus.UP.status.equals(hostState) ? "RECOVERY" : "PROBLEM");
        /*
        notification.setHostNotificationId("33");
        notification.setNotificationComment("some comment");
        notification.setNotificationRecipients("admin@gwos.com");
        notification.setNotificationAuthOrAlias("alias");
         */
    }

    public String getHostState() {
        return hostState;
    }

    public void setHostState(String hostState) {
        this.hostState = hostState;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getHostGroupNames() {
        return hostGroupNames;
    }

    public void setHostGroupNames(String hostGroupNames) {
        this.hostGroupNames = hostGroupNames;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }

    public String getHostOutput() {
        return hostOutput;
    }

    public void setHostOutput(String hostOutput) {
        this.hostOutput = hostOutput;
    }

    public String getCheckDateTime() {
        return checkDateTime;
    }

    public void setCheckDateTime(String checkDateTime) {
        this.checkDateTime = checkDateTime;
    }

    public String getHostNotificationId() {
        return hostNotificationId;
    }

    public void setHostNotificationId(String hostNotificationId) {
        this.hostNotificationId = hostNotificationId;
    }

    public String getNotificationAuthOrAlias() {
        return notificationAuthOrAlias;
    }

    public void setNotificationAuthOrAlias(String notificationAuthOrAlias) {
        this.notificationAuthOrAlias = notificationAuthOrAlias;
    }

    public String getNotificationComment() {
        return notificationComment;
    }

    public void setNotificationComment(String notificationComment) {
        this.notificationComment = notificationComment;
    }

    public String getNotificationRecipients() {
        return notificationRecipients;
    }

    public void setNotificationRecipients(String notificationRecipients) {
        this.notificationRecipients = notificationRecipients;
    }
}
