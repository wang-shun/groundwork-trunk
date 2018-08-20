package com.groundworkopensource.portal.statusviewer.handler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.model.impl.Category;
import org.groundwork.foundation.ws.model.impl.Host;
import org.groundwork.foundation.ws.model.impl.HostGroup;
import org.groundwork.foundation.ws.model.impl.ServiceStatus;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

import com.groundworkopensource.portal.common.ApplicationType;
import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.common.PropertyUtils;
import com.groundworkopensource.portal.common.ResourceUtils;
import com.groundworkopensource.portal.common.exception.GWPortalGenericException;
import com.groundworkopensource.portal.common.ws.IWSFacade;
import com.groundworkopensource.portal.common.ws.WebServiceFactory;
import com.groundworkopensource.portal.common.ws.WebServiceFactory.WebServiceType;
import com.groundworkopensource.portal.statusviewer.bean.UserExtendedRoleBean;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.NetworkMetaEntity;
import com.groundworkopensource.portal.statusviewer.common.NetworkMetaEntityComparator;
import com.groundworkopensource.portal.statusviewer.common.PortletUtils;
import com.groundworkopensource.portal.statusviewer.common.SortTypeEnum;

/**
 * Class that handles Search functionality in Search tab in network tree portlet
 * 
 * @author nitin_jadhav
 * 
 */
