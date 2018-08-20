/*
 * Copyright (C) 2010 GroundWork Open Source, Inc. (GroundWork) All rights
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
package com.groundwork.dashboard.portlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.groundwork.collage.util.MonitorStatusBubbleUp;
import com.groundwork.dashboard.portlets.dto.DashboardHost;
import com.groundwork.dashboard.portlets.dto.DashboardService;
import com.groundwork.dashboard.portlets.dto.HitListEditPrefs;
import com.groundwork.dashboard.portlets.dto.HitListPrefs;
import com.groundwork.dashboard.portlets.dto.HitListResult;
import com.groundworkopensource.portal.common.CommonConstants;
import com.groundworkopensource.portal.common.GroundworkInfoReader;
import com.groundworkopensource.portal.model.ExtendedRoleList;
import com.groundworkopensource.portal.model.ExtendedUIRole;
import org.groundwork.foundation.ws.impl.WSClientConfiguration;
import org.groundwork.rs.client.ExtendedRoleClient;
import org.groundwork.rs.client.HostClient;
import org.groundwork.rs.client.HostGroupClient;
import org.groundwork.rs.client.ServiceClient;
import org.groundwork.rs.dto.DtoDepthType;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoHostGroup;
import org.groundwork.rs.dto.DtoService;

import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class HitListPortlet extends GroundworkDashboardPortlet {

    protected final static String PREFS_HOST_GROUP = "hostGroup";
    protected final static String PREFS_HOST_GROUP_DEFAULT = "--ALL--";

    private static String currentVersion = null;
    private static Boolean is710 = false;
    private static Object semaphore = new Object();

    @Override
    protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        response.setContentType("text/html");
        super.doView(request, response);
    }

    @Override
    protected void doEdit(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        response.setContentType("text/html");
        if (authenticate(request, response)) {
            PortletRequestDispatcher dispatcher = null;
            String url = request.getPreferences().getValue(PREFS_EDIT, "/app/views/hitlist-edit.jsp" );
            dispatcher = getPortletContext().getRequestDispatcher(url);
            dispatcher.include(request, response);
        }
    }

    @Override
    public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
        String resourceID = request.getResourceID();
        if (resourceID == null) {
            response.addProperty(ResourceResponse.HTTP_STATUS_CODE, "400");
            response.getWriter().println(createError(400, "invalid resource id"));
            return;
        }
        if (resourceID.equals("hitlist")) { // Standard VIEW mode
            String selectedHostGroup = request.getPreferences().getValue(PREFS_HOST_GROUP, PREFS_HOST_GROUP_DEFAULT);
            HitListResult result = computeHitList(selectedHostGroup);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
            if (!authenticate(request, response)) {
                result.setSuccess(false);
                result.setMessage("Failed to authenticate. Cannot connect to Foundation Server");
            }
            HitListPrefs prefs = new HitListPrefs();
            prefs.setRows(Integer.parseInt(request.getPreferences().getValue(PREFS_ROWS, "5")));
            prefs.setRefreshSeconds(Integer.parseInt(request.getPreferences().getValue(PREFS_REFRESH_SECONDS, "60")));
            prefs.setHostGroup(selectedHostGroup);
            result.setPrefs(prefs);
            StringWriter writer = new StringWriter();
            objectMapper.writeValue(writer, result);
            response.getWriter().println(writer);
        }
        else { // EDIT mode
            if (resourceID.equals("writePrefs")) {
                StringWriter writer = new StringWriter();
                drain(request.getReader(), writer);
                String json = writer.toString();
                ObjectMapper writeMapper = new ObjectMapper();
                HitListPrefs update = writeMapper.readValue(json, HitListPrefs.class);
                request.getPreferences().setValue(PREFS_ROWS, Integer.toString(update.getRows()));
                request.getPreferences().setValue(PREFS_REFRESH_SECONDS, Integer.toString(update.getRefreshSeconds()));
                request.getPreferences().setValue(PREFS_HOST_GROUP, update.getHostGroup());
                request.getPreferences().store();
            }
            HitListEditPrefs prefs = new HitListEditPrefs();
            prefs.setRows(Integer.parseInt(request.getPreferences().getValue(PREFS_ROWS, "5")));
            prefs.setRefreshSeconds(Integer.parseInt(request.getPreferences().getValue(PREFS_REFRESH_SECONDS, "60")));
            prefs.setHostGroup(request.getPreferences().getValue(PREFS_HOST_GROUP, PREFS_HOST_GROUP_DEFAULT));
            List<String> hostGroups = retrieveHostGroups(request);
            prefs.setHostGroups(hostGroups);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
            StringWriter writer = new StringWriter();
            objectMapper.writeValue(writer, prefs);
            response.getWriter().println(writer);
        }
    }

    /**
     * Host Problems:
     *
     1. Down and Unacknowledged 			==> host.status == UNSCHEDULED_DOWN(8) OR DOWN(21)
     2. Down and Acknowledged 				==> host.status == UNSCHEDULED_DOWN(8) OR DOWN(21) // ACKNOWLEDGEMENT(DOWN)(13)
     3. Unreachable 						==> host.status == UNREACHABLE(7) OR SUSPENDED(23)
     4. Scheduled DOWN  					==> host.status == SCHEDULED_DOWN(6)

     Service Problems (Hosts Up):
     1. Critical and Unacknowledged      	==> host.status == OK AND service.status = CRITICAL(20)
     2. Warning and unacknowledged       	==> host.status == OK AND service.status = WARNING(9)
     3. Critical (acknowledged)          	==> host.status == OK AND service.status = ACKNOWLEDGEMENT (CRITICAL)(12)
     4. Warning (acknowledged) 				==> host.status == OK AND service.status = ACKNOWLEDGEMENT (WARNING) (11)

     Service Problems (Hosts Down):
     1. Critical Services on Down Hosts  	==> host.stats !== OK AND service.status = CRITICAL (20)
     2. Warning Services on Down Hosts   	==> host.stats !== OK AND service.status = WARNING (9)
     * @return
     */
    protected HitListResult computeHitList(String selectedHostGroup) {
        Set<String> upHosts = new HashSet<>();
        Set<String> downHosts = new HashSet<>();
        String foundationRestService = WSClientConfiguration.getProperty(WSClientConfiguration.FOUNDATION_REST_ENDPOINT);
        HitListResult result = new HitListResult();

        try {
            // Process hosts
            HostClient hostClient = new HostClient(foundationRestService);
            List<DtoHost> hosts;
            if (selectedHostGroup == null || selectedHostGroup.isEmpty() || selectedHostGroup.equalsIgnoreCase(PREFS_HOST_GROUP_DEFAULT)) {
                hosts = hostClient.list(DtoDepthType.Shallow);
            } else {
                if (currentVersion == null) {
                    synchronized (semaphore) {
                        Properties props = GroundworkInfoReader.readInfoProperties();
                        currentVersion = props.getProperty("version");
                        if (currentVersion != null) {
                            is710 = currentVersion.startsWith("7.1.0");
                        }
                    }
                }
                if (is710) {
                    hosts = hostClient.query("hostgroup = '" + selectedHostGroup + "'", DtoDepthType.Shallow);
                }
                else {
                    List<String> filter = new ArrayList<>();
                    filter.add(selectedHostGroup);
                    hosts = hostClient.filterByHostGroups(filter, DtoDepthType.Shallow);
                }
            }
            for (DtoHost host : hosts) {
                String status = (host.getMonitorStatus() == null) ? MonitorStatusBubbleUp.UP : host.getMonitorStatus();
                DashboardHost dashboardHost = new DashboardHost(host.getHostName(), status, host.getLastStateChange());
                if (status.equals(MonitorStatusBubbleUp.UP)) {
                    upHosts.add(host.getHostName());
                } else if (status.equals(MonitorStatusBubbleUp.UNSCHEDULED_DOWN) || status.equals(MonitorStatusBubbleUp.DOWN)) {
                    downHosts.add(host.getHostName());
                    if (host.isAcknowledged()) {
                        result.addHostDownAcknowledged(dashboardHost);
                    } else {
                        result.addHostDownUnacknowledged(dashboardHost);
                    }
                } else if (status.equals(MonitorStatusBubbleUp.UNREACHABLE) || status.equals(MonitorStatusBubbleUp.SUSPENDED)) {
                    result.addHostUnreachable(dashboardHost);
                } else if (status.equals(MonitorStatusBubbleUp.SCHEDULED_DOWN)) {
                    result.addHostScheduledDown(dashboardHost);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("host status not matched: " + host.getHostName() + ", " + status);
                    }
                }
            }

            // Process services
            ServiceClient serviceClient = new ServiceClient(foundationRestService);
            List<DtoService> services = serviceClient.list();
            for (DtoService service : services) {
                String status = (service.getMonitorStatus() == null) ? MonitorStatusBubbleUp.OK : service.getMonitorStatus();
                DashboardService dashboardService = new DashboardService(service.getDescription(), status, service.getLastStateChange(), service.getHostName());
                boolean isCritical = status.equals(MonitorStatusBubbleUp.UNSCHEDULED_CRITICAL)
                        || status.equals(MonitorStatusBubbleUp.CRITICAL)
                        || status.equals(MonitorStatusBubbleUp.SCHEDULED_CRITICAL);
                boolean isWarning = status.equals(MonitorStatusBubbleUp.WARNING);
                boolean isAcknowledged = false;
                //String ack = service.getProperty("isAcknowledged");
                String ack = service.getProperty("isProblemAcknowledged");
                if (ack != null) {
                    isAcknowledged = Boolean.parseBoolean(ack);
                }
                if (downHosts.contains(service.getHostName())) {
                    if (isCritical) {
                        result.addServiceCriticalDown(dashboardService);
                    } else if (isWarning) {
                        result.addServiceWarningDown(dashboardService);
                    }
                } else if (upHosts.contains(service.getHostName())) {
                    if (isCritical) {
                        if (isAcknowledged) {
                            result.addServiceCriticalAcknowledged(dashboardService);
                        } else {
                            result.addServiceCriticalUnacknowledged(dashboardService);
                        }
                    } else if (isWarning) {
                        if (isAcknowledged) {
                            result.addServiceWarningAcknowledged(dashboardService);
                        } else {
                            result.addServiceWarningUnacknowledged(dashboardService);
                        }
                    } else {
                        //                    if (log.isDebugEnabled()) {
                        //                        log.debug("service neither warning nor critical: " + service.getHostName() + " : " + service.getDescription());
                        //                    }
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("service's host neither up or down: " + service.getHostName() + " : " + service.getDescription());
                    }
                }
            }
            result.updateCounts();
            return result;
        }
        catch (Exception e) {
            String message = "Failed to retrieve hit list: " + e.getMessage();
            log.error(message, e);
            result.setSuccess(false);
            result.setMessage(message);
            return result;
        }
    }

    protected List<String> retrieveHostGroups(ResourceRequest request) {
        String foundationRestService = WSClientConfiguration.getProperty(WSClientConfiguration.FOUNDATION_REST_ENDPOINT);
        String extensionURL = getPortalExtensionURL();
        Set<String> filteredHostGroups = new HashSet<>();
        if (extensionURL != null) {
            ExtendedRoleClient client = new ExtendedRoleClient(extensionURL, MediaType.APPLICATION_XML_TYPE);
            ExtendedRoleList roles = client.findRolesByUser(request.getRemoteUser());
            for (ExtendedUIRole extRole : roles.getList()) {
                String list = extRole.getHgList();
                if (list != null && !list.isEmpty()) {
                    String[] tokens = list.split(",");
                    if (tokens.length > 0) {
                        for (String token : tokens) {
                            filteredHostGroups.add(token);
                        }
                    }
                }
            }
            //filteredHostGroups.add("STOR:oracle");
        }
        List<String> hostGroups = new ArrayList<>();
        HostGroupClient hgClient = new HostGroupClient(foundationRestService);
        List<DtoHostGroup> dtoHostGroups = hgClient.list(DtoDepthType.Simple);
        if (filteredHostGroups.size() > 0) {
            for (DtoHostGroup group : dtoHostGroups) {
                if (filteredHostGroups.contains(group.getName())) {
                    hostGroups.add(group.getName());
                }
            }
        }
        else {
            for (DtoHostGroup group : dtoHostGroups) {
                hostGroups.add(group.getName());
            }
        }
        return hostGroups;
    }

    protected String getPortalExtensionURL() {
        try (InputStream file = new FileInputStream(new File(CommonConstants.STATUS_VIEWER_PROP_PATH))) {
                //"/usr/local/groundwork/config/status-viewer.properties"))) {
            Properties props = new Properties();
            props.load(file);
            // "portal.extension.resteasy.service.url"
            return props.getProperty(CommonConstants.PORTAL_EXTN_RESTEASY_URL_KEY);
        } catch (Exception e) {
            log.error("error loading resteasy props: " + e.getMessage(), e);
        }
        return null;
    }

}
