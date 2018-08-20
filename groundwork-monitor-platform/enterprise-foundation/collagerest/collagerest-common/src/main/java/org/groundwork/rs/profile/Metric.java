package org.groundwork.rs.profile;

import java.io.Serializable;


@Deprecated
public class Metric implements Serializable {
	private String name = null;
	
	private String description = null;
	
	private boolean isMonitored = false;
	
	private boolean isGraphed = false;
	
	private double warningThreshold = 30000.0;
	
	private double criticalThreshold = 1500.0;

    public Metric() {
    }

    public Metric(String name, boolean monitored, boolean graphed, String warningThreshold,	String criticalThreshold) {
        this.name = name;
        this.isMonitored = monitored;
        this.isGraphed = graphed;
        this.warningThreshold = Double.parseDouble(warningThreshold);
        this.criticalThreshold = Double.parseDouble(criticalThreshold);
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

	public boolean isMonitored() {
		return isMonitored;
	}

	public void setMonitored(boolean isMonitored) {
		this.isMonitored = isMonitored;
	}

	public boolean isGraphed() {
		return isGraphed;
	}

	public void setGraphed(boolean isGraphed) {
		this.isGraphed = isGraphed;
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
	

}
