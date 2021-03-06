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
package com.groundworkopensource.portal.statusviewer.portlet;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.groundworkopensource.portal.common.BasePortlet;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.PortletUtils;

/**
 * This portlet displays the filter options for hosts and services
 * 
 * @author mridu_narang
 * 
 */

public class CommentsPortlet extends BasePortlet {

    /**
     * Title of portlet
     */
    public static final String COMMENTS_TITLE = "Comments";

    /**
     * (non-Javadoc).
     * 
     * @see javax.portlet.GenericPortlet#processAction(javax.portlet.ActionRequest,
     *      javax.portlet.ActionResponse)
     */
    @Override
    public void processAction(ActionRequest request, ActionResponse response)
            throws PortletException, IOException {

        /*
         * Method is called in response to a user action such as clicking a
         * hyperlink or submitting a form. In this method a portlet may modify
         * its own state as well as persistent information it.
         */

    }

    /**
     * (non-Javadoc).
     * 
     * @see javax.portlet.GenericPortlet#doView(RenderRequest
     *      request,RenderResponse response)
     */
    @Override
    protected void doView(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        super.setViewPath(Constant.COMMENTS_VIEW_PATH);

        // Set the portlet title.
        response.setTitle(PortletUtils.getPortletTitle(request, COMMENTS_TITLE,
                true));

        super.doView(request, response);
    }
}
