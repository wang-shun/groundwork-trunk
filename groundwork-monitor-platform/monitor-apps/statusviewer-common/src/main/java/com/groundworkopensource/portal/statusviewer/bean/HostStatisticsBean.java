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
 * This Bean provide host statistics to host Status portlet.
 * 
 * @author manish_kjain
 * 
 */
public class HostStatisticsBean extends ServerPush implements Serializable {

    // /**
    // * graphic img UI component ID constant part
    // */
    // private static final String NO_PIE_IMG = "NoPieImg";

    /**
     * graphic img UI component ID constant part
     */
    private static final String HOST_STATISTICS_PORTLET = "hostStatisticsPortlet_";

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 5587534800634343305L;

    /** Enable log4j for ServiceStatisticsBean class */
    private static final Logger LOGGER = Logger
            .getLogger(HostStatisticsBean.class.getName());

    /**
     * row count of host list.
     */
    private int hostRowCount;

    /**
     * total host count.
     */
    private long totalHostCount;
    /**
     * total filtered host count.
     */
    private int filteredHostCount;
    /**
     * current status of pop up window.
     */
    private String currentPopstatus = "empty";

    /**
     * hold previous selected host filter
     */
    private String previousSelectedHostFilter;

    /**
     * return byte array of service group Pie chart.
     */
    private byte[] hostPieChart;
    /**
     * hidden field to avoid web service call for pie chart.
     */
    private String hostHiddenField = Constant.HIDDEN;
    /**
     * rows of model pop data table
     */
    private int popupRowSize;
    /**
     * style
     */
    private String style = Constant.EMPTY_STRING;

    /**
     * StateController
     */
    private StateController stateController = null;

    /**
     * host Navigation Hidden Field
     */
    private String hostNavHiddenField = Constant.EMPTY_STRING;
    /**
     * previous node ID
     */
    private int prevNodeId;
    /**
     * previous node Type
     */
    private NodeType prevNodeType;

    /**
     * dynamic form id
     */
    private String hostStatusFrmID;

    /**
     * Returns the hostRowCount.
     * 
     * @return the hostRowCount
     */
    public int getHostRowCount() {
        return hostRowCount;
    }

    /**
     * Sets the hostRowCount.
     * 
     * @param hostRowCount
     *            the hostRowCount to set
     */
    public void setHostRowCount(int hostRowCount) {
        this.hostRowCount = hostRowCount;
    }

    /**
     * host count list
     */
    private List<StatisticsBean> hostCountList = Collections
            .synchronizedList(new ArrayList<StatisticsBean>());
    /**
     * host list.
     */
    private List<ModelPopUpDataBean> hostList = Collections
            .synchronizedList(new ArrayList<ModelPopUpDataBean>());

    /**
     * Returns the hostList.
     * 
     * @return the hostList
     */
    public List<ModelPopUpDataBean> getHostList() {
        return hostList;
    }

    /**
     * Sets the hostList.
     * 
     * @param hostList
     *            the hostList to set
     */
    public void setHostList(List<ModelPopUpDataBean> hostList) {
        this.hostList = hostList;

    }

    /**
     * Returns the totalHostCount.
     * 
     * @return the totalHostCount
     */
    public long getTotalHostCount() {
        return totalHostCount;
    }

