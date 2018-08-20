package org.groundwork.cloudhub.metrics;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseProperties {

    private Map<String, Object> properties = new HashMap<>();

    public void addProperty(String name, Object value) {
        properties.put(name, value);
    }

    public Object getProperty(String name) {
        return properties.get(name);
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}
