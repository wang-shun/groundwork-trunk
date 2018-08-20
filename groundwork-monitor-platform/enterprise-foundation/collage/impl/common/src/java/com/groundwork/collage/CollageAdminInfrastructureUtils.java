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

package com.groundwork.collage;

import com.groundwork.collage.metrics.CollageMetrics;
import com.groundwork.collage.metrics.CollageTimer;
import com.groundwork.collage.model.Category;
import com.groundwork.collage.model.Device;
import com.groundwork.collage.model.Host;
import com.groundwork.collage.model.HostGroup;
import com.groundwork.collage.model.ServiceStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.category.CategoryService;
import org.groundwork.foundation.bs.hostidentity.HostIdentityService;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * CollageAdminInfrastructureUtils
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class CollageAdminInfrastructureUtils {


    private static CollageMetrics collageMetrics = null;

    private static CollageMetrics getCollageMetrics() {
        if (collageMetrics == null) {
            collageMetrics = CollageFactory.getInstance().getCollageMetrics();
        }
        return collageMetrics;
    }

    public static CollageTimer startMetricsTimer() {
        StackTraceElement element = Thread.currentThread().getStackTrace()[2];
        String className = element.getClassName().substring(element.getClassName().lastIndexOf('.') + 1);
        CollageMetrics collageMetrics = getCollageMetrics();
        return (collageMetrics == null ? null : collageMetrics.startTimer(className, element.getMethodName()));
    }

    public static void stopMetricsTimer(CollageTimer timer) {
        CollageMetrics collageMetrics = getCollageMetrics();
        if (collageMetrics != null) getCollageMetrics().stopTimer(timer);
    }

    protected static Log log = LogFactory.getLog(CollageAdminInfrastructure.class);

    /**
     * Update host via admin.
     *
     * @param host updating host or null
     * @param device updating host device or null
     * @param properties host properties
     * @param mergeHosts merge hosts with matching but different names
     * @param admin admin service
     * @return updated host or null if not merged
     */
    public static Host updateHost(Host host, Device device, Map<String,String> properties,
                                  boolean mergeHosts, CollageAdminInfrastructure admin) {
        CollageTimer timer = startMetricsTimer();
        // update host
        host = admin.addOrUpdateHost(host, device, mergeHosts, properties);
        if (host == null) {
            stopMetricsTimer(timer);
            return null;
        }
        // propagate status change to host groups
        String lastMonitorStatus = host.getLastMonitorStatus();
        String updateMonitorStatus = (((host.getHostStatus() != null) && (host.getHostStatus().getHostMonitorStatus() != null)) ?
                host.getHostStatus().getHostMonitorStatus().getName() : null);
        if ((lastMonitorStatus != null) && !lastMonitorStatus.equalsIgnoreCase(updateMonitorStatus)) {
            admin.propagateHostChangesToHostGroup(host);
        }
        stopMetricsTimer(timer);
        return host;
    }

    /**
     * Remove host via admin.
     *
     * @param hostName host name
     * @param admin admin service
     * @return removed success
     */
    public static boolean removeHost(String hostName, HostIdentityService hostIdentityService,
                                     CollageAdminInfrastructure admin) {
        CollageTimer timer = startMetricsTimer();
        // lookup host
        Host host = hostIdentityService.getHostByIdOrHostName(hostName);
        if (host == null) {
            stopMetricsTimer(timer);
            return false;
        }
        // save host host groups for change propagation
        Set<HostGroup> hostHostGroups = ((host.getHostGroups() != null) ?
                new HashSet<HostGroup>(host.getHostGroups()) : Collections.EMPTY_SET);
        // remove host
        Integer id = admin.removeHost(hostName);
        if (id == null) {
            stopMetricsTimer(timer);
            return false;
        }
        // remove host from host categories, (propagates change to host categories)
        admin.removeCategoryEntity(CategoryService.ENTITY_TYPE_CODE_HOST, id);
        // propagate change to host groups
        if (!hostHostGroups.isEmpty()) {
            admin.propagateHostChangesToHostGroup(hostHostGroups);
        }
        stopMetricsTimer(timer);
        return true;
    }

    /**
     * Update service via admin.
     *
     * @param service updating service or null
     * @param monitorServer service monitor server
     * @param appType service application type
     * @param hostName service host name
     * @param deviceIdentification service host device identification
     * @param agentId service agent id
     * @param host updating service host or null
     * @param device updating service host device or null
     * @param properties service properties
     * @param mergeHosts merge hosts with matching but different names
     * @param admin admin service
     * @return updated service
     */
    public static ServiceStatus updateService(ServiceStatus service, String monitorServer, String appType,
                                              String hostName, String deviceIdentification, String agentId, Host host,
                                              Device device, Map<String,String> properties, boolean mergeHosts,
                                              CollageAdminInfrastructure admin) {
        CollageTimer timer = startMetricsTimer();
        // update service
        service = admin.updateServiceStatus(service, monitorServer, appType, hostName, deviceIdentification, agentId,
                host, device, mergeHosts, properties);
        if (service == null) {
            stopMetricsTimer(timer);
            return null;
        }
        // propagate status change to service group, host, and host groups
        String lastMonitorStatus = service.getLastMonitorStatus();
        String updateMonitorStatus = ((service.getMonitorStatus() != null) ? service.getMonitorStatus().getName() : null);
        if ((lastMonitorStatus != null) && !lastMonitorStatus.equalsIgnoreCase(updateMonitorStatus)) {
            long start = System.currentTimeMillis();
            if (log.isDebugEnabled()) {
                log.debug("Starting propagate services for service: " + service.getServiceDescription() + " and host " + hostName);
            }
            admin.propagateServiceChangesToServiceGroup(service);
            admin.propagateServiceChangesToHost(service);
            admin.propagateHostChangesToHostGroup(service.getHost());
            if (log.isDebugEnabled()) {
                log.debug("Execution time for propagate is " + (System.currentTimeMillis() - start) + " ms");
            }
        }
        stopMetricsTimer(timer);
        return service;
    }

    /**
     * Remove service and delete from service groups via admin.
     *
     * @param hostName service host name
     * @param serviceDescription service description
     * @param hostIdentityService host identity service service
     * @param admin admin service
     * @return removed success
     */
    public static boolean removeService(String hostName, String serviceDescription,
                                        HostIdentityService hostIdentityService, CollageAdminInfrastructure admin) {
        CollageTimer timer = startMetricsTimer();
        // lookup service
        ServiceStatus service = hostIdentityService.getServiceByDescriptionAndHostIdOrHostName(serviceDescription,
                hostName);
        if (service == null) {
            stopMetricsTimer(timer);
            return false;
        }
        // save service host for change propagation
        Host serviceHost = service.getHost();
        // remove service
        Integer id = admin.removeService(hostName, serviceDescription);
        if (id == null || id == -1) {
            stopMetricsTimer(timer);
            return false;
        }
        // remove service from service groups/service categories, (propagates change to
        // service groups/service categories)
        admin.removeCategoryEntity(CategoryService.ENTITY_TYPE_CODE_SERVICESTATUS, id);
        // propagate changes to host and host group
        admin.propagateServiceChangesToHost(serviceHost);
        admin.propagateHostChangesToHostGroup(serviceHost);
        stopMetricsTimer(timer);
        return true;
    }

    /**
     * Remove service group and service group from custom groups via admin.
     *
     * @param name service group name
     * @param admin admin service
     * @return removed success
     */
    public static boolean removeServiceGroup(String name, CollageAdminInfrastructure admin) {
        CollageTimer timer = startMetricsTimer();
        // remove service group
        Category removed = admin.removeCategory(name, CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
        if (removed == null) {
            stopMetricsTimer(timer);
            return false;
        }
        // remove service group from custom groups
        admin.removeCategoryEntity(CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP, removed.getID());
        stopMetricsTimer(timer);
        return true;
    }

    /**
     * Remove host group and host groups from custom groups via admin.
     *
     * @param name host group name
     * @param admin admin service
     * @return removed success
     */
    public static boolean removeHostGroup(String name, CollageAdminInfrastructure admin) {
        CollageTimer timer = startMetricsTimer();
        // remove host group
        Integer id = admin.removeHostGroup(name);
        if (id == null) {
            stopMetricsTimer(timer);
            return false;
        }
        // remove host group from custom groups
        admin.removeCategoryEntity(CategoryService.ENTITY_TYPE_CODE_HOSTGROUP, id);
        stopMetricsTimer(timer);
        return true;
    }
}
