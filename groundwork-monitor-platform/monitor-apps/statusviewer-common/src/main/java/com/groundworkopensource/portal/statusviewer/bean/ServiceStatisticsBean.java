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
import java.util.List;
import java.util.Random;

import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;

import com.groundworkopensource.portal.common.ApplicationType;
import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.common.PropertyUtils;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.NodeType;
import com.groundworkopensource.portal.statusviewer.common.PortletUtils;
import com.groundworkopensource.portal.statusviewer.handler.StateController;
import com.groundworkopensource.portal.statusviewer.handler.StatisticsHandler;
import com.groundworkopensource.portal.statusviewer.handler.SubpageIntegrator;
import com.icesoft.faces.component.datapaginator.DataPaginator;

/**
 * This Bean provide Service statistics data to service Status portlet.
 * 
 * @author manish_kjain
 * 
 */
public class ServiceStatisticsBean extends ServerPush implements Serializable {

    /**
     * graphic img UI component ID constant part
     */
    private static final String SERVICE_STATISTICS_PORTLET = "serviceStatisticsPortlet_";
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -332968941832946385L;
    /** Enable log4j for ServiceStatisticsBean class */
    private static final Logger LOGGER = Logger
            .getLogger(ServiceStatisticsBean.class.getName());
    /**
     * total count of services under host or host group.
     */
    private long totalServiceCount;
    /**
     * filtered count of services under host or host group.
     */
    private int filteredServiceCount;
    /**
     * current pop up status
     */
    private String currentPopstatus = "empty";

    // /**
    // * Flag for bean initial state
    // */
    // private final boolean isInitialState = true;

    /**
     * previous selected services filter
     */
    private String previousServiceFilter;
    /**
     * rows of model pop data table
     */
    private int popupRowSize;

    /**
     * StateController
     */
    private StateController stateController = null;

    /**
     * service monitor status count list.
     */
    private List<StatisticsBean> serviceCountList = Collections
            .synchronizedList(new ArrayList<StatisticsBean>());
    /**
     * service list
     */
    private List<ModelPopUpDataBean> serviceList = Collections
            .synchronizedList(new ArrayList<ModelPopUpDataBean>());
    /**
     * dynamic form id
     */
    private String serviceStatusFrmID;

    /**
     * service Navigation Hidden Field
     */
    private String serviceNavHiddenField = Constant.EMPTY_STRING;
    /**
     * previous node ID
     */
    private int prevNodeId;
    /**
     * previous node Type
     */
    private NodeType prevNodeType;

    /**
     * Constructor
     */
    public ServiceStatisticsBean() {

        try {
            popupRowSize = Integer.parseInt(PropertyUtils.getProperty(
                    ApplicationType.STATUS_VIEWER,
                    Constant.SERVICE_POPUP_PAGE_SIZE));

        } catch (NumberFormatException numberFormatException) {
            LOGGER
                    .error("NumberFormatException while getting service popup page size from status-viewer properties files Hence default page size is set");
            popupRowSize = Constant.TEN;
        } catch (Exception e) {
            LOGGER
                    .error("Exception while getting service popup page size from status-viewer properties files Hence default page size is set");
            popupRowSize = Constant.TEN;
        }
        stateController = new StateController();
        if (!PortletUtils.isInStatusViewer()) {
            int randomID = new Random().nextInt(Constant.TEN_HOUSANED);
            setServiceStatusFrmID(SERVICE_STATISTICS_PORTLET + "frm" + randomID);
        }
    }

    /**
     * Row count of service LIst.
     */
    private int serviceRowCount;

    /**
     * return byte array of service Pie chart.
     */
    private byte[] servicePieChart;
    /**
     * hidden field to avoid web service call for pie chart.
     */
    private String serviceHiddenField = Constant.HIDDEN;

    /**
     * Returns the serviceRowCount.
     * 
     * @return the serviceRowCount
     */
    public int getServiceRowCount() {
        return serviceRowCount;
    }

    /**
     * Sets the serviceRowCount.
     * 
     * @param serviceRowCount
     *            the serviceRowCount to set
     */
    public void setServiceRowCount(int serviceRowCount) {
        this.serviceRowCount = serviceRowCount;
    }

    /**
     * Returns the totalServiceCount.
     * 
     * @return the totalServiceCount
     */
    public long getTotalServiceCount() {
        return totalServiceCount;
    }

    /**
     * Sets the totalServiceCount.
     * 
     * @param totalServiceCount
     *            the totalServiceCount to set
     */
    public void setTotalServiceCount(long totalServiceCount) {
        this.totalServiceCount = totalServiceCount;
    }

    /**
     * Returns the filteredServiceCount.
     * 
     * @return the filteredServiceCount
     */
    public int getFilteredServiceCount() {
        return filteredServiceCount;
    }

