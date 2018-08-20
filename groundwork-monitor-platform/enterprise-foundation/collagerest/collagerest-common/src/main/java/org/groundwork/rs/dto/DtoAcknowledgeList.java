package org.groundwork.rs.dto;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="acks")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoAcknowledgeList {

    @XmlElement(name="ack")
    @JsonProperty("acks")
    private List<DtoAcknowledge> acks = new ArrayList<DtoAcknowledge>();

    public DtoAcknowledgeList() {}
    public DtoAcknowledgeList(List<DtoAcknowledge> acks) { this.acks = acks; }

    public List<DtoAcknowledge> getAcks() {
        return acks;
    }

    public void add(DtoAcknowledge ack) {
        acks.add(ack);
    }

    public int size() {
        return acks.size();
    }

}
