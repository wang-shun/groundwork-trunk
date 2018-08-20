/*
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

package org.groundwork.cloudhub.inventory;

import org.groundwork.cloudhub.connectors.ConnectorConstants;
import org.groundwork.cloudhub.gwos.GwosServiceStatus;
import org.groundwork.cloudhub.gwos.GwosStatus;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoHostGroup;
import org.groundwork.rs.dto.DtoService;
import org.groundwork.rs.dto.DtoServiceGroup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * MonitorInventoryDifference
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class MonitorInventoryDifference {

    public static final String SCHEDULED_DOWNTIME_DEPTH_PROPERTY_NAME = MonitorInventory.SCHEDULED_DOWNTIME_DEPTH_PROPERTY_NAME;
    public static final String LAST_STATE_CHANGE_PROPERTY_NAME = MonitorInventory.LAST_STATE_CHANGE_PROPERTY_NAME;
    public static final String LAST_PLUGIN_OUTPUT_PROPERTY_NAME = MonitorInventory.LAST_PLUGIN_OUTPUT_PROPERTY_NAME;
    public static final String ACKNOWLEDGED_PROPERTY_NAME = MonitorInventory.ACKNOWLEDGED_PROPERTY_NAME;
    public static final String PROBLEM_ACKNOWLEDGED_PROPERTY_NAME = MonitorInventory.PROBLEM_ACKNOWLEDGED_PROPERTY_NAME;
    public static final String COMMENTS_PROPERTY_NAME = MonitorInventory.COMMENTS_PROPERTY_NAME;

    private static final long DATE_EPSILON_MILLIS = 1000L;
    private static final SimpleDateFormat GWOS_ISO_DATE_FORMAT = new SimpleDateFormat(ConnectorConstants.gwosIsoDateFormat);

    /**
     * Inventory type.
     */
    public enum Inventory {HOST, HOST_GROUP, SERVICE, SERVICE_GROUP}

    /**
     * Difference type.
     */
    public enum Type {ADD, DIFFERENCE, REMOVE}

    /**
     * Difference result inner class.
     */
    public static class Difference {

        public Inventory inventory;
        public String name;
        public Type type;
        public boolean statusChanged;
        public boolean notifyStatusChanged;
        public String monitorStatus;

        public Difference(Inventory inventory, String name, Type type) {
            this.inventory = inventory;
            this.name = name;
            this.type = type;
        }

        public Difference(Inventory inventory, String name, Type type, boolean statusChanged, boolean notifyStatusChanged,
                          String monitorStatus) {
            this(inventory, name, type);
            this.statusChanged = statusChanged;
            this.notifyStatusChanged = notifyStatusChanged;
            this.monitorStatus = monitorStatus;
        }
    }

    /**
     * Compute difference between monitor inventories. Differences are returned
     * with inventory and types that when applied to the the current inventory
     * will transform it into the target inventory. In some circumstances target
     * host and service last change dates are modified in the target to reflect
     * more recent synthetic state change times.
     *
     * @param inventory0 current monitor inventory
     * @param inventory1 target monitor inventory
     * @return differences collection
     */
    public static Collection<Difference> difference(MonitorInventory inventory0, MonitorInventory inventory1) {
        List<Difference> differences = new ArrayList<Difference>();

        // compare inventory hosts
        for (Map.Entry<String,DtoHost> hostsEntry : inventory0.getHosts().entrySet()) {
            String hostName = hostsEntry.getKey();
            DtoHost dtoHost0 = hostsEntry.getValue();
            DtoHost dtoHost1 = inventory1.getHosts().get(hostName);
            if (dtoHost1 != null) {
                if (!inventoryEquals(dtoHost0, dtoHost1)) {
                    // check status changed
                    boolean statusChanged = !inventoryEquals(dtoHost0.getMonitorStatus(), dtoHost1.getMonitorStatus());
                    boolean notifyStatusChanged = false;
                    if (statusChanged) {
                        // notify status changed only if not transitioning from PENDING to UP
                        boolean pendingToUp = (inventoryEquals(dtoHost0.getMonitorStatus(), GwosStatus.PENDING.status) &&
                                inventoryEquals(dtoHost1.getMonitorStatus(), GwosStatus.UP.status));
                        notifyStatusChanged = !pendingToUp;
                    }
                    differences.add(new Difference(Inventory.HOST, hostName, Type.DIFFERENCE, statusChanged,
                            notifyStatusChanged, dtoHost1.getMonitorStatus()));
                }
            } else {
                differences.add(new Difference(Inventory.HOST, hostName, Type.REMOVE));
            }
        }
        for (String hostName : inventory1.getHosts().keySet()) {
            if (!inventory0.getHosts().containsKey(hostName)) {
                differences.add(new Difference(Inventory.HOST, hostName, Type.ADD, true, false, GwosStatus.PENDING.status));
            }
        }

        // compare inventory host groups
        for (Map.Entry<String,DtoHostGroup> hostGroupsEntry : inventory0.getHostGroups().entrySet()) {
            String name = hostGroupsEntry.getKey();
            DtoHostGroup dtoHostGroup0 = hostGroupsEntry.getValue();
            DtoHostGroup dtoHostGroup1 = inventory1.getHostGroups().get(name);
            if (dtoHostGroup1 != null) {
                if (!inventoryEquals(dtoHostGroup0, dtoHostGroup1)) {
                    differences.add(new Difference(Inventory.HOST_GROUP, name, Type.DIFFERENCE));
                }
            } else {
                differences.add(new Difference(Inventory.HOST_GROUP, name, Type.REMOVE));
            }
        }
        for (String name : inventory1.getHostGroups().keySet()) {
            if (!inventory0.getHostGroups().containsKey(name)) {
                differences.add(new Difference(Inventory.HOST_GROUP, name, Type.ADD));
            }
        }

        // compare inventory services
        for (Map.Entry<String,DtoService> servicesEntry : inventory0.getServices().entrySet()) {
            String inventoryServiceKey = servicesEntry.getKey();
            DtoService dtoService0 = servicesEntry.getValue();
            DtoService dtoService1 = inventory1.getServices().get(inventoryServiceKey);
            if (dtoService1 != null) {
                if (!inventoryEquals(dtoService0, dtoService1)) {
                    boolean statusChanged = !inventoryEquals(dtoService0.getMonitorStatus(), dtoService1.getMonitorStatus());
                    boolean notifyStatusChanged = false;
                    if (statusChanged) {
                        // notify status changed only if not transitioning from PENDING to OK or
                        // from UNKNOWN to OK when host is transitioning from SUSPENDED; when
                        // connector hosts are unsuspended an inventory sync is performed thus
                        // this test is made - it is not made for subsequent status change events
                        boolean pendingToOk = (inventoryEquals(dtoService0.getMonitorStatus(), GwosServiceStatus.PENDING.status) &&
                                inventoryEquals(dtoService1.getMonitorStatus(), GwosServiceStatus.OK.status));
                        boolean unsuspendUnknownToOk = false;
                        if (inventoryEquals(dtoService0.getMonitorStatus(), GwosServiceStatus.UNKNOWN.status) &&
                                inventoryEquals(dtoService1.getMonitorStatus(), GwosServiceStatus.OK.status)) {
                            DtoHost dtoHost0 = inventory0.getHosts().get(dtoService0.getHostName());
                            DtoHost dtoHost1 = inventory1.getHosts().get(dtoService1.getHostName());
                            if ((dtoHost0 != null) && (dtoHost1 != null)) {
                                unsuspendUnknownToOk = (inventoryEquals(dtoHost0.getMonitorStatus(), GwosStatus.SUSPENDED.status) &&
                                        !inventoryEquals(dtoHost1.getMonitorStatus(), GwosStatus.SUSPENDED.status));
                            }
                        }
                        notifyStatusChanged = !pendingToOk && !unsuspendUnknownToOk;
                    }
                    differences.add(new Difference(Inventory.SERVICE, inventoryServiceKey, Type.DIFFERENCE,
                            statusChanged, notifyStatusChanged, dtoService1.getMonitorStatus()));
                }
            } else {
                differences.add(new Difference(Inventory.SERVICE, inventoryServiceKey, Type.REMOVE));
            }
        }
        for (String inventoryServiceKey : inventory1.getServices().keySet()) {
            if (!inventory0.getServices().containsKey(inventoryServiceKey)) {
                differences.add(new Difference(Inventory.SERVICE, inventoryServiceKey, Type.ADD, true, false,
                        GwosServiceStatus.PENDING.status));
            }
        }

        // compare inventory service groups
        for (Map.Entry<String,DtoServiceGroup> serviceGroupsEntry : inventory0.getServiceGroups().entrySet()) {
            String name = serviceGroupsEntry.getKey();
            DtoServiceGroup dtoServiceGroup0 = serviceGroupsEntry.getValue();
            DtoServiceGroup dtoServiceGroup1 = inventory1.getServiceGroups().get(name);
            if (dtoServiceGroup1 != null) {
                if (!inventoryEquals(dtoServiceGroup0, dtoServiceGroup1)) {
                    differences.add(new Difference(Inventory.SERVICE_GROUP, name, Type.DIFFERENCE));
                }
            } else {
                differences.add(new Difference(Inventory.SERVICE_GROUP, name, Type.REMOVE));
            }
        }
        for (String name : inventory1.getServiceGroups().keySet()) {
            if (!inventory0.getServiceGroups().containsKey(name)) {
                differences.add(new Difference(Inventory.SERVICE_GROUP, name, Type.ADD));
            }
        }

        return differences;
    }

    /**
     * Compare host inventory.
     *
     * @param dtoHost0 current host to compare
     * @param dtoHost1 target host to compare
     * @return equals
     */
    private static final boolean inventoryEquals(DtoHost dtoHost0, DtoHost dtoHost1) {
        // trival safety checks, (should not be triggered for differences)
        if (dtoHost0 == dtoHost1) {
            return true;
        }
        if ((dtoHost0 == null) || (dtoHost1 == null)) {
            return false;
        }
        if (!inventoryEquals(dtoHost0.getHostName(), dtoHost1.getHostName())) {
            return false;
        }
        // host owner is determined by application type
        boolean owner = inventoryEquals(dtoHost0.getAppType(), dtoHost1.getAppType());
        if (owner) {
            // side effect target last state change in event synthetic state
            // change has occurred more recently as set in current than
            // reflected in target
            if (inventoryNewer(dtoHost0.getLastStateChange(), dtoHost1.getLastStateChange())) {
                dtoHost1.setLastStateChange(dtoHost0.getLastStateChange());
            }
            if (inventoryNewer(dtoHost0.getProperty(LAST_STATE_CHANGE_PROPERTY_NAME),
                    dtoHost1.getProperty(LAST_STATE_CHANGE_PROPERTY_NAME))) {
                dtoHost1.putProperty(LAST_STATE_CHANGE_PROPERTY_NAME, dtoHost0.getProperty(LAST_STATE_CHANGE_PROPERTY_NAME));
            }
            // note: application type cannot be updated, (do not compare application
            // type or agent id since multiple owners are allowed to manage host groups to
            // support unified monitoring); host monitor server cannot be compared since it
            // is not set in GWOS inventory;
            boolean equals = (inventoryEquals(dtoHost0.getDescription(), dtoHost1.getDescription()) &&
                    inventoryEquals(dtoHost0.getDeviceIdentification(), dtoHost1.getDeviceIdentification()) &&
                    inventoryEquals(dtoHost0.getDeviceDisplayName(), dtoHost1.getDeviceDisplayName()) &&
                    inventoryEquals(dtoHost0.getProperty(SCHEDULED_DOWNTIME_DEPTH_PROPERTY_NAME),
                            dtoHost1.getProperty(SCHEDULED_DOWNTIME_DEPTH_PROPERTY_NAME)) &&
                    inventoryEquals(dtoHost0.getMonitorStatus(), dtoHost1.getMonitorStatus()) &&
                    inventoryEquals(dtoHost0.getLastStateChange(), dtoHost1.getLastStateChange()) &&
                    inventoryEquals(dtoHost0.getProperty(LAST_STATE_CHANGE_PROPERTY_NAME),
                            dtoHost1.getProperty(LAST_STATE_CHANGE_PROPERTY_NAME)) &&
                    inventoryEquals(dtoHost0.getLastCheckTime(), dtoHost1.getLastCheckTime()) &&
                    inventoryEquals(dtoHost0.getNextCheckTime(), dtoHost1.getNextCheckTime()) &&
                    inventoryEquals(dtoHost0.getStateType(), dtoHost1.getStateType()) &&
                    inventoryEquals(dtoHost0.isAcknowledged(), dtoHost1.isAcknowledged()) &&
                    inventoryEquals(dtoHost0.getProperty(ACKNOWLEDGED_PROPERTY_NAME),
                            dtoHost1.getProperty(ACKNOWLEDGED_PROPERTY_NAME)) &&
                    inventoryEquals(dtoHost0.getCheckType(), dtoHost1.getCheckType()) &&
                    inventoryContains(dtoHost0.getLastPlugInOutput(), dtoHost1.getLastPlugInOutput()) &&
                    inventoryContains(dtoHost0.getProperty(LAST_PLUGIN_OUTPUT_PROPERTY_NAME),
                            dtoHost1.getProperty(LAST_PLUGIN_OUTPUT_PROPERTY_NAME)) &&
                    inventoryEquals(dtoHost0.getProperty(COMMENTS_PROPERTY_NAME),
                            dtoHost1.getProperty(COMMENTS_PROPERTY_NAME)));
            // if not equal, side effect agent id to prevent accidental change in
            // ownership to support unified monitoring)
            if (!equals) {
                dtoHost0.setAgentId(null);
            }
            return equals;
        } else {
            // note: if not owner, only last plugin output can be updated
            return (inventoryContains(dtoHost0.getLastPlugInOutput(), dtoHost1.getLastPlugInOutput()) &&
                    inventoryContains(dtoHost0.getProperty(LAST_PLUGIN_OUTPUT_PROPERTY_NAME),
                            dtoHost1.getProperty(LAST_PLUGIN_OUTPUT_PROPERTY_NAME)));
        }
    }

    /**
     * Compare host group inventory.
     *
     * @param dtoHostGroup0 current host group to compare
     * @param dtoHostGroup1 target host group to compare
     * @return equals
     */
    private static final boolean inventoryEquals(DtoHostGroup dtoHostGroup0, DtoHostGroup dtoHostGroup1) {
        // trival safety checks, (should not be triggered for differences)
        if (dtoHostGroup0 == dtoHostGroup1) {
            return true;
        }
        if ((dtoHostGroup0 == null) || (dtoHostGroup1 == null)) {
            return false;
        }
        if (!inventoryEquals(dtoHostGroup0.getName(), dtoHostGroup1.getName())) {
            return false;
        }
        boolean equals = true;
        // note: application type and agent id cannot be updated, (do not compare
        // since multiple owners are allowed to manage host groups to support
        // unified monitoring)
        if (inventoryEquals(dtoHostGroup0.getDescription(), dtoHostGroup1.getDescription()) &&
                inventoryEquals(dtoHostGroup0.getHosts(), dtoHostGroup1.getHosts())) {
            if ((dtoHostGroup0.getHosts() != null) && (dtoHostGroup1.getHosts() != null)) {
                Set<String> hostNames0 = new HashSet<String>();
                for (DtoHost dtoHost0 : dtoHostGroup0.getHosts()) {
                    hostNames0.add(dtoHost0.getHostName());
                }
                for (DtoHost dtoHost1 : dtoHostGroup1.getHosts()) {
                    if (!hostNames0.contains(dtoHost1.getHostName())) {
                        equals = false;
                        break;
                    }
                }
            }
        } else {
            equals = false;
        }
        // if not equal, side effect application type and agent id to prevent
        // accidental change in ownership to support unified monitoring)
        if (!equals) {
            dtoHostGroup1.setAppType(null);
            dtoHostGroup1.setAgentId(null);
        }
        return equals;
    }

    /**
     * Compare service inventory.
     *
     * @param dtoService0 current service to compare
     * @param dtoService1 target service to compare
     * @return equals
     */
    private static final boolean inventoryEquals(DtoService dtoService0, DtoService dtoService1) {
        // trival safety checks, (should not be triggered for differences)
        if (dtoService0 == dtoService1) {
            return true;
        }
        if ((dtoService0 == null) || (dtoService1 == null)) {
            return false;
        }
        if (!inventoryEquals(dtoService0.getHostName(), dtoService1.getHostName()) ||
                !inventoryEquals(dtoService0.getDescription(), dtoService1.getDescription())) {
            return false;
        }
        // service owner is determined by application type
        boolean owner = inventoryEquals(dtoService0.getAppType(), dtoService1.getAppType());
        if (!owner) {
            return false;
        }
        // side effect target last state change in event synthetic state
        // change has occurred more recently as set in current than
        // reflected in target
        if (inventoryNewer(dtoService0.getLastStateChange(), dtoService1.getLastStateChange())) {
            dtoService1.setLastStateChange(dtoService0.getLastStateChange());
        }
        // note: application type cannot be updated, prevent agent id from being
        // modified on update, (do not compare application type or agent id since
        // multiple owners are allowed to manage host groups to support unified
        // monitoring); service monitor server cannot be compared since it is not
        // set in GWOS inventory
        boolean equals = (inventoryEquals(dtoService0.getProperty(SCHEDULED_DOWNTIME_DEPTH_PROPERTY_NAME),
                dtoService1.getProperty(SCHEDULED_DOWNTIME_DEPTH_PROPERTY_NAME)) &&
                inventoryEquals(dtoService0.getMonitorStatus(), dtoService1.getMonitorStatus()) &&
                inventoryEquals(dtoService0.getLastStateChange(), dtoService1.getLastStateChange()) &&
                inventoryEquals(dtoService0.getLastCheckTime(), dtoService1.getLastCheckTime()) &&
                inventoryEquals(dtoService0.getNextCheckTime(), dtoService1.getNextCheckTime()) &&
                inventoryEquals(dtoService0.getStateType(), dtoService1.getStateType()) &&
                inventoryEquals(dtoService0.getLastHardState(), dtoService1.getLastHardState()) &&
                inventoryEquals(dtoService0.getProperty(PROBLEM_ACKNOWLEDGED_PROPERTY_NAME),
                        dtoService1.getProperty(PROBLEM_ACKNOWLEDGED_PROPERTY_NAME)) &&
                inventoryEquals(dtoService0.getCheckType(), dtoService1.getCheckType()) &&
                inventoryContains(dtoService0.getLastPlugInOutput(), dtoService1.getLastPlugInOutput()) &&
                inventoryEquals(dtoService0.getProperty(LAST_PLUGIN_OUTPUT_PROPERTY_NAME),
                        dtoService1.getProperty(LAST_PLUGIN_OUTPUT_PROPERTY_NAME)) &&
                inventoryEquals(dtoService0.getProperty(COMMENTS_PROPERTY_NAME),
                        dtoService1.getProperty(COMMENTS_PROPERTY_NAME)));
        // if not equal, side effect agent id to prevent accidental change in
        // ownership to support unified monitoring)
        if (!equals) {
            dtoService0.setAgentId(null);
        }
        return equals;
    }

    /**
     * Compare service group inventory.
     *
     * @param dtoServiceGroup0 current service group to compare
     * @param dtoServiceGroup1 target service group to compare
     * @return equals
     */
    private static final boolean inventoryEquals(DtoServiceGroup dtoServiceGroup0, DtoServiceGroup dtoServiceGroup1) {
        // trival safety checks, (should not be triggered for differences)
        if (dtoServiceGroup0 == dtoServiceGroup1) {
            return true;
        }
        if ((dtoServiceGroup0 == null) || (dtoServiceGroup1 == null)) {
            return false;
        }
        if (!inventoryEquals(dtoServiceGroup0.getName(), dtoServiceGroup1.getName())) {
            return false;
        }
        boolean equals = true;
        // note: application type and agent id can be updated, but prevent this
        // to keep symmetric with host groups behavior, (do not compare since
        // multiple owners are allowed to manage service groups to support unified
        // monitoring)
        if (inventoryEquals(dtoServiceGroup0.getDescription(), dtoServiceGroup1.getDescription()) &&
                inventoryEquals(dtoServiceGroup0.getServices(), dtoServiceGroup1.getServices())) {
            if ((dtoServiceGroup0.getServices() != null) && (dtoServiceGroup1.getServices() != null)) {
                Set<String> inventoryServiceKeys0 = new HashSet<String>();
                for (DtoService dtoService0 : dtoServiceGroup0.getServices()) {
                    inventoryServiceKeys0.add(dtoService0.getHostName() + "!" + dtoService0.getDescription());
                }
                for (DtoService dtoService1 : dtoServiceGroup1.getServices()) {
                    if (!inventoryServiceKeys0.contains(dtoService1.getHostName() + "!" + dtoService1.getDescription())) {
                        equals = false;
                        break;
                    }
                }
            }
        } else {
            equals = false;
        }
        // if not equal, side effect application type and agent id to prevent
        // accidental change in ownership to support unified monitoring)
        if (!equals) {
            dtoServiceGroup1.setAppType(null);
            dtoServiceGroup1.setAgentId(null);
        }
        return equals;
    }

    /**
     * Compare string fields, (also considered equal if target missing).
     *
     * @param str0 current string to compare
     * @param str1 target string to compare
     * @return equals
     */
    private static final boolean inventoryEquals(String str0, String str1) {
        return (((str0 != null) && str0.equals(str1)) ||
                ((str0 == null) && (str1 == null)) ||
                ((str0 != null) && str0.isEmpty() && (str1 == null)) ||
                ((str0 == null) && (str1 != null) && str1.isEmpty()) ||
                (str1 == null));
    }

    /**
     * Test string fields contains, (also considered containing if target missing).
     *
     * @param str0 current string to compare
     * @param str1 target string to compare
     * @return contains
     */
    private static final boolean inventoryContains(String str0, String str1) {
        return (((str0 != null) && (str1 != null) && str0.contains(str1)) || ((str0 == null) && (str1 == null)) || (str1 == null));
    }

    /**
     * Compare integer fields, (also considered equal if target missing).
     *
     * @param int0 current integer to compare
     * @param int1 target integer to compare
     * @return equals
     */
    private static final boolean inventoryEquals(Integer int0, Integer int1) {
        return (((int0 != null) && int0.equals(int1)) || ((int0 == null) && (int1 == null)) || (int1 == null));
    }

    /**
     * Compare boolean fields, (also considered equal if target missing).
     *
     * @param bool0 current boolean to compare
     * @param bool1 target boolean to compare
     * @return equals
     */
    private static final boolean inventoryEquals(Boolean bool0, Boolean bool1) {
        return (((bool0 != null) && bool0.equals(bool1)) || ((bool0 == null) && (bool1 == null)) || (bool1 == null));
    }

    /**
     * Compare date fields within millisecond epsilon, (also considered equal
     * if target missing).
     *
     * @param date0 current date to compare
     * @param date1 target date to compare
     * @return equals
     */
    private static final boolean inventoryEquals(Date date0, Date date1) {
        return (((date0 != null) && (date1 != null) &&
                (date0.equals(date1) || (Math.abs(date0.getTime()-date1.getTime()) < DATE_EPSILON_MILLIS))) ||
                ((date0 == null) && (date1 == null)) || (date1 == null));
    }

    /**
     * Compare date fields if newer, (not considered newer if target missing)
     *
     * @param date0 current date to compare
     * @param date1 target date to compare
     * @return equals
     */
    private static final boolean inventoryNewer(Date date0, Date date1) {
        return ((date0 != null) && (date1 != null) && (date0.getTime() > date1.getTime()));
    }

    /**
     * Compare sring date fields if newer, (not considered newer if target missing)
     *
     * @param date0 current date to compare
     * @param date1 target date to compare
     * @return equals
     */
    private static final boolean inventoryNewer(String date0, String date1) {
        if ((date0 != null) && (date1 != null)) {
            if (date0.equals(date1)) {
                return false;
            }
            synchronized (GWOS_ISO_DATE_FORMAT) {
                try {
                    Date date0AsDate = GWOS_ISO_DATE_FORMAT.parse(date0);
                    Date date1AsDate = GWOS_ISO_DATE_FORMAT.parse(date1);
                    return (date0AsDate.getTime() > date1AsDate.getTime());
                } catch (ParseException e) {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    /**
     * Compare collection fields size, (also considered equal if target missing).
     *
     * @param collection0 current date to compare
     * @param collection1 target date to compare
     * @return equals
     */
    private static final boolean inventoryEquals(Collection collection0, Collection collection1) {
        return (((collection0 != null) && (collection1 != null) && (collection0.size() == collection1.size())) ||
                ((collection0 == null) && (collection1 == null)) ||
                ((collection0 != null) && collection0.isEmpty() && (collection1 == null)) ||
                ((collection0 == null) && (collection1 != null) && collection1.isEmpty()) ||
                ((collection1 == null) || collection1.isEmpty()));
    }
}
