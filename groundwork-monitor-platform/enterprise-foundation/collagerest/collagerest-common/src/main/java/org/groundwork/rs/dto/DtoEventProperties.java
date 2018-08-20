package org.groundwork.rs.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "event")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoEventProperties extends DtoPropertiesBase {

    @XmlAttribute
    protected Integer id;

    public DtoEventProperties() {}

    public DtoEventProperties(Integer id) {
        this.id = id ;
    }

    public int getId() {
        return id;
    }

}