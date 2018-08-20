package org.groundwork.rs.dto;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * DtoStateTransitionList
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@XmlRootElement(name="stateTransitionList")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoStateTransitionList {

    @XmlElement(name="stateTransition")
    @JsonProperty("stateTransitionList")
    private List<DtoStateTransition> stateTransitions = new ArrayList<>();

    public DtoStateTransitionList() {}
    public DtoStateTransitionList(List<DtoStateTransition> stateTransitions) {this.stateTransitions = stateTransitions;}

    public List<DtoStateTransition> getStateTransitions() {
        return stateTransitions;
    }

    public void add(DtoStateTransition stateTransition) {
        stateTransitions.add(stateTransition);
    }

    public int size() {
        return stateTransitions.size();
    }
}
