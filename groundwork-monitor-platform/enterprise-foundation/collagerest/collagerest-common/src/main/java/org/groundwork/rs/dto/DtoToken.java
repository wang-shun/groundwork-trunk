package org.groundwork.rs.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "token")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoToken {

    @XmlAttribute
    protected String value;

    @XmlAttribute
    protected String app;

    public DtoToken() {
    }

    public DtoToken(String value, String app) {
        this.app = app;
        this.value = value;
    }

    public String toString() {
        return String.format("token: %s, app: %s",
                (value == null) ? "" : value,
                (app == null) ? "" : app);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }
}
