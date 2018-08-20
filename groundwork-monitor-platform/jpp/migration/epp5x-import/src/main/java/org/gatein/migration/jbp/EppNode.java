package org.gatein.migration.jbp;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

public class EppNode {
    private String              uri;
    private String              name;
    private Map<String, String> label      = new HashMap<String, String>();
    private String              visibility = "DISPLAYED";
    private String              parentUri;
    private String              portalName;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getLabel() {
        return label;
    }

    public void addLabel(String locale, String value) {
        // System.out.println("LABEL VALUE1: " + value);
        // System.out.println("LABEL VALUE2: " +
        // Arrays.asList(StringUtils.split(value))); //
        // System.out.println("LABEL VALUE3: " +
        // StringUtils.join(StringUtils.split(value), " "));
        this.label.put(locale, StringUtils.join(StringUtils.split(value), " "));
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getPortalName() {
        return portalName;
    }

    public void setPortalName(String portalName) {
        this.portalName = portalName;
    }

    public String getParentUri() {
        return parentUri;
    }

    public void setParentUri(String parentUri) {
        this.parentUri = parentUri;
    }

    public String getPageReference() {
        return parentUri + "::" + uri;
    }
}
