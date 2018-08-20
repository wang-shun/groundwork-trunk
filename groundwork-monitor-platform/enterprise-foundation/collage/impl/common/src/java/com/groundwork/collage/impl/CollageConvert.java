/*
 * Collage - The ultimate data integration framework. Copyright (C) 2004-2007
 * GroundWork Open Source Solutions info@groundworkopensource.com
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published by the
 * Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 */
package com.groundwork.collage.impl;

import com.groundwork.collage.exception.CollageException;
import com.groundwork.collage.model.Action;
import com.groundwork.collage.model.*;
import com.groundwork.collage.model.ApplicationType;
import com.groundwork.collage.model.AttributeData;
import com.groundwork.collage.model.Category;
import com.groundwork.collage.model.CategoryEntity;
import com.groundwork.collage.model.CheckType;
import com.groundwork.collage.model.Component;
import com.groundwork.collage.model.Device;
import com.groundwork.collage.model.EntityType;
import com.groundwork.collage.model.Host;
import com.groundwork.collage.model.HostGroup;
import com.groundwork.collage.model.HostStatus;
import com.groundwork.collage.model.MonitorStatus;
import com.groundwork.collage.model.OperationStatus;
import com.groundwork.collage.model.Priority;
import com.groundwork.collage.model.ServiceStatus;
import com.groundwork.collage.model.Severity;
import com.groundwork.collage.model.StateTransition;
import com.groundwork.collage.model.StateType;
import com.groundwork.collage.model.TypeRule;
import com.groundwork.collage.model.impl.ActionPerform;
import com.groundwork.collage.model.impl.ActionReturn;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.MatchType;
import org.groundwork.foundation.dao.SortCriteria;
import org.groundwork.foundation.ws.model.impl.*;

import java.text.DecimalFormat;
import java.util.*;

/**
 * Set of conversion methods to convert from foundation object instances to
 * client (WebService) instances.
 * 
 * CollageConvert is not part of a transaction and is assuming all data needed
 * has been retrieved prior to conversion.
 */
public final class CollageConvert {
    protected final static String APP_TYPE_NAGIOS = "NAGIOS";

    private static Log log = LogFactory.getLog(CollageConvert.class);

    public  CollageConvert() {
    }

    /** ********************************************************************** */
    /***************************************************************************
     * Device Conversions /
     **************************************************************************/

    /*
     * Convert Hibernate Object Device to WebService Object Device
     * 
     * TODO: Properties attached to Host are not supported yet. Need to be added
     */
    public org.groundwork.foundation.ws.model.impl.Device[] convertDevice(
            Collection<com.groundwork.collage.model.Device> collection) {
        if (collection == null || collection.size() == 0)
            return null;

        org.groundwork.foundation.ws.model.impl.Device[] deviceArray = new org.groundwork.foundation.ws.model.impl.Device[collection
                .size()];

        try {
            // Iterate over objects in collection
            Iterator<com.groundwork.collage.model.Device> itDevices = collection
                    .iterator();
            com.groundwork.collage.model.Device hibernateObject = null;
            int ii = 0;

            while (itDevices.hasNext()) {
                hibernateObject = itDevices.next();

                if (hibernateObject == null)
                    continue;

                deviceArray[ii] = convert(hibernateObject);
                ii++;
            }

        } catch (Exception e) {
            log.error("Exception in convertDevice() mapping method. Error: "
                    + e);
            return null;
        }

        return deviceArray;
    }

    public org.groundwork.foundation.ws.model.impl.Device convert(
            com.groundwork.collage.model.Device hibernateObject) {
        if (hibernateObject == null)
            return null;

        org.groundwork.foundation.ws.model.impl.Device wsObject = new org.groundwork.foundation.ws.model.impl.Device();

        /*
         * Set the members. No properties TODO: Read the properties out of the
         * "Property Bag" PropertyEntities for the EntityType DEVICE
         */
        wsObject.setDeviceID(hibernateObject.getDeviceId());
        wsObject.setName(hibernateObject.getDisplayName());
        wsObject.setIdentification(hibernateObject.getIdentification());

        return wsObject;
    }

    /** ********************************************************************** */
    /***************************************************************************
     * Host Conversions /
     **************************************************************************/

    /*
     * Convert Hibernate Object Host to WebService Object Host
     * 
     * TODO: Properties attached to Host are not supported yet. Need to be added
     */
    public org.groundwork.foundation.ws.model.impl.Host[] convertHost(
            Collection<com.groundwork.collage.model.Host> collection) {
        if (collection == null || collection.size() == 0)
            return null;

        org.groundwork.foundation.ws.model.impl.Host[] hostArray = new org.groundwork.foundation.ws.model.impl.Host[collection
                .size()];

        // Iterate over objects in collection
        com.groundwork.collage.model.Host hibernateObject = null;

        Iterator<com.groundwork.collage.model.Host> itHost = collection
                .iterator();
        int ii = 0;

        while (itHost.hasNext()) {
            hibernateObject = itHost.next();

            if (hibernateObject == null)
                continue;

            hostArray[ii] = convert(hibernateObject);
            ii++;
        }

        return hostArray;
    }

    /*
     * Convert Hibernate Object Host to WebService Object Host
     * 
     * TODO: Properties attached to Host are not supported yet. Need to be added
     */
    public org.groundwork.foundation.ws.model.impl.Host[] convertHost(
            Collection<com.groundwork.collage.model.Host> collection,
            boolean deep) {
        {
            if (collection == null || collection.size() == 0)
                return null;

            org.groundwork.foundation.ws.model.impl.Host[] hostArray = new org.groundwork.foundation.ws.model.impl.Host[collection
                    .size()];

            // Iterate over objects in collection
            com.groundwork.collage.model.Host hibernateObject = null;

            Iterator<com.groundwork.collage.model.Host> itHost = collection
                    .iterator();
            int ii = 0;

            while (itHost.hasNext()) {
                hibernateObject = itHost.next();

                if (hibernateObject == null)
                    continue;

                hostArray[ii] = convert(hibernateObject, deep);
                ii++;
            }

            return hostArray;
        }
    }

    public org.groundwork.foundation.ws.model.impl.Host convert(
            com.groundwork.collage.model.Host hibernateObject) {
        if (hibernateObject == null)
            return null;

        com.groundwork.collage.model.Device hibernateDevice = null;
        com.groundwork.collage.model.HostStatus hibernateStatus = null;
        com.groundwork.collage.model.MonitorStatus hibernateMonitorStatus = null;
        org.groundwork.foundation.ws.model.impl.Host wsObject = new org.groundwork.foundation.ws.model.impl.Host();
        Set<HostGroup> hibernateHostGroup = null;
        /*
         * Set the members. No properties TODO: Read the properties out of the
         * "Property Bag" PropertyEntities for the EntityType HOST
         */

        wsObject.setApplicationTypeID(hibernateObject.getApplicationTypeId());
        wsObject.setHostID(hibernateObject.getHostId().intValue());
        wsObject.setName(hibernateObject.getHostName());

        hibernateDevice = hibernateObject.getDevice();
        if (hibernateDevice != null) {
            wsObject
                    .setDevice(new org.groundwork.foundation.ws.model.impl.Device(
                            hibernateDevice.getDeviceId().intValue(),
                            hibernateDevice.getDisplayName(), hibernateDevice
                                    .getIdentification()));
        } else {
            wsObject.setDevice(null);
        }

        hibernateStatus = hibernateObject.getHostStatus();
        if (hibernateStatus != null) {
            hibernateMonitorStatus = hibernateStatus.getHostMonitorStatus();

            if (hibernateMonitorStatus != null) {
                // Set Monitor Status
                wsObject.setMonitorStatus(convert(hibernateMonitorStatus));
            }

            wsObject.setLastCheckTime(hibernateStatus.getLastCheckTime());
            wsObject.setNextCheckTime(hibernateStatus.getNextCheckTime());

            // Convert Host Status Dynamic Properties
            wsObject.setPropertyTypeBinding(convert(
                    (PropertyExtensible) hibernateStatus, true));
        }

        wsObject.setStateType(convert(hibernateStatus.getStateType()));

        wsObject.setCheckType(convert(hibernateStatus.getCheckType()));

        hibernateHostGroup = hibernateObject.getHostGroups();

        // Add logic for service availability here.JIRA 104.
        Set<ServiceStatus> serviceStatuses = hibernateObject
                .getServiceStatuses();
        Iterator<ServiceStatus> iter = serviceStatuses.iterator();
        double count = 0;
        while (iter.hasNext()) {
            String status = iter.next().getMonitorStatus().getName();
            if (!"OK".equalsIgnoreCase(status)) {
                count++;
            } // end if
        } // end while
        // Calculate only if there are services
        if (count > 0.0) {
            DecimalFormat formater = new DecimalFormat("##.##");
            String serviceAvailability = formater.format(count
                    / serviceStatuses.size() * 100);
            wsObject.setServiceAvailability(Double
                    .parseDouble(serviceAvailability));
        } else {
            wsObject.setServiceAvailability(Double.parseDouble("0.0"));
        } // end if
        // wsObject.setHostGroups(convert(hibernateHostGroup, false));

        return wsObject;
    }

    public org.groundwork.foundation.ws.model.impl.Host convert(
            com.groundwork.collage.model.Host hibernateObject, boolean deep) {
        if (hibernateObject == null)
            return null;

        com.groundwork.collage.model.Device hibernateDevice = null;
        com.groundwork.collage.model.HostStatus hibernateStatus = null;
        com.groundwork.collage.model.MonitorStatus hibernateMonitorStatus = null;
        org.groundwork.foundation.ws.model.impl.Host wsObject = new org.groundwork.foundation.ws.model.impl.Host();

        /*
         * Set the members. No properties TODO: Read the properties out of the
         * "Property Bag" PropertyEntities for the EntityType HOST
         */

        wsObject.setApplicationTypeID(hibernateObject.getApplicationTypeId());
        wsObject.setHostID(hibernateObject.getHostId().intValue());
        wsObject.setName(hibernateObject.getHostName());

        hibernateDevice = hibernateObject.getDevice();
        if (hibernateDevice != null) {
            wsObject
                    .setDevice(new org.groundwork.foundation.ws.model.impl.Device(
                            hibernateDevice.getDeviceId().intValue(),
                            hibernateDevice.getDisplayName(), hibernateDevice
                                    .getIdentification()));
        } else {
            wsObject.setDevice(null);
        }

        hibernateStatus = hibernateObject.getHostStatus();
        if (hibernateStatus != null) {
            hibernateMonitorStatus = hibernateStatus.getHostMonitorStatus();

            if (hibernateMonitorStatus != null) {
                // Set Monitor Status
                wsObject.setMonitorStatus(convert(hibernateMonitorStatus));
            }

            wsObject.setLastCheckTime(hibernateStatus.getLastCheckTime());
            wsObject.setNextCheckTime(hibernateStatus.getNextCheckTime());

            // Convert Host Status Dynamic Properties
            wsObject.setPropertyTypeBinding(convert(
                    (PropertyExtensible) hibernateStatus, true));
        }

        wsObject.setStateType(convert(hibernateStatus.getStateType()));
        wsObject.setCheckType(convert(hibernateStatus.getCheckType()));

        // Add logic for service availability here.JIRA 104.
        Set<ServiceStatus> serviceStatuses = hibernateObject
                .getServiceStatuses();
        String serviceAvailability;
        if (serviceStatuses != null && serviceStatuses.size() == 0) {
            serviceAvailability = "0";
        } else {
            Iterator<ServiceStatus> iter = serviceStatuses.iterator();
            int count = 0;
            while (iter.hasNext()) {
                String status = iter.next().getMonitorStatus().getName();
                if (!"OK".equalsIgnoreCase(status)) {
                    count++;
                } // end if
            } // end while
            DecimalFormat formater = new DecimalFormat("##.##");
            serviceAvailability = formater.format(count
                    / serviceStatuses.size() * 100);
        }
        wsObject
                .setServiceAvailability(Double.parseDouble(serviceAvailability));
        if (deep) {
            /*
             * Set<HostGroup> hibernateHostGroup = null; hibernateHostGroup =
             * hibernateObject.getHostGroups();
             * wsObject.setHostGroups(convert(hibernateHostGroup, false));
             */
        }

        return wsObject;
    }

    public org.groundwork.foundation.ws.model.impl.HostStatus convert(
            HostStatus hibernateObject) {
        if (hibernateObject == null)
            return null;

        return new org.groundwork.foundation.ws.model.impl.HostStatus(
                hibernateObject.getHostStatusId(), hibernateObject
                        .getApplicationType().getApplicationTypeId(),
                hibernateObject.getLastCheckTime(), convert(hibernateObject
                        .getStateType()), convert(
                        (PropertyExtensible) hibernateObject, true));
    }

    /** ********************************************************************** */
    /***************************************************************************
     * Host Group Conversions /
     **************************************************************************/

    /*
     * Below the conversion methods (Hibernate -> Web Service )
     */
    public org.groundwork.foundation.ws.model.impl.HostGroup[] convertHostGroup(
            Collection<com.groundwork.collage.model.HostGroup> collection,
            boolean deep) {
        if (collection == null || collection.size() == 0)
            return null;

        /* Result set */
        org.groundwork.foundation.ws.model.impl.HostGroup[] hostGroupArray = new org.groundwork.foundation.ws.model.impl.HostGroup[collection
                .size()];

        Iterator<com.groundwork.collage.model.HostGroup> itHostGroup = collection
                .iterator();

        int ii = 0;

        // Iterate over objects in collection
        com.groundwork.collage.model.HostGroup hibernateObject = null;
        org.groundwork.foundation.ws.model.impl.HostGroup wsObject = null;
        Collection hosts = null;

        while (itHostGroup.hasNext()) {
            hibernateObject = itHostGroup.next();

            if (hibernateObject == null)
                continue;

            wsObject = new org.groundwork.foundation.ws.model.impl.HostGroup();

            /*
             * Set the host group members
             */
            ApplicationType appType = hibernateObject.getApplicationType();

            wsObject.setApplicationTypeID((appType == null) ? -1 : appType
                    .getApplicationTypeId().intValue());
            wsObject.setApplicationName((appType == null) ? null : appType
                    .getName());
            wsObject
                    .setHostGroupID(hibernateObject.getHostGroupId().intValue());
            wsObject.setName(hibernateObject.getName());
            wsObject.setDescription(hibernateObject.getDescription());

            /* Alias newly added for 6.0 */
            wsObject.setAlias(hibernateObject.getAlias());

            // We need to key of this flag otherwise if getHosts() is called
            // then the will automatically
            // be lazy-loaded.
            if (deep == true) {
                hosts = hibernateObject.getHosts();
                if (hosts != null && hosts.size() > 0) {
                    /* Convert and Assign it to HostGroup object */
                    wsObject.setHosts(convertHost((Collection<Host>) hosts));
                }
            } else {
                hosts = hibernateObject.getHosts();
                org.groundwork.foundation.ws.model.impl.Host[] newHosts = new org.groundwork.foundation.ws.model.impl.Host[hosts
                        .size()];
                if (hosts != null && hosts.size() > 0) {
                    Iterator<Host> iter = hosts.iterator();
                    int i = 0;
                    while (iter.hasNext()) {
                        org.groundwork.foundation.ws.model.impl.Host host = new org.groundwork.foundation.ws.model.impl.Host();
                        host.setHostID(iter.next().getHostId().intValue());
                        newHosts[i] = host;
                        i++;
                    }
                }
                wsObject.setHosts(newHosts);
            }

            /* Assign it to result set */
            hostGroupArray[ii] = wsObject;
            ii++;
        }

        return hostGroupArray;
    }

