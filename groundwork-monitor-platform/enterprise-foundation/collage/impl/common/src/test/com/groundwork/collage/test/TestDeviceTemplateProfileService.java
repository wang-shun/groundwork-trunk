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

package com.groundwork.collage.test;

import com.groundwork.collage.model.DeviceTemplateProfile;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.groundwork.foundation.bs.devicetemplateprofile.DeviceTemplateProfileService;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

/**
 * TestDeviceTemplateProfileService
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class TestDeviceTemplateProfileService extends AbstractTestCaseWithTransactionSupport {

    /** DeviceTemplateProfile service */
    private DeviceTemplateProfileService deviceTemplateProfileService;

    /**
     * Test constructor.
     *
     * @param test test to execute
     */
    public TestDeviceTemplateProfileService(String test) {
        super(test);
    }

    /**
     * Declare tests to be run.
     *
     * @return test suite
     */
    public static Test suite() {
        // initialize test database once per suite
        executeScript(false, "testdata/monitor-data.sql");

        // run all tests
        TestSuite suite = new TestSuite(TestDeviceTemplateProfileService.class);

        // or a subset thereof
        //TestSuite suite = new TestSuite();
        //suite.addTest(new TestMonitorServerService("testDeviceTemplateProfileServiceCRUD"));
        //suite.addTest(new TestMonitorServerService("testDeviceTemplateProfileServiceUniqueness"));
        //suite.addTest(new TestMonitorServerService("testDeviceTemplateProfileServiceQuery"));

        return suite;
    }

    /**
     * Setup test.
     *
     * @throws Exception
     */
    public void setUp() throws Exception {
        // setup test
        super.setUp();

        // initialize services
        deviceTemplateProfileService = collage.getDeviceTemplateProfileService();
        assertNotNull(deviceTemplateProfileService);
    }

    /**
     * DeviceTemplateProfile service CRUD test method.
     */
    public void testDeviceTemplateProfileServiceCRUD() throws Exception {
        DeviceTemplateProfile deviceTemplateProfile0 = null;
        DeviceTemplateProfile deviceTemplateProfile1 = null;
        DeviceTemplateProfile deviceTemplateProfile2 = null;
        try {
            // basic create tests
            deviceTemplateProfile0 = deviceTemplateProfileService.createDeviceTemplateProfile("test-device-identification-0", "test device identification 0");
            deviceTemplateProfile1 = deviceTemplateProfileService.createDeviceTemplateProfile("test-device-identification-1", "test device identification 1");
            deviceTemplateProfile1.setCactiHostTemplate("cacti-host-template");
            deviceTemplateProfile2 = deviceTemplateProfileService.createDeviceTemplateProfile("test-device-identification-2", "test device identification 2");
            deviceTemplateProfile2.setMonarchHostProfile("monarch-host-profile");
            // basic save tests
            deviceTemplateProfileService.saveDeviceTemplateProfile(deviceTemplateProfile0);
            deviceTemplateProfileService.saveDeviceTemplateProfiles(Arrays.asList(new DeviceTemplateProfile[]{deviceTemplateProfile1, deviceTemplateProfile2}));
            // basic validation test
            DeviceTemplateProfile invalidDeviceTemplateProfile = deviceTemplateProfileService.createDeviceTemplateProfile("test-device-identification-3");
            invalidDeviceTemplateProfile.setCactiHostTemplate("cacti-host-template");
            invalidDeviceTemplateProfile.setMonarchHostProfile("monarch-host-profile");
            try {
                deviceTemplateProfileService.saveDeviceTemplateProfile(invalidDeviceTemplateProfile);
                fail("Expected invalid device device template failure");
            } catch (BusinessServiceException bse) {
            }
            // basic read device identifications tests
            Collection<String> deviceIdentifications = deviceTemplateProfileService.getDeviceIdentifications();
            assertTrue(deviceIdentifications.contains("test-device-identification-0"));
            assertTrue(deviceIdentifications.contains("test-device-identification-1"));
            assertTrue(deviceIdentifications.contains("test-device-identification-2"));
            // basic read tests
            DeviceTemplateProfile readDeviceTemplateProfile0 = deviceTemplateProfileService.getDeviceTemplateProfileByDeviceIdentification("test-device-identification-0");
            assertNotNull(readDeviceTemplateProfile0);
            assertNotNull(readDeviceTemplateProfile0.getDeviceTemplateProfileId());
            assertEquals("test-device-identification-0", readDeviceTemplateProfile0.getDeviceIdentification());
            assertEquals("test device identification 0", readDeviceTemplateProfile0.getDeviceDescription());
            assertNull(readDeviceTemplateProfile0.getCactiHostTemplate());
            assertNull(readDeviceTemplateProfile0.getMonarchHostProfile());
            assertNotNull(readDeviceTemplateProfile0.getTimestamp());
            DeviceTemplateProfile readDeviceTemplateProfile1 = deviceTemplateProfileService.getDeviceTemplateProfileByDeviceIdentification("test-device-identification-1");
            assertNotNull(readDeviceTemplateProfile1);
            assertNotNull(readDeviceTemplateProfile1.getDeviceTemplateProfileId());
            assertEquals("test-device-identification-1", readDeviceTemplateProfile1.getDeviceIdentification());
            assertEquals("test device identification 1", readDeviceTemplateProfile1.getDeviceDescription());
            assertEquals("cacti-host-template", readDeviceTemplateProfile1.getCactiHostTemplate());
            assertNull(readDeviceTemplateProfile1.getMonarchHostProfile());
            assertNotNull(readDeviceTemplateProfile1.getTimestamp());
            DeviceTemplateProfile readDeviceTemplateProfile2 = deviceTemplateProfileService.getDeviceTemplateProfileByDeviceIdentification("test-device-identification-2");
            assertNotNull(readDeviceTemplateProfile2);
            assertNotNull(readDeviceTemplateProfile2.getDeviceTemplateProfileId());
            assertEquals("test-device-identification-2", readDeviceTemplateProfile2.getDeviceIdentification());
            assertEquals("test device identification 2", readDeviceTemplateProfile2.getDeviceDescription());
            assertNull(readDeviceTemplateProfile2.getCactiHostTemplate());
            assertEquals("monarch-host-profile", readDeviceTemplateProfile2.getMonarchHostProfile());
            assertNotNull(readDeviceTemplateProfile2.getTimestamp());
            readDeviceTemplateProfile0 = deviceTemplateProfileService.getDeviceTemplateProfileById(readDeviceTemplateProfile0.getDeviceTemplateProfileId());
            assertNotNull(readDeviceTemplateProfile0);
            Collection<DeviceTemplateProfile> deviceTemplateProfiles = deviceTemplateProfileService.getDeviceTemplateProfilesByDeviceIdentifications(Arrays.asList(new String[]{"test-device-identification-0", "test-device-identification-1", "test-device-identification-2"}));
            assertNotNull(deviceTemplateProfiles);
            assertEquals(3, deviceTemplateProfiles.size());
            // basic update tests
            DeviceTemplateProfile updateDeviceTemplateProfile0 = deviceTemplateProfileService.getDeviceTemplateProfileById(readDeviceTemplateProfile0.getDeviceTemplateProfileId());
            Date updateDeviceTemplateProfile0Timestamp = readDeviceTemplateProfile0.getTimestamp();
            updateDeviceTemplateProfile0.setDeviceDescription("updated test device identification 0");
            updateDeviceTemplateProfile0.setCactiHostTemplate("cacti-host-template");
            deviceTemplateProfileService.saveDeviceTemplateProfile(updateDeviceTemplateProfile0);
            DeviceTemplateProfile updatedDeviceTemplateProfile0 = deviceTemplateProfileService.getDeviceTemplateProfileByDeviceIdentification("test-device-identification-0");
            assertNotNull(updatedDeviceTemplateProfile0);
            assertEquals(readDeviceTemplateProfile0.getDeviceTemplateProfileId(), updatedDeviceTemplateProfile0.getDeviceTemplateProfileId());
            assertEquals("test-device-identification-0", updatedDeviceTemplateProfile0.getDeviceIdentification());
            assertEquals("updated test device identification 0", updatedDeviceTemplateProfile0.getDeviceDescription());
            assertEquals("cacti-host-template", updatedDeviceTemplateProfile0.getCactiHostTemplate());
            assertNull(updatedDeviceTemplateProfile0.getMonarchHostProfile());
            assertTrue(updateDeviceTemplateProfile0Timestamp.before(updatedDeviceTemplateProfile0.getTimestamp()));
            // basic delete tests
            deviceTemplateProfileService.deleteDeviceTemplateProfileById(deviceTemplateProfile0.getDeviceTemplateProfileId());
            deviceTemplateProfile0 = deviceTemplateProfileService.getDeviceTemplateProfileById(deviceTemplateProfile0.getDeviceTemplateProfileId());
            assertNull(deviceTemplateProfile0);
            deviceTemplateProfileService.deleteDeviceTemplateProfiles(Arrays.asList(new DeviceTemplateProfile[]{deviceTemplateProfile1, deviceTemplateProfile2}));
            deviceTemplateProfile1 = deviceTemplateProfileService.getDeviceTemplateProfileById(deviceTemplateProfile1.getDeviceTemplateProfileId());
            assertNull(deviceTemplateProfile1);
            deviceTemplateProfile2 = deviceTemplateProfileService.getDeviceTemplateProfileById(deviceTemplateProfile2.getDeviceTemplateProfileId());
            assertNull(deviceTemplateProfile2);
        } finally {
            // cleanup test objects
            if (deviceTemplateProfile0 != null) {
                deviceTemplateProfileService.deleteDeviceTemplateProfile(deviceTemplateProfile0);
            }
            if (deviceTemplateProfile1 != null) {
                deviceTemplateProfileService.deleteDeviceTemplateProfile(deviceTemplateProfile1);
            }
            if (deviceTemplateProfile2 != null) {
                deviceTemplateProfileService.deleteDeviceTemplateProfile(deviceTemplateProfile2);
            }
        }
    }

    /**
     * DeviceTemplateProfile service uniqueness test method.
     */
    public void testDeviceTemplateProfileServiceUniqueness() throws Exception {
        DeviceTemplateProfile deviceTemplateProfile = null;
        try {
            // create unique test DeviceTemplateProfile
            deviceTemplateProfile = deviceTemplateProfileService.createDeviceTemplateProfile("test");
            deviceTemplateProfileService.saveDeviceTemplateProfile(deviceTemplateProfile);
            // disable hibernate log4j logging
            disableHibernateLogging();
            // test device identification uniqueness
            DeviceTemplateProfile duplicateDeviceTemplateProfile = deviceTemplateProfileService.createDeviceTemplateProfile("test");
            try {
                deviceTemplateProfileService.saveDeviceTemplateProfile(duplicateDeviceTemplateProfile);
                fail("Expected duplicate device identification failure");
            } catch (DataIntegrityViolationException dive) {
            }
            // enable hibernate log4j logging
            reenableHibernateLogging();
            // remove test DeviceTemplateProfile
            deviceTemplateProfileService.deleteDeviceTemplateProfileById(deviceTemplateProfile.getDeviceTemplateProfileId());
            deviceTemplateProfile = deviceTemplateProfileService.getDeviceTemplateProfileById(deviceTemplateProfile.getDeviceTemplateProfileId());
            assertNull(deviceTemplateProfile);
        } finally {
            // cleanup test objects
            if (deviceTemplateProfile != null) {
                deviceTemplateProfileService.deleteDeviceTemplateProfile(deviceTemplateProfile);
            }
        }
    }

    /**
     * DeviceTemplateProfile service query test method.
     */
    public void testDeviceTemplateProfileServiceQuery() throws Exception {
        DeviceTemplateProfile deviceTemplateProfile = null;
        try {
            // setup test DeviceTemplateProfile
            deviceTemplateProfile = deviceTemplateProfileService.createDeviceTemplateProfile("test", "test description");
            deviceTemplateProfileService.saveDeviceTemplateProfile(deviceTemplateProfile);
            // test queries for DeviceTemplateProfile
            FilterCriteria deviceIdentificationFilterCriteria = FilterCriteria.ieq(DeviceTemplateProfile.HP_DEVICE_IDENTIFICATION, "test");
            FoundationQueryList results = deviceTemplateProfileService.getDeviceTemplateProfiles(deviceIdentificationFilterCriteria, null, 0, 1);
            assertNotNull(results);
            assertEquals(1, results.getTotalCount());
            assertEquals(1, results.size());
            assertTestDeviceTemplateProfile((DeviceTemplateProfile)results.get(0));
            results = deviceTemplateProfileService.queryDeviceTemplateProfiles("from DeviceTemplateProfile where "+DeviceTemplateProfile.HP_DEVICE_DESCRIPTION+" like '%description%'", "select count(*) from DeviceTemplateProfile", 0, 1);
            assertNotNull(results);
            assertEquals(1, results.getTotalCount());
            assertEquals(1, results.size());
            assertTestDeviceTemplateProfile((DeviceTemplateProfile)results.get(0));
            // teardown test DeviceTemplateProfile
            boolean deleted = deviceTemplateProfileService.deleteDeviceTemplateProfileByDeviceIdentification(deviceTemplateProfile.getDeviceIdentification());
            assertTrue(deleted);
            deviceTemplateProfile = deviceTemplateProfileService.getDeviceTemplateProfileById(deviceTemplateProfile.getDeviceTemplateProfileId());
            assertNull(deviceTemplateProfile);
        } finally {
            // cleanup test objects
            if (deviceTemplateProfile != null) {
                deviceTemplateProfileService.deleteDeviceTemplateProfile(deviceTemplateProfile);
            }
        }
    }

    private void assertTestDeviceTemplateProfile(DeviceTemplateProfile readDeviceTemplateProfile) {
        assertNotNull(readDeviceTemplateProfile);
        assertEquals("test", readDeviceTemplateProfile.getDeviceIdentification());
        assertEquals("test description", readDeviceTemplateProfile.getDeviceDescription());
    }
}
