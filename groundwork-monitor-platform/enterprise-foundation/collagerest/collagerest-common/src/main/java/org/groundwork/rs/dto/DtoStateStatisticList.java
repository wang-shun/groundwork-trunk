package org.groundwork.rs.dto;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="statistics")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoStateStatisticList {

    @XmlElement(name="statistic")
    @JsonProperty("statistics")
    private List<DtoStateStatistic> statistics = new ArrayList<DtoStateStatistic>();

    public DtoStateStatisticList() {}
    public DtoStateStatisticList(List<DtoStateStatistic> statistics) {this.statistics = statistics;}

    public List<DtoStateStatistic> getStatistics() {
        return statistics;
    }

    public void add(DtoStateStatistic statistic) {
        statistics.add(statistic);
    }

    public int size() {
        return statistics.size();
    }

}
