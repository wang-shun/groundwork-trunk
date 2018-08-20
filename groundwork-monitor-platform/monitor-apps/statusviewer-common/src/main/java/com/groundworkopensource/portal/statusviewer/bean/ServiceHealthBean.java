package com.groundworkopensource.portal.statusviewer.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.faces.event.ActionEvent;

import com.groundworkopensource.portal.statusviewer.common.NetworkMetaEntity;
import com.groundworkopensource.portal.statusviewer.common.NetworkObjectStatusEnum;

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

/**
 * Bean for Service Health portlet.
 * 
 * @author rashmi_tambe
 */
public class ServiceHealthBean implements Serializable {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 4594409118672449065L;

    /**
     * Service Name.
     */
    private String serviceName;

    /**
     * Service Name label.
     */
    private String serviceNameLabel;
    /**
     * Service Status.
     */
    private NetworkObjectStatusEnum status;
    /**
     * Service State Time - time from which Service is in this particular state.
     */
    private String serviceStateDuration;

    /**
     * Service desc - required for configuration page
     */
    private String serviceDesc;
    /**
     * host Name - required for configuration page
     */
    private String hostName;

    /**
     * Count of service groups to show on UI
     */
    private int serviceGroupsCount;

    /**
     * Count of dependent services to show on UI
     */
    private int dependentServicesCount;

    /**
     * List that contains service groups for this service
     */
    private List<NetworkMetaEntity> groupList = new ArrayList<NetworkMetaEntity>();

    /**
     * List that contains service groups for this service
     */
    private List<NetworkMetaEntity> dependentList = new ArrayList<NetworkMetaEntity>();

    /**
     * URL for parent Host of this service.
     */
    private String hostUrl;

    /**
     * Field that is bound to Sort Column of groups list for this service.
     */
    private String sortGroupColumn;

    /**
     * Field that is bound to Ascending field of groups list for this service.
     */
    private boolean ascendingForGroup;

    /**
     * Field that is bound to sort column of dependents list for this service.
     */
    private String sortDependentColumn;

    /**
     * Field that is bound to Ascending field of dependents list for this
     * service.
     */
    private boolean ascendingForDependent;

    /**
     * true if service is in Warning status
     */
    private boolean warningStatus;

    /**
     * Variable which decides if user in Admin or Operator role.
     */
    private boolean userInAdminOrOperatorRole;

    /**
     * Last state change date.
     */
    private String lastStateChangeDate;

    /**
     * Notes for the Service.
     */
    private String serviceNotes;

    /**
     * Returns the serviceName.
     * 
     * @return the serviceName
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Sets the serviceName.
     * 
     * @param serviceName
     *            the serviceName to set
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * @return the hostName
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * @param hostName
     *            the hostName to set
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * @return the serviceDesc
     */
    public String getServiceDesc() {
        return serviceDesc;
    }

    /**
     * @param serviceDesc
     *            the serviceDesc to set
     */
    public void setServiceDesc(String serviceDesc) {
        this.serviceDesc = serviceDesc;
    }

    /**
     * default constructor
     */
    public ServiceHealthBean() {
        serviceGroupsCount = 0;
        dependentServicesCount = 0;
    }

    /**
     * Sets the serviceState.
     * 
     * @param status
     */
    public void setStatus(NetworkObjectStatusEnum status) {
        this.status = status;
    }

    /**
     * Returns the serviceState.
     * 
     * @return the serviceState
     */
    public NetworkObjectStatusEnum getStatus() {
        return status;
    }

    /**
     * Sets the serviceGroupsCount.
     * 
     * @param serviceGroupsCount
     *            the serviceGroupsCount to set
     */
    public void setServiceGroupsCount(int serviceGroupsCount) {
        this.serviceGroupsCount = serviceGroupsCount;
    }

    /**
     * Returns the serviceGroupsCount.
     * 
     * @return the serviceGroupsCount
     */
    public int getServiceGroupsCount() {
        return serviceGroupsCount;
    }

    /**
     * Sets the dependentServicesCount.
     * 
     * @param dependentServicesCount
     *            the dependentServicesCount to set
     */
    public void setDependentServicesCount(int dependentServicesCount) {
        this.dependentServicesCount = dependentServicesCount;
    }

