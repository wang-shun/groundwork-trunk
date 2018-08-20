/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2014  GroundWork Open Source Solutions info@groundworkopensource.com

 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of version 2 of the GNU General Public License
 *     as published by the Free Software Foundation.

 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.

 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.groundwork.collage.model;

import java.util.Date;

/**
 * AuditLog
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public interface AuditLog extends PropertyExtensible {

    /** AuditLog actions */
    enum Action {
        ADD,
        DELETE,
        MODIFY,
        SYNC,
        ENABLE,
        DISABLE,
        BACKUP,
        RESTORE,
        ACTION
    }

    /** Entity type */
    static final String ENTITY_TYPE_CODE = "AuditLog";

    /** Spring bean interface id */
    static final String INTERFACE_NAME = "com.groundwork.collage.model.AuditLog";

    /** Hibernate component name that this entity service using */
    static final String COMPONENT_NAME = "com.groundwork.collage.model.impl.AuditLog";

    /* Built-In (Non-dynamic) Properties - The Properties which have a description (hibernate property)
     * defined are properties that can be used to query the entity.
     */
    static final String HP_ID = "auditLogId";
    static final String HP_SUBSYSTEM = "subsystem";
    static final String HP_ACTION = "action";
    static final String HP_DESCRIPTION = "description";
    static final String HP_USERNAME = "username";
    static final String HP_TIMESTAMP = "timestamp";
    static final String HP_HOST_NAME = "hostName";
    static final String HP_SERVICE_DESCRIPTION = "serviceDescription";
    static final String HP_HOST_GROUP_NAME = "hostGroupName";
    static final String HP_SERVICE_GROUP_NAME = "serviceGroupName";

    /** Entity Property Constants */
    static final String EP_ID = "AuditLogId";
    static final String EP_SUBSYSTEM = "Subsystem";
    static final String EP_ACTION = "Action";
    static final String EP_DESCRIPTION = "Description";
    static final String EP_USERNAME = "Username";
    static final String EP_TIMESTAMP = "Timestamp";
    static final String EP_HOST_NAME = "HostName";
    static final String EP_SERVICE_DESCRIPTION = "ServiceDescription";
    static final String EP_HOST_GROUP_NAME = "HostGroupName";
    static final String EP_SERVICE_GROUP_NAME = "ServiceGroupName";

    Integer getAuditLogId();

    String getSubsystem();
    void setSubsystem(String subsystem);

    Action getAction();
    void setAction(Action action);

    String getDescription();
    void setDescription(String description);

    String getUsername();
    void setUsername(String username);

    Date getTimestamp();
    void setTimestamp(Date timestamp);

    String getHostName();
    void setHostName(String hostName);

    String getServiceDescription();
    void setServiceDescription(String serviceDescription);

    String getHostGroupName();
    void setHostGroupName(String hostGroupName);

    String getServiceGroupName();
    void setServiceGroupName(String serviceGroupName);
}
