package org.groundwork.cloudhub.metrics;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dtaylor on 5/18/17.
 */
public class BaseCollectionState implements MetricCollectionState {

    private Map<String,Object> properties = new HashMap();
    private Map<String,String> exceptions = new HashMap();

    public BaseCollectionState() {}
    
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
