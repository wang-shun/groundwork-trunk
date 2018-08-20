package org.groundwork.rs.dto;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="propertyTypes")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoPropertyTypeList {

    @XmlElement(name="propertyType")
    @JsonProperty("propertyTypes")
    private List<DtoPropertyType> propertyTypes = new ArrayList<DtoPropertyType>();

    public DtoPropertyTypeList() {}
    public DtoPropertyTypeList(List<DtoPropertyType> propertyTypes) {this.propertyTypes = propertyTypes;}

    public List<DtoPropertyType> getPropertyTypes() {
        return propertyTypes;
    }

    public void add(DtoPropertyType propertyType) {
        propertyTypes.add(propertyType);
    }

    public int size() {
        return propertyTypes.size();
    }

}
