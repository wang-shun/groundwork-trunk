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
public class DtoServiceGroupUpdateList {


    @XmlElement(name="serviceGroup")
    @JsonProperty("serviceGroups")
    private List<DtoServiceGroupUpdate> serviceGroups = new ArrayList<DtoServiceGroupUpdate>();

    public DtoServiceGroupUpdateList() {}
    public DtoServiceGroupUpdateList(List<DtoServiceGroupUpdate> serviceGroups) {this.serviceGroups = serviceGroups;}

    public List<DtoServiceGroupUpdate> getServiceGroups() {
        return serviceGroups;
    }

    public void add(DtoServiceGroupUpdate serviceGroup) {
        serviceGroups.add(serviceGroup);
    }

    public int size() {
        return serviceGroups.size();
    }

}
