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

package com.groundworkopensource.portal.statusviewer.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.event.ActionEvent;

import com.groundworkopensource.webapp.console.ConsoleConstants;
import com.groundworkopensource.webapp.console.DataPage;
import com.groundworkopensource.webapp.console.PagedListDataModel;
import com.groundworkopensource.webapp.console.EventBean;
import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.model.impl.Category;
import org.groundwork.foundation.ws.model.impl.EntityTypeProperty;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.HostGroup;
import org.groundwork.foundation.ws.model.impl.ServiceStatus;
import org.groundwork.foundation.ws.model.impl.SimpleHost;
import org.groundwork.foundation.ws.model.impl.Sort;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

import com.groundworkopensource.portal.common.ApplicationType;
import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.common.PropertyUtils;
import com.groundworkopensource.portal.common.ResourceUtils;
import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.common.exception.GWPortalGenericException;
import com.groundworkopensource.portal.common.exception.PreferencesException;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;
import com.groundworkopensource.portal.common.ws.IWSFacade;
import com.groundworkopensource.portal.common.ws.WebServiceFactory;
import com.groundworkopensource.portal.common.ws.WebServiceFactory.WebServiceType;
import com.groundworkopensource.portal.statusviewer.common.Constant;

import com.groundworkopensource.portal.statusviewer.common.NodeType;

import com.groundworkopensource.portal.statusviewer.common.PortletUtils;
import com.groundworkopensource.portal.statusviewer.handler.EventHandler;
import com.groundworkopensource.webapp.console.EventQueryManager;
import com.groundworkopensource.portal.statusviewer.handler.ReferenceTreeMetaModel;
import com.groundworkopensource.portal.statusviewer.handler.SubpageIntegrator;
import com.icesoft.faces.component.datapaginator.DataPaginator;
import com.icesoft.faces.component.ext.HtmlCommandLink;
import com.icesoft.faces.component.ext.HtmlDataTable;
import com.icesoft.faces.component.ext.HtmlGraphicImage;
import com.icesoft.faces.component.ext.HtmlOutputText;
import com.icesoft.faces.component.ext.HtmlPanelGrid;
import com.icesoft.faces.component.ext.HtmlPanelGroup;
import com.icesoft.faces.component.ext.RowSelector;
import com.icesoft.faces.component.ext.RowSelectorEvent;
import com.icesoft.faces.component.ext.UIColumn;
import com.icesoft.faces.component.menubar.MenuItem;
import com.icesoft.faces.component.menupopup.MenuPopup;
import com.icesoft.faces.webapp.http.core.SessionExpiredException;

/**
 * The DataTableBean contains the data to build the icefaces:datatable
 * structure. It is renderable and uses the icefaces server initiated rendering
 * architecture. It is also a message listener for the events from the
 * foundation.Datatable is updated incrementally if there is any new event
 * message showing up.
 * 
 * @author manish_kjain
 * 
 */
@SuppressWarnings("deprecation")
public class EventDataTableBean extends PagedListDataModel {

    /**
     * Event portlet hide dynamic column property key
     */
    private static final String EVEN_PORTLET_COLUMNS_HIDE = "even.portlet.columns.hide";

    /**
     * event page size preference
     */
    private static final String EVENTS_PER_PAGE_PREFERENCE = "eventsPerPage";

    /**
     * Event bean Array.
     */
    private EventBean[] events;

    /** Enable log4j for EventDataTableBean class */
    private static final Logger LOGGER = Logger
            .getLogger(EventDataTableBean.class.getName());
    /**
     * enable Multiple row selection
     */
    private boolean multipleSelection = true;
    /**
     * List hold the current selected rows
     */
    private List<EventBean> selectedRows = null;
    /**
     * Data table column name Array list to be displayed on portlet.
     */
    private ArrayList<String> columnNames = new ArrayList<String>();
    /**
     * Data table column label Array list to be displayed on portlet data table
     * header.
     */
    private ArrayList<String> columnLabels = new ArrayList<String>();

    /**
     * HtmlDataTable instance variable to construct data table.
     */
    private HtmlDataTable eventDataTable = null;
    /**
     * HtmlDataTable instance variable for host group view to construct data
     * table.
     */
    private HtmlDataTable sgEventDataTable = null;
    /**
     * HtmlDataTable instance variable for service view to construct data table.
     */
    private HtmlDataTable serviceEventDataTable = null;
    /**
     * HtmlDataTable instance variable for host group to construct data table.
     */
    private HtmlDataTable hgEventDataTable = null;
    /**
     * DataPaginator instance variable to Paginate data table.
     */
    private DataPaginator eventsPager = null;

    /**
     * current application instance.
     */
    private final Application application = FacesContext.getCurrentInstance()
            .getApplication();

    /**
     * Last start Row
     */
    private int lastStartRow = -1;
    /**
     * initial last page is null.
     */
    private DataPage lastPage = null;
    /**
     * Default sort column name
     */
    private String sortColumnName = null;
    /**
     * default sorting order.
     */
    private boolean ascending = false;
    /**
     * HtmlGraphicImage instance variable
     */
    private HtmlGraphicImage arrowImg = null;
    /**
     * is row selected or not .By default row is not selected
     */
    private boolean isRowSelected = false;
    /**
     * Menu pop up instance variable
     */
    private MenuPopup eventMenuPop;

    /**
     * menu item for accept log massage.
     */
    private MenuItem acceptMenuItem;
    /**
     * menu item for notify log massage.
     */
    private MenuItem notifyMenuItem;
    /**
     * menu item for close log massage.
     */
    private MenuItem closeMenuItem;
    /**
     * menu item for open log massage.
     */
    private MenuItem openMenuItem;
    /**
     * menu item for open log massage.
     */
    private MenuItem nagiosMenuItem;
    /**
     * menu item for Syslog OR Snmptrap
     */
    private MenuItem sysLogORSnmptrapMenuItem;

    /**
     * determine Syslog OR Snmptrap menu item is render or not
     */
    private boolean isSyslogORSnmptrapMenu = false;
    /**
     * determine nagois acknowledge is render or not
     */
    private boolean isnagiosAcknowledge = false;
    /**
     * boolean variable to enable and disable pop up menu.
     */
    private boolean enablePopUpMenu = false;
    /**
     * check weather pop up menu is clicked or not
     */
    private boolean popUpmenuClicked = false;
    /**
     * number of rows in table
     */
    private int tableRows;

    /**
     * Error boolean to set if error occurred
     */
    private boolean error = false;
    /**
     * info boolean to set if information occurred
     */
    private boolean info = false;

    /**
     * boolean variable message set true when display any type of messages
     * (Error,info or warning) in UI
     */
    private boolean message = false;

    /**
     * information message to show on UI
     */
    private String infoMessage;

    /**
     * Error message to show on UI
     */
    private String errorMessage;

    /**
     * EventFilterBean instance variable
     */
    private EventFilterBean eventFilterBean = null;

    /**
     * SubpageIntegrator
     */
    private SubpageIntegrator subpageIntegrator = null;
    /**
     * selectedNodeId default 0
     */
    private int selectedNodeId = 0;
    /**
     * selectedNodeType
     */
    private NodeType selectedNodeType = null;
    /**
     * selectedNodeName
     */
    private String selectedNodeName = null;
    /**
     * inStatusViewer
     */
    private boolean inStatusViewer = false;

    /**
     * hidden field to display error / info message in dashboard.
     */
    private String eventHiddenField = Constant.HIDDEN;
    /**
     * EntityTypeProperty array
     */
    private EntityTypeProperty[] dynamicColumns = null;

    /**
     * Event handler instance variable
     */
    private EventHandler eventHandler = null;
    /**
     * style class for scroll bar in data table for different layout
     */
    private String styleClass;
    /**
     * column names
     */
    private String eventColumns = null;

    /**
     * DataPaginator
     */
    private DataPaginator dataPaginator;
    /**
     * DataPaginator instance for host group view
     */
    private DataPaginator hgDataPaginator;
    /**
     * DataPaginator instance for service group view
     */
    private DataPaginator sgDataPaginator;
    /**
     * DataPaginator instance for service view
     */
    private DataPaginator serviceDataPaginator;

    /**
     * dynamic form id for dash board
     */
    private String dashboradformID;

    /**
     * TEN_THOUSAND
     */
    private static final int TEN_THOUSAND = 10000;

    /**
     * UserRoleBean instance.
     */
    private UserExtendedRoleBean userExtendedRoleBean;

    /**
     * ReferenceTreeMetaModel instance
     */
    private ReferenceTreeMetaModel referenceTreeModel = (ReferenceTreeMetaModel) FacesUtils
            .getManagedBean(Constant.REFERENCE_TREE);


    private static final String PROP_COMMENTS = "Comments";

    private static final String[] ACKNOWLEDGEMENT_COLUMNS = {"AcknowledgedBy",
            PROP_COMMENTS};

    /**
     * Stored version of UIViewRoot, which is useful to generate unique IDs for
     * elements. Stored version is used when the FacesdContext object is
     * unavailable.
     */
    private UIViewRoot viewRoot;

    /**
     * APP_TYPE.
     */
    private static final String APP_TYPE = "apptype";

    /**
     * IMGPATH_DYNA_POPUP.
     */
    private static final String IMGPATH_DYNA_POPUP = "/xmlhttp/css/xp/css-images/tree_nav_top_open_no_siblings.gif";

    /**
     * LogMessageID.
     */
    private static final String PARAM_LOGMESSAGE_ID = "LogMessageID";

