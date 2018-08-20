/*
 * 
 * Copyright 2007 GroundWork Open Source, Inc. ("GroundWork") All rights
 * reserved. This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package com.groundworkopensource.portal.statusviewer.common;

import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.FilterOperator;

import com.groundworkopensource.common.utils.HostFilter;
import com.groundworkopensource.common.utils.Property;
import com.groundworkopensource.common.utils.ServiceFilter;
import com.groundworkopensource.portal.common.CommonConstants;
import com.groundworkopensource.portal.common.FilterAggregator;
import com.groundworkopensource.portal.common.FilterConstants;

/**
 * Class generates filter required for portlets based on filter selected in the
 * filter portlet. It returns separate filters for host group, host, service,
 * service group for calls getHostsByCriteria, getServicesByCriteria,
 * getHostGroupsByCriteria. It also computes separate filters for events -
 * getEventsByCriteria based on whether they are filtered according to host or
 * service parameters.
 * 
 * @author mridu_narang
 */

public class FilterComputer {

    /**
     * LAST_STATE_CHANGE
     */
    private static final String LAST_STATE_CHANGE = "lastStateChange";

    /**
     * Logger
     */
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Filter corresponding to monitor status
     */
    private Filter monitorStatusFilter = null;

    /**
     * Property Filter
     */
    private Filter propertyFilter = new Filter();

    /**
     * Filter corresponding to acknowledged status
     */
    private Filter isAcknowledgedFilter = null;

    /**
     * Filter corresponding to time
     */
    private Filter timeFilter = null;

    /**
     * Filter Aggregator
     */
    private final FilterAggregator filterAggregator;

    /**
     * Default Constructor
     */
    public FilterComputer() {
        this.filterAggregator = FilterAggregator.getInstance();
    }

    /*
     * getHostFilter, getHostGroupFilter, getServiceFilter,
     * getServiceGroupFilter are all based on calls to methods
     * getHostsByCriteria, getServiceByCriteria, getHostGroupsByCriteria. They
     * are rather independent generic filters & not portlet-wise filters.
     * Methods for event filters are specific to event portlet.
     */
    /**
     * Method to retrieve filter for host
     * 
     * @param hostFilterKey
     *            Key to access/identify host filter
     * 
     * @return Filter with host parameters to be applied
     */
    public Filter getHostFilter(String hostFilterKey) {

        // Getting selected filter to apply
        HostFilter hostFilterToApply = this.filterAggregator
                .getHostFilter(hostFilterKey);

        if (hostFilterToApply == null) {
            // Means that portlet has passed an invalid key - throw exception
            this.logger
                    .debug("getHostFilter() : Invalid host key passed as parameter. Cannot retrieve host filter for given key.");
            /*
             * Null filter returned to calling portlet
             * 
             * Each portlet calling this method needs to do a null check, see
             * behavior of ANDing with null filter
             */
            return null;
        }

        // Filter - 1 For monitor status
        if (hostFilterToApply.getMonitorStatus() != null) {
            setMonitorStatusFilter(
                    FilterConstants.HOST_STATUS_HOST_MONITOR_STATUS_NAME,
                    hostFilterToApply.getMonitorStatus().trim());
        }

        // Filter - 2 For propertyFilter
        Property hostProperty = hostFilterToApply.getProperty();

        if (hostProperty != null) {
            // Will never be null due to XML constraints however still check

            if (hostProperty.getName().equalsIgnoreCase(
                    FilterConstants.IS_ACKNOWLEDGED)) {
                setIsAcknowledgedFilter(
                        FilterConstants.HOST_STATUS_PROPERTYVALUES_NAME,
                        FilterConstants.IS_ACKNOWLEDGED,
                        FilterConstants.HOST_STATUS_PROPERTYVALUES_VALUEBOOLEAN,
                        hostProperty.getValue());
                setPropertyFilter(getIsAcknowledgedFilter());
            } else {
                setHostOrHGTimeFilter(CommonConstants.LAST_STATE_CHANGE,
                        hostProperty.getValue(),
                        FilterConstants.HOST_STATUS_PROPERTYVALUES_NAME,
                        FilterConstants.HOST_STATUS_PROPERTY_VALUES_VALUE_DATE);
                setPropertyFilter(getTimeFilter());
            }
        }

        /*
         * If Monitor Status or Property Filter is null then return valid of the
         * 2 filters accordingly. Else AND both property and monitor filters and
         * return it.
         */
        if (this.monitorStatusFilter == null) {
            this.logger
                    .debug("getHostFilter(): Monitor-status filter is null.");

            if (this.propertyFilter == null) {
                this.logger
                        .debug("getHostFilter(): Both monitor-status filter & property filter are null.");
                return null;
            }
            // Return Property Filter
            return this.propertyFilter;
        }
        if (this.propertyFilter == null) {
            this.logger.debug("getHostFilter(): Property filter is null.");
            // Return Monitor Status Filter
            return this.monitorStatusFilter;
        }
        // Apply both filters
        return Filter.AND(this.monitorStatusFilter, this.propertyFilter);
    }

