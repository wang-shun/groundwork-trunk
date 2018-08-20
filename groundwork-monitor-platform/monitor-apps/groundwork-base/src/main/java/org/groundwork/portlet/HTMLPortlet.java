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

package org.groundwork.portlet;

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

import javax.portlet.PortletConfig;
import javax.portlet.PortletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author Roger Ruttimann (rruttimann@gwos.com) GroundWOrk Open Source Inc.
 * 
 */
public class HTMLPortlet extends GenericPortlet
{

    /* INIT Parameter defining the URL */
    private static final String PARAM_URL = "URL";
	
	/**
	 * Logging
	 */
	private static Log logger = LogFactory.getLog(HTMLPortlet.class);
	
	/* Keep track of configured URL since it might change doing doView*/
    protected String configuredURL = null;
	

    /**
     * Default constructor
     */
    public HTMLPortlet() {

    }

    /**
     * Init phase of the portlet. Using it to read INIT params defined in the
     * portlet.xml
     * 
     * @param config
     * @throws PortletException
     */
    public void init(PortletConfig config) throws PortletException {
        super.init(config);

        // Get the INIT PARAMETERS for this portlet. 
        configuredURL = config.getInitParameter(PARAM_URL);
 
    }

    public void doView(RenderRequest request, RenderResponse response) {
        try {
            response.setContentType("text/html");
            PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher(configuredURL);
            prd.include(request, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}