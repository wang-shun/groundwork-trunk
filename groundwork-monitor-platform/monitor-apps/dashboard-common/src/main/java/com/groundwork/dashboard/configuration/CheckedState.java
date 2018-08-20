package com.groundwork.dashboard.configuration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlAccessorType(XmlAccessType.FIELD)
public class CheckedState {
    @XmlAttribute
    private Boolean checked;
    @XmlAttribute
    private String name;
    @XmlAttribute
    private String displayName;

    public CheckedState() {}
    
    public CheckedState(String name, Boolean checked) {
        this.name = name;
        this.checked = checked;
    }

    public CheckedState(String name, String  displayName, Boolean checked) {
        this.name = name;
        this.displayName = displayName;
        this.checked = checked;
    }

    public Boolean getChecked() {
        return checked;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }
}
