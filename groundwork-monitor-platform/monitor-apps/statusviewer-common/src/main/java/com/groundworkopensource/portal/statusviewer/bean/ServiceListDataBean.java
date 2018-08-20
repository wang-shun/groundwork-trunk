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

import com.groundworkopensource.portal.common.FilterConstants;
import com.groundworkopensource.portal.common.ResourceUtils;
import com.groundworkopensource.portal.common.exception.GWPortalGenericException;
import com.groundworkopensource.portal.common.ws.IWSFacade;
import com.groundworkopensource.portal.common.ws.impl.FoundationWSFacade;
import com.groundworkopensource.portal.statusviewer.common.CollagePropertyTypeConstants;
import com.groundworkopensource.portal.statusviewer.common.CommonUtils;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.DateUtils;
import com.groundworkopensource.portal.statusviewer.common.MonitorStatusUtilities;
import com.groundworkopensource.portal.statusviewer.common.NodeType;
import com.groundworkopensource.portal.statusviewer.common.NodeURLBuilder;
import com.groundworkopensource.portal.statusviewer.common.ServiceListDataPage;
import com.groundworkopensource.portal.statusviewer.common.ServicePagedListDataModel;
import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.model.impl.BooleanProperty;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.MonitorStatus;
import org.groundwork.foundation.ws.model.impl.PropertyTypeBinding;
import org.groundwork.foundation.ws.model.impl.ServiceStatus;
import org.groundwork.foundation.ws.model.impl.Sort;
import org.groundwork.foundation.ws.model.impl.SortItem;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

import javax.faces.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * This class provides model for generating dynamic list. pages will be
 * generated on demand, instead of loading everything on startup.
 * 
 * @author nitin_jadhav
 */
public class ServiceListDataBean extends ServicePagedListDataModel {

    /**
     * Last start Row
     */
    private int lastStartRow = -1;

    /**
     * initial last page is null.
     */
    private ServiceListDataPage lastPage = null;

    /**
     * Logger
     */
    private static final Logger LOGGER = Logger
            .getLogger(ServiceListDataBean.class.getName());

    /**
     * property UNAVAILABLE
     */
    private static final String UNAVAILABLE = ResourceUtils
            .getLocalizedMessage("com_groundwork_portal_statusviewer_unavailable");

    /**
     * number of rows in table
     */
    private int tableRows;

    /**
     * Max Status Information Details characters. This property will be read
     * from properties file. if not present, it will take default size 10.
     */
    private int maxStatusInformationDetailsChars;

    /**
     * Default sort column name
     */
    private String sortColumnName = Constant.SERVICE_LIST_SORT_COLUMN_NAME;

    /**
     * default sorting order.
     */
    private boolean ascending = true;

    /**
     * IWSFacade instance variable.
     */
    private final IWSFacade foundFacade;

    /**
     * Filter for getting services under Host OR HostGroup. this is class's own
     * filters, as opposed to external filters provided by state controller.
     */
    private Filter primaryFilter;

    /**
     * Final filter, which is combination of primaryFilter and external filter
     * provided by state controller.
     */
    private Filter finalFilter;
    
    /**
     * show acknowledged Flag to get only not acknowledge services
     */
    private boolean showNonAcknowledged = false;

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
     * lastUpdateString
     */
    private String lastUpdateString = ResourceUtils
            .getLocalizedMessage("com_groundwork_portal_statusviewer_txtLastUpdate");

    /**
     * Constructor
     * 
     * @param pageSize
     */
    public ServiceListDataBean(int pageSize) {
        super(pageSize);
        foundFacade = new FoundationWSFacade();
        tableRows = pageSize;
    }

    /**
     * returns specified data page.
     * 
     * @see com.groundworkopensource.portal.statusviewer.common.ServicePagedListDataModel#fetchPage(int,
     *      int)
     */
    @Override
    public ServiceListDataPage fetchPage(int startRow, int pageSize) {
        return getDataPage(startRow, pageSize);
    }

    /**
     * Gets the data page
     * 
     * @param startRow
     * @param pageSize
     * @return
     */
    private ServiceListDataPage getDataPage(int startRow, int pageSize) {
        try {
            if (lastPage == null || startRow != lastStartRow) {
                List<ServiceBean> serviceList = null;
                Sort sort = new Sort(isAscending(), sortColumnName);

                // if final filter is not null then use final filter, else copy
                // and
                // use primary filter
                if (finalFilter == null) {
                    finalFilter = primaryFilter;
                }
                // query services
                serviceList = queryForServicesByFilter(finalFilter, startRow,
                        sort);
                // end if
                int dataSetSize = 0;
                if (serviceList.size() > 0) {
                    dataSetSize = serviceList.get(0).getTotalCount();
                } // end if
                lastStartRow = startRow;
                setLastPage(new ServiceListDataPage(dataSetSize, startRow,
                        serviceList));
            }
        } catch (Exception ex) {
            LOGGER.error("Exception occured in getDataPage method" + ex);
            this.setMessage(true);
            this.setError(true);
            this.setErrorMessage(ex.getMessage());
        }
        return lastPage;
    }