    /**
     * Sets the totalHostCount.
     * 
     * @param totalHostCount
     *            the totalHostCount to set
     */
    public void setTotalHostCount(long totalHostCount) {
        this.totalHostCount = totalHostCount;
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
     * Returns the hostCountList.
     * 
     * @return the hostCountList
     */
    public List<StatisticsBean> gethostCountList() {
        return hostCountList;
    }

    /**
     * Sets the hostCountList.
     * 
     * @param hostCountList
     *            the hostCountList to set
     */
    public void setHostCountList(List<StatisticsBean> hostCountList) {

        this.hostCountList = hostCountList;
    }

    /**
     * Returns the filteredHostCount.
     * 
     * @return the filteredHostCount
     */
    public int getFilteredHostCount() {
        return filteredHostCount;
    }

    /**
     * Sets the filteredHostCount.
     * 
     * @param filteredHostCount
     *            the filteredHostCount to set
     */
    public void setFilteredHostCount(int filteredHostCount) {
        this.filteredHostCount = filteredHostCount;
    }

    /**
     * action to be perform on close model pop up window
     * 
     * @param e
     */
    public void closeWindow(ActionEvent e) {
        DataPaginator dataPaginator = (DataPaginator) e.getComponent()
                .findComponent("hostmodelpopupdatatable");
        if (null != dataPaginator) {
            dataPaginator.gotoFirstPage();
        }

        PopUpSelectBean popUpSelectBean = (PopUpSelectBean) FacesUtils
                .getManagedBean(Constant.POP_UP_SELECT_BEAN);
        if (popUpSelectBean != null) {
            popUpSelectBean.setSelectValue(Constant.FILTEREDHOST);
        }

    }

    /**
     * Sets the hostPieChart.
     * 
     * @param hostPieChart
     *            the hostPieChart to set
     */
    public void setHostPieChart(byte[] hostPieChart) {
        this.hostPieChart = hostPieChart;
    }

    /**
     * Returns the hostPieChart.
     * 
     * @return the hostPieChart
     */
    public byte[] getHostPieChart() {
        return hostPieChart;
    }

    /**
     * Sets the hostHiddenField.
     * 
     * @param hostHiddenField
     *            the hostHiddenField to set
     */
    public void setHostHiddenField(String hostHiddenField) {
        this.hostHiddenField = hostHiddenField;
    }

    /**
     * Returns the hostHiddenField.
     * 
     * @return the hostHiddenField
     */
    public String getHostHiddenField() {
        // LOGGER
        // .error(
        // "enter   getHostHiddenField ***********************************************"
        // );
        String currentHostFilter = stateController.getCurrentHostFilter();
        // This part is not called on JMS thread or if previous and current
        // selected filter are same.
        if (isIntervalRender()
                || !currentHostFilter
                        .equalsIgnoreCase(this.previousSelectedHostFilter)) {
            // On render of hidden field calling web service and set host group
            // statistics as well as pie chart byte array
            // get current instance of statisticsHandler

            // StatisticsHandler statisticsHandler = (StatisticsHandler)
            // FacesUtils
            // .getManagedBean(Constant.STATISTICS_HANDLER);
            StatisticsHandler statisticsHandler = (StatisticsHandler) FacesUtils
                    .getManagedBean(Constant.STATISTICS_HANDLER);
            if (statisticsHandler != null
                    && !statisticsHandler.isDashboardInfo()) {
                LOGGER.debug("Setting Host statistics................."
                        + statisticsHandler.getSelectedNodeType()
                        + statisticsHandler.getSelectedNodeName());

                statisticsHandler.setHostStatistics();
            } // end if
        } // end if
        setIntervalRender(false);

        // clean up the data before returning
        currentHostFilter = null;

        return hostHiddenField;
    }

    /**
     * constructor
     */
    public HostStatisticsBean() {
        try {
            popupRowSize = Integer.parseInt(PropertyUtils.getProperty(
                    ApplicationType.STATUS_VIEWER, "host.summary.popup.rows"));

        } catch (NumberFormatException numberFormatException) {
            LOGGER
                    .error("NumberFormatException while getting host popup page size from status-viewer properties files Hence default page size is set");
            popupRowSize = Constant.TEN;
        } catch (Exception e) {
            LOGGER
                    .error("Exception while getting host popup page size from status-viewer properties files Hence default page size is set");
            popupRowSize = Constant.TEN;
        }
        stateController = new StateController();
        if (!PortletUtils.isInStatusViewer()) {
            int randomID = new Random().nextInt(Constant.TEN_HOUSANED);
            hostStatusFrmID = HOST_STATISTICS_PORTLET + "frm" + randomID;
        }
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
     * Sets the style.
     * 
     * @param style
     *            the style to set
     */
    public void setStyle(String style) {
        this.style = style;
    }

    /**
     * Returns the style.
     * 
     * @return the style
     */
    public String getStyle() {
        boolean inDashbord = PortletUtils.isInDashbord();
        if (!inDashbord) {
            style = Constant.WIDTH_490PX;
        }
        return style;
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
     * Sets the hostNavHiddenField.
     * 
     * @param hostNavHiddenField
     *            the hostNavHiddenField to set
     */
    public void setHostNavHiddenField(String hostNavHiddenField) {
        this.hostNavHiddenField = hostNavHiddenField;
    }

    /**
     * Returns the hostNavHiddenField.
     * 
     * @return the hostNavHiddenField
     */
    public String getHostNavHiddenField() {
        if (isIntervalRender()) {
            return hostNavHiddenField;
        }
        StatisticsHandler statisticsHandler = (StatisticsHandler) FacesUtils
                .getManagedBean(Constant.STATISTICS_HANDLER);
        if (null != statisticsHandler) {
            SubpageIntegrator subpageIntegrator = statisticsHandler
                    .getSubpageIntegrator();
            if (subpageIntegrator.isInStatusViewer()) {
                // fetch the latest nav params
                subpageIntegrator.setNavigationParameters();
                // check for node type and node Id
                int nodeID = subpageIntegrator.getNodeID();
                NodeType nodeType = subpageIntegrator.getNodeType();

                if (prevNodeType == null
                        || ((nodeID != prevNodeId || !nodeType
                                .equals(prevNodeType)))) {
                    prevNodeType = nodeType;
                    prevNodeId = nodeID;
                    // update node type vals
                    statisticsHandler.setSelectedNodeType(nodeType);
                    statisticsHandler.setSelectedNodeName(subpageIntegrator
                            .getNodeName());
                    statisticsHandler.setSelectedNodeId(nodeID);

                    // update state-controller
                    stateController.update(nodeType, subpageIntegrator
                            .getNodeName(), nodeID);
                    setIntervalRender(true);
                    this.getHostHiddenField();
                }
            }
        }
        return hostNavHiddenField;
    }

    /**
     * Sets the prevNodeId.
     * 
     * @param prevNodeId
     *            the prevNodeId to set
     */
    public void setPrevNodeId(int prevNodeId) {
        this.prevNodeId = prevNodeId;
    }

    /**
     * Returns the prevNodeId.
     * 
     * @return the prevNodeId
     */
    public int getPrevNodeId() {
        return prevNodeId;
    }

    /**
     * Sets the prevNodeType.
     * 
     * @param prevNodeType
     *            the prevNodeType to set
     */
    public void setPrevNodeType(NodeType prevNodeType) {
        this.prevNodeType = prevNodeType;
    }

    /**
     * Returns the prevNodeType.
     * 
     * @return the prevNodeType
     */
    public NodeType getPrevNodeType() {
        return prevNodeType;
    }

    /**
     * Sets the hostStatusFrmID.
     * 
     * @param hostStatusFrmID
     *            the hostStatusFrmID to set
     */
    public void setHostStatusFrmID(String hostStatusFrmID) {
        this.hostStatusFrmID = hostStatusFrmID;
    }

    /**
     * Returns the hostStatusFrmID.
     * 
     * @return the hostStatusFrmID
     */
    public String getHostStatusFrmID() {
        return hostStatusFrmID;
    }

}