    /**
     * Method to retrieve filter for host group
     * 
     * @param hostFilterKey
     *            Key to access/identify the host filter
     * 
     * @return Filter with host parameters to be applied
     */
    public Filter getHostGroupFilter(String hostFilterKey) {

        // Getting selected filter to apply
        HostFilter hostGroupFilterToApply = this.filterAggregator
                .getHostFilter(hostFilterKey);

        if (hostGroupFilterToApply == null) {
            // Means that portlet has passed an invalid key - throw exception
            this.logger
                    .error("getHostGroupFilter() : Invalid host key passed as parameter. Cannot retrieve host group filter for given key.");
            /*
             * Null filter returned to calling portlet
             * 
             * Each portlet calling this method needs to do a null check, see
             * behavior of ANDing with null filter
             */
            return null;
        }

        // Filter - 1 For monitor status
        if (hostGroupFilterToApply.getMonitorStatus() != null) {
            setMonitorStatusFilter(
                    FilterConstants.HOSTGROUP_MONITORSTATUS_NAME,
                    hostGroupFilterToApply.getMonitorStatus().trim());
        }

        // Filter - 2 For propertyFilter
        Property hostGroupProperty = hostGroupFilterToApply.getProperty();

        if (hostGroupProperty != null) {
            // will never be null due to XML constraints however still check

            if (hostGroupProperty.getName().equalsIgnoreCase(
                    FilterConstants.IS_ACKNOWLEDGED)) {
                setIsAcknowledgedFilter(
                        FilterConstants.HOSTGROUP_PROPERTYVALUES_NAME,
                        FilterConstants.IS_ACKNOWLEDGED,
                        FilterConstants.HOSTGROUP_STATUS_PROPERTYVALUES_VALUEBOOLEAN,
                        hostGroupProperty.getValue());
                setPropertyFilter(getIsAcknowledgedFilter());
            } else {
                setHostOrHGTimeFilter(
                        CommonConstants.LAST_STATE_CHANGE,
                        hostGroupProperty.getValue(),
                        FilterConstants.HOSTGROUP_PROPERTYVALUES_NAME,
                        FilterConstants.HOSTGROUP_STATUS_PROPERTYVALUES_VALUE_DATE);
                setPropertyFilter(getTimeFilter());
            }
        }

        /*
         * If Monitor Status or Property Filter is null then return valid of the
         * 2 filters accordingly. Else AND both property and monitor filters and
         * return it.
         */
        if (this.monitorStatusFilter == null) {
            this.logger
                    .debug("()getHostGroupFilter: Monitor-status filter is null.");

            if (this.propertyFilter == null) {
                this.logger
                        .debug("()getHostGroupFilter: Both monitor-status filter & property filter are null.");
                return null;
            }
            // Return Property Filter
            return this.propertyFilter;
        }
        if (this.propertyFilter == null) {
            this.logger.debug("()getHostGroupFilter: Property filter is null.");
            // Return Monitor Status Filter
            return this.monitorStatusFilter;
        }
        // Apply both filters
        return Filter.AND(this.monitorStatusFilter, this.propertyFilter);
    }

