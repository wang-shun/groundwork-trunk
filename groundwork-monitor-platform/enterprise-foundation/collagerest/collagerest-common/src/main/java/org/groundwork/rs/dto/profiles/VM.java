package org.groundwork.rs.dto.profiles;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@XmlRootElement(name = "vm")
@XmlAccessorType(XmlAccessType.FIELD)
public class VM  implements Serializable{

    @XmlElement(name="metric")
    private List<Metric> metrics;

    public VM() {
        metrics = new LinkedList<Metric>();
    }

    public List<Metric> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<Metric> metrics) {
        this.metrics = metrics;
    }

    public void addMetric(Metric metric) {
        if (metrics == null) {
            metrics = new LinkedList<Metric>();
        }
        metrics.add(metric);
    }

}
