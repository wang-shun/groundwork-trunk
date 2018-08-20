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

package com.groundworkopensource.portal.statusviewer.handler;

import java.util.List;

import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.model.impl.CategoryEntity;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.FilterOperator;
import org.groundwork.foundation.ws.model.impl.Host;
import org.groundwork.foundation.ws.model.impl.ServiceStatus;

import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.common.FilterConstants;
import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.common.exception.PreferencesException;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;
import com.groundworkopensource.portal.common.ws.IWSFacade;
import com.groundworkopensource.portal.common.ws.WebServiceFactory;
import com.groundworkopensource.portal.common.ws.WebServiceFactory.WebServiceType;
import com.groundworkopensource.portal.common.ws.impl.FoundationWSFacade;
import com.groundworkopensource.webapp.console.EventBean;
import com.groundworkopensource.portal.statusviewer.bean.EventDataTableBean;
import com.groundworkopensource.portal.statusviewer.bean.EventFilterBean;
import com.groundworkopensource.portal.statusviewer.bean.EventListBean;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.webapp.console.DataPage;
import com.groundworkopensource.portal.statusviewer.common.FilterComputer;
import com.groundworkopensource.portal.statusviewer.common.NodeType;
import com.groundworkopensource.portal.statusviewer.common.PortletUtils;
import com.groundworkopensource.portal.statusviewer.bean.UserExtendedRoleBean;

/**
 * This class is a handler of Event portlet.
 * 
 * @author manish_kjain
 * 
 */
public class EventHandler {

    /**
     * Application type Query string
     */
    private static final String APPLICATION_TYPE_NAME = "applicationType.name";

    /**
     * logger
     */
    private static final Logger LOGGER = Logger.getLogger(EventHandler.class
            .getName());

    /**
     * EventFilterBean instance variable
     */
    private EventFilterBean eventFilterBean = null;

    /**
     * stateController instance variable
     */
    private StateController stateController = null;

    /**
     * selectedNodeId default 0
     */
    private int selectedNodeId = 0;
    /**
     * selectedNodeType
     */
    private NodeType selectedNodeType = NodeType.NETWORK;
    /**
     * selectedNodeName
     */
    private String selectedNodeName = "";

    /**
     * web Service foundation Instance
     */
    private final IWSFacade webServiceInstance = new WebServiceFactory()
            .getWebServiceInstance(WebServiceType.FOUNDATION_WEBSERVICE);

    /**
     * @param eventFilterBean
     * 
     */
    public EventHandler(EventFilterBean eventFilterBean) {

        stateController = new StateController();
        // If faces context is null, then it is JMS thread
        if (FacesContext.getCurrentInstance() != null) {
            this.eventFilterBean = (EventFilterBean) FacesUtils
                    .getManagedBean(Constant.EVENT_FILTER_BEAN);
        } else {
            this.eventFilterBean = eventFilterBean;
        }
    }

    /**
     * Populate events in to data table
     */
    public void populateEvents() {
        refreshDataTable(null);

    }

    /**
     * Initializes the data table page
     * 
     * @param eventListBeanParam
     * 
     */
    public void refreshDataTable(EventListBean eventListBeanParam) {
        EventListBean eventListBean = null;
        if (FacesContext.getCurrentInstance() != null) {
            eventListBean = (EventListBean) FacesUtils
                    .getManagedBean(Constant.EVENT_LIST_BEAN);
        } else {
            eventListBean = eventListBeanParam;
        }
        EventDataTableBean eventTableBean = eventListBean.getDataTableBean();
        eventTableBean.setLastPage(null);
        if (FacesContext.getCurrentInstance() == null) {
            eventTableBean.setEventFilterBean(eventFilterBean);
        }
        // eventTableBean.setLastStartRow(-1);
        DataPage page = eventTableBean.fetchPage(0, eventTableBean
                .getTableRows());
        eventTableBean.setPage(page);
        List<EventBean> eventList = page.getData();
        // end if
        EventBean[] events = eventList.toArray(new EventBean[eventList.size()]);
        eventTableBean.setEvents(events);

    }

