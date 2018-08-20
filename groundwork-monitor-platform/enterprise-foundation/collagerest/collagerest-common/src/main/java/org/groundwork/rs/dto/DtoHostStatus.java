package org.groundwork.rs.dto;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

@XmlRootElement(name = "hostStatus")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoHostStatus {

    @XmlAttribute
    private Integer hostStatusId;
    @XmlElement(name="monitorStatus")
    @JsonProperty("hostMonitorStatus")
    private DtoMonitorStatus hostMonitorStatus;
    @XmlAttribute
    private Date lastCheckTime;
    @XmlElement(name="checkType")
    private DtoCheckType checkType;
    @XmlAttribute
    private Date nextCheckTime;
    @XmlElement(name="stateType")
    private DtoStateType stateType;

    public DtoHostStatus() {}

    public Integer getHostStatusId() {
        return hostStatusId;
    }

    public void setHostStatusId(Integer hostStatusId) {
        this.hostStatusId = hostStatusId;
    }

    public DtoMonitorStatus getHostMonitorStatus() {
        return hostMonitorStatus;
    }

    public void setHostMonitorStatus(DtoMonitorStatus hostMonitorStatus) {
        this.hostMonitorStatus = hostMonitorStatus;
    }

    public Date getLastCheckTime() {
        return lastCheckTime;
    }

    public void setLastCheckTime(Date lastCheckTime) {
        this.lastCheckTime = lastCheckTime;
    }

    public DtoCheckType getCheckType() {
        return checkType;
    }

    public void setCheckType(DtoCheckType checkType) {
        this.checkType = checkType;
    }

    public Date getNextCheckTime() {
        return nextCheckTime;
    }

    public void setNextCheckTime(Date nextCheckTime) {
        this.nextCheckTime = nextCheckTime;
    }

    public DtoStateType getStateType() {
        return stateType;
    }

    public void setStateType(DtoStateType stateType) {
        this.stateType = stateType;
    }
}