    /**
     * Method to retrieve filter for service
     * 
     * @param serviceFilterKey
     *            Key to access/identify service filter
     * 
     * @return Filter with service parameters to be applied
     */
    public Filter getServiceFilter(String serviceFilterKey) {
        // Getting selected filter to apply
        ServiceFilter serviceFilterToApply = this.filterAggregator
                .getServiceFilter(serviceFilterKey);

        if (serviceFilterToApply == null) {
            // Means that portlet has passed an invalid key - throw exception
            this.logger
                    .error("getServiceFilter() : Invalid service key passed as parameter. Cannot retrieve service filter for given key.");
            /*
             * Null filter returned to calling portlet
             * 
             * Each portlet calling this method needs to do a null check, see
             * behavior of ANDing with null filter
             */

            return null;
        }
        // Filter - 1 For monitor status
        if (serviceFilterToApply.getMonitorStatus() != null) {
            setMonitorStatusFilter(FilterConstants.MONITOR_STATUS_NAME,
                    serviceFilterToApply.getMonitorStatus().trim());
        }
        // Filter - 2 For propertyFilter
        Property serviceProperty = serviceFilterToApply.getProperty();

        if (serviceProperty != null) {
            // will never be null due to XML constraints however still check

            if (serviceProperty.getName().equalsIgnoreCase(
                    FilterConstants.IS_ACKNOWLEDGED)) {
                setIsAcknowledgedFilter(
                        FilterConstants.SERVICE_STATUS_PROPERTYVALUES_NAME,
                        FilterConstants.IS_PROBLEM_ACKNOWLEDGED,
                        FilterConstants.SERVICE_STATUS_PROPERTYVALUES_VALUEBOOLEAN,
                        serviceProperty.getValue());
                setPropertyFilter(getIsAcknowledgedFilter());
            } else {
                setServiceTimeFilter(serviceProperty.getName(), serviceProperty
                        .getValue(), LAST_STATE_CHANGE);
                setPropertyFilter(getTimeFilter());
            }
        }
        /*
         * If Monitor Status or Property Filter is null then return valid of the
         * 2 filters accordingly. Else AND both property and monitor filters and
         * return it.
         */
        if (this.monitorStatusFilter == null) {
            this.logger
                    .debug("getServiceFilter(): Monitor-status filter is null.");

            if (this.propertyFilter == null) {
                this.logger
                        .debug("getServiceFilter(): Both monitor-status filter & property filter are null.");
                return null;
            }
            // Return Property Filter
            return this.propertyFilter;
        }
        if (this.propertyFilter == null) {
            this.logger.debug("getServiceFilter(): Property filter is null.");
            // Return Monitor Status Filter
            return this.monitorStatusFilter;
        }
        // Apply both filters
        return Filter.AND(this.monitorStatusFilter, this.propertyFilter);
    }

    /**
     * Method to retrieve filter for a service group
     * 
     * @param serviceFilterKey
     *            Key to access/identify the service filter
     * 
     * @return Filter with service parameters to be applied
     */

    public Filter getServiceGroupFilter(String serviceFilterKey) {
        // Relay call
        this.logger
                .debug("getServiceGroupFilter() : Retrieving service group filter");
        Filter serviceGroupFilter = getServiceFilter(serviceFilterKey);
        return serviceGroupFilter;
    }

    // EVENT FILTERS