    /**
     * Returns the dependentServicesCount.
     * 
     * @return the dependentServicesCount
     */
    public int getDependentServicesCount() {
        return dependentServicesCount;
    }

    /**
     * Sets the groupList.
     * 
     * @param groupList
     *            the groupList to set
     */
    public void setGroupList(List<NetworkMetaEntity> groupList) {
        this.groupList = groupList;
    }

    /**
     * Returns the groupList.
     * 
     * @return the groupList
     */
    public List<NetworkMetaEntity> getGroupList() {
        return groupList;
    }

    /**
     * Returns service group count for this service
     * 
     * @return the groupCount
     */
    public int getGroupCount() {
        return groupList.size();
    }

    /**
     * Sets the serviceStateDuration.
     * 
     * @param serviceStateDuration
     *            the serviceStateDuration to set
     */
    public void setServiceStateDuration(String serviceStateDuration) {
        this.serviceStateDuration = serviceStateDuration;
    }

    /**
     * Returns the serviceStateDuration.
     * 
     * @return the serviceStateDuration
     */
    public String getServiceStateDuration() {
        return serviceStateDuration;
    }

    /**
     * Sets the dependentList.
     * 
     * @param dependentList
     *            the dependentList to set
     */
    public void setDependentList(List<NetworkMetaEntity> dependentList) {
        this.dependentList = dependentList;
    }

    /**
     * Returns the dependentList.
     * 
     * @return the dependentList
     */
    public List<NetworkMetaEntity> getDependentList() {
        // sortDependentList();
        return dependentList;
    }

    /**
     * Sets the hostUrl.
     * 
     * @param hostUrl
     *            the hostUrl to set
     */
    public void setHostUrl(String hostUrl) {
        this.hostUrl = hostUrl;
    }

    /**
     * Returns the hostUrl.
     * 
     * @return the hostUrl
     */
    public String getHostUrl() {
        return hostUrl;
    }

    // /**
    // * Sorts the Group List for Service health portlet.
    // */
    // public void sortGroupList() {
    // Comparator<NetworkMetaEntity> comparator = new
    // Comparator<NetworkMetaEntity>() {
    // public int compare(NetworkMetaEntity entity1,
    // NetworkMetaEntity entity2) {
    // String name1 = entity1.getName();
    // String name2 = entity2.getName();
    // int result = 0;
    // // For sort order ascending -
    // if (isAscendingForGroup()) {
    // result = name1.compareTo(name2);
    // } else {
    // // Descending
    // result = name2.compareTo(name1);
    // }
    // return result;
    // }
    // };
    // // sort the serviceList
    // Collections.sort(groupList, comparator);
    // }

    /**
     * Sorts the Group List for Service health portlet.
     * 
     * @param event
     */
    public void sortGroupList(ActionEvent event) {
        Comparator<NetworkMetaEntity> comparator = new Comparator<NetworkMetaEntity>() {
            public int compare(NetworkMetaEntity entity1,
                    NetworkMetaEntity entity2) {
                String name1 = entity1.getName();
                String name2 = entity2.getName();
                int result = 0;
                // For sort order ascending -
                if (isAscendingForGroup()) {
                    result = name1.compareTo(name2);
                } else {
                    // Descending
                    result = name2.compareTo(name1);
                }
                return result;
            }
        };
        // set ascending
        ascendingForGroup = !ascendingForGroup;
        // sort the group List
        Collections.sort(groupList, comparator);
    }

    /**
     * @param event
     */
    public void sort(ActionEvent event) {
        setAscendingForGroup(false);
        setSortGroupColumn(null);
        sortGroupList(event);
    }

    /**
     * Sets the sortGroupColumn.
     * 
     * @param sortGroupColumn
     *            the sortGroupColumn to set
     */
    public void setSortGroupColumn(String sortGroupColumn) {
        this.sortGroupColumn = sortGroupColumn;
    }

    /**
     * Returns the sortGroupColumn.
     * 
     * @return the sortGroupColumn
     */
    public String getSortGroupColumn() {
        return sortGroupColumn;
    }

