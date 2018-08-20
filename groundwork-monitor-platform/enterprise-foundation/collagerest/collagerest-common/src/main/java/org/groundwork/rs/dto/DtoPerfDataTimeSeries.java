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

package org.groundwork.rs.dto;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * DtoPerfDataTimeSeries
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@XmlRootElement(name = "perfDataTimeSeries")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoPerfDataTimeSeries {

    @XmlAttribute
    private String appType;
    @XmlAttribute
    private String serverName;
    @XmlAttribute
    private String serviceName;
    @XmlAttribute
    private Long startTime;
    @XmlAttribute
    private Long endTime;
    @XmlAttribute
    private Long interval;
    @XmlElement(name="perfDataTimeSeriesValue")
    @JsonProperty("perfDataTimeSeriesValues")
    private List<DtoPerfDataTimeSeriesValue> perfDataTimeSeriesValues = new ArrayList<DtoPerfDataTimeSeriesValue>();

    /**
     * Default persistence constructor.
     */
    public DtoPerfDataTimeSeries() {
    }

    /**
     * Constructor.
     *
     * @param appType performance data application type or null for all
     * @param serverName performance data server name
     * @param serviceName performance data service name
     * @param startTime performance data start time, (millis since epoch)
     * @param endTime performance data end time, (millis since epoch)
     * @param interval performance data interval, (millis)
     */
    public DtoPerfDataTimeSeries(String appType, String serverName, String serviceName, Long startTime, Long endTime,
                                 Long interval) {
        this.appType = appType;
        this.serverName = serverName;
        this.serviceName = serviceName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.interval = interval;
    }

    /**
     * Constructor with performance data time series values.
     *
     * @param appType performance data application type or null for all
     * @param serverName performance data server name
     * @param serviceName performance data service name
     * @param startTime performance data start time, (millis since epoch)
     * @param endTime performance data end time, (millis since epoch)
     * @param interval performance data interval, (millis)
     * @param perfDataTimeSeriesValues performance data time series values
     */
    public DtoPerfDataTimeSeries(String appType, String serverName, String serviceName, Long startTime, Long endTime,
                                 Long interval, List<DtoPerfDataTimeSeriesValue> perfDataTimeSeriesValues) {
        this(appType, serverName, serviceName, startTime, endTime, interval);
        this.perfDataTimeSeriesValues.addAll(perfDataTimeSeriesValues);
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public Long getInterval() {
        return interval;
    }

    public void setInterval(Long interval) {
        this.interval = interval;
    }

    public List<DtoPerfDataTimeSeriesValue> getPerfDataTimeSeriesValues() {
        return perfDataTimeSeriesValues;
    }

    public void add(DtoPerfDataTimeSeriesValue perfDataTimeSeriesValue) {
        perfDataTimeSeriesValues.add(perfDataTimeSeriesValue);
    }

    public void add(DtoPerfDataTimeSeries perfDataTimeSeries) {
        if ((perfDataTimeSeries != null) && (perfDataTimeSeries.size() > 0)) {
            perfDataTimeSeriesValues.addAll(perfDataTimeSeries.getPerfDataTimeSeriesValues());
        }
    }

    public int size() {
        return perfDataTimeSeriesValues.size();
    }
}