    /**
     * Method to retrieve event filter for host-group-sub-page and network
     * sub-page. Call must be only used in those cases where host filter is
     * applicable independently.
     * 
     * @param hostFilterKey
     *            Key to access/identify host filter
     * 
     * @return Filter with host parameters to be applied
     */
    public Filter getEventHostFilter(String hostFilterKey) {

        // Getting selected filter to apply
        HostFilter hostFilterToApply = this.filterAggregator
                .getHostFilter(hostFilterKey);

        if (hostFilterToApply == null) {
            // Means that portlet has passed an invalid key - throw exception
            this.logger
                    .error("getEventHostFilter() : Invalid host key passed as parameter. Cannot retrieve event host filter for given key.");
            /*
             * Null filter returned to calling portlet
             * 
             * Each portlet calling this method needs to do a null check, see
             * behavior of ANDing with null filter
             */
            return null;
        }

        // Filter - 1 For monitor status
        if (hostFilterToApply.getMonitorStatus() != null) {
            setMonitorStatusFilter(
                    FilterConstants.EVENT_DEVICE_HOSTS_HOSTSTATUS_MONITORSTATUS,
                    hostFilterToApply.getMonitorStatus().trim());
        }

        // Filter - 2 For propertyFilter
        Property hostProperty = hostFilterToApply.getProperty();

        if (hostProperty != null) {
            // will never be null due to XML constraints however still check

            if (hostProperty.getName().equalsIgnoreCase(
                    FilterConstants.IS_ACKNOWLEDGED)) {
                setIsAcknowledgedFilter(
                        FilterConstants.EVENT_DEVICE_HOSTS_HOSTSTATUS_PROPERTYVALUES_NAME,
                        FilterConstants.IS_ACKNOWLEDGED,
                        FilterConstants.EVENT_DEVICE_HOSTS_HOSTSTATUS_PROPERTYVALUES_VALUEBOOLEAN,
                        hostProperty.getValue());
                setPropertyFilter(getIsAcknowledgedFilter());
            } else {
                setHostOrHGTimeFilter(
                        CommonConstants.LAST_STATE_CHANGE,
                        hostProperty.getValue(),
                        FilterConstants.EVENT_DEVICE_HOSTS_HOSTSTATUS_PROPERTYVALUES_NAME,
                        FilterConstants.EVENT_DEVICE_HOSTS_HOSTSTATUS_PROPERTYVALUES_VALUEDATE);
                setPropertyFilter(getTimeFilter());
            }
        }

        /*
         * If Monitor Status or Property Filter is null then return valid of the
         * 2 filters accordingly. Else AND both property and monitor filters and
         * return it.
         */

        if (this.monitorStatusFilter == null) {
            this.logger
                    .debug("getEventHostFilter(): Monitor-status filter is null.");

            if (this.propertyFilter == null) {
                this.logger
                        .debug("getEventHostFilter(): Both monitor-status filter & property filter are null.");
                return null;
            }
            // Return Property Filter
            return this.propertyFilter;
        }
        if (this.propertyFilter == null) {
            this.logger.debug("getEventHostFilter(): Property filter is null.");
            // Return Monitor Status Filter
            return this.monitorStatusFilter;
        }
        // Apply both filters
        return Filter.AND(this.monitorStatusFilter, this.propertyFilter);
    }

    /**
     * Method to retrieve event filter for a service-group-sub-page AND
     * host-sub-page. This call must be used only in those cases where the
     * service filter is applicable
     * 
     * @param serviceFilterKey
     *            Key to access/identify the service filter
     * 
     * @return Filter with service parameters to be applied
     */

