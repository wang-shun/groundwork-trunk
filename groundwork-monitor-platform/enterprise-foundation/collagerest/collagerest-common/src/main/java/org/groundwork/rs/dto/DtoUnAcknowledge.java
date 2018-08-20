package org.groundwork.rs.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "unack")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoUnAcknowledge {

    @XmlAttribute
    protected String appType;

    @XmlAttribute
    protected String host;

    @XmlAttribute
    private String service;

    public DtoUnAcknowledge() {
    }

    public DtoUnAcknowledge(String appType, String hostName) {
        this.appType = appType;
        this.host = hostName;
    }

    public String toString() {
        return String.format("appType: %s, hostName: %s, service: %s",
                (appType == null) ? "" : appType,
                (host == null) ? "" : host,
                (service == null) ? "" : service);
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

    public void setService(String service) {
        this.service = service;
    }
}
