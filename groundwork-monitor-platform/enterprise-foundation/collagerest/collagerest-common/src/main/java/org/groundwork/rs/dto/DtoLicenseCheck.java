package org.groundwork.rs.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "licenseCheck")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoLicenseCheck {

    @XmlAttribute
    private boolean success = false;
    @XmlAttribute
    private String message ;
    @XmlAttribute
    private int devicesRequested;
    @XmlAttribute
    private int devices;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getDevicesRequested() {
        return devicesRequested;
    }

    public void setDevicesRequested(int devicesRequested) {
        this.devicesRequested = devicesRequested;
    }

    public int getDevices() {
        return devices;
    }

    public void setDevices(int devices) {
        this.devices = devices;
    }
}
