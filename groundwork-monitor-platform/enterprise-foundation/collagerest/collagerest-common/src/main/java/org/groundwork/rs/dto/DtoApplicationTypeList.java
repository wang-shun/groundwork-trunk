package org.groundwork.rs.dto;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="applicationTypes")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoApplicationTypeList {

    @XmlElement(name="applicationType")
    @JsonProperty("applicationTypes")
    private List<DtoApplicationType> applicationTypes = new ArrayList<DtoApplicationType>();

    public DtoApplicationTypeList() {}
    public DtoApplicationTypeList(List<DtoApplicationType> applicationTypes) {this.applicationTypes = applicationTypes;}

    public List<DtoApplicationType> getApplicationTypes() {
        return applicationTypes;
    }

    public void add(DtoApplicationType applicationType) {
        applicationTypes.add(applicationType);
    }

    public int size() {
        return applicationTypes.size();
    }

}
