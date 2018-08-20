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
import com.groundwork.dashboard.portlets.dto.TopPrefs;

import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import java.io.IOException;
import java.io.StringWriter;

public class TopDashboardPortlet extends GroundworkDashboardPortlet {

    protected static final String PREFS_SERVICE_LABELS = "serviceLabels";
    protected static final String PREFS_SERVICE_NAMES = "serviceNames";
    protected static final String PREFS_SERVICE = "service";
    protected static final String REGEX_LABEL_SPLITTER = "\\s*,\\s*";

    @Override
    protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        response.setContentType("text/html");
        setTitle(request, response);
        super.doView(request, response);
    }

    @Override
    protected void doEdit(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        response.setContentType("text/html");
        setTitle(request, response);
        if (authenticate(request, response)) {
            PortletRequestDispatcher dispatcher = null;
            String url = request.getPreferences().getValue(PREFS_EDIT, "/app/views/top-edit.jsp" );
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
        if (resourceID.equals("writePrefs")) {
            StringWriter writer = new StringWriter();
            drain(request.getReader(), writer);
            String json = writer.toString();
            ObjectMapper writeMapper = new ObjectMapper();
            TopPrefs update = writeMapper.readValue(json, TopPrefs.class);
            request.getPreferences().setValue(PREFS_ROWS, Integer.toString(update.getRows()));
            request.getPreferences().setValue(PREFS_SERVICE, update.getService());
            request.getPreferences().setValue(PREFS_REFRESH_SECONDS, Integer.toString(update.getRefreshSeconds()));
            request.getPreferences().store();
        }
        TopPrefs prefs = new TopPrefs();
        String labels = request.getPreferences().getValue(PREFS_SERVICE_LABELS, "a,b");
        String[] labelsArray = labels.split(REGEX_LABEL_SPLITTER);
        String serviceNames = request.getPreferences().getValue(PREFS_SERVICE_NAMES, "c,d");
        String[] namesArray = serviceNames.split(REGEX_LABEL_SPLITTER);
        prefs.setRows(Integer.parseInt(request.getPreferences().getValue(PREFS_ROWS, "5")));
        prefs.setService(request.getPreferences().getValue(PREFS_SERVICE, ""));
        prefs.setServiceNames(namesArray);
        prefs.setServiceLabels(labelsArray);
        prefs.setRefreshSeconds(Integer.parseInt(request.getPreferences().getValue(PREFS_REFRESH_SECONDS, "60")));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        StringWriter writer = new StringWriter();
        objectMapper.writeValue(writer, prefs);
        response.getWriter().println(writer);
    }

    protected void setTitle(RenderRequest request, RenderResponse response) {
        String service = request.getPreferences().getValue(PREFS_SERVICE, "none");
        String labels = request.getPreferences().getValue(PREFS_SERVICE_LABELS, "a,b");
        String[] labelsArray = labels.split(REGEX_LABEL_SPLITTER);
        String serviceNames = request.getPreferences().getValue(PREFS_SERVICE_NAMES, "c,d");
        String[] namesArray = serviceNames.split(REGEX_LABEL_SPLITTER);
        String title = null;
        int ix = 0;
        for (String name : namesArray) {
            if (name.equals(service)) {
                title = labelsArray[ix];
                break;
            }
            ix++;
        }
        response.setTitle((title == null) ? "Top" : title);
    }
}
