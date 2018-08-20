package org.groundwork.rs.dto;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="perfDataList")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoPerfDataList {

    @XmlElement(name="perfData")
    @JsonProperty("perfDataList")
    private List<DtoPerfData> perfDatas = new ArrayList<DtoPerfData>();

    public DtoPerfDataList() {}
    public DtoPerfDataList(List<DtoPerfData> perfDatas) {this.perfDatas = perfDatas;}

    public List<DtoPerfData> getPerfDataList() {
        return perfDatas;
    }

    public void add(DtoPerfData perfData) {
        perfDatas.add(perfData);
    }

    public int size() {
        return perfDatas.size();
    }

}
