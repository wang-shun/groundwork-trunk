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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;

import com.groundworkopensource.portal.common.ApplicationType;
import com.groundworkopensource.portal.common.CommonConstants;
import com.groundworkopensource.portal.common.FilterAggregator;
import com.groundworkopensource.portal.common.PropertyUtils;
import com.groundworkopensource.portal.statusviewer.common.listener.JMSTopicConnection;
import com.groundworkopensource.portal.statusviewer.handler.ReferenceTreeMetaModel;
import com.groundworkopensource.portal.common.LocaleBean;
import com.groundworkopensource.portal.common.FacesUtils;
import  com.icesoft.faces.async.render.RenderManager;

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
     * (non-Javadoc)
     * 
     * @see javax.servlet.GenericServlet#init()
     */
    @Override
    public void init() throws ServletException {
    	FacesUtils.setServletContext(getServletContext());
        PropertyUtils.loadPropertiesIntoServletContext(
                ApplicationType.STATUS_VIEWER, getServletContext());

        // Initialize filters configuration
        FilterAggregator filterAggregator = FilterAggregator.getInstance();
        if (filterAggregator != null) {
            filterAggregator.init(getServletContext().getRealPath(
                    CommonConstants.FILTER_XML_NAME), getServletContext()
                    .getRealPath(CommonConstants.SCHEMA_NAME));
        } else {
            LOGGER
                    .warn("Received null instance of Filter Aggregator. Failed to initialize filters.");
        }
        LOGGER.info("Initializing RTMM Cache!");
        RenderManager renderManager = new RenderManager();
        getServletContext().setAttribute("renderManager",renderManager);
        JMSTopicConnection topicConn = new JMSTopicConnection();
        getServletContext().setAttribute("jmsTopicConnection",topicConn);
        LocaleBean localeBean = new LocaleBean(ApplicationType.STATUS_VIEWER);
        getServletContext().setAttribute("localeBean",localeBean);  
        ReferenceTreeMetaModel rtmm = new ReferenceTreeMetaModel();
        getServletContext().setAttribute("referenceTree",rtmm);   
    }
}
