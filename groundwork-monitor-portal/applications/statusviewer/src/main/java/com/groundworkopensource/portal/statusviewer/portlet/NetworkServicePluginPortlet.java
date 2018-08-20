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

import java.io.IOException;
import java.util.ArrayList;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.groundworkopensource.portal.statusviewer.bean.networkservice.NetworkServiceConfig;
import com.groundworkopensource.portal.statusviewer.bean.networkservice.NetworkServiceDatabase;
import com.groundworkopensource.portal.statusviewer.bean.networkservice.Notification;

/**
 * NetworkServicePluginPortlet Portlet Class
 */
public class NetworkServicePluginPortlet extends GenericPortlet {

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

        // LOGGER.error("%%%%%%%%%%%%%% In ProcessAction .... ");
        String action = request.getParameter("ns_update");
        String notificationId = request.getParameter("ns_update_id");
        String reloadConfig = request.getParameter("reload_config");

        PortletPreferences prefs = request.getPreferences();
        // find all unread/all
        // update notification with specific id

        if (reloadConfig != null && reloadConfig.equalsIgnoreCase("reload")) {
            NetworkServiceConfig.load();
        }

        if (action == null) {
            action = "";
        }
        if (notificationId != null) {
            Integer id = Integer.parseInt(notificationId);
            NetworkServiceDatabase networkServiceDatabaseInstance = NetworkServiceDatabase
                    .getInstance();
            response.setRenderParameter("ns_update_id", notificationId);
            if (action.equalsIgnoreCase("unread")) {
                networkServiceDatabaseInstance.markNotificationAsUnread(id);
            } else if (action.equalsIgnoreCase("read")) {
                networkServiceDatabaseInstance.markNotificationAsRead(id);
            } else if (action.equalsIgnoreCase("archived")) {
                networkServiceDatabaseInstance.markNotificationAsArchived(id);
            } else if (action.equalsIgnoreCase("not_archived")) {
                networkServiceDatabaseInstance
                        .markNotificationAsNotArchived(id);
            }
        }
        if (action.equalsIgnoreCase("show_all")) {
            prefs.setValue("show_mode", "all");
        } else if (action.equalsIgnoreCase("show_unread")) {
            prefs.setValue("show_mode", "unread");
        } else {
            response.setPortletMode(PortletMode.VIEW);
            // prefs.setValue("show_mode", "unread");
        }

        prefs.store();
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

        PortletPreferences prefs = request.getPreferences();
        NetworkServiceDatabase networkServiceDatabaseInstance = NetworkServiceDatabase
                .getInstance();
        prefs.setValue("network_service_plugin_info",
                networkServiceDatabaseInstance.getInstallationInformation());

        if (NetworkServiceConfig.isActivated()) {
            ArrayList<Notification> notifications = networkServiceDatabaseInstance
                    .findNotifications(prefs.getValue("show_mode", "unread"));
            if (notifications == null) {
                prefs.setValue("db_info", NetworkServiceConfig
                        .get("ns.msg.db_connection_problems"));
            } else {
                prefs.setValue("db_info", NetworkServiceConfig
                        .get("ns.msg.no_notifications"));
            }
            // NotificationsSet result = new NotificationsSet(notifications);
            // request.setAttribute("results", result);
        }
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
