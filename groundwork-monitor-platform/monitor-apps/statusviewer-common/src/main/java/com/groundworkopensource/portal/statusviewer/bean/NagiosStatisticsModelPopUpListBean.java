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
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.SimpleHost;
import org.groundwork.foundation.ws.model.impl.SimpleServiceStatus;
import org.groundwork.foundation.ws.model.impl.Sort;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

import com.groundworkopensource.portal.common.ApplicationType;
import com.groundworkopensource.portal.common.PropertyUtils;
import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;
import com.groundworkopensource.portal.common.ws.IWSFacade;
import com.groundworkopensource.portal.common.ws.impl.FoundationWSFacade;
import com.groundworkopensource.portal.common.ws.impl.HostWSFacade;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.DateUtils;
import com.groundworkopensource.portal.statusviewer.common.MonitorStatusUtilities;
import com.groundworkopensource.portal.statusviewer.common.NetworkObjectStatusEnum;
import com.groundworkopensource.portal.statusviewer.common.NodeType;
import com.groundworkopensource.portal.statusviewer.common.NodeURLBuilder;
import com.groundworkopensource.portal.statusviewer.common.StatisticsModelPopUpDataPage;
import com.groundworkopensource.portal.statusviewer.common.StatisticsPagedListDataModel;

/**
 * @author manish_kjain
 * 
 */