    /** ********************************************************************** */
    /***************************************************************************
     * Log Message Status Conversions /
     **************************************************************************/
    public org.groundwork.foundation.ws.model.impl.LogMessage[] convertLogMessage(
            Collection<com.groundwork.collage.model.LogMessage> collection) {
        if (collection == null || collection.size() == 0)
            return null;

        org.groundwork.foundation.ws.model.impl.LogMessage[] logMsgArray = new org.groundwork.foundation.ws.model.impl.LogMessage[collection
                .size()];

        // Iterate over objects in collection
        com.groundwork.collage.model.LogMessage hibernateObject = null;
        Iterator<com.groundwork.collage.model.LogMessage> itLogMsg = collection
                .iterator();
        int ii = 0;

        while (itLogMsg.hasNext()) {
            hibernateObject = itLogMsg.next();

            if (hibernateObject == null)
                continue;

            logMsgArray[ii] = convert(hibernateObject);
            ii++;
        }

        return logMsgArray;
    }

    public org.groundwork.foundation.ws.model.impl.LogMessage convert(
            com.groundwork.collage.model.LogMessage hibernateObject) {
        if (hibernateObject == null)
            return null;

        org.groundwork.foundation.ws.model.impl.LogMessage wsObject = new org.groundwork.foundation.ws.model.impl.LogMessage();

        ApplicationType appType = hibernateObject.getApplicationType();

        wsObject.setApplicationTypeID((appType == null) ? -1 : appType
                .getApplicationTypeId().intValue());
        wsObject.setApplicationName((appType == null) ? null : appType
                .getName());
        wsObject.setLogMessageID(hibernateObject.getLogMessageId().intValue());
        wsObject.setTextMessage(hibernateObject.getTextMessage());
        wsObject.setMessageCount(hibernateObject.getMsgCount().intValue());
        wsObject.setFirstInsertDate(hibernateObject.getFirstInsertDate());
        wsObject.setLastInsertDate(hibernateObject.getLastInsertDate());
        wsObject.setReportDate(hibernateObject.getReportDate());

        OperationStatus opStatus = hibernateObject.getOperationStatus();
        if (opStatus != null) {
            wsObject.setOperationStatus(convert(opStatus));
        }

        MonitorStatus monStatus = hibernateObject.getMonitorStatus();
        if (monStatus != null) {
            wsObject.setMonitorStatus(convert(monStatus));
        }

        Device device = hibernateObject.getDevice();
        if (device != null) {
            wsObject.setDevice(convert(device));
        }

        Severity severity = hibernateObject.getSeverity();
        if (severity != null) {
            wsObject.setSeverity(convert(severity));
        }

        TypeRule typeRule = hibernateObject.getTypeRule();
        if (typeRule != null) {
            wsObject.setTypeRule(convert(typeRule));
        }

        Priority priority = hibernateObject.getPriority();
        if (priority != null) {
            wsObject.setPriority(convert(priority));
        }

        Component component = hibernateObject.getComponent();
        if (component != null) {
            wsObject.setComponent(convert(component));
        }

        HostStatus hostStatus = hibernateObject.getHostStatus();
        if (hostStatus != null && hostStatus.getHost() != null) {
            wsObject.setHost(convert(hostStatus.getHost()));
        }
        
        ServiceStatus serviceStatus = hibernateObject.getServiceStatus();
        if (serviceStatus != null) {
            wsObject.setServiceStatus(convert(serviceStatus));
        }


        // Convert dynamic properties
        wsObject.setPropertyTypeBinding(convert(
                (PropertyExtensible) hibernateObject, true));

        return wsObject;
    }

    /** ********************************************************************** */
    /***************************************************************************
     * Service Status Conversions /
     **************************************************************************/

    public org.groundwork.foundation.ws.model.impl.ServiceStatus convert(
            com.groundwork.collage.model.ServiceStatus hibernateObject) {
        if (hibernateObject == null)
            return null;

        org.groundwork.foundation.ws.model.impl.ServiceStatus wsObject = null;
        com.groundwork.collage.model.ApplicationType appType = hibernateObject
                .getApplicationType();

        wsObject = new org.groundwork.foundation.ws.model.impl.ServiceStatus();

        wsObject.setServiceStatusID(hibernateObject.getServiceStatusId()
                .intValue());
        wsObject
                .setApplicationTypeID(appType.getApplicationTypeId().intValue());
        wsObject.setDescription(hibernateObject.getServiceDescription());
        wsObject.setLastCheckTime(hibernateObject.getLastCheckTime());
        wsObject.setNextCheckTime(hibernateObject.getNextCheckTime());
        wsObject.setLastStateChange(hibernateObject.getLastStateChange());

        // Convert Monitor Status
        com.groundwork.collage.model.MonitorStatus hibernateMonitorStatus = hibernateObject
                .getMonitorStatus();
        if (hibernateMonitorStatus != null) {
            wsObject.setMonitorStatus(convert(hibernateMonitorStatus));
        }

        // Convert Host
        com.groundwork.collage.model.Host hibernateHost = hibernateObject
                .getHost();
        if (hibernateHost != null) {
            wsObject.setHost(convert(hibernateHost));
        }

        /* Built-in properies */

        String metricType = hibernateObject.getMetricType();
        if (metricType != null && metricType.length() > 0)
            wsObject.setMetricType(metricType);

        String domain = hibernateObject.getDomain();
        if (domain != null && domain.length() > 0)
            wsObject.setDomain(domain);

        StateType stateType = hibernateObject.getStateType();
        if (stateType != null)
            wsObject.setStateType(convert(stateType));

        CheckType checkType = hibernateObject.getCheckType();
        if (checkType != null)
            wsObject.setCheckType(convert(checkType));

        MonitorStatus lastHardState = hibernateObject.getLastHardState();
        if (lastHardState != null)
            wsObject.setLastHardState(convert(lastHardState));

        // Assign properties to Object
        wsObject.setPropertyTypeBinding(convert(
                (PropertyExtensible) hibernateObject, true));

        return wsObject;
    }

    /** ********************************************************************** */
    /***************************************************************************
     * Service Status Conversions /
     **************************************************************************/

