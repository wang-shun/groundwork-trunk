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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;

import com.groundworkopensource.portal.common.ApplicationType;
import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.common.PropertyUtils;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.PortletUtils;
import com.groundworkopensource.portal.statusviewer.handler.StateController;
import com.groundworkopensource.portal.statusviewer.handler.StatisticsHandler;

/**
 * This Bean provide Service Group statistics data to Service Group Status
 * portlet.
 * 
 * @author manish_kjain
 * 
 */
public class ServiceGroupStatistics extends ServerPush implements Serializable {

    /**
     * graphic img UI component ID constant part
     */
    private static final String SG_STATISTICS_PORTLET = "sgStatisticsPortlet_";

    /**
     * boolean variable to used close and open model popup window
     */

    private boolean visible = false;

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 3038124417454950137L;

    /** Enable log4j for ServiceStatisticsBean class */
    private static final Logger LOGGER = Logger
            .getLogger(ServiceGroupStatistics.class.getName());
    /**
     * total Service group count.
     */
    private long totalServicesGroupsCount;
    /**
     * total of filtered Service group count.
     */
    private int filteredServicesGroupsCount;
    /**
     * current status of pop up window.
     */
    private String currentPopstatus = "empty";

    /**
     * String property for the column to sort.
     */
    private String sortColumn;

    /**
     * boolean property indicating the ascending sort order
     */
    private boolean ascending = true;
    /**
     * host group count list
     */

    // /**
    // * Flag for bean initial state
    // */
    // private boolean isInitialState = true;
    /**
     * rows of model pop data table
     */
    private int popupRowSize;

    /**
     * StateController
     */
    private StateController stateController = null;

    /**
     * dynamic form id
     */
    private String sgStatusFrmID;

    /**
     * service group count list
     */
    private List<StatisticsBean> servicesGroupsCountList = Collections
            .synchronizedList(new ArrayList<StatisticsBean>());
    /**
     * service group list/
     */
    private List<ModelPopUpDataBean> servicesGroupsList = Collections
            .synchronizedList(new ArrayList<ModelPopUpDataBean>());

    /**
     * 
     */
    public ServiceGroupStatistics() {
        try {
            popupRowSize = Integer.parseInt(PropertyUtils.getProperty(
                    ApplicationType.STATUS_VIEWER,
                    Constant.SERVICE_GROUP_SUMMARY_POPUP_ROWS));

        } catch (NumberFormatException numberFormatException) {
            LOGGER
                    .error("Exception while getting service group popup page size from status-viewer properties files Hence default page size is set");
            popupRowSize = Constant.TEN;
        } catch (Exception exception) {
            LOGGER
                    .error("Exception while getting service group popup page size from status-viewer properties files Hence default page size is set");
            popupRowSize = Constant.TEN;
        }
        stateController = new StateController();
        if (!PortletUtils.isInStatusViewer()) {
            int randomID = new Random().nextInt(Constant.TEN_HOUSANED);
            sgStatusFrmID = SG_STATISTICS_PORTLET + "frm" + randomID;
        }
    }

    /**
     * number of row in service list.
     */
    private int serviceGroupRowCount;

    /**
     * return byte array of service group Pie chart.
     */
    private byte[] serviceGroupPieChart;

    /**
     * Previous Service Filter.
     */
    private String previousServiceFilter;
    /**
     * hidden field to avoid web service call for pie chart.
     */
    private String sgHiddenField = Constant.HIDDEN;

    /**
     * Returns the serviceGroupRowCount.
     * 
     * @return the serviceGroupRowCount
     */
    public int getServiceGroupRowCount() {
        return serviceGroupRowCount;
    }

    /**
     * Sets the serviceGroupRowCount.
     * 
     * @param serviceGroupRowCount
     *            the serviceGroupRowCount to set
     */
    public void setServiceGroupRowCount(int serviceGroupRowCount) {
        this.serviceGroupRowCount = serviceGroupRowCount;
    }

