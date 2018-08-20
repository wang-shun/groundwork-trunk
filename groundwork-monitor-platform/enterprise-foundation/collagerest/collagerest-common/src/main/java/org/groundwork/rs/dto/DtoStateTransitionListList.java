package org.groundwork.rs.dto;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * DtoStateTransitionListList
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@XmlRootElement(name="stateTransitionListList")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoStateTransitionListList {

    @XmlElement(name="stateTransitionList")
    @JsonProperty("stateTransitionListList")
    private List<DtoStateTransitionList> stateTransitionLists = new ArrayList<>();

    public DtoStateTransitionListList() {}
    public DtoStateTransitionListList(List<DtoStateTransitionList> stateTransitionLists) {this.stateTransitionLists = stateTransitionLists;}

    public List<DtoStateTransitionList> getStateTransitionLists() {
        return stateTransitionLists;
    }

    public void add(DtoStateTransitionList stateTransitionList) {
        stateTransitionLists.add(stateTransitionList);
    }

    public int size() {
        return stateTransitionLists.size();
    }
}