    public Filter getEventServiceFilter(String serviceFilterKey) {

        // Getting selected filter to apply
        ServiceFilter serviceFilterToApply = this.filterAggregator
                .getServiceFilter(serviceFilterKey);

        if (serviceFilterToApply == null) {
            // Means that portlet has passed an invalid key - throw exception
            this.logger
                    .error("getEventServiceFilter(): Invalid service key passed as parameter. Cannot retrieve service filter for given key.");
            /*
             * Null filter returned to calling portlet
             * 
             * Each portlet calling this method needs to do a null check, see
             * behavior of ANDing with null filter
             */

            return null;
        }
        // Filter - 1 For monitor status
        if (serviceFilterToApply.getMonitorStatus() != null) {
            setMonitorStatusFilter(
                    FilterConstants.SERVICE_STATUS_MONITOR_STATUS_NAME,
                    serviceFilterToApply.getMonitorStatus().trim());
        }
        // Filter - 2 For propertyFilter
        Property serviceProperty = serviceFilterToApply.getProperty();

        if (serviceProperty != null) {
            // will never be null due to XML constraints however still check

            if (serviceProperty.getName().equalsIgnoreCase(
                    FilterConstants.IS_ACKNOWLEDGED)) {
                setIsAcknowledgedFilter(
                        FilterConstants.EVENT_SERVICE_STATUS_PROPERTYVALUES_NAME,
                        FilterConstants.IS_PROBLEM_ACKNOWLEDGED,
                        FilterConstants.EVENT_SERVICE_STATUS_PROPERTYVALUES_VALUEBOOLEAN,
                        serviceProperty.getValue());
                setPropertyFilter(getIsAcknowledgedFilter());
            } else {

                setServiceTimeFilter(serviceProperty.getName(), serviceProperty
                        .getValue(),
                        FilterConstants.SERVICE_STATUS_LAST_STATE_CHANGE);
                setPropertyFilter(getTimeFilter());
            }
        }

        /*
         * If Monitor Status or Property Filter is null then return valid of the
         * 2 filters accordingly. Else AND both property and monitor filters and
         * return it.
         */
        if (this.monitorStatusFilter == null) {
            this.logger
                    .debug("getEventServiceFilter(): Monitor-status filter is null.");

            if (this.propertyFilter == null) {
                this.logger
                        .debug("getEventServiceFilter(): Both monitor-status filter & property filter are null.");
                return null;
            }
            // Return Property Filter
            return this.propertyFilter;
        }
        if (this.propertyFilter == null) {
            this.logger
                    .debug("getEventServiceFilter(): Property filter is null.");
            // Return Monitor Status Filter
            return this.monitorStatusFilter;
        }
        // Apply both filters
        return Filter.AND(this.monitorStatusFilter, this.propertyFilter);
    }

    // NOTE: No filters for service sub-page

    // FILTER METHODS
    /**
     * Returns the monitorStatusFilter.
     * 
     * @return the monitorStatusFilter
     */
    public Filter getMonitorStatusFilter() {
        return this.monitorStatusFilter;
    }

    /**
     * Sets the monitorStatusFilter.
     * 
     * @param monitorStatusFilter
     *            the monitorStatusFilter to set
     */
    public void setMonitorStatusFilter(Filter monitorStatusFilter) {
        this.monitorStatusFilter = monitorStatusFilter;
    }

    /**
     * Sets the monitorStatusFilter.
     * 
     * @param queryString
     * @param monitorStatus
     * 
     */
    public void setMonitorStatusFilter(String queryString, String monitorStatus) {
        this.monitorStatusFilter = new Filter(queryString, FilterOperator.EQ,
                monitorStatus);
    }

    /**
     * Returns the isAcknowledgedFilter.
     * 
     * @return the isAcknowledgedFilter
     */
    public Filter getIsAcknowledgedFilter() {
        return this.isAcknowledgedFilter;
    }

    /**
     * Sets the isAcknowledgedFilter.
     * 
     * @param isAcknowledgedFilter
     *            the isAcknowledgedFilter to set
     */
    public void setIsAcknowledgedFilter(Filter isAcknowledgedFilter) {
        this.isAcknowledgedFilter = isAcknowledgedFilter;
    }

    /**
     * 
     * Method returns isAcknowledged filter for given parameters
     * 
     * @param leftQueryString
     * @param leftVal
     * @param rightQueryString
     * @param rightVal
     * 
     */
    private void setIsAcknowledgedFilter(String leftQueryString,
            String leftVal, String rightQueryString, String rightVal) {

        Filter leftFilter, rightFilter;

        leftFilter = new Filter(leftQueryString, FilterOperator.EQ, leftVal);
        rightFilter = new Filter(rightQueryString, FilterOperator.EQ, Boolean
                .parseBoolean(rightVal));
        this.isAcknowledgedFilter = Filter.AND(leftFilter, rightFilter);
    }

