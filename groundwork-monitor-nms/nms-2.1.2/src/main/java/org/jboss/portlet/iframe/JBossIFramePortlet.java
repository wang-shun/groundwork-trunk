/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.portlet.iframe;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSecurityException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.io.IOException;

/**
 * User: Chris Mills (millsy@jboss.com) Date: 04-Mar-2006 Time: 10:45:10
 */

public class JBossIFramePortlet extends GenericPortlet
{
    private static final String defaultURL = "http://www.jboss.com";
    private static final String defaultHeight = "200px";
    private static final String defaultWidth = "100%";
    private static final String defaultNonIFrameMessage = "Your browser does not support iframes";

    public void processAction(ActionRequest request, ActionResponse response)
            throws PortletException, PortletSecurityException, IOException {
        String op = request.getParameter("op");
        StringBuffer message = new StringBuffer(1024);
        if ((op != null) && (op.trim().length() > 0)) {
            if (op.equalsIgnoreCase("update")) {
                PortletPreferences prefs = request.getPreferences();
                String url = request.getParameter("url");
                String height = request.getParameter("height");
                String width = request.getParameter("width");
                String noIFrameMessage = request
                        .getParameter("noiframemessage");

                boolean px = false;
                boolean save = true;
                if ((url != null) && (height != null) && (width != null)
                        && (noIFrameMessage != null)) {
                    /*
                     * if(!url.startsWith("http://")) { save = false;
                     * message.append("URLs must start with 'http://'<br/>"); }
                     */

                    try {
                        if (height.endsWith("px")) {
                            height = height.substring(0, height.length() - 2);
                        }
                        Integer.parseInt(height);
                    } catch (NumberFormatException nfe) {
                        // Bad height value
                        save = false;
                        message.append("Height must be an integer<br/>");
                    }
                    try {
                        if (width.endsWith("px")) {
                            px = true;
                            width = width.substring(0, width.length() - 2);
                        } else if (width.endsWith("%")) {
                            width = width.substring(0, width.length() - 1);
                        }
                        Integer.parseInt(width);
                    } catch (NumberFormatException nfe) {
                        // Bad height value
                        save = false;
                        message.append("Width must be an integer<br/>");
                    }

                    if (save) {
                        prefs.setValue("iframeheight", height + "px");
                        prefs.setValue("iframewidth", px ? width + "px" : width
                                + "%");
                        prefs.setValue("iframeurl", url);
                        prefs.setValue("iframemessage", noIFrameMessage);
                        prefs.store();
                        response.setPortletMode(PortletMode.VIEW);
                        return;
                    }
                }
            } else if (op.equalsIgnoreCase("cancel")) {
                response.setPortletMode(PortletMode.VIEW);
                return;
            } else {
                message.append("Operation not found");
            }
        } else {
            message.append("Operation is null");
        }

        response.setRenderParameter("message", message.toString());
        response.setPortletMode(PortletMode.EDIT);
    }

    public void doView(RenderRequest request, RenderResponse response) {
        try {
            setRenderAttributes(request);
            response.setContentType("text/html");
            PortletRequestDispatcher prd = getPortletContext()
                    .getRequestDispatcher("/WEB-INF/iframe/iframe.jsp");
            prd.include(request, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doEdit(RenderRequest request, RenderResponse response)
            throws IOException, PortletException {
        setRenderAttributes(request);
        response.setContentType("text/html");
        response.setTitle("Edit");
        PortletRequestDispatcher prd = getPortletContext()
                .getRequestDispatcher("/WEB-INF/iframe/edit.jsp");
        prd.include(request, response);
    }

    private void setRenderAttributes(RenderRequest request) {
        PortletPreferences prefs = request.getPreferences();
        request.setAttribute("iframeurl", prefs.getValue("iframeurl",
        		JBossIFramePortlet.defaultURL));
        request.setAttribute("iframeheight", prefs.getValue("iframeheight",
        		JBossIFramePortlet.defaultHeight));
        request.setAttribute("iframewidth", prefs.getValue("iframewidth",
        		JBossIFramePortlet.defaultWidth));
        request.setAttribute("iframemessage", prefs.getValue("iframemessage",
        		JBossIFramePortlet.defaultNonIFrameMessage));
    }
}
