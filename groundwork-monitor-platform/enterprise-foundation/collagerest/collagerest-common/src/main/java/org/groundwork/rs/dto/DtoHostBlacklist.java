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
 * DtoHostBlacklist
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@XmlRootElement(name = "hostBlacklist")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoHostBlacklist {

    @XmlAttribute
    private Integer hostBlacklistId;
    @XmlAttribute
    private String hostName;

    /**
     * HostBlacklist constructor.
     *
     * @param hostName host name
     */
    public DtoHostBlacklist(String hostName) {
        this(null, hostName);
    }

    /**
     * HostBlacklist constructor.
     *
     * @param hostBlacklistId id
     * @param hostName host name
     */
    public DtoHostBlacklist(Integer hostBlacklistId, String hostName) {
        if (hostName == null) {
            throw new IllegalArgumentException("hostName must be specified");
        }
        this.hostBlacklistId = hostBlacklistId;
        this.hostName = hostName;
    }

    /**
     * Default HostBlacklist constructor.
     */
    public DtoHostBlacklist() {
    }

    /**
     * toString Object protocol implementation.
     *
     * @return HostBlacklist as String
     */
    @Override
    public String toString() {
        return String.format(getClass().getName()+"@%x[hostBlacklistId=%d,hostName=%s]",
                System.identityHashCode(this), hostBlacklistId, hostName);
    }

    public Integer getHostBlacklistId() {
        return hostBlacklistId;
    }

    public void setHostBlacklistId(Integer hostBlacklistId) {
        this.hostBlacklistId = hostBlacklistId;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
}