    public org.groundwork.foundation.ws.model.impl.ServiceStatus convert(
            com.groundwork.collage.model.ServiceStatus hibernateObject,
            boolean deep) {
        if (hibernateObject == null)
            return null;

        org.groundwork.foundation.ws.model.impl.ServiceStatus wsObject = null;
        com.groundwork.collage.model.ApplicationType appType = hibernateObject
                .getApplicationType();

        wsObject = new org.groundwork.foundation.ws.model.impl.ServiceStatus();

        wsObject.setServiceStatusID(hibernateObject.getServiceStatusId()
                .intValue());
        wsObject
                .setApplicationTypeID(appType.getApplicationTypeId().intValue());
        wsObject.setDescription(hibernateObject.getServiceDescription());
        wsObject.setLastCheckTime(hibernateObject.getLastCheckTime());
        wsObject.setNextCheckTime(hibernateObject.getNextCheckTime());
        wsObject.setLastStateChange(hibernateObject.getLastStateChange());

        // Convert Monitor Status
        com.groundwork.collage.model.MonitorStatus hibernateMonitorStatus = hibernateObject
                .getMonitorStatus();
        if (hibernateMonitorStatus != null) {
            wsObject.setMonitorStatus(convert(hibernateMonitorStatus));
        }

        // Convert Host
        com.groundwork.collage.model.Host hibernateHost = hibernateObject
                .getHost();
        if (hibernateHost != null) { // always make the deep false for Host
            wsObject.setHost(convert(hibernateHost, false));
        }

        /* Built-in properies */

        String metricType = hibernateObject.getMetricType();
        if (metricType != null && metricType.length() > 0)
            wsObject.setMetricType(metricType);

        String domain = hibernateObject.getDomain();
        if (domain != null && domain.length() > 0)
            wsObject.setDomain(domain);

        StateType stateType = hibernateObject.getStateType();
        if (stateType != null)
            wsObject.setStateType(convert(stateType));

        CheckType checkType = hibernateObject.getCheckType();
        if (checkType != null)
            wsObject.setCheckType(convert(checkType));

        MonitorStatus lastHardState = hibernateObject.getLastHardState();
        if (lastHardState != null)
            wsObject.setLastHardState(convert(lastHardState));

        // Assign properties to Object
        wsObject.setPropertyTypeBinding(convert(
                (PropertyExtensible) hibernateObject, true));

        return wsObject;
    }

    /*
     * Below the conversion methods (Hibernate -> Web Service )
     */
    public org.groundwork.foundation.ws.model.impl.ServiceStatus[] convertServiceStatus(
            Collection<com.groundwork.collage.model.ServiceStatus> collection) {
        if (collection == null || collection.size() == 0)
            return null;

        org.groundwork.foundation.ws.model.impl.ServiceStatus[] serviceArray = new org.groundwork.foundation.ws.model.impl.ServiceStatus[collection
                .size()];

        // Iterate over objects in collection
        com.groundwork.collage.model.ServiceStatus hibernateObject = null;
        Iterator<com.groundwork.collage.model.ServiceStatus> itService = collection
                .iterator();
        int ii = 0;

        while (itService.hasNext()) {
            hibernateObject = itService.next();

            if (hibernateObject == null)
                continue;

            serviceArray[ii] = convert(hibernateObject);
            ii++;
        }

        return serviceArray;
    }

    /*
     * Below the conversion methods (Hibernate -> Web Service )
     */
    public org.groundwork.foundation.ws.model.impl.ServiceStatus[] convertServiceStatus(
            Collection<com.groundwork.collage.model.ServiceStatus> collection,
            boolean deep) {
        if (collection == null || collection.size() == 0)
            return null;

        org.groundwork.foundation.ws.model.impl.ServiceStatus[] serviceArray = new org.groundwork.foundation.ws.model.impl.ServiceStatus[collection
                .size()];

        // Iterate over objects in collection
        com.groundwork.collage.model.ServiceStatus hibernateObject = null;
        Iterator<com.groundwork.collage.model.ServiceStatus> itService = collection
                .iterator();
        int ii = 0;

        while (itService.hasNext()) {
            hibernateObject = itService.next();

            if (hibernateObject == null)
                continue;

            serviceArray[ii] = convert(hibernateObject, deep);
            ii++;
        }

        return serviceArray;
    }

    /** ********************************************************************** */
    /***************************************************************************
     * Statistic Conversions /
     **************************************************************************/

    public org.groundwork.foundation.ws.model.impl.StateStatistics[] convertStatistics(
            Collection<com.groundwork.collage.model.impl.StateStatistics> collection) {
        if (collection == null || collection.size() == 0) {
            return null;
        }

        org.groundwork.foundation.ws.model.impl.StateStatistics[] statisticsArray = new org.groundwork.foundation.ws.model.impl.StateStatistics[collection
                .size()];

        // Iterate over objects in collection
        com.groundwork.collage.model.impl.StateStatistics hibernateObject = null;

        Iterator<com.groundwork.collage.model.impl.StateStatistics> it = collection
                .iterator();
        int ii = 0;

        while (it.hasNext()) {
            hibernateObject = it.next();

            if (hibernateObject == null)
                continue;

            statisticsArray[ii] = convert(hibernateObject);
            ii++;
        }

        return statisticsArray;
    }

    public org.groundwork.foundation.ws.model.impl.StateStatistics convert(
            com.groundwork.collage.model.impl.StateStatistics hibernateObject) {
        if (hibernateObject == null)
            return null;

        org.groundwork.foundation.ws.model.impl.StateStatistics wsObject = new org.groundwork.foundation.ws.model.impl.StateStatistics();

        wsObject.setName(hibernateObject.getHostGroupName());
        wsObject.setTotalHosts(hibernateObject.getTotalHosts());
        wsObject.setTotalServices(hibernateObject.getTotalServices());

        // Convert Statistic Property instances
        wsObject.setStatisticProperties(convertStatisticProperties(hibernateObject
                .getStatisticProperties()));

        return wsObject;
    }

    public org.groundwork.foundation.ws.model.impl.StatisticProperty[] convertStatisticProperties(
            Collection<com.groundwork.collage.model.impl.StatisticProperty> collection) {
        if (collection == null || collection.size() == 0) {
            return null;
        }

        org.groundwork.foundation.ws.model.impl.StatisticProperty[] statisticsArray = new org.groundwork.foundation.ws.model.impl.StatisticProperty[collection
                .size()];

        // Iterate over objects in collection
        com.groundwork.collage.model.impl.StatisticProperty hibernateObject = null;
        org.groundwork.foundation.ws.model.impl.StatisticProperty wsObject = null;

        Iterator<com.groundwork.collage.model.impl.StatisticProperty> it = collection
                .iterator();
        int ii = 0;

        while (it.hasNext()) {
            hibernateObject = it.next();

            if (hibernateObject == null)
                continue;

            wsObject = new org.groundwork.foundation.ws.model.impl.StatisticProperty();

            wsObject.setCount(hibernateObject.getCount());
            wsObject.setName(hibernateObject.getName());

            statisticsArray[ii] = wsObject;
            ii++;
        }

        return statisticsArray;
    }

    public org.groundwork.foundation.ws.model.impl.NagiosStatisticProperty[] convertNagiosStatProps(
            Collection<com.groundwork.collage.model.impl.NagiosStatisticProperty> collection) {
        if (collection == null || collection.size() == 0) {
            return null;
        }

        org.groundwork.foundation.ws.model.impl.NagiosStatisticProperty[] statisticsArray = new org.groundwork.foundation.ws.model.impl.NagiosStatisticProperty[collection
                .size()];

        // Iterate over objects in collection
        com.groundwork.collage.model.impl.NagiosStatisticProperty hibernateObject = null;
        org.groundwork.foundation.ws.model.impl.NagiosStatisticProperty wsObject = null;

        Iterator<com.groundwork.collage.model.impl.NagiosStatisticProperty> it = collection
                .iterator();
        int ii = 0;

        while (it.hasNext()) {
            hibernateObject = it.next();

            if (hibernateObject == null)
                continue;

            wsObject = new org.groundwork.foundation.ws.model.impl.NagiosStatisticProperty();

            wsObject.setHostStatisticDisabled(hibernateObject
                    .getHostStatisticDisabled());
            wsObject.setHostStatisticEnabled(hibernateObject
                    .getHostStatisticEnabled());
            wsObject.setServiceStatisticDisabled(hibernateObject
                    .getServiceStatisticDisabled());
            wsObject.setServiceStatisticEnabled(hibernateObject
                    .getServiceStatisticEnabled());
            wsObject.setPropertyName(hibernateObject.getPropertyName());

            statisticsArray[ii] = wsObject;
            ii++;
        }

        return statisticsArray;
    }

