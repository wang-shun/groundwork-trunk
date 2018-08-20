package org.groundwork.rs.dto;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="hosts")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoHostList {

    @XmlElement(name="host")
    @JsonProperty("hosts")
    private List<DtoHost> hosts = new ArrayList<DtoHost>();

    public DtoHostList() {}
    public DtoHostList(List<DtoHost> hosts) {this.hosts = hosts;}

    public List<DtoHost> getHosts() {
        return hosts;
    }

    public void add(DtoHost host) {
        hosts.add(host);
    }

    public int size() {
        return hosts.size();
    }
    
}
