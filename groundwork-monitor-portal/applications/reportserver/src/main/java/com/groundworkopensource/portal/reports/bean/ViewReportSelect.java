/*
 *  Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork)
 *  All rights reserved. Use is subject to GroundWork commercial license terms.
 */

package com.groundworkopensource.portal.reports.bean;

/**
 * The back-end bean for the selection of option for viewing and publishing
 * reports.
 * 
 * @author nitin_jadhav
 */

public class ViewReportSelect {

    /**
     * currently selected JSP page.
     */
    private String currentView;

    /**
     * constructor.
     */
    public ViewReportSelect() {
        super();
    }

    /**
     * constructor.
     * 
     * @param view
     */
    public ViewReportSelect(final String view) {
        currentView = view;
    }

    /**
     * setter.
     * 
     * @param currentView
     */
    public void setCurrentView(String currentView) {
        this.currentView = currentView;
    }

    /**
     * getter.
     * 
     * @return currentView
     */
    public String getCurrentView() {
        return currentView;
    }
}