    /**
     * Initializes the bean.Populates the default "All Events".
     */
    private void init() {
        this.sortColumnName = Constant.DEFAULT_SORT_COLUMN_NAME;
        this.initializeSubPageNode();
        this.setTableRows(getEventTablePageSize());
        // set application type dynamic column if selected node type is not
        // network i.e. for host view ,host group view ,service view or service
        // group view.
        if (inStatusViewer) {
            this
                    .setapplicationTypeDynamicColumns(Constant.NAGIOS
                            .toUpperCase());
        } else {
            if (selectedNodeType != NodeType.NETWORK) {
                this.setapplicationTypeDynamicColumns(Constant.NAGIOS
                        .toUpperCase());

            }
        }
        // getting build in column from status-viewer property file
        try {
            eventColumns = PropertyUtils.getProperty(
                    ApplicationType.STATUS_VIEWER, "built_in_columns");
        } catch (Exception e) {
            LOGGER
                    .warn("Exception while getting built_in_columns property........ ");
            eventColumns = Constant.EVENT_COLUMN_STRING;
        }
        this.initializeComponent();

    }

    /**
     * 
     */
    private void initializeSubPageNode() {
        try {
            subpageIntegrator = new SubpageIntegrator();
            subpageIntegrator.doSubpageIntegration(null);
            inStatusViewer = PortletUtils.isInStatusViewer();

            if (inStatusViewer) {
                // Got the parameters - get the required data from
                // SubpageIntegrator
                selectedNodeType = subpageIntegrator.getNodeType();
                selectedNodeId = subpageIntegrator.getNodeID();
                selectedNodeName = subpageIntegrator.getNodeName();

            } else {
                // do the dashboard handling
                boolean checkNodeNamePref = true;
                // For dashboard, initialize NodeType, NodeName parameters
                // Node Type
                selectedNodeType = NodeType.HOST;
                try {
                    String nodeTypePreference = FacesUtils
                            .getPreference(Constant.NODE_TYPE_PREF);
                    if (null == nodeTypePreference
                            || nodeTypePreference.equals(Constant.EMPTY_STRING)) {
                        throw new PreferencesException();
                    }
                    if (NodeType.NETWORK.getTypeName().equals(
                            nodeTypePreference)) {
                        // Entire Network
                        selectedNodeType = NodeType.NETWORK;

                    } else if (NodeType.SERVICE_GROUP.getTypeName().equals(
                            nodeTypePreference)) {
                        // Service Group
                        selectedNodeType = NodeType.SERVICE_GROUP;

                    } else if (NodeType.HOST_GROUP.getTypeName().equals(
                            nodeTypePreference)) {
                        // Host Group
                        selectedNodeType = NodeType.HOST_GROUP;
                    } else if (NodeType.SERVICE.getTypeName().equals(
                            nodeTypePreference)) {
                        // Service
                        selectedNodeType = NodeType.SERVICE;
                        selectedNodeName = Constant.EMPTY_STRING;
                        checkNodeNamePref = false;
                    }
                } catch (PreferencesException e) {
                    List<String> extRoleHostGroupList = userExtendedRoleBean
                            .getExtRoleHostGroupList();
                    List<String> extRoleServiceGroupList = userExtendedRoleBean
                            .getExtRoleServiceGroupList();
                    if (extRoleHostGroupList.isEmpty()
                            && extRoleServiceGroupList.isEmpty()) {
                        selectedNodeType = NodeType.NETWORK;
                    } else if (!extRoleHostGroupList.isEmpty()
                            && !extRoleHostGroupList
                                    .contains(UserExtendedRoleBean.RESTRICTED_KEYWORD)) {
                        selectedNodeType = NodeType.HOST_GROUP;
                        selectedNodeName = userExtendedRoleBean
                                .getDefaultHostGroup();
                        checkNodeNamePref = false;
                    } else if (!extRoleServiceGroupList.isEmpty()
                            && !extRoleServiceGroupList
                                    .contains(UserExtendedRoleBean.RESTRICTED_KEYWORD)) {
                        selectedNodeType = NodeType.SERVICE_GROUP;
                        selectedNodeName = userExtendedRoleBean
                                .getDefaultServiceGroup();
                        checkNodeNamePref = false;
                    }
                }

                // check for 'node name' filter
                if (checkNodeNamePref
                        && !selectedNodeType.equals(NodeType.NETWORK)) {
                    // Node Name
                    try {
                        selectedNodeName = FacesUtils
                                .getPreference(Constant.NODE_NAME_PREF);
                        if (null == selectedNodeName
                                || selectedNodeName
                                        .equals(Constant.EMPTY_STRING)) {
                            throw new PreferencesException();
                        }
                    } catch (PreferencesException e1) {
                        setMessage(true);
                        setInfo(true);
                        setInfoMessage(new PreferencesException().getMessage());
                        return;
                    }
                }
            }

            /*
             * check if selected node name is null then set default node type
             * and name.
             */
            if (selectedNodeName == null) {
                LOGGER
                        .debug("selectedNodeName is null ...Setting default(Entire network) node type and name");
                selectedNodeType = NodeType.NETWORK;
                selectedNodeId = 0;
                selectedNodeName = Constant.EMPTY_STRING;
            }

        } catch (Exception e) {
            LOGGER.error("Exception in initializeSubPageNode() method "
                    + e.getMessage());
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[Events Portlet] # Node Type [" + selectedNodeType
                    + "] # Node Name [" + selectedNodeName + "] # Node ID ["
                    + selectedNodeId + "]");
        }
    }

    /**
     * return event table page size from properties file .
     * 
     * @return int
     */
    private int getEventTablePageSize() {
        int eventPageSize;

        // eventPageSize from status-viewer properties other wise get from
        // Preferences
        try {
            String prefPageSize;
            try {
                prefPageSize = FacesUtils
                        .getPreference(EVENTS_PER_PAGE_PREFERENCE);
            } catch (PreferencesException e) {
                prefPageSize = null;
            }
            if (null != prefPageSize
                    && !Constant.EMPTY_STRING.equals(prefPageSize)) {
                eventPageSize = Integer.parseInt(prefPageSize);
            } else {
                eventPageSize = Integer.parseInt(PropertyUtils
                        .getProperty(ApplicationType.STATUS_VIEWER,
                                Constant.EVENT_PAGE_SIZE));
            }

        } catch (NumberFormatException numberFormatException) {
            eventPageSize = Constant.FIVE;
        }

        return eventPageSize;
    }

    /**
     * constructor
     */
    public EventDataTableBean() {
        super(Integer.parseInt(PropertyUtils.getProperty(
                ApplicationType.STATUS_VIEWER, Constant.EVENT_PAGE_SIZE)));
        // get the UserRoleBean managed instance
        userExtendedRoleBean = PortletUtils.getUserExtendedRoleBean();
        this.init();

        if (!inStatusViewer) {
            int randomID = new Random().nextInt(TEN_THOUSAND);
            // Unique id for form UI component
            dashboradformID = "eventPortlet_frm" + randomID;
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
     * Sets the events for the data table.
     * 
     * @param events
     */
    public void setEvents(EventBean[] events) {
        this.events = events;
    }

    /**
     * Gets the selectedRows for the data table.
     * 
     * @return List
     */
    public List<EventBean> getSelectedRows() {

        return selectedRows;
    }

    /**
     * get dynamicColumns
     * 
     * @return EntityTypeProperty
     */
    public EntityTypeProperty[] getDynamicColumns() {
        return dynamicColumns;
    }

    /**
     * set dynamicColumns
     * 
     * @param dynamicColumns
     */
    public void setDynamicColumns(EntityTypeProperty[] dynamicColumns) {
        this.dynamicColumns = dynamicColumns;
    }

    /**
     * Sets the selectedRows from the data table.
     * 
     * @param selectedRows
     */
    public void setSelectedRows(List<EventBean> selectedRows) {
        this.selectedRows = selectedRows;
    }

    /**
     * Method for the row selection listener.
     * 
     * @param event
     */
    public void rowSelection(RowSelectorEvent event) {

        selectedRows = new ArrayList<EventBean>();
        for (int i = events.length - 1; i >= 0; i--) {
            if (events[i].isSelected()) {
                selectedRows.add(events[i]);
                this.setRowSelected(true);
                this.setEnablePopUpMenu(true);

            }
        } // end for
        EventMenuActionBean action = (EventMenuActionBean) FacesUtils
                .getManagedBean(Constant.EVENT_MENU_ACTION_BEAN);
        // if pop up menu item action is performed then reset of selected event.
        if (isEnablePopUpMenu() && !isPopUpmenuClicked()) {
            // check nagios acknowledge should be render or not.if selected
            // event application type is nagios then nagois menu item render.
            if (action != null
                    && action.isSingleAppType(selectedRows
                            .toArray(new EventBean[selectedRows.size()]))) {
                String applicationType = action.getSingleAppType();
                if (Constant.NAGIOS.equalsIgnoreCase(applicationType)) {
                    this.isnagiosAcknowledge = true;
                }
                if (Constant.SNMPTRAP.equalsIgnoreCase(applicationType)
                        || Constant.SYSLOG.equalsIgnoreCase(applicationType)) {
                    this.isSyslogORSnmptrapMenu = true;
                }
            }
            this.constructComponent();
            this.isnagiosAcknowledge = false;
            this.isSyslogORSnmptrapMenu = false;
        } else {
            this.resetEvents();
            this.setPopUpmenuClicked(false);
            selectedRows.clear();

        }
        // Synch up the messageSelectBean
        boolean rowSel = ((RowSelector) event.getComponent()).getValue();
        if (rowSel) {
            LOGGER.debug("Event selected=" + rowSel);
            EventFreezeBean eventFreezeBean = (EventFreezeBean) FacesUtils
                    .getManagedBean(Constant.EVENT_FREEZE_BEAN);
            if (eventFreezeBean != null) {
                eventFreezeBean.freeze(true);
            }

        } // end if
        EventMessageSelectBean msgSelectBean = (EventMessageSelectBean) FacesUtils
                .getManagedBean(Constant.EVENT_MESSAGE_SELECT_BEAN);
        msgSelectBean.setAllRows(selectedRows
                .toArray(new EventBean[selectedRows.size()]));
        if (selectedRows.size() <= 0) {

            msgSelectBean.setSelectAllButtonText(ResourceUtils
                    .getLocalizedMessage(Constant.SELECT_ALL_BUTTON_LABEL));
        } // end if

        if (action != null) {
            MenuItem menu = action.getMenuModel().get(0);
            // check if row is selected then set icon other wise clear the sub
            // menu and pop up menu.
            if (selectedRows.size() >= 1) {
                menu.setIcon(Constant.EMPTY_STRING);

            } else {
                menu.setIcon(Constant.EMPTY_STRING);
                menu.getChildren().clear();
                this.setEnablePopUpMenu(false);
                this.constructComponent();

            } // end if
            try {
                action.menuListener();
            } catch (WSDataUnavailableException e) {
                setMessage(true);
                setError(true);
                setErrorMessage(e.getMessage());
            } catch (GWPortalException e) {
                setMessage(true);
                setError(true);
                setErrorMessage(e.getMessage());
            }
        }

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
     * Sets the multiple selection property for the data table.This can be used
     * dynamically to turn ON and OFF the multiple selection.
     * 
     * @param multipleSelection
     */
    public void setMultipleSelection(boolean multipleSelection) {
        this.multipleSelection = multipleSelection;
    }

    /**
     * Gets the column names
     * 
     * @return ArrayList
     */
    public ArrayList<String> getColumnNames() {
        return columnNames;
    }

    /**
     * Sets the column names
     * 
     * @param columnNames
     */
    public void setColumnNames(ArrayList<String> columnNames) {
        this.columnNames = columnNames;
    }

    /**
     * Gets the HTMLTable
     * 
     * @return HtmlDataTable
     */
    public HtmlDataTable getEventDataTable() {

        return eventDataTable;
    }

    /**
     * Sets the HTMLTable
     * 
     * @param eventDataTable
     */
    public void setEventDataTable(HtmlDataTable eventDataTable) {
        this.eventDataTable = eventDataTable;
    }

    /**
     * Constructs data table
     */
    public void constructComponent() {
        if (inStatusViewer) {

            switch (selectedNodeType) {
                case HOST_GROUP:

                    if (hgEventDataTable == null) {
                        // Always create a new HTML table
                        hgEventDataTable = new HtmlDataTable();
                    } // end if
                    hgEventDataTable.setRows(this.getTableRows());

                    List<UIComponent> hglist = hgEventDataTable.getChildren();
                    hglist.clear();

                    this.addBuiltInProperties(hgEventDataTable);
                    this.addDynamicProperties(hgEventDataTable);
                    this.addAcknowledgementColumns(hgEventDataTable);
                    break;
                case HOST:
                    if (eventDataTable == null) {
                        // Always create a new HTML table
                        eventDataTable = new HtmlDataTable();
                    } // end if

                    eventDataTable.setRows(this.getTableRows());

                    List<UIComponent> list = eventDataTable.getChildren();
                    list.clear();

                    this.addBuiltInProperties(eventDataTable);
                    this.addDynamicProperties(eventDataTable);
                    this.addAcknowledgementColumns(eventDataTable);
                    break;
                case SERVICE_GROUP:
                    // service group view html event data table instance
                    if (sgEventDataTable == null) {
                        // Always create a new HTML table
                        sgEventDataTable = new HtmlDataTable();
                    } // end if
                    sgEventDataTable.setRows(this.getTableRows());
                    List<UIComponent> sglist = sgEventDataTable.getChildren();
                    sglist.clear();
                    this.addBuiltInProperties(sgEventDataTable);
                    this.addDynamicProperties(sgEventDataTable);
                    this.addAcknowledgementColumns(sgEventDataTable);
                    break;
                case SERVICE:
                    // service view html event data table instance
                    if (serviceEventDataTable == null) {
                        // Always create a new HTML table
                        serviceEventDataTable = new HtmlDataTable();
                    } // end if
                    serviceEventDataTable.setRows(this.getTableRows());

                    List<UIComponent> serviceEventDataTablelist = serviceEventDataTable
                            .getChildren();
                    serviceEventDataTablelist.clear();

                    this.addBuiltInProperties(serviceEventDataTable);
                    this.addDynamicProperties(serviceEventDataTable);
                    this.addAcknowledgementColumns(serviceEventDataTable);
                    break;

                default:
                    break;

            }

        } else {

            if (eventDataTable == null) {
                // Always create a new HTML table
                eventDataTable = new HtmlDataTable();
            } // end if

            eventDataTable.setRows(this.getTableRows());

            List<UIComponent> list = eventDataTable.getChildren();
            list.clear();

            this.addBuiltInProperties(eventDataTable);
            this.addDynamicProperties(eventDataTable);
        }
    }

    /**
     * Initialize data table components .
     */
    public void initializeComponent() {
        if (inStatusViewer) {

            // host group view html event data table instance
            if (hgEventDataTable == null) {
                // Always create a new HTML table
                hgEventDataTable = new HtmlDataTable();
            } // end if
            hgEventDataTable.setRows(this.getTableRows());
            List<UIComponent> hglist = hgEventDataTable.getChildren();
            hglist.clear();
            this.addBuiltInProperties(hgEventDataTable);
            this.addDynamicProperties(hgEventDataTable);

            // host view html event data table instance
            if (eventDataTable == null) {
                // Always create a new HTML table
                eventDataTable = new HtmlDataTable();
            } // end if
            eventDataTable.setRows(this.getTableRows());
            List<UIComponent> list = eventDataTable.getChildren();
            list.clear();
            this.addBuiltInProperties(eventDataTable);
            this.addDynamicProperties(eventDataTable);

            // service group view html event data table instance
            if (sgEventDataTable == null) {
                // Always create a new HTML table
                sgEventDataTable = new HtmlDataTable();
            } // end if
            sgEventDataTable.setRows(this.getTableRows());
            List<UIComponent> sglist = sgEventDataTable.getChildren();
            sglist.clear();
            this.addBuiltInProperties(sgEventDataTable);
            this.addDynamicProperties(sgEventDataTable);

            // service view html event data table instance
            if (serviceEventDataTable == null) {
                // Always create a new HTML table
                serviceEventDataTable = new HtmlDataTable();
            } // end if
            serviceEventDataTable.setRows(this.getTableRows());

            List<UIComponent> serviceEventDataTablelist = serviceEventDataTable
                    .getChildren();
            serviceEventDataTablelist.clear();

            this.addBuiltInProperties(serviceEventDataTable);
            this.addDynamicProperties(serviceEventDataTable);

        } else {

            if (eventDataTable == null) {
                // Always create a new HTML table
                eventDataTable = new HtmlDataTable();
            } // end if

            eventDataTable.setRows(this.getTableRows());

            List<UIComponent> list = eventDataTable.getChildren();
            list.clear();

            this.addBuiltInProperties(eventDataTable);
            this.addDynamicProperties(eventDataTable);
        }
    }

    /**
     * This method create data table component like column,header,row selected
     * etc. and add in HtmlDataTable object as a child
     */

    private void addBuiltInProperties(HtmlDataTable eventDataTable) {
        ExpressionFactory ef = application.getExpressionFactory();
        if (FacesContext.getCurrentInstance() != null) {
            viewRoot = FacesContext.getCurrentInstance().getViewRoot();
        }
        if (eventColumns != null) {
            columnNames = new ArrayList<String>();
            StringTokenizer stringTokenizer = new StringTokenizer(eventColumns,
                    Constant.COMMA);
            while (stringTokenizer.hasMoreTokens()) {
                String column = stringTokenizer.nextToken();
                StringTokenizer stknLabel = new StringTokenizer(column,
                        Constant.COLON);
                columnNames.add(stknLabel.nextToken());
                columnLabels.add(stknLabel.nextToken());
            }
        } else {
            LOGGER.debug("No built_in_columns property specified.");
        }

        // First populate "more column" and the built-in columns and then the
        // dynamic ones
        this.addDynamicPropColumn(ef,eventDataTable);
        // First populate the built-in columns and then the dynamic ones
        int columnCount = columnNames.size();
        for (int index = 0; index < columnCount; index++) {
            String columnName = columnNames.get(index);
            String columnLabel = columnLabels.get(index);
            String columnBinding = Constant.EVENT_BEAN_EL + columnName
                    + Constant.CLOSED_BRACE;

            UIColumn column = new UIColumn();
            column.setId(Constant.COL + columnName);
            // add row selector
            if (index == 0) {
                RowSelector rowSelector = new RowSelector();
                rowSelector.setId(Constant.RS + columnName);
                ValueExpression rsBindValExp = ef.createValueExpression(
                        FacesContext.getCurrentInstance().getELContext(),
                        Constant.EVENT_SELECT_ED, boolean.class);

                rowSelector.setValueExpression(Constant.VALUE, rsBindValExp);
                ValueExpression msBindValExp = ef
                        .createValueExpression(
                                FacesContext.getCurrentInstance()
                                        .getELContext(),
                                Constant.EVENT_LIST_BEAN_DATA_TABLE_BEAN_MULTIPLE_SELECTION,
                                boolean.class);

                rowSelector.setValueExpression(Constant.MULTIPLE, msBindValExp);
                Class[] ca = { com.icesoft.faces.component.ext.RowSelectorEvent.class };
                // method binding class has been replaced by MethodExpression
                // but icefaces has not updated our selection listener method.
                javax.faces.el.MethodBinding mb = application
                        .createMethodBinding(
                                Constant.EVENT_LIST_BEAN_DATA_TABLE_BEAN_ROW_SELECTION,
                                ca);
                rowSelector.setSelectionListener(mb);
                rowSelector
                        .setMouseOverClass(Constant.ICE_ROW_SEL_SELECTED_MOUSE_OVER);
                rowSelector.setSelectedClass(Constant.ICE_ROW_SEL_SELECTED);
                rowSelector
                        .setSelectedMouseOverClass(Constant.ICE_ROW_SEL_SELECTED);

                column.getChildren().add(rowSelector);

            } // end if
            HtmlPanelGrid htmlPgHeader = new HtmlPanelGrid();
            htmlPgHeader.setId(Constant.SORT_HEADER_ID + columnName);
            htmlPgHeader.setColumns(2);
            if (Constant.TEXT_MESSAGE.equalsIgnoreCase(columnName)) {
                htmlPgHeader.setStyle(Constant.WIDTH_300PX);
            } else {
                htmlPgHeader.setStyle(Constant.CELLPADDING_0_CELLSPACING_0);
            }

            HtmlCommandLink sortHeader = new HtmlCommandLink();
            sortHeader.setId(Constant.HEADER + columnName);
            sortHeader.setAccesskey(columnName);
            // Column labels go here
            sortHeader.setValue(columnLabel);
            // Css applied on Header of table
            sortHeader.setStyleClass(Constant.ICE_OUT_TXT);
            sortHeader
                    .setActionListener(createActionListenerMethodBinding(Constant.EVENT_LIST_BEAN_DATA_TABLE_BEAN_SORT));
            htmlPgHeader.getChildren().add(sortHeader);

            if (sortColumnName.equalsIgnoreCase(columnName)) {
                arrowImg = new HtmlGraphicImage();
                arrowImg.setId(Constant.SORT_IMG);
                if (ascending) {
                    arrowImg.setUrl(Constant.IMAGES_SORT_ARROW_UP_GIF);
                } else {
                    arrowImg.setUrl(Constant.IMAGES_SORT_ARROW_DOWN_GIF);
                }
                htmlPgHeader.getChildren().add(arrowImg);
            } // end if

            column.setHeader(htmlPgHeader);

            HtmlOutputText text = new HtmlOutputText();
            text.setId(Constant.TXT + columnName);

            HtmlPanelGroup htmlpg = new HtmlPanelGroup();
            htmlpg.setId(Constant.PNL + columnName);

            // set menu pop id on panel group
            htmlpg.setMenuPopup(Constant.MNU_POP_UP_EVENT);
            // create pop up menu component.
            this.createPopUpMenuComponent();
            column.getChildren().add(eventMenuPop);
            // check if column name is status bean then set value and style
            // class on panel group.
            if (columnName.equalsIgnoreCase(Constant.STATUS_BEAN) || columnName.equalsIgnoreCase(Constant.EL_BIND_COL_SEVERITY)) {
                ValueExpression styleClassBindValExp = ef
                        .createValueExpression(FacesContext
                                .getCurrentInstance().getELContext(),
                                Constant.EVENT_BEAN_EL + columnName
                                        + Constant.STYLE_CLASS_EL, String.class);
                column.setValueExpression(Constant.STYEL_CLASS,
                        styleClassBindValExp);

                ValueExpression textValBindValExp = ef
                        .createValueExpression(FacesContext
                                .getCurrentInstance().getELContext(),
                                Constant.EVENT_BEAN_EL + columnName
                                        + Constant.VALUE_EL, String.class);
                text.setValueExpression(Constant.VALUE, textValBindValExp);
                htmlpg.getChildren().add(text);

            } else if (columnName
                    .equalsIgnoreCase(Constant.DEFAULT_SORT_COLUMN_NAME)) {

                ValueExpression textValBindValExp = ef.createValueExpression(
                        FacesContext.getCurrentInstance().getELContext(),
                        columnBinding, String.class);
                text.setValueExpression(Constant.VALUE, textValBindValExp);
                htmlpg.getChildren().add(text);

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
                htmlpg.setStyleClass(Constant.TABLE_COLUMN);

                ValueExpression textValBindValExp = ef.createValueExpression(
                        FacesContext.getCurrentInstance().getELContext(),
                        columnBinding, String.class);
                text.setValueExpression(Constant.VALUE, textValBindValExp);
                htmlpg.getChildren().add(text);
            } // end if
            column.getChildren().add(htmlpg);
            eventDataTable.getChildren().add(column);
        } // end for

    }

    private void addDynamicPropColumn(ExpressionFactory ef,HtmlDataTable eventDataTable) {

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
        eventDataTable.getChildren().add(column);
    }


    /**
     * Add acknowledgement columns
     */
    private void addAcknowledgementColumns(HtmlDataTable eventDataTable) {
        ExpressionFactory ef = application.getExpressionFactory();
        for (String dynColumnName : ACKNOWLEDGEMENT_COLUMNS) {
            UIColumn dynColumn = new UIColumn();
            LOGGER.debug("Ack column=" + dynColumnName);
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
            LOGGER.debug("Adding ack column " + dynColumnName);
        }

    }

    /**
     * 
     */
    private void createPopUpMenuComponent() {
        // creating menu pop up
        eventMenuPop = new MenuPopup();
        eventMenuPop.setId(Constant.MNU_POP_UP_EVENT); //
        // create menu item for pop up menu
        eventMenuPop.setRendered(enablePopUpMenu);
        Class[] menuItemClass = { ActionEvent.class };
        // method binding class has been replaced by MethodExpression
        // but icefaces has not updated our listener method.
        javax.faces.el.MethodBinding popUpmenuMethodBinding = application
                .createMethodBinding(
                        Constant.EVENT_MENU_ACTION_BEAN_MENU_POP_UP_LISTENER,
                        menuItemClass);
        // accept log message menu item

        acceptMenuItem = new MenuItem();
        acceptMenuItem.setId(Constant.ACCEPT_LOG_MESSAGE_ID);
        acceptMenuItem.setValue(Constant.ACCEPT_LOG_MESSAGE);
        acceptMenuItem.setActionListener(popUpmenuMethodBinding);
        UIParameter acceptMenuParam = new UIParameter();
        acceptMenuParam.setName(Constant.MENU_POP_UP_PARAM);
        acceptMenuParam.setValue(Constant.ACCEPT);
        acceptMenuItem.getChildren().add(acceptMenuParam);
        // notify log message menu item
        notifyMenuItem = new MenuItem();
        notifyMenuItem.setId(Constant.NOTIFY_LOG_MESSAGE_ID);
        notifyMenuItem.setValue(Constant.NOTIFY_LOG_MESSAGE);
        notifyMenuItem.setActionListener(popUpmenuMethodBinding);
        UIParameter notifyMenuParam = new UIParameter();
        notifyMenuParam.setName(Constant.MENU_POP_UP_PARAM);
        notifyMenuParam.setValue(Constant.NOTIFY);
        notifyMenuItem.getChildren().add(notifyMenuParam);
        // close log message menu item
        closeMenuItem = new MenuItem();
        closeMenuItem.setId(Constant.CLOSE_LOG_MESSAGE_ID);
        closeMenuItem.setValue(Constant.CLOSE_LOG_MESSAGE);
        closeMenuItem.setActionListener(popUpmenuMethodBinding);
        UIParameter closeMenuParam = new UIParameter();
        closeMenuParam.setName(Constant.MENU_POP_UP_PARAM);
        closeMenuParam.setValue(Constant.CLOSE);
        closeMenuItem.getChildren().add(closeMenuParam);

        // open log message menu item
        openMenuItem = new MenuItem();
        openMenuItem.setId(Constant.OPEN_LOG_MESSAGE_ID);
        openMenuItem.setValue(Constant.OPEN_LOG_MESSAGE);
        openMenuItem.setActionListener(popUpmenuMethodBinding);
        UIParameter openMenuParam = new UIParameter();
        openMenuParam.setName(Constant.MENU_POP_UP_PARAM);
        openMenuParam.setValue(Constant.OPEN_MENU);
        openMenuItem.getChildren().add(openMenuParam);

        // nagios Menu Item
        nagiosMenuItem = new MenuItem();
        nagiosMenuItem.setId(Constant.NAGIOS_ACKNOWLEDGE_ID);
        nagiosMenuItem.setValue(Constant.NAGIOS_ACKNOWLEDGE);
        nagiosMenuItem.setActionListener(popUpmenuMethodBinding);
        UIParameter nagiosMenuParam = new UIParameter();
        nagiosMenuParam.setName(Constant.MENU_POP_UP_PARAM);
        nagiosMenuParam.setValue(Constant.NAGIOS);
        nagiosMenuItem.getChildren().add(nagiosMenuParam);
        nagiosMenuItem.setRendered(isnagiosAcknowledge);

        // syslog or snmp trap Menu Item
        sysLogORSnmptrapMenuItem = new MenuItem();
        sysLogORSnmptrapMenuItem.setId(Constant.SYS_LOG_OR_SNMP_ID);
        sysLogORSnmptrapMenuItem.setValue(Constant.SUBMIT_PASSIVE_CHECK);
        sysLogORSnmptrapMenuItem.setActionListener(popUpmenuMethodBinding);
        UIParameter sysLogORSnmptrapParam = new UIParameter();
        sysLogORSnmptrapParam.setName(Constant.MENU_POP_UP_PARAM);
        sysLogORSnmptrapParam.setValue(Constant.PASSIVE);
        sysLogORSnmptrapMenuItem.getChildren().add(sysLogORSnmptrapParam);
        sysLogORSnmptrapMenuItem.setRendered(isSyslogORSnmptrapMenu);
        // add menu item in pop menu
        eventMenuPop.getChildren().add(acceptMenuItem);
        eventMenuPop.getChildren().add(notifyMenuItem);
        eventMenuPop.getChildren().add(closeMenuItem);
        eventMenuPop.getChildren().add(openMenuItem);
        eventMenuPop.getChildren().add(nagiosMenuItem);
        eventMenuPop.getChildren().add(sysLogORSnmptrapMenuItem);
    }

    /**
     * Getter for data paginator
     * 
     * @return DataPaginator
     */
    public DataPaginator getEventsPager() {
        return eventsPager;
    }

    /**
     * Setter for data paginator
     * 
     * @param eventsPager
     */
    public void setEventsPager(DataPaginator eventsPager) {
        this.eventsPager = eventsPager;
    }

    /**
     * Fetches the data page
     */
    @Override
    public DataPage fetchPage(int startRow, int pageSize) {
        // call enclosing managed bean method to fetch the data
        return getDataPage(startRow, pageSize);
    }

    /**
     * Gets the data page
     * 
     * @param startRow
     * @param pageSize
     * @return
     */
    private DataPage getDataPage(int startRow, int pageSize) {

        try {
            if (lastPage == null || startRow != lastStartRow) {
                Filter filter = null;
                List<EventBean> eventList = new Vector<EventBean>();
                try {

                    // set filter depending on sub page
                    this.setFilterForNodeType();
                    if (FacesContext.getCurrentInstance() != null) {
                        EventFilterBean localEventFilterBean = (EventFilterBean) FacesUtils
                                .getManagedBean(Constant.EVENT_FILTER_BEAN);
                        if (localEventFilterBean != null) {
                            filter = localEventFilterBean.getFilter();
                        } // end if
                    } else if (eventFilterBean != null) {
                        filter = eventFilterBean.getFilter();
                        // }
                    } // end if

                    Sort sort = new Sort(this.ascending, this.sortColumnName);
                    if (filter != null) {

                        if (dynamicColumns != null) {
                            eventList = new EventQueryManager()
                                    .queryForEventsByFilter(filter,
                                            dynamicColumns, startRow, sort,
                                            this.getTableRows());
                        } else {

                            eventList = new EventQueryManager()
                                    .queryForEventsByFilter(filter, startRow,
                                            sort, this.getTableRows());
                        } // end if

                    }
                } catch (GWPortalGenericException e) {
                    setMessage(true);
                    setError(true);
                    setErrorMessage(e.getMessage());
                }
                // end if
                int dataSetSize = 0;

                if (eventList.size() > 0) {
                    dataSetSize = eventList.get(0).getTotalCount();
                } // end if
                lastStartRow = startRow;
                lastPage = new DataPage(dataSetSize, startRow, eventList);
                events = eventList.toArray(new EventBean[eventList.size()]);
            }
        } catch (SessionExpiredException e) {
            // ignoring SessionExpiredException: User session has expired or it
            // was invalidated.
            LOGGER
                    .debug("User session has expired or it was invalidated. "
                            + e);
        } catch (Exception e) {
            LOGGER.error("Exception occur in getDataPage method" + e);
            setMessage(true);
            setError(true);
            setErrorMessage(e.getMessage());
        }
        return lastPage;

    }

    /**
     * Sets the last start row
     * 
     * @param lastStartRow
     */
    public void setLastStartRow(int lastStartRow) {
        this.lastStartRow = lastStartRow;
    }

    /**
     * Sets the last page
     * 
     * @param lastPage
     */
    public void setLastPage(DataPage lastPage) {
        this.lastPage = lastPage;
    }

    /**
     * 
     * @return String
     */
    public String getSortColumnName() {
        return sortColumnName;
    }

    /**
     * 
     * @param sortColumnName
     */
    public void setSortColumnName(String sortColumnName) {

        this.sortColumnName = sortColumnName;

    }

    /**
     * 
     * @return boolean
     */
    public boolean isAscending() {
        return ascending;
    }

    /**
     * 
     * @param ascending
     */
    public void setAscending(boolean ascending) {

        this.ascending = ascending;
    }

    /**
     * Creates an action listener method for the menu
     * 
     * @param actionListenerString
     * @return
     */

    private MethodBinding createActionListenerMethodBinding(
            String actionListenerString) {
        Class[] args = { ActionEvent.class };
        // method binding class has been replaced by MethodExpression
        // but icefaces has not updated our selection listener method.
        MethodBinding methodBinding = null;
        MethodBinding createMethodBinding = FacesContext.getCurrentInstance()
                .getApplication().createMethodBinding(actionListenerString,
                        args);
        methodBinding = createMethodBinding;
        return methodBinding;
    }

    /**
     * get column label of data table
     * 
     * @return ArrayList
     */
    public ArrayList<String> getColumnLabels() {
        return columnLabels;
    }

    /**
     * set column label of data table
     * 
     * @param columnLabels
     */
    public void setColumnLabels(ArrayList<String> columnLabels) {
        this.columnLabels = columnLabels;
    }

    /**
     * Sets the isRowSelected.
     * 
     * @param isRowSelected
     *            the isRowSelected to set
     */
    public void setRowSelected(boolean isRowSelected) {
        this.isRowSelected = isRowSelected;
    }

    /**
     * Returns the isRowSelected.
     * 
     * @return the isRowSelected
     */
    public boolean isRowSelected() {
        return isRowSelected;
    }

    /**
     * Sets the eventMenuPop.
     * 
     * @param eventMenuPop
     *            the eventMenuPop to set
     */
    public void setEventMenuPop(MenuPopup eventMenuPop) {
        this.eventMenuPop = eventMenuPop;
    }

    /**
     * Returns the eventMenuPop.
     * 
     * @return the eventMenuPop
     */
    public MenuPopup getEventMenuPop() {
        return eventMenuPop;
    }

    /**
     * Returns the acceptMenuItem.
     * 
     * @return the acceptMenuItem
     */
    public MenuItem getAcceptMenuItem() {
        return acceptMenuItem;
    }

    /**
     * Sets the acceptMenuItem.
     * 
     * @param acceptMenuItem
     *            the acceptMenuItem to set
     */
    public void setAcceptMenuItem(MenuItem acceptMenuItem) {
        this.acceptMenuItem = acceptMenuItem;
    }

    /**
     * Returns the notifyMenuItem.
     * 
     * @return the notifyMenuItem
     */
    public MenuItem getNotifyMenuItem() {
        return notifyMenuItem;
    }

    /**
     * Sets the notifyMenuItem.
     * 
     * @param notifyMenuItem
     *            the notifyMenuItem to set
     */
    public void setNotifyMenuItem(MenuItem notifyMenuItem) {
        this.notifyMenuItem = notifyMenuItem;
    }

    /**
     * Returns the closeMenuItem.
     * 
     * @return the closeMenuItem
     */
    public MenuItem getCloseMenuItem() {
        return closeMenuItem;
    }

    /**
     * Sets the closeMenuItem.
     * 
     * @param closeMenuItem
     *            the closeMenuItem to set
     */
    public void setCloseMenuItem(MenuItem closeMenuItem) {
        this.closeMenuItem = closeMenuItem;
    }

    /**
     * Returns the openMenuItem.
     * 
     * @return the openMenuItem
     */
    public MenuItem getOpenMenuItem() {
        return openMenuItem;
    }

    /**
     * Sets the openMenuItem.
     * 
     * @param openMenuItem
     *            the openMenuItem to set
     */
    public void setOpenMenuItem(MenuItem openMenuItem) {
        this.openMenuItem = openMenuItem;
    }

    /**
     * Returns the enablePopUpMenu.
     * 
     * @return the enablePopUpMenu
     */
    public boolean isEnablePopUpMenu() {
        return enablePopUpMenu;
    }

    /**
     * Sets the enablePopUpMenu.
     * 
     * @param enablePopUpMenu
     *            the enablePopUpMenu to set
     */
    public void setEnablePopUpMenu(boolean enablePopUpMenu) {
        this.enablePopUpMenu = enablePopUpMenu;
    }

    /**
     * reset all selected event.
     */
    public void resetEvents() {
        if (events != null) {
            for (int i = 0; i < events.length; i++) {
                events[i].setSelected(false);
            } // end for
        } // end if
    }

    /**
     * Sets the popUpmenuClicked.
     * 
     * @param popUpmenuClicked
     *            the popUpmenuClicked to set
     */
    public void setPopUpmenuClicked(boolean popUpmenuClicked) {
        this.popUpmenuClicked = popUpmenuClicked;
    }

    /**
     * Returns the popUpmenuClicked.
     * 
     * @return the popUpmenuClicked
     */
    public boolean isPopUpmenuClicked() {
        return popUpmenuClicked;
    }

    /**
     * Sets the nagiosMenuItem.
     * 
     * @param nagiosMenuItem
     *            the nagiosMenuItem to set
     */
    public void setNagiosMenuItem(MenuItem nagiosMenuItem) {
        this.nagiosMenuItem = nagiosMenuItem;
    }

    /**
     * Returns the nagiosMenuItem.
     * 
     * @return the nagiosMenuItem
     */
    public MenuItem getNagiosMenuItem() {
        return nagiosMenuItem;
    }

    /**
     * Listener for sorting. This method is responsible to sort data table
     * column and set appropriate image on column.
     * 
     * @param event
     * 
     */
    public void sort(ActionEvent event) {

        UIComponent comp = event.getComponent();

        if (comp instanceof com.icesoft.faces.component.ext.HtmlCommandLink) {
            HtmlCommandLink htmlcommandLink = (HtmlCommandLink) comp;
            this.sortColumnName = htmlcommandLink.getAccesskey();
            ascending = !ascending;
            if (arrowImg == null) {
                arrowImg = new HtmlGraphicImage();
                arrowImg.setId(Constant.SORT_IMG);
            }
            if (ascending) {
                arrowImg.setUrl(Constant.IMAGES_SORT_ARROW_UP_GIF);
            } else {
                arrowImg.setUrl(Constant.IMAGES_SORT_ARROW_DOWN_GIF);
            }
            htmlcommandLink.getParent().getChildren().add(arrowImg);
        } // end if

        // reset all selected rows
        EventMessageSelectBean msgSelectBean = (EventMessageSelectBean) FacesUtils
                .getManagedBean(Constant.EVENT_MESSAGE_SELECT_BEAN);
        EventMenuActionBean eventMenuActionBean = (EventMenuActionBean) FacesUtils
                .getManagedBean(Constant.EVENT_MENU_ACTION_BEAN);
        eventMenuActionBean.reset();
        if (msgSelectBean.getAllRows() != null
                && msgSelectBean.getAllRows().length > 0) {
            msgSelectBean.reset();
        } // end if
        lastPage = null;
        page = fetchPage(0, this.getTableRows());
        List<EventBean> eventList = page.getData();
        events = eventList.toArray(new EventBean[eventList.size()]);

    }

    /**
     * set filter depending on sub page
     * 
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     * @throws PreferencesException
     * 
     * 
     * */
    private void setFilterForNodeType() throws WSDataUnavailableException,
            GWPortalException, PreferencesException {

        if (eventHandler == null) {
            if (FacesContext.getCurrentInstance() == null) {
                eventHandler = new EventHandler(this.eventFilterBean);
            } else {
                eventHandler = new EventHandler(null);
            }
        }
        switch (selectedNodeType) {
            case NETWORK:
                eventHandler.setFilterForNetWork();
                break;
            case HOST_GROUP:
                eventHandler.setFilterForHostGroup(selectedNodeName);
                break;
            case HOST:
                eventHandler.setFilterForHost(selectedNodeName);
                break;
            case SERVICE_GROUP:
                eventHandler.setFilterForServiceGroup(selectedNodeName);
                break;
            case SERVICE:
                eventHandler
                        .setFilterForService(selectedNodeId, inStatusViewer);
            default:
                break;

        }

    }

    /**
     * Returns the isnagiosAcknowledge.
     * 
     * @return the isnagiosAcknowledge
     */
    public boolean isIsnagiosAcknowledge() {
        return isnagiosAcknowledge;
    }

    /**
     * Sets the isnagiosAcknowledge.
     * 
     * @param isnagiosAcknowledge
     *            the isnagiosAcknowledge to set
     */
    public void setIsnagiosAcknowledge(boolean isnagiosAcknowledge) {
        this.isnagiosAcknowledge = isnagiosAcknowledge;
    }

    /**
     * Sets the syslogORSnmptrapMenuItem.
     * 
     * @param syslogORSnmptrapMenuItem
     *            the syslogORSnmptrapMenuItem to set
     */
    public void setSyslogORSnmptrapMenuItem(MenuItem syslogORSnmptrapMenuItem) {
        sysLogORSnmptrapMenuItem = syslogORSnmptrapMenuItem;
    }

    /**
     * Returns the isSyslogORSnmptrapMenu.
     * 
     * @return the isSyslogORSnmptrapMenu
     */
    public boolean isSyslogORSnmptrapMenu() {
        return isSyslogORSnmptrapMenu;
    }

    /**
     * Sets the isSyslogORSnmptrapMenu.
     * 
     * @param isSyslogORSnmptrapMenu
     *            the isSyslogORSnmptrapMenu to set
     */
    public void setSyslogORSnmptrapMenu(boolean isSyslogORSnmptrapMenu) {
        this.isSyslogORSnmptrapMenu = isSyslogORSnmptrapMenu;
    }

    /**
     * Returns the sysLogORSnmptrapMenuItem.
     * 
     * @return the sysLogORSnmptrapMenuItem
     */
    public MenuItem getSysLogORSnmptrapMenuItem() {
        return sysLogORSnmptrapMenuItem;
    }

    /**
     * Sets the sysLogORSnmptrapMenuItem.
     * 
     * @param sysLogORSnmptrapMenuItem
     *            the sysLogORSnmptrapMenuItem to set
     */
    public void setSysLogORSnmptrapMenuItem(MenuItem sysLogORSnmptrapMenuItem) {
        this.sysLogORSnmptrapMenuItem = sysLogORSnmptrapMenuItem;
    }

    /**
     * Sets the tableRows.
     * 
     * @param tableRows
     *            the tableRows to set
     */
    public void setTableRows(int tableRows) {
        this.tableRows = tableRows;
    }

    /**
     * Returns the tableRows.
     * 
     * @return the tableRows
     */
    public int getTableRows() {
        return tableRows;
    }

    /**
     * Sets the error.
     * 
     * @param error
     *            the error to set
     */
    public void setError(boolean error) {
        this.error = error;
    }

    /**
     * Returns the error.
     * 
     * @return the error
     */
    public boolean isError() {
        return error;
    }

    /**
     * Sets the info.
     * 
     * @param info
     *            the info to set
     */
    public void setInfo(boolean info) {
        this.info = info;
    }

    /**
     * Returns the info.
     * 
     * @return the info
     */
    public boolean isInfo() {
        return info;
    }

    /**
     * Sets the message.
     * 
     * @param message
     *            the message to set
     */
    public void setMessage(boolean message) {
        this.message = message;
    }

    /**
     * Returns the message.
     * 
     * @return the message
     */
    public boolean isMessage() {
        return message;
    }

    /**
     * Sets the infoMessage.
     * 
     * @param infoMessage
     *            the infoMessage to set
     */
    public void setInfoMessage(String infoMessage) {
        this.infoMessage = infoMessage;
    }

    /**
     * Returns the infoMessage.
     * 
     * @return the infoMessage
     */
    public String getInfoMessage() {
        return infoMessage;
    }

    /**
     * Sets the errorMessage.
     * 
     * @param errorMessage
     *            the errorMessage to set
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Returns the errorMessage.
     * 
     * @return the errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * @param eventFilterBean
     */
    public void setEventFilterBean(EventFilterBean eventFilterBean) {
        this.eventFilterBean = eventFilterBean;
    }

    /**
     * Sets the eventHiddenField.
     * 
     * @param eventHiddenField
     *            the eventHiddenField to set
     */
    public void setEventHiddenField(String eventHiddenField) {
        this.eventHiddenField = eventHiddenField;
    }

    /**
     * Returns the eventHiddenField. This gets used in Dashboard for checking if
     * Host Group name set as a preference by user is valid or not.
     * 
     * @return the eventHiddenField
     */
    public String getEventHiddenField() {
        if (null != subpageIntegrator && !inStatusViewer) {
            handleDashboardProcessing();
            return eventHiddenField;
        }

        if (inStatusViewer) {
            if (eventHandler == null) {
                if (FacesContext.getCurrentInstance() == null) {
                    eventHandler = new EventHandler(this.eventFilterBean);
                } else {
                    eventHandler = new EventHandler(null);
                }
            }

            // fetch the latest nav params
            subpageIntegrator.setNavigationParameters();
            // check for node type and node Id
            int nodeID = subpageIntegrator.getNodeID();
            NodeType nodeType = subpageIntegrator.getNodeType();

            if (nodeID != selectedNodeId || !nodeType.equals(selectedNodeType)) {

                switch (selectedNodeType) {
                    case HOST:
                        if (dataPaginator != null) {
                            dataPaginator.gotoFirstPage();

                        }
                        break;
                    case HOST_GROUP:
                        if (hgDataPaginator != null) {
                            hgDataPaginator.gotoFirstPage();

                        }
                        break;
                    case SERVICE:
                        if (serviceDataPaginator != null) {
                            serviceDataPaginator.gotoFirstPage();

                        }
                        break;
                    case SERVICE_GROUP:
                        if (sgDataPaginator != null) {
                            sgDataPaginator.gotoFirstPage();

                        }
                        break;
                    default:
                        break;
                }

                // update node type vals
                selectedNodeType = nodeType;
                selectedNodeName = subpageIntegrator.getNodeName();
                selectedNodeId = nodeID;

                // default sorting order.
                ascending = false;

                this.sortColumnName = Constant.DEFAULT_SORT_COLUMN_NAME;

                EventMenuActionBean action = (EventMenuActionBean) FacesUtils
                        .getManagedBean(Constant.EVENT_MENU_ACTION_BEAN);
                if (action != null) {
                    // reset selected menu for previous node
                    action.reset();
                }
                EventMessageSelectBean msgSelectBean = (EventMessageSelectBean) FacesUtils
                        .getManagedBean(Constant.EVENT_MESSAGE_SELECT_BEAN);
                if (msgSelectBean != null) {
                    msgSelectBean.reset();
                }
                // setting popUpmenuClicked as default value
                this.setPopUpmenuClicked(false);
                this.constructComponent();
                EventFreezeBean eventFreezeBean = (EventFreezeBean) FacesUtils
                        .getManagedBean(Constant.EVENT_FREEZE_BEAN);
                if (eventFreezeBean != null) {
                    eventFreezeBean.freeze(false);
                }

                // refreshing data table as per new selected node type
                eventHandler.refreshDataTable(null, selectedNodeType,
                        selectedNodeName, selectedNodeId);
            }
        }
        return eventHiddenField;
    }

    /**
     * Handles dashboard processing - validates node names.
     */
    private void handleDashboardProcessing() {
        // create the foundationWSFacade instance
        IWSFacade foundationWSFacade = new WebServiceFactory()
                .getWebServiceInstance(WebServiceType.FOUNDATION_WEBSERVICE);

        // if in dashboard, check if provided preference parameter is valid.
        switch (selectedNodeType) {
            case HOST:
                // validate if entered Host exists
                try {
                    SimpleHost hostByName = foundationWSFacade
                            .getSimpleHostByName(selectedNodeName, false);
                    if (null == hostByName) {
                        throw new WSDataUnavailableException();
                    }
                    /*
                     * set the selected node Id here (seems weird but required
                     * for JMS Push in Dashboard)
                     */
                    selectedNodeId = hostByName.getHostID();

                    // check for extended role permissions
                    if (!referenceTreeModel
                            .checkNodeForExtendedRolePermissions(
                                    selectedNodeId, NodeType.HOST,
                                    selectedNodeName, userExtendedRoleBean
                                            .getExtRoleHostGroupList(),
                                    userExtendedRoleBean
                                            .getExtRoleServiceGroupList())) {
                        String inadequatePermissionsMessage = ResourceUtils
                                .getLocalizedMessage("com_groundwork_portal_statusviewer_inadequate_permissions")
                                + " [" + selectedNodeName + "]";
                        setMessage(true);
                        setInfo(true);
                        setInfoMessage(inadequatePermissionsMessage);
                    }
                } catch (WSDataUnavailableException e) {
                    String hostNotAvailableErrorMessage = ResourceUtils
                            .getLocalizedMessage("com_groundwork_portal_statusviewer_hostUnavailable")
                            + " [" + selectedNodeName + "]";
                    setMessage(true);
                    setInfo(true);
                    setInfoMessage(hostNotAvailableErrorMessage);
                } catch (GWPortalGenericException e) {
                    LOGGER
                            .warn(
                                    "Exception while retrieving Host By Name in Dashboard. JMS PUSH may not work. Exception ["
                                            + e.getMessage() + "]", e);
                }
                break;

            case SERVICE_GROUP:
                // validate if entered Service Group exists
                try {
                    Category category = foundationWSFacade
                            .getCategoryByName(selectedNodeName);
                    if (null == category) {
                        // show appropriate error message to the user
                        String serviceGroupNotAvailableErrorMessage = ResourceUtils
                                .getLocalizedMessage("com_groundwork_portal_statusviewer_serviceGroupUnavailable")
                                + " [" + selectedNodeName + "]";
                        LOGGER.error(serviceGroupNotAvailableErrorMessage);
                        setMessage(true);
                        setInfo(true);
                        setInfoMessage(serviceGroupNotAvailableErrorMessage);
                        return;
                    }
                    // set the selected node Id
                    selectedNodeId = category.getCategoryId();

                    // check for extended role permissions
                    if (!userExtendedRoleBean.getExtRoleServiceGroupList()
                            .isEmpty()
                            && !referenceTreeModel
                                    .checkNodeForExtendedRolePermissions(
                                            selectedNodeId,
                                            NodeType.SERVICE_GROUP,
                                            selectedNodeName,
                                            userExtendedRoleBean
                                                    .getExtRoleHostGroupList(),
                                            userExtendedRoleBean
                                                    .getExtRoleServiceGroupList())) {
                        String inadequatePermissionsMessage = ResourceUtils
                                .getLocalizedMessage("com_groundwork_portal_statusviewer_inadequate_permissions")
                                + " [" + selectedNodeName + "]";
                        setMessage(true);
                        setInfo(true);
                        setInfoMessage(inadequatePermissionsMessage);
                    }
                } catch (GWPortalGenericException e) {
                    LOGGER
                            .warn(
                                    "Exception while retrieving Service Group By Name in Dashboard. JMS PUSH may not work. Exception ["
                                            + e.getMessage() + "]", e);
                }
                break;

            case HOST_GROUP:
                try {
                    HostGroup hostGroupByName = foundationWSFacade
                            .getHostGroupsByName(selectedNodeName);
                    selectedNodeId = hostGroupByName.getHostGroupID();

                    // check for extended role permissions
                    if (!userExtendedRoleBean.getExtRoleHostGroupList()
                            .isEmpty()
                            && !referenceTreeModel
                                    .checkNodeForExtendedRolePermissions(
                                            selectedNodeId,
                                            NodeType.HOST_GROUP,
                                            selectedNodeName,
                                            userExtendedRoleBean
                                                    .getExtRoleHostGroupList(),
                                            userExtendedRoleBean
                                                    .getExtRoleServiceGroupList())) {
                        String inadequatePermissionsMessage = ResourceUtils
                                .getLocalizedMessage("com_groundwork_portal_statusviewer_inadequate_permissions")
                                + " [" + selectedNodeName + "]";
                        setMessage(true);
                        setInfo(true);
                        setInfoMessage(inadequatePermissionsMessage);
                    }
                } catch (WSDataUnavailableException e) {
                    String hostGroupNotAvailableErrorMessage = ResourceUtils
                            .getLocalizedMessage("com_groundwork_portal_statusviewer_hostGroupUnavailable")
                            + " [" + selectedNodeName + "]";
                    LOGGER.error(hostGroupNotAvailableErrorMessage);
                    setMessage(true);
                    setInfo(true);
                    setInfoMessage(hostGroupNotAvailableErrorMessage);
                } catch (GWPortalGenericException e) {
                    LOGGER
                            .warn(
                                    "Exception while retrieving Host Group By Name. JMS PUSH may not work. Exception ["
                                            + e.getMessage() + "]", e);
                }

                break;

            /*case NETWORK:
                // check for extended role permissions
                if (!userExtendedRoleBean.getExtRoleHostGroupList().isEmpty()
                        || !userExtendedRoleBean.getExtRoleServiceGroupList()
                                .isEmpty()) {
                    String inadequatePermissionsMessage = ResourceUtils
                            .getLocalizedMessage("com_groundwork_portal_statusviewer_inadequate_permissions")
                            + " [ Entire Network ] data";
                    setMessage(true);
                    setInfo(true);
                    setInfoMessage(inadequatePermissionsMessage);
                }
                break;*/

            case SERVICE:
                String hostName = Constant.EMPTY_STRING;
                String serviceName = Constant.EMPTY_STRING;
                try {
                    // fetch the event service - host name from preferences
                    hostName = FacesUtils
                            .getPreference(Constant.DEFAULT_HOST_PREF);
                    if (null == hostName
                            || hostName.equals(Constant.EMPTY_STRING)) {
                        throw new PreferencesException();
                    }

                    // fetch the event service - service name from preferences
                    serviceName = FacesUtils
                            .getPreference(Constant.DEFAULT_SERVICE_PREF);
                    if (null == serviceName
                            || serviceName.equals(Constant.EMPTY_STRING)) {
                        throw new PreferencesException();
                    }

                    // get this service from foundation
                    ServiceStatus service = foundationWSFacade
                            .getServiceByHostAndServiceName(hostName,
                                    serviceName);
                    // check for extended role permissions
                    if (!referenceTreeModel
                            .checkNodeForExtendedRolePermissions(service
                                    .getServiceStatusID(), NodeType.SERVICE,
                                    serviceName, userExtendedRoleBean
                                            .getExtRoleHostGroupList(),
                                    userExtendedRoleBean
                                            .getExtRoleServiceGroupList())) {
                        String inadequatePermissionsMessage = ResourceUtils
                                .getLocalizedMessage("com_groundwork_portal_statusviewer_inadequate_permissions")
                                + " [" + serviceName + "]";
                        setMessage(true);
                        setInfo(true);
                        setInfoMessage(inadequatePermissionsMessage);
                        return;
                    }
                    selectedNodeId = service.getServiceStatusID();

                } catch (PreferencesException e) {
                    setMessage(true);
                    setInfo(true);
                    setInfoMessage(new PreferencesException().getMessage());

                } catch (WSDataUnavailableException e) {
                    String serviceNotAvailableErrorMessage = ResourceUtils
                            .getLocalizedMessage("com_groundwork_portal_statusviewer_serviceUnavailable")
                            + " ["
                            + serviceName
                            + "] "
                            + ResourceUtils
                                    .getLocalizedMessage("com_groundwork_portal_statusviewer_serviceForHostUnavailable")
                            + " [" + hostName + "] ";
                    LOGGER.error(serviceNotAvailableErrorMessage);
                    setMessage(true);
                    setInfo(true);
                    setInfoMessage(serviceNotAvailableErrorMessage);

                } catch (GWPortalGenericException e) {
                    LOGGER
                            .error("Error occured while initializing event portlet in dashboard");
                    setMessage(true);
                    setError(true);
                    setErrorMessage(e.getMessage());
                }

                break;
            default:
                break;
        }
    }

    /**
     * set dynamic column depending on application type.
     * 
     * @param applicationType
     */
    public void setapplicationTypeDynamicColumns(String applicationType) {

        try {
            // create the foundationWSFacade instance
            IWSFacade foundationWSFacade = new WebServiceFactory()
                    .getWebServiceInstance(WebServiceType.FOUNDATION_WEBSERVICE);
            WSFoundationCollection col = foundationWSFacade
                    .getEntityTypeProperties(Constant.LOG_MESSAGE,
                            applicationType, false);
            EntityTypeProperty[] entityTypeProperties = col
                    .getEntityTypeProperty();
            // Set the dynamic columns
            if (applicationType.equalsIgnoreCase(Constant.NAGIOS.toUpperCase())) {
                EntityTypeProperty nagiosServiceProp = new EntityTypeProperty();
                nagiosServiceProp.setName(Constant.DYNAMIC_COLUMN_SERVICE);
                org.groundwork.foundation.ws.model.impl.ApplicationType nagiosAppType = new org.groundwork.foundation.ws.model.impl.ApplicationType();
                nagiosAppType.setName(Constant.NAGIOS.toUpperCase());
                nagiosServiceProp.setApplicationType(nagiosAppType);
                entityTypeProperties[entityTypeProperties.length - 1] = nagiosServiceProp;
            } // end if
            this.setDynamicColumns(entityTypeProperties);
        } catch (WSDataUnavailableException e) {
            LOGGER
                    .error("WSDataUnavailableException in setapplicationTypeDynamicColumns method.Hence DynamicColumns is null ");
        } catch (GWPortalException e) {
            LOGGER
                    .error("GWPortalException in setapplicationTypeDynamicColumns method.Hence DynamicColumns is null ");
        } catch (Exception e) {
            LOGGER
                    .error("Exception in setapplicationTypeDynamicColumns method.Hence DynamicColumns is null  "
                            + e.getMessage());
        }

    }

    /**
     * Adds Dynamic properties to the table
     */
    private void addDynamicProperties(HtmlDataTable eventDataTable) {
        // Now populate the dynamic properties here
        // First populate the Nagios service column first
        ExpressionFactory ef = application.getExpressionFactory();
        String hideCoulmnsname = null;
        if (dynamicColumns != null) {

            try {
                hideCoulmnsname = PropertyUtils.getProperty(
                        ApplicationType.STATUS_VIEWER,
                        EVEN_PORTLET_COLUMNS_HIDE);
            } catch (Exception e) {
                LOGGER
                        .error("Exception while getting even.portlet.columns.hide from propertices file"
                                + e.getMessage());
            }
            // creating array list for hide columns
            ArrayList<String> hidecolumnArray = new ArrayList<String>();
            if (hideCoulmnsname != null) {
                StringTokenizer stringTokenizer = new StringTokenizer(
                        hideCoulmnsname, Constant.COMMA);
                while (stringTokenizer.hasMoreTokens()) {
                    String hideColumn = stringTokenizer.nextToken();
                    hidecolumnArray.add(hideColumn.toLowerCase());

                }
            } else {
                LOGGER
                        .debug("no dynamic coulmns found in status-viewer propertices file ");

            }

            // Now populate the rest of the columns
            for (int dynaIndex = 0; dynaIndex < dynamicColumns.length; dynaIndex++) {
                EntityTypeProperty entityTypeProperty = dynamicColumns[dynaIndex];
                if (!entityTypeProperty.getApplicationType().getName()
                        .equalsIgnoreCase(Constant.APP_TYPE_SYSTEM)) {

                    String dynColumnName = (entityTypeProperty.getName()
                            .substring(0, 1).toLowerCase() + entityTypeProperty
                            .getName().substring(1)).trim();
                    if (dynColumnName != null
                            && !hidecolumnArray.contains(dynColumnName
                                    .toLowerCase())) {

                        if (!dynColumnName
                                .equalsIgnoreCase(Constant.DYNAMIC_COLUMN_SERVICE)) {
                            UIColumn dynColumn = new UIColumn();
                            String columnBinding = Constant.EVENT_BEAN_EL
                                    + Constant.DYNAMIC_PROPERTY
                                    + entityTypeProperty.getName() + "}";
                            dynColumn.setId(Constant.COL + dynColumnName);

                            HtmlOutputText dynHeader = new HtmlOutputText();
                            dynHeader.setId(Constant.HEADER + dynColumnName);
                            dynHeader.setValue(dynColumnName);
                            dynHeader.setStyleClass(Constant.TABLE_HEADER);
                            dynColumn.setHeader(dynHeader);
                            HtmlOutputText dynText = new HtmlOutputText();
                            dynText.setId(Constant.TXT + dynColumnName);
                            ValueExpression colBindValExp = ef
                                    .createValueExpression(FacesContext
                                            .getCurrentInstance()
                                            .getELContext(), columnBinding,
                                            String.class);

                            dynText.setValueExpression(Constant.VALUE,
                                    colBindValExp);

                            HtmlPanelGroup htmlpg = new HtmlPanelGroup();
                            htmlpg.setId(Constant.PNL + dynColumnName);
                            // htmlpg.setStyleClass(ConsoleConstants.
                            // STYLE_TABLE_COL)
                            // ;
                            htmlpg.getChildren().add(dynText);
                            dynColumn.getChildren().add(htmlpg);
                            eventDataTable.getChildren().add(dynColumn);
                            LOGGER.debug("Adding dynamic column "
                                    + dynColumnName);
                        } // end if
                    } // end if

                } // end if
            } // end for
        } // end if

    } // end method

    /**
     * Sets the styleClass.
     * 
     * @param styleClass
     *            the styleClass to set
     */
    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }

    /**
     * Returns the styleClass.
     * 
     * @return the styleClass
     */
    public String getStyleClass() {
        if (NodeType.NETWORK == selectedNodeType
                || NodeType.HOST_GROUP == selectedNodeType
                || NodeType.SERVICE == selectedNodeType) {
            styleClass = "div_horizontal_Scroll_bar_width1120";
        } else {
            styleClass = "div_horizontal_Scroll_bar_width1080";
        }
        return styleClass;
    }

    /**
     * Sets the dataPaginator.
     * 
     * @param dataPaginator
     *            the dataPaginator to set
     */
    public void setDataPaginator(DataPaginator dataPaginator) {
        this.dataPaginator = dataPaginator;
    }

    /**
     * Returns the dataPaginator.
     * 
     * @return the dataPaginator
     */
    public DataPaginator getDataPaginator() {
        return dataPaginator;
    }

    /**
     * Sets the hgDataPaginator.
     * 
     * @param hgDataPaginator
     *            the hgDataPaginator to set
     */
    public void setHgDataPaginator(DataPaginator hgDataPaginator) {
        this.hgDataPaginator = hgDataPaginator;
    }

    /**
     * Returns the hgDataPaginator.
     * 
     * @return the hgDataPaginator
     */
    public DataPaginator getHgDataPaginator() {
        return hgDataPaginator;
    }

    /**
     * Sets the hgEventDataTable.
     * 
     * @param hgEventDataTable
     *            the hgEventDataTable to set
     */
    public void setHgEventDataTable(HtmlDataTable hgEventDataTable) {
        this.hgEventDataTable = hgEventDataTable;
    }

    /**
     * Returns the hgEventDataTable.
     * 
     * @return the hgEventDataTable
     */
    public HtmlDataTable getHgEventDataTable() {

        return hgEventDataTable;
    }

    /**
     * Sets the sgDataPaginator.
     * 
     * @param sgDataPaginator
     *            the sgDataPaginator to set
     */
    public void setSgDataPaginator(DataPaginator sgDataPaginator) {
        this.sgDataPaginator = sgDataPaginator;
    }

    /**
     * Returns the sgDataPaginator.
     * 
     * @return the sgDataPaginator
     */
    public DataPaginator getSgDataPaginator() {
        return sgDataPaginator;
    }

    /**
     * Returns the serviceDataPaginator.
     * 
     * @return the serviceDataPaginator
     */
    public DataPaginator getServiceDataPaginator() {
        return serviceDataPaginator;
    }

    /**
     * Sets the serviceDataPaginator.
     * 
     * @param serviceDataPaginator
     *            the serviceDataPaginator to set
     */
    public void setServiceDataPaginator(DataPaginator serviceDataPaginator) {
        this.serviceDataPaginator = serviceDataPaginator;
    }

    /**
     * Sets the sgEventDataTable.
     * 
     * @param sgEventDataTable
     *            the sgEventDataTable to set
     */
    public void setSgEventDataTable(HtmlDataTable sgEventDataTable) {
        this.sgEventDataTable = sgEventDataTable;
    }

    /**
     * Returns the sgEventDataTable.
     * 
     * @return the sgEventDataTable
     */
    public HtmlDataTable getSgEventDataTable() {
        return sgEventDataTable;
    }

    /**
     * Sets the serviceEventDataTable.
     * 
     * @param serviceEventDataTable
     *            the serviceEventDataTable to set
     */
    public void setServiceEventDataTable(HtmlDataTable serviceEventDataTable) {
        this.serviceEventDataTable = serviceEventDataTable;
    }

    /**
     * Returns the serviceEventDataTable.
     * 
     * @return the serviceEventDataTable
     */
    public HtmlDataTable getServiceEventDataTable() {
        return serviceEventDataTable;
    }

    /**
     * Returns the eventHandler.
     * 
     * @return the eventHandler
     */
    public EventHandler getEventHandler() {
        return eventHandler;
    }

    /**
     * Sets the eventHandler.
     * 
     * @param eventHandler
     *            the eventHandler to set
     */
    public void setEventHandler(EventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    /**
     * Sets the dashboradformID.
     * 
     * @param dashboradformID
     *            the dashboradformID to set
     */
    public void setDashboradformID(String dashboradformID) {
        this.dashboradformID = dashboradformID;
    }

    /**
     * Returns the dashboradformID.
     * 
     * @return the dashboradformID
     */
    public String getDashboradformID() {
        return dashboradformID;
    }

}
