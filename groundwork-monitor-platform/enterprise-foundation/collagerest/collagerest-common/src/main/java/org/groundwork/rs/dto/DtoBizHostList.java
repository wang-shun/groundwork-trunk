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
public class DtoBizHostList {

    @XmlElement(name="host")
    @JsonProperty("hosts")
    private List<DtoBizHost> hosts = new ArrayList<DtoBizHost>();

    public DtoBizHostList() {}
    public DtoBizHostList(List<DtoBizHost> hosts) {this.hosts = hosts;}

    public List<DtoBizHost> getHosts() {
        return hosts;
    }

    public void add(DtoBizHost host) {
        hosts.add(host);
    }

    public int size() {
        return hosts.size();
    }
    
}
