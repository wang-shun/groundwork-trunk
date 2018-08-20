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
import com.groundwork.dashboard.portlets.dto.NocBoardResult;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Enumeration;

public class GroundworkDashboardPortlet extends DashboardPortlet {

    public static final String GW_APP_NAME = "monitor-dashboard";

    protected String getAppName() {
        return GW_APP_NAME;
    }

    public static final boolean DEV_MODE = false;

    protected void provideHeaders(RenderRequest request, RenderResponse response) {

        if (!DEV_MODE) {
            //addStyleLink(response, request.getContextPath() + "/wro/DASHBOARD_CSS.css", GW_DASHBOARD_CSS_ID);
            addStyleLink(response, request.getContextPath() + "/app/monitor-dashboard-skin.css", GW_DASHBOARD_CSS_ID);
            addJavaScript(response, request.getContextPath() + "/wro/DASHBOARD_LIB_JS.js", "gwmd_dashboard_lib");
            addJavaScript(response, request.getContextPath() + "/wro/DASHBOARD_GW_JS.js", "gwmd_dashboard_gw");
        }
        else {
            addStyleLink(response, request.getContextPath() + "/app/monitor-dashboard-skin.css", GW_DASHBOARD_CSS_ID);
            addJavaScript(response, request.getContextPath() + "/wro/DASHBOARD_LIB_JS.js", "gwmd_dashboard_lib");
            addJavaScript(response, request.getContextPath() + "/app/scripts/TextMessages.js", "gwmd_TextMessage");
            addJavaScript(response, request.getContextPath() + "/app/scripts/ServerService.js", "gwmd_ServerService");
//            addJavaScript(response, request.getContextPath() + "/app/scripts/services.js", "gwmd_services");
            addJavaScript(response, request.getContextPath() + "/app/scripts/RestApiService.js", "gwmd_services");
            addJavaScript(response, request.getContextPath() + "/app/scripts/PortletService.js", "gwmd_PortletService");
            addJavaScript(response, request.getContextPath() + "/app/scripts/app.js", "gwmd_app");
            addJavaScript(response, request.getContextPath() + "/app/scripts/controllers/controller.js", "gwmd_controllers");
            addJavaScript(response, request.getContextPath() + "/app/scripts/controllers/InfrastructureController.js", "gwmd_infrastructure");
            addJavaScript(response, request.getContextPath() + "/app/scripts/controllers/EventsController.js", "gwmd_events");
            addJavaScript(response, request.getContextPath() + "/app/scripts/controllers/TopController.js", "gwmd_top");
            addJavaScript(response, request.getContextPath() + "/app/scripts/controllers/NocBoardController.js", "gwmd_noc");
            addJavaScript(response, request.getContextPath() + "/app/scripts/controllers/NocBoardConfigController.js", "gwmd_nocconfig");
            addJavaScript(response, request.getContextPath() + "/app/scripts/controllers/MonitorEditController.js", "gwmd_monitoredit");
            addJavaScript(response, request.getContextPath() + "/app/scripts/controllers/AuditLogController.js", "gwmd_auditlog");
            addJavaScript(response, request.getContextPath() + "/app/scripts/controllers/AuditLogEditController.js", "gwmd_auditlogedit");
            addJavaScript(response, request.getContextPath() + "/app/scripts/controllers/HostIdentitiesController.js", "gwmd_hostidentities");
            addJavaScript(response, request.getContextPath() + "/app/scripts/controllers/CategoriesController.js", "gwmd_categories");
            addJavaScript(response, request.getContextPath() + "/app/scripts/controllers/BlackListController.js", "gwmd_blacklist");
            addJavaScript(response, request.getContextPath() + "/app/scripts/controllers/DeviceTemplatesController.js", "gwmd_devicetemplates");
            addJavaScript(response, request.getContextPath() + "/app/scripts/directives.js", "gwmd_directives");
            addJavaScript(response, request.getContextPath() + "/app/scripts/filters.js", "gwmd_filters");
        }
    }

    private void debugCookies(ResourceRequest resourceRequest) {
        HttpServletRequest request = getServletRequest(resourceRequest);
        Cookie[] cookies = request.getCookies();
        String scheme = request.getScheme();
        int port = request.getServerPort();
        String serverName = request.getServerName();
        System.out.println(String.format("%s://%s:%d", scheme, serverName, port));
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            System.out.println("header: " + headerName);
            Enumeration<String> headers = request.getHeaders(headerName);
            while (headers.hasMoreElements()) {
                String headerValue = headers.nextElement();
                System.out.println("---- " + headerValue);
            }
        }
        System.out.println("--- cookies");
        for (Cookie c : cookies) {
            System.out.println(debugCookie(c));
        }
    }

    private String debugCookie(Cookie c) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("cookie: ");
        buffer.append(c.getName());
        buffer.append(", ");
        buffer.append(c.getValue());
        buffer.append(", ");
        buffer.append(c.getPath());
        buffer.append(", ");
        buffer.append(c.getDomain());
        buffer.append(", ");
        buffer.append(c.getMaxAge());
        return buffer.toString();
    }

    protected String buildBaseURL(ResourceRequest resourceRequest) {
        HttpServletRequest request = getServletRequest(resourceRequest);
        StringBuffer buffer = new StringBuffer(request.getScheme());
        buffer.append("://").append(request.getServerName());
        if (request.getServerPort() != 443 && request.getServerPort() != 80) {
            buffer.append(":").append(request.getServerPort());
        }
        return buffer.toString();
    }

    protected String buildURL(ResourceRequest resourceRequest, String path) {
        HttpServletRequest request = getServletRequest(resourceRequest);
        StringBuffer buffer = new StringBuffer(request.getScheme());
        buffer.append("://").append(request.getServerName());
        if (request.getServerPort() != 443 && request.getServerPort() != 80) {
            buffer.append(":").append(request.getServerPort());
        }
        if (!path.startsWith("/")) {
            buffer.append("/");
        }
        buffer.append(path);
        return buffer.toString();
    }

    protected void writeSimpleResult(ResourceResponse response, Object result) {
        try {
            StringWriter writer = new StringWriter();
            ObjectMapper writeMapper = new ObjectMapper();
            writeMapper.writeValue(writer, result);
            response.getWriter().println(writer);
        } catch (Exception e) {
            log.error(e);
        }
    }

    protected boolean validateResourceRequest(ResourceRequest request, ResourceResponse response, String resourceID) throws PortletException, IOException {
        if (resourceID == null) {
            NocBoardResult result = new NocBoardResult();
            result.setSuccess(false);
            result.setMessage("Invalid Resource");
            writeSimpleResult(response, result);
            return false;
        }
        if (!authenticate(request, response)) {
            NocBoardResult result = new NocBoardResult();
            result.setSuccess(false);
            result.setMessage("Failed to authenticate. Cannot connect to Foundation Server");
            writeSimpleResult(response, result);
            return false;
        }
        return true;
    }

}
