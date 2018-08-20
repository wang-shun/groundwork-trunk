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
 * DtoDeviceTemplateProfile
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@XmlRootElement(name = "deviceTemplateProfile")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoDeviceTemplateProfile {

    @XmlAttribute
    private Integer deviceTemplateProfileId;
    @XmlAttribute
    private String deviceIdentification;
    @XmlAttribute
    private String deviceDescription;
    @XmlAttribute
    private String cactiHostTemplate;
    @XmlAttribute
    private String monarchHostProfile;
    @XmlAttribute
    private Date timestamp;

    /**
     * DeviceTemplateProfile constructor.
     *
     * @param deviceIdentification device identification
     * @param deviceDescription device description
     */
    public DtoDeviceTemplateProfile(String deviceIdentification, String deviceDescription) {
        this(null, deviceIdentification, deviceDescription, null, null, null);
    }

    /**
     * DeviceTemplateProfile constructor.
     *
     * @param deviceTemplateProfileId
     * @param deviceIdentification
     * @param deviceDescription
     * @param cactiHostTemplate
     * @param monarchHostProfile
     * @param timestamp
     */
    public DtoDeviceTemplateProfile(Integer deviceTemplateProfileId, String deviceIdentification, String deviceDescription, String cactiHostTemplate, String monarchHostProfile, Date timestamp) {
        if (deviceIdentification == null) {
            throw new IllegalArgumentException("deviceIdentification must be specified");
        }
        this.deviceTemplateProfileId = deviceTemplateProfileId;
        this.deviceIdentification = deviceIdentification;
        this.deviceDescription = deviceDescription;
        this.cactiHostTemplate = cactiHostTemplate;
        this.monarchHostProfile = monarchHostProfile;
        this.timestamp = timestamp;
    }

    /**
     * Default DeviceTemplateProfile constructor.
     */
    public DtoDeviceTemplateProfile() {
    }

    /**
     * toString Object protocol implementation.
     *
     * @return DeviceTemplateProfile as String
     */
    @Override
    public String toString() {
        return String.format(getClass().getName()+"@%x[deviceTemplateProfileId=%d,deviceIdentification=%s,deviceDescritpion=%s,cactiHostTemplate=%s,monarchHostProfile=%s,timestamp=%s]",
                System.identityHashCode(this), deviceTemplateProfileId, deviceIdentification, deviceDescription, cactiHostTemplate, monarchHostProfile, timestamp);
    }

    public Integer getDeviceTemplateProfileId() {
        return deviceTemplateProfileId;
    }

    public void setDeviceTemplateProfileId(Integer deviceTemplateProfileId) {
        this.deviceTemplateProfileId = deviceTemplateProfileId;
    }

    public String getDeviceIdentification() {
        return deviceIdentification;
    }

    public void setDeviceIdentification(String deviceIdentification) {
        this.deviceIdentification = deviceIdentification;
    }

    public String getDeviceDescription() {
        return deviceDescription;
    }

    public void setDeviceDescription(String deviceDescription) {
        this.deviceDescription = deviceDescription;
    }

    public String getCactiHostTemplate() {
        return cactiHostTemplate;
    }

    public void setCactiHostTemplate(String cactiHostTemplate) {
        this.cactiHostTemplate = cactiHostTemplate;
    }

    public String getMonarchHostProfile() {
        return monarchHostProfile;
    }

    public void setMonarchHostProfile(String monarchHostProfile) {
        this.monarchHostProfile = monarchHostProfile;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