    /**
     * Initializes the data table page and updates state controller with latest
     * node parameters.
     * 
     * @param eventListBeanParam
     * @param selectedNodeType
     * @param selectedNodeName
     * @param selectedNodeId
     */
    public void refreshDataTable(EventListBean eventListBeanParam,
            NodeType selectedNodeType, String selectedNodeName,
            int selectedNodeId) {
        setSelectedNodeId(selectedNodeId);
        setSelectedNodeName(selectedNodeName);
        setSelectedNodeType(selectedNodeType);

        // update state-controller
        stateController.update(selectedNodeType, selectedNodeName,
                selectedNodeId);
        // refresh the data table
        refreshDataTable(eventListBeanParam);
    }

    /**
     * Displays the events
     * 
     * @param eventList
     */
    @SuppressWarnings("unused")
    private void displayEvents(List<EventBean> eventList) {

        EventBean[] events = eventList.toArray(new EventBean[eventList.size()]);
        EventListBean eventListBean = (EventListBean) FacesUtils
                .getManagedBean(Constant.EVENT_LIST_BEAN);
        EventDataTableBean eventTableBean = eventListBean.getDataTableBean();

        if (eventTableBean != null) {
            eventTableBean.setEvents(events);
        }

    }

    /**
     * create the filter for host group
     * 
     * @param hostGroupName
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    public void setFilterForHostGroup(String hostGroupName)
            throws WSDataUnavailableException, GWPortalException {
        FilterComputer filterComputer = new FilterComputer();
        Filter filter = null;
        Filter operationStatusfilter = null;
        if (hostGroupName != null) {
            hostGroupName = hostGroupName.trim();
        }
        // create filter for Host group id
        Filter leftoperationStatusfilter = new Filter(
                FilterConstants.DEVICE_HOSTS_HOST_GROUPS_NAME,
                FilterOperator.EQ, hostGroupName);
        // create filter for get all open event
        Filter rightoperationStatusfilter = new Filter(
                FilterConstants.OPERATION_STATUS_NAME, FilterOperator.EQ,
                Constant.OPEN);
        Filter ackFilter = new Filter(
                FilterConstants.OPERATION_STATUS_NAME, FilterOperator.EQ,
                Constant.ACKNOWLEDGE);
        operationStatusfilter = Filter.AND(leftoperationStatusfilter, Filter.OR(rightoperationStatusfilter,ackFilter));
        String filterKey = stateController.getCurrentHostFilter();
        String currentServiceFilter = stateController.getCurrentServiceFilter();
        // check if any filter selected in filter portlet.
        if (Constant.EMPTY_STRING.equalsIgnoreCase(filterKey)
                && Constant.EMPTY_STRING.equalsIgnoreCase(currentServiceFilter)) {
            filter = operationStatusfilter;
        } else if (!Constant.EMPTY_STRING.equalsIgnoreCase(filterKey)
                && Constant.EMPTY_STRING.equalsIgnoreCase(currentServiceFilter)) {
            Filter rightHostFilter = filterComputer
                    .getEventHostFilter(filterKey);
            filter = Filter.AND(operationStatusfilter, rightHostFilter);
        } else if (Constant.EMPTY_STRING.equalsIgnoreCase(filterKey)
                && !Constant.EMPTY_STRING
                        .equalsIgnoreCase(currentServiceFilter)) {
            Filter rightServiceFilter = filterComputer
                    .getEventServiceFilter(currentServiceFilter);
            filter = Filter.AND(operationStatusfilter, rightServiceFilter);
        } else {
        	filter = getEventFilterForHG(hostGroupName);
        }
        
        if (eventFilterBean != null) {
	        eventFilterBean.setFilter(filter);
	        eventFilterBean.setPreviousSelectedHostFilterName(filterKey);
	        eventFilterBean
	                .setPreviousSelectedServiceFilterName(currentServiceFilter);
        }

        // this.initializePage();
        filterComputer = null;

    }

    /**
     * create the filter for Entire Network
     * 
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     * 
     * 
     */
    public void setFilterForNetWork() throws WSDataUnavailableException,
            GWPortalException {
        FilterComputer filterComputer = new FilterComputer();
        Filter filter = null;
        Filter leftfilter = null;

        leftfilter = new Filter(FilterConstants.OPERATION_STATUS_NAME,
                FilterOperator.EQ, Constant.OPEN);
        Filter ackFilter = new Filter(
                FilterConstants.OPERATION_STATUS_NAME, FilterOperator.EQ,
                Constant.ACKNOWLEDGE);
        leftfilter = Filter.AND(leftfilter, Filter.OR(leftfilter,ackFilter));
        // get host filter for entire network
        String hostFilterKey = stateController.getCurrentHostFilter();
        String serviceFilterKey = stateController.getCurrentServiceFilter();
        // check which filter selected on filter portlet .
        if (Constant.EMPTY_STRING.equalsIgnoreCase(hostFilterKey)
                && Constant.EMPTY_STRING.equalsIgnoreCase(serviceFilterKey)) {
        	
        	UserExtendedRoleBean userExtendedRoleBean = PortletUtils.getUserExtendedRoleBean();
            // if no filter selected then only left filter should apply
        	List<String> extRoleHostGroupList = userExtendedRoleBean
					.getExtRoleHostGroupList();
			List<String> extRoleServiceGroupList = userExtendedRoleBean
					.getExtRoleServiceGroupList();
			if (extRoleHostGroupList.isEmpty()
					&& extRoleServiceGroupList.isEmpty()) {
				filter = leftfilter;
			} else {
				StringBuilder authHostGroupsBuilder = new StringBuilder();
				for (String authorizedHostGroup : extRoleHostGroupList) {
					authHostGroupsBuilder.append(authorizedHostGroup);
					authHostGroupsBuilder.append(",");
				} // end for
				String authHostGroups = authHostGroupsBuilder.substring(0,
						authHostGroupsBuilder.length() - 1);

				filter = new Filter("hostStatus.host.hostGroups.name", FilterOperator.IN,
						authHostGroups);
				// filter = Filter.AND(leftFilter, tempfilter);
			}
        } else if (!Constant.EMPTY_STRING.equalsIgnoreCase(hostFilterKey)
                && Constant.EMPTY_STRING.equalsIgnoreCase(serviceFilterKey)) {
            // host filter is selected
            Filter rightFilter = filterComputer
                    .getEventHostFilter(hostFilterKey);
            filter = Filter.AND(leftfilter, rightFilter);
        } else if (Constant.EMPTY_STRING.equalsIgnoreCase(hostFilterKey)
                && !Constant.EMPTY_STRING.equalsIgnoreCase(serviceFilterKey)) {
            // service filter is selected
            Filter rightFilter = filterComputer
                    .getEventServiceFilter(serviceFilterKey);
            filter = Filter.AND(leftfilter, rightFilter);
        } else {
            // host and service filter is selected
            /*
             * Filter hostFilter = filterComputer
             * .getEventHostFilter(hostFilterKey); Filter serviceFilter =
             * filterComputer .getEventServiceFilter(serviceFilterKey); Filter
             * hostAndServiceFilter = Filter.AND(hostFilter, serviceFilter);
             * filter = Filter.AND(leftfilter, hostAndServiceFilter);
             */
            filter = this.getFilterForNetwork();
        }
        
        if (eventFilterBean != null) {
	        eventFilterBean.setFilter(filter);
	        eventFilterBean.setPreviousSelectedHostFilterName(hostFilterKey);
	        eventFilterBean.setPreviousSelectedServiceFilterName(serviceFilterKey);
        }
        filterComputer = null;
    }

