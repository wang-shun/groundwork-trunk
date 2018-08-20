/*
 * Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork) All rights
 * reserved. Use is subject to GroundWork commercial license terms.
 */

package com.groundworkopensource.webapp.console;

import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.icesoft.faces.component.datapaginator.DataPaginator;
import com.icesoft.faces.component.ext.HtmlCommandLink;
import com.icesoft.faces.component.ext.HtmlDataTable;
import com.icesoft.faces.component.ext.HtmlGraphicImage;
import com.icesoft.faces.component.ext.HtmlOutputLink;
import com.icesoft.faces.component.ext.HtmlOutputText;
import com.icesoft.faces.component.ext.HtmlPanelGrid;
import com.icesoft.faces.component.ext.HtmlPanelGroup;
import com.icesoft.faces.component.ext.RowSelector;
import com.icesoft.faces.component.ext.RowSelectorEvent;
import com.icesoft.faces.component.ext.UIColumn;
import com.icesoft.faces.component.menubar.MenuItem;
import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.model.impl.EntityTypeProperty;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.Sort;

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * The DataTableBean contains the data to build the icefaces:datatable
 * structure. It is renderable and uses the icefaces server initiated rendering
 * architecture. It is also a message listener for the events from the
 * foundation.Datatable is updated incrementally if there is any new event
 * message showing up.
 *
 * @author ashanmugam
 * @since 5.2
 */
public class DataTableBean extends PagedListDataModel {

    /**
     * Stored version of UIViewRoot, which is useful to generate unique IDs for
     * elements. Stored version is used when the FacesdContext object is
     * unavailable.
     */
    private UIViewRoot viewRoot;

    /**
     * PARAM_NAME.
     */
    private static final String PARAM_NAME = "name";

    /**
     * APP_TYPE.
     */
    private static final String APP_TYPE = "apptype";

    /**
     * IMGPATH_SERVICES_POPUP.
     */
    private static final String IMGPATH_SERVICES_POPUP = "/images/window_gear.png";

    private static final String IMGPATH_ACKNOWLEDGED = "/assets/icons/acknowledged_event.gif";

    /**
     * IMGPATH_DYNA_POPUP.
     */
    private static final String IMGPATH_DYNA_POPUP = "/xmlhttp/css/xp/css-images/tree_nav_top_open_no_siblings.gif";

    /**
     * The events.
     */
    private EventBean[] events;

    /**
     * Enable log4j for DataTableBean class.
     */
    public static Logger logger = Logger.getLogger(DataTableBean.class
            .getName());

    /**
     * The multiple selection.
     */
    private boolean multipleSelection = true;

    /**
     * The selected rows.
     */
    private List<EventBean> selectedRows = null;

    /**
     * The column names.
     */
    private ArrayList<String> columnNames = new ArrayList<String>();

    /**
     * The column labels.
     */
    private ArrayList<String> columnLabels = new ArrayList<String>();

    /**
     * The dynamic columns.
     */
    private EntityTypeProperty[] dynamicColumns = null;

    /**
     * The event data table.
     */
    private HtmlDataTable eventDataTable = null;

    /**
     * The events pager.
     */
    private DataPaginator eventsPager = null;

    /**
     * The app.
     */
    private Application app = FacesContext.getCurrentInstance()
            .getApplication();

    // private HtmlPanelGroup panelGroup = null;

    // For paginator
    /**
     * The last start row.
     */
    private int lastStartRow = -1;

    /**
     * The last page.
     */
    private DataPage lastPage = null;

    /**
     * The sort column name.
     */
    private String sortColumnName = null;

    /**
     * The ascending.
     */
    private boolean ascending = false;

    /**
     * The filter bean.
     */
    private FilterBean filterBean = null;

    /**
     * The arrow img.
     */
    private HtmlGraphicImage arrowImg = null;
    /**
     * ExtendedUIRoleBean
     */
    private ExtendedUIRoleBean extendedUIRoleBean;

    /**
     * ExtendedUIRole host group list
     */
    private String extRoleHostGroupList = ConsoleConstants.EMPTY_STRING;

    /**
     * ExtendedUIRole service group list
     */
    private String extRoleServiceGroupList = ConsoleConstants.EMPTY_STRING;

    /**
     * LogMessageID.
     */
    private static final String PARAM_LOGMESSAGE_ID = "LogMessageID";

    private static final String PROP_COMMENTS = "Comments";

    private static final String[] ACKNOWLEDGEMENT_COLUMNS = {"AcknowledgedBy",
            PROP_COMMENTS};

    private static final String STYLE_URLMAPPER_LINK = "color: #0101DF;\n" +
            "   text-decoration: underline;";

    /**
     * Inits the.
     */
    private void init() {
        sortColumnName = ConsoleConstants.DEFAULT_SORT_COLUMN;
        logger.debug("Constructing the datatable..");
        this.constructComponent();
        logger.debug("Populating all events...");

    }

    /**
     * Instantiates a new data table bean.
     */
    public DataTableBean() {
        super(Integer.parseInt(PropertyUtils
                .getProperty(ConsoleConstants.PROP_PAGE_SIZE)));
        init();

        /**
         * Faces context will be null on JMS or Non JSF thread. Perform a null
         * check. Make increase the visibility of the statisbean to class level
         * for the JMS thread.
         */
        if (FacesContext.getCurrentInstance() != null) {
            extendedUIRoleBean = ConsoleHelper.getExtendedUIRoleBean();
        }

        if (extendedUIRoleBean != null) {
            if (!extendedUIRoleBean.getHostGroupList().contains(
                    ExtendedUIRoleBean.RESTRICTED_KEYWORD)) {

                extRoleHostGroupList = extendedUIRoleBean
                        .getHostGroupListString();
            }
            if (!extendedUIRoleBean.getServiceGroupList().contains(
                    ExtendedUIRoleBean.RESTRICTED_KEYWORD)) {

                extRoleServiceGroupList = extendedUIRoleBean
                        .getServiceGroupListString();
            }

        }
    }