    /**
     * Sets the filteredServiceCount.
     * 
     * @param filteredServiceCount
     *            the filteredServiceCount to set
     */
    public void setFilteredServiceCount(int filteredServiceCount) {
        this.filteredServiceCount = filteredServiceCount;
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
     * Returns the serviceCountList.
     * 
     * @return the serviceCountList
     */
    public List<StatisticsBean> getServiceCountList() {
        return serviceCountList;
    }

    /**
     * Sets the serviceCountList.
     * 
     * @param serviceCountList
     *            the serviceCountList to set
     */
    public void setServiceCountList(List<StatisticsBean> serviceCountList) {
        this.serviceCountList = serviceCountList;
    }

    /**
     * Returns the serviceList.
     * 
     * @return the serviceList
     */
    public List<ModelPopUpDataBean> getServiceList() {
        return serviceList;
    }

    /**
     * Sets the serviceList.
     * 
     * @param serviceList
     *            the serviceList to set
     */
    public void setServiceList(List<ModelPopUpDataBean> serviceList) {
        this.serviceList = serviceList;

    }

    /**
     * action to be perform on close model pop up window
     * 
     * @param e
     */
    public void closeWindow(ActionEvent e) {
        DataPaginator dataPaginator = (DataPaginator) e.getComponent()
                .findComponent("servicemodelpagination");
        if (null != dataPaginator) {
            dataPaginator.gotoFirstPage();
        }

    }

    /**
     * Sets the servicePieChart.
     * 
     * @param servicePieChart
     *            the servicePieChart to set
     */
    public void setServicePieChart(byte[] servicePieChart) {
        this.servicePieChart = servicePieChart;
    }

    /**
     * Returns the servicePieChart.
     * 
     * @return the servicePieChart
     */
    public byte[] getServicePieChart() {
        return servicePieChart;
    }

    /**
     * Sets the serviceHiddenField.
     * 
     * @param serviceHiddenField
     *            the serviceHiddenField to set
     */
    public void setServiceHiddenField(String serviceHiddenField) {
        this.serviceHiddenField = serviceHiddenField;
    }

    /**
     * Returns the serviceHiddenField.
     * 
     * @return the serviceHiddenField
     */
    public String getServiceHiddenField() {

        String currentServiceFilter = stateController.getCurrentServiceFilter();
        if (isIntervalRender()
                || !currentServiceFilter
                        .equalsIgnoreCase(this.previousServiceFilter)) {

            // On render of hidden field calling web service and set host group
            // statistics as well as pie chart byte array
            // get current instance of statisticsHandler
            StatisticsHandler statisticsHandler = (StatisticsHandler) FacesUtils
                    .getManagedBean(Constant.STATISTICS_HANDLER);
            if (statisticsHandler != null
                    && !statisticsHandler.isDashboardInfo()) {
                LOGGER
                        .debug("Setting Service statistics for SERVICE SUMMARY PORTLET");
                statisticsHandler.setServiceStatistics();

            } // end if
        } // end if
        setIntervalRender(false);

        // clean up the data before returning
        currentServiceFilter = null;

        return serviceHiddenField;
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
     * Sets the popupRowSize.
     * 
     * @param popupRowSize
     *            the popupRowSize to set
     */
    public void setPopupRowSize(int popupRowSize) {
        this.popupRowSize = popupRowSize;
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
     * boolean variable to used close and open model popup window
     */
    private boolean visible = false;

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
     * Sets the serviceNavHiddenField.
     * 
     * @param serviceNavHiddenField
     *            the serviceNavHiddenField to set
     */
    public void setServiceNavHiddenField(String serviceNavHiddenField) {
        this.serviceNavHiddenField = serviceNavHiddenField;
    }

    /**
     * Returns the serviceNavHiddenField.
     * 
     * @return the serviceNavHiddenField
     */
    public String getServiceNavHiddenField() {
        if (isIntervalRender()) {
            return serviceNavHiddenField;
        }
        StatisticsHandler statisticsHandler = (StatisticsHandler) FacesUtils
                .getManagedBean(Constant.STATISTICS_HANDLER);
        if (null != statisticsHandler) {
            SubpageIntegrator subpageIntegrator = statisticsHandler
                    .getSubpageIntegrator();
            if (subpageIntegrator.isInStatusViewer()) {
                // LOGGER
                // .warn("@@@@@@@@@@@@@ in SV of service statistics @@@@@@@@@");
                // // fetch the latest nav params
                subpageIntegrator.setNavigationParameters();
                // check for node type and node Id
                int nodeID = subpageIntegrator.getNodeID();
                NodeType nodeType = subpageIntegrator.getNodeType();

                if (prevNodeType == null
                        || ((nodeID != prevNodeId || !nodeType
                                .equals(prevNodeType)))) {
                    // LOGGER
                    // .warn(
                    // "@@@@@@@@@@@@@ service statistics: calling getServiceHiddenField @@@@@@@@@"
                    // );
                    // initialize it
                    prevNodeType = nodeType;
                    prevNodeId = nodeID;
                    setIntervalRender(true);
                    // update node type vals
                    statisticsHandler.setSelectedNodeType(nodeType);
                    statisticsHandler.setSelectedNodeName(subpageIntegrator
                            .getNodeName());
                    statisticsHandler.setSelectedNodeId(nodeID);
                    // update state-controller
                    stateController.update(nodeType, subpageIntegrator
                            .getNodeName(), nodeID);

                    this.getServiceHiddenField();
                    return serviceNavHiddenField;
                }

            }
        }
        return serviceNavHiddenField;
    }

    /**
     * Sets the serviceStatusFrmID.
     * 
     * @param serviceStatusFrmID
     *            the serviceStatusFrmID to set
     */
    public void setServiceStatusFrmID(String serviceStatusFrmID) {
        this.serviceStatusFrmID = serviceStatusFrmID;
    }

    /**
     * Returns the serviceStatusFrmID.
     * 
     * @return the serviceStatusFrmID
     */
    public String getServiceStatusFrmID() {
        return serviceStatusFrmID;
    }

}