    /**
     * Converts a stateStatistics collection into a collection of
     * HostGroupStatisticProperty instances
     * 
     * @param stateStatistics
     * @return
     */
    public Collection<HostGroupStatisticProperty> convertStateStatistics(
            Collection<com.groundwork.collage.model.impl.StateStatistics> stateStatistics) {
        if (stateStatistics == null) {
            return null;
        }

        Collection<HostGroupStatisticProperty> hgStatList = new ArrayList<HostGroupStatisticProperty>(
                stateStatistics.size());

        Iterator<com.groundwork.collage.model.impl.StateStatistics> it = stateStatistics
                .iterator();
        while (it.hasNext()) {
            com.groundwork.collage.model.impl.StateStatistics stateStatistic = it
                    .next();

            List<com.groundwork.collage.model.impl.StatisticProperty> stats = stateStatistic
                    .getStatisticProperties();
            if (stats == null || stats.size() == 0) {
                continue;
            }

            for (int j = 0; j < stats.size(); j++) {
                com.groundwork.collage.model.impl.StatisticProperty stat = stats
                        .get(j);
                hgStatList.add(new HostGroupStatisticProperty(stateStatistic
                        .getHostGroupName(), stat.getName(), stat.getCount()));
            }
        }

        return hgStatList;
    }

    public org.groundwork.foundation.ws.model.impl.AttributeData[] convertAttributeData(
            Collection<AttributeData> collection) {
        org.groundwork.foundation.ws.model.impl.AttributeData[] attrDataArray = null;

        if (collection != null && collection.size() > 0) {
            attrDataArray = new org.groundwork.foundation.ws.model.impl.AttributeData[collection
                    .size()];

            // Iterate over objects in collection
            com.groundwork.collage.model.AttributeData hibernateObject = null;
            org.groundwork.foundation.ws.model.impl.AttributeData wsObject = null;

            Iterator it = collection.iterator();
            int ii = 0;

            while (it.hasNext()) {
                hibernateObject = (com.groundwork.collage.model.AttributeData) it
                        .next();

                if (hibernateObject != null) {
                    wsObject = new org.groundwork.foundation.ws.model.impl.AttributeData();

                    Integer attributeID = hibernateObject.getID();

                    wsObject.setAttributeID((attributeID == null) ? -1
                            : attributeID.intValue());
                    wsObject.setName(hibernateObject.getName());
                    wsObject.setDescription(hibernateObject.getDescription());

                    attrDataArray[ii] = wsObject;
                    ii++;
                }
            }
        }

        return attrDataArray;
    }

    /** ********************************************************************** */
    /***************************************************************************
     * State Type Conversion /
     **************************************************************************/
    public org.groundwork.foundation.ws.model.impl.StateType convert(
            StateType hibernateObject) {
        // Note: We return null if the id is invalid
        if (hibernateObject == null || hibernateObject.getStateTypeId() == null)
            return null;

        return new org.groundwork.foundation.ws.model.impl.StateType(
                hibernateObject.getStateTypeId().intValue(), hibernateObject
                        .getName(), hibernateObject.getDescription());
    }

    /** ********************************************************************** */
    /***************************************************************************
     * Check Type Conversion /
     **************************************************************************/
    public org.groundwork.foundation.ws.model.impl.CheckType convert(
            CheckType hibernateObject) {
        // Note: We return null if the id is invalid
        if (hibernateObject == null || hibernateObject.getCheckTypeId() == null)
            return null;

        return new org.groundwork.foundation.ws.model.impl.CheckType(
                hibernateObject.getCheckTypeId().intValue(), hibernateObject
                        .getName(), hibernateObject.getDescription());
    }

    /** ********************************************************************** */
    /***************************************************************************
     * Monitor Status Conversion /
     **************************************************************************/
    public org.groundwork.foundation.ws.model.impl.MonitorStatus convert(
            MonitorStatus hibernateObject) {
        // Note: We return null if the id is invalid
        if (hibernateObject == null
                || hibernateObject.getMonitorStatusId() == null)
            return null;

        return new org.groundwork.foundation.ws.model.impl.MonitorStatus(
                hibernateObject.getMonitorStatusId().intValue(),
                hibernateObject.getName(), hibernateObject.getDescription());
    }

    /** ********************************************************************** */
    /***************************************************************************
     * Monitor Status Conversion /
     **************************************************************************/
    public org.groundwork.foundation.ws.model.impl.Severity convert(
            Severity hibernateObject) {
        // Note: We return null if the id is invalid
        if (hibernateObject == null || hibernateObject.getSeverityId() == null)
            return null;

        return new org.groundwork.foundation.ws.model.impl.Severity(
                hibernateObject.getSeverityId().intValue(), hibernateObject
                        .getName(), hibernateObject.getDescription());
    }

    /** ********************************************************************** */
    /***************************************************************************
     * Operation Status Conversion /
     **************************************************************************/
    public org.groundwork.foundation.ws.model.impl.OperationStatus convert(
            OperationStatus hibernateObject) {
        // Note: We return null if the id is invalid
        if (hibernateObject == null
                || hibernateObject.getOperationStatusId() == null)
            return null;

        return new org.groundwork.foundation.ws.model.impl.OperationStatus(
                hibernateObject.getOperationStatusId().intValue(),
                hibernateObject.getName(), hibernateObject.getDescription());
    }

    /** ********************************************************************** */
    /***************************************************************************
     * Type Rule Conversion /
     **************************************************************************/
    public org.groundwork.foundation.ws.model.impl.TypeRule convert(
            TypeRule hibernateObject) {
        // Note: We return null if the id is invalid
        if (hibernateObject == null || hibernateObject.getTypeRuleId() == null)
            return null;

        return new org.groundwork.foundation.ws.model.impl.TypeRule(
                hibernateObject.getTypeRuleId().intValue(), hibernateObject
                        .getName(), hibernateObject.getDescription());
    }

    /** ********************************************************************** */
    /***************************************************************************
     * Priority Conversion /
     **************************************************************************/
    public org.groundwork.foundation.ws.model.impl.Priority convert(
            Priority hibernateObject) {
        // Note: We return null if the id is invalid
        if (hibernateObject == null || hibernateObject.getPriorityId() == null)
            return null;

        return new org.groundwork.foundation.ws.model.impl.Priority(
                hibernateObject.getPriorityId().intValue(), hibernateObject
                        .getName(), hibernateObject.getDescription());
    }

    /** ********************************************************************** */
    /***************************************************************************
     * Operation Status Conversion /
     **************************************************************************/
    public org.groundwork.foundation.ws.model.impl.Component convert(
            Component hibernateObject) {
        // Note: We return null if the id is invalid
        if (hibernateObject == null || hibernateObject.getComponentId() == null)
            return null;

        return new org.groundwork.foundation.ws.model.impl.Component(
                hibernateObject.getComponentId().intValue(), hibernateObject
                        .getName(), hibernateObject.getDescription());
    }

    /** ********************************************************************** */
    /***************************************************************************
     * Application Type Conversion /
     **************************************************************************/

