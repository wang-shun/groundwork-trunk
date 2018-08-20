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

import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import java.io.IOException;
import java.io.StringWriter;

public class HostTutorialAjaxPortlet extends DashboardPortlet {

    public static final String TUTORIAL_APP_NAME = "monitor-dashboard";

    protected String getAppName() {
        return TUTORIAL_APP_NAME;
    }

    protected final static String VIEW_HOSTS = "/WEB-INF/view/hosts-jquery-tutorial.jsp";

    @Override
    public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        response.setContentType("text/html");
        if (authenticate(request, response)) {
            PortletRequestDispatcher dispatcher = getPortletContext().getRequestDispatcher(VIEW_HOSTS);
            dispatcher.include(request, response);
        }
    }

    protected void provideHeaders(RenderRequest request, RenderResponse response) {
        addStyleLink(response, "https://cdn.datatables.net/1.10.6/css/jquery.dataTables.css", "gwforge-datatable");
        addJavaScript(response, request.getContextPath() + "/app/scripts/jquery.min.js", "gwforge-jquery");
        addJavaScript(response, request.getContextPath() + "/app/scripts/jquery-noconflict.js", "gwforge-jquery-nc");
        addJavaScript(response, request.getContextPath() + "/app/scripts/jquery.dataTables.js", "gwforge-jquery-dt");
        addJavaScript(response, request.getContextPath() + "/app/scripts/jquery.cookie.min.js", "gwforge-jquery-cookie");
    }

    @Override
    public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
        String resourceID = request.getResourceID();
        if (resourceID == null) {
            response.addProperty(ResourceResponse.HTTP_STATUS_CODE, "400");
            response.getWriter().println(createError(400, "invalid resource id"));
            return;
        }
        if (resourceID.equals("writePrefs")) {
            StringWriter writer = new StringWriter();
            drain(request.getReader(), writer);
            String json = writer.toString();
            ObjectMapper writeMapper = new ObjectMapper();
            TutorialPrefs update = writeMapper.readValue(json, TutorialPrefs.class);
            request.getPreferences().setValue(PREFS_REFRESH_SECONDS , Integer.toString(update.getRows()));
            request.getPreferences().store();
        }
        TutorialPrefs prefs = new TutorialPrefs();
        prefs.setRows(Integer.parseInt(request.getPreferences().getValue(PREFS_ROWS, "10")));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        StringWriter writer = new StringWriter();
        objectMapper.writeValue(writer, prefs);
        response.getWriter().println(writer);
    }


}
