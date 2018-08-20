package org.groundwork.cloudhub.connectors.vmware2;

import com.vmware.vim25.DynamicProperty;
import org.groundwork.cloudhub.metrics.MetricCollectionState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetricCollectorInstance implements MetricCollectionState {

    private String name;
    private Map<String,Object> properties = new HashMap();
    private Map<String,String> exceptions = new HashMap();

    public MetricCollectorInstance(String name, List<DynamicProperty> propSet) {
        this.name = name;
        for (DynamicProperty prop : propSet) {
            properties.put(prop.getName(), prop.getVal());
        }
    }

    public String getName() {
        return name;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public Object getProperty(String name) {
        return properties.get(name);
    }

    public Map<String, String> getExceptions() {
        return exceptions;
    }

    public void addException(String key, String message) {
        exceptions.put(key, message);
    }

    @Override
    public void clear() {
        properties.clear();
        exceptions.clear();
    }

}
