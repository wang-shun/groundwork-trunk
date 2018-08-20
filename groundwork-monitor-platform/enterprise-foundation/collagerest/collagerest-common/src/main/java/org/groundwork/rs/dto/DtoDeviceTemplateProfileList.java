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

import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * DtoDeviceTemplateProfileList
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@XmlRootElement(name="deviceTemplateProfiles")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoDeviceTemplateProfileList {

    @XmlElement(name="deviceTemplateProfile")
    @JsonProperty("deviceTemplateProfiles")
    private List<DtoDeviceTemplateProfile> deviceTemplateProfiles = new ArrayList<DtoDeviceTemplateProfile>();

    /**
     * Shallow copy constructor.
     *
     * @param deviceTemplateProfiles DeviceTemplateProfiles instances to copy.
     */
    public DtoDeviceTemplateProfileList(Collection<DtoDeviceTemplateProfile> deviceTemplateProfiles) {
        this.deviceTemplateProfiles.addAll(deviceTemplateProfiles);
    }

    /**
     * Default constructor.
     */
    public DtoDeviceTemplateProfileList() {
    }

    /**
     * Add device template profile instance to device template profiles list.
     *
     * @param deviceTemplateProfile device template profile instance to add
     */
    public void add(DtoDeviceTemplateProfile deviceTemplateProfile) {
        deviceTemplateProfiles.add(deviceTemplateProfile);
    }

    /**
     * Get device template profiles list size.
     *
     * @return size of device template profiles list
     */
    public int size() {
        return deviceTemplateProfiles.size();
    }

    public List<DtoDeviceTemplateProfile> getDeviceTemplateProfiles() {
        return deviceTemplateProfiles;
    }
}