    /**
     * set filter For service Group
     * 
     * @param serviceGroup
     * 
     * 
     */
    public void setFilterForServiceGroup(String serviceGroup) {
        Filter filter = null;
        FilterComputer filterComputer = new FilterComputer();
        try {

            FoundationWSFacade foundationWSFacade = new FoundationWSFacade();
            // get CategoryEntity for service Group
            CategoryEntity[] entities = foundationWSFacade
                    .getCategoryEntities(serviceGroup);

            Filter leftFilter = null;
            if (entities != null) {
                for (int i = 0; i < entities.length; i++) {
                    CategoryEntity entity = entities[i];
                    if (leftFilter != null) {
                        Filter servicefilter = new Filter(
                                FilterConstants.SERVICE_STATUS_SERVICE_STATUS_ID,
                                FilterOperator.EQ, entity.getObjectID());
                        leftFilter = Filter.OR(leftFilter, servicefilter);
                    } else {
                        leftFilter = new Filter(
                                FilterConstants.SERVICE_STATUS_SERVICE_STATUS_ID,
                                FilterOperator.EQ, entity.getObjectID());
                    } // end if
                } // end for

            } else {
                // If the category entities are null, then create a filter with
                // negative id, say -1, so it returns empty set.
                leftFilter = new Filter(
                        FilterConstants.SERVICE_STATUS_SERVICE_STATUS_ID,
                        FilterOperator.EQ, -1);
            } // end if
            String filterKey = stateController.getCurrentServiceFilter();
            String currentHostFilter = stateController.getCurrentHostFilter();
            // check if any filter selected in filter portlet.
            if (Constant.EMPTY_STRING.equalsIgnoreCase(filterKey)) {
                filter = leftFilter;
            } else {
                Filter rightFilter = filterComputer
                        .getEventServiceFilter(filterKey);
                filter = Filter.AND(leftFilter, rightFilter);
            }
            Filter openEventFilter = new Filter(
                    FilterConstants.OPERATION_STATUS_NAME, FilterOperator.EQ,
                    Constant.OPEN);
            Filter ackFilter = new Filter(
                    FilterConstants.OPERATION_STATUS_NAME, FilterOperator.EQ,
                    Constant.ACKNOWLEDGE);

            Filter finalFilter = Filter.AND(openEventFilter, Filter.OR(filter,ackFilter));
            if (eventFilterBean != null) {
	            eventFilterBean.setFilter(finalFilter);
	            eventFilterBean
	                    .setPreviousSelectedHostFilterName(currentHostFilter);
	            eventFilterBean.setPreviousSelectedServiceFilterName(filterKey);
            }

        } catch (Exception exc) {
            LOGGER.error(exc.getMessage());
        } finally {
            filterComputer = null;
        }
    }

