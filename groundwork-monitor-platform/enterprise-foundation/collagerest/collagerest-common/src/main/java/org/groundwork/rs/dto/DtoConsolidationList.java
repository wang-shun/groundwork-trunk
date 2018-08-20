package org.groundwork.rs.dto;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="consolidations")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoConsolidationList {

    @XmlElement(name="consolidation")
    @JsonProperty("consolidations")
    private List<DtoConsolidation> consolidations = new ArrayList<DtoConsolidation>();

    public DtoConsolidationList() {}
    public DtoConsolidationList(List<DtoConsolidation> consolidations) {this.consolidations = consolidations;}

    public List<DtoConsolidation> getConsolidations() {
        return consolidations;
    }

    public void add(DtoConsolidation consolidation) {
        consolidations.add(consolidation);
    }

    public int size() {
        return consolidations.size();
    }

}