    /**
     * Returns the timeFilter.
     * 
     * @return the timeFilter
     */
    public Filter getTimeFilter() {
        return this.timeFilter;
    }

    /**
     * Sets the timeFilter.
     * 
     * @param timeFilter
     *            the timeFilter to set
     */
    public void setTimeFilter(Filter timeFilter) {
        this.timeFilter = timeFilter;
    }

    /**
     * 
     * Method returns time filter for given parameters
     * 
     * @param time
     * @param monitorStatus
     * @param leftQueryString
     * @param rightQueryString
     */
    private void setServiceTimeFilter(String timeStatus, String timeValue,
            String queryString) {

        // Decide the filter operator to apply
        FilterOperator timeOpr;
        long timeValueLong = Long.parseLong(timeValue);
        // For 'more than' has convention '-' i.e. negative values
        if (timeValueLong < 0) {
            timeOpr = FilterOperator.GE;
            timeValueLong = -timeValueLong;
        } else {
            timeOpr = FilterOperator.LE;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(calendar.getTimeInMillis() - timeValueLong);
        Date timeValueinLong = calendar.getTime();
        Filter filter = new Filter(queryString, timeOpr, timeValueinLong);
        this.timeFilter = filter;
    }

    /**
     * 
     * Method returns time filter for given parameters
     * 
     * @param time
     * @param monitorStatus
     * @param leftQueryString
     * @param rightQueryString
     */
    private void setHostOrHGTimeFilter(String timeStatus, String timeValue,
            String leftQueryString, String rightQueryString) {

        Filter leftFilter, rightFilter;

        // Decide the filter operator to apply
        FilterOperator timeOpr;
        long timeValueLong = Long.parseLong(timeValue);
        // For 'more than' has convention '-' i.e. negative values
        if (Long.parseLong(timeValue) < 0) {
            timeOpr = FilterOperator.GE;
            timeValueLong = -timeValueLong;
        } else {
            timeOpr = FilterOperator.LE;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(calendar.getTimeInMillis() - timeValueLong);
        Date timeValueinDate = calendar.getTime();
        leftFilter = new Filter(leftQueryString, FilterOperator.EQ, timeStatus);
        rightFilter = new Filter(rightQueryString, timeOpr, timeValueinDate);
        this.timeFilter = Filter.AND(leftFilter, rightFilter);

    }

    /**
     * 
     * Method returns time filter for given parameters
     * 
     * @param time
     * @param monitorStatus
     * @param leftQueryString
     * @param rightQueryString
     */
    @SuppressWarnings("unused")
    private void setTimeFilter(String timeStatus, String timeValue,
            String leftQueryString, String rightQueryString) {

        Filter leftFilter, rightFilter;

        // Decide the filter operator to apply
        FilterOperator timeOpr;

        // For 'more than' has convention '-' i.e. negative values
        if (Long.parseLong(timeValue) < 0) {
            timeOpr = FilterOperator.LE;
        } else {
            timeOpr = FilterOperator.GE;
        }

        leftFilter = new Filter(leftQueryString, FilterOperator.EQ, timeStatus);
        rightFilter = new Filter(rightQueryString, timeOpr, Long
                .parseLong(timeValue));
        this.timeFilter = Filter.AND(leftFilter, rightFilter);
    }

    /**
     * Returns the propertyFilter.
     * 
     * @return the propertyFilter
     */
    public Filter getPropertyFilter() {
        return this.propertyFilter;
    }

    /**
     * Sets the propertyFilter.
     * 
     * @param propertyFilter
     *            the propertyFilter to set
     */
    public void setPropertyFilter(Filter propertyFilter) {
        this.propertyFilter = propertyFilter;
    }
}
