package org.groundwork.rs.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "notification")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoServiceNotification {

    @XmlAttribute
    private String serviceState;
    @XmlAttribute
    private String hostName;
    @XmlAttribute
    private String hostGroupNames;
    @XmlAttribute
    private String serviceGroupNames;
    @XmlAttribute
    private String serviceDescription;
    @XmlAttribute
    private String serviceOutput;
    @XmlAttribute
    private String notificationType;
    @XmlAttribute
    private String hostAlias;
    @XmlAttribute
    private String hostAddress;
    @XmlAttribute
    private String checkDateTime;
    @XmlAttribute
    private String serviceNotificationId;
    @XmlAttribute
    private String notificationAuthOrAlias;
    @XmlAttribute
    private String notificationComment;
    @XmlAttribute
    private String notificationRecipients;

    public DtoServiceNotification() {}

    public String getServiceState() {
        return serviceState;
    }

    public void setServiceState(String serviceState) {
        this.serviceState = serviceState;
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

    public String getServiceGroupNames() {
        return serviceGroupNames;
    }

    public void setServiceGroupNames(String serviceGroupNames) {
        this.serviceGroupNames = serviceGroupNames;
    }

    public String getServiceDescription() {
        return serviceDescription;
    }

    public void setServiceDescription(String serviceDescription) {
        this.serviceDescription = serviceDescription;
    }

    public String getServiceOutput() {
        return serviceOutput;
    }

    public void setServiceOutput(String serviceOutput) {
        this.serviceOutput = serviceOutput;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getHostAlias() {
        return hostAlias;
    }

    public void setHostAlias(String hostAlias) {
        this.hostAlias = hostAlias;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }

    public String getCheckDateTime() {
        return checkDateTime;
    }

    public void setCheckDateTime(String checkDateTime) {
        this.checkDateTime = checkDateTime;
    }

    public String getServiceNotificationId() {
        return serviceNotificationId;
    }

    public void setServiceNotificationId(String serviceNotificationId) {
        this.serviceNotificationId = serviceNotificationId;
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
