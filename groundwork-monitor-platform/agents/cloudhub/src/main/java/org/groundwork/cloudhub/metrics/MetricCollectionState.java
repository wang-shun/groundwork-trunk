package org.groundwork.cloudhub.metrics;

import java.util.Map;

public interface MetricCollectionState {

    void clear();

    Map<String, Object> getProperties();

    Object getProperty(String name);

    Map<String, String> getExceptions();

    void addException(String key, String message);
}