    /**
     * create the filter for host
     * 
     * @param hostName
     * 
     * 
     * 
     * 
     */
    public void setFilterForHost(String hostName) {
        Filter filter = null;
        Filter operationStatusfilter = null;
        FilterComputer filterComputer = new FilterComputer();
        if (hostName != null) {
            hostName = hostName.trim();
        }
        // create filter for Host group id
        Filter leftoperationStatusfilter = new Filter(
                FilterConstants.DEVICE_HOSTS_HOST_NAME, FilterOperator.EQ,
                hostName);
        // create filter for get all open event
        Filter rightoperationStatusfilter = new Filter(
                FilterConstants.OPERATION_STATUS_NAME, FilterOperator.EQ,
                Constant.OPEN);
        Filter ackFilter = new Filter(
                FilterConstants.OPERATION_STATUS_NAME, FilterOperator.EQ,
                Constant.ACKNOWLEDGE);
        operationStatusfilter = Filter.AND(leftoperationStatusfilter, Filter.OR(rightoperationStatusfilter,ackFilter));

        String filterKey = stateController.getCurrentServiceFilter();
        String currenthostFilter = stateController.getCurrentHostFilter();
        // check if any filter selected in filter portlet.
        if (Constant.EMPTY_STRING.equalsIgnoreCase(filterKey)) {
            filter = operationStatusfilter;
        } else {
            Filter rightFilter = filterComputer
                    .getEventServiceFilter(filterKey);
            filter = Filter.AND(operationStatusfilter, rightFilter);
        }
        if (eventFilterBean != null) {
	        eventFilterBean.setFilter(filter);
	        eventFilterBean.setPreviousSelectedHostFilterName(currenthostFilter);
	        eventFilterBean.setPreviousSelectedServiceFilterName(filterKey);
        }
        filterComputer = null;
    }

