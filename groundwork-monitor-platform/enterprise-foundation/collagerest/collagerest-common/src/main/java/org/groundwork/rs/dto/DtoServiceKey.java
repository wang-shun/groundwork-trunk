package org.groundwork.rs.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "service")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoServiceKey {

    @XmlAttribute
    private String service;

    @XmlAttribute
    private String host;

    public DtoServiceKey() {
    }

    public DtoServiceKey(String service, String host) {
        this.service = service;
        this.host = host;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof DtoServiceKey)) {
            return false;
        }
        DtoServiceKey otherKey = (DtoServiceKey) other;
        return (host.equals(otherKey.host) &&
                ((service == null && otherKey.service == null) ||
                        (service != null && service.equals(otherKey.service))));
    }

    @Override
    public int hashCode() {
        return host.hashCode()*31+(service != null ? service.hashCode() : 0);
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
