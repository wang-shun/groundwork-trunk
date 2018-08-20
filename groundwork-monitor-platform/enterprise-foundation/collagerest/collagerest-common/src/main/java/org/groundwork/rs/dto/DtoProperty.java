package org.groundwork.rs.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "property")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoProperty {

    @XmlAttribute
    private String name;
//    @XmlAttribute
//    private String type;
    @XmlAttribute
    private String value;

    public DtoProperty() {}

    public DtoProperty(String key, Object property) {
        this.name = key;
        this.value = PropertiesSupport.convertProperty(property);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }


}
