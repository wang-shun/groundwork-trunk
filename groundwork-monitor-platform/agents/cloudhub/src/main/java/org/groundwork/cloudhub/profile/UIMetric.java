package org.groundwork.cloudhub.profile;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.groundwork.agents.utils.StringUtils;
import org.groundwork.rs.dto.profiles.Metric;

import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.math.BigDecimal;

@JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
public class UIMetric implements Serializable {

	private String name = null;
    private String customName = null;
	private String description = null;
	private boolean monitored = false;
	private boolean graphed = false;
    private double warningThreshold = 30000.0;
    private double criticalThreshold = 1500.0;
    private String expression = null;
    private String format = null;
    private String serviceType = null;
    private String sourceType;
    private String computeType;

    @Pattern(regexp="(-1|[0-9]{1,14}(?:\\.[0-9]{1,2})?)", message="Not a valid number.")
    private String uiWarningThreshold = "30000.0";

    @Pattern(regexp="(-1|[0-9]{1,14}(?:\\.[0-9]{1,2})?)", message="Not a valid number.")
    private String uiCriticalThreshold = "1500.0";

    public UIMetric() {}

    public UIMetric(Metric metric) {
        setName(metric.getName());
        setDescription(metric.getDescription());
        setMonitored(metric.isMonitored());
        setGraphed(metric.isGraphed());
        setWarningThreshold(metric.getWarningThreshold());
        setCriticalThreshold(metric.getCriticalThreshold());
        setUiWarningThreshold(String.valueOf(new BigDecimal(metric.getWarningThreshold())));
        setUiCriticalThreshold(String.valueOf(new BigDecimal(metric.getCriticalThreshold())));
        setSourceType(metric.getSourceType());
        setComputeType(metric.getComputeType());
        setCustomName(metric.getCustomName());
        setExpression(metric.getExpression());
        setFormat(metric.getFormat());

        setServiceType(metric.getServiceType());
    }

    @JsonProperty
    public String getServiceName() {
        if (!StringUtils.isEmpty(customName)) {
            return customName;
        }
        return name;
    }

    public void setServiceName(String name) {
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public double getWarningThreshold() {
		return warningThreshold;
	}

	public void setWarningThreshold(double warningThreshold) {
		this.warningThreshold = warningThreshold;
	}

	public double getCriticalThreshold() {
		return criticalThreshold;
	}

	public void setCriticalThreshold(double criticalThreshold) {
		this.criticalThreshold = criticalThreshold;
	}

    public boolean isMonitored() {
        return monitored;
    }

    public void setMonitored(boolean monitored) {
        this.monitored = monitored;
    }

    public boolean isGraphed() {
        return graphed;
    }

    public void setGraphed(boolean graphed) {
        this.graphed = graphed;
    }
    
    public String getUiWarningThreshold() {
		return uiWarningThreshold;
	}

	public void setUiWarningThreshold(String uiWarningThreshold) {
		this.uiWarningThreshold = uiWarningThreshold;
	}

	public String getUiCriticalThreshold() {
		return uiCriticalThreshold;
	}

	public void setUiCriticalThreshold(String uiCriticalThreshold) {
		this.uiCriticalThreshold = uiCriticalThreshold;
	}

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getComputeType() {
        return computeType;
    }

    public void setComputeType(String computeType) {
        this.computeType = computeType;
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }
}