    /**
     * Gets the Event array of EventBean data.
     *
     * @return array of EventBean data.
     */
    public EventBean[] getEvents() {
        return events;
    }

    /**
     * Sets the events for the datatable.
     *
     * @param events the events
     */
    public void setEvents(EventBean[] events) {
        this.events = events;
    }

    /**
     * Gets the selectedRows for the datatable.
     *
     * @return the selected rows
     */
    public List<EventBean> getSelectedRows() {

        return selectedRows;
    }

    /**
     * Sets the selectedRows from the datatable.
     *
     * @param selectedRows the selected rows
     */
    public void setSelectedRows(List<EventBean> selectedRows) {
        this.selectedRows = selectedRows;
    }

    /**
     * Method for the rowselection listener.
     *
     * @param event the event
     */
    public void rowSelection(RowSelectorEvent event) {

        TabsetBean tabset = ConsoleHelper.getTabSetBean();
        Tab tab = tabset.getTabs().get(tabset.getTabIndex());
        selectedRows = new ArrayList<EventBean>();
        for (int i = events.length - 1; i >= 0; i--) {
            // logger.info("inside for loop");
            if (events[i].isSelected()) {
                logger.debug("Event selected=" + i);
                selectedRows.add(events[i]);
            } // end if
        } // end for
        // Synch up the messageSelectBean
        boolean rowSel = ((RowSelector) event.getComponent()).getValue();
        if (rowSel) {
            tab.getFreezeBean().freeze(true);
        } // end if

        MessageSelectBean msgSelectBean = tab.getMsgSelector();
        msgSelectBean.setAllRows((EventBean[]) selectedRows
                .toArray(new EventBean[selectedRows.size()]));
        if (selectedRows.size() <= 0) {
            msgSelectBean
                    .setSelectAllButtonText(ResourceUtils
                            .getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_SELECT_ALL));
        } // end if
        ActionBean action = tab.getActionBean();
        MenuItem menu = (MenuItem) action.getMenuModel().get(0);
        if (selectedRows.size() >= 1) {
            logger.debug("Clearing menumodel");
            menu.setIcon(ConsoleConstants.MENU_ICON_ON);

            // action.setStyleClass("darkButton active");
        } else {
            menu.setIcon(ConsoleConstants.MENU_ICON_OFF);
            menu.getChildren().clear();
            // action.setStyleClass("darkButton inActive");
        } // end if
        action.menuListener();
    }

    /**
     * Gets the multiple selection property of the datatable.
     *
     * @return boolean
     */
    public boolean isMultipleSelection() {
        return multipleSelection;
    }

    /**
     * Sets the multiple selection property for the datatable.This can be used
     * dynamically to turn ON and OFF the multiple sellection.
     *
     * @param multipleSelection the multiple selection
     */
    public void setMultipleSelection(boolean multipleSelection) {
        this.multipleSelection = multipleSelection;
    }

    /**
     * Gets the column names.
     *
     * @return the column names
     */
    public ArrayList<String> getColumnNames() {
        return columnNames;
    }

    /**
     * Sets the column names.
     *
     * @param columnNames the column names
     */
    public void setColumnNames(ArrayList<String> columnNames) {
        this.columnNames = columnNames;
    }

    /**
     * Gets the HTMLTable.
     *
     * @return the event data table
     */
    public HtmlDataTable getEventDataTable() {
        return eventDataTable;
    }

    /**
     * Sets the HTMLTable.
     *
     * @param eventDataTable the event data table
     */
    public void setEventDataTable(HtmlDataTable eventDataTable) {
        this.eventDataTable = eventDataTable;
    }

    /**
     * Construcs the datatable.
     */
    public void constructComponent() {
        logger.debug("Enter constructComponent");
        // Always create a new panel
        // if (panelGroup == null && eventDataTable == null) {
        if (eventDataTable == null) {

            logger.debug("Building new HTML table");
            // panelGroup = new HtmlPanelGroup();

            // Always create a new HTML table
            eventDataTable = new HtmlDataTable();
        } // end if

        eventDataTable.setRows(Integer.parseInt(PropertyUtils
                .getProperty(ConsoleConstants.PROP_PAGE_SIZE)));

        // }
        logger.debug("Child Count=" + eventDataTable.getChildCount());
        logger.debug("Clearing the html table");
        List<UIComponent> list = eventDataTable.getChildren();
        list.clear();
        logger.debug("About to add built-in..");
        this.addBuiltInProperties();
        logger.debug("About to add dynamics..");
        this.addDynamicProperties();

        logger.debug("Child Count=" + eventDataTable.getChildCount());
        logger.debug("Exit constructComponent");

    }

    /**
     * Gets the dynamic columns.
     *
     * @return the dynamic columns
     */
    public EntityTypeProperty[] getDynamicColumns() {
        return dynamicColumns;
    }

    /**
     * Sets the dynamic columns.
     *
     * @param dynamicColumns the new dynamic columns
     */
    public void setDynamicColumns(EntityTypeProperty[] dynamicColumns) {
        this.dynamicColumns = dynamicColumns;
    }

    /**
     * Add acknowledgement columns
     */
    private void addAcknowledgementColumns() {
        ExpressionFactory ef = app.getExpressionFactory();
        for (String dynColumnName : ACKNOWLEDGEMENT_COLUMNS) {
            UIColumn dynColumn = new UIColumn();
            logger.debug("Ack column=" + dynColumnName);
            String columnBinding = ConsoleConstants.EL_BIND_EVENT_LEFT
                    + "dynamicProperty." + dynColumnName + "}";
            dynColumn.setId("col_" + dynColumnName);

            HtmlOutputText dynHeader = new HtmlOutputText();
            dynHeader.setId("hdr_" + dynColumnName);
            dynHeader.setValue(dynColumnName);
            dynHeader.setStyleClass("table-header");
            dynColumn.setHeader(dynHeader);
            HtmlOutputText dynText = new HtmlOutputText();
            dynText.setId("txt_" + dynColumnName);
            if (dynColumnName.equalsIgnoreCase(PROP_COMMENTS)) {
                String commentsShortColumnBinding = ""
                        + ConsoleConstants.EL_BIND_EVENT_LEFT
                        + "commentsShort}";
                ValueExpression colBindValExp = ef.createValueExpression(
                        FacesContext.getCurrentInstance().getELContext(),
                        commentsShortColumnBinding, String.class);

                dynText.setValueExpression(ConsoleConstants.EL_BIND_ATT_VALUE,
                        colBindValExp);

                ValueExpression commentsFullValBindValExp = ef
                        .createValueExpression(FacesContext
                                .getCurrentInstance().getELContext(),
                                columnBinding, String.class);
                dynText.setValueExpression(ConsoleConstants.EL_BIND_ATT_TITLE,
                        commentsFullValBindValExp);
            } else {

                ValueExpression colBindValExp = ef.createValueExpression(
                        FacesContext.getCurrentInstance().getELContext(),
                        columnBinding, String.class);

                dynText.setValueExpression(ConsoleConstants.EL_BIND_ATT_VALUE,
                        colBindValExp);

            }

            HtmlPanelGroup htmlpg = new HtmlPanelGroup();
            htmlpg.setId("pnl_" + dynColumnName);
            htmlpg.setStyleClass(ConsoleConstants.STYLE_TABLE_COL);
            htmlpg.getChildren().add(dynText);
            dynColumn.getChildren().add(htmlpg);
            eventDataTable.getChildren().add(dynColumn);
            logger.debug("Adding ack column " + dynColumnName);
        }

    }

    /**
     * Adds Dynamic properties to the table.
     */
    private void addDynamicProperties() {
        // Now populate the dynamic properties here

        // First populate the Nagios service column first
        ExpressionFactory ef = app.getExpressionFactory();
        if (dynamicColumns != null) {

            // Now populate the rest of the columns
            for (int dynaIndex = 0; dynaIndex < dynamicColumns.length; dynaIndex++) {
                EntityTypeProperty entityTypeProperty = (EntityTypeProperty) dynamicColumns[dynaIndex];
                String appType = entityTypeProperty
                        .getApplicationType()
                        .getName();
                if (!appType
                        .equalsIgnoreCase(
                                ConsoleConstants.PROP_NAME_APP_TYPE_SYSTEM)) {

                    String dynColumnName = (entityTypeProperty.getName()
                            .substring(0, 1).toLowerCase() + entityTypeProperty
                            .getName().substring(1)).trim();
                    // Filter out acknowledgement columns as it is added in builtin properties
                    if (dynColumnName != null
                            && isDynaColVisible(dynColumnName) && !dynColumnName.equalsIgnoreCase("service") && !Arrays.asList(ACKNOWLEDGEMENT_COLUMNS).contains(entityTypeProperty.getName())) {
                        UIColumn dynColumn = new UIColumn();
                        logger.debug("Dynamic column=" + dynColumnName);
                        String columnBinding = ConsoleConstants.EL_BIND_EVENT_LEFT
                                + "dynamicProperty."
                                + entityTypeProperty.getName() + "}";
                        dynColumn.setId("col_" + dynColumnName);

                        HtmlOutputText dynHeader = new HtmlOutputText();
                        dynHeader.setId("hdr_" + dynColumnName);
                        dynHeader.setValue(dynColumnName);
                        dynHeader.setStyleClass("table-header");
                        dynColumn.setHeader(dynHeader);

                        HtmlOutputText dynText = new HtmlOutputText();
                        dynText.setId("txt_" + dynColumnName);
                        ValueExpression colBindValExp = ef
                                .createValueExpression(FacesContext
                                        .getCurrentInstance().getELContext(),
                                        columnBinding, String.class);

                        dynText.setValueExpression(
                                ConsoleConstants.EL_BIND_ATT_VALUE,
                                colBindValExp);

                        HtmlPanelGroup htmlpg = new HtmlPanelGroup();
                        htmlpg.setId("pnl_" + dynColumnName);
                        htmlpg.setStyleClass(ConsoleConstants.STYLE_TABLE_COL);
                        htmlpg.getChildren().add(dynText);
                        dynColumn.getChildren().add(htmlpg);
                        eventDataTable.getChildren().add(dynColumn);
                        logger.debug("Adding dynamic column " + dynColumnName);
                    } // end if

                } // end if
            } // end for
        } // end if

    } // end method

    /**
     * Helper to find if the dynamic column is visible or not
     *
     * @param column
     * @return
     */
    private boolean isDynaColVisible(String column) {
        boolean result = true;
        String excludedDynaColumns = PropertyUtils
                .getProperty(ConsoleConstants.PROP_EXCLUDED_DYNA_COLS);
        if (excludedDynaColumns != null
                && !excludedDynaColumns.equalsIgnoreCase("")) {
            StringTokenizer stkn = new StringTokenizer(excludedDynaColumns, ",");
            while (stkn.hasMoreTokens()) {
                String excludedColumn = stkn.nextToken();
                if (column.equalsIgnoreCase(excludedColumn))
                    return false;
            }
        }
        return result;
    }

    /**
     * Adds Static properties to the table.
     */
    private void addBuiltInProperties() {

        if (FacesContext.getCurrentInstance() != null) {
            viewRoot = FacesContext.getCurrentInstance().getViewRoot();
        }

        ExpressionFactory ef = app.getExpressionFactory();
        // initialize column names
        String built_in_columns = PropertyUtils
                .getProperty(ConsoleConstants.PROP_BUILTIN_COLS);

        if (built_in_columns != null) {
            if (built_in_columns.indexOf(sortColumnName) == -1)
                sortColumnName = null;
            columnNames = new ArrayList<String>();
            StringTokenizer stkn = new StringTokenizer(built_in_columns, ",");
            while (stkn.hasMoreTokens()) {
                String column = stkn.nextToken();
                StringTokenizer stknLabel = new StringTokenizer(column, ":");
                String columnName = stknLabel.nextToken();
                String columnLabel = stknLabel.nextToken();
                if (sortColumnName == null)
                    sortColumnName = columnName;
                columnNames.add(columnName);
                columnLabels.add(columnLabel);
            }
        } else {
            logger.error("No built_in_columns property specified in console.properties");
        }
        // First populate "more column" and the built-in columns and then the
        // dynamic ones
        this.addDynamicPropColumn(ef);

        int columnCount = columnNames.size();
        logger.debug("About to add built-ins..");
        for (int index = 0; index < columnCount; index++) {

            String columnName = (String) columnNames.get(index);
            String columnLabel = (String) columnLabels.get(index);
            String columnBinding = ConsoleConstants.EL_BIND_EVENT_LEFT
                    + columnName + "}";
            logger.debug("Adding built-in column " + columnName);
            UIColumn column = new UIColumn();
            column.setId("col_" + columnName);

            if (index == 0) {
                logger.debug("Adding row selector");
                RowSelector rs = new RowSelector();
                rs.setId("rs_" + columnName);
                ValueExpression rsBindValExp = ef.createValueExpression(
                        FacesContext.getCurrentInstance().getELContext(),
                        ConsoleConstants.EL_BIND_EVENT_SEL, boolean.class);

                rs.setValueExpression(ConsoleConstants.EL_BIND_ATT_VALUE,
                        rsBindValExp);
                ValueExpression msBindValExp = ef.createValueExpression(
                        FacesContext.getCurrentInstance().getELContext(),
                        ConsoleConstants.EL_BIND_TABLE_MULTIPLE_SEL,
                        boolean.class);

                rs.setValueExpression(ConsoleConstants.EL_BIND_ATT_MULTIPLE,
                        msBindValExp);
                Class ca[] = {com.icesoft.faces.component.ext.RowSelectorEvent.class};

                javax.faces.el.MethodBinding mb = app.createMethodBinding(
                        ConsoleConstants.EL_BIND_ROW_SEL, ca);
                rs.setSelectionListener(mb);
                rs.setSelectedClass("rowClicked");
                rs.setSelectedMouseOverClass("rowClicked");

                column.getChildren().add(rs);

            } // end if
            HtmlPanelGrid htmlPgHeader = new HtmlPanelGrid();
            htmlPgHeader.setId("sortHeaderId_" + columnName);
            htmlPgHeader.setColumns(2);
            htmlPgHeader.setStyle("cellpadding:0;cellspacing:0;");
            HtmlCommandLink sortHeader = new HtmlCommandLink();

            sortHeader.setId("header_" + columnName);

            sortHeader.setAccesskey(columnName);

            // Column labels go here
            sortHeader.setValue(columnLabel);
            sortHeader.setStyleClass("table-header");
            sortHeader
                    .setActionListener(createActionListenerMethodBinding(ConsoleConstants.EL_BIND_TABLE_SORT));
            htmlPgHeader.getChildren().add(sortHeader);

            if (sortColumnName.equalsIgnoreCase(columnName)) {
                arrowImg = new HtmlGraphicImage();
                arrowImg.setId("sortImg");
                if (ascending)
                    arrowImg.setUrl(ConsoleConstants.SORT_ARROW_UP);
                else
                    arrowImg.setUrl(ConsoleConstants.SORT_ARROW_DOWN);
                htmlPgHeader.getChildren().add(arrowImg);
            } // end if

            column.setHeader(htmlPgHeader);
            HtmlOutputText text = new HtmlOutputText();
            text.setId("txt_" + columnName);

            HtmlPanelGroup htmlpg = new HtmlPanelGroup();
            htmlpg.setId("pnl_" + columnName);
            if (columnName
                    .equalsIgnoreCase(ConsoleConstants.EL_BIND_COL_MON_STATUS)
                    || columnName
                    .equalsIgnoreCase(ConsoleConstants.EL_BIND_COL_SEVERITY)) {
                ValueExpression styleClassBindValExp = ef
                        .createValueExpression(FacesContext
                                .getCurrentInstance().getELContext(),
                                ConsoleConstants.EL_BIND_EVENT_LEFT
                                        + columnName
                                        + ConsoleConstants.EL_BIND_STYLE_RIGHT,
                                String.class);
                column.setValueExpression("styleClass", styleClassBindValExp);

                ValueExpression textValBindValExp = ef.createValueExpression(
                        FacesContext.getCurrentInstance().getELContext(),
                        ConsoleConstants.EL_BIND_EVENT_LEFT + columnName
                                + ConsoleConstants.EL_BIND_VALUE_RIGHT,
                        String.class);
                text.setValueExpression(ConsoleConstants.EL_BIND_ATT_VALUE,
                        textValBindValExp);

                htmlpg.getChildren().add(text);
            } else if (columnName.equalsIgnoreCase("host")) {

                htmlPgHeader
                        .setStyle("cellpadding:0;cellspacing:0;min-width:80px;");

                String linkNameBinding = ConsoleConstants.EL_BIND_EVENT_LEFT
                        + "host" + "}";
                // For NAGIOS application events
                // link for device to SV
                HtmlOutputLink deviceLink = new HtmlOutputLink();
                deviceLink.setId(viewRoot.createUniqueId());

                String urlBinding = ConsoleConstants.EL_BIND_EVENT_LEFT
                        + "deviceUrl" + "}";

                ValueExpression urlBindValExp = ef.createValueExpression(
                        FacesContext.getCurrentInstance().getELContext(),
                        urlBinding, String.class);
                deviceLink.setValueExpression(
                        ConsoleConstants.EL_BIND_ATT_VALUE, urlBindValExp);

                ValueExpression styleBinding = ef.createValueExpression(
                        FacesContext.getCurrentInstance().getELContext(),
                        STYLE_URLMAPPER_LINK + " "
                                + ConsoleConstants.EL_BIND_EVENT_LEFT
                                + "deviceLinkVisible}", String.class);
                deviceLink.setValueExpression(ConsoleConstants.STYLE,
                        styleBinding);

                HtmlOutputText txtName = new HtmlOutputText();
                txtName.setId(viewRoot.createUniqueId());

                // value expression
                txtName.setValueExpression(ConsoleConstants.EL_BIND_ATT_VALUE,
                        ef.createValueExpression(FacesContext
                                .getCurrentInstance().getELContext(),
                                linkNameBinding, String.class));
                deviceLink.getChildren().add(txtName);

                HtmlOutputText txtNameNoLink = new HtmlOutputText();
                txtNameNoLink.setId(viewRoot.createUniqueId());

                // value expression
                txtNameNoLink.setValueExpression(
                        ConsoleConstants.EL_BIND_ATT_VALUE, ef
                        .createValueExpression(FacesContext
                                .getCurrentInstance().getELContext(),
                                linkNameBinding, String.class));
                ValueExpression textStyleBinding = ef.createValueExpression(
                        FacesContext.getCurrentInstance().getELContext(),
                        ConsoleConstants.EL_BIND_EVENT_LEFT
                                + "deviceTextVisible}", String.class);
                txtNameNoLink.setValueExpression(ConsoleConstants.STYLE,
                        textStyleBinding);

                // link for popup
                HtmlCommandLink deviceServiceLink = new HtmlCommandLink();
                deviceServiceLink.setId(viewRoot.createUniqueId());

                ValueExpression textValBindValExp = ef.createValueExpression(
                        FacesContext.getCurrentInstance().getELContext(),
                        columnBinding, String.class);
                deviceServiceLink.setValueExpression(
                        ConsoleConstants.EL_BIND_ATT_VALUE, textValBindValExp);
                deviceServiceLink.setValue("");
                deviceServiceLink.setStyle("float:right; margin-left:1px");

                HtmlGraphicImage image = new HtmlGraphicImage();
                image.setId(viewRoot.createUniqueId());
                image.setValue(IMGPATH_SERVICES_POPUP);

                deviceServiceLink.getChildren().add(image);

                // device name as parameter
                UIParameter nameParam = new UIParameter();
                nameParam.setId(viewRoot.createUniqueId());
                nameParam.setName(PARAM_NAME);
                nameParam.setValueExpression(
                        ConsoleConstants.EL_BIND_ATT_VALUE, ef
                        .createValueExpression(FacesContext
                                .getCurrentInstance().getELContext(),
                                linkNameBinding, String.class));
                // device name as parameter
                UIParameter appTypeParam = new UIParameter();
                appTypeParam.setId(viewRoot.createUniqueId());
                appTypeParam.setName(APP_TYPE);
                appTypeParam.setValueExpression(
                        ConsoleConstants.EL_BIND_ATT_VALUE, ef
                        .createValueExpression(FacesContext
                                .getCurrentInstance().getELContext(),
                                ConsoleConstants.EL_BIND_EVENT_LEFT
                                        + "applicationType" + "}",
                                String.class));
                deviceServiceLink.getChildren().add(nameParam);
                deviceServiceLink.getChildren().add(appTypeParam);

                deviceServiceLink
                        .setActionListener(createActionListenerMethodBinding("#{tabset.tabs[tabset.tabIndex].dataTableBean.showDetails}"));

                htmlpg.getChildren().add(deviceLink);
                htmlpg.getChildren().add(txtNameNoLink);
                htmlpg.getChildren().add(deviceServiceLink);
            } else if (columnName.equalsIgnoreCase("serviceDescription")) {

                htmlPgHeader
                        .setStyle("cellpadding:0;cellspacing:0;min-width:80px;");

                String linkNameBinding = ConsoleConstants.EL_BIND_EVENT_LEFT
                        + "serviceDescriptionShort" + "}";
                // For NAGIOS application events
                // link for device to SV
                HtmlOutputLink serviceLink = new HtmlOutputLink();
                serviceLink.setId(viewRoot.createUniqueId());

                String urlBinding = ConsoleConstants.EL_BIND_EVENT_LEFT
                        + "serviceUrl" + "}";
                String serviceFullColumnBinding = ""
                        + ConsoleConstants.EL_BIND_EVENT_LEFT
                        + "serviceDescription}";

                ValueExpression urlBindValExp = ef.createValueExpression(
                        FacesContext.getCurrentInstance().getELContext(),
                        urlBinding, String.class);
                serviceLink.setValueExpression(
                        ConsoleConstants.EL_BIND_ATT_VALUE, urlBindValExp);

                ValueExpression styleBinding = ef.createValueExpression(
                        FacesContext.getCurrentInstance().getELContext(),
                        STYLE_URLMAPPER_LINK + " "
                                + ConsoleConstants.EL_BIND_EVENT_LEFT
                                + "serviceLinkVisible}", String.class);
                serviceLink.setValueExpression(ConsoleConstants.STYLE,
                        styleBinding);

                HtmlOutputText txtName = new HtmlOutputText();
                txtName.setId(viewRoot.createUniqueId());
                ValueExpression serviceFullValBindValExp = ef
                        .createValueExpression(FacesContext
                                .getCurrentInstance().getELContext(),
                                serviceFullColumnBinding, String.class);

                // value expression
                txtName.setValueExpression(ConsoleConstants.EL_BIND_ATT_VALUE,
                        ef.createValueExpression(FacesContext
                                .getCurrentInstance().getELContext(),
                                linkNameBinding, String.class));
                txtName.setValueExpression(ConsoleConstants.EL_BIND_ATT_TITLE,serviceFullValBindValExp);
                serviceLink.getChildren().add(txtName);

                HtmlOutputText txtNameNoLink = new HtmlOutputText();
                txtNameNoLink.setId(viewRoot.createUniqueId());

                // value expression
                txtNameNoLink.setValueExpression(
                        ConsoleConstants.EL_BIND_ATT_VALUE, ef
                                .createValueExpression(FacesContext
                                                .getCurrentInstance().getELContext(),
                                        linkNameBinding, String.class));
                ValueExpression textStyleBinding = ef.createValueExpression(
                        FacesContext.getCurrentInstance().getELContext(),
                        ConsoleConstants.EL_BIND_EVENT_LEFT
                                + "serviceTextVisible}", String.class);
                txtNameNoLink.setValueExpression(ConsoleConstants.STYLE,
                        textStyleBinding);

                htmlpg.getChildren().add(serviceLink);
                htmlpg.getChildren().add(txtNameNoLink);
            } else if (columnName.equalsIgnoreCase("textMessage")) {

                ValueExpression textValBindValExp = ef.createValueExpression(
                        FacesContext.getCurrentInstance().getELContext(),
                        columnBinding, String.class);
                text.setValueExpression(ConsoleConstants.EL_BIND_ATT_VALUE,
                        textValBindValExp);
                String textMessageFullColumnBinding = ""
                        + ConsoleConstants.EL_BIND_EVENT_LEFT
                        + "textMessageFull}";
                ValueExpression textMessageFullValBindValExp = ef
                        .createValueExpression(FacesContext
                                .getCurrentInstance().getELContext(),
                                textMessageFullColumnBinding, String.class);
                text.setValueExpression(ConsoleConstants.EL_BIND_ATT_TITLE,
                        textMessageFullValBindValExp);
                htmlpg.getChildren().add(text);
            } else {
                htmlpg.setStyleClass(ConsoleConstants.STYLE_TABLE_COL);

                ValueExpression textValBindValExp = ef.createValueExpression(
                        FacesContext.getCurrentInstance().getELContext(),
                        columnBinding, String.class);
                text.setValueExpression(ConsoleConstants.EL_BIND_ATT_VALUE,
                        textValBindValExp);
                htmlpg.getChildren().add(text);
            } // end if

            column.getChildren().add(htmlpg);
            eventDataTable.getChildren().add(column);
        } // end for

        if (ConsoleHelper.getFilterBean() == null)
            this.addAcknowledgementColumns();
        else if (!isNagiosAppTypeInFilter(ConsoleHelper.getFilterBean().getFilter())) {
            this.addAcknowledgementColumns();
        }
    }

    private boolean isNagiosAppTypeInFilter(Filter filter) {
        boolean result = false;
        if (filter != null) {
            if (filter.getStringProperty() != null && filter.getStringProperty().getValue().equalsIgnoreCase(Constant.NAGIOS)) {
                return true;
            }
            if (filter.getRightFilter() != null) {
                result = this.isNagiosAppTypeInFilter(filter.getRightFilter());
            }
            if (filter.getLeftFilter() != null) {
                result = this.isNagiosAppTypeInFilter(filter.getLeftFilter());
            }
        }
        return result;
    }

    private void addDynamicPropColumn(ExpressionFactory ef) {

        UIColumn column = new UIColumn();
        column.setId(viewRoot.createUniqueId());

        HtmlPanelGrid htmlPgHeader = new HtmlPanelGrid();
        htmlPgHeader.setId(viewRoot.createUniqueId());
        htmlPgHeader.setColumns(1);
        htmlPgHeader.setStyle("cellpadding:0;cellspacing:0;");
        HtmlOutputText text = new HtmlOutputText();
        text.setValue("");
        htmlPgHeader.getChildren().add(text);
        column.setHeader(htmlPgHeader);

        HtmlCommandLink dynamicPropLink = new HtmlCommandLink();
        dynamicPropLink.setId(viewRoot.createUniqueId());
        HtmlPanelGroup htmlpg = new HtmlPanelGroup();
        htmlpg.setId(viewRoot.createUniqueId());

        ValueExpression styleValBindValExp = ef
                .createValueExpression(
                        FacesContext.getCurrentInstance().getELContext(),
                        ConsoleConstants.EL_BIND_EVENT_LEFT
                                + "operationStatus eq \"OPEN\" ? \"openMsg\" : (event.operationStatus eq \"NOTIFIED\" ? \"notifyMsg\" : (event.operationStatus eq \"CLOSED\" ? \"closeMsg\" : (event.operationStatus eq \"ACCEPTED\" ? \"acceptMsg\" : (event.operationStatus eq \"ACKNOWLEDGED\" ? \"acknowledgeMsg\" : \"otherMsg\"))))"
                                + "}", String.class);
        column.setValueExpression(
                "styleClass", styleValBindValExp);

        HtmlGraphicImage image = new HtmlGraphicImage();
        image.setId(viewRoot.createUniqueId());
        image.setValue(IMGPATH_DYNA_POPUP);

        dynamicPropLink.getChildren().add(image);
        // Don't render if the apptype is system

       /* ValueExpression renderedValBindValExp = ef
                .createValueExpression(
                        FacesContext.getCurrentInstance().getELContext(),
                        ConsoleConstants.EL_BIND_EVENT_LEFT
                                + "applicationType eq \"SYSTEM\" ? false : true"
                                + "}", Boolean.class);*/
        ValueExpression renderedValBindValExp = ef
                .createValueExpression(
                        FacesContext.getCurrentInstance().getELContext(),
                        ConsoleConstants.EL_BIND_EVENT_LEFT
                                + "dynamicProperty == null ? false : true}", Boolean.class);
        dynamicPropLink.setValueExpression(
                ConsoleConstants.EL_BIND_ATT_RENDERED, renderedValBindValExp);

        // LogMessageID as parameter
        UIParameter logMessageIdParam = new UIParameter();
        logMessageIdParam.setId(viewRoot.createUniqueId());
        logMessageIdParam.setName(PARAM_LOGMESSAGE_ID);
        logMessageIdParam.setValueExpression(
                ConsoleConstants.EL_BIND_ATT_VALUE, ef.createValueExpression(
                FacesContext.getCurrentInstance().getELContext(),
                ConsoleConstants.EL_BIND_EVENT_LEFT + "logMessageID"
                        + "}", Integer.class));
        // device name as parameter
        UIParameter appTypeParam = new UIParameter();
        appTypeParam.setId(viewRoot.createUniqueId());
        appTypeParam.setName(APP_TYPE);
        appTypeParam.setValueExpression(ConsoleConstants.EL_BIND_ATT_VALUE, ef
                .createValueExpression(FacesContext.getCurrentInstance()
                        .getELContext(), ConsoleConstants.EL_BIND_EVENT_LEFT
                        + "applicationType" + "}", String.class));
        dynamicPropLink.getChildren().add(logMessageIdParam);
        dynamicPropLink.getChildren().add(appTypeParam);

        dynamicPropLink
                .setActionListener(createActionListenerMethodBinding("#{tabset.tabs[tabset.tabIndex].dataTableBean.showDynamicProps}"));

        htmlpg.getChildren().add(dynamicPropLink);
        column.getChildren().add(htmlpg);
        eventDataTable.getChildren().add(column);
    }

    /**
     * Getter for data paginator.
     *
     * @return DataPaginator
     */
    public DataPaginator getEventsPager() {
        return eventsPager;
    }

    /**
     * Setter for data paginator.
     *
     * @param eventsPager the events pager
     */
    public void setEventsPager(DataPaginator eventsPager) {
        this.eventsPager = eventsPager;
    }

    /**
     * Fetches the data page.
     *
     * @param startRow the start row
     * @param pageSize the page size
     * @return DataPage
     */
    public DataPage fetchPage(int startRow, int pageSize) {
        // call enclosing managed bean method to fetch the data
        return getDataPage(startRow, pageSize);
    }

    /**
     * Gets the data page.
     *
     * @param startRow the start row
     * @param pageSize the page size
     * @return the data page
     */
    private DataPage getDataPage(int startRow, int pageSize) {
        logger.debug("Enter getDataPage" + "--" + lastStartRow + "--"
                + startRow + "--" + lastPage);

        if (lastPage == null || startRow != lastStartRow) {
            Filter filter = null;
            if (FacesContext.getCurrentInstance() == null) {
                // JMS Thread
                logger.debug("JMS thread....");
                filter = filterBean.getFilter();
            } else {
                filter = ConsoleHelper.getFilterBean().getFilter();
            } // end if
            logger.debug("Filter" + filter);
            List<EventBean> eventList = null;
            Sort sort = new Sort(this.ascending, this.sortColumnName);
            /*
             * if (filter == null) { filter = new Filter(
			 * ConsoleConstants.PROP_NAME_OPERATION_STATUS, FilterOperator.EQ,
			 * ConsoleConstants.OPERATION_STATUS_OPEN); } // end if
			 */
            if (dynamicColumns != null) {
                eventList = new EventQueryManager().queryForEventsByFilter(
                        filter, dynamicColumns, startRow, sort,
                        extRoleHostGroupList, extRoleServiceGroupList);
            } else {
                eventList = new EventQueryManager().queryForEventsByFilter(
                        filter, startRow, sort, extRoleHostGroupList,
                        extRoleServiceGroupList);
            } // end if
            int dataSetSize = 0;
            logger.debug("Eventlist size=" + eventList.size());
            if (eventList.size() > 0) {
                dataSetSize = eventList.get(0).getTotalCount();
            } // end if
            lastStartRow = startRow;
            lastPage = new DataPage(dataSetSize, startRow, eventList);
            logger.debug(dataSetSize);
            events = (EventBean[]) eventList.toArray(new EventBean[eventList
                    .size()]);
        }
        return lastPage;

    }

    /**
     * Sets the last start row.
     *
     * @param lastStartRow the last start row
     */
    public void setLastStartRow(int lastStartRow) {
        this.lastStartRow = lastStartRow;
    }

    /**
     * Sets the last page.
     *
     * @param lastPage the last page
     */
    public void setLastPage(DataPage lastPage) {
        this.lastPage = lastPage;
    }

    /**
     * Gets the sort column name.
     *
     * @return the sort column name
     */
    public String getSortColumnName() {
        return sortColumnName;
    }

    /**
     * Sets the sort column name.
     *
     * @param sortColumnName the new sort column name
     */
    public void setSortColumnName(String sortColumnName) {

        this.sortColumnName = sortColumnName;

    }

    /**
     * Checks if is ascending.
     *
     * @return true, if is ascending
     */
    public boolean isAscending() {
        return ascending;
    }

    /**
     * Sets the ascending.
     *
     * @param ascending the new ascending
     */
    public void setAscending(boolean ascending) {

        this.ascending = ascending;
    }

    /**
     * Listener for sorting.
     *
     * @param event the event
     */
    public void sort(ActionEvent event) {

        logger.debug("Sort column=" + this.sortColumnName + "----"
                + this.ascending + "---" + eventDataTable.isSortAscending());
        UIComponent comp = event.getComponent();

        if (comp instanceof com.icesoft.faces.component.ext.HtmlCommandLink) {
            HtmlCommandLink csh = (HtmlCommandLink) comp;
            logger.debug("Column " + csh.getAccesskey()
                    + " Clicked..order by (table)"
                    + eventDataTable.isSortAscending() + "---(bean)"
                    + this.ascending);
            this.sortColumnName = csh.getAccesskey();
            ascending = !ascending;
            if (ascending)
                arrowImg.setUrl(ConsoleConstants.SORT_ARROW_UP);
            else
                arrowImg.setUrl(ConsoleConstants.SORT_ARROW_DOWN);
            csh.getParent().getChildren().add(arrowImg);
        } // end if

        lastPage = null;
        page = fetchPage(0, Integer.parseInt(PropertyUtils
                .getProperty(ConsoleConstants.PROP_PAGE_SIZE)));
        List<EventBean> eventList = page.getData();
        events = (EventBean[]) eventList
                .toArray(new EventBean[eventList.size()]);
    }

    /**
     * Creates an action listener method for the menu.
     *
     * @param actionListenerString the action listener string
     * @return the method binding
     */
    private MethodBinding createActionListenerMethodBinding(
            String actionListenerString) {
        Class args[] = {ActionEvent.class};
        MethodBinding methodBinding = null;

        methodBinding = FacesContext.getCurrentInstance().getApplication()
                .createMethodBinding(actionListenerString, args);
        return methodBinding;
    }

    /**
     * Gets the column labels.
     *
     * @return the column labels
     */
    public ArrayList<String> getColumnLabels() {
        return columnLabels;
    }

    /**
     * Sets the column labels.
     *
     * @param columnLabels the new column labels
     */
    public void setColumnLabels(ArrayList<String> columnLabels) {
        this.columnLabels = columnLabels;
    }

    /**
     * Gets the filter bean.
     *
     * @return the filter bean
     */
    public FilterBean getFilterBean() {
        return filterBean;
    }

    /**
     * Sets the filter bean.
     *
     * @param filterBean the new filter bean
     */
    public void setFilterBean(FilterBean filterBean) {
        this.filterBean = filterBean;
    }

    /**
     * Show Details.
     *
     * @param ae the ae
     */
    public void showDetails(ActionEvent ae) {
        // String deviceName = (String) ((HtmlCommandLink) ae.getComponent())
        // .getValue();
        String deviceNameParam = FacesUtils.getRequestParameter(PARAM_NAME);
        String appTypeParam = FacesUtils.getRequestParameter(APP_TYPE);
        PopupBean popup = ConsoleHelper.getPopupBean();
        popup.setShowDraggablePanel(true);

        ConsoleManager mgr = ConsoleHelper.getConsoleManager();
        HostDetailBean hostDetail = mgr.getHostDetails(deviceNameParam);

        popup.setTitle("Device details");
        popup.setHost(hostDetail);
        popup.setAppType(appTypeParam);

    } // end method

    /**
     * Sets the extendedUIRoleBean.
     *
     * @param extendedUIRoleBean the extendedUIRoleBean to set
     */
    public void setExtendedUIRoleBean(ExtendedUIRoleBean extendedUIRoleBean) {
        this.extendedUIRoleBean = extendedUIRoleBean;
    }

    /**
     * Returns the extendedUIRoleBean.
     *
     * @return the extendedUIRoleBean
     */
    public ExtendedUIRoleBean getExtendedUIRoleBean() {
        return extendedUIRoleBean;
    }

    /**
     * Popup action for showing more dynamic columns
     *
     * @param ae
     */
    public void showDynamicProps(ActionEvent ae) {
        String paramValue = FacesUtils.getRequestParameter(PARAM_LOGMESSAGE_ID);
        if (paramValue != null && !paramValue.equalsIgnoreCase("")) {
            Integer logMessageId = Integer.parseInt(paramValue);
            String appTypeParam = FacesUtils.getRequestParameter(APP_TYPE);

            for (EventBean event : events) {
                if (event.getLogMessageID() == logMessageId) {
                    Map<String, Object> tmpMap = event.getDynamicProperty();

                    Map<String, Object> dynaMap = new HashMap<String, Object>();
                    // Check if the dynamic property is visible or not
                    if (tmpMap != null) {
                        for (String key : tmpMap.keySet()) {
                            if (isDynaColVisible(key)) {
                                dynaMap.put(key, tmpMap.get(key));
                            } // end if
                        } // end for
                    }
                    if (dynaMap != null && !dynaMap.isEmpty()) {
                        PopupBean popup = ConsoleHelper.getPopupBean();
                        popup.setShowDynamicProps(true);
                        popup.setDynaPropMap(null);
                        popup.setDynaPropMap(dynaMap);
                        popup.setTitle("Additional Columns for the message");
                        popup.setAppType(appTypeParam);
                    } // end if
                } // end if
            } // end if
        } // end if
    } // end method

}
