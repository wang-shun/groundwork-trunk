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
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.model.impl.BooleanProperty;
import org.groundwork.foundation.ws.model.impl.DateProperty;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.Host;
import org.groundwork.foundation.ws.model.impl.HostQueryType;
import org.groundwork.foundation.ws.model.impl.MonitorStatus;
import org.groundwork.foundation.ws.model.impl.PropertyTypeBinding;
import org.groundwork.foundation.ws.model.impl.SimpleHost;
import org.groundwork.foundation.ws.model.impl.Sort;
import org.groundwork.foundation.ws.model.impl.SortCriteria;
import org.groundwork.foundation.ws.model.impl.SortItem;
import org.groundwork.foundation.ws.model.impl.StringProperty;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;
import org.jfree.util.Log;

import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.common.FilterConstants;
import com.groundworkopensource.portal.common.ResourceUtils;
import com.groundworkopensource.portal.common.exception.GWPortalGenericException;
import com.groundworkopensource.portal.common.ws.IWSFacade;
import com.groundworkopensource.portal.common.ws.impl.FoundationWSFacade;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.DateUtils;
import com.groundworkopensource.portal.statusviewer.common.HostListDataPage;
import com.groundworkopensource.portal.statusviewer.common.HostPagedListDataModel;
import com.groundworkopensource.portal.statusviewer.common.MonitorStatusUtilities;
import com.groundworkopensource.portal.statusviewer.common.NetworkMetaEntity;
import com.groundworkopensource.portal.statusviewer.common.NetworkObjectStatusEnum;
import com.groundworkopensource.portal.statusviewer.common.NodeType;
import com.groundworkopensource.portal.statusviewer.common.NodeURLBuilder;
import com.groundworkopensource.portal.statusviewer.handler.ReferenceTreeMetaModel;

/**
 * This class provides model for generating dynamic list. pages will be
 * generated on demand, instead of loading everything on startup.
 * 
 * @author nitin_jadhav
 */
/**
 * @author nitin_jadhav
 * 
 */
public class HostListDataBean extends HostPagedListDataModel {

    /**
     * property UNAVAILABLE
     */
    private static final String UNAVAILABLE = ResourceUtils
            .getLocalizedMessage("com_groundwork_portal_statusviewer_unavailable");

    /**
     * Last start Row
     */
    private int lastStartRow = -1;

    /**
     * initial last page is null.
     */
    private HostListDataPage lastPage = null;

    /**
     * Logger
     */
    private static final Logger LOGGER = Logger
            .getLogger(HostListDataBean.class.getName());

    /**
     * <BR>
     */
    private static final String BR = "<BR/>";

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
     * Sort column name
     */
    private String sortColumnName = Constant.HOST_LIST_SORT_COLUMN_NAME;

    /**
     * default sorting order.
     */
    private boolean ascending = true;

    /**
     * IWSFacade instance variable.
     */
    private IWSFacade foundFacade = null;

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
     * selectedNodeType
     */
    private NodeType selectedNodeType;

    /**
     * Flag for "Show Non up hosts" filter.
     */
    private boolean showNonUpHosts = false;
    
    /**
     * Flag for "Show Non unreachable hosts" filter.
     */
    private boolean showNonUnreachableHosts = false;

    /**
     * Flag for "Show Non down unscheduled hosts" filter.
     */
    private boolean showNonDownUnScheduledHosts = false;
    
    /**
     * Flag for "Show Non down scheduled hosts" filter.
     */
    private boolean showNonDownScheduledHosts = false;
    
    /**
     * Flag for "Show Non pending hosts" filter.
     */
    private boolean showNonPendingHosts = false;
    
    /**
     * Flag for "Show Non down Acknowledged hosts" filter.
     */
    private boolean showNonAcknowledgedHosts = false;
    
    /**
     * lastUpdateString
     */
    private String lastUpdateString = ResourceUtils
            .getLocalizedMessage("com_groundwork_portal_statusviewer_txtLastUpdate");

    /**
     * ReferenceTreeMetaModel object
     */
    private ReferenceTreeMetaModel referenceTreeModel = (ReferenceTreeMetaModel) FacesUtils
            .getManagedBean(Constant.REFERENCE_TREE);

    /**
     * @param pageSize
     */
    public HostListDataBean(int pageSize) {
        super(pageSize);
        foundFacade = new FoundationWSFacade();
        tableRows = pageSize;
    }

