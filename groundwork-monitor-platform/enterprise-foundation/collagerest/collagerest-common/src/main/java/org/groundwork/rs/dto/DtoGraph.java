package org.groundwork.rs.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;

@XmlRootElement(name = "graph")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoGraph {

    @XmlAttribute
    private String label;

    @XmlElement
    private byte[] graph;

    public DtoGraph() {
    }

    public DtoGraph(String label, byte[] graph) {
        this.label = label;
        this.graph = Arrays.copyOf(graph, graph.length);
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public byte[] getGraph() {
        return graph;
    }

    public void setGraph(byte[] graph) {
        this.graph = graph;
    }
}
