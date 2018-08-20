package org.groundwork.cloudhub.configuration;

import org.hibernate.validator.constraints.NotBlank;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Created by dtaylor on 4/28/17.
 */
@XmlRootElement(name = "view")
@XmlType(propOrder={"enabled", "name"})
@XmlAccessorType(XmlAccessType.FIELD)
public class ConfigurationView {

    @XmlAttribute
    @NotBlank(message="View Name cannot be empty.")
    private String name = null;

    @XmlAttribute
    private Boolean enabled = false;

    @XmlAttribute
    private Boolean isService = false;

    public ConfigurationView() {
    }

    public ConfigurationView(String name, Boolean enabled, Boolean isService) {
        this.name = name;
        this.enabled = enabled;
        this.isService = isService;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean isService() {
        return isService;
    }

    public void setService(Boolean service) {
        isService = service;
    }
}