    /**
     * create the filter for service
     * 
     * @param serviceId
     * @param inStatusViewer
     * @throws PreferencesException
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    public void setFilterForService(int serviceId, boolean inStatusViewer)
            throws PreferencesException, WSDataUnavailableException,
            GWPortalException {
        FilterComputer filterComputer = new FilterComputer();
        Filter filter = null;
        Filter operationStatusfilter = null;
        // create filter for Host group id
        Filter leftoperationStatusfilter = new Filter(
                FilterConstants.SERVICE_STATUS_SERVICE_STATUS_ID,
                FilterOperator.EQ, serviceId);
        // create filter for get all open event
        Filter rightoperationStatusfilter = new Filter(
                FilterConstants.OPERATION_STATUS_NAME, FilterOperator.EQ,
                Constant.OPEN);
        // filter for get all open event for particular host Group
        Filter ackFilter = new Filter(
                FilterConstants.OPERATION_STATUS_NAME, FilterOperator.EQ,
                Constant.ACKNOWLEDGE);
        operationStatusfilter = Filter.AND(leftoperationStatusfilter, Filter.OR(rightoperationStatusfilter,ackFilter));
        String serviceFilterKey = stateController.getCurrentServiceFilter();
        String currentHostFilter = stateController.getCurrentHostFilter();
        // check if any filter selected in filter portlet.
        if (Constant.EMPTY_STRING.equalsIgnoreCase(serviceFilterKey)) {
            filter = operationStatusfilter;
        } else {
            Filter rightFilter = filterComputer
                    .getEventServiceFilter(serviceFilterKey);
            filter = Filter.AND(operationStatusfilter, rightFilter);
        }
        if (eventFilterBean != null) {
	        eventFilterBean.setFilter(filter);
	        eventFilterBean.setPreviousSelectedHostFilterName(currentHostFilter);
	        eventFilterBean.setPreviousSelectedServiceFilterName(serviceFilterKey);
        }
        filterComputer = null;
    }

    /**
     * Create and populate data table bean
     */
    public void createAndPopulateDataTable() {
        EventListBean eventListBean = (EventListBean) FacesUtils
                .getManagedBean(Constant.EVENT_LIST_BEAN);
        EventDataTableBean eventDataTableBean = new EventDataTableBean();
        eventListBean.setDataTableBean(eventDataTableBean);

    }

