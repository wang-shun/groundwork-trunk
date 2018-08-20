package org.groundwork.rs.client;

import java.util.Date;

public class GraphParameterBuilder {
    private String applicationType = null;
    private String hostName = null;
    private String serviceName = null;
    private Long startDate = null;
    private Long endDate = null;
    private Integer graphWidth = null;

    public String getApplicationType() {
        return applicationType;
    }

    /**
     * @param applicationType is an optional parameter. Will default to NAGIOS if not specified.
     * @return the builder representing all parameters
     */
    public GraphParameterBuilder setApplicationType(String applicationType) {
        this.applicationType = applicationType;
        return this;
    }

    public String getHostName() {
        return hostName;
    }

    /**
     * @param hostName is a required parameter. The host name for which all graphs will be retrieved.
     * @return the builder representing all parameters
     */
    public GraphParameterBuilder setHostName(String hostName) {
        this.hostName = hostName;
        return this;
    }

    public String getServiceName() {
        return serviceName;
    }

    /**
     * @param serviceName is an optional parameter. If not specified, a graph will be generated for all services
     *                    for the given host name.
     * @return the builder representing all parameters
     */
    public GraphParameterBuilder setServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }


    public Long getStartDate() {
        return startDate;
    }

    /**
     * @param startDate Start date is total number of seconds since epoch (time in seconds since 01-01-1970)
     *                  Optional parameter. If not specified, will default to 24 hours prior to now.
     * @return the builder representing all parameters
     */
    public GraphParameterBuilder setStartDate(Long startDate) {
        this.startDate = startDate;
        return this;
    }

    public Long getEndDate() {
        return endDate;
    }

    /**
     * @param endDate optional. Total number of seconds since epoch (time in seconds since 01-01-1970)Default to current time
     *                If not specified, defaults to now.
     * @return the builder representing all parameters
     */
    public GraphParameterBuilder setEndDate(Long endDate) {
        this.endDate = endDate;
        return this;
    }

    public Integer getGraphWidth() {
        return graphWidth;
    }

    /**
     * @param graphWidth optional. The width of the graph in pixels.
     *                If not specified, defaults to 620
     * @return the builder representing all parameters
     */
    public GraphParameterBuilder setGraphWidth(Integer graphWidth) {
        this.graphWidth = graphWidth;
        return this;
    }

    /**
     * @param interval short hand for setting the number of seconds prior to 'now' for the start date
     * @return the builder representing all parameters
     */
    public GraphParameterBuilder setStartDateInterval(Long interval) {
        this.startDate = new Date().getTime() - interval;
        return this;
    }

    /**
     * @return the builder representing all parameters
     */
    public GraphParameterBuilder build() {
        return this;
    }

    public String toString() {
        String message = String.format("host: %s, service %s, appType %s, start: %d, end: %d, graphWidth: %d",
                (hostName == null) ? "" : hostName,
                (serviceName == null) ? "" : serviceName,
                (applicationType == null) ? "" : applicationType,
                (startDate == null) ? "0" : startDate,
                (endDate == null) ? "0" : endDate,
                (graphWidth == null) ? "0" : graphWidth);
        return message;
    }

}