    /**
     * (non-Javadoc)
     * 
     * @see com.groundworkopensource.portal.statusviewer.common.HostPagedListDataModel#fetchPage(int,
     *      int)
     */
    @Override
    public HostListDataPage fetchPage(int startRow, int pageSize) {
        return getDataPage(startRow, pageSize);
    }

    /**
     * Gets the specified data page
     * 
     * @param startRow
     * @param pageSize
     * @return HostListDataPage
     */
    private HostListDataPage getDataPage(int startRow, int pageSize) {
        if (lastPage == null || startRow != lastStartRow) {
            List<HostBean> hostList = null;
            Sort sort = new Sort(isAscending(), sortColumnName);

            // if final filter is not null then use final filter, else copy and
            // use primary filter
            if (finalFilter == null) {
                finalFilter = primaryFilter;
            }

            hostList = queryForHostsByFilter(finalFilter, startRow, sort);

            // end if
            int dataSetSize = 0;
            if (hostList.size() > 0) {
                dataSetSize = hostList.get(0).getTotalCount();
            } // end if
            lastStartRow = startRow;
            setLastPage(new HostListDataPage(dataSetSize, startRow, hostList));

        }
        return lastPage;
    }

    /**
     * Queries events by filter
     * 
     * @param filter
     * @param startIndex
     * @param sort
     * @return List
     */
    public List<HostBean> queryForHostsByFilter(Filter filter, int startIndex,
            Sort sort) {
        List<HostBean> hostList = new ArrayList<HostBean>();
        WSFoundationCollection collection = null;

        String sortColumn = sort.getSortItem(0).getPropertyName();
        SortItem sortItem = new SortItem();
        sortItem.setPropertyName(sortColumn);
        sortItem.setSortAscending(sort.getSortItem(0).isSortAscending());
        sort.setSortItem(0, sortItem);
        // sorting troubled services
        if (FilterConstants.HOST_MONITOR_STATUS.equalsIgnoreCase(sortColumn)) {
            SortItem sortItem1 = new SortItem();
            sortItem1.setPropertyName("serviceStatuses.monitorStatus");
            sortItem1.setSortAscending(false);
            SortItem SortItemArray[] = { sortItem, sortItem1 };
            sort.setSortItem(SortItemArray);
        }

        try {
            collection = foundFacade.getSimpleHostsbyCriteria(filter, sort,
                    startIndex, tableRows, true);

        } catch (GWPortalGenericException ge) {
            setMessage(true);
            setErrorMessage(ge.getMessage());
            setError(true);
        }
        if (collection != null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER
                        .debug("Got "
                                + startIndex
                                + " to "
                                + (startIndex + tableRows)
                                + "rows from getHostsbyCriteria() in queryForHostsByFilter()");
            }

            SimpleHost[] simpleHosts = collection.getSimpleHost();
            if (simpleHosts == null) {
                return hostList;
            }
            
            // totalCount - count(acknowledged hosts)
            int totalCount = collection.getTotalCount();
            if (showNonAcknowledgedHosts) {
                for (SimpleHost host : simpleHosts) {
                	if (host.isAcknowledged()) {
                        totalCount--;
                    }
                }
            } 

            for (SimpleHost host : simpleHosts) {
                String duration;
                String statusInfoDetails;
                // Duration
                if (host.getLastStateChange() != null) {
                    duration = DateUtils.computeDuration(host
                            .getLastStateChange());
                } else {
                    duration = Constant.EMPTY_STRING;
                }
                String tooltip = null;

                // Status Information Details
                if (host.getLastPlugInOutput() != null) {
                	StringTokenizer stkn = new StringTokenizer(host.getLastPlugInOutput(),"^^^");
					String currentAttempt = (stkn.hasMoreTokens() ? stkn.nextToken() : "NA") + " of " + (stkn.hasMoreTokens() ? stkn.nextToken() : "NA");
					int inDownTime = Integer.parseInt((stkn.hasMoreTokens() ? stkn.nextToken() : "0"));
					String lastStateChange = (stkn.hasMoreTokens() ? stkn.nextToken() : "NA");
					statusInfoDetails = (stkn.hasMoreTokens() ? stkn.nextToken() : "NA");
					String nextCheckTime = (stkn.hasMoreTokens() ? stkn.nextToken() : "NA");
                    LOGGER.debug("Current attempt :" + currentAttempt + "; ScheduledDownTime : " + inDownTime + "; LastStateChange : " + lastStateChange + "; NextCheckTime : " + nextCheckTime);
                    if (statusInfoDetails.length() > maxStatusInformationDetailsChars) {
                        tooltip = statusInfoDetails;
                        statusInfoDetails = statusInfoDetails.substring(0,
                                maxStatusInformationDetailsChars)
                                + Constant.ELLIPSES;
                    } else {
                        tooltip = lastUpdateString + Constant.COLON
                                + Constant.SPACE + host.getLastCheckTime();
                    }
                } else {
                    statusInfoDetails = UNAVAILABLE;
                }

                // create hostBean by supplying all the data, and insert into
                // hostList
                HostBean hostBean = new HostBean(host.getHostID(), host
                        .getName(), MonitorStatusUtilities.getEntityStatus(
                        host, NodeType.HOST), duration, statusInfoDetails, host
                        .isAcknowledged(), tooltip);
                hostBean.setTotalCount(totalCount);
                // set service status as well as tool tip for service status
                determineServiceStatus(hostBean, host.getHostID());

                // Set URL
                hostBean.setUrl(NodeURLBuilder.buildNodeURL(NodeType.HOST,
                        Integer.valueOf(host.getHostID()), host.getName()));

                // Set the acknowledge status property - e.g. Not to
                // acknowledge if in OK/PENDING state.
                hostBean.setAcknowledgeStatus(true);

                String monitorStatus = host.getMonitorStatus();

                if (monitorStatus != null) {
                    if ((monitorStatus.equalsIgnoreCase(Constant.UP))
                            || (monitorStatus
                                    .equalsIgnoreCase(Constant.PENDING))) {
                        hostBean.setAcknowledgeStatus(false);
                    }
                } else {
                    LOGGER
                            .debug("queryForHostsByFilter(): Monitor Status for host "
                                    + host.getName() + " is null");
                }
                if (showNonAcknowledgedHosts && host.isAcknowledged()) {
                    // ingnore add
                }
                else {
                	hostList.add(hostBean);
                }
            }
        }
        return hostList;
    }

    /**
     * Queries events for entire network
     * 
     * @param startIndex
     * @param sort
     * 
     * @return List
     */
    public List<HostBean> queryForHosts(int startIndex, Sort sort) {
        List<HostBean> hostList = new ArrayList<HostBean>();
        WSFoundationCollection collection = null;

        String sortColumn = sort.getSortItem(0).getPropertyName();
        SortItem sortItem = new SortItem();
        sortItem.setPropertyName(sortColumn);
        sortItem.setSortAscending(sort.getSortItem(0).isSortAscending());
        sort.setSortItem(0, sortItem);
        SortCriteria sortCriteria = null;
        if (sort.getSortItem(0).isSortAscending()) {
            sortCriteria = new SortCriteria("ascending", "name");

        } else {
            sortCriteria = new SortCriteria("descending", "name");
        }
        try {

            collection = foundFacade.getHosts(HostQueryType.ALL, null, null,
                    startIndex, tableRows, sortCriteria);
        } catch (GWPortalGenericException ge) {
            setMessage(true);
            setErrorMessage(ge.getMessage());
            setError(true);
        }
        if (collection != null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER
                        .debug("Got "
                                + startIndex
                                + " to "
                                + (startIndex + tableRows)
                                + "rows from getHostsbyCriteria() in queryForHostsByFilter()");
            }

            Host[] simpleHosts = collection.getHost();
            if (simpleHosts == null) {
                return hostList;
            }

            // totalCount - count(acknowledged hosts)
            int totalCount = collection.getTotalCount();
            if (showNonAcknowledgedHosts) {
                for (Host host : simpleHosts) {
                    boolean isAcknowledged = false;
                    // Duration
                    PropertyTypeBinding propertyTypeBinding = host
                            .getPropertyTypeBinding();
                    if (propertyTypeBinding != null) {
                        BooleanProperty acknowledgedProperty = propertyTypeBinding
                                .getBooleanProperty("isAcknowledged");
                        if (acknowledgedProperty != null) {
                            isAcknowledged = acknowledgedProperty.isValue();
                            totalCount--;
                        }
                    }
                }
            }
            
            for (Host host : simpleHosts) {
                String duration = Constant.EMPTY_STRING;
                String statusInfoDetails = Constant.EMPTY_STRING;
                String tooltip = null;
                boolean isAcknowledged = false;
                // Duration
                PropertyTypeBinding propertyTypeBinding = host
                        .getPropertyTypeBinding();
                if (propertyTypeBinding != null) {
                    DateProperty lastStateChangeProperty = propertyTypeBinding
                            .getDateProperty("LastStateChange");
                    if (lastStateChangeProperty != null) {
                        duration = DateUtils
                                .computeDuration(lastStateChangeProperty
                                        .getValue());
                    } else {
                        duration = Constant.EMPTY_STRING;
                    }

                    StringProperty lastPluginOutputProperty = propertyTypeBinding
                            .getStringProperty("LastPluginOutput");

                    // Status Information Details
                    if (lastPluginOutputProperty != null) {
                        statusInfoDetails = lastPluginOutputProperty.getValue();
                        if (statusInfoDetails.length() > maxStatusInformationDetailsChars) {
                            tooltip = statusInfoDetails;
                            statusInfoDetails = statusInfoDetails.substring(0,
                                    maxStatusInformationDetailsChars)
                                    + Constant.ELLIPSES;
                        } else {
                            tooltip = lastUpdateString + Constant.COLON
                                    + Constant.SPACE + host.getLastCheckTime();
                        }
                    } else {
                        statusInfoDetails = UNAVAILABLE;
                    }
                    BooleanProperty acknowledgedProperty = propertyTypeBinding
                            .getBooleanProperty("isAcknowledged");
                    if (acknowledgedProperty != null) {
                        isAcknowledged = acknowledgedProperty.isValue();
                    }
                }
                // create hostBean by supplying all the data, and insert into
                // hostList
                HostBean hostBean = new HostBean(host.getHostID(), host
                        .getName(), MonitorStatusUtilities.getEntityStatus(
                        host, NodeType.HOST), duration, statusInfoDetails,
                        isAcknowledged, tooltip);
                hostBean.setTotalCount(totalCount);
                // set service status as well as tool tip for service status
                determineServiceStatus(hostBean, host.getHostID());

                // Set URL
                hostBean.setUrl(NodeURLBuilder.buildNodeURL(NodeType.HOST,
                        Integer.valueOf(host.getHostID()), host.getName()));

                // Set the acknowledge status property - e.g. Not to
                // acknowledge if in OK/PENDING state.
                hostBean.setAcknowledgeStatus(true);

                MonitorStatus monitorStatus = host.getMonitorStatus();
                if (monitorStatus != null) {
                    if (monitorStatus.getName() != null) {
                        if ((monitorStatus.getName()
                                .equalsIgnoreCase(Constant.UP))
                                || (monitorStatus.getName()
                                        .equalsIgnoreCase(Constant.PENDING))) {
                            hostBean.setAcknowledgeStatus(false);
                        }
                    } else {
                        LOGGER.debug("queryForHosts: Monitor Status for host "
                                + host.getName() + " is null");
                    }
                } else {
                    LOGGER.debug("queryForHosts: Monitor Status for host "
                            + host.getName() + " is null");
                }
                if (showNonAcknowledgedHosts && isAcknowledged) {
                    // ingnore add
                }
                else {
                	hostList.add(hostBean);
                }
            }
        }
        return hostList;
    }

    /**
     * decide the aggregate service status of Host by calculating from services
     * status.
     * 
     * Also, it sets tool tip to show when mouse hovers over aggregated status
     * icon
     * 
     * IT uses ReferenceTreeMetaModel's getServicesUnderHost() call to get all
     * the services. TODO: is above sentenced logic correct? Verify.
     * 
     * @param hostId
     * 
     * @param hostId
     */
    private void determineServiceStatus(HostBean hostBean, int hostId) {

        // commenting WEB SERVICE CALL logic and replacing it with calls to
        // ReferenceTreeMetaModel

        // // foundationWSFacade Object to call web services.
        // final IWSFacade foundationWSFacade = new WebServiceFactory()
        // .getWebServiceInstance(WebServiceType.FOUNDATION_WEBSERVICE);
        //
        // ServiceStatus[] servicesUnderHost = foundationWSFacade
        // .getServicesByHostId(hostId);

        int criticalScheduledCount = 0, criticalUnscheduledCount = 0, unknownCount = 0, pendingCount = 0, okCount = 0, warningCount = 0;

        Iterator<Integer> servicesIterator = referenceTreeModel
                .getServicesUnderHost(Integer.valueOf(hostId));

        ArrayList<NetworkMetaEntity> serviceList = new ArrayList<NetworkMetaEntity>();
        while (servicesIterator.hasNext()) {
            NetworkMetaEntity serviceEntity = referenceTreeModel
                    .getServiceById(servicesIterator.next());
            if (serviceEntity != null) {
                serviceList.add(serviceEntity);
                switch (serviceEntity.getStatus()) {
                    case SERVICE_OK:
                        okCount++;
                        break;
                    case SERVICE_PENDING:
                        pendingCount++;
                        break;
                    case SERVICE_UNKNOWN:
                        unknownCount++;
                        break;
                    case SERVICE_WARNING:
                        warningCount++;
                        break;
                    case SERVICE_CRITICAL_SCHEDULED:
                        criticalScheduledCount++;
                        break;
                    case SERVICE_CRITICAL_UNSCHEDULED:
                        criticalUnscheduledCount++;
                        break;
                    default:
                        LOGGER.info("Unknown Service status type found: "
                                + serviceEntity.getStatus());
                }
            }
        }

        // set tool tip header as well as list of services
        StringBuilder tooltip = new StringBuilder();
        if (criticalUnscheduledCount > 0) {
            hostBean
                    .setServiceStatus(NetworkObjectStatusEnum.SERVICE_CRITICAL_UNSCHEDULED);
            for (NetworkMetaEntity entity : serviceList) {
                if (entity.getStatus() == NetworkObjectStatusEnum.SERVICE_CRITICAL_UNSCHEDULED) {
                    tooltip.append(entity.getExtendedName() + BR);
                }
            }
            hostBean.setServiceStatusToolTip(tooltip.toString());
        } else if (criticalScheduledCount > 0) {
            hostBean
                    .setServiceStatus(NetworkObjectStatusEnum.SERVICE_CRITICAL_SCHEDULED);
            for (NetworkMetaEntity entity : serviceList) {
                if (entity.getStatus() == NetworkObjectStatusEnum.SERVICE_CRITICAL_SCHEDULED) {
                    tooltip.append(entity.getExtendedName() + BR);
                }
            }
            hostBean.setServiceStatusToolTip(tooltip.toString());
        } else if (warningCount > 0) {
            hostBean.setServiceStatus(NetworkObjectStatusEnum.SERVICE_WARNING);
            for (NetworkMetaEntity entity : serviceList) {
                if (entity.getStatus() == NetworkObjectStatusEnum.SERVICE_WARNING) {
                    tooltip.append(entity.getExtendedName() + BR);
                }
            }
            hostBean.setServiceStatusToolTip(tooltip.toString());
        } else if (unknownCount > 0) {
            hostBean.setServiceStatus(NetworkObjectStatusEnum.SERVICE_UNKNOWN);
            for (NetworkMetaEntity entity : serviceList) {
                if (entity.getStatus() == NetworkObjectStatusEnum.SERVICE_UNKNOWN) {
                    tooltip.append(entity.getExtendedName() + BR);
                }
            }
            hostBean.setServiceStatusToolTip(tooltip.toString());
        } else if (pendingCount > 0) {
            hostBean.setServiceStatus(NetworkObjectStatusEnum.SERVICE_PENDING);
            for (NetworkMetaEntity entity : serviceList) {
                if (entity.getStatus() == NetworkObjectStatusEnum.SERVICE_PENDING) {
                    tooltip.append(entity.getExtendedName() + BR);
                }
            }
            hostBean.setServiceStatusToolTip(tooltip.toString());
        } else if (okCount > 0) {
            hostBean.setServiceStatus(NetworkObjectStatusEnum.SERVICE_OK);
            for (NetworkMetaEntity entity : serviceList) {
                if (entity.getStatus() == NetworkObjectStatusEnum.SERVICE_OK) {
                    tooltip.append(entity.getExtendedName() + BR);
                }
            }
            hostBean.setServiceStatusToolTip(tooltip.toString());
        } else {

            // else return NO_STATUS
            // LOGGER.warn("Cant determine aggregate entity status for host "
            // + hostId);
            hostBean.setServiceStatus(NetworkObjectStatusEnum.NO_STATUS);
            hostBean.setServiceStatusToolTip(Constant.EMPTY_STRING);
        }
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
        // set sortColumnName over here to default one - "hostName"
        sortColumnName = Constant.HOST_LIST_SORT_COLUMN_NAME;

        page = fetchPage(0, tableRows);
    }

    /**
     * Sets the lastPage.
     * 
     * @param hostListDataPage
     *            the lastPage to set
     */
    public void setLastPage(HostListDataPage hostListDataPage) {
        this.lastPage = hostListDataPage;
    }

    /**
     * Returns the lastPage.
     * 
     * @return the lastPage
     */
    public HostListDataPage getLastPage() {
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
     * Handles error : sets error flag and message.
     * 
     * @param errorMessage
     */
    public void handleError(String errorMessage) {
        setMessage(true);
        setError(true);
        setErrorMessage(errorMessage);
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
     * Sets the showNonUpHosts.
     * 
     * @param showNonUpHosts
     *            the showNonUpHosts to set
     */
    public void setShowNonUpHosts(boolean showNonUpHosts) {
        this.showNonUpHosts = showNonUpHosts;
    }

    /**
     * Sets the showNonUnreachableHosts.
     * 
     * @param showNonUnreachableHosts
     *            the showNonUnreachableHosts to set
     */
    public void setShowNonUnreachableHosts(boolean showNonUnreachableHosts) {
        this.showNonUnreachableHosts = showNonUnreachableHosts;
    }
    
    /**
     * Sets the showNonDownUnScheduledHosts.
     * 
     * @param showNonDownUnScheduledHosts
     *            the showNonDownUnScheduledHosts to set
     */
    public void setShowNonDownUnScheduledHosts(boolean showNonDownUnScheduledHosts) {
        this.showNonDownUnScheduledHosts = showNonDownUnScheduledHosts;
    }
    
    /**
     * Sets the showNonDownScheduledHosts.
     * 
     * @param showNonDownScheduledHosts
     *            the showNonDownScheduledHosts to set
     */
    public void setShowNonDownScheduledHosts(boolean showNonDownScheduledHosts) {
        this.showNonDownScheduledHosts = showNonDownScheduledHosts;
    }

    /**
     * Sets the showNonPendingHosts.
     * 
     * @param showNonPendingHosts
     *            the showNonPendingHosts to set
     */
    public void setShowNonPendingHosts(boolean showNonPendingHosts) {
        this.showNonPendingHosts = showNonPendingHosts;
    }
    
    /**
     * Sets the showNonAcknowledgedHosts.
     * 
     * @param showNonAcknowledgedHosts
     *            the showNonAcknowledgedHosts to set
     */
    public void setShowNonAcknowledgedHosts(boolean showNonAcknowledgedHosts) {
        this.showNonAcknowledgedHosts = showNonAcknowledgedHosts;
    }
    
    /**
     * Returns the showNonUpHosts.
     * 
     * @return the showNonUpHosts
     */
    public boolean isShowNonUpHosts() {
        return showNonUpHosts;
    }
    
    /**
     * Returns the showNonUnreachableHosts.
     * 
     * @return the showNonUnreachableHosts
     */
    public boolean isShowNonUnreachableHosts() {
        return showNonUnreachableHosts;
    }

    /**
     * Returns the showNonDownUnScheduledHosts.
     * 
     * @return the showNonDownUnScheduledHosts
     */
    public boolean isShowNonDownUnScheduledHosts() {
        return showNonDownUnScheduledHosts;
    }
    
    /**
     * Returns the showNonDownScheduledHosts.
     * 
     * @return the showNonDownScheduledHosts
     */
    public boolean isShowNonDownScheduledHosts() {
        return showNonDownScheduledHosts;
    }
    
    /**
     * Returns the showNonPendingHosts.
     * 
     * @return the showNonPendingHosts
     */
    public boolean isShowNonPendingHosts() {
        return showNonPendingHosts;
    }
    
    /**
     * Returns the showNonAcknowledgedHosts.
     * 
     * @return the showNonAcknowledgedHosts
     */
    public boolean isShowNonAcknowledgedHosts() {
        return showNonAcknowledgedHosts;
    }

}
