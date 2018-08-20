package com.groundworkopensource.portal.statusviewer.portlet;

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

import java.io.IOException;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.groundworkopensource.portal.common.BasePortlet;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.PortletUtils;

/**
 * Date TimePortlet - Displays current date and time formatted according to the
 * user's locale (specified within their JBoss Portal user preferences), or the
 * default locale for the system if none is selected.
 * 
 * @author shivangi_walvekar
 * 
 */
public class DateTimePortlet extends BasePortlet {

    /**
     * Constant String for portlet title
     */
    private static final String DATE_TIME_PORTLET_TITLE = "Date-Time";

    /**
     * (non-Javadoc)
     * 
     * @see com.groundworkopensource.portal.common.BasePortlet#doView(javax.portlet.RenderRequest,
     *      javax.portlet.RenderResponse)
     */
    @Override
    protected void doView(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        super.setViewPath(Constant.DATE_TIME_PORTLET_PATH);
        response.setTitle(PortletUtils.getPortletTitle(request,
                DATE_TIME_PORTLET_TITLE, false, false));
        super.doView(request, response);
    }
}
