package org.groundwork.rs.dto.profiles;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "metric")
@XmlType(propOrder={"description", "expression", "format", "sourceType", "computeType", "criticalThreshold", "warningThreshold", "graphed", "monitored", "customName", "name"})
//@XmlType(propOrder={"name", "customName", "monitored", "graphed", "warningThreshold", "criticalThreshold", "sourceType", "computeType", "expression", "format", "description"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Metric {

    public static final String SOURCE_TYPE_CEILOMETER = "ceilometer";
    public static final String SOURCE_TYPE_COMPUTE = "compute";
    public static final String SOURCE_TYPE_STORAGE = "storage";
    public static final String SOURCE_TYPE_NETWORK = "network";
    public static final String SOURCE_TYPE_RESOURCEPOOL = "resourcePool";

    public static final String COMPUTE_TYPE_REGEX = "regex";

    @XmlAttribute
	private String name = null;

    @XmlAttribute
    private String customName = null;

    @XmlAttribute
	private String description = null;
    @XmlAttribute
	private boolean monitored = false;
    @XmlAttribute
	private boolean graphed = false;
    @XmlAttribute
    private double warningThreshold = -1.0;
    @XmlAttribute
    private double criticalThreshold = -1.0;
    @XmlAttribute
    private String sourceType;
    @XmlAttribute
    private String computeType;
    @XmlAttribute
    private String expression;
    @XmlAttribute
    private String format;
    @XmlAttribute
    private String serviceType;

	public Metric() {
    }

    public Metric(String name, String description, boolean monitored, boolean graphed,
                  double warningThreshold,	double criticalThreshold, String sourceType, String computeType,
                  String customName, String expression, String format, String serviceType) {
        this.name = name;
        this.description = description;
        this.monitored = monitored;
        this.graphed = graphed;
        this.warningThreshold = warningThreshold;
        this.criticalThreshold = criticalThreshold;
        this.sourceType = sourceType;
        this.computeType = computeType;
        this.customName = customName;
        this.expression = expression;
        this.format = format;
        this.serviceType = serviceType;
    }

    public String getServiceName() {
        if (!isEmpty(customName)) {
            return customName;
        }
        return name;
    }

    public static boolean isEmpty(String str) {
        return (str == null || str.trim().equals(""));
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
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
