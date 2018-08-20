/*
 * Copyright 2009 GroundWork Open Source, Inc. ("GroundWork") All rights
 * reserved. This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License aString with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package com.groundworkopensource.portal.model;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Class UserNavigation.
 * 
 * @author Hibernate CodeGenerator
 */
@XmlRootElement(name = "navigation")
public class UserNavigation implements Serializable {

    /** serialVersionUID. */
    //private static final String serialVersionUID = -6889002035007975292L;

    /** identifier field. */
    private Integer id;

    /** persistent field. */
    private String userId;

    /** persistent field. */
    private int nodeId;

    /** persistent field. */
    private String nodeName;

    /** persistent field. */
    private String nodeType;

    /** nullable persistent field. */
    private String parentInfo;

    /** persistent field - toolTip. */
    private String toolTip;

    /** application type */
    private String appType;

    /** application type */
    private String tabHistory;

    /** application type */
    private String nodeLabel;

    /**
     * full constructor.
     * 
     * @param id
     *            the id
     * @param userId
     *            the user id
     * @param nodeId
     *            the node id
     * @param nodeName
     *            the node name
     * @param nodeType
     *            the node type
     * @param parentInfo
     *            the parent info
     * @param toolTip
     *            the toolTip information
     * @param app_type
     *            application type
     * @param tabHistory
     * @param nodeLabel
     */
    public UserNavigation(Integer id, String userId, int nodeId, String nodeName,
            String nodeType, String parentInfo, String toolTip,
            String app_type, String tabHistory, String nodeLabel) {
        this.id = id;
        this.userId = userId;
        this.nodeId = nodeId;
        this.nodeName = nodeName;
        this.nodeType = nodeType;
        this.parentInfo = parentInfo;
        this.toolTip = toolTip;
        this.appType = app_type;
        this.tabHistory = tabHistory;
        this.nodeLabel = nodeLabel;

    }

    /**
     * default constructor.
     */
    public UserNavigation() {
    }

    /**
     * minimal constructor.
     * 
     * @param id
     *            the id
     * @param userId
     *            the user id
     * @param nodeId
     *            the node id
     * @param nodeName
     *            the node name
     * @param nodeType
     *            the node type
     */
    public UserNavigation(Integer id, String userId, int nodeId, String nodeName,
            String nodeType) {
        this.id = id;
        this.userId = userId;
        this.nodeId = nodeId;
        this.nodeName = nodeName;
        this.nodeType = nodeType;
    }

    /**
     * Gets the id.
     * 
     * @return the id
     */
    @XmlAttribute
    public Integer getId() {
        return this.id;
    }

    /**
     * Sets the id.
     * 
     * @param id
     *            the new id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Gets the user id.
     * 
     * @return the user id
     */
    @XmlAttribute
    public String getUserId() {
        return this.userId;
    }

    /**
     * Sets the user id.
     * 
     * @param userId
     *            the new user id
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Gets the node id.
     * 
     * @return the node id
     */
    @XmlAttribute
    public int getNodeId() {
        return this.nodeId;
    }

    /**
     * Sets the node id.
     * 
     * @param nodeId
     *            the new node id
     */
    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    /**
     * Gets the node name.
     * 
     * @return the node name
     */
    @XmlAttribute
    public String getNodeName() {
        return this.nodeName;
    }

    /**
     * Sets the node name.
     * 
     * @param nodeName
     *            the new node name
     */
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    /**
     * Gets the node type.
     * 
     * @return the node type
     */
    @XmlAttribute
    public String getNodeType() {
        return this.nodeType;
    }

    /**
     * Sets the node type.
     * 
     * @param nodeType
     *            the new node type
     */
    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    /**
     * Gets the parent info.
     * 
     * @return the parent info
     */
    @XmlAttribute
    public String getParentInfo() {
        return this.parentInfo;
    }

    /**
     * Sets the parent info.
     * 
     * @param parentInfo
     *            the new parent info
     */
    public void setParentInfo(String parentInfo) {
        this.parentInfo = parentInfo;
    }

    /**
     * Sets the toolTip.
     * 
     * @param toolTip
     *            the toolTip to set
     */
    public void setToolTip(String toolTip) {
        this.toolTip = toolTip;
    }

    /**
     * Returns the toolTip.
     * 
     * @return the toolTip
     */
    @XmlAttribute
    public String getToolTip() {
        return toolTip;
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", getId()).toString();
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof UserNavigation))
            return false;
        UserNavigation castOther = (UserNavigation) other;
        return new EqualsBuilder().append(this.getId(), castOther.getId())
                .isEquals();
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getId()).toHashCode();
    }

    /**
     * Sets the appType.
     * 
     * @param appType
     *            the appType to set
     */
    public void setAppType(String appType) {
        this.appType = appType;
    }

    /**
     * Returns the appType.
     * 
     * @return the appType
     */
    @XmlAttribute
    public String getAppType() {
        return appType;
    }

    /**
     * Sets the tabHistory.
     * 
     * @param tabHistory
     *            the tabHistory to set
     */
    public void setTabHistory(String tabHistory) {
        this.tabHistory = tabHistory;
    }

    /**
     * Returns the tabHistory.
     * 
     * @return the tabHistory
     */
    @XmlAttribute
    public String getTabHistory() {
        return tabHistory;
    }

    /**
     * Sets the nodeLabel.
     * 
     * @param nodeLabel
     *            the nodeLabel to set
     */
    public void setNodeLabel(String nodeLabel) {
        this.nodeLabel = nodeLabel;
    }

    /**
     * Returns the nodeLabel.
     * 
     * @return the nodeLabel
     */
    @XmlAttribute
    public String getNodeLabel() {
        return nodeLabel;
    }

}
