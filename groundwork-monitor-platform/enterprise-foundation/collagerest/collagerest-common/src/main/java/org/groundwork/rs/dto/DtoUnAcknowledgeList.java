package org.groundwork.rs.dto;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="unacks")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoUnAcknowledgeList {

    @XmlElement(name="unack")
    @JsonProperty("unacks")
    private List<DtoUnAcknowledge> unacks = new ArrayList<DtoUnAcknowledge>();

    public DtoUnAcknowledgeList() {}
    public DtoUnAcknowledgeList(List<DtoUnAcknowledge> unacks) { this.unacks = unacks; }

    public List<DtoUnAcknowledge> getUnacks() {
        return unacks;
    }

    public void add(DtoUnAcknowledge unack) {
        unacks.add(unack);
    }

    public int size() {
        return unacks.size();
    }
}
