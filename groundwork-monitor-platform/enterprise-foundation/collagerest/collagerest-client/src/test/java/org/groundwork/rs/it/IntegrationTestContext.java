package org.groundwork.rs.it;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IntegrationTestContext<T> {

    public static String DEFAULT_DELIMITER = "-";
    private Map<String,T> results = new HashMap<>();
    private String prefix;
    private String agentId;
    private int start;
    private int count;
    private String delimiter = DEFAULT_DELIMITER;
    private String owner;
    private Boolean skipUnmatchedResults = false;
    private Boolean reuseChildren = false;
    private String[] monitorStatuses = null;
    private Map<String,Object[]> propertyValues = new ConcurrentHashMap<>();

    public IntegrationTestContext(String prefix, String agentId, int start , int count) {
        this(prefix, agentId, start, count, null, null);
    }

    public IntegrationTestContext(String prefix, String agentId, int start , int count, String owner, String[] status) {
        this.prefix = prefix;
        this.agentId = agentId;
        this.start = start;
        this.count = count;
        this.owner = owner;
        this.monitorStatuses = status;
    }

    public void addResult(String key, T result) {
        results.put(key, result);
    }

    public T lookupResult(String key) {
        return results.get(key);
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Collection<T> getEntities() {
        return results.values();
    }

    public Boolean getSkipUnmatchedResults() {
        return skipUnmatchedResults;
    }

    public void setSkipUnmatchedResults(Boolean skipUnmatchedResults) {
        this.skipUnmatchedResults = skipUnmatchedResults;
    }

    public Boolean getReuseChildren() {
        return reuseChildren;
    }

    public void setReuseChildren(Boolean reuseChildren) {
        this.reuseChildren = reuseChildren;
    }

    public String[] getMonitorStatuses() {
        return monitorStatuses;
    }

    public void setMonitorStatuses(String[] monitorStatuses) {
        this.monitorStatuses = monitorStatuses;
    }

    public void setPropertyValues(String property, Object[] values) {
        propertyValues.put(property, values);
    }

    public Object[] getValuesForProperty(String property) {
        return propertyValues.get(property);
    }

    public int getResultsSize() {
        return this.results.size();
    }

    public String formatNameKey(int n) {
        return String.format(prefix + delimiter + IntegrationTestGenerator.FORMAT_NUMBER_SUFFIX, n);
    }
}
