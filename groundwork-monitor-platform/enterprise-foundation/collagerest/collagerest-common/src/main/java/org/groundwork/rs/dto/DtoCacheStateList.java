package org.groundwork.rs.dto;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="caches")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoCacheStateList {

    @XmlElement(name="cache")
    @JsonProperty("caches")
    private List<DtoCacheState> cacheStates = new ArrayList<DtoCacheState>();

    public DtoCacheStateList() {}
    public DtoCacheStateList(List<DtoCacheState> cs) {this.cacheStates = cs;}

    public List<DtoCacheState> getCacheStates() {
        return cacheStates;
    }

    public void add(DtoCacheState cs) {
        cacheStates.add(cs);
    }

    public int size() {
        return cacheStates.size();
    }

}