public class NagiosStatisticsModelPopUpListBean extends
        StatisticsPagedListDataModel {

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
     * Sorting image arrow
     */
    private String sortImgUrl = Constant.IMAGES_SORT_ARROW_UP_GIF;
    /**
     * 
     */
    private String linkClicked;
    /**
     * date time pattern
     */
    private String dateTimePattern;

    /**
     * Constant for N/A
     */
    private static final String NOT_AVAILABLE = "N/A";

    /**
     * Logger
     */
    private static final Logger LOGGER = Logger
            .getLogger(NagiosStatisticsModelPopUpListBean.class.getName());

    /**
     * constructor
     * 
     * @param pageSize
     * @param filter
     * @param sortColName
     * @param linkClicked
     * 
     */
    public NagiosStatisticsModelPopUpListBean(int pageSize, Filter filter,
            String sortColName, String linkClicked) {
        super(pageSize);
        this.tableRows = pageSize;
        this.filter = filter;
        this.sortColumnName = sortColName;
        this.linkClicked = linkClicked;
        foundFacade = new FoundationWSFacade();
        try {
            dateTimePattern = PropertyUtils.getProperty(
                    ApplicationType.STATUS_VIEWER,
                    Constant.STATUS_VIEWER_DATETIME_PATTERN);
        } catch (Exception e) {
            // Ignore exception
            dateTimePattern = Constant.DEFAULT_DATETIME_PATTERN;
        }

    }

    /**
     * (non-Javadoc)
     * 
     * @see com.groundworkopensource.portal.statusviewer.common.
     *      StatisticsPagedListDataModel#fetchPage(int, int)
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
                        .getTableRows());
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
            int startRow, Sort sort, int pageSize) throws GWPortalException,
            WSDataUnavailableException {
        List<ModelPopUpDataBean> modelPopUpDataList = Collections
                .synchronizedList(new ArrayList<ModelPopUpDataBean>());
        if (Constant.SERVICES.equalsIgnoreCase(linkClicked)) {
            return getDisabledServices(filter, sort, startRow, pageSize);
        } else if (Constant.HOSTS.equalsIgnoreCase(linkClicked)) {
            return getDisabledHosts(filter, sort, startRow, pageSize);
        }
        return modelPopUpDataList;
    }

    /**
     * Retrieves list of disabled services using getServicesbyCriteria() ws
     * call.
     * 
     * @param filter
     * @param sort
     * @param startRow
     * @param pageSize
     * @return modelPopupDataList
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    public List<ModelPopUpDataBean> getDisabledServices(Filter filter,
            Sort sort, int startRow, int pageSize) throws GWPortalException,
            WSDataUnavailableException {
        List<ModelPopUpDataBean> modelPopupDataList = Collections
                .synchronizedList(new ArrayList<ModelPopUpDataBean>());
        // // Create instance of serviceWSFacade
        // ServiceWSFacade serviceWSFacade = new ServiceWSFacade();

        // Call getServicesbyCriteria() API
        // SimpleServiceStatus[] simpleServicesArray = serviceWSFacade
        // .getSimpleServicesbyCriteria(filter, sort, startRow, pageSize);
        WSFoundationCollection simpleServiceCollection = foundFacade
                .getSimpleServiceCollectionbyCriteria(filter, sort, startRow,
                        pageSize);

        if (null != simpleServiceCollection) {
            SimpleServiceStatus[] simpleServicesArray = simpleServiceCollection
                    .getSimpleService();

            if (simpleServicesArray != null) {
                /*
                 * Iterate over the Services array and populate the
                 * ModelPopUpDataBean list
                 */
                for (SimpleServiceStatus simpleService : simpleServicesArray) {
                    ModelPopUpDataBean modelPopUpDataBean = new ModelPopUpDataBean();
                    modelPopUpDataBean.setTotalCount(simpleServiceCollection
                            .getTotalCount());
                    if (simpleService != null) {
                        modelPopUpDataBean.setName(simpleService
                                .getDescription());
                        // Set the host URL, so that user can navigate to the
                        // service page from modal-popup of disabled Service.
                        modelPopUpDataBean.setSubPageURL(NodeURLBuilder
                                .buildNodeURL(NodeType.SERVICE, simpleService
                                        .getServiceStatusID(), simpleService
                                        .getDescription()));
                        Date lastCheckTime = simpleService.getLastCheckTime();
                        if (lastCheckTime == null) {
                            modelPopUpDataBean.setDatetime(NOT_AVAILABLE);
                        } else {
                            try {
                                modelPopUpDataBean
                                        .setDatetime(DateUtils.format(
                                                lastCheckTime, dateTimePattern));
                            } catch (Exception e) {
                                modelPopUpDataBean
                                        .setDatetime(DateUtils
                                                .format(
                                                        lastCheckTime,
                                                        Constant.DEFAULT_DATETIME_PATTERN));
                            }
                        }
                        /*
                         * To get the icon to be displayed for the
                         * service-status.
                         */
                        NetworkObjectStatusEnum serviceStatus = MonitorStatusUtilities
                                .getEntityStatus(simpleService,
                                        NodeType.SERVICE);
                        if (serviceStatus != null) {
                            modelPopUpDataBean.setIconPath(serviceStatus
                                    .getIconPath());
                        }
                        String parentName = simpleService.getHostName();
                        int parentId = simpleService.getHostId();
                        if (parentName != null) {
                            modelPopUpDataBean.setParentName(parentName);
                            modelPopUpDataBean.setParentPageURL(NodeURLBuilder
                                    .buildNodeURL(NodeType.HOST, parentId,
                                            parentName));
                        }
                        modelPopupDataList.add(modelPopUpDataBean);
                    }
                }
            }
        }
        return modelPopupDataList;
    }

    /**
     * Retrieves list of disabled hosts using getHostsbyCriteria() ws call.
     * 
     * @param filter
     * @param sort
     * @param startRow
     * @param pageSize
     * @return modelPopupDataList
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    public List<ModelPopUpDataBean> getDisabledHosts(Filter filter, Sort sort,
            int startRow, int pageSize) throws GWPortalException,
            WSDataUnavailableException {
        List<ModelPopUpDataBean> modelPopupDataList = Collections
                .synchronizedList(new ArrayList<ModelPopUpDataBean>());
        // Create instance of HostWSFacade
        HostWSFacade hostWSFacade = new HostWSFacade();
        // Call getSimpleHostsbyCriteria() API
        WSFoundationCollection wsFoundationCollection = hostWSFacade
                .getSimpleHostsbyCriteria(filter, sort, startRow, pageSize,
                        false);
        if ((wsFoundationCollection == null)
                || (wsFoundationCollection.getSimpleHost() == null)
                || (wsFoundationCollection.getSimpleHost().length == 0)) {
            throw new GWPortalException(
                    "getSimpleHostsbyCriteria() returned empty results");
        }
        SimpleHost[] simpleHostArray = wsFoundationCollection.getSimpleHost();

        if (simpleHostArray != null) {
            /*
             * Iterate over the Host array and populate the ModelPopUpDataBean
             * list
             */
            for (SimpleHost simpleHost : simpleHostArray) {
                ModelPopUpDataBean modelPopUpDataBean = new ModelPopUpDataBean();
                modelPopUpDataBean.setTotalCount(wsFoundationCollection
                        .getTotalCount());
                if (simpleHost != null) {
                    modelPopUpDataBean.setName(simpleHost.getName());
                    // Set the host URL, so that user can navigate to the host
                    // page from modal-popup of disabled Hosts.
                    modelPopUpDataBean.setSubPageURL(NodeURLBuilder
                            .buildNodeURL(NodeType.HOST,
                                    simpleHost.getHostID(), simpleHost
                                            .getName()));
                    Date lastCheckTime = simpleHost.getLastCheckTime();
                    if (lastCheckTime == null) {
                        modelPopUpDataBean.setDatetime(NOT_AVAILABLE);
                    } else {
                        try {
                            modelPopUpDataBean.setDatetime(DateUtils.format(
                                    lastCheckTime, dateTimePattern));
                        } catch (Exception e) {
                            modelPopUpDataBean.setDatetime(DateUtils.format(
                                    lastCheckTime,
                                    Constant.DEFAULT_DATETIME_PATTERN));
                        }
                    }
                    /* To get the icon to be displayed for the host-status. */
                    NetworkObjectStatusEnum hostStatus = MonitorStatusUtilities
                            .getEntityStatus(simpleHost, NodeType.HOST);
                    if (hostStatus != null) {
                        modelPopUpDataBean
                                .setIconPath(hostStatus.getIconPath());
                    }
                    modelPopupDataList.add(modelPopUpDataBean);
                }
            }
        }
        return modelPopupDataList;
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
     * Sets the linkClicked.
     * 
     * @param linkClicked
     *            the linkClicked to set
     */
    public void setLinkClicked(String linkClicked) {
        this.linkClicked = linkClicked;
    }

    /**
     * Returns the linkClicked.
     * 
     * @return the linkClicked
     */
    public String getLinkClicked() {
        return linkClicked;
    }

    /**
     * Sets the dateTimePattern.
     * 
     * @param dateTimePattern
     *            the dateTimePattern to set
     */
    public void setDateTimePattern(String dateTimePattern) {
        this.dateTimePattern = dateTimePattern;
    }

    /**
     * Returns the dateTimePattern.
     * 
     * @return the dateTimePattern
     */
    public String getDateTimePattern() {
        return dateTimePattern;
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
     * Sets the lastPage.
     * 
     * @param lastPage
     *            the lastPage to set
     */
    public void setLastPage(StatisticsModelPopUpDataPage lastPage) {
        this.lastPage = lastPage;
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
        page = fetchPage(0, this.tableRows);
    }
}