    public org.groundwork.foundation.ws.model.impl.ApplicationType[] convertApplicationType(
            Collection<ApplicationType> collection) {
        org.groundwork.foundation.ws.model.impl.ApplicationType[] appTypeArray = null;

        if (collection != null && collection.size() > 0) {
            appTypeArray = new org.groundwork.foundation.ws.model.impl.ApplicationType[collection
                    .size()];

            // Iterate over objects in collection
            com.groundwork.collage.model.ApplicationType hibernateObject = null;

            Iterator it = collection.iterator();
            int ii = 0;

            while (it.hasNext()) {
                hibernateObject = (com.groundwork.collage.model.ApplicationType) it
                        .next();

                if (hibernateObject != null) {
                    appTypeArray[ii] = convert(hibernateObject);
                    ii++;
                }
            }
        }

        return appTypeArray;
    }

    public org.groundwork.foundation.ws.model.impl.ApplicationType convert(
            ApplicationType hibernateObject) {
        // Note: We return null if the id is invalid
        if (hibernateObject == null
                || hibernateObject.getApplicationTypeId() == null)
            return null;

        return new org.groundwork.foundation.ws.model.impl.ApplicationType(
                hibernateObject.getApplicationTypeId().intValue(),
                hibernateObject.getName(), hibernateObject.getDescription());
    }

    /** ********************************************************************** */
    /***************************************************************************
     * Entity Type Conversion /
     **************************************************************************/

    public org.groundwork.foundation.ws.model.impl.EntityType[] convertEntityType(
            Collection<EntityType> collection) {
        org.groundwork.foundation.ws.model.impl.EntityType[] entityTypeArray = null;

        if (collection != null && collection.size() > 0) {
            entityTypeArray = new org.groundwork.foundation.ws.model.impl.EntityType[collection
                    .size()];

            // Iterate over objects in collection
            com.groundwork.collage.model.EntityType hibernateObject = null;

            Iterator it = collection.iterator();
            int ii = 0;

            while (it.hasNext()) {
                hibernateObject = (com.groundwork.collage.model.EntityType) it
                        .next();

                if (hibernateObject != null) {
                    entityTypeArray[ii] = convert(hibernateObject);
                    ii++;
                }
            }
        }

        return entityTypeArray;
    }

    public org.groundwork.foundation.ws.model.impl.EntityType convert(
            EntityType hibernateObject) {
        // Note: We return null if the id is invalid
        if (hibernateObject == null
                || hibernateObject.getEntityTypeId() == null)
            return null;

        return new org.groundwork.foundation.ws.model.impl.EntityType(
                hibernateObject.getEntityTypeId().intValue(), hibernateObject
                        .getName(), hibernateObject.getDescription());
    }

    /** ********************************************************************** */
    /***************************************************************************
     * Application Entity Property Conversion /
     **************************************************************************/

    public org.groundwork.foundation.ws.model.impl.EntityTypeProperty[] convertEntityProperty(
            Collection<ApplicationEntityProperty> collection) {
        org.groundwork.foundation.ws.model.impl.EntityTypeProperty[] entityPropArray = null;

        if (collection != null && collection.size() > 0) {
            entityPropArray = new org.groundwork.foundation.ws.model.impl.EntityTypeProperty[collection
                    .size()];

            // Iterate over objects in collection
            com.groundwork.collage.model.ApplicationEntityProperty hibernateObject = null;

            Iterator it = collection.iterator();
            int ii = 0;

            while (it.hasNext()) {
                hibernateObject = (com.groundwork.collage.model.ApplicationEntityProperty) it
                        .next();

                if (hibernateObject != null) {
                    entityPropArray[ii] = convert(hibernateObject);
                    ii++;
                }
            }
        }

        return entityPropArray;
    }

    public org.groundwork.foundation.ws.model.impl.EntityTypeProperty convert(
            ApplicationEntityProperty hibernateObject) {
        // Note: We return null if the hibernate object is invalid
        if (hibernateObject == null)
            return null;

        PropertyType propType = hibernateObject.getPropertyType();
        if (propType == null)
            return null;

        return new org.groundwork.foundation.ws.model.impl.EntityTypeProperty(
                convert(hibernateObject.getApplicationType()),
                convert(hibernateObject.getEntityType()), propType
                        .getPropertyTypeId(), propType.getName(), propType
                        .getDescription(), PropertyDataType.fromValue(propType
                        .getPrimitiveType()), propType.getRelatedEntityType());
    }

    /** ********************************************************************** */
    /***************************************************************************
     * Action Conversions /
     **************************************************************************/

    public org.groundwork.foundation.ws.model.impl.Action[] convertAction(
            Collection<Action> collection) {
        org.groundwork.foundation.ws.model.impl.Action[] actionArray = null;

        if (collection != null && collection.size() > 0) {
            actionArray = new org.groundwork.foundation.ws.model.impl.Action[collection
                    .size()];

            // Iterate over objects in collection
            com.groundwork.collage.model.Action hibernateObject = null;

            Iterator it = collection.iterator();
            int ii = 0;

            while (it.hasNext()) {
                hibernateObject = (com.groundwork.collage.model.Action) it
                        .next();

                if (hibernateObject != null) {
                    actionArray[ii] = convert(hibernateObject);
                    ii++;
                }
            }
        }

        return actionArray;
    }

    /** ********************************************************************** */
    /***************************************************************************
     * StateTransition Conversions /
     **************************************************************************/

    public org.groundwork.foundation.ws.model.impl.StateTransition[] convertStateTransition(
            Collection<StateTransition> collection) {
        org.groundwork.foundation.ws.model.impl.StateTransition[] transitionArray = null;

        if (collection != null && collection.size() > 0) {
            transitionArray = new org.groundwork.foundation.ws.model.impl.StateTransition[collection
                    .size()];

            // Iterate over objects in collection
            com.groundwork.collage.model.StateTransition hibernateObject = null;

            Iterator it = collection.iterator();
            int ii = 0;

            while (it.hasNext()) {
                Object backObj = it.next();
                if (backObj instanceof com.groundwork.collage.model.impl.HostStateTransition)
                    hibernateObject = (com.groundwork.collage.model.impl.HostStateTransition) backObj;
                else
                    hibernateObject = (com.groundwork.collage.model.impl.ServiceStateTransition) backObj;

                if (hibernateObject != null) {
                    transitionArray[ii] = convert(hibernateObject);
                    ii++;
                }
            }
        }

        return transitionArray;
    }

    public org.groundwork.foundation.ws.model.impl.StateTransition convert(
            com.groundwork.collage.model.StateTransition hibernateObject) {
        // Note: We return null if the hibernate object is invalid
        if (hibernateObject == null)
            return null;
        if (hibernateObject instanceof com.groundwork.collage.model.impl.HostStateTransition) {
            return new org.groundwork.foundation.ws.model.impl.StateTransition(
                    hibernateObject.getHostName(), convert(hibernateObject
                            .getFromStatus()), hibernateObject
                            .getFromTransitionDate(), convert(hibernateObject
                            .getToStatus()), hibernateObject
                            .getToTransitionDate(), hibernateObject
                            .getEndTransitionDate(), hibernateObject
                            .getDurationInState());
        } else {
            com.groundwork.collage.model.impl.ServiceStateTransition serviceStateObject = (com.groundwork.collage.model.impl.ServiceStateTransition) hibernateObject;
            return new org.groundwork.foundation.ws.model.impl.StateTransition(
                    serviceStateObject.getHostName(), serviceStateObject
                            .getServiceDescription(),
                    convert(serviceStateObject.getFromStatus()),
                    serviceStateObject.getFromTransitionDate(),
                    convert(serviceStateObject.getToStatus()),
                    serviceStateObject.getToTransitionDate(),
                    serviceStateObject.getEndTransitionDate(),
                    serviceStateObject.getDurationInState());
        }
    }

