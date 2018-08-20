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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.EntityBusinessServiceImpl;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationDAO;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.groundwork.foundation.dao.SortCriteria;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * DeviceTemplateProfileServiceImpl
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class DeviceTemplateProfileServiceImpl extends EntityBusinessServiceImpl implements DeviceTemplateProfileService {

    private static Log log = LogFactory.getLog(DeviceTemplateProfileServiceImpl.class);

    /** Default sort criteria */
    private static final SortCriteria DEFAULT_SORT_CRITERIA = SortCriteria.asc(DeviceTemplateProfile.HP_DEVICE_IDENTIFICATION);

    /**
     * DeviceTemplateProfileServiceImpl FoundationDAO constructor.
     *
     * @param foundationDAO service Foundation DAO
     */
    public DeviceTemplateProfileServiceImpl(FoundationDAO foundationDAO) {
        super(foundationDAO, DeviceTemplateProfile.INTERFACE_NAME, DeviceTemplateProfile.COMPONENT_NAME);
    }

    @Override
    public FoundationQueryList getDeviceTemplateProfiles(FilterCriteria filterCriteria, SortCriteria sortCriteria, int firstResult, int maxResults) throws BusinessServiceException {
        sortCriteria = ((sortCriteria != null) ? sortCriteria : DEFAULT_SORT_CRITERIA);
        return query(filterCriteria, sortCriteria, firstResult, maxResults);
    }

    @Override
    public Collection<String> getDeviceIdentifications() throws BusinessServiceException {
        return _foundationDAO.query("select dtp." + DeviceTemplateProfile.HP_DEVICE_IDENTIFICATION + " from DeviceTemplateProfile dtp");
    }

    @Override
    public FoundationQueryList queryDeviceTemplateProfiles(String hqlQuery, String hqlCountQuery, int firstResult, int maxResults) throws BusinessServiceException {
        String testHqlQuery = hqlQuery.trim().toLowerCase();
        if (!(testHqlQuery.startsWith("from ") || testHqlQuery.startsWith("select "))) {
            throw new BusinessServiceException("Only DeviceTemplateProfileService HQL SELECT/FROM query supported");
        }
        String testHqlCountQuery = hqlCountQuery.trim().toLowerCase();
        if (!testHqlCountQuery.startsWith("select ") || !testHqlCountQuery.contains(" count(*) ")) {
            throw new BusinessServiceException("Only DeviceTemplateProfileService HQL SELECT count(*) query supported");
        }
        return _foundationDAO.queryWithPaging(hqlQuery, hqlCountQuery, firstResult, maxResults);
    }

    @Override
    public DeviceTemplateProfile getDeviceTemplateProfileByDeviceIdentification(String deviceIdentification) throws BusinessServiceException {
        if ((deviceIdentification == null) || (deviceIdentification.length() == 0)) {
            return null;
        }
        // query by device identification
        FilterCriteria filterCriteria = FilterCriteria.eq(DeviceTemplateProfile.HP_DEVICE_IDENTIFICATION, deviceIdentification);
        FoundationQueryList results = query(filterCriteria, null, -1, -1);
        return (((results != null) && (results.size() > 0)) ? (DeviceTemplateProfile)results.get(0) : null);
    }

    @Override
    public DeviceTemplateProfile getDeviceTemplateProfileById(int id) throws BusinessServiceException {
        // query by id
        return (DeviceTemplateProfile)queryById(id);
    }

    @Override
    public Collection<DeviceTemplateProfile> getDeviceTemplateProfilesByDeviceIdentifications(Collection<String> deviceIdentifications) throws BusinessServiceException {
        if ((deviceIdentifications == null) || deviceIdentifications.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        // query by device identifications
        FilterCriteria filterCriteria = null;
        for (String deviceIdentification : deviceIdentifications) {
            FilterCriteria deviceIdentificationFilterCriteria = FilterCriteria.eq(DeviceTemplateProfile.HP_DEVICE_IDENTIFICATION, deviceIdentification);
            if (filterCriteria == null) {
                filterCriteria = deviceIdentificationFilterCriteria;
            } else {
                filterCriteria.or(deviceIdentificationFilterCriteria);
            }
        }
        FoundationQueryList results = query(filterCriteria, DEFAULT_SORT_CRITERIA, -1, -1);
        return (((results != null) && (results.size() > 0)) ? (Collection<DeviceTemplateProfile>)results.getResults() : Collections.EMPTY_LIST);
    }

    @Override
    public DeviceTemplateProfile createDeviceTemplateProfile(String deviceIdentification) throws BusinessServiceException {
        // create and construct DeviceTemplateProfile instance
        return createDeviceTemplateProfile(deviceIdentification, null);
    }

    @Override
    public DeviceTemplateProfile createDeviceTemplateProfile(String deviceIdentification, String deviceDescription) throws BusinessServiceException {
        // create and construct DeviceTemplateProfile instance
        com.groundwork.collage.model.impl.DeviceTemplateProfile deviceTemplateProfile = (com.groundwork.collage.model.impl.DeviceTemplateProfile)create();
        deviceTemplateProfile.setDeviceIdentification(deviceIdentification);
        deviceTemplateProfile.setDeviceDescription(deviceDescription);
        return deviceTemplateProfile;
    }

    @Override
    public void saveDeviceTemplateProfile(DeviceTemplateProfile deviceTemplateProfile) throws BusinessServiceException {
        // validate device template profile
        if ((deviceTemplateProfile.getCactiHostTemplate() != null) && (deviceTemplateProfile.getMonarchHostProfile() != null)) {
            throw new BusinessServiceException("DeviceTemplateProfile with device identification "+deviceTemplateProfile.getDeviceIdentification()+" cannot be saved with both a Cacti Host Template and a Monarch Host Profile.");
        }
        // save device template profile
        deviceTemplateProfile.setTimestamp(new Date());
        save(deviceTemplateProfile);
    }

    @Override
    public void saveDeviceTemplateProfiles(List<DeviceTemplateProfile> deviceTemplateProfiles) throws BusinessServiceException {
        // validate device template profiles
        for (DeviceTemplateProfile deviceTemplateProfile : deviceTemplateProfiles) {
            if ((deviceTemplateProfile.getCactiHostTemplate() != null) && (deviceTemplateProfile.getMonarchHostProfile() != null)) {
                throw new BusinessServiceException("DeviceTemplateProfile with device identification "+deviceTemplateProfile.getDeviceIdentification()+" cannot be saved with both a Cacti Host Template and a Monarch Host Profile.");
            }
        }
        // save device template profiles
        for (DeviceTemplateProfile deviceTemplateProfile : deviceTemplateProfiles) {
            deviceTemplateProfile.setTimestamp(new Date());
        }
        save(deviceTemplateProfiles);
    }

    @Override
    public void deleteDeviceTemplateProfileById(int id) throws BusinessServiceException {
        delete(id);
    }

    @Override
    public boolean deleteDeviceTemplateProfileByDeviceIdentification(String deviceIdentification) throws BusinessServiceException {
        DeviceTemplateProfile deviceTemplateProfile = getDeviceTemplateProfileByDeviceIdentification(deviceIdentification);
        if (deviceTemplateProfile == null) {
            return false;
        }
        deleteDeviceTemplateProfile(deviceTemplateProfile);
        return true;
    }

    @Override
    public void deleteDeviceTemplateProfile(DeviceTemplateProfile deviceTemplateProfile) throws BusinessServiceException {
        delete(deviceTemplateProfile);
    }

    @Override
    public void deleteDeviceTemplateProfiles(List<DeviceTemplateProfile> deviceTemplateProfiles) throws BusinessServiceException {
        delete(deviceTemplateProfiles);
    }
}
