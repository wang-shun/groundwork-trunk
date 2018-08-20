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
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.model.impl.Category;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.SimpleHost;
import org.groundwork.foundation.ws.model.impl.SimpleServiceStatus;
import org.groundwork.foundation.ws.model.impl.Sort;
import org.groundwork.foundation.ws.model.impl.SortCriteria;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

import com.groundworkopensource.portal.common.ApplicationType;
import com.groundworkopensource.portal.common.PropertyUtils;
import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;
import com.groundworkopensource.portal.common.ws.IWSFacade;
import com.groundworkopensource.portal.common.ws.impl.FoundationWSFacade;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.DateUtils;
import com.groundworkopensource.portal.statusviewer.common.NetworkObjectStatusEnum;
import com.groundworkopensource.portal.statusviewer.common.NodeType;
import com.groundworkopensource.portal.statusviewer.common.NodeURLBuilder;
import com.groundworkopensource.portal.statusviewer.common.StatisticsModelPopUpDataPage;
import com.groundworkopensource.portal.statusviewer.common.StatisticsPagedListDataModel;

/**
 * @author manish_kjain
 * 
 */
public class StatisticsModelPopUpListBean extends StatisticsPagedListDataModel {

    /**
     * Logger
     */
    private static final Logger LOGGER = Logger
            .getLogger(StatisticsModelPopUpListBean.class.getName());

    /**
     * IWSFacade instance variable.
     */
    private IWSFacade foundFacade = null;
    /**
     * Last start Row
     */
    private int lastStartRow = -1;
    /**
     * initial last page is null.
     */
    private StatisticsModelPopUpDataPage lastPage = null;
    /**
     * Default sort column name
     */
    private String sortColumnName = "serviceDescription";

    /**
     * number of rows in table
     */
    private int tableRows;

    /**
     * current selected final filter
     */
    private Filter filter;

    /**
     * default sorting order.
     */
    private boolean ascending = true;

    /**
     * current model pop status of portlet.
     */
    private String currentPopupStatus = null;
    /**
     * Sorting image arrow
     */
    private String sortImgUrl = Constant.IMAGES_SORT_ARROW_UP_GIF;
    /**
     * nodeType
     */
    private NodeType nodeType;

    /**
     * date time pattern
     */
    private String dateTimePattern;

