/*
 *  Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork)
 *  All rights reserved. Use is subject to GroundWork commercial license terms.
 */

package com.groundworkopensource.portal.reports.bean;

import javax.faces.event.ValueChangeEvent;

import com.icesoft.faces.async.render.SessionRenderer;

import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.reports.common.FacesUtils;
import com.groundworkopensource.portal.reports.common.IPCUtils;
import com.groundworkopensource.portal.reports.common.ReportConstants;

/**
 * The back-end controller bean for the selection of option for viewing and
 * publishing reports.
 * 
 * @author nitin_jadhav
 */

public class ViewReportSelectBean {

	/**
	 * constructor.
	 * @throws GWPortalException 
	 */
	public ViewReportSelectBean() throws GWPortalException {
		ViewReportSelect select = new ViewReportSelect(
				ReportConstants.REPORT_SELECT_OPTION_VIEW);
		select.setCurrentView(ReportConstants.REPORT_SELECT_OPTION_VIEW);
		setcurrentReportView(select);
	}

	/**
	 * Name of renderGroup to be rendered, for AJAX PUSH.
	 */
	private String groupName;

	/**
	 * session variable.
	 */

	private static final String CURRENT_VIEW_KEY = "org.iceface.current.employee";

	/**
	 * This method is called when user changes option in report operation
	 * selection drop down menu.
	 * 
	 * @param event
	 * @throws GWPortalException 
	 */
	public void changeReportView(final ValueChangeEvent event) throws GWPortalException {

		String view = (String) event.getNewValue();
		ViewReportSelect select = new ViewReportSelect(view);
		setcurrentReportView(select);

		ReportTreeBean treeBean = (ReportTreeBean) FacesUtils
				.getManagedBean("reportTreeBean");
		treeBean.refreshTree(view);

	}

	/**
	 * @return ViewReportSelect
	 * @throws GWPortalException 
	 */
	public ViewReportSelect getcurrentReportView() throws GWPortalException {
		return (ViewReportSelect) IPCUtils
				.getApplicationAttribute(CURRENT_VIEW_KEY);
	}

	/**
	 * @param view
	 * @throws GWPortalException 
	 */
	public void setcurrentReportView(final ViewReportSelect view) throws GWPortalException {
		if (groupName == null) {
			groupName = IPCUtils.getSessionID();
			SessionRenderer.addCurrentSession(groupName);
		}
		IPCUtils.setApplicationAttribute(CURRENT_VIEW_KEY, view);
		SessionRenderer.render(groupName);
	}

}
