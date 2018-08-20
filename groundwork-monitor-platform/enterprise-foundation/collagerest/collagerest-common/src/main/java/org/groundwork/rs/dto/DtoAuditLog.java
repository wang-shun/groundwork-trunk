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

package org.groundwork.rs.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

/**
 * DtoAuditLog
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@XmlRootElement(name = "auditLog")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoAuditLog {

    @XmlAttribute
    private Integer auditLogId;
    @XmlAttribute
    private String subsystem;
    @XmlAttribute
    private String action;
    @XmlAttribute
    private String description;
    @XmlAttribute
    private String username;
    @XmlAttribute
    private Date timestamp;
    @XmlAttribute
    private String hostName;
    @XmlAttribute
    private String serviceDescription;
    @XmlAttribute
    private String hostGroupName;
    @XmlAttribute
    private String serviceGroupName;

    /**
     * Base DtoAuditLog constructor.
     *
     * @param auditLogId
     * @param subsystem
     * @param action
     * @param description
     * @param username
     * @param timestamp
     */
    public DtoAuditLog(Integer auditLogId, String subsystem, String action, String description, String username,
                       Date timestamp) {
        if ((subsystem == null) || (action == null) || (description == null) || (username == null)) {
            throw new IllegalArgumentException("subsystem, action, description, and username must be specified");
        }
        this.auditLogId = auditLogId;
        this.subsystem = subsystem;
        this.action = action;
        this.description = description;
        this.timestamp = timestamp;
        this.username = username;
    }

    /**
     * Host DtoAuditLog constructor.
     *
     * @param auditLogId
     * @param subsystem
     * @param action
     * @param description
     * @param username
     * @param timestamp
     * @param hostName
     */
    public DtoAuditLog(Integer auditLogId, String subsystem, String action, String description, String username,
                       Date timestamp, String hostName) {
        this(auditLogId, subsystem, action, description, username, timestamp);
        this.hostName = hostName;
    }

    /**
     * Service DtoAuditLog constructor.
     *
     * @param auditLogId
     * @param subsystem
     * @param action
     * @param description
     * @param username
     * @param timestamp
     * @param hostName
     * @param serviceDescription
     */
    public DtoAuditLog(Integer auditLogId, String subsystem, String action, String description, String username,
                       Date timestamp, String hostName, String serviceDescription) {
        this(auditLogId, subsystem, action, description, username, timestamp, hostName);
        this.serviceDescription = serviceDescription;
    }

    /**
     * Base client DtoAuditLog constructor, (id and timestamp set by server).
     *
     * @param subsystem
     * @param action
     * @param description
     * @param username
     */
    public DtoAuditLog(String subsystem, String action, String description, String username) {
        this((Integer)null, subsystem, action, description, username, (Date)null);
    }

    /**
     * Host client DtoAuditLog constructor, (id and timestamp set by server).
     *
     * @param subsystem
     * @param action
     * @param description
     * @param username
     * @param hostName
     */
    public DtoAuditLog(String subsystem, String action, String description, String username, String hostName) {
        this((Integer)null, subsystem, action, description, username, (Date)null, hostName);
    }

    /**
     * Service client DtoAuditLog constructor, (id and timestamp set by server).
     *
     * @param subsystem
     * @param action
     * @param description
     * @param username
     * @param hostName
     * @param serviceDescription
     */
    public DtoAuditLog(String subsystem, String action, String description, String username, String hostName,
                       String serviceDescription) {
        this((Integer)null, subsystem, action, description, username, (Date)null, hostName, serviceDescription);
    }

    /**
     * Default DtoAuditLog API constructor.
     */
    public DtoAuditLog() {
    }

    /**
     * toString Object protocol implementation.
     *
     * @return AuditLog as String
     */
    @Override
    public String toString() {
        return String.format(getClass().getName()+
                        "@%x[auditLogId=%d,subsystem=%s,action=%s,description=%s,username=%s,timestamp=%s,hostName=%s,serviceDescription=%s,hostGroupName=%s,serviceGroupName=%s]",
                System.identityHashCode(this), auditLogId, subsystem, action, description, username, timestamp, hostName,
                serviceDescription, hostGroupName, serviceGroupName);
    }

    public Integer getAuditLogId() {
        return auditLogId;
    }

    public void setAuditLogId(Integer auditLogId) {
        this.auditLogId = auditLogId;
    }

    public String getSubsystem() {
        return subsystem;
    }

    public void setSubsystem(String subsystem) {
        this.subsystem = subsystem;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getServiceDescription() {
        return serviceDescription;
    }

    public void setServiceDescription(String serviceDescription) {
        this.serviceDescription = serviceDescription;
    }

    public String getHostGroupName() {
        return hostGroupName;
    }

    public void setHostGroupName(String hostGroupName) {
        this.hostGroupName = hostGroupName;
    }

    public String getServiceGroupName() {
        return serviceGroupName;
    }

    public void setServiceGroupName(String serviceGroupName) {
        this.serviceGroupName = serviceGroupName;
    }
}
