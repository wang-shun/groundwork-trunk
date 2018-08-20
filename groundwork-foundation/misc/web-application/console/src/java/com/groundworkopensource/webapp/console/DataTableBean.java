/*
 * Console - The ultimate view for log messages.
 *
 * Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")  
 * All rights reserved. This program is free software; you can redistribute it
 * and/or modify it under the terms of the GNU General Public License version 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for 
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 */
package com.groundworkopensource.webapp.console;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.model.impl.EntityTypeProperty;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.FilterOperator;
import org.groundwork.foundation.ws.model.impl.ServiceStatus;
import org.groundwork.foundation.ws.model.impl.Sort;

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
	private EventBean[] events;

	/** Enable log4j for DataTableBean class */
	public static Logger logger = Logger.getLogger(DataTableBean.class
			.getName());

	private boolean multipleSelection = true;
	private List<EventBean> selectedRows = null;
	private ArrayList<String> columnNames = new ArrayList<String>();
	private ArrayList<String> columnLabels = new ArrayList<String>();
	private EntityTypeProperty[] dynamicColumns = null;
	private HtmlDataTable eventDataTable = null;
	private DataPaginator eventsPager = null;
	private Application app = FacesContext.getCurrentInstance()
			.getApplication();

	// private HtmlPanelGroup panelGroup = null;

	// For paginator
	private int lastStartRow = -1;
	private DataPage lastPage = null;

	private String sortColumnName = null;
	private boolean ascending = false;
	private FilterBean filterBean = null;

	private HtmlGraphicImage arrowImg = null;

	/*
	 * Initializes the bean.Populates the default "All Events".
	 * 
	 */
	private void init() {
		sortColumnName = ConsoleConstants.DEFAULT_SORT_COLUMN;
		logger.debug("Constructing the datatable..");
		this.constructComponent();
		logger.debug("Populating all events...");

	}

	public DataTableBean() {
		super(Integer.parseInt(PropertyUtils
				.getProperty(ConsoleConstants.PROP_PAGE_SIZE)));
		init();
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
	 * @param events
	 */
	public void setEvents(EventBean[] events) {
		this.events = events;
	}

	/**
	 * Gets the selectedRows for the datatable.
	 * 
	 * @return
	 */
	public List<EventBean> getSelectedRows() {

		return selectedRows;
	}

	/**
	 * Sets the selectedRows from the datatable.
	 * 
	 * @param selectedRows
	 */
	public void setSelectedRows(List<EventBean> selectedRows) {
		this.selectedRows = selectedRows;
	}

	/**
	 * Method for the rowselection listener.
	 * 
	 * @param event
	 */
	public void rowSelection(RowSelectorEvent event) {

		TabsetBean tabset = ConsoleHelper.getTabSetBean();
		Tab tab = tabset.getTabs().get(
				tabset.getTabIndex());
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
			msgSelectBean.setSelectAllButtonText(ResourceUtils.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_SELECT_ALL));
		} // end if
		ActionBean action = tab.getActionBean();
		MenuItem menu = (MenuItem) action.getMenuModel().get(0);
		if (selectedRows.size() >= 1) {
			logger.debug("Clearing menumodel");
			menu.setIcon(ConsoleConstants.MENU_ICON_ON);
			
			//action.setStyleClass("darkButton active");
		} else {
			menu.setIcon(ConsoleConstants.MENU_ICON_OFF);
			menu.getChildren().clear();
			//action.setStyleClass("darkButton inActive");
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
	 * @param multipleSelection
	 */
	public void setMultipleSelection(boolean multipleSelection) {
		this.multipleSelection = multipleSelection;
	}

	/**
	 * Gets the column names
	 * 
	 * @return
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
	 * @return
	 */
	public HtmlDataTable getEventDataTable() {
		return eventDataTable;
	}

	/**
	 * Sets the HTMLTable
	 * 
	 * @return
	 */
	public void setEventDataTable(HtmlDataTable eventDataTable) {
		this.eventDataTable = eventDataTable;
	}

	/**
	 * Construcs the datatable
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

	public EntityTypeProperty[] getDynamicColumns() {
		return dynamicColumns;
	}

	public void setDynamicColumns(EntityTypeProperty[] dynamicColumns) {
		this.dynamicColumns = dynamicColumns;
	}

	/**
	 * Adds Dynamic properties to the table
	 */
	private void addDynamicProperties() {
		// Now populate the dynamic properties here
		// First populate the Nagios service column first
		ExpressionFactory ef = app.getExpressionFactory();
		if (dynamicColumns != null) {
			for (int dynaIndex = 0; dynaIndex < dynamicColumns.length; dynaIndex++) {
				EntityTypeProperty entityTypeProperty = (EntityTypeProperty) dynamicColumns[dynaIndex];
				if (!entityTypeProperty.getApplicationType().getName()
						.equalsIgnoreCase(
								ConsoleConstants.PROP_NAME_APP_TYPE_SYSTEM)) {

					String dynColumnName = (entityTypeProperty.getName()
							.substring(0, 1).toLowerCase() + entityTypeProperty
							.getName().substring(1)).trim();
					if (dynColumnName != null
							&& dynColumnName
									.equalsIgnoreCase(ConsoleConstants.NAGIOS_SERVICE_COLUMN)) {
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

						/*
						 * CommandSortHeader sortHeader = new
						 * CommandSortHeader(); sortHeader.setId("header_" +
						 * dynColumnName);
						 * sortHeader.setColumnName(entityTypeProperty.getName());
						 * sortHeader.setArrow(true);
						 * sortHeader.setValue(entityTypeProperty.getName());
						 * sortHeader.setStyleClass("table-header");
						 * sortHeader.setActionListener(createActionListenerMethodBinding("#{tabset.tabs[tabset.dynamicTabSet.selectedIndex].dataTableBean.sort}"));
						 * dynColumn.setHeader(sortHeader);
						 */
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
			// Now populate the rest of the columns
			for (int dynaIndex = 0; dynaIndex < dynamicColumns.length; dynaIndex++) {
				EntityTypeProperty entityTypeProperty = (EntityTypeProperty) dynamicColumns[dynaIndex];
				if (!entityTypeProperty.getApplicationType().getName()
						.equalsIgnoreCase(
								ConsoleConstants.PROP_NAME_APP_TYPE_SYSTEM)) {

					String dynColumnName = (entityTypeProperty.getName()
							.substring(0, 1).toLowerCase() + entityTypeProperty
							.getName().substring(1)).trim();
					if (dynColumnName != null
							&& !dynColumnName
									.equalsIgnoreCase(ConsoleConstants.NAGIOS_SERVICE_COLUMN)) {
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

						/*
						 * CommandSortHeader sortHeader = new
						 * CommandSortHeader(); sortHeader.setId("header_" +
						 * dynColumnName);
						 * sortHeader.setColumnName(entityTypeProperty.getName());
						 * sortHeader.setArrow(true);
						 * sortHeader.setValue(entityTypeProperty.getName());
						 * sortHeader.setStyleClass("table-header");
						 * sortHeader.setActionListener(createActionListenerMethodBinding("#{tabset.tabs[tabset.dynamicTabSet.selectedIndex].dataTableBean.sort}"));
						 * dynColumn.setHeader(sortHeader);
						 */
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
	 * Adds Static properties to the table
	 */
	private void addBuiltInProperties() {
		ExpressionFactory ef = app.getExpressionFactory();
		// initialize column names
		String built_in_columns = PropertyUtils
				.getProperty(ConsoleConstants.PROP_BUILTIN_COLS);
		if (built_in_columns != null) {
			columnNames = new ArrayList<String>();
			StringTokenizer stkn = new StringTokenizer(built_in_columns, ",");
			while (stkn.hasMoreTokens()) {
				String column = stkn.nextToken();
				StringTokenizer stknLabel = new StringTokenizer(column, ":");
				columnNames.add(stknLabel.nextToken());
				columnLabels.add(stknLabel.nextToken());
			}
		} else {
			logger
					.error("No built_in_columns property specified in console.properties");
		}
		// First populate the built-in columns and then the dynamic ones
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
				Class ca[] = { com.icesoft.faces.component.ext.RowSelectorEvent.class };

				javax.faces.el.MethodBinding mb = app.createMethodBinding(
						ConsoleConstants.EL_BIND_ROW_SEL, ca);
				rs.setSelectionListener(mb);
				rs.setMouseOverClass("rowOver");
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
					.equalsIgnoreCase(ConsoleConstants.EL_BIND_COL_MON_STATUS)) {
				ValueExpression styleClassBindValExp = ef
						.createValueExpression(FacesContext
								.getCurrentInstance().getELContext(),
								ConsoleConstants.EL_BIND_EVENT_LEFT
										+ columnName
										+ ConsoleConstants.EL_BIND_STYLE_RIGHT,
								String.class);
				htmlpg.setValueExpression("styleClass", styleClassBindValExp);

				ValueExpression textValBindValExp = ef.createValueExpression(
						FacesContext.getCurrentInstance().getELContext(),
						ConsoleConstants.EL_BIND_EVENT_LEFT + columnName
								+ ConsoleConstants.EL_BIND_VALUE_RIGHT,
						String.class);
				text.setValueExpression(ConsoleConstants.EL_BIND_ATT_VALUE,
						textValBindValExp);

				htmlpg.getChildren().add(text);
			} else if (columnName.equalsIgnoreCase("device")) {

				HtmlCommandLink deviceLink = new HtmlCommandLink();
				deviceLink.setId("lnk_" + columnName);
				ValueExpression textValBindValExp = ef.createValueExpression(
						FacesContext.getCurrentInstance().getELContext(),
						columnBinding, String.class);
				deviceLink.setValueExpression(
						ConsoleConstants.EL_BIND_ATT_VALUE, textValBindValExp);
				deviceLink
						.setActionListener(createActionListenerMethodBinding("#{tabset.tabs[tabset.tabIndex].dataTableBean.showDetails}"));
				htmlpg.getChildren().add(deviceLink);
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
	}

	/**
	 * Getter for data paginator
	 * 
	 * @return
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
			if (filter == null) {
				filter = new Filter(
						ConsoleConstants.PROP_NAME_OPERATION_STATUS,
						FilterOperator.EQ,
						ConsoleConstants.OPERATION_STATUS_OPEN);
			} // end if
			if (dynamicColumns != null) {
				eventList = new EventQueryManager().queryForEventsByFilter(
						filter, dynamicColumns, startRow, sort);
			} else {
				eventList = new EventQueryManager().queryForEventsByFilter(
						filter, startRow, sort);
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

	public String getSortColumnName() {
		return sortColumnName;
	}

	public void setSortColumnName(String sortColumnName) {

		this.sortColumnName = sortColumnName;

	}

	public boolean isAscending() {
		return ascending;
	}

	public void setAscending(boolean ascending) {

		this.ascending = ascending;
	}

	/**
	 * Listener for sorting
	 * 
	 * @param e
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
	 * Creates an action listener method for the menu
	 * 
	 * @param actionListenerString
	 * @return
	 */
	private MethodBinding createActionListenerMethodBinding(
			String actionListenerString) {
		Class args[] = { ActionEvent.class };
		MethodBinding methodBinding = null;

		methodBinding = FacesContext.getCurrentInstance().getApplication()
				.createMethodBinding(actionListenerString, args);
		return methodBinding;
	}

	public ArrayList<String> getColumnLabels() {
		return columnLabels;
	}

	public void setColumnLabels(ArrayList<String> columnLabels) {
		this.columnLabels = columnLabels;
	}

	public FilterBean getFilterBean() {
		return filterBean;
	}

	public void setFilterBean(FilterBean filterBean) {
		this.filterBean = filterBean;
	}

	/**
	 * Show Details
	 * 
	 * @param ae
	 */
	public void showDetails(ActionEvent ae) {
		String deviceName = (String) ((HtmlCommandLink) ae.getComponent())
				.getValue();
		PopupBean popup = ConsoleHelper.getPopupBean();
		popup.setShowDraggablePanel(true);

		ConsoleManager mgr = ConsoleHelper.getConsoleManager();
		HostDetailBean hostDetail = mgr.getHostDetails(deviceName);

		popup.setTitle("Device details");
		popup.setHost(hostDetail);
		
	} // end method

}
