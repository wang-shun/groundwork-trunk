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

/**
 * DtoBizHostServiceInDowntime
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@XmlRootElement(name = "bizHostServiceInDowntime")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoBizHostServiceInDowntime {

    @XmlAttribute
    private String hostName;
    @XmlAttribute
    private String serviceDescription;
    @XmlAttribute
    private Integer scheduledDowntimeDepth;
    @XmlAttribute
    private String entityType;
    @XmlAttribute
    private String entityName;

    /**
     * BizHostServiceInDowntime host constructor.
     *
     * @param hostName host name
     */
    public DtoBizHostServiceInDowntime(String hostName) {
        this(hostName, null);
    }

    /**
     * BizHostServiceInDowntime host service constructor.
     *
     * @param hostName host name
     * @param serviceDescription service description
     */
    public DtoBizHostServiceInDowntime(String hostName, String serviceDescription) {
        this(hostName, serviceDescription, null, null, null);
    }

    /**
     * BizHostServiceInDowntime return constructor.
     *
     * @param hostName host name
     * @param serviceDescription service description
     * @param scheduledDowntimeDepth scheduled downtime depth
     * @param entityType entity type
     * @param entityName entity name
     */
    public DtoBizHostServiceInDowntime(String hostName,
                                       String serviceDescription,
                                       Integer scheduledDowntimeDepth,
                                       String entityType,
                                       String entityName) {
        this.hostName = hostName;
        this.serviceDescription = serviceDescription;
        this.scheduledDowntimeDepth = scheduledDowntimeDepth;
        this.entityType = entityType;
        this.entityName = entityName;
    }

    /**
     * Default BizHostServiceInDowntime constructor.
     */
    public DtoBizHostServiceInDowntime() {
    }

    /**
     * toString Object protocol implementation.
     *
     * @return BizHostServiceInDowntime as String
     */
    @Override
    public String toString() {
        return String.format(getClass().getName()+"@%x[hostName=%s,serviceDescription=%s,scheduledDowntimeDepth=%d,entityType=%s,entityName=%s]",
                System.identityHashCode(this), hostName, serviceDescription, scheduledDowntimeDepth, entityType, entityName);
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

    public Integer getScheduledDowntimeDepth() {
        return scheduledDowntimeDepth;
    }

    public void setScheduledDowntimeDepth(Integer scheduledDowntimeDepth) {
        this.scheduledDowntimeDepth = scheduledDowntimeDepth;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }
}
