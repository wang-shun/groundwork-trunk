package com.groundwork.dashboard.configuration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlRootElement(name = "dashboard")
public class DashboardConfiguration {

    private String name;
    private String title;
    private String serviceGroup;
    private String hostGroup;
    private Boolean autoExpand = true;

    private int downtimeHours = 2;
    private int percentageSLA = 90;
    private int availabilityHours = 24;

    private int rows = 20;
    private int refreshSeconds = 60;

    private List<CheckedState> ackFilters = new ArrayList<>();
    private List<CheckedState> downTimeFilters = new ArrayList<>();
    private List<CheckedState> states = new ArrayList<>();
    private List<CheckedState> columns = new ArrayList<>();

    public DashboardConfiguration() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getServiceGroup() {
        return serviceGroup;
    }

    public void setServiceGroup(String serviceGroup) {
        this.serviceGroup = serviceGroup;
    }

    public String getHostGroup() {
        return hostGroup;
    }

    public void setHostGroup(String hostGroup) {
        this.hostGroup = hostGroup;
    }

    public Boolean getAutoExpand() {
        return autoExpand;
    }

    public void setAutoExpand(Boolean autoExpand) {
        this.autoExpand = autoExpand;
    }

    public int getDowntimeHours() {
        return downtimeHours;
    }

    public void setDowntimeHours(int downtimeHours) {
        this.downtimeHours = downtimeHours;
    }

    public int getAvailabilityHours() {
        return availabilityHours;
    }

    public void setAvailabilityHours(int availabilityHours) {
        this.availabilityHours = availabilityHours;
    }

    public int getPercentageSLA() {
        return percentageSLA;
    }

    public void setPercentageSLA(int percentageSLA) {
        this.percentageSLA = percentageSLA;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getRefreshSeconds() {
        return refreshSeconds;
    }

    public void setRefreshSeconds(int refreshSeconds) {
        this.refreshSeconds = refreshSeconds;
    }

    @XmlElementWrapper(name="ackFilters")
    @XmlElement(name="filter")
    public List<CheckedState> getAckFilters() {
        return ackFilters;
    }

    public void setAckFilters(List<CheckedState> ackFilters) {
        this.ackFilters = ackFilters;
    }

    @XmlElementWrapper(name="downtimeFilters")
    @XmlElement(name="filter")
    public List<CheckedState> getDownTimeFilters() {
        return downTimeFilters;
    }

    public void setDownTimeFilters(List<CheckedState> downTimeFilters) {
        this.downTimeFilters = downTimeFilters;
    }

    @XmlElementWrapper
    @XmlElement(name="state")
    public List<CheckedState> getStates() {
        return states;
    }

    public void setStates(List<CheckedState> states) {
        this.states = states;
    }

    @XmlElementWrapper
    @XmlElement(name="column")
    public List<CheckedState> getColumns() {
        return columns;
    }

    public void setColumns(List<CheckedState> columns) {
        this.columns = columns;
    }

    // Defaults
    public static final String ACKED = "Acked";
    public static final String NOT_ACKED = "Not Acked";
    public static final String IN_DOWNTIME = "In Downtime";
    public static final String NOT_IN_DOWNTIME = "Not In Downtime";
    public static final String STATE_OK = "OK";
    public static final String STATE_WARNING = "Warning";
    public static final String STATE_CRITICAL = "Critical";
    public static final String STATE_UNSCHEDULED_CRITICAL = "Unscheduled Critical";
    public static final String STATE_UNKNOWN = "Unknown";
    public static final String STATE_PENDING = "Pending";
    public static final String COLUMNS_HOST = "Host";
    public static final String COLUMNS_SERVICE_NAME = "Service Name";
    public static final String COLUMNS_STATUS = "Status";
    public static final String COLUMNS_TIME_DOWN = "Time Down";
    public static final String COLUMNS_MAINTENANCE = "Maintenance";
    public static final String COLUMNS_ACK = "Ack";
    public static final String COLUMNS_AVAILABILITY = "Availability";
    public static final String COLUMNS_COMMENT = "Comment";

    public static void setDefaults(DashboardConfiguration dashboard) {
        dashboard.ackFilters.add(new CheckedState(ACKED, false));
        dashboard.ackFilters.add(new CheckedState(NOT_ACKED, true));
        dashboard.downTimeFilters.add(new CheckedState(IN_DOWNTIME, true));
        dashboard.downTimeFilters.add(new CheckedState(NOT_IN_DOWNTIME, true));
        dashboard.states.add(new CheckedState(STATE_OK, true));
        dashboard.states.add(new CheckedState(STATE_WARNING, true));
        dashboard.states.add(new CheckedState(STATE_CRITICAL, true));
        dashboard.states.add(new CheckedState(STATE_UNSCHEDULED_CRITICAL, true));
        dashboard.states.add(new CheckedState(STATE_UNKNOWN, true));
        dashboard.states.add(new CheckedState(STATE_PENDING, true));
        dashboard.columns.add(new CheckedState(COLUMNS_HOST, true));
        dashboard.columns.add(new CheckedState(COLUMNS_SERVICE_NAME, true));
        dashboard.columns.add(new CheckedState(COLUMNS_STATUS, true));
        dashboard.columns.add(new CheckedState(COLUMNS_TIME_DOWN, true));
        dashboard.columns.add(new CheckedState(COLUMNS_MAINTENANCE, true));
        dashboard.columns.add(new CheckedState(COLUMNS_ACK, true));
        dashboard.columns.add(new CheckedState(COLUMNS_AVAILABILITY, true));
        dashboard.columns.add(new CheckedState(COLUMNS_COMMENT, true));
    }
}
