package org.groundwork.rs.dto;

import javax.xml.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@XmlAccessorType(XmlAccessType.FIELD)
public abstract class JaxbBean {

    @XmlAttribute
    private Integer id;

    @XmlElementWrapper(name="properties")
    @XmlElement(name="property")
    private Map<String, Object> properties;

    public JaxbBean() {}

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    protected void initProperties(Map<String, Object> props) {
        if (props != null) {
            properties = new HashMap<String, Object>();
            properties.putAll(props);
        }
    }
}
