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

package org.groundwork.foundation.bs.devicetemplateprofile;

import com.groundwork.collage.model.DeviceTemplateProfile;
import org.groundwork.foundation.bs.BusinessService;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.groundwork.foundation.dao.SortCriteria;

import java.util.Collection;
import java.util.List;

/**
 * DeviceTemplateProfileService
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public interface DeviceTemplateProfileService extends BusinessService {

    /**
     * General query by criteria API for DeviceTemplateProfile instances.
     *
     * @param filterCriteria filter criteria
     * @param sortCriteria optional sort criteria or null
     * @param firstResult index of first result to return
     * @param maxResults number of results to return
     * @return DeviceTemplateProfile query results
     * @throws BusinessServiceException
     */
    FoundationQueryList getDeviceTemplateProfiles(FilterCriteria filterCriteria, SortCriteria sortCriteria, int firstResult, int maxResults) throws BusinessServiceException;

    /**
     * General query by HQL API for DeviceTemplateProfile instances.
     *
     * @param hqlQuery HQL query string
     * @param hqlCountQuery HQL count query string
     * @param firstResult index of first result to return
     * @param maxResults number of results to return
     * @return DeviceTemplateProfile query results
     * @throws BusinessServiceException
     */
    FoundationQueryList queryDeviceTemplateProfiles(String hqlQuery, String hqlCountQuery, int firstResult, int maxResults) throws BusinessServiceException;

    /**
     * Get DeviceTemplateProfile instance device identifications.
     *
     * @return collection of device identifications
     * @throws BusinessServiceException
     */
    Collection<String> getDeviceIdentifications() throws BusinessServiceException;

    /**
     * Get DeviceTemplateProfile instance by device identification
     *
     * @param deviceIdentification device identification to match
     * @return matched DeviceTemplateProfile or null
     * @throws BusinessServiceException
     */
    DeviceTemplateProfile getDeviceTemplateProfileByDeviceIdentification(String deviceIdentification) throws BusinessServiceException;

    /**
     * Get DeviceTemplateProfile instance by primary id.
     *
     * @param id primary id
     * @return matched DeviceTemplateProfile or null
     * @throws BusinessServiceException
     */
    DeviceTemplateProfile getDeviceTemplateProfileById(int id) throws BusinessServiceException;

    /**
     * Get DeviceTemplateProfile instances that match a list of device identifications.
     *
     * @param deviceIdentifications device identifications
     * @return collection of matched DeviceTemplateProfiles
     * @throws BusinessServiceException
     */
    Collection<DeviceTemplateProfile> getDeviceTemplateProfilesByDeviceIdentifications(Collection<String> deviceIdentifications) throws BusinessServiceException;

    /**
     * Create DeviceTemplateProfile instance with device identification.
     *
     * @param deviceIdentification device identification
     * @return created DeviceTemplateProfile instance
     * @throws BusinessServiceException
     */
    DeviceTemplateProfile createDeviceTemplateProfile(String deviceIdentification) throws BusinessServiceException;

    /**
     * Create DeviceTemplateProfile instance with device identification and description.
     *
     * @param deviceIdentification device identification
     * @param deviceDescription device description
     * @return created DeviceTemplateProfile instance
     * @throws BusinessServiceException
     */
    DeviceTemplateProfile createDeviceTemplateProfile(String deviceIdentification, String deviceDescription) throws BusinessServiceException;

    /**
     * Save DeviceTemplateProfile instance.
     *
     * @param deviceTemplateProfile DeviceTemplateProfile instance
     * @throws BusinessServiceException
     */
    void saveDeviceTemplateProfile(DeviceTemplateProfile deviceTemplateProfile) throws BusinessServiceException;

    /**
     * Save DeviceTemplateProfile instances.
     *
     * @param deviceTemplateProfiles DeviceTemplateProfile instances
     * @throws BusinessServiceException
     */
    void saveDeviceTemplateProfiles(List<DeviceTemplateProfile> deviceTemplateProfiles) throws BusinessServiceException;

    /**
     * Delete DeviceTemplateProfile instance by id.
     *
     * @param id primary id to delete
     * @throws BusinessServiceException
     */
    void deleteDeviceTemplateProfileById(int id) throws BusinessServiceException;

    /**
     * Delete DeviceTemplateProfile instance by device identification.
     *
     * @param deviceIdentification device identification to delete
     * @return deleted status
     * @throws BusinessServiceException
     */
    boolean deleteDeviceTemplateProfileByDeviceIdentification(String deviceIdentification) throws BusinessServiceException;

    /**
     * Delete DeviceTemplateProfile instance.
     *
     * @param deviceTemplateProfile DeviceTemplateProfile instance
     * @throws BusinessServiceException
     */
    void deleteDeviceTemplateProfile(DeviceTemplateProfile deviceTemplateProfile) throws BusinessServiceException;

    /**
     * Delete DeviceTemplateProfile instances.
     *
     * @param deviceTemplateProfiles DeviceTemplateProfile instances
     * @throws BusinessServiceException
     */
    void deleteDeviceTemplateProfiles(List<DeviceTemplateProfile> deviceTemplateProfiles) throws BusinessServiceException;
}
