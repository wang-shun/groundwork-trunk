package org.groundwork.rs.dto;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="devices")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoDeviceList {

    @XmlElement(name="device")
    @JsonProperty("devices")
    private List<DtoDevice> devices = new ArrayList<DtoDevice>();

    public DtoDeviceList() {}
    public DtoDeviceList(List<DtoDevice> devices) {this.devices = devices;}

    public List<DtoDevice> getDevices() {
        return devices;
    }

    public void add(DtoDevice device) {
        devices.add(device);
    }

    public int size() {
        return devices.size();
    }

}
