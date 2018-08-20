package com.groundworkopensource.portal.statusviewer.portlet;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.groundworkopensource.portal.common.BasePortlet;
import com.groundworkopensource.portal.common.EditPrefsBean;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.DashboardEditPrefConstants;
import com.groundworkopensource.portal.statusviewer.common.NodeType;
import com.groundworkopensource.portal.statusviewer.common.PortletUtils;
import com.groundworkopensource.portal.statusviewer.common.TimeIntervalEnumEE;

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

/**
 * Performance Measurement Portlet. This portlet is part of "Host View" and
 * "Service View"
 * 
 * @author rashmi_tambe
 */
public class PerfMeasurementPortletEE extends BasePortlet {

    /**
     * Time filter Preference
     */
    private static final String TIMEPREF = "timepref";
    /**
     * PERFORMANCE_MEAUREMENT_PORTLET_TITLE.
     */
    private static final String PERFORMANCE_MEAUREMENT_PORTLET_TITLE = "Performance Measurement";

    /**
     * Date format String
     */
    private static final String DATE_FORMAT_24_HR = "MM/dd/yyyy H:mm";
    /**
     * custom date format
     */
    private SimpleDateFormat custDateFormat = new SimpleDateFormat(
            DATE_FORMAT_24_HR);

    /**
     * (non-Javadoc)
     * 
     * @see com.groundworkopensource.portal.common.BasePortlet#doView(javax.portlet.RenderRequest,
     *      javax.portlet.RenderResponse)
     */
    @Override
    protected void doView(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        // Set the portlet title.
        response.setTitle(PortletUtils.getPortletTitle(request,
                PERFORMANCE_MEAUREMENT_PORTLET_TITLE, true));

        super.setViewPath(Constant.PERF_MEASUREMENT_PATH_EE);
        super.doView(request, response);
    }

    /**
     * (non-Javadoc)
     * 
     * @see javax.portlet.GenericPortlet#processAction(javax.portlet.ActionRequest,
     *      javax.portlet.ActionResponse)
     */
    @Override
    public void processAction(ActionRequest request, ActionResponse response)
            throws PortletException, IOException {
        Map<String, String> requestPreferenceParamsMap = DashboardEditPrefConstants
                .getRequestPreferenceParamsMap(NodeType.SERVICE);
        requestPreferenceParamsMap.put(TIMEPREF, TIMEPREF);
        requestPreferenceParamsMap
                .put("custStartDatePref", "custStartDatePref");
        requestPreferenceParamsMap.put("custEndDatePref", "custEndDatePref");
        // call processAction() of BasePortlet.
        super.processAction(request, response, requestPreferenceParamsMap);
    }

    /**
     * This method is Responsible for editing preferences of host statistics
     * portlet
     * 
     * @param request
     * @param response
     * @throws PortletException
     * @throws IOException
     */
    @Override
    protected void doEdit(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        response.setTitle("Edit Performance Measurement Preferences");
        List<EditPrefsBean> editPreferences = DashboardEditPrefConstants
                .getEditPreferences(NodeType.SERVICE);
        editPreferences.add(new EditPrefsBean(TIMEPREF,
                TimeIntervalEnumEE.TODAY.getValue(), TIMEPREF, true, false));
        Calendar calendar = Calendar.getInstance();
        String defaultCustEndDate = custDateFormat.format(calendar.getTime());
        SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("MM/dd/yyyy");
        String defaultCustStartDate = dateOnlyFormat.format(calendar.getTime())
                + " 00:00";
        editPreferences.add(new EditPrefsBean("custStartDatePref",
                defaultCustStartDate, "custStartDatePref", true, false));
        editPreferences.add(new EditPrefsBean("custEndDatePref",
                defaultCustEndDate, "custEndDatePref", true, false));
        // call doEditPref() of BasePortlet.
        super.doEditPref(request, response, editPreferences,
                "/jsp/PerfMeasurementPrefEE.jsp");
    }

}
