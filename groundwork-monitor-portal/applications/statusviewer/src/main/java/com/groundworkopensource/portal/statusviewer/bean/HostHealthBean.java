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
public class HostHealthBean implements Serializable {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -6077070405907160878L;

    /**
     * Host Name.
     */
    private String hostName;

    /**
     * Host Name as label on UI.
     */
    private String hostNameLabel;

    /**
     * Host Alias.
     */
    private String alias;

    /**
     * Host Status.
     */
    private NetworkObjectStatusEnum status;
    /**
     * Host State Time - time from which Service is in this particular state.
     */
    private String hostStateDuration;

    /**
     * Count of groups of this host to show on UI
     */
    private int hostGroupsCount;

    /**
     * Count of parents of this host to show on UI
     */
    private int parentsCount;

    /**
     * List of groups for this host
     */
    private List<NetworkMetaEntity> groupList = new ArrayList<NetworkMetaEntity>();

    /**
     * List of parents for this host
     */
    private List<NetworkMetaEntity> parentList = new ArrayList<NetworkMetaEntity>();

    /**
     * Field that is bound to Sort Column of groups list for this host.
     */
    private String sortGroupColumn;

    /**
     * Field that is bound to Ascending field of groups list for this host.
     */
    private boolean ascendingForGroup = true;

    /**
     * Field that is bound to sort column of parents list for this host.
     */
    private String sortParentColumn;

    /**
     * Field that is bound to Ascending field of parents list for this host.
     */
    private boolean ascendingForParent;

    /**
     * Variable which decides if user in Admin or Operator role.
     */
    private boolean userInAdminOrOperatorRole;

    /**
     * Last state change date.
     */
    private String lastStateChangeDate;

    /**
     * Notes for the Host.
     */
    private String hostNotes;

    /**
     * Returns the status.
     * 
     * @return the status
     */
    public NetworkObjectStatusEnum getStatus() {
        return status;
    }

    /**
     * Sets the status.
     * 
     * @param status
     *            the status to set
     */
    public void setStatus(NetworkObjectStatusEnum status) {
        this.status = status;
    }

    /**
     * Returns the hostGroupsCount.
     * 
     * @return the hostGroupsCount
     */
    public int getHostGroupsCount() {
        return hostGroupsCount;
    }

    /**
     * Sets the hostGroupsCount.
     * 
     * @param hostGroupsCount
     *            the hostGroupsCount to set
     */
    public void setHostGroupsCount(int hostGroupsCount) {
        this.hostGroupsCount = hostGroupsCount;
    }

    /**
     * Returns the parentsCount.
     * 
     * @return the parentsCount
     */
    public int getParentsCount() {
        return parentsCount;
    }

    /**
     * Sets the parentsCount.
     * 
     * @param parentsCount
     *            the parentsCount to set
     */
    public void setParentsCount(int parentsCount) {
        this.parentsCount = parentsCount;
    }

    /**
     * Sets the hostName.
     * 
     * @param hostName
     *            the hostName to set
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * Returns the hostName.
     * 
     * @return the hostName
     */
    public String getHostName() {
        return hostName;
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
     * Sets the parentList.
     * 
     * @param parentList
     *            the parentList to set
     */
    public void setParentList(List<NetworkMetaEntity> parentList) {
        this.parentList = parentList;
    }

    /**
     * Returns the parentList.
     * 
     * @return the parentList
     */
    public List<NetworkMetaEntity> getParentList() {
        sortParentList();
        return parentList;
    }

    /**
     * Sets the hostStateDuration.
     * 
     * @param hostStateDuration
     *            the hostStateDuration to set
     */
    public void setHostStateDuration(String hostStateDuration) {
        this.hostStateDuration = hostStateDuration;
    }

    /**
     * Returns the hostStateDuration.
     * 
     * @return the hostStateDuration
     */

    public String getHostStateDuration() {
        return hostStateDuration;
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
     * Sets the sortParentColumn.
     * 
     * @param sortParentColumn
     *            the sortParentColumn to set
     */
    public void setSortParentColumn(String sortParentColumn) {
        this.sortParentColumn = sortParentColumn;
    }

    /**
     * Returns the sortParentColumn.
     * 
     * @return the sortParentColumn
     */
    public String getSortParentColumn() {
        return sortParentColumn;
    }

    /**
     * Sets the ascendingForParent.
     * 
     * @param ascendingForParent
     *            the ascendingForParent to set
     */
    public void setAscendingForParent(boolean ascendingForParent) {
        this.ascendingForParent = ascendingForParent;
    }

    /**
     * Returns the ascendingForParent.
     * 
     * @return the ascendingForParent
     */
    public boolean isAscendingForParent() {
        return ascendingForParent;
    }

    /**
     * Sorts the Group List for Host health portlet.
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
     * Sorts the Parent List for Host health portlet.
     */
    public void sortParentList() {
        Comparator<NetworkMetaEntity> comparator = new Comparator<NetworkMetaEntity>() {
            public int compare(NetworkMetaEntity entity1,
                    NetworkMetaEntity entity2) {
                String name1 = entity1.getName();
                String name2 = entity2.getName();
                int result = 0;
                // For sort order ascending -
                if (isAscendingForParent()) {
                    result = name1.compareTo(name2);
                } else {
                    // Descending
                    result = name2.compareTo(name1);
                }
                return result;
            }
        };
        // sort the parent List
        Collections.sort(parentList, comparator);
    }

    /**
     * Sets the alias.
     * 
     * @param alias
     *            the alias to set
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * Returns the alias.
     * 
     * @return the alias
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Sets the hostNameLabel.
     * 
     * @param hostNameLabel
     *            the hostNameLabel to set
     */
    public void setHostNameLabel(String hostNameLabel) {
        this.hostNameLabel = hostNameLabel;
    }

    /**
     * Returns the hostNameLabel.
     * 
     * @return the hostNameLabel
     */
    public String getHostNameLabel() {
        return hostNameLabel;
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
     * Sets the hostNotes.
     * 
     * @param hostNotes
     *            the hostNotes to set
     */
    public void setHostNotes(String hostNotes) {
        this.hostNotes = hostNotes;
    }

    /**
     * Returns the hostNotes.
     * 
     * @return the hostNotes
     */
    public String getHostNotes() {
        return hostNotes;
    }

}