    /**
     * Sets the sortDependentColumn.
     * 
     * @param sortDependentColumn
     *            the sortDependentColumn to set
     */
    public void setSortDependentColumn(String sortDependentColumn) {
        this.sortDependentColumn = sortDependentColumn;
    }

    /**
     * Returns the sortDependentColumn.
     * 
     * @return the sortDependentColumn
     */
    public String getSortDependentColumn() {
        return sortDependentColumn;
    }

    /**
     * Sets the ascendingForGroup.
     * 
     * @param ascendingForGroup
     *            the ascendingForGroup to set
     */
    public void setAscendingForGroup(boolean ascendingForGroup) {
        this.ascendingForGroup = ascendingForGroup;
    }

    /**
     * Returns the ascendingForGroup.
     * 
     * @return the ascendingForGroup
     */
    public boolean isAscendingForGroup() {
        return ascendingForGroup;
    }

    /**
     * Sets the ascendingForDependent.
     * 
     * @param ascendingForDependent
     *            the ascendingForDependent to set
     */
    public void setAscendingForDependent(boolean ascendingForDependent) {
        this.ascendingForDependent = ascendingForDependent;
    }

    /**
     * Returns the ascendingForDependent.
     * 
     * @return the ascendingForDependent
     */
    public boolean isAscendingForDependent() {
        return ascendingForDependent;
    }

    /**
     * Sets the warningStatus.
     * 
     * @param warningStatus
     *            the warningStatus to set
     */
    public void setWarningStatus(boolean warningStatus) {
        this.warningStatus = warningStatus;
    }

    /**
     * Returns the warningStatus.
     * 
     * @return the warningStatus
     */
    public boolean isWarningStatus() {
        return warningStatus;
    }

    /**
     * Sets the serviceNameLabel.
     * 
     * @param serviceNameLabel
     *            the serviceNameLabel to set
     */
    public void setServiceNameLabel(String serviceNameLabel) {
        this.serviceNameLabel = serviceNameLabel;
    }

    /**
     * Returns the serviceNameLabel.
     * 
     * @return the serviceNameLabel
     */
    public String getServiceNameLabel() {
        return serviceNameLabel;
    }

    /**
     * Sets the userInAdminOrOperatorRole.
     * 
     * @param userInAdminOrOperatorRole
     *            the userInAdminOrOperatorRole to set
     */
    public void setUserInAdminOrOperatorRole(boolean userInAdminOrOperatorRole) {
        this.userInAdminOrOperatorRole = userInAdminOrOperatorRole;
    }

    /**
     * Returns the userInAdminOrOperatorRole.
     * 
     * @return the userInAdminOrOperatorRole
     */
    public boolean isUserInAdminOrOperatorRole() {
        return userInAdminOrOperatorRole;
    }

    /**
     * Sets the serviceNotes.
     * 
     * @param serviceNotes
     *            the serviceNotes to set
     */
    public void setServiceNotes(String serviceNotes) {
        this.serviceNotes = serviceNotes;
    }

    /**
     * Returns the serviceNotes.
     * 
     * @return the serviceNotes
     */
    public String getServiceNotes() {
        return serviceNotes;
    }

    /**
     * Sets the lastStateChangeDate.
     * 
     * @param lastStateChangeDate
     *            the lastStateChangeDate to set
     */
    public void setLastStateChangeDate(String lastStateChangeDate) {
        this.lastStateChangeDate = lastStateChangeDate;
    }

    /**
     * Returns the lastStateChangeDate.
     * 
     * @return the lastStateChangeDate
     */
    public String getLastStateChangeDate() {
        return lastStateChangeDate;
    }

    /**
     * Sorts the Parent List for Host health portlet.
     */
    /*
     * public void sortDependentList() { Comparator<NetworkMetaEntity>
     * comparator = new Comparator<NetworkMetaEntity>() { public int
     * compare(NetworkMetaEntity entity1, NetworkMetaEntity entity2) { String
     * name1 = entity1.getName(); String name2 = entity2.getName(); int result =
     * 0; // For sort order ascending - if (isAscendingForDependent()) { result
     * = name1.compareTo(name2); } else { // Descending result =
     * name2.compareTo(name1); } return result; } }; // sort the serviceList
     * Collections.sort(dependentList, comparator); }
     */

}
