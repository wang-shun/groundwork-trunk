/*
 * Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork) All rights
 * reserved. Use is subject to GroundWork commercial license terms.
 */

package com.groundworkopensource.portal.reports.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import com.groundworkopensource.portal.common.ApplicationType;
import com.groundworkopensource.portal.common.PropertyUtils;
import com.groundworkopensource.portal.reports.common.XMLGenerate;
import com.groundworkopensource.portal.common.LocaleBean;
import com.groundworkopensource.portal.common.FacesUtils;

/**
 * ReportViewerInitServler for loading report-viewer.properties file.
 * 
 * @author swapnil_gujrathi
 * 
 */
public class ReportViewerInitServlet extends HttpServlet {
    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * (non-Javadoc)
     * 
     * @see javax.servlet.GenericServlet#init()
     */
    public void init() throws ServletException {
        // application type = Report Viewer
    	FacesUtils.setServletContext(getServletContext());
    	LocaleBean localeBean = new LocaleBean(ApplicationType.REPORT_VIEWER);
        getServletContext().setAttribute("localeBean",localeBean);
        PropertyUtils.loadPropertiesIntoServletContext(
                ApplicationType.REPORT_VIEWER, getServletContext());
        XMLGenerate xmlGenerate = new XMLGenerate();
        xmlGenerate.generateXML();

    }

}