    /**
     * Queries events by selected filter
     * 
     * @param filter
     * @param startIndex
     * @param sort
     *            of ServiceBean
     * @return List
     */
    public List<ServiceBean> queryForServicesByFilter(Filter filter,
            int startIndex, Sort sort) {
        List<ServiceBean> servicesList = new ArrayList<ServiceBean>();
        WSFoundationCollection wsfoundationCollection = null;

        String sortColumn = sort.getSortItem(0).getPropertyName();
        SortItem sortItem = new SortItem();
        sortItem.setPropertyName(sortColumn);
        sortItem.setSortAscending(sort.getSortItem(0).isSortAscending());
        sort.setSortItem(0, sortItem);
        try {
            wsfoundationCollection = foundFacade.getServicesbyCriteria(filter,
                    sort, startIndex, tableRows);
        } catch (GWPortalGenericException ge) {
            this.setMessage(true);
            this.setErrorMessage(ge.getMessage());
            this.setError(true);
        }
        // total count
        if (wsfoundationCollection != null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER
                        .debug("Got "
                                + startIndex
                                + " to "
                                + (startIndex + tableRows)
                                + "rows from getServicesbyCriteria() in queryForHostsByFilter()");
            }
            ServiceStatus[] serviceStatus = wsfoundationCollection
                    .getServiceStatus();
            int totalCount = wsfoundationCollection.getTotalCount();
            if (serviceStatus == null) {
                return servicesList;
            }
            
            // get correct totalcount if showNonAcknowledged true
            if (showNonAcknowledged) {
               for (ServiceStatus service : serviceStatus) {
                    PropertyTypeBinding propertyTypeBinding = service
                            .getPropertyTypeBinding();
                    if (propertyTypeBinding != null) {
                        boolean isAcknowledged = false;

                        BooleanProperty isAcknowledgedValue = propertyTypeBinding
                                .getBooleanProperty(FilterConstants.IS_PROBLEM_ACKNOWLEDGED);
                        if (isAcknowledgedValue != null
                                && isAcknowledgedValue.isValue()) {
                           isAcknowledged = true;
                           totalCount--;
                        }
                    }
                }
            }

            // get ServiceBean object out of information from serviceStatus and
            // store into list

            for (ServiceStatus service : serviceStatus) {
                PropertyTypeBinding propertyTypeBinding = service
                        .getPropertyTypeBinding();
                if (propertyTypeBinding != null) {
                    String lastPluginOutput = (String) propertyTypeBinding
                            .getPropertyValue(CollagePropertyTypeConstants.LAST_PLUGIN_OUTPUT_PROPERTY);
                    String tooltip = null;
                    if (lastPluginOutput == null) {
                        lastPluginOutput = UNAVAILABLE;
                    } else {
                        // not null
                        if (lastPluginOutput.length() > maxStatusInformationDetailsChars) {
                            tooltip = lastPluginOutput;
                            lastPluginOutput = lastPluginOutput.substring(0,
                                    maxStatusInformationDetailsChars)
                                    + Constant.ELLIPSES;
                        } else {
                            tooltip = lastUpdateString
                                    + service.getLastCheckTime();
                        }
                    }
                    boolean isAcknowledged = false;

                    BooleanProperty isAcknowledgedValue = propertyTypeBinding
                            .getBooleanProperty(FilterConstants.IS_PROBLEM_ACKNOWLEDGED);
                    if (isAcknowledgedValue != null
                            && isAcknowledgedValue.isValue()) {
                        isAcknowledged = true;
                    }
                    String applicationType = CommonUtils.getApplicationNameByID(service.getApplicationTypeID());
                    ServiceBean serviceBean = new ServiceBean(service
                            .getDescription(), service.getHost().getName(),
                            MonitorStatusUtilities.getEntityStatus(service,
                                    NodeType.SERVICE), DateUtils
                                    .computeDuration(service
                                            .getLastStateChange()),
                            lastPluginOutput, isAcknowledged, tooltip, applicationType);
                    serviceBean
                            .setMaxStatusInfoTooltipChars(maxStatusInformationDetailsChars);
                    serviceBean.setTotalCount(totalCount);

                    // Set URL for service
                    serviceBean.setUrl(NodeURLBuilder.buildNodeURL(
                            NodeType.SERVICE, service.getServiceStatusID(),
                            service.getDescription()));
                    // Set URL for host
                    serviceBean.setParentURL(NodeURLBuilder.buildNodeURL(
                            NodeType.HOST, service.getHost().getHostID(),
                            service.getHost().getName()));

                    // Set the acknowledge status property - e.g. Not to
                    // acknowledge if in OK/PENDING state.
                    serviceBean.setAcknowledgeStatus(true);

                    MonitorStatus monitorStatus = service.getMonitorStatus();

                    if (monitorStatus != null) {
                        if ((monitorStatus.getName()
                                .equalsIgnoreCase(Constant.OK))
                                || (monitorStatus.getName()
                                        .equalsIgnoreCase(Constant.PENDING))) {
                            serviceBean.setAcknowledgeStatus(false);
                        }
                    } else {
                        LOGGER
                                .debug("queryForServicesByFilter(): Monitor Status for service "
                                        + service.getDescription() + " is null");
                    }
                    if (isAcknowledged && showNonAcknowledged) {
                        //do not add the service to the list because it is acknowledged
                    }
                    else {
                    	servicesList.add(serviceBean);
                    }
                    
                }
            }
        }
        return servicesList;
    }

    /**
     * Returns the lastStartRow.
     * 
     * @return the lastStartRow
     */
    public int getLastStartRow() {
        return lastStartRow;
    }

    /**
     * Sets the lastStartRow.
     * 
     * @param lastStartRow
     *            the lastStartRow to set
     */
    public void setLastStartRow(int lastStartRow) {
        this.lastStartRow = lastStartRow;
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
     * Sets the tableRows.
     * 
     * @param tableRows
     *            the tableRows to set
     */
    public void setTableRows(int tableRows) {
        this.tableRows = tableRows;
    }

    /**
     * Listener for sorting. This method is responsible to sort data table
     * column and set appropriate image on column.
     * 
     * @param event
     * 
     */
    public void sort(ActionEvent event) {
        ascending = !ascending;
        lastPage = null;
        // set sortColumnName over here to default one - "serviceDescription"
        sortColumnName = Constant.SERVICE_LIST_SORT_COLUMN_NAME;
        page = fetchPage(0, tableRows);
    }

    /**
     * Sets the lastPage.
     * 
     * @param lastPage
     *            the lastPage to set
     */
    public void setLastPage(ServiceListDataPage lastPage) {
        this.lastPage = lastPage;
    }

    /**
     * Returns the lastPage.
     * 
     * @return the lastPage
     */
    public ServiceListDataPage getLastPage() {
        return lastPage;
    }

    /**
     * Sets the sortColumnName.
     * 
     * @param sortColumnName
     *            the sortColumnName to set
     */
    public void setSortColumnName(String sortColumnName) {
        this.sortColumnName = sortColumnName;
    }

    /**
     * Returns the sortColumnName.
     * 
     * @return the sortColumnName
     */
    public String getSortColumnName() {
        return sortColumnName;
    }

    /**
     * Sets the primaryFilter.
     * 
     * @param primaryFilter
     *            the primaryFilter to set
     */
    public void setPrimaryFilter(Filter primaryFilter) {
        this.primaryFilter = primaryFilter;
    }

    /**
     * Returns the primaryFilter.
     * 
     * @return the primaryFilter
     */
    public Filter getPrimaryFilter() {
        return primaryFilter;
    }
    
    /**
     * Returns the showNonAcknowledged flag.
     * 
     * @return the showNonAcknowledged
     */
    public boolean getShowNonAcknowledged() {
        return showNonAcknowledged;
    }
    
    /**
     * Sets the showNonAcknowledged flag.
     * 
     * @param showNonAcknowledged
     *            the showNonAcknowledged flag 
     */
    public void setShowNonAcknowledged(boolean showNonAcknowledged) {
        this.showNonAcknowledged = showNonAcknowledged;
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
    public boolean isAscending() {
        return ascending;
    }

    /**
     * Sets the finalFilter.
     * 
     * @param finalFilter
     *            the finalFilter to set
     */
    public void setFinalFilter(Filter finalFilter) {
        this.finalFilter = finalFilter;
    }

    /**
     * Returns the finalFilter.
     * 
     * @return the finalFilter
     */
    public Filter getFinalFilter() {
        return finalFilter;
    }

    /**
     * Sets the maxStatusInformationDetailsChars.
     * 
     * @param maxStatusInformationDetailsChars
     *            the maxStatusInformationDetailsChars to set
     */
    public void setMaxStatusInformationDetailsChars(
            int maxStatusInformationDetailsChars) {
        this.maxStatusInformationDetailsChars = maxStatusInformationDetailsChars;
    }

    /**
     * Returns the maxStatusInformationDetailsChars.
     * 
     * @return the maxStatusInformationDetailsChars
     */
    public int getMaxStatusInformationDetailsChars() {
        return maxStatusInformationDetailsChars;
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
        return errorMessage;
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
     * Method sets the error flag to true, sets the error message to be
     * displayed to the user and logs the error. Ideally each catch block should
     * call this method.
     * 
     * @param resourceKey
     *            - key for the localized message to be displayed on the UI.
     * @param logMessage
     *            - message to be logged.
     * 
     */
    public void handleError(String resourceKey, String logMessage) {
        setMessage(true);
        setError(true);
        setErrorMessage(ResourceUtils.getLocalizedMessage(resourceKey));
        LOGGER.error(logMessage);
    }

    /**
     * Handles Info : sets Info flag and message.
     * 
     * @param infoMessage
     */
    public void handleInfo(String infoMessage) {
        setMessage(true);
        setInfo(true);
        setInfoMessage(infoMessage);
    }
}
