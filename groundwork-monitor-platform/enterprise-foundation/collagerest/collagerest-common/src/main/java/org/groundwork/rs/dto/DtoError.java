/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2015  GroundWork Open Source Solutions info@groundworkopensource.com

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
 * DtoError
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@XmlRootElement(name = "error")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoError {

    @XmlAttribute
    private String error;
    @XmlAttribute
    private Integer status;

    /**
     * Default constructor.
     */
    public DtoError() {
    }

    /**
     * Error message constructor.
     *
     * @param error error message string
     */
    public DtoError(String error) {
        this.error = error;
    }

    /**
     * Error message and HTTP status code constructor.
     *
     * @param error error message string
     * @param status HTTP status code
     */
    public DtoError(String error, int status) {
        this(error);
        this.status = status;
    }

    /**
     * toString Object protocol implementation.
     *
     * @return HostIdentity as String
     */
    @Override
    public String toString() {
        return String.format(getClass().getName()+"@%x[error=%s,status=%s]",
                System.identityHashCode(this), error, ((status != null) ? status.toString() : null));
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