    /**
     * Returns the totalServicesGroupsCount.
     * 
     * @return the totalServicesGroupsCount
     */
    public long getTotalServicesGroupsCount() {
        return totalServicesGroupsCount;
    }

    /**
     * Sets the totalServicesGroupsCount.
     * 
     * @param totalServicesGroupsCount
     *            the totalServicesGroupsCount to set
     */
    public void setTotalServicesGroupsCount(long totalServicesGroupsCount) {
        this.totalServicesGroupsCount = totalServicesGroupsCount;
    }

    /**
     * Returns the filteredServicesGroupsCount.
     * 
     * @return the filteredServicesGroupsCount
     */
    public int getFilteredServicesGroupsCount() {
        return filteredServicesGroupsCount;
    }

    /**
     * Sets the filteredServicesGroupsCount.
     * 
     * @param filteredServicesGroupsCount
     *            the filteredServicesGroupsCount to set
     */
    public void setFilteredServicesGroupsCount(int filteredServicesGroupsCount) {
        this.filteredServicesGroupsCount = filteredServicesGroupsCount;
    }

    /**
     * Returns the currentPopstatus.
     * 
     * @return the currentPopstatus
     */
    public String getCurrentPopstatus() {
        return currentPopstatus;
    }

    /**
     * Sets the currentPopstatus.
     * 
     * @param currentPopstatus
     *            the currentPopstatus to set
     */
    public void setCurrentPopstatus(String currentPopstatus) {
        this.currentPopstatus = currentPopstatus;
    }

    /**
     * Returns the servicesGroupsCountList.
     * 
     * @return the servicesGroupsCountList
     */
    public List<StatisticsBean> getServicesGroupsCountList() {
        return servicesGroupsCountList;
    }

    /**
     * Sets the servicesGroupsCountList.
     * 
     * @param servicesGroupsCountList
     *            the servicesGroupsCountList to set
     */
    public void setServicesGroupsCountList(
            List<StatisticsBean> servicesGroupsCountList) {
        this.servicesGroupsCountList = servicesGroupsCountList;
    }

    /**
     * Returns the servicesGroupsList.
     * 
     * @return the servicesGroupsList
     */
    public List<ModelPopUpDataBean> getServicesGroupsList() {

        return servicesGroupsList;
    }

    /**
     * Sets the servicesGroupsList.
     * 
     * @param servicesGroupsList
     *            the servicesGroupsList to set
     */
    public void setServicesGroupsList(
            List<ModelPopUpDataBean> servicesGroupsList) {
        this.servicesGroupsList = servicesGroupsList;
        this.sort(null);
    }

    /**
     * Sorts the disabledEntityList on host/service name. This
     * 
     * @param event
     * 
     */
    public void sort(ActionEvent event) {
        Comparator<ModelPopUpDataBean> comparator = new Comparator<ModelPopUpDataBean>() {
            public int compare(ModelPopUpDataBean popupBean1,
                    ModelPopUpDataBean popupBean2) {
                String name1 = popupBean1.getName();
                String name2 = popupBean2.getName();
                int result = 0;
                // For sort order ascending -
                if (getAscending()) {
                    result = name1.compareTo(name2);
                } else {
                    // Descending
                    result = name2.compareTo(name1);
                }
                return result;
            }
        };
        if (event != null) {
            // set ascending
            ascending = !ascending;
        }
        Collections.sort(servicesGroupsList, comparator);
    }

    /**
     * Sets the sortColumn.
     * 
     * @param sortColumn
     *            the sortColumn to set
     */
    public void setSortColumn(String sortColumn) {
        this.sortColumn = sortColumn;
    }

    /**
     * Returns the sortColumn.
     * 
     * @return the sortColumn
     */
    public String getSortColumn() {
        return sortColumn;
    }

    /**
     * Sets the ascending.
     * 
     * @param ascending
     *            the ascending to set
     */
    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }

    /**
     * Returns the ascending.
     * 
     * @return the ascending
     */
    public boolean getAscending() {
        return ascending;
    }

    /**
     * action to be perform on close model pop up window
     * 
     * @param e
     */
    public void closeWindow(ActionEvent e) {

        // set by default sorting should be ascending.
        setAscending(true);
        setSortColumn(null);
        // get current instance of StatisticsHandler
        StatisticsHandler statisticsHandler = (StatisticsHandler) FacesUtils
                .getManagedBean(Constant.STATISTICS_HANDLER);
        if (statisticsHandler != null) {
            statisticsHandler.setAllServiceGroupList(null);
            statisticsHandler.setFilteredServiceGroupList(null);
        }

    }

    /**
     * Sets the sgHiddenField.
     * 
     * @param sgHiddenField
     *            the sgHiddenField to set
     */
    public void setSgHiddenField(String sgHiddenField) {
        this.sgHiddenField = sgHiddenField;
    }

    /**
     * Returns the sgHiddenField.
     * 
     * @return the sgHiddenField
     */
    public String getSgHiddenField() {

        String currentServiceFilter = stateController.getCurrentServiceFilter();
        if (isIntervalRender()
                || !currentServiceFilter
                        .equalsIgnoreCase(this.previousServiceFilter)) {

            // On render of hidden field calling web service and set host group
            // statistics as well as pie chart byte array
            // get current instance of statisticsHandler
            StatisticsHandler statisticsHandler = (StatisticsHandler) FacesUtils
                    .getManagedBean(Constant.STATISTICS_HANDLER);
            if (statisticsHandler != null) {
                LOGGER.info("setting Service group statistics...............");
                statisticsHandler.setServiceGroupStatistics();

            }
        }
        setIntervalRender(false);

        // clean up the data before returning
        currentServiceFilter = null;

        return sgHiddenField;
    }

    /**
     * Sets the serviceGroupPieChart.
     * 
     * @param serviceGroupPieChart
     *            the serviceGroupPieChart to set
     */
    public void setServiceGroupPieChart(byte[] serviceGroupPieChart) {
        this.serviceGroupPieChart = serviceGroupPieChart;
    }

    /**
     * Returns the serviceGroupPieChart.
     * 
     * @return the serviceGroupPieChart
     */
    public byte[] getServiceGroupPieChart() {
        return serviceGroupPieChart;
    }

    /**
     * Sets the previousServiceFilter.
     * 
     * @param previousServiceFilter
     *            the previousServiceFilter to set
     */
    public void setPreviousServiceFilter(String previousServiceFilter) {
        this.previousServiceFilter = previousServiceFilter;
    }

    /**
     * Returns the previousServiceFilter.
     * 
     * @return the previousServiceFilter
     */
    public String getPreviousServiceFilter() {
        return previousServiceFilter;
    }

    /**
     * Returns the popupRowSize.
     * 
     * @return the popupRowSize
     */
    public int getPopupRowSize() {

        return popupRowSize;
    }

    /**
     * (non-Javadoc)
     * 
     * @see com.groundworkopensource.portal.statusviewer.bean.ServerPush#refresh(java.lang.String)
     */
    @Override
    public void refresh(String xmlMessage) {
        // TODO Method not implemented yet: ServerPush.refresh(...) is not
        // implemented by manish_kjain
    }

    /**
     * @return boolean
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * @param visible
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     *
     */
    public void closePopup() {
        visible = false;
    }

    /**
     *
     */
    public void openPopup() {
        visible = true;
    }

    /**
     * Sets the sgStatusFrmID.
     * 
     * @param sgStatusFrmID
     *            the sgStatusFrmID to set
     */
    public void setSgStatusFrmID(String sgStatusFrmID) {
        this.sgStatusFrmID = sgStatusFrmID;
    }

    /**
     * Returns the sgStatusFrmID.
     * 
     * @return the sgStatusFrmID
     */
    public String getSgStatusFrmID() {
        return sgStatusFrmID;
    }
}
