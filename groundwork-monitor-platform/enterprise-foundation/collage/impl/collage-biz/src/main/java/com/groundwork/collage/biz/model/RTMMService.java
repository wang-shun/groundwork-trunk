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

import java.util.Date;

/**
 * RTMMService
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class RTMMService {

    private final Integer id;

    private String description;
    private String monitorStatus;
    private String appTypeName;
    private String appTypeDisplayName;
    private Date lastCheckTime;
    private Date nextCheckTime;
    private Date lastStateChange;
    private Boolean isProblemAcknowledged;
    private Long currentAttempt;
    private Long maxAttempts;
    private Integer scheduledDowntimeDepth;
    private String lastPluginOutput;
    private String performanceData;

    public RTMMService(Integer id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).
                append("id", id).
                append("description", description).
                append("monitorStatus", monitorStatus).
                append("appTypeName", appTypeName).
                append("appTypeDisplayName", appTypeDisplayName).
                append("lastCheckTime", lastCheckTime).
                append("nextCheckTime", nextCheckTime).
                append("lastStateChange", lastStateChange).
                append("isProblemAcknowledged", isProblemAcknowledged).
                append("currentAttempt", currentAttempt).
                append("maxAttempts", maxAttempts).
                append("scheduledDowntimeDepth", scheduledDowntimeDepth).
                append("lastPluginOutput", lastPluginOutput).
                append("performanceData", performanceData).
                toString();
    }

    public Integer getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMonitorStatus() {
        return monitorStatus;
    }

    public void setMonitorStatus(String monitorStatus) {
        this.monitorStatus = monitorStatus;
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

    public Boolean getIsProblemAcknowledged() {
        return isProblemAcknowledged;
    }

    public void setIsProblemAcknowledged(Boolean isProblemAcknowledged) {
        this.isProblemAcknowledged = isProblemAcknowledged;
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

    public Integer getScheduledDowntimeDepth() {
        return scheduledDowntimeDepth;
    }

    public void setScheduledDowntimeDepth(Integer scheduledDowntimeDepth) {
        this.scheduledDowntimeDepth = scheduledDowntimeDepth;
    }

    public String getLastPluginOutput() {
        return lastPluginOutput;
    }

    public void setLastPluginOutput(String lastPluginOutput) {
        this.lastPluginOutput = lastPluginOutput;
    }

    public String getPerformanceData() {
        return performanceData;
    }

    public void setPerformanceData(String performanceData) {
        this.performanceData = performanceData;
    }
}
