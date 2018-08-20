/*
 * 
 * Copyright 2007 GroundWork Open Source, Inc. ("GroundWork") All rights
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

package com.groundworkopensource.portal.statusviewer.servlet;

import com.groundworkopensource.portal.common.ApplicationType;
import com.groundworkopensource.portal.common.CommonConstants;
import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.common.FilterAggregator;
import com.groundworkopensource.portal.common.LocaleBean;
import com.groundworkopensource.portal.common.PropertyUtils;
import com.groundworkopensource.portal.statusviewer.common.listener.JMSTopicConnection;
import com.groundworkopensource.portal.statusviewer.handler.ReferenceTreeMetaModel;
import com.icesoft.faces.async.render.RenderManager;
import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.impl.WSClientConfiguration;
import org.groundwork.rs.client.PortalCustomGroupMigration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.util.Properties;

/**
 * StatusViewerInitServler for loading status.properties file.
 * 
 * @author swapnil_gujrathi
 * 
 */
public class StatusViewerInitServlet extends HttpServlet {
    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logger
     */
    private static final Logger LOGGER = Logger
            .getLogger(StatusViewerInitServlet.class.getName());

    /**
     * Portal custom groups migration enabled property name
     */
    private static final String PORTAL_CUSTOM_GROUPS_MIGRATION_ENABLED_PROP_NAME = "portal.custom.groups.migration.enabled";

    /**
     * Portal custom groups migration dryrun property name
     */
    private static final String PORTAL_CUSTOM_GROUPS_MIGRATION_DRYRUN_PROP_NAME = "portal.custom.groups.migration.dryrun";

    private ReferenceTreeMetaModel rtmm;

    /**
     * (non-Javadoc)
     * 
     * @see javax.servlet.GenericServlet#init()
     */
    @Override
    public void init() throws ServletException {
        // initialize servlet context and properties
    	FacesUtils.setServletContext(getServletContext());
        PropertyUtils.loadPropertiesIntoServletContext(
                ApplicationType.STATUS_VIEWER, getServletContext());
        Properties properties = (Properties) getServletContext().
                getAttribute(ApplicationType.STATUS_VIEWER.getContextAttributeName());

        // Initialize filters configuration
        FilterAggregator filterAggregator = FilterAggregator.getInstance();
        if (filterAggregator != null) {
            filterAggregator.init(getServletContext().getRealPath(
                    CommonConstants.FILTER_XML_NAME), getServletContext()
                    .getRealPath(CommonConstants.SCHEMA_NAME));
        } else {
            LOGGER.warn("Received null instance of Filter Aggregator. Failed to initialize filters.");
        }

        // migrate portal custom groups to custom group categories
        boolean migratePortalCustomGroups =
                Boolean.parseBoolean(properties.getProperty(PORTAL_CUSTOM_GROUPS_MIGRATION_ENABLED_PROP_NAME, "false"));
        boolean migratePortalCustomGroupsDryrun =
                Boolean.parseBoolean(properties.getProperty(PORTAL_CUSTOM_GROUPS_MIGRATION_DRYRUN_PROP_NAME, "false"));
        try {
            if (migratePortalCustomGroups) {
                // get configured client deployment urls
                String portalDeploymentUrl = properties.getProperty(CommonConstants.PORTAL_EXTN_RESTEASY_URL_KEY);
                if (portalDeploymentUrl == null) {
                    portalDeploymentUrl = FacesUtils.getContextParam(CommonConstants.PORTAL_EXTN_RESTEASY_URL_KEY);
                }
                String deploymentUrl = WSClientConfiguration.getProperty(WSClientConfiguration.FOUNDATION_REST_ENDPOINT);

                // verify migration required and perform migration if so
                LOGGER.info("Checking portal custom groups migration.");
                if (PortalCustomGroupMigration.required(portalDeploymentUrl, deploymentUrl)) {
                    LOGGER.info("Performing portal custom groups migration.");
                    if (PortalCustomGroupMigration.perform(portalDeploymentUrl, deploymentUrl,
                            migratePortalCustomGroupsDryrun)) {
                        LOGGER.info("Portal custom groups migrated successfully. The " +
                                PORTAL_CUSTOM_GROUPS_MIGRATION_ENABLED_PROP_NAME +
                                " property in " + ApplicationType.STATUS_VIEWER.getDefaultPropertiesPath() +
                                " can be set to 'false' to disable migration overhead.");
                    } else {
                        LOGGER.info("Portal custom groups migration failed.");
                    }
                } else {
                    LOGGER.info("Portal custom groups migration not required. The " +
                            PORTAL_CUSTOM_GROUPS_MIGRATION_ENABLED_PROP_NAME +
                            " property in " + ApplicationType.STATUS_VIEWER.getDefaultPropertiesPath() +
                            " can be set to 'false' to disable migration overhead.");
                }
            }
        } catch (Exception e) {
            LOGGER.error("Unexpected portal custom groups migration exception: "+e, e);
        }

        // initialize context beans
        RenderManager renderManager = new RenderManager();
        RenderManager.setServletConfig(getServletConfig());
        getServletContext().setAttribute("renderManager",renderManager);
        JMSTopicConnection topicConn = new JMSTopicConnection();
        getServletContext().setAttribute("jmsTopicConnection",topicConn);
        LocaleBean localeBean = new LocaleBean(ApplicationType.STATUS_VIEWER);
        getServletContext().setAttribute("localeBean",localeBean);  
        rtmm = new ReferenceTreeMetaModel();
        getServletContext().setAttribute("referenceTree",rtmm);   
    }


    @Override
    public void destroy() {
        rtmm.close();
    }

}
