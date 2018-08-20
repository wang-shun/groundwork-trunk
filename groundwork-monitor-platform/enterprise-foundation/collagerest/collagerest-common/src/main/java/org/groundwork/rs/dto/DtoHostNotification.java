package org.groundwork.rs.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "notification")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoHostNotification {

    @XmlAttribute
    private String hostState;
    @XmlAttribute
    private String hostName;
    @XmlAttribute
    private String hostGroupNames;
    @XmlAttribute
    private String notificationType;
    @XmlAttribute
    private String hostAddress;
    @XmlAttribute
    private String hostOutput;
    @XmlAttribute
    private String checkDateTime;
    @XmlAttribute
    private String hostNotificationId;
    @XmlAttribute
    private String notificationAuthOrAlias;
    @XmlAttribute
    private String notificationComment;
    @XmlAttribute
    private String notificationRecipients;

    public DtoHostNotification() {}

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
