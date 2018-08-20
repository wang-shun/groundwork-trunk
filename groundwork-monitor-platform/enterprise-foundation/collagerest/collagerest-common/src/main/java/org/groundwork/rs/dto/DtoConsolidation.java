package org.groundwork.rs.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "consolidation")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoConsolidation {

    @XmlAttribute
    private Integer id;

    @XmlAttribute
    private String name;

    @XmlAttribute
    private String criteria;

    public DtoConsolidation() {}

    public DtoConsolidation(String name, String criteria) {
        this.name = name;
        this.criteria = criteria;
    }

    public DtoConsolidation(Integer id, String name, String criteria) {
        this.id = id;
        this.name = name;
        this.criteria = criteria;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer consolidationId) {
        this.id = consolidationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCriteria() {
        return criteria;
    }

    public void setCriteria(String criteria) {
        this.criteria = criteria;
    }

    public String toString() {
        return String.format("Consolidation: %d - %s - %s", id, name, criteria);
    }

}
