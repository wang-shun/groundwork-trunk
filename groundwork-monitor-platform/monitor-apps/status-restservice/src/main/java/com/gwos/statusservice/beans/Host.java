package com.gwos.statusservice.beans;

import javax.xml.bind.annotation.XmlAttribute;

public class Host extends BaseEntity {
	
	private Service[] service = null;
	private String currentAttempt = "NA";
	private String maxAttempts = "NA";
	private String lastCheckTime = "NA";
	private String nextCheckTime = "NA";
	private String lastStateChange = "NA";
	private String alias = "NA";

    /* Service summary statistics summarized on the Host level */
    private int pendingNormal = 0;
    private int pendingDowntime = 0;

    private int okNormal = 0;
    private int okDowntime = 0;

    private int warningNormal = 0;
    private int warningDowntime = 0;
    private int warningAck = 0;
    private int warningAckdown = 0;

    private int criticalNormal = 0;
    private int criticalDowntime = 0;
    private int criticalAck = 0;
    private int criticalAckdown = 0;

    private int unknownNormal = 0;
    private int unknownDowntime = 0;
    private int unknownAck = 0;
    private int unknownAckdown = 0;

    // Getters and setters
	/* All getters have the XmlAttribute annotation
	   The JAXB doesn't allow Uppercase and underscore in the attribute name. For this reason the expected name,
	   example CRITICAL_normal, needs to be defined explicit. The internal name has to be criticalNormal otherwise
	   the marshalling will fail.
	 */


    @XmlAttribute(name = "PENDING_normal")
    public int getPendingNormal() {
        return pendingNormal;
    }

    public void setPendingNormal(int pendingNormal) {
        this.pendingNormal = pendingNormal;
    }

    @XmlAttribute(name = "PENDING_downtime")
    public int getPendingDowntime() {
        return pendingDowntime;
    }

    public void setPendingDowntime(int pendingDowntime) {
        this.pendingDowntime = pendingDowntime;
    }

    @XmlAttribute(name = "OK_normal")
    public int getOkNormal() {
        return okNormal;
    }

    public void setOkNormal(int okNormal) {
        this.okNormal = okNormal;
    }

    @XmlAttribute(name = "OK_downtime")
    public int getOkDowntime() {
        return okDowntime;
    }

    public void setOkDowntime(int okDowntime) {
        this.okDowntime = okDowntime;
    }

    @XmlAttribute(name = "WARNING_normal")
    public int getWarningNormal() {
        return warningNormal;
    }

    public void setWarningNormal(int warningNormal) {
        this.warningNormal = warningNormal;
    }

    @XmlAttribute(name = "WARNING_downtime")
    public int getWarningDowntime() {
        return warningDowntime;
    }

    public void setWarningDowntime(int warningDowntime) {
        this.warningDowntime = warningDowntime;
    }

    @XmlAttribute(name = "WARNING_ack")
    public int getWarningAck() {
        return warningAck;
    }

    public void setWarningAck(int warningAck) {
        this.warningAck = warningAck;
    }

    @XmlAttribute(name = "WARNING_ackdown")
    public int getWarningAckdown() {
        return warningAckdown;
    }

    public void setWarningAckdown(int warningAckdown) {
        this.warningAckdown = warningAckdown;
    }

    @XmlAttribute(name = "CRITICAL_normal")
    public int getCriticalNormal() {
        return criticalNormal;
    }

    public void setCriticalNormal(int criticalNormal) {
        this.criticalNormal = criticalNormal;
    }

    @XmlAttribute(name = "CRITICAL_downtime")
    public int getCriticalDowntime() {
        return criticalDowntime;
    }

    public void setCriticalDowntime(int criticalDowntime) {
        this.criticalDowntime = criticalDowntime;
    }

    @XmlAttribute(name = "CRITICAL_ack")
    public int getCriticalAck() {
        return criticalAck;
    }

    public void setCriticalAck(int criticalAck) {
        this.criticalAck = criticalAck;
    }

    @XmlAttribute(name = "CRITICAL_ackdown")
    public int getCriticalAckdown() {
        return criticalAckdown;
    }

    public void setCriticalAckdown(int criticalAckdown) {
        this.criticalAckdown = criticalAckdown;
    }

    @XmlAttribute(name = "UNKNOWN_normal")
    public int getUnknownNormal() {
        return unknownNormal;
    }

    public void setUnknownNormal(int unknownNormal) {
        this.unknownNormal = unknownNormal;
    }

    @XmlAttribute(name = "UNKNOWN_downtime")
    public int getUnknownDowntime() {
        return unknownDowntime;
    }

    public void setUnknownDowntime(int unknownDowntime) {
        this.unknownDowntime = unknownDowntime;
    }

    @XmlAttribute(name = "UNKNOWN_ack")
    public int getUnknownAck() {
        return unknownAck;
    }

    public void setUnknownAck(int unknownAck) {
        this.unknownAck = unknownAck;
    }

    @XmlAttribute(name = "UNKNOWN_ackdown")
    public int getUnknownAckdown() {
        return unknownAckdown;
    }

    public void setUnknownAckdown(int unknownAckdown) {
        this.unknownAckdown = unknownAckdown;
    }

	public Service[] getService() {
		return service;
	}

	public void setService(Service[] service) {
		this.service = service;
	}

	@XmlAttribute
	public String getCurrentAttempt() {
		return currentAttempt;
	}

	public void setCurrentAttempt(String currentAttempt) {
		this.currentAttempt = currentAttempt;
	}

	@XmlAttribute
	public String getLastCheckTime() {
		return lastCheckTime;
	}
	
	public void setLastCheckTime(String lastCheckTime) {
		this.lastCheckTime = lastCheckTime;
	}

	@XmlAttribute
	public String getNextCheckTime() {
		return nextCheckTime;
	}

	public void setNextCheckTime(String nextCheckTime) {
		this.nextCheckTime = nextCheckTime;
	}

	@XmlAttribute
	public String getLastStateChange() {
		return lastStateChange;
	}

	public void setLastStateChange(String lastStateChange) {
		this.lastStateChange = lastStateChange;
	}

	@XmlAttribute
	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	@XmlAttribute
	public String getMaxAttempts() {
		return maxAttempts;
	}

	public void setMaxAttempts(String maxAttempts) {
		this.maxAttempts = maxAttempts;
	}
	
	

}
