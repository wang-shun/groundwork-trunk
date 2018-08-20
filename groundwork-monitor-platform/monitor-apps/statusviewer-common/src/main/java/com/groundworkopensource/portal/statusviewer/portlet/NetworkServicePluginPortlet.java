/*
 * Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork) All rights
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
 * 
 * dname.pl - This is a utility script to clean up the foundation database for
 * device display names and identification fields that were inconsistently fed
 * into the database. This can cause some issues in the display for the event
 * console, especially when upgrading an older database. Use in consultation
 * with GroundWork Support!
 */

package com.groundworkopensource.portal.statusviewer.portlet;

import com.groundworkopensource.portal.common.GroundworkInfoReader;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.io.IOException;

/**
 * NetworkServicePluginPortlet Portlet Class
 */
public class NetworkServicePluginPortlet extends GenericPortlet {
    protected static final String GW_INFO = "gwInstallInfo";

    // /**
    // * LOGGER
    // */
    // private static final Logger LOGGER = Logger
    // .getLogger(NetworkServicePluginPortlet.class.getName());

    /**
     * (non-Javadoc)
     * 
     * @see javax.portlet.GenericPortlet#processAction(javax.portlet.ActionRequest,
     *      javax.portlet.ActionResponse)
     */
    @Override
    public void processAction(ActionRequest request, ActionResponse response)
            throws PortletException, IOException {

    }

    /**
     * (non-Javadoc)
     * 
     * @param request
     * @param response
     * @throws IOException
     * @throws PortletException
     */
    @Override
    public void doView(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {

        String info = (String)request.getPortletSession(true).getAttribute(GW_INFO);
        if (info == null) {
            info = GroundworkInfoReader.readInfoHTML();
            request.getPortletSession().setAttribute(GW_INFO, info);
        }
        request.setAttribute(GW_INFO, info);
        response.setContentType("text/html");
        PortletRequestDispatcher dispatcher = getPortletContext()
                .getRequestDispatcher("/jsp/networkService.jsp");
        dispatcher.include(request, response);
    }

    /**
     * (non-Javadoc)
     * 
     * @param request
     * @param response
     * @throws IOException
     * @throws PortletException
     */
    @Override
    public void doEdit(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        // response.setContentType("text/html");
        //
        // PortletRequestDispatcher dispatcher = getPortletContext()
        // .getRequestDispatcher("/edit.jsp");
        // dispatcher.include(request, response);
    }

    /**
     * (non-Javadoc)
     * 
     * @param request
     * @param response
     * @throws IOException
     * @throws PortletException
     */
    @Override
    public void doHelp(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        //
        // response.setContentType("text/html");
        // PortletRequestDispatcher dispatcher = getPortletContext()
        // .getRequestDispatcher("/help.jsp");
        // dispatcher.include(request, response);
    }

    // @Override
    // public void init() {
    // // NetworkServiceConfig.load();
    // }
}