    /**
     * constructor
     * 
     * @param pageSize
     * @param appliedFilter
     * @param popupStatus
     * @param type
     * @param sortColName
     */
    public StatisticsModelPopUpListBean(int pageSize, Filter appliedFilter,
            String popupStatus, NodeType type, String sortColName) {
        super(pageSize);
        // setting number of row in data table
        setTableRows(pageSize);
        // current applied filter
        filter = appliedFilter;
        foundFacade = new FoundationWSFacade();
        // current model pop up status
        setCurrentPopupStatus(popupStatus);
        this.setNodeType(type);
        this.setSortColumnName(sortColName);
        try {
            dateTimePattern = PropertyUtils.getProperty(
                    ApplicationType.STATUS_VIEWER,
                    Constant.STATUS_VIEWER_DATETIME_PATTERN);
        } catch (Exception e) {
            // Ignore exception
            dateTimePattern = Constant.EVENT_DATETIME_PATTERN;
        }

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
     * Sets the lastStartRow.
     * 
     * @param lastStartRow
     *            the lastStartRow to set
     */
    public void setLastStartRow(int lastStartRow) {
        this.lastStartRow = lastStartRow;
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
     * Sets the lastPage.
     * 
     * @param lastPage
     *            the lastPage to set
     */
    public void setLastPage(StatisticsModelPopUpDataPage lastPage) {
        this.lastPage = lastPage;
    }

    /**
     * Returns the lastPage.
     * 
     * @return the lastPage
     */
    public StatisticsModelPopUpDataPage getLastPage() {
        return lastPage;
    }

    /**
     * Sets the filter.
     * 
     * @param filter
     *            the filter to set
     */
    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    /**
     * Returns the filter.
     * 
     * @return the filter
     */
    public Filter getFilter() {
        return filter;
    }

    /**
     * (non-Javadoc)
     * 
     * @see com.groundworkopensource.portal.statusviewer.common.StatisticsPagedListDataModel#fetchPage(int,
     *      int)
     */
    @Override
    public StatisticsModelPopUpDataPage fetchPage(int startRow, int pageSize) {
        return getDataPage(startRow, pageSize);
    }

    /**
     * Gets the specified data page
     * 
     * @param startRow
     * @param pageSize
     * @return HostListDataPage
     */
    private StatisticsModelPopUpDataPage getDataPage(int startRow, int pageSize) {
        if (lastPage == null || startRow != lastStartRow) {

            Sort sort = new Sort(isAscending(), sortColumnName);

            // query services
            List<ModelPopUpDataBean> popUpDataList = Collections
                    .synchronizedList(new ArrayList<ModelPopUpDataBean>());
            try {
                popUpDataList = getPopUpDataList(filter, startRow, sort, this
                        .getTableRows(), currentPopupStatus);
            } catch (GWPortalException e) {
                LOGGER.error(e.getMessage());
            } catch (WSDataUnavailableException e) {
                LOGGER.error(e.getMessage());
            }
            // end if
            int dataSetSize = 0;
            if (popUpDataList.size() > 0) {
                dataSetSize = popUpDataList.get(0).getTotalCount();

            } // end if
            lastStartRow = startRow;
            setLastPage(new StatisticsModelPopUpDataPage(dataSetSize, startRow,
                    popUpDataList));

        }
        return lastPage;
    }

    /**
     * get model popup data list
     * 
     * @param filter
     * @param startRow
     * @param sort
     * @param pageSize
     * @param serviceCurrentStatus
     * @return List < ModelPopUpDataBean >
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    private List<ModelPopUpDataBean> getPopUpDataList(Filter filter,
            int startRow, Sort sort, int pageSize, String currentStatus)
            throws GWPortalException, WSDataUnavailableException {
        List<ModelPopUpDataBean> modelPopUpDataList = Collections
                .synchronizedList(new ArrayList<ModelPopUpDataBean>());
        switch (nodeType) {
            case HOST:
                modelPopUpDataList = this.getHostPopUpDataList(filter,
                        startRow, sort, pageSize, currentStatus);
                break;
            case HOST_GROUP:

                break;
            case SERVICE:
                modelPopUpDataList = this.getServicePopUpDataList(filter,
                        startRow, sort, pageSize, currentStatus);
                break;
            case SERVICE_GROUP:
                // modelPopUpDataList = this.getServiceGroupFilteredDataList(
                // filter, startRow, sort, pageSize);
                break;
            default:
                break;
        }
        return modelPopUpDataList;
    }

    /**
     * returns service Model window data list.
     * 
     * @param filter
     * @return List
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */

    private List<ModelPopUpDataBean> getServicePopUpDataList(Filter filter,
            int startRow, Sort sort, int pageSize, String serviceCurrentStatus)
            throws GWPortalException, WSDataUnavailableException {
        List<ModelPopUpDataBean> serviceList = Collections
                .synchronizedList(new ArrayList<ModelPopUpDataBean>());
        WSFoundationCollection simpleServiceCollection = foundFacade
                .getSimpleServiceCollectionbyCriteria(filter, sort, startRow,
                        pageSize);
        if (null != simpleServiceCollection) {
            SimpleServiceStatus[] serviceArr = simpleServiceCollection
                    .getSimpleService();

            if (serviceArr != null) {
                for (int i = 0; i < serviceArr.length; i++) {
                    ModelPopUpDataBean modelpopupbean = new ModelPopUpDataBean();
                    modelpopupbean.setTotalCount(simpleServiceCollection
                            .getTotalCount());
                    if (serviceArr[i] != null) {

                        if (serviceArr[i].isAcknowledged()) {
                            modelpopupbean.setAcknowledged(Constant.YES);
                        } else {
                            modelpopupbean.setAcknowledged(Constant.NO);
                        }
                        modelpopupbean.setName(serviceArr[i].getDescription());
                        modelpopupbean.setSubPageURL(NodeURLBuilder
                                .buildNodeURL(NodeType.SERVICE, serviceArr[i]
                                        .getServiceStatusID(), serviceArr[i]
                                        .getDescription()));
                        modelpopupbean.setParentName(serviceArr[i]
                                .getHostName());
                        // TODO :
                        if (!NetworkObjectStatusEnum.SERVICE_PENDING
                                .getStatus().equalsIgnoreCase(
                                        serviceCurrentStatus)) {
                            Date lastCheckTime = serviceArr[i]
                                    .getLastCheckTime();

                            // check for lastchecktime if null then display N/A
                            // on
                            // UI
                            if (null == lastCheckTime) {
                                modelpopupbean
                                        .setDatetime(Constant.NOT_AVAILABLE_STRING);

                            } else {
                                try {
                                    modelpopupbean.setDatetime(DateUtils
                                            .format(lastCheckTime,
                                                    dateTimePattern));
                                } catch (Exception e) {
                                    modelpopupbean
                                            .setDatetime(DateUtils
                                                    .format(
                                                            lastCheckTime,
                                                            Constant.DEFAULT_DATETIME_PATTERN));
                                }
                            }

                        }
                        // Setting service parent name(Host)

                        String hostName = serviceArr[i].getHostName();
                        if (hostName != null) {
                            modelpopupbean.setParentName(hostName);
                        }
                        // Setting service parent sub page URL
                        modelpopupbean.setParentPageURL(NodeURLBuilder
                                .buildNodeURL(NodeType.HOST, serviceArr[i]
                                        .getHostId(), hostName));

                        serviceList.add(modelpopupbean);
                    }
                }

            }
        }
        return serviceList;

    }

    /**
     * 
     * Method get the data from web services and set in to host statistics bean.
     * 
     *@param filter
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    private List<ModelPopUpDataBean> getHostPopUpDataList(Filter filter,
            int startRow, Sort sort, int pageSize, String hostPopUpStatus)
            throws WSDataUnavailableException, GWPortalException {
        WSFoundationCollection collection = foundFacade
                .getSimpleHostsbyCriteria(filter, sort, startRow, pageSize,
                        false);
        SimpleHost[] hostArr = collection.getSimpleHost();
        List<ModelPopUpDataBean> hostList = Collections
                .synchronizedList(new ArrayList<ModelPopUpDataBean>());

        if (hostArr != null) {
            for (int i = 0; i < hostArr.length; i++) {
                ModelPopUpDataBean modelpopupdatabean = new ModelPopUpDataBean();
                modelpopupdatabean.setTotalCount(collection.getTotalCount());
                if (hostArr[i] != null) {

                    if (hostArr[i].isAcknowledged()) {
                        modelpopupdatabean.setAcknowledged(Constant.YES);
                    } else {
                        modelpopupdatabean.setAcknowledged(Constant.NO);
                    }
                    // end if

                    modelpopupdatabean.setName(hostArr[i].getName());
                    modelpopupdatabean.setSubPageURL(NodeURLBuilder
                            .buildNodeURL(NodeType.HOST,
                                    hostArr[i].getHostID(), hostArr[i]
                                            .getName()));
                    // TODO : date format pattern string should be come from
                    // application property.
                    // check current pop status is not in pending state because
                    // LastCheckTime is null in pending monitor status
                    if (!NetworkObjectStatusEnum.HOST_PENDING.getStatus()
                            .equalsIgnoreCase(hostPopUpStatus)) {
                        Date lastCheckTime = hostArr[i].getLastCheckTime();

                        // check for lastCheckTime if null then display N/A on
                        // UI
                        if (null == lastCheckTime) {
                            modelpopupdatabean
                                    .setDatetime(Constant.NOT_AVAILABLE_STRING);
                        } else {
                            try {
                                modelpopupdatabean
                                        .setDatetime(DateUtils.format(
                                                lastCheckTime, dateTimePattern));
                            } catch (Exception e) {
                                modelpopupdatabean
                                        .setDatetime(DateUtils
                                                .format(
                                                        lastCheckTime,
                                                        Constant.DEFAULT_DATETIME_PATTERN));
                            }
                        }
                    }
                    hostList.add(modelpopupdatabean);
                }
            }

        }
        return hostList;

    }

    /**
     * returns service group filtered data list
     * 
     * @param filter
     * @return List < ModelPopUpDataBean >
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    @SuppressWarnings("unused")
    private List<ModelPopUpDataBean> getServiceGroupFilteredDataList(
            Filter filter, int startRow, Sort sort, int pageSize)
            throws WSDataUnavailableException, GWPortalException {
        List<ModelPopUpDataBean> serviceGroupList = Collections
                .synchronizedList(new ArrayList<ModelPopUpDataBean>());
        Category[] category = null;
        if (filter == null) {
            return serviceGroupList;
        }
        SortCriteria sortCriteria = null;
        // creating sort
        if (isAscending()) {
            sortCriteria = new SortCriteria("ascending", sortColumnName);
        } else {
            sortCriteria = new SortCriteria("descending", sortColumnName);
        }
        // getting service group
        WSFoundationCollection categoryCollection = foundFacade
                .getCategoryCollectionbyCriteria(filter, startRow, pageSize,
                        sortCriteria, false, false);
        if (categoryCollection != null) {
            category = categoryCollection.getCategory();

            if (category != null) {
                for (int i = 0; i < category.length; i++) {
                    ModelPopUpDataBean modelpopupdatabean = new ModelPopUpDataBean();
                    modelpopupdatabean.setTotalCount(categoryCollection
                            .getTotalCount());
                    modelpopupdatabean.setName(category[i].getName());
                    modelpopupdatabean.setSubPageURL(NodeURLBuilder
                            .buildNodeURL(NodeType.SERVICE_GROUP, category[i]
                                    .getCategoryId(), category[i].getName()));
                    serviceGroupList.add(modelpopupdatabean);
                }
            }
        }
        return serviceGroupList;
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
     * Sets the currentPopupStatus.
     * 
     * @param currentPopupStatus
     *            the currentPopupStatus to set
     */
    public void setCurrentPopupStatus(String currentPopupStatus) {
        this.currentPopupStatus = currentPopupStatus;
    }

    /**
     * Returns the currentPopupStatus.
     * 
     * @return the currentPopupStatus
     */
    public String getCurrentPopupStatus() {
        return currentPopupStatus;
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
        if (ascending) {
            sortImgUrl = Constant.IMAGES_SORT_ARROW_UP_GIF;
        } else {
            sortImgUrl = Constant.IMAGES_SORT_ARROW_DOWN_GIF;
        }
        lastPage = null;
        page = fetchPage(0, Constant.TEN);
    }

    /**
     * Sets the sortImgUrl.
     * 
     * @param sortImgUrl
     *            the sortImgUrl to set
     */
    public void setSortImgUrl(String sortImgUrl) {
        this.sortImgUrl = sortImgUrl;
    }

    /**
     * Returns the sortImgUrl.
     * 
     * @return the sortImgUrl
     */
    public String getSortImgUrl() {
        return sortImgUrl;
    }

    /**
     * Sets the nodeType.
     * 
     * @param nodeType
     *            the nodeType to set
     */
    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    /**
     * Returns the nodeType.
     * 
     * @return the nodeType
     */
    public NodeType getNodeType() {
        return nodeType;
    }

    // /**
    // * Sets the dataPaginator.
    // *
    // * @param dataPaginator
    // * the dataPaginator to set
    // */
    // public void setDataPaginator(DataPaginator dataPaginator) {
    // this.dataPaginator = dataPaginator;
    // }
    //
    // /**
    // * Returns the dataPaginator.
    // *
    // * @return the dataPaginator
    // */
    // public DataPaginator getDataPaginator() {
    // return dataPaginator;
    // }
}
