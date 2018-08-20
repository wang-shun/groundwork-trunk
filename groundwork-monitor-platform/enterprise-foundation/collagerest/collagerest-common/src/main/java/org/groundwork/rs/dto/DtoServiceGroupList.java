package org.groundwork.rs.dto;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="serviceGroups")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoServiceGroupList {


    @XmlElement(name="serviceGroup")
    @JsonProperty("serviceGroups")
    private List<DtoServiceGroup> serviceGroups = new ArrayList<DtoServiceGroup>();

    public DtoServiceGroupList() {}
    public DtoServiceGroupList(List<DtoServiceGroup> serviceGroups) {this.serviceGroups = serviceGroups;}

    public List<DtoServiceGroup> getServiceGroups() {
        return serviceGroups;
    }

    public void add(DtoServiceGroup serviceGroup) {
        serviceGroups.add(serviceGroup);
    }

    public int size() {
        return serviceGroups.size();
    }

}
