/*
 * Copyright (C) 2017 GroundWork Open Source, Inc. (GroundWork) All rights
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
import com.groundwork.dashboard.NocConfiguration;
import com.groundwork.dashboard.configuration.CheckedState;
import com.groundwork.dashboard.configuration.DashboardConfiguration;
import com.groundwork.dashboard.configuration.DashboardConfigurationException;
import com.groundwork.dashboard.configuration.DashboardConfigurationFactory;
import com.groundwork.dashboard.configuration.DashboardConfigurationService;
import com.groundwork.dashboard.portlets.dto.NocBoardAck;
import com.groundwork.dashboard.portlets.dto.NocBoardNotification;
import com.groundwork.dashboard.portlets.dto.NocBoardPostComment;
import com.groundwork.dashboard.portlets.dto.NocBoardResult;
import com.groundwork.dashboard.portlets.dto.SimpleResult;
import com.groundwork.downtime.DowntimeContext;
import com.groundwork.downtime.DowntimeException;
import com.groundwork.downtime.DowntimeService;
import com.groundwork.downtime.DowntimeServiceFactory;
import com.groundworkopensource.portal.common.eventbroker.NagiosCommandProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.ws.impl.WSClientConfiguration;
import org.groundwork.rs.client.CommentClient;
import org.groundwork.rs.client.ServiceClient;
import org.groundwork.rs.dto.DtoDepthType;
import org.groundwork.rs.dto.DtoOperationResults;
import org.groundwork.rs.dto.DtoService;

import javax.portlet.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

public class NocBoardPortlet extends GroundworkDashboardPortlet {

    protected static Log log = LogFactory.getLog(NocBoardPortlet.class);

    protected DashboardConfigurationService dashboardService;

    protected final static String PREFS_DASHBOARD_NAME = "dashboard";


    @Override
    public void init(PortletConfig config) throws PortletException {
        super.init(config);
        dashboardService = DashboardConfigurationFactory.getConfigurationService(DashboardConfigurationFactory.ServiceType.NOC);
    }

    @Override
    protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        response.setContentType("text/html");
        if (MOCK_DATA) {
            String url = request.getPreferences().getValue(PREFS_VIEW, "/app/views/monitor.html");
            PortletRequestDispatcher dispatcher = getPortletContext().getRequestDispatcher(url);
            dispatcher.include(request, response);
            return;
        }
        String nocRole = NocConfiguration.getProperty(NocConfiguration.NOC_DOWNTIME_ROLE);
        if (authenticate(request, response, nocRole)) {
            String url = request.getPreferences().getValue(PREFS_VIEW, "/app/views/monitor.html" );
            PortletRequestDispatcher dispatcher = getPortletContext().getRequestDispatcher(url);
            dispatcher.include(request, response);
        }
    }

    @Override
    protected void doEdit(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        response.setContentType("text/html");
        if (authenticate(request, response)) {
            PortletRequestDispatcher dispatcher = null;
            String url = request.getPreferences().getValue(PREFS_EDIT, "/app/views/nocboard-edit.jsp");
            dispatcher = getPortletContext().getRequestDispatcher(url);
            dispatcher.include(request, response);
        }
    }

    private boolean MOCK_DATA = false;

    @Override
    public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
        if (MOCK_DATA) {
            //InputStream stream = this.getPortletContext().getResourceAsStream("/testdata/nocboard.json");
            //drain(new InputStreamReader(stream), response.getWriter());
            FileReader reader = new FileReader(new File("/usr/local/groundwork/mockdata/nocboard-full.json"));
            drain(reader, response.getWriter());
            return;
        }
        String resourceID = request.getResourceID();
        if (!validateResourceRequest(request, response, resourceID)) {
            return;
        }
        if (resourceID.equals("addComment")) {
            addComment(request, response);
            return;
        }
        if (resourceID.equals("deleteComment")) {
            deleteComment(request, response);
            return;
        }
        if (resourceID.equals("postAck")) {
            postAck(request, response);
            return;
        }
        if (resourceID.equals("notifications")) {
            postNotification(request, response);
            return;
        }
        // process default - serve data with nocboard resource id
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        try {
            String dashboardName = request.getPreferences().getValue(PREFS_DASHBOARD_NAME, null);
            if (dashboardName == null) {
                throw new DashboardConfigurationException("Preference is not set");
            }
            DashboardConfiguration dashboard = dashboardService.read(dashboardName);

            // override dashboard display of columns if global settings overriden
            Boolean enabledAvailability = NocConfiguration.getBooleanProperty(NocConfiguration.NOC_DOWNTIME_ENABLE);
            if (!NocConfiguration.getBooleanProperty(NocConfiguration.NOC_DOWNTIME_ENABLE)) {
                overrideColumnSetting(dashboard.getColumns(), DashboardConfiguration.COLUMNS_MAINTENANCE);
            }
            if (!NocConfiguration.getBooleanProperty(NocConfiguration.NOC_AVAILABILITY_ENABLE)) {
                overrideColumnSetting(dashboard.getColumns(), DashboardConfiguration.COLUMNS_AVAILABILITY);
            }

            Boolean enableDowntime = NocConfiguration.getBooleanProperty(NocConfiguration.NOC_DOWNTIME_ENABLE);
            DowntimeContext downtimeContext = null;
            String message = null;
            if (enableDowntime) {
                try {
                    downtimeContext = acquireContext(request);
                }
                catch (Exception e) {
                    message = "Failed to login to downtime: " +e.getMessage();
                    log.error(message, e);
                }
            }
            // retrieve NOCBoard Statistics
            NocBoardResult board = NocBoardEngine.calculateBoard(
                    dashboard.getHostGroup(),
                    dashboard.getServiceGroup(),
                    dashboard,
                    downtimeContext);
            if (message != null) {
                board.setMessage(message);
            }
            String username = request.getRemoteUser();
            board.setUsername((username == null) ? "Guest" : username);
            StringWriter writer = new StringWriter();
            objectMapper.writeValue(writer, board);
            response.getWriter().println(writer);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            NocBoardResult result = new NocBoardResult();
            result.setSuccess(false);
            result.setMessage(e.getMessage());
            StringWriter writer = new StringWriter();
            objectMapper.writeValue(writer, result);
            response.getWriter().println(writer);
        }
    }

    protected void overrideColumnSetting(List<CheckedState> columns, String columnName) {
        for (CheckedState state : columns) {
            if (state.getName().equals(columnName)) {
                state.setChecked(false);
                break;
            }
        }
    }

    protected void addComment(ResourceRequest request, ResourceResponse response) {
        SimpleResult result = new SimpleResult();
        NagiosCommandProcessor nagiosCommand = new NagiosCommandProcessor();
        try {
            StringWriter writer = new StringWriter();
            drain(request.getReader(), writer);
            String json = writer.toString();
            ObjectMapper writeMapper = new ObjectMapper();
            NocBoardPostComment comment = writeMapper.readValue(json, NocBoardPostComment.class);
            String username = request.getRemoteUser();
            if (!comment.getCommentUser().equals(username)) {
                comment.setCommentUser(username);
            }
            ServiceClient serviceClient = new ServiceClient(WSClientConfiguration.getProperty(WSClientConfiguration.FOUNDATION_REST_ENDPOINT));
            CommentClient commentClient = new CommentClient(WSClientConfiguration.getProperty(WSClientConfiguration.FOUNDATION_REST_ENDPOINT));
            DtoService dtoService = serviceClient.lookup(comment.getService(), comment.getHost(), DtoDepthType.Deep);
            if (dtoService.getAppType().toLowerCase().equals("nagios")) {
                nagiosCommand.addServiceComment(comment.getCommentText(), comment.getHost(), comment.getCommentUser(), comment.getService());

            } else {
                DtoOperationResults commentResult = commentClient.addServiceComment(dtoService.getId(), comment.getCommentText(), comment.getCommentUser());
                result.setMessage(commentResult.getResults().get(0).getEntity());
            }

        } catch (Exception e) {
            log.error(e);
            result.setSuccess(false);
            result.setMessage(e.getMessage());
        }
        writeSimpleResult(response, result);
    }

    protected void deleteComment(ResourceRequest request, ResourceResponse response) {
        SimpleResult result = new SimpleResult();
        NagiosCommandProcessor nagiosCommand = new NagiosCommandProcessor();
        try {
            StringWriter writer = new StringWriter();
            drain(request.getReader(), writer);
            String json = writer.toString();
            ObjectMapper writeMapper = new ObjectMapper();
            NocBoardPostComment comment = writeMapper.readValue(json, NocBoardPostComment.class);
            ServiceClient serviceClient = new ServiceClient(WSClientConfiguration.getProperty(WSClientConfiguration.FOUNDATION_REST_ENDPOINT));
            CommentClient commentClient = new CommentClient(WSClientConfiguration.getProperty(WSClientConfiguration.FOUNDATION_REST_ENDPOINT));
            DtoService dtoService = serviceClient.lookup(comment.getService(), comment.getHost(), DtoDepthType.Deep);
            if (dtoService.getAppType().toLowerCase().equals("nagios")) {
                nagiosCommand.deleteServiceComment(comment.getCommentUser(), comment.getCommentID());
                result.setMessage("deleted comment: " + comment.getCommentID());
            } else {
                commentClient.deleteServiceComment(dtoService.getId(), Integer.parseInt(comment.getCommentID()));

            }

        } catch (Exception e) {
            log.error(e);
            result.setSuccess(false);
            result.setMessage(e.getMessage());
        }
        writeSimpleResult(response, result);
    }

    protected void postAck(ResourceRequest request, ResourceResponse response) {
        SimpleResult result = new SimpleResult();
        NagiosCommandProcessor nagiosCommand = new NagiosCommandProcessor();
        try {
            StringWriter writer = new StringWriter();
            drain(request.getReader(), writer);
            String json = writer.toString();
            ObjectMapper writeMapper = new ObjectMapper();
            NocBoardAck ack = writeMapper.readValue(json, NocBoardAck.class);
            String username = request.getRemoteUser();
            if (!ack.getAcknowledger().equals(username)) {
                ack.setAcknowledger(username);
            }
            ServiceClient serviceClient = new ServiceClient(WSClientConfiguration.getProperty(WSClientConfiguration.FOUNDATION_REST_ENDPOINT));
            DtoService dtoService = serviceClient.lookup(ack.getService(), ack.getHost(), DtoDepthType.Deep);
            if (dtoService.getMonitorStatus().toUpperCase().equals("OK")){
                result.setSuccess(false);
                result.setMessage("Cannot Acknowledge a service in an 'OK' state");

            }
            else if (dtoService.getAppType().toLowerCase().equals("nagios")) {
                nagiosCommand.processAck(ack.getAcknowledgeComment(), ack.getHost(), username, ack.getService(), true);
                result.setMessage("acknowledged for host/service: " + ack.getHost() + ":" + ack.getService());
            }
            else{

                serviceClient.acknowledge(dtoService, ack.getAcknowledger(), ack.getAcknowledgeComment());
                result.setMessage("acknowledged for host/service: " + ack.getHost() + ":" + ack.getService());
            }

        } catch (Exception e) {
            log.error(e);
            result.setSuccess(false);
            result.setMessage(e.getMessage());
        }
        writeSimpleResult(response, result);
    }

    protected void postNotification(ResourceRequest request, ResourceResponse response) {
        SimpleResult result = new SimpleResult();
        try {
            StringWriter writer = new StringWriter();
            drain(request.getReader(), writer);
            String json = writer.toString();
            ObjectMapper writeMapper = new ObjectMapper();
            NocBoardNotification notification = writeMapper.readValue(json, NocBoardNotification.class);
            // TODO: update notification in Foundation
            result.setMessage("notified for host/service: " + notification.getHost() + ":" + notification.getService());
        } catch (Exception e) {
            log.error(e);
            result.setSuccess(false);
            result.setMessage(e.getMessage());
        }
        writeSimpleResult(response, result);
    }

    public static final String SESSION_DOWNTIME_CONTEXT = "SESSION_DOWNTIME_CONTEXT";

    private DowntimeContext acquireContext(ResourceRequest request) throws DowntimeException {
        Boolean enableDowntime = NocConfiguration.getBooleanProperty(NocConfiguration.NOC_DOWNTIME_ENABLE);
        if (enableDowntime) {
            DowntimeService downtimeService = DowntimeServiceFactory.getServiceInstance();
            DowntimeContext context = (DowntimeContext) request.getPortletSession().getAttribute(SESSION_DOWNTIME_CONTEXT);
            if (context == null || !context.isLoggedOn()) {
                String username = NocConfiguration.getProperty(NocConfiguration.NOC_DOWNTIME_USERNAME);
                String password = NocConfiguration.getEncryptedProperty(NocConfiguration.NOC_DOWNTIME_PASSWORD);
                String overrideUrl = NocConfiguration.getProperty(NocConfiguration.NOC_DOWNTIME_URL_OVERRIDE);
                context = downtimeService.login(
                        (overrideUrl == null) ? buildBaseURL(request) : overrideUrl,
                        (username == null) ? "admin" : username,
                        (password == null) ? "admin" : password);
                request.getPortletSession().setAttribute(SESSION_DOWNTIME_CONTEXT, context);
            }
            return context;
        }
        return null;
    }
}
