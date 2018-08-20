package org.groundwork.rs.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ack")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoAcknowledge {

    @XmlAttribute
    protected String appType;

    @XmlAttribute
    protected String host;

    @XmlAttribute
    private String service;

    @XmlAttribute
    private String acknowledgedBy;

    @XmlAttribute
    private String acknowledgeComment;

    public DtoAcknowledge() {
    }

    public DtoAcknowledge(String appType, String host) {
        this.appType = appType;
        this.host = host;
    }

    public DtoAcknowledge(String appType, String hostName, String serviceDescription) {
        this.appType = appType;
        this.host = hostName;
        this.service = serviceDescription;
    }

    public String toString() {
        return String.format("appType: %s, hostName: %s, service: %s, acknowledgedBy: %s, acknowledgeComment: %s",
                (appType == null) ? "" : appType,
                (host == null) ? "" : host,
                (service == null) ? "" : service,
                (acknowledgedBy == null) ? "" : acknowledgedBy,
                (acknowledgeComment == null) ? "" : acknowledgeComment);
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String hostName) {
        this.host = hostName;
    }

    public String getService() {
        return service;
    }

    public void setService(String serviceDescription) {
        this.service = serviceDescription;
    }

    public String getAcknowledgedBy() {
        return acknowledgedBy;
    }

    public void setAcknowledgedBy(String acknowledgedBy) {
        this.acknowledgedBy = acknowledgedBy;
    }

    public String getAcknowledgeComment() {
        return acknowledgeComment;
    }

    public void setAcknowledgeComment(String acknowledgeComment) {
        this.acknowledgeComment = acknowledgeComment;
    }
}