    /**
     * return filter to get all event when host and service filter is applied.
     * 
     * @return filter
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    private Filter getEventFilterForHG(String hostGroupName)
            throws WSDataUnavailableException, GWPortalException {

        String currentHostFilterKey = stateController.getCurrentHostFilter();
        String currentServiceFilterKey = stateController
                .getCurrentServiceFilter();
        FilterComputer filterComputer = new FilterComputer();
        Filter hostFilter = filterComputer.getHostFilter(currentHostFilterKey);
        Filter serviceFilter = filterComputer
                .getServiceFilter(currentServiceFilterKey);
        Filter leftFilter = new Filter(FilterConstants.HOST_GROUPS_NAME,
                FilterOperator.EQ, hostGroupName);
        Filter hostGroupFlter = Filter.AND(leftFilter, hostFilter);
        Host[] hostsbyCriteria = null;
        // getting all host which belong to hostGroupID and Current selected
        // host filter
        hostsbyCriteria = webServiceInstance.getHostsbyCriteria(hostGroupFlter);
        String commaSepHostID = getCommaSepHostID(hostsbyCriteria);
        if (Constant.EMPTY_STRING.equalsIgnoreCase(commaSepHostID)) {
            LOGGER
                    .debug("Comma seprated  host id is empty.hence no event for selected  filter and filter:-"
                            + currentHostFilterKey);
            return null;
        }
        Filter serviceLeftFilter = new Filter(FilterConstants.HOST_HOST_ID,
                FilterOperator.IN, commaSepHostID);
        Filter serviceFilter1 = Filter.AND(serviceLeftFilter, serviceFilter);
        // getting all service which are in comma separated host id and satisfy
        // Current
        // selected Service filter
        ServiceStatus[] servicesbyCriteria = webServiceInstance
                .getServicesbyCriteria(serviceFilter1);
        String commaSepServiceID = getCommaSepServiceID(servicesbyCriteria);
        if (Constant.EMPTY_STRING.equalsIgnoreCase(commaSepServiceID)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER
                        .debug("Comma seprated service id is empty.hence no event for selected filter and Host filter :-"
                                + currentHostFilterKey
                                + " Service Filter:-"
                                + currentServiceFilterKey);
            }
            return null;
        }
        Filter finalleftFilter = new Filter(
                FilterConstants.SERVICE_STATUS_SERVICE_STATUS_ID,
                FilterOperator.IN, commaSepServiceID);
        // create filter for get all open event
        Filter finalrightFilter = new Filter(
                FilterConstants.OPERATION_STATUS_NAME, FilterOperator.EQ,
                Constant.OPEN);

        Filter ackFilter = new Filter(
                FilterConstants.OPERATION_STATUS_NAME, FilterOperator.EQ,
                Constant.ACKNOWLEDGE);
        Filter finalFilter = Filter.AND(finalleftFilter, Filter.OR(finalrightFilter, ackFilter));
        filterComputer = null;
        return finalFilter;
    }

    /**
     * return comma separated Host id String
     * 
     * @param hostArr
     * @return String
     */
    private String getCommaSepHostID(Host[] hostArr) {
        String hostIds = Constant.EMPTY_STRING;
        // create String for host ID
        StringBuffer hostIDBuilder = new StringBuffer(Constant.EMPTY_STRING);
        if (null != hostArr && hostArr.length > 0) {
            // creating comma Separated service Status ID String
            for (Host host : hostArr) {
                hostIDBuilder.append(host.getHostID());
                hostIDBuilder.append(Constant.COMMA);

            }
            int lastcommaindex = hostIDBuilder.lastIndexOf(Constant.COMMA);
            // remove comma at last
            hostIds = hostIDBuilder.substring(0, lastcommaindex);
        }

        return hostIds;

    }

    /**
     * return comma separated service id String
     * 
     * @param serviceArr
     * @return String
     */
    private String getCommaSepServiceID(ServiceStatus[] serviceArr) {
        String serviceIds = Constant.EMPTY_STRING;
        // create String for host ID
        StringBuffer serviceIDBuilder = new StringBuffer(Constant.EMPTY_STRING);
        if (null != serviceArr && serviceArr.length > 0) {
            // creating comma Separated service Status ID String
            for (ServiceStatus serviceStatus : serviceArr) {
                serviceIDBuilder.append(serviceStatus.getServiceStatusID());
                serviceIDBuilder.append(Constant.COMMA);

            }
            int lastcommaindex = serviceIDBuilder.lastIndexOf(Constant.COMMA);
            // remove comma at last
            serviceIds = serviceIDBuilder.substring(0, lastcommaindex);
        }

        return serviceIds;

    }