public class NetworkSearchHandler implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 2927100473840528588L;

    /**
     * Error variable to decide whether or not to show error message on UI
     */
    private boolean error = false;

    /**
     * Error message to show error on UI
     */
    private String errorMessage = "";

    /**
     * get max search results from application specific properties file
     */
    private int maxSearchResultCount;

    /**
     * DEFAULT search result count
     */
    private static final int DEFAULT_SEARCH_RESULT_COUNT = 100;

    /**
     * Date format to be used in SimpleDateFormat Class. take from properties
     * file
     */
    private String dateFormatString = "";
    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger
            .getLogger(NetworkSearchHandler.class.getName());

    /**
     * EMPTY STRING
     */
    private static final String EMPTY_STRING = "";

    /**
     * SelectItem list for sorting options such as HOST and HOST_GROUP.
     */
    private final ArrayList<SelectItem> sortingOptions = new ArrayList<SelectItem>();

    /**
     * Currently selected sorting option on search screen
     */
    private String selectedSortOption;

    /**
     * foundationWSFacade Object to call web services.
     */

    private final IWSFacade foundationWSFacade = new WebServiceFactory()
            .getWebServiceInstance(WebServiceType.FOUNDATION_WEBSERVICE);

    /**
     * search query, bound to search box on UI
     */
    private String searchQuery = "";

    /**
     * list containing Result
     */
    private final List<NetworkMetaEntity> searchResultList = new ArrayList<NetworkMetaEntity>();

    /**
     * Number of actual results from service
     */
    private int searchResultCount = 0;

    /**
     * Is this first search? This field is used to show the message about empty
     * search box and no results found scenarios.
     */
    private boolean firstSearch = true;

    /**
     * Comparator for sorting based on selected option
     */
    private final NetworkMetaEntityComparator comparator = new NetworkMetaEntityComparator();

    /**
     * determines if the warning popup should be visible or not.
     */
    private boolean popupVisible = false;

    /**
     * this warning will be displayed on popup if search query returns excessive
     * results (this is first half)
     */
    private final String searchResultsWarningProlog = ResourceUtils
            .getLocalizedMessage("com_groundwork_portal_statusviewer_warningResultsExceedsMaxCount_prolog");

    /**
     * this warning will be displayed on pop up if search query returns
     * excessive results (this is second half)
     */
    private final String searchResultsWarningEpilog = ResourceUtils
            .getLocalizedMessage("com_groundwork_portal_statusviewer_warningResultsExceedsMaxCount_epilog");

    /**
     * UserExtendedRoleBean
     */
    private UserExtendedRoleBean userExtendedRoleBean;

    /**
     * constructor
     */
    public NetworkSearchHandler() {
        // set initial values from property files
        setValuesFromProperties();
        // Insert sorting options into sortSelectOptions
        insertSortingOptions();
        // get the userExtendedRoleBean managed instance
        userExtendedRoleBean = PortletUtils.getUserExtendedRoleBean();
    }

    /**
     * get maxSearchResultCount and dateFormatProperty from application property
     * files.
     */
    private void setValuesFromProperties() {

        String countProperty = PropertyUtils.getProperty(
                ApplicationType.STATUS_VIEWER,
                "portal.statusviewer.search.maxSearchResultCount");
        try {
            if (countProperty != null) {
                setMaxSearchResultCount(Integer.parseInt(countProperty));
                LOGGER.debug("Got maxSearchResultCount from properties file: "
                        + getMaxSearchResultCount());
            }
        } catch (NumberFormatException e) {
            // error! use default value
            setMaxSearchResultCount(DEFAULT_SEARCH_RESULT_COUNT);
        }
        String dateFormatProperty = PropertyUtils.getProperty(
                ApplicationType.STATUS_VIEWER,
                "portal.statusviewer.dateFormatString");
        if (dateFormatProperty != null && dateFormatProperty != "") {
            dateFormatString = dateFormatProperty;
            LOGGER.debug("Got dateFormatString from properties file: "
                    + dateFormatProperty);
        }
    }

    /**
     * Insert sorting options into list, so the user can choose one of them on
     * UI.
     */
    private void insertSortingOptions() {
        getSortingOptions().add(
        // This option will have alphabetic sequence after sorting.m
                new SelectItem(SortTypeEnum.ALPHABETIC.getDisplayName(),
                        SortTypeEnum.ALPHABETIC.getDisplayName()));

        // This option will have following sequence after sorting:
        // HOST > HOSTGROUP > SERVICE > SERVICEGROUP
        getSortingOptions().add(
                new SelectItem(SortTypeEnum.HOST_HOSTGROUP_SERVICE_SERVICEGROUP
                        .getDisplayName(),
                        SortTypeEnum.HOST_HOSTGROUP_SERVICE_SERVICEGROUP
                                .getDisplayName()));
        // This option will have following sequence after sorting:
        // HOSTGROUP > HOST > SERVICEGROUP > SERVICE
        getSortingOptions().add(
                new SelectItem(SortTypeEnum.HOSTGROUP_HOST_SERVICEGROUP_SERVICE
                        .getDisplayName(),
                        SortTypeEnum.HOSTGROUP_HOST_SERVICEGROUP_SERVICE
                                .getDisplayName()));
        // This option will have following sequence after sorting:
        // SERVICE > SERVICEGROUP > HOST > HOSTGROUP
        getSortingOptions().add(
                new SelectItem(SortTypeEnum.SERVICE_SERVICEGROUP_HOST_HOSTGROUP
                        .getDisplayName(),
                        SortTypeEnum.SERVICE_SERVICEGROUP_HOST_HOSTGROUP
                                .getDisplayName()));

        // This option will have following sequence after sorting:
        // SERVICEGROUP > SERVICE > HOSTGROUP >HOST
        getSortingOptions().add(
                new SelectItem(SortTypeEnum.SERVICEGROUP_SERVICE_HOSTGROUP_HOST
                        .getDisplayName(),
                        SortTypeEnum.SERVICEGROUP_SERVICE_HOSTGROUP_HOST
                                .getDisplayName()));
    }

    /**
     * Search for query with web service call.
     * 
     * @param event
     */
    public void search(ActionEvent event) {
        this.searchEntities();

    }

    /**
     * Search for query with web service call.
     */
    private void searchEntities() {
        WSFoundationCollection resultCollection;
        searchResultList.clear();
        searchResultCount = 0;
        if (searchQuery == null || searchQuery.equals(EMPTY_STRING)) {
            LOGGER.debug("Empty input text found.");
            if (firstSearch) {
                firstSearch = false;
            }
            return;
        }
        // Web service call. We will send max+1 search results count as second
        // parameter. so, it can return maximum max+1 results.
        try {
            // TODO remove null check once user extended role logic are in
            // place.
            String serviceGroupListString = userExtendedRoleBean
                    .getServiceGroupListString();
            String hostGroupListString = userExtendedRoleBean
                    .getHostGroupListString();
            if (serviceGroupListString == null || hostGroupListString == null) {
                serviceGroupListString = Constant.EMPTY_STRING;
                hostGroupListString = Constant.EMPTY_STRING;
            }
            resultCollection = foundationWSFacade.searchEntity(searchQuery,
                    getMaxSearchResultCount() + 1, serviceGroupListString,
                    hostGroupListString);
        } catch (GWPortalGenericException e) {
            LOGGER
                    .error("error occured while accessing search Web services(WSCommon).");
            setErrorMessage(e.getMessage());
            setError(true);
            return;
        }

        if (resultCollection == null || resultCollection.getTotalCount() == 0) {
            LOGGER.debug("No result found for query \"" + searchQuery + "\"");
            // add message to UI
            if (firstSearch) {
                firstSearch = false;
            }
            return;
        }
        ReferenceTreeMetaModel referenceTreeModel = (ReferenceTreeMetaModel) FacesUtils
                .getManagedBean(Constant.REFERENCE_TREE);

        // Process all hosts that are in search result
        Host[] hosts = resultCollection.getHost();
        if (hosts != null) {
            for (Host host : hosts) {
                NetworkMetaEntity hostMetaEntity = referenceTreeModel
                        .getHostById(host.getHostID());
                if (hostMetaEntity != null) {
                    hostMetaEntity.setDateFormatString(dateFormatString);
                    searchResultList.add(hostMetaEntity);
                }
            }
        }

        // Process all host groups that are in search result
        HostGroup[] hostGroups = resultCollection.getHostGroup();
        if (hostGroups != null) {
            for (HostGroup hostGroup : hostGroups) {
                NetworkMetaEntity hostGroupMetaEntity = referenceTreeModel
                        .getHostGroupById(hostGroup.getHostGroupID());

                if (hostGroupMetaEntity != null) {
                    hostGroupMetaEntity.setDateFormatString(dateFormatString);
                    searchResultList.add(hostGroupMetaEntity);
                }
            }
        }

        // Process all services that are in search result
        ServiceStatus[] services = resultCollection.getServiceStatus();
        if (services != null) {
            for (ServiceStatus serviceStatus : services) {
                NetworkMetaEntity serviceMetaEntity = referenceTreeModel
                        .getServiceById(serviceStatus.getServiceStatusID());
                if (serviceMetaEntity != null) {
                    serviceMetaEntity.setDateFormatString(dateFormatString);
                    searchResultList.add(serviceMetaEntity);
                }
            }
        }

        // Process all service groups that are in search result
        Category[] serviceGroups = resultCollection.getCategory();
        if (serviceGroups != null) {
            for (Category serviceGroup : serviceGroups) {
                NetworkMetaEntity serviceGroupMetaEntity = referenceTreeModel
                        .getServiceGroupById(serviceGroup.getCategoryId());
                if (serviceGroupMetaEntity != null) {
                    serviceGroupMetaEntity
                            .setDateFormatString(dateFormatString);
                    searchResultList.add(serviceGroupMetaEntity);
                }
            }
        }
        // Sort result table alphabetically : Type NetworkMetaEntity is
        // "Comparable" for alphabetic sort.
        Collections.sort(searchResultList);

        // show warning message popup to user if result count exceeds the Max
        // result count
        if (resultCollection.getTotalCount() > maxSearchResultCount) {
            // searchResultCount must be maxSearchResultCount+1. since we have
            // to display only maxSearchResultCount results on UI, delete last
            // result
            searchResultList.remove(maxSearchResultCount);
            searchResultCount = searchResultList.size();
            popupVisible = true;
        }
        searchResultCount = searchResultList.size();

        // set "first" time search as false, if its true
        if (firstSearch) {
            firstSearch = false;
        }
    }

    /**
     * Search for query with web service call .
     */
    public void searchAction() {
        this.searchEntities();
    }

    /**
     * Sort search results based on selected option.
     * 
     * @param event
     */
    public void sortSearchResults(ActionEvent event) {
        if (selectedSortOption
                .equals(SortTypeEnum.HOST_HOSTGROUP_SERVICE_SERVICEGROUP
                        .getDisplayName())) {
            comparator
                    .setSortType(SortTypeEnum.HOST_HOSTGROUP_SERVICE_SERVICEGROUP);
        } else if (selectedSortOption
                .equals(SortTypeEnum.HOSTGROUP_HOST_SERVICEGROUP_SERVICE
                        .getDisplayName())) {
            comparator
                    .setSortType(SortTypeEnum.HOSTGROUP_HOST_SERVICEGROUP_SERVICE);
        } else if (selectedSortOption
                .equals(SortTypeEnum.SERVICE_SERVICEGROUP_HOST_HOSTGROUP
                        .getDisplayName())) {
            comparator
                    .setSortType(SortTypeEnum.SERVICE_SERVICEGROUP_HOST_HOSTGROUP);
        } else if (selectedSortOption
                .equals(SortTypeEnum.SERVICEGROUP_SERVICE_HOSTGROUP_HOST
                        .getDisplayName())) {
            comparator
                    .setSortType(SortTypeEnum.SERVICEGROUP_SERVICE_HOSTGROUP_HOST);
        } else if (selectedSortOption.equals(SortTypeEnum.ALPHABETIC
                .getDisplayName())) {
            Collections.sort(searchResultList);
            return;
        }
        Collections.sort(searchResultList, comparator);
    }

    /**
     * Method called when close button on UI is called.
     */
    public void closePopup() {
        setPopupVisible(false);
    }

    /**
     * sets search result count
     * 
     * @param searchResultCount
     */
    public void setSearchResultCount(int searchResultCount) {
        this.searchResultCount = searchResultCount;
    }

    /**
     * returns search result count
     * 
     * @return searchResultCount
     */
    public int getSearchResultCount() {
        return searchResultCount;
    }

    /**
     * returns result list
     * 
     * @return searchResultList
     */
    public List<NetworkMetaEntity> getSearchResultList() {
        return searchResultList;
    }

    /**
     * sets search query
     * 
     * @param searchQuery
     */
    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    /**
     * returns search query
     * 
     * @return searchQuery
     */
    public String getSearchQuery() {
        return searchQuery;
    }

    /**
     * sets sort option
     * 
     * @param selectedSortOption
     */
    public void setSelectedSortOption(String selectedSortOption) {
        this.selectedSortOption = selectedSortOption;
    }

    /**
     * returns selected sort option
     * 
     * @return String
     */
    public String getSelectedSortOption() {
        return selectedSortOption;
    }

    /**
     * Returns Sorting options.
     * 
     * @return ArrayList of Sorting Options
     */
    public ArrayList<SelectItem> getSortingOptions() {
        return sortingOptions;
    }

    /**
     * Returns true on error.
     * 
     * @return true on error
     */
    public boolean isError() {
        return error;
    }

    /**
     * Sets error message.
     * 
     * @param errorMessage
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Returns the error message.
     * 
     * @return errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets true if there is an error on page.
     * 
     * @param error
     */
    public void setError(boolean error) {
        this.error = error;
    }

    /**
     * This method is called when user clicks on "retry now" button on error
     * screen.
     * 
     * @param event
     */
    public void reloadPage(ActionEvent event) {
        error = false;
    }

    /**
     * Sets the popupVisible.
     * 
     * @param popupVisible
     *            the popupVisible to set
     */
    public void setPopupVisible(boolean popupVisible) {
        this.popupVisible = popupVisible;
    }

    /**
     * Returns the popupVisible.
     * 
     * @return the popupVisible
     */
    public boolean isPopupVisible() {
        return popupVisible;
    }

    /**
     * Sets the maxSearchResultCount.
     * 
     * @param maxSearchResultCount
     *            the maxSearchResultCount to set
     */
    public void setMaxSearchResultCount(int maxSearchResultCount) {
        this.maxSearchResultCount = maxSearchResultCount;
    }

    /**
     * Returns the maxSearchResultCount.
     * 
     * @return the maxSearchResultCount
     */
    public int getMaxSearchResultCount() {
        return maxSearchResultCount;
    }

    /**
     * returns search result warning string
     * 
     * @return String
     */
    public String getSearchResultsWarningProlog() {
        return searchResultsWarningProlog;
    }

    /**
     * returns search result warning string
     * 
     * @return String
     */
    public String getSearchResultsWarningEpilog() {
        return searchResultsWarningEpilog.replaceAll(Constant.HASH, Integer
                .toString(maxSearchResultCount));
    }

    /**
     * returns whether the message such as no results found should be displayed
     * or not.
     * 
     * @return boolean
     */
    public boolean isShowMessage() {
        if (firstSearch || searchResultCount != 0) {
            return false;
        }
        return true;
    }

}