    public org.groundwork.foundation.ws.model.impl.StateTransition convert(
            com.groundwork.collage.model.impl.ServiceStateTransition hibernateObject) {
        // Note: We return null if the hibernate object is invalid
        if (hibernateObject == null)
            return null;

        return new org.groundwork.foundation.ws.model.impl.StateTransition(
                hibernateObject.getHostName(), hibernateObject
                        .getServiceDescription(), convert(hibernateObject
                        .getFromStatus()), hibernateObject
                        .getFromTransitionDate(), convert(hibernateObject
                        .getToStatus()), hibernateObject.getToTransitionDate(),
                hibernateObject.getEndTransitionDate(), hibernateObject
                        .getDurationInState());
    }

    public org.groundwork.foundation.ws.model.impl.Category[] convertCategory(
            Collection<Category> collection) {
        org.groundwork.foundation.ws.model.impl.Category[] categoryArray = null;
        if (collection != null && collection.size() > 0) {
            categoryArray = new org.groundwork.foundation.ws.model.impl.Category[collection
                    .size()];
            // Iterate over objects in collection
            com.groundwork.collage.model.Category hibernateObject = null;
            Iterator it = collection.iterator();
            int ii = 0;
            while (it.hasNext()) {
                hibernateObject = (com.groundwork.collage.model.Category) it
                        .next();
                if (hibernateObject != null) {
                    categoryArray[ii] = convert(hibernateObject);
                    ii++;
                }
            }
        }
        return categoryArray;
    }

    public org.groundwork.foundation.ws.model.impl.Category convert(
            Category hibernateObject) {
        // Note: We return null if the hibernate object is invalid
        if (hibernateObject == null)
            return null;
        return new org.groundwork.foundation.ws.model.impl.Category(
                hibernateObject.getCategoryId().intValue(), hibernateObject
                        .getName(), hibernateObject.getDescription(),
                convert(hibernateObject.getEntityType()), null,
                convertCategoryEntity(hibernateObject.getCategoryEntities()));
    }

    public org.groundwork.foundation.ws.model.impl.CategoryEntity[] convertCategoryEntity(
            Collection<CategoryEntity> collection) {
        org.groundwork.foundation.ws.model.impl.CategoryEntity[] categoryEntityArray = null;
        if (collection != null && collection.size() > 0) {
            categoryEntityArray = new org.groundwork.foundation.ws.model.impl.CategoryEntity[collection
                    .size()];
            // Iterate over objects in collection
            com.groundwork.collage.model.CategoryEntity hibernateObject = null;
            Iterator it = collection.iterator();
            int ii = 0;
            while (it.hasNext()) {
                hibernateObject = (com.groundwork.collage.model.CategoryEntity) it
                        .next();
                if (hibernateObject != null) {
                    categoryEntityArray[ii] = convert(hibernateObject);
                    ii++;
                }
            }
        }
        return categoryEntityArray;
    }

    public org.groundwork.foundation.ws.model.impl.CategoryEntity convert(
            CategoryEntity hibernateObject) {
        // Note: We return null if the hibernate object is invalid
        if (hibernateObject == null)
            return null;
        return new org.groundwork.foundation.ws.model.impl.CategoryEntity(
                hibernateObject.getObjectID().intValue(), hibernateObject
                        .getCategoryEntityID().intValue(),
                convert(hibernateObject.getEntityType()), null);
    }

    public org.groundwork.foundation.ws.model.impl.Action convert(
            Action hibernateObject) {
        // Note: We return null if the hibernate object is invalid
        if (hibernateObject == null)
            return null;

        return new org.groundwork.foundation.ws.model.impl.Action(
                hibernateObject.getActionId().intValue(), hibernateObject
                        .getName(), hibernateObject.getDescription(),
                convertApplicationType((Collection<ApplicationType>) hibernateObject
                        .getApplicationTypes()));
    }

    public org.groundwork.foundation.ws.model.impl.ActionReturn[] convert(
            Collection<ActionReturn> collection) {
        org.groundwork.foundation.ws.model.impl.ActionReturn[] actionReturnArray = null;

        if (collection != null && collection.size() > 0) {
            actionReturnArray = new org.groundwork.foundation.ws.model.impl.ActionReturn[collection
                    .size()];

            // Iterate over objects in collection
            com.groundwork.collage.model.impl.ActionReturn hibernateObject = null;

            Iterator it = collection.iterator();
            int ii = 0;

            while (it.hasNext()) {
                hibernateObject = (com.groundwork.collage.model.impl.ActionReturn) it
                        .next();

                if (hibernateObject != null) {
                    actionReturnArray[ii] = convert(hibernateObject);
                    ii++;
                }
            }
        }

        return actionReturnArray;
    }

    public org.groundwork.foundation.ws.model.impl.ActionReturn convert(
            ActionReturn hibernateObject) {
        // Note: We return null if the hibernate object is invalid
        if (hibernateObject == null)
            return null;

        return new org.groundwork.foundation.ws.model.impl.ActionReturn(
                hibernateObject.getActionId(), hibernateObject.getReturnCode(),
                hibernateObject.getReturnValue());
    }

    public List<ActionPerform> convert(
            org.groundwork.foundation.ws.model.impl.ActionPerform[] actionPerforms) {
        List<ActionPerform> listPerforms = new ArrayList<ActionPerform>(5);

        if (actionPerforms == null || actionPerforms.length == 0)
            return listPerforms;

        org.groundwork.foundation.ws.model.impl.ActionPerform actionPerform = null;
        for (int i = 0; i < actionPerforms.length; i++) {
            actionPerform = actionPerforms[i];

            listPerforms.add(convert(actionPerform));
        }

        return listPerforms;
    }

    public ActionPerform convert(
            org.groundwork.foundation.ws.model.impl.ActionPerform actionPerform) {
        if (actionPerform == null)
            return null;

        // Convert parameters to Map
        Map<String, String> parameterMap = new HashMap<String, String>(5);
        StringProperty[] parameters = actionPerform.getParameters();
        if (parameters != null && parameters.length > 0) {
            StringProperty property = null;
            for (int i = 0; i < parameters.length; i++) {
                property = parameters[i];

                parameterMap.put(property.getName(), property.getValue());
            }
        }

        return new ActionPerform(actionPerform.getActionID(), parameterMap);
    }

    /**
     * Convert WS Sort to DAO sort This should be moved into a separate layer
     * from DAO. The DAO layer should not know anything about its clients
     */
    public SortCriteria convert(Sort sort) {
        if (sort == null)
            return null;

        SortItem[] sortItems = sort.getSortItem();
        if (sortItems == null || sortItems.length == 0)
            return null;

        SortCriteria sortCriteria = null;
        SortItem item = null;
        int length = sortItems.length;

        for (int i = 0; i < length; i++) {
            item = sortItems[i];
            if (sortCriteria == null)
                sortCriteria = (item.isSortAscending()) ? SortCriteria.asc(item
                        .getPropertyName()) : SortCriteria.desc(item
                        .getPropertyName());
            else
                sortCriteria.addSort(item.getPropertyName(), item
                        .isSortAscending());
        }

        return sortCriteria;
    }

