/*
 * Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork) All rights
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
 * 
 * dname.pl - This is a utility script to clean up the foundation database for
 * device display names and identification fields that were inconsistently fed
 * into the database. This can cause some issues in the display for the event
 * console, especially when upgrading an older database. Use in consultation
 * with GroundWork Support!
 */

package com.groundworkopensource.portal.statusviewer.bean.networkservice;

import java.io.Serializable;
import java.sql.ResultSet;

import org.apache.log4j.Logger;

/**
 * The Class Notification.
 */
public class Notification implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1499156430363838477L;

    /**
     * LOGGER
     */
    private static final Logger LOGGER = Logger.getLogger(Notification.class
            .getName());

    /**
     * Instantiates a new notification.
     * 
     * @param title
     *            the title
     * @param description
     *            the description
     * @param id
     *            the id
     */
    public Notification(String title, String description, Integer id) {
        this.setTitle(title);
        this.setDescription(description);
        this.setId(id);
    }

    /**
     * Instantiates a new notification.
     * 
     * @param rs
     *            the rs
     */
    public Notification(ResultSet rs) {
        try {
            if (rs != null) {
                this.id = rs.getInt("id");
                this.type = rs.getString("type");
                this.createdAt = rs.getString("created_at");
                this.guid = rs.getString("guid");
                this.title = rs.getString("title");
                this.description = rs.getString("description");
                this.isCritical = rs.getBoolean("critical");
                this.webpageUrl = rs.getString("webpage_url");
                this.webpageUrlDescription = rs
                        .getString("webpage_url_description");
                this.isRead = rs.getBoolean("is_read");
                this.isArchived = rs.getBoolean("is_archived");
            }
        } catch (Exception e) {
            LOGGER
                    .debug("Exception while instantiating a new notification. Exception : "
                            + e.getMessage());
        }

    }

    /**
     * Gets the id.
     * 
     * @return the id
     */
    public Integer getId() {
        return id;
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
     * Gets the guid.
     * 
     * @return the guid
     */
    public String getGuid() {
        return guid;
    }

    /**
     * Sets the guid.
     * 
     * @param guid
     *            the new guid
     */
    public void setGuid(String guid) {
        this.guid = guid;
    }

    /**
     * Gets the type.
     * 
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type.
     * 
     * @param type
     *            the new type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets the description.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     * 
     * @param description
     *            the new description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the title.
     * 
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title.
     * 
     * @param title
     *            the new title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the created at.
     * 
     * @return the created at
     */
    public String getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the created at.
     * 
     * @param createdAt
     *            the new created at
     */
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets the webpage url description.
     * 
     * @return the webpage url description
     */
    public String getWebpageUrlDescription() {
        return webpageUrlDescription;
    }

    /**
     * Sets the webpage url description.
     * 
     * @param webpageUrlDescription
     *            the new webpage url description
     */
    public void setWebpageUrlDescription(String webpageUrlDescription) {
        this.webpageUrlDescription = webpageUrlDescription;
    }

    /**
     * Gets the webpage url.
     * 
     * @return the webpage url
     */
    public String getWebpageUrl() {
        return webpageUrl;
    }

    /**
     * Sets the webpage url.
     * 
     * @param webpageUrl
     *            the new webpage url
     */
    public void setWebpageUrl(String webpageUrl) {
        this.webpageUrl = webpageUrl;
    }

    /**
     * Checks if is critical.
     * 
     * @return the boolean
     */
    public Boolean isCritical() {
        return isCritical;
    }

    /**
     * Checks if is archived.
     * 
     * @return the boolean
     */
    public Boolean isArchived() {
        return isArchived;
    }

    /**
     * Checks if is read.
     * 
     * @return the boolean
     */
    public Boolean isRead() {
        return isRead;
    }

    // view helpers
    /**
     * Gets the css class.
     * 
     * @param type
     *            the type
     * 
     * @return the css class
     */
    public String getCssClass(Integer type) {
        String postfix = "";
        String base = "";

        switch (type) {
            case 1:
                base = "ns-notification-shown";
                break; // notification shown
            default:
                base = "ns-notification";
                break; // list
        }
        if (isCritical()) {
            postfix = "-critical";
        }
        if (isArchived()) {
            postfix = "-archived";
        }
        if (isRead()) {
            postfix = "-read";
        }

        return base + postfix;
    }

    /**
     * Gets the icon name.
     * 
     * @return the icon name
     */
    public String getIconName() {
        return ("ns_notification_" + getType() + ".gif");
    }

    /** The id. */
    private Integer id;

    /** The type. */
    private String type;

    /** The is critical. */
    private Boolean isCritical = false;

    /** The is read. */
    private Boolean isRead = false;

    /** The is archived. */
    private Boolean isArchived = false;

    /** The guid. */
    private String guid;

    /** The title. */
    private String title;

    /** The description. */
    private String description;

    /** The created at. */
    private String createdAt;

    /** The webpage url description. */
    private String webpageUrlDescription;

    /** The webpage url. */
    private String webpageUrl;

    /**
     * Sets the isCritical.
     * 
     * @param isCritical
     *            the isCritical to set
     */
    public void setIsCritical(Boolean isCritical) {
        this.isCritical = isCritical;
    }

    /**
     * Sets the isRead.
     * 
     * @param isRead
     *            the isRead to set
     */
    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    /**
     * Sets the isArchived.
     * 
     * @param isArchived
     *            the isArchived to set
     */
    public void setIsArchived(Boolean isArchived) {
        this.isArchived = isArchived;
    }

}
