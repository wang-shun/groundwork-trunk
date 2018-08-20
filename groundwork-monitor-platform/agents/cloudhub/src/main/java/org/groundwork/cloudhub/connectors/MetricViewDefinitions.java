package org.groundwork.cloudhub.connectors;

import org.groundwork.cloudhub.configuration.ConfigurationView;
import org.groundwork.cloudhub.metrics.BaseQuery;
import org.groundwork.cloudhub.metrics.ComputeType;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dtaylor on 5/5/17.
 */
public class MetricViewDefinitions {
    private final ConfigurationView view;
    private Map<String,BaseQuery> queryMap;
    private boolean useServiceNameKey = true;
    private boolean standardView = false;

    public MetricViewDefinitions(ConfigurationView view, List<BaseQuery> queries) {
        this(view, queries, true);
    }
    public MetricViewDefinitions(ConfigurationView view, List<BaseQuery> queries, boolean useServiceNameKey) {
        this(view, queries, useServiceNameKey, false);
    }

    public MetricViewDefinitions(ConfigurationView view, List<BaseQuery> queries, boolean useServiceNameKey, boolean standardView) {
        this.standardView = standardView;
        this.useServiceNameKey = useServiceNameKey;
        this.view = view;
        this.queryMap = new HashMap<>();
        for (BaseQuery query : queries) {
            if (standardView) {
                String name = (useServiceNameKey) ? query.getServiceName() : query.getQuery();
                queryMap.put(name, query);
            }
            else {
                if (query.getServiceType() != null && query.getServiceType().equals(view.getName())) {
                    if (query.getComputeType().equals(ComputeType.health)) {
                        queryMap.put(query.getQuery(), query);
                    } else {
                        String name = (useServiceNameKey) ? query.getServiceName() : query.getQuery();
                        queryMap.put(name, query);
                    }
                }
            }
        }
    }

    public String getViewName() {
        return view.getName();
    }

    public Boolean isEnabled() {
        return view.isEnabled();
    }

    public Boolean isService() {
        return view.isService();
    }

    public BaseQuery getMetric(String name) {
        return queryMap.get(name);
    }

    public Collection<BaseQuery> getQueries() {
        return queryMap.values();
    }

    public Map<String,BaseQuery> getQueryMap() {
        return queryMap;
    }

}
