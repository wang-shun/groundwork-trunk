/*
 * Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork) All rights
 * reserved. Use is subject to GroundWork commercial license terms.
 */

package com.groundworkopensource.portal.reports.common;

/**
 * This class defines constants to be used across the report portlets . TODO:
 * shall we define a common constants file for all portlet applicaions?
 * 
 * @author manish jain
 */
public class ReportConstants {
    /**
     * Protected Constructor - Rationale: Instantiating utility classes does not
     * make sense. Hence the constructors should either be private or (if you
     * want to allow sub-classing) protected. <br>
     * 
     * Refer to "HideUtilityClassConstructor" section in
     * http://checkstyle.sourceforge.net/config_design.html.
     */
    protected ReportConstants() {
        // prevents calls from subclass
        throw new UnsupportedOperationException();
    }

    /**
     * REPORT_DIR
     */
    public static final String REPORT_DIR = "ReportDir";

    /**
     * REPORT_EN_XML
     */
    public static final String REPORT_EN_XML = "report_en.xml";

    /**
     * FILE_OBJECT_ID
     */
    public static final String FILE_OBJECT_ID = "FileObjectId";

    /**
     * DISPALY_NAME
     */
    public static final String DISPLAY_NAME = "display-name";

    /**
     * REPORT_FILE
     */
    public static final String REPORT_FILE = "reportFile";

    /**
     * REPORT
     */
    public static final String REPORT = "Report";

    /**
     * BRANCH_CONTRACTED_ICON
     */
    public static final String BRANCH_CONTRACTED_ICON = "/images/tree_folder_open.gif";

    /**
     * BRANCH_EXPANDED_ICON
     */
    public static final String BRANCH_EXPANDED_ICON = "/images/tree_folder_close.gif";

    /**
     * BRANCH_LEAF_ICON
     */
    public static final String BRANCH_LEAF_ICON = "/images/tree_document.gif";

    /**
     * REPORTS_NAME_XML
     */
    public static final String REPORTS_NAME_XML = "reports.name.xml";

    /**
     * REPORT_URL_PART2
     */
    public static final String REPORT_URL_PART2 = "&__masterpage=true&__format=html&__toolbar=true";

    /**
     * REPORT_URL_PART1
     */
    public static final String REPORT_URL_PART1 = "/birtviewer/frameset?__report=";

    /**
     * COLAN
     */
    public static final String COLAN = ":";

    /**
     * HTTP
     */
    public static final String HTTP = "http://";

    /**
     * HTTPs
     */
    public static final String HTTPS = "https://";

    /**
     * REPORT_ID
     */
    public static final String REPORT_ID = "reportID";

    /**
     * JSCALL_STRING1
     */
    public static final String JSCALL_STRING1 = "document.getElementById('birtViewer').src ='";

    /**
     * JSCALL_STRING2
     */
    public static final String JSCALL_STRING2 = "';";

    /**
     * NULL_STRING
     */
    public static final String NULL_STRING = "null";

    /**
     * REPORT_SELECT_OPTION_VIEW
     */
    public static final String REPORT_SELECT_OPTION_VIEW = "viewReport";

    /**
     * REPORT_SELECT_OPTION_PUBLISH
     */
    public static final String REPORT_SELECT_OPTION_PUBLISH = "publishReport";

    /**
     * EMPTY_STRING
     */
    public static final String EMPTY_STRING = "";

    /**
     * Name of file list managed bean
     */
    public static final String FILE_LIST_BEAN = "fileListBean";

    /**
     * REPORT_FILE_EXTENSION
     */
    public static final String REPORT_FILE_EXTENSION = "rptdesign";

    /**
     * DIR_NAME
     */
    public static final String DIR_NAME = "Dirname";

    /**
     * SLASH
     */
    public static final String SLASH = "/";

    /**
     * SPACE
     */
    public static final String SPACE = " ";
    /**
     * Xml file Path
     */
    public static final String REPORT_XML_PATH = "/usr/local/groundwork/gwreports/";

    /**
     * secure.access.enabled property
     */
    public static final String SECURE_ACCESS_ENABLED = "secure.access.enabled";
}