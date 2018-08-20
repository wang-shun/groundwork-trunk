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
 * This Bean provide host Group statistics data to Host Group Status portlet.
 * 
 * @author manish_kjain
 * 
 */
public class HostGroupStatisticsBean extends ServerPush implements Serializable {

    // /**
    // * graphic img UI component ID constant part
    // */
    // private static final String NO_PIE_IMG = "NoPieImg";
    // /**
    // * graphic img UI component ID constant part
    // */
    // private static final String PIE_IMG = "PieImg";
    /**
     * graphic img UI component ID constant part
     */
    private static final String HG_STATISTICS_PORTLET = "hgStatisticsPortlet_";
    /**
     * boolean variable to used close and open model popup window
     */

    private boolean visible = false;
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 2593704753219254337L;

    /**
     * logger
     */
    private static final Logger LOGGER = Logger
            .getLogger(HostGroupStatisticsBean.class.getName());

    // /**
    // *
    // */
    // private boolean isInitialState = true;

    /**
     * row count of host group list.
     */
    private int rowCount;

    /**
     * hold previous selected host filter
     */
    private String previousSelectedHostFilter;

    /**
     * rows of model pop data table
     */
    private int popupRowSize;

    /**
     * Returns the rowCount.
     * 
     * @return the rowCount
     */
    public int getRowCount() {
        return rowCount;
    }

    /**
     * Sets the rowCount.
     * 
     * @param rowCount
     *            the rowCount to set
     */
    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    /**
     * total host group count.
     */
    private long totalHostGroupCount;
    /**
     * total of filtered host group count.
     */
    private int filteredHostGroupCount;
    /**
     * Currect pop up window status
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
     * StateController
     */
    private StateController stateController = null;

    /**
     * dynamic form id
     */
    private String hgStatusFrmID;
    /**
     * host group count list
     */

    private List<StatisticsBean> hostGroupCountList = Collections
            .synchronizedList(new ArrayList<StatisticsBean>());
    /**
     * Host group list
     */
    private List<ModelPopUpDataBean> hostGroupList = Collections
            .synchronizedList(new ArrayList<ModelPopUpDataBean>());

    /**
     * constructor
     */
    public HostGroupStatisticsBean() {
        try {
            popupRowSize = Integer.parseInt(PropertyUtils.getProperty(
                    ApplicationType.STATUS_VIEWER,
                    Constant.HOST_GROUP_SUMMARY_POPUP_ROWS));

        } catch (NumberFormatException numberFormatException) {
            LOGGER
                    .error("NumberFormatException while getting host group popup page size from status-viewer properties files Hence default page size is set");
            popupRowSize = Constant.TEN;
        } catch (Exception exception) {
            LOGGER
                    .error("Exception while getting host group popup page size from status-viewer properties files Hence default page size is set");
            popupRowSize = Constant.TEN;
        }

        stateController = new StateController();
        if (!PortletUtils.isInStatusViewer()) {
            int randomID = new Random().nextInt(Constant.TEN_HOUSANED);
            hgStatusFrmID = HG_STATISTICS_PORTLET + "frm" + randomID;
        }
    }

    /**
     * return byte array of host group Pie chart.
     */
    private byte[] hostGroupPieChart;

    /**
     * hidden field to avoid web service call for pie chart.
     */
    private String hgHiddenField = Constant.HIDDEN;

    /**
     * Returns the hostGroupCountList.
     * 
     * @return the hostGroupCountList
     */
    public List<StatisticsBean> getHostGroupCountList() {
        return hostGroupCountList;

    }

    /**
     * Returns the hostGroupList.
     * 
     * @return the hostGroupList
     */
    public List<ModelPopUpDataBean> getHostGroupList() {

        return hostGroupList;
    }

    /**
     * Sets the hostGroupList.
     * 
     * @param hostGroupList
     *            the hostGroupList to set
     */
    public void setHostGroupList(List<ModelPopUpDataBean> hostGroupList) {
        this.hostGroupList = hostGroupList;
        this.sort(null);
    }

    /**
     * Sets the hostGroupCountList.
     * 
     * @param hostGroupCountList
     *            the hostGroupCountList to set
     */
    public void setHostGroupCountList(List<StatisticsBean> hostGroupCountList) {
        this.hostGroupCountList = hostGroupCountList;
    }

    /**
     * Returns the totalHostGroupCount.
     * 
     * @return the totalHostGroupCount
     */
    public long getTotalHostGroupCount() {
        return totalHostGroupCount;
    }

    /**
     * Sets the totalHostGroupCount.
     * 
     * @param totalHostGroupCount
     *            the totalHostGroupCount to set
     */
    public void setTotalHostGroupCount(long totalHostGroupCount) {
        this.totalHostGroupCount = totalHostGroupCount;
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
     * Returns the filteredHostGroupCount.
     * 
     * @return the filteredHostGroupCount
     */
    public int getFilteredHostGroupCount() {
        return filteredHostGroupCount;
    }

    /**
     * Sets the filteredHostGroupCount.
     * 
     * @param filteredHostGroupCount
     *            the filteredHostGroupCount to set
     */
    public void setFilteredHostGroupCount(int filteredHostGroupCount) {
        this.filteredHostGroupCount = filteredHostGroupCount;
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
        Collections.sort(hostGroupList, comparator);
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

        // get current instance of PopUpSelectBean
        StatisticsHandler statisticsHandler = (StatisticsHandler) FacesUtils
                .getManagedBean(Constant.STATISTICS_HANDLER);
        if (statisticsHandler != null) {
            statisticsHandler.setAllHostGroupList(null);
            statisticsHandler.setFilteredHostGroupList(null);
        }

    }

    /**
     * Sets the hostGroupPieChart.
     * 
     * @param hostGroupPieChart
     *            the hostGroupPieChart to set
     */
    public void setHostGroupPieChart(byte[] hostGroupPieChart) {
        this.hostGroupPieChart = hostGroupPieChart;
    }

    /**
     * Returns the hostGroupPieChart.
     * 
     * @return the hostGroupPieChart
     */
    public byte[] getHostGroupPieChart() {
        return hostGroupPieChart;
    }

    /**
     * Sets the hgHiddenField.
     * 
     * @param hgHiddenField
     *            the hgHiddenField to set
     */
    public void setHgHiddenField(String hgHiddenField) {
        this.hgHiddenField = hgHiddenField;
    }

    /**
     * Returns the hgHiddenField.
     * 
     * @return the hgHiddenField
     */
    public String getHgHiddenField() {

        String currentHostFilter = stateController.getCurrentHostFilter();
        // This part is not called on JMS thread or if previous and current
        // selected filter are same.
        if (isIntervalRender()
                || !currentHostFilter
                        .equalsIgnoreCase(previousSelectedHostFilter)) {

            // On render of hidden field calling web service and set host group
            // statistics as well as pie chart byte array
            // get current instance of statisticsHandler
            StatisticsHandler statisticsHandler = (StatisticsHandler) FacesUtils
                    .getManagedBean(Constant.STATISTICS_HANDLER);
            if (statisticsHandler != null) {
                LOGGER.debug("setting Host group statistics....");
                statisticsHandler.setHostGroupStatistics();
            }
            setIntervalRender(false);
        } // end if

        // clean up the data before returning
        currentHostFilter = null;

        return hgHiddenField;
    }

    /**
     * Sets the previousSelectedHostFilter.
     * 
     * @param previousSelectedHostFilter
     *            the previousSelectedHostFilter to set
     */
    public void setPreviousSelectedHostFilter(String previousSelectedHostFilter) {
        this.previousSelectedHostFilter = previousSelectedHostFilter;
    }

    /**
     * Returns the previousSelectedHostFilter.
     * 
     * @return the previousSelectedHostFilter
     */
    public String getPreviousSelectedHostFilter() {
        return previousSelectedHostFilter;
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
     * Sets the hgStatusFrmID.
     * 
     * @param hgStatusFrmID
     *            the hgStatusFrmID to set
     */
    public void setHgStatusFrmID(String hgStatusFrmID) {
        this.hgStatusFrmID = hgStatusFrmID;
    }

    /**
     * Returns the hgStatusFrmID.
     * 
     * @return the hgStatusFrmID
     */
    public String getHgStatusFrmID() {
        return hgStatusFrmID;
    }

}
