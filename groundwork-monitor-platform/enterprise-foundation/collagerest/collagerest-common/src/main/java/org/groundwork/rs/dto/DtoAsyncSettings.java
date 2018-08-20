package org.groundwork.rs.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "asyncSettings")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoAsyncSettings {

    @XmlAttribute
    private Integer threadPoolSize;

    @XmlAttribute
    private Integer queueSize;

    @XmlAttribute
    private Integer throttleThreshold;

    @XmlAttribute
    private Integer throttleWaitMs;

    public DtoAsyncSettings() {
    }

    public DtoAsyncSettings(Integer threadPoolSize, Integer queueSize, Integer throttleThreshold, Integer throttleWaitMs) {
        this.threadPoolSize = threadPoolSize;
        this.queueSize = queueSize;
        this.throttleThreshold = throttleThreshold;
        this.throttleWaitMs = throttleWaitMs;
    }

    public Integer getThreadPoolSize() {
        return threadPoolSize;
    }

    public void setThreadPoolSize(Integer threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }

    public Integer getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(Integer queueSize) {
        this.queueSize = queueSize;
    }

    public Integer getThrottleThreshold() {
        return throttleThreshold;
    }

    public void setThrottleThreshold(Integer throttleThreshold) {
        this.throttleThreshold = throttleThreshold;
    }

    public Integer getThrottleWaitMs() {
        return throttleWaitMs;
    }

    public void setThrottleWaitMs(Integer throttleWaitMs) {
        this.throttleWaitMs = throttleWaitMs;
    }
}
