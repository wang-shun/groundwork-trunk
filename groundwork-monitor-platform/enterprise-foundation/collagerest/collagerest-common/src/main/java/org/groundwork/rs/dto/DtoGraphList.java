package org.groundwork.rs.dto;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="graphs")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoGraphList {

    @XmlElement(name="graph")
    @JsonProperty("graphs")
    private List<DtoGraph> graphs = new ArrayList<DtoGraph>();

    public DtoGraphList() {}
    public DtoGraphList(List<DtoGraph> graphs) { this.graphs = graphs; }

    public List<DtoGraph> getGraphs() {
        return graphs;
    }

    public void add(DtoGraph graph) {
        graphs.add(graph);
    }

    public int size() {
        return graphs.size();
    }

}