    /**
     * Convert WS Filter to DAO Filter
     */
    public FilterCriteria convert(Filter filter) throws CollageException {
        if (filter == null)
            return null;

        // Validate Filter - If the operator is null we return a null
        // FilterCriteria
        // We return a null FilterCritiria b/c the PHP Web Client passes any
        // empty Filter (<Filter/>)
        // for a null filter instead of no Filter at all.
        FilterOperator operator = filter.getOperator();
        if (operator == null) {
            if (log.isInfoEnabled())
                log
                        .info("Invalid Filter - No operator - Returning null FilterCriteria from convert.");
            return null;
        }

        String propertyName = null;
        Object value = null;

        // Complex / Nested Filter
        if (operator == FilterOperator.AND || operator == FilterOperator.OR) {
            if (filter.getLeftFilter() == null)
                throw new CollageException("Invalid null LeftFilter");

            if (filter.getRightFilter() == null)
                throw new CollageException("Invalid null RightFilter");
        } else {
            propertyName = filter.getPropertyName();
            if (propertyName == null || propertyName.length() == 0) {
                throw new CollageException(
                        "Invalid null / empty Filter property name");
            }

            value = filter.getValue();
            if (value == null) {
                throw new CollageException(
                        "Invalid null / empty Filter property value");
            }
        }

        FilterCriteria filterCriteria = null;

        if (operator == FilterOperator.AND) {
            filterCriteria = convert(filter.getLeftFilter());

            // Invalid left filter
            if (filterCriteria == null)
                return null;

            filterCriteria.and(convert(filter.getRightFilter()));
        } else if (operator == FilterOperator.OR) {
            filterCriteria = convert(filter.getLeftFilter());

            // Invalid left filter
            if (filterCriteria == null)
                return null;

            filterCriteria.or(convert(filter.getRightFilter()));
        } else if (operator == FilterOperator.EQ) {
            filterCriteria = FilterCriteria.eq(propertyName, value);
        } else if (operator == FilterOperator.GT) {
            filterCriteria = FilterCriteria.gt(propertyName, value);
        } else if (operator == FilterOperator.LT) {
            filterCriteria = FilterCriteria.lt(propertyName, value);
        } else if (operator == FilterOperator.GE) {
            filterCriteria = FilterCriteria.ge(propertyName, value);
        } else if (operator == FilterOperator.LE) {
            filterCriteria = FilterCriteria.le(propertyName, value);
        } else if (operator == FilterOperator.NE) {
            filterCriteria = FilterCriteria.ne(propertyName, value);
        } else if (operator == FilterOperator.LIKE) {
            // NOTE: We map the WS Filter LIKE to a case-insenstive match
            // anywhere
            filterCriteria = FilterCriteria.ilike(propertyName, (String) value,
                    MatchType.ANYWHERE);
        } else if (operator == FilterOperator.IN) {
            StringTokenizer stkn = new StringTokenizer((String) value, ",");
            Object[] objArray = new Object[stkn.countTokens()];
            int i = 0;
            while (stkn.hasMoreTokens()) {
                // First always assume that the value supplied is int array
                String tokenValue = stkn.nextToken();
                try {
                    objArray[i] = new Integer(tokenValue);
                } catch (NumberFormatException nfe) // if number format
                // exception then try to set
                // for String array
                {
                    objArray[i] = tokenValue;
                } // end if
                i++;
            }
            filterCriteria = FilterCriteria.in(propertyName, objArray);
        } else {
            throw new CollageException(
                    "convert(Filter filter) - Filter Operator not implemented - "
                            + operator);
        }

        return filterCriteria;
    }

    public org.groundwork.foundation.ws.model.impl.PropertyTypeBinding[] convertPropertyExtensible(
            Collection<PropertyExtensible> collection, boolean bDyanmicOnly) {
        org.groundwork.foundation.ws.model.impl.PropertyTypeBinding[] propArray = null;

        if (collection != null && collection.size() > 0) {
            propArray = new org.groundwork.foundation.ws.model.impl.PropertyTypeBinding[collection
                    .size()];

            // Iterate over objects in collection
            com.groundwork.collage.model.PropertyExtensible hibernateObject = null;

            Iterator it = collection.iterator();
            int ii = 0;

            while (it.hasNext()) {
                hibernateObject = (com.groundwork.collage.model.PropertyExtensible) it
                        .next();

                if (hibernateObject != null) {
                    propArray[ii] = convert(hibernateObject, bDyanmicOnly);
                    ii++;
                }
            }
        }

        return propArray;
    }

    /** ********************************************************************** */
    /***************************************************************************
     * Private Methods /
     **************************************************************************/

    private org.groundwork.foundation.ws.model.impl.PropertyTypeBinding convert(
            PropertyExtensible hibernateObject, boolean bDynamicOnly) {
        // Return an empty binding
        if (hibernateObject == null
                || hibernateObject.getProperties(bDynamicOnly) == null)
            return new org.groundwork.foundation.ws.model.impl.PropertyTypeBinding();

        String key;

        List<StringProperty> stringProperties = new ArrayList<StringProperty>();
        List<IntegerProperty> intProperties = new ArrayList<IntegerProperty>();
        List<DateProperty> dateProperties = new ArrayList<DateProperty>();
        List<BooleanProperty> boolProperties = new ArrayList<BooleanProperty>();
        List<LongProperty> longProperties = new ArrayList<LongProperty>();
        List<DoubleProperty> doubleProperties = new ArrayList<DoubleProperty>();

        Map hibernateProperties = hibernateObject.getProperties(bDynamicOnly);
        Iterator it = hibernateProperties.keySet().iterator();
        Object propertyValue = null;
        while (it.hasNext()) {
            key = (String) it.next();

            // NOTE: We are comparing the type of value instance instead
            // of PropertyType to avoid having to load
            // ApplicationType.applicationEntityProperties
            // Collage convert is not part of a transaction and is assuming all
            // data needed has been
            // retrieved prior to conversion.

            // propType = hibernateObject.getPropertyType(key);
            propertyValue = hibernateProperties.get(key);

            if (propertyValue == null)
                continue;

            if (propertyValue instanceof Integer)
                intProperties.add(new IntegerProperty(key,
                        (Integer) propertyValue));
            else if (propertyValue instanceof String)
                stringProperties.add(new StringProperty(key,
                        (String) propertyValue));
            else if (propertyValue instanceof Boolean)
                boolProperties.add(new BooleanProperty(key,
                        (Boolean) propertyValue));
            else if (propertyValue instanceof Date)
                dateProperties.add(new DateProperty(key, (Date) propertyValue));
            else if (propertyValue instanceof Long)
                longProperties.add(new LongProperty(key, (Long) propertyValue));
            else if (propertyValue instanceof Double)
                doubleProperties.add(new DoubleProperty(key,
                        (Double) propertyValue));
        }

        org.groundwork.foundation.ws.model.impl.PropertyTypeBinding propBinding = new org.groundwork.foundation.ws.model.impl.PropertyTypeBinding();

        if (stringProperties.size() > 0)
            propBinding.setStringProperty(stringProperties
                    .toArray(new StringProperty[stringProperties.size()]));

        if (intProperties.size() > 0)
            propBinding.setIntegerProperty(intProperties
                    .toArray(new IntegerProperty[intProperties.size()]));

        if (dateProperties.size() > 0)
            propBinding.setDateProperty(dateProperties
                    .toArray(new DateProperty[dateProperties.size()]));

        if (doubleProperties.size() > 0)
            propBinding.setDoubleProperty(doubleProperties
                    .toArray(new DoubleProperty[doubleProperties.size()]));

        if (boolProperties.size() > 0)
            propBinding.setBooleanProperty(boolProperties
                    .toArray(new BooleanProperty[boolProperties.size()]));

        if (longProperties.size() > 0)
            propBinding.setLongProperty(longProperties
                    .toArray(new LongProperty[longProperties.size()]));

        return propBinding;
    }
}
