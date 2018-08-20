/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2015  GroundWork Open Source Solutions info@groundworkopensource.com

 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of version 2 of the GNU General Public License
 *     as published by the Free Software Foundation.

 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.

 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.groundwork.collage.biz.model;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * RTMMHost
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class RTMMHost {

    private final Integer id;
    private final List<RTMMService> services = new ArrayList<RTMMService>();

    private String hostName;
    private String monitorStatus;
    private String alias;
    private String appTypeName;
    private String appTypeDisplayName;
    private Date lastCheckTime;
    private Date nextCheckTime;
    private Date lastStateChange;
    private Boolean isAcknowledged;
    private Boolean isProblemAcknowledged;
    private Integer scheduledDowntimeDepth;
    private Long currentAttempt;
    private Long maxAttempts;
    private String lastPluginOutput;

    public RTMMHost(Integer id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).
                append("id", id).
                append("services", services).
                append("hostName", hostName).
                append("monitorStatus", monitorStatus).
                append("alias", alias).
                append("appTypeName", appTypeName).
                append("appTypeDisplayName", appTypeDisplayName).
                append("lastCheckTime", lastCheckTime).
                append("nextCheckTime", nextCheckTime).
                append("lastStateChange", lastStateChange).
                append("isAcknowledged", isAcknowledged).
                append("isProblemAcknowledged", isProblemAcknowledged).
                append("scheduledDowntimeDepth", scheduledDowntimeDepth).
                append("currentAttempt", currentAttempt).
                append("maxAttempts", maxAttempts).
                append("lastPluginOutput", lastPluginOutput).
                toString();
    }

    public Integer getId() {
        return id;
    }

    public List<RTMMService> getServices() {
        return services;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getMonitorStatus() {
        return monitorStatus;
    }

    public void setMonitorStatus(String monitorStatus) {
        this.monitorStatus = monitorStatus;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAppTypeName() {
        return appTypeName;
    }

    public void setAppTypeName(String appTypeName) {
        this.appTypeName = appTypeName;
    }

    public String getAppTypeDisplayName() {
        return appTypeDisplayName;
    }

    public void setAppTypeDisplayName(String appTypeDisplayName) {
        this.appTypeDisplayName = appTypeDisplayName;
    }

    public Date getLastCheckTime() {
        return lastCheckTime;
    }

    public void setLastCheckTime(Date lastCheckTime) {
        this.lastCheckTime = lastCheckTime;
    }

    public Date getNextCheckTime() {
        return nextCheckTime;
    }

    public void setNextCheckTime(Date nextCheckTime) {
        this.nextCheckTime = nextCheckTime;
    }

    public Date getLastStateChange() {
        return lastStateChange;
    }

    public void setLastStateChange(Date lastStateChange) {
        this.lastStateChange = lastStateChange;
    }

    public Boolean getIsAcknowledged() {
        return isAcknowledged;
    }

    public void setIsAcknowledged(Boolean isAcknowledged) {
        this.isAcknowledged = isAcknowledged;
    }

    public Boolean getIsProblemAcknowledged() {
        return isProblemAcknowledged;
    }

    public void setIsProblemAcknowledged(Boolean isProblemAcknowledged) {
        this.isProblemAcknowledged = isProblemAcknowledged;
    }

    public Integer getScheduledDowntimeDepth() {
        return scheduledDowntimeDepth;
    }

    public void setScheduledDowntimeDepth(Integer scheduledDowntimeDepth) {
        this.scheduledDowntimeDepth = scheduledDowntimeDepth;
    }

    public Long getCurrentAttempt() {
        return currentAttempt;
    }

    public void setCurrentAttempt(Long currentAttempt) {
        this.currentAttempt = currentAttempt;
    }

    public Long getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(Long maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public String getLastPluginOutput() {
        return lastPluginOutput;
    }

    public void setLastPluginOutput(String lastPluginOutput) {
        this.lastPluginOutput = lastPluginOutput;
    }
}
