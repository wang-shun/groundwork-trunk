package org.groundwork.rs.dto;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

/**
 * DtoStateTransition
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@XmlRootElement(name = "stateTransition")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoStateTransition {

    @XmlAttribute
    private String hostName;
    @XmlAttribute
    private String serviceName;
    @XmlElement(name="fromStatus")
    @JsonProperty("fromStatus")
    private DtoMonitorStatus fromStatus;
    @XmlAttribute
    private Date fromTransitionDate;
    @XmlElement(name="toStatus")
    @JsonProperty("toStatus")
    private DtoMonitorStatus toStatus;
    @XmlAttribute
    private Date toTransitionDate;
    @XmlAttribute
    private Long durationInState;

    public DtoStateTransition() {}

    public DtoStateTransition(String hostName, String serviceName, DtoMonitorStatus fromStatus, Date fromTransitionDate,
                              DtoMonitorStatus toStatus, Date toTransitionDate, Long durationInState) {
        this.hostName = hostName;
        this.serviceName = serviceName;
        this.fromStatus = fromStatus;
        this.fromTransitionDate = fromTransitionDate;
        this.toStatus = toStatus;
        this.toTransitionDate = toTransitionDate;
        this.durationInState = durationInState;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public DtoMonitorStatus getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(DtoMonitorStatus fromStatus) {
        this.fromStatus = fromStatus;
    }

    public Date getFromTransitionDate() {
        return fromTransitionDate;
    }

    public void setFromTransitionDate(Date fromTransitionDate) {
        this.fromTransitionDate = fromTransitionDate;
    }

    public DtoMonitorStatus getToStatus() {
        return toStatus;
    }

    public void setToStatus(DtoMonitorStatus toStatus) {
        this.toStatus = toStatus;
    }

    public Date getToTransitionDate() {
        return toTransitionDate;
    }

    public void setToTransitionDate(Date toTransitionDate) {
        this.toTransitionDate = toTransitionDate;
    }

    public Long getDurationInState() {
        return durationInState;
    }

    public void setDurationInState(Long durationInState) {
        this.durationInState = durationInState;
    }
}
