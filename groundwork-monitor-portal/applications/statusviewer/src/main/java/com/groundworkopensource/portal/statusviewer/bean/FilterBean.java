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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.faces.model.SelectItem;

import org.apache.log4j.Logger;

import com.groundworkopensource.common.utils.HostFilter;
import com.groundworkopensource.common.utils.ServiceFilter;
import com.groundworkopensource.portal.common.FilterAggregator;
import com.groundworkopensource.portal.statusviewer.common.NodeType;
import com.groundworkopensource.portal.statusviewer.common.PortletUtils;
import com.groundworkopensource.portal.statusviewer.handler.StateController;
import com.groundworkopensource.portal.statusviewer.handler.SubpageIntegrator;

/**
 * Class denoting the backing bean for the filter portlet
 * 
 * @author mridu_narang
 */

public class FilterBean implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -4539275789673563094L;

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(FilterBean.class
            .getName());

    /**
     * UI SelectItem list for Host States Filters
     */
    private ArrayList<SelectItem> hostFilterList = new ArrayList<SelectItem>();

    /**
     * UI SelectItem list for Service States Filters
     */
    private ArrayList<SelectItem> serviceFilterList = new ArrayList<SelectItem>();

    /**
     * Value of selected host filter
     */
    private String selectedHostFilter = "";

    /**
     * Value of selected service filter
     */
    private String selectedServiceFilter = "";

    /**
     * Filter Aggregator
     */
    private final FilterAggregator filterAggregator;

    /**
     * Error boolean to set if error occurred
     */
    private boolean error = false;

    /**
     * Error message to show on UI
     */
    private String errorMessage = "";

    /**
     * Boolean field to indicate if host filter is applicable
     */
    private boolean hostFilterVisible = true;

    /**
     * Boolean field to indicate if service filter is applicable
     */
    private boolean serviceFilterVisible = true;

    /**
     * Boolean field true if selected node type is host group other wise false.
     */
    private boolean hostGroupNodeType = false;

    /**
     * blank panel group render variable .true if selected node type is service
     * group other wise false
     */
    private boolean pnlGroupBlankRender = false;

    /**
     * selected NodeType
     */
    private NodeType selectedNodeType;

    /**
     * node Id
     */
    private int nodeId;

    /**
     * node Name
     */
    private String nodeName;

    /**
     * selectedNodeType
     */
    private NodeType prevNodeType;

    /**
     * nodeId
     */
    private int prevNodeId;
    /**
     * hiddenField
     */
    private String hiddenField;
    /**
     * StateController instance
     */
    private StateController stateController;

    /**
     * Default constructor
     */
    public FilterBean() {
        /**
         * Aggregator to populate list of host and service filters during bean
         * initialization
         */
        this.filterAggregator = FilterAggregator.getInstance();

        if (this.filterAggregator.isInitError()) {
            // Error loading filters
            setError(true);
            setErrorMessage("Error loading filters ! ");
            return;
        }

        // do the subpage integration
        handleSubpageIntegration();

        // initialize host and service filters
        initHostFilters();
        initServiceFilters();
    }

    /**
     * @return The host filter list
     */
    public ArrayList<SelectItem> getHostFilterList() {
        return this.hostFilterList;
    }

    /**
     * @param hostFilterList
     */
    public void setHostFilterList(ArrayList<SelectItem> hostFilterList) {
        this.hostFilterList = hostFilterList;
    }

    /**
     * @return The service filter list
     */
    public ArrayList<SelectItem> getServiceFilterList() {
        return this.serviceFilterList;
    }

    /**
     * @param selectedServiceFilter
     */
    public void setSelectedServiceFilter(String selectedServiceFilter) {
        this.selectedServiceFilter = selectedServiceFilter;
    }

    /**
     * @return The selected host filter
     */
    public String getSelectedHostFilter() {
        return this.selectedHostFilter;
    }

    /**
     * @param selectedHostFilter
     */
    public void setSelectedHostFilter(String selectedHostFilter) {
        this.selectedHostFilter = selectedHostFilter;
    }

    /**
     * @return The selected service filter
     */
    public String getSelectedServiceFilter() {
        return this.selectedServiceFilter;
    }

    /**
     * @param serviceFilterList
     */
    public void setServiceFilterList(ArrayList<SelectItem> serviceFilterList) {
        this.serviceFilterList = serviceFilterList;
    }

    /**
     * Method to initialize host filters as menu items
     */
    private void initHostFilters() {

        if (this.filterAggregator != null) {

            // Retrieve all host filters
            Map<String, HostFilter> hostFilters = this.filterAggregator
                    .getAllHostFilters();

            // Create set of all host filters
            Set<Map.Entry<String, HostFilter>> hostFilterSet = hostFilters
                    .entrySet();

            // Create iterator for host filter set
            Iterator<Map.Entry<String, HostFilter>> iterator = hostFilterSet
                    .iterator();

            /*
             * Populate host filter list in 'SelectItem' component of backing
             * bean
             */
            while (iterator.hasNext()) {
                Map.Entry<String, HostFilter> entry = iterator.next();
                this.hostFilterList.add(new SelectItem(entry.getKey(), entry
                        .getValue().getLabel()));
            }
        } else {
            LOGGER
                    .debug("Filter Aggregator is null. No host filters populated.");
        }
    }

    /**
     * Method to initialize service filters as menu items
     */
    private void initServiceFilters() {

        if (this.filterAggregator != null) {

            // Retrieve all service filters
            Map<String, ServiceFilter> serviceFilters = this.filterAggregator
                    .getAllServiceFilters();

            // Create set of all service filters
            Set<Map.Entry<String, ServiceFilter>> entries = serviceFilters
                    .entrySet();

            // Create iterator for service filter set
            Iterator<Map.Entry<String, ServiceFilter>> iterator = entries
                    .iterator();

            /*
             * Populate service filter list in 'SelectItem' component of backing
             * bean
             */
            while (iterator.hasNext()) {
                Map.Entry<String, ServiceFilter> entry = iterator.next();
                this.serviceFilterList.add(new SelectItem(entry.getKey(), entry
                        .getValue().getLabel()));
            }
        } else {
            LOGGER
                    .debug("Instance of Filter Aggregator is null. No service filters populated.");
        }
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
        return this.errorMessage;
    }

    /**
     * Sets the hostFilterVisible.
     * 
     * @param hostFilterVisible
     *            the hostFilterVisible to set
     */
    public void setHostFilterVisible(boolean hostFilterVisible) {
        this.hostFilterVisible = hostFilterVisible;
    }

    /**
     * Returns the hostFilterVisible.
     * 
     * @return the hostFilterVisible
     */
    public boolean isHostFilterVisible() {
        if (selectedNodeType.equals(NodeType.NETWORK)
                || selectedNodeType.equals(NodeType.HOST_GROUP)) {
            this.hostFilterVisible = true;
        } else {
            this.hostFilterVisible = false;
        }
        return this.hostFilterVisible;
    }

    /**
     * Sets the serviceFilterVisible.
     * 
     * @param serviceFilterVisible
     *            the serviceFilterVisible to set
     */
    public void setServiceFilterVisible(boolean serviceFilterVisible) {
        this.serviceFilterVisible = serviceFilterVisible;
    }

    /**
     * Returns the serviceFilterVisible.
     * 
     * @return the serviceFilterVisible
     */
    public boolean isServiceFilterVisible() {
        if (selectedNodeType.equals(NodeType.SERVICE)) {
            this.serviceFilterVisible = false;
        } else {
            this.serviceFilterVisible = true;
        }
        return this.serviceFilterVisible;
    }

    /**
     * Handles the subpage integration: Reads parameters from request in case of
     * Status Viewer. If portlet is in dashboard, reads preferences.
     */
    private void handleSubpageIntegration() {
        SubpageIntegrator subpageIntegrator = new SubpageIntegrator();
        // pass preferenceKeysMap as null
        boolean isPrefSet = subpageIntegrator.doSubpageIntegration(null);
        if (!isPrefSet) {
            /*
             * Filter Portlet is applicable for "Network View". So we should not
             * show error here - instead assign Node Type as NETWORK with NodeId
             * as 0.
             */
            this.selectedNodeType = NodeType.NETWORK;
            return;
        }
        // get the required data from SubpageIntegrator
        this.selectedNodeType = subpageIntegrator.getNodeType();
        stateController = subpageIntegrator.getStateController();
        // nullify subpage integrator object
        subpageIntegrator = null;

        LOGGER.debug("[Filter Portlet] # Node Type [" + selectedNodeType + "]");
    }

    /**
     * Sets the hostGroupNodeType.
     * 
     * @param hostGroupNodeType
     *            the hostGroupNodeType to set
     */
    public void setHostGroupNodeType(boolean hostGroupNodeType) {
        this.hostGroupNodeType = hostGroupNodeType;
    }

    /**
     * Returns the hostGroupNodeType.
     * 
     * @return the hostGroupNodeType
     */
    public boolean isHostGroupNodeType() {
        if (selectedNodeType == NodeType.HOST_GROUP) {
            return true;
        }
        return hostGroupNodeType;
    }

    /**
     * Sets the pnlGroupBlankRender.
     * 
     * @param pnlGroupBlankRender
     *            the pnlGroupBlankRender to set
     */
    public void setPnlGroupBlankRender(boolean pnlGroupBlankRender) {
        this.pnlGroupBlankRender = pnlGroupBlankRender;
    }

    /**
     * Returns the pnlGroupBlankRender.
     * 
     * @return the pnlGroupBlankRender
     */
    public boolean isPnlGroupBlankRender() {
        boolean inDashbord = PortletUtils.isInDashbord();
        if (inDashbord) {
            return false;
        }
        if (NodeType.SERVICE_GROUP == selectedNodeType) {
            pnlGroupBlankRender = true;
        }
        return pnlGroupBlankRender;

    }

    /**
     * Sets the hiddenField.
     * 
     * @param hiddenField
     *            the hiddenField to set
     */
    public void setHiddenField(String hiddenField) {
        this.hiddenField = hiddenField;
    }

    /**
     * Returns the hiddenField.
     * 
     * @return the hiddenField
     */
    public String getHiddenField() {
        if (null != stateController
                && (null == prevNodeType || (!prevNodeType
                        .equals(selectedNodeType) || prevNodeId != this.nodeId))) {
            prevNodeType = selectedNodeType;
            prevNodeId = this.nodeId;
            // update state-controller
            stateController.update(selectedNodeType, nodeName, nodeId);

            selectedHostFilter = stateController.getCurrentHostFilter();
            selectedServiceFilter = stateController.getCurrentServiceFilter();
        }

        return hiddenField;
    }

    /**
     * @param nodeType
     * @param nodeName
     * @param nodeId
     */
    public void update(NodeType nodeType, String nodeName, int nodeId) {
        this.selectedNodeType = nodeType;
        this.nodeId = nodeId;
        this.nodeName = nodeName;
    }

    /**
     * Returns the selectedNodeType.
     * 
     * @return the selectedNodeType
     */
    public NodeType getSelectedNodeType() {
        return selectedNodeType;
    }

    /**
     * Sets the selectedNodeType.
     * 
     * @param selectedNodeType
     *            the selectedNodeType to set
     */
    public void setSelectedNodeType(NodeType selectedNodeType) {
        this.selectedNodeType = selectedNodeType;
    }

    /**
     * Returns the nodeId.
     * 
     * @return the nodeId
     */
    public int getNodeId() {
        return nodeId;
    }

    /**
     * Sets the nodeId.
     * 
     * @param nodeId
     *            the nodeId to set
     */
    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    /**
     * Returns the nodeName.
     * 
     * @return the nodeName
     */
    public String getNodeName() {
        return nodeName;
    }

    /**
     * Sets the nodeName.
     * 
     * @param nodeName
     *            the nodeName to set
     */
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

}
