package org.groundwork.rs.dto;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="results")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoOperationResults {

    public static final String INSERT = "Insert";
    public static final String UPDATE = "Update";
    public static final String DELETE = "Delete";
    public static final String CLEAR = "Clear";
    public static final String RENAME = "Rename";

    @XmlAttribute
    private int successful = 0;
    @XmlAttribute
    private Integer failed = 0;
    @XmlAttribute
    private String entityType;
    @XmlAttribute
    private String operation;
    @XmlAttribute
    private Integer warning = 0;

    @XmlElement(name="result")
    @JsonProperty("results")
    private List<DtoOperationResult> results = new ArrayList<DtoOperationResult>();

    public DtoOperationResults() {}

    public DtoOperationResults(String entityType, String operation) {
        this.entityType = entityType;
        this.operation = operation;
    }

    public DtoOperationResults(String entityType, String operation, List<DtoOperationResult> results) {
        this.entityType = entityType;
        this.operation = operation;
        this.results = results;
        int successCount = 0;
        int failureCount = 0;
        int warningCount = 0;
        for (DtoOperationResult result : results) {
            switch (result.getStatus()) {
                case DtoOperationResult.SUCCESS: successCount++; break;
                case DtoOperationResult.FAILURE: failureCount++; break;
                case DtoOperationResult.WARNING: warningCount++; break;
            }
        }
        setSuccessful(successCount);
        setFailed(failureCount);
        setWarning(warningCount);
    }

    public void merge(DtoOperationResults otherResults) {
        successful += otherResults.successful;
        if (otherResults.failed != null) {
            if (failed == null) {
                failed = otherResults.failed;
            } else {
                failed += otherResults.failed;
            }
        }
        if (otherResults.entityType != null) {
            entityType = otherResults.entityType;
        }
        if (otherResults.operation != null) {
            operation = otherResults.operation;
        }
        if (otherResults.warning != null) {
            if (warning == null) {
                warning = otherResults.warning;
            } else {
                warning += otherResults.warning;
            }
        }
        if (otherResults.results != null) {
            if (results == null) {
                results = otherResults.results;
            } else {
                results.addAll(otherResults.results);
            }
        }
    }

    public Integer getSuccessful() {
        return successful;
    }

    public void setSuccessful(Integer successful) {
        this.successful = successful;
    }

    public Integer getFailed() {
        return failed;
    }

    public void setFailed(Integer failed) {
        this.failed = failed;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    @XmlAttribute(name="count")
    public Integer getCount() {
        return results.size();
    }

    public void setCount(Integer count) {
    }

    public List<DtoOperationResult> getResults() {
        return results;
    }

    public void setResults(List<DtoOperationResult> results) {
        this.results = results;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public Integer getWarning() {
        return warning;
    }

    public void setWarning(Integer warning) {
        this.warning = warning;
    }

    public void fail(String entity, String message) {
        DtoOperationResult op = new DtoOperationResult(entity, message);
        op.setStatus(DtoOperationResult.FAILURE);
        failed++;
        results.add(op);
    }

    public void warn(String entity, String message) {
        DtoOperationResult op = new DtoOperationResult(entity, message);
        op.setStatus(DtoOperationResult.WARNING);
        warning++;
        results.add(op);
    }

    public void success(String entity, URI location) {
        DtoOperationResult op = new DtoOperationResult(entity, location);
        op.setStatus(DtoOperationResult.SUCCESS);
        successful++;
        results.add(op);
    }

    public void success(String entity, String message) {
        DtoOperationResult op = new DtoOperationResult(entity, message);
        op.setStatus(DtoOperationResult.SUCCESS);
        successful++;
        results.add(op);
    }

    public void success(String entity, URI location, String message) {
        DtoOperationResult op = new DtoOperationResult(entity, location, message);
        op.setStatus(DtoOperationResult.SUCCESS);
        successful++;
        results.add(op);
    }

    public String summary() {
        return String.format("entityType: %s, operation: %s, successful: %d, failed: %d, warning: %d, count: %d",
                entityType, operation, successful, ((failed != null) ? failed : 0), ((warning != null) ? warning : 0), getCount());
    }

    public String toString() {
        return String.format("entityType: %s, operation: %s, successful: %d, failed: %d, warning: %d, results: %s",
                entityType, operation, successful, ((failed != null) ? failed : 0), ((warning != null) ? warning : 0),
                results);
    }
}

