package org.groundwork.rs.dto;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="entityTypes")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoEntityTypeList {

    @XmlElement(name="entityType")
    @JsonProperty("entityTypes")
    private List<DtoEntityType> entityTypes = new ArrayList<DtoEntityType>();

    public DtoEntityTypeList() {}
    public DtoEntityTypeList(List<DtoEntityType> entityTypes) {this.entityTypes = entityTypes;}

    public List<DtoEntityType> getEntityTypes() {
        return entityTypes;
    }

    public void add(DtoEntityType entityType) {
        entityTypes.add(entityType);
    }

    public int size() {
        return entityTypes.size();
    }

}
