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
public class DtoHostNotificationList {

    @XmlElement(name="notification")
    @JsonProperty("notifications")
    private List<DtoHostNotification> notifications = new ArrayList<DtoHostNotification>();

    public DtoHostNotificationList() {}
    public DtoHostNotificationList(List<DtoHostNotification> notifications) {this.notifications = notifications;}

    public List<DtoHostNotification> getNotifications() {
        return notifications;
    }

    public void add(DtoHostNotification notification) {
        notifications.add(notification);
    }

    public int size() {
        return notifications.size();
    }

}