    /**
     * return left filter to get all open event if both service and host filter
     * is applied.
     * 
     * @return Filter
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    private Filter getFilterForNetwork() throws WSDataUnavailableException,
            GWPortalException {
        String currentHostFilterKey = stateController.getCurrentHostFilter();
        String currentServiceFilterKey = stateController
                .getCurrentServiceFilter();
        FilterComputer filterComputer = new FilterComputer();
        Filter hostFilter = filterComputer.getHostFilter(currentHostFilterKey);
        Filter serviceFilter = filterComputer
                .getServiceFilter(currentServiceFilterKey);

        // getting all host under applied host filter in filter portlet
        Host[] hostsbyCriteria = webServiceInstance
                .getHostsbyCriteria(hostFilter);
        String commaSepHostID = getCommaSepHostID(hostsbyCriteria);
        if (Constant.EMPTY_STRING.equalsIgnoreCase(commaSepHostID)) {
            LOGGER
                    .debug("Comma seprated  host id is empty.hence no event for selected  filter and filter:-"
                            + currentHostFilterKey);
            return null;
        }
        Filter serviceLeftFilter = new Filter(FilterConstants.HOST_HOST_ID,
                FilterOperator.IN, commaSepHostID);
        Filter serviceFilterFinal = Filter
                .AND(serviceLeftFilter, serviceFilter);
        // getting all service which are belong to comma separated host id and
        // satisfy
        // Current selected Service filter
        ServiceStatus[] servicesbyCriteria = webServiceInstance
                .getServicesbyCriteria(serviceFilterFinal);
        String commaSepServiceID = getCommaSepServiceID(servicesbyCriteria);
        if (Constant.EMPTY_STRING.equalsIgnoreCase(commaSepServiceID)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER
                        .debug("Comma seprated service id is empty.hence no event for selected filter and Host filter :-"
                                + currentHostFilterKey
                                + " Service Filter:-"
                                + currentServiceFilterKey);
            }
            return null;
        }
        Filter finalleftFilter = new Filter(
                FilterConstants.SERVICE_STATUS_SERVICE_STATUS_ID,
                FilterOperator.IN, commaSepServiceID);
        // create filter for get all open event
        Filter finalrightFilter = new Filter(
                FilterConstants.OPERATION_STATUS_NAME, FilterOperator.EQ,
                Constant.OPEN);
        Filter ackFilter = new Filter(
                FilterConstants.OPERATION_STATUS_NAME, FilterOperator.EQ,
                Constant.ACKNOWLEDGE);
        Filter finalFilter = Filter.AND(finalleftFilter, Filter.OR(finalrightFilter,ackFilter));
        filterComputer = null;
        return finalFilter;
    }

    /**
     * Returns the stateController.
     * 
     * @return the stateController
     */
    public StateController getStateController() {
        return stateController;
    }

    /**
     * Sets the stateController.
     * 
     * @param stateController
     *            the stateController to set
     */
    public void setStateController(StateController stateController) {
        this.stateController = stateController;
    }

    /**
     * Sets the selectedNodeId.
     * 
     * @param selectedNodeId
     *            the selectedNodeId to set
     */
    public void setSelectedNodeId(int selectedNodeId) {
        this.selectedNodeId = selectedNodeId;
    }

    /**
     * Returns the selectedNodeId.
     * 
     * @return the selectedNodeId
     */
    public int getSelectedNodeId() {
        return selectedNodeId;
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
     * Returns the selectedNodeType.
     * 
     * @return the selectedNodeType
     */
    public NodeType getSelectedNodeType() {
        return selectedNodeType;
    }

    /**
     * Sets the selectedNodeName.
     * 
     * @param selectedNodeName
     *            the selectedNodeName to set
     */
    public void setSelectedNodeName(String selectedNodeName) {
        this.selectedNodeName = selectedNodeName;
    }

    /**
     * Returns the selectedNodeName.
     * 
     * @return the selectedNodeName
     */
    public String getSelectedNodeName() {
        return selectedNodeName;
    }

}
