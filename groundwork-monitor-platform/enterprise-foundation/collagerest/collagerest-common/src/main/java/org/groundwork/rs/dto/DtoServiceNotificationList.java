package org.groundwork.rs.dto;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="notifications")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoServiceNotificationList {

    @XmlElement(name="notification")
    @JsonProperty("notifications")
    private List<DtoServiceNotification> notifications = new ArrayList<DtoServiceNotification>();

    public DtoServiceNotificationList() {}
    public DtoServiceNotificationList(List<DtoServiceNotification> notifications) {this.notifications = notifications;}

    public List<DtoServiceNotification> getNotifications() {
        return notifications;
    }

    public void add(DtoServiceNotification notification) {
        notifications.add(notification);
    }

    public int size() {
        return notifications.size();
    }

}
