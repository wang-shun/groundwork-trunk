package org.groundwork.rs.dto;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="hostGroups")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoHostGroupList {

    @XmlElement(name="hostGroup")
    @JsonProperty("hostGroups")
    private List<DtoHostGroup> hostGroups = new ArrayList<DtoHostGroup>();

    public DtoHostGroupList() {}
    public DtoHostGroupList(List<DtoHostGroup> hostGroups) {this.hostGroups = hostGroups;}

    public List<DtoHostGroup> getHostGroups() {
        return hostGroups;
    }

    public void add(DtoHostGroup hostGroup) {
        hostGroups.add(hostGroup);
    }

    public int size() {
        return hostGroups.size();
    }

}
