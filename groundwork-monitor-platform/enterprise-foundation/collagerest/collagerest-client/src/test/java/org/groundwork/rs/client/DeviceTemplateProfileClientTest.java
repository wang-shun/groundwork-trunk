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

package org.groundwork.rs.client;

import org.groundwork.rs.dto.DtoDeviceTemplateProfile;
import org.groundwork.rs.dto.DtoDeviceTemplateProfileList;
import org.groundwork.rs.dto.DtoOperationResults;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * DeviceTemplateProfileClientTest
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class DeviceTemplateProfileClientTest extends AbstractClientTest {

    @Test
    public void testDeviceTemplateProfileClient() throws Exception {
        if (serverDown) return;
        DeviceTemplateProfileClient client = new DeviceTemplateProfileClient(getDeploymentURL());

        // test client using XML
        client.setMediaType(MediaType.APPLICATION_XML_TYPE);
        doTestDeviceTemplateProfileClient(client);

        // test client using JSON
        client.setMediaType(MediaType.APPLICATION_JSON_TYPE);
        doTestDeviceTemplateProfileClient(client);
    }

    private void doTestDeviceTemplateProfileClient(DeviceTemplateProfileClient client) throws Exception {
        String deviceTemplateProfileDeviceIdentification0 = null;
        String deviceTemplateProfileDeviceIdentification1 = null;
        String deviceTemplateProfileDeviceIdentification2 = null;
        try {
            // post client DeviceTemplateProfiles
            DtoDeviceTemplateProfile deviceTemplateProfile0 = new DtoDeviceTemplateProfile("test-device-identification-0", "test device identification 0");
            DtoDeviceTemplateProfile deviceTemplateProfile1 = new DtoDeviceTemplateProfile("test-device-identification-1", "test device identification 1");
            deviceTemplateProfile1.setCactiHostTemplate("cacti-host-template");
            DtoDeviceTemplateProfile deviceTemplateProfile2 = new DtoDeviceTemplateProfile("test-device-identification-2", "test device identification 2");
            deviceTemplateProfile2.setMonarchHostProfile("monarch-host-profile");
            DtoDeviceTemplateProfileList deviceTemplateProfiles = new DtoDeviceTemplateProfileList();
            deviceTemplateProfiles.add(deviceTemplateProfile0);
            deviceTemplateProfiles.add(deviceTemplateProfile1);
            deviceTemplateProfiles.add(deviceTemplateProfile2);
            DtoOperationResults insertResults = client.post(deviceTemplateProfiles);
            assertNotNull(insertResults);
            assertEquals("DeviceTemplateProfile", insertResults.getEntityType());
            assertEquals("Insert", insertResults.getOperation());
            assertNotNull(insertResults.getSuccessful());
            assertEquals(3, insertResults.getSuccessful().intValue());
            assertNotNull(insertResults.getResults());
            assertEquals(3, insertResults.getResults().size());
            deviceTemplateProfileDeviceIdentification0 = "test-device-identification-0";
            deviceTemplateProfileDeviceIdentification1 = "test-device-identification-1";
            deviceTemplateProfileDeviceIdentification2 = "test-device-identification-2";

            // lookup DeviceTemplateProfiles
            DtoDeviceTemplateProfile readDeviceTemplateProfile0 = client.lookup("test-device-identification-0");
            assertNotNull(readDeviceTemplateProfile0);
            assertNotNull(readDeviceTemplateProfile0.getDeviceTemplateProfileId());
            assertEquals("test-device-identification-0", readDeviceTemplateProfile0.getDeviceIdentification());
            assertEquals("test device identification 0", readDeviceTemplateProfile0.getDeviceDescription());
            assertNull(readDeviceTemplateProfile0.getCactiHostTemplate());
            assertNull(readDeviceTemplateProfile0.getMonarchHostProfile());
            assertNotNull(readDeviceTemplateProfile0.getTimestamp());
            DtoDeviceTemplateProfile readDeviceTemplateProfile1 = client.lookup("test-device-identification-1");
            assertNotNull(readDeviceTemplateProfile1);
            assertNotNull(readDeviceTemplateProfile1.getDeviceTemplateProfileId());
            assertEquals("test-device-identification-1", readDeviceTemplateProfile1.getDeviceIdentification());
            assertEquals("test device identification 1", readDeviceTemplateProfile1.getDeviceDescription());
            assertEquals("cacti-host-template", readDeviceTemplateProfile1.getCactiHostTemplate());
            assertNull(readDeviceTemplateProfile1.getMonarchHostProfile());
            assertNotNull(readDeviceTemplateProfile1.getTimestamp());
            DtoDeviceTemplateProfile readDeviceTemplateProfile2 = client.lookup("test-device-identification-2");
            assertNotNull(readDeviceTemplateProfile2);
            assertNotNull(readDeviceTemplateProfile2.getDeviceTemplateProfileId());
            assertEquals("test-device-identification-2", readDeviceTemplateProfile2.getDeviceIdentification());
            assertEquals("test device identification 2", readDeviceTemplateProfile2.getDeviceDescription());
            assertNull(readDeviceTemplateProfile2.getCactiHostTemplate());
            assertEquals("monarch-host-profile", readDeviceTemplateProfile2.getMonarchHostProfile());
            assertNotNull(readDeviceTemplateProfile2.getTimestamp());

            // query DeviceTemplateProfiles
            List<DtoDeviceTemplateProfile> listResults = client.list(0, 2);
            assertNotNull(listResults);
            assertEquals(2, listResults.size());
            List<DtoDeviceTemplateProfile> listAllResults = client.list();
            assertNotNull(listAllResults);
            assertTrue(listAllResults.size() >= 3);
            int testsFound = 0;
            for (DtoDeviceTemplateProfile deviceTemplateProfile : listAllResults) {
                if (deviceTemplateProfile.getDeviceIdentification().startsWith("test-device-identification-")) {
                    testsFound++;
                }
            }
            assertEquals(3, testsFound);
            List<DtoDeviceTemplateProfile> queryResults = client.query("deviceIdentification like 'test-device-identification-%' ORDER BY deviceIdentification");
            assertNotNull(queryResults);
            assertEquals(3, queryResults.size());
            List<DtoDeviceTemplateProfile> queryPageResults = client.query("deviceIdentification like 'test-device-identification-%' ORDER BY deviceIdentification", 1, 1);
            assertNotNull(queryPageResults);
            assertEquals(1, queryPageResults.size());
            assertEquals("test-device-identification-1", queryPageResults.get(0).getDeviceIdentification());

            // update DeviceTemplateProfiles
            Date readDeviceTemplateProfile1Timestamp = readDeviceTemplateProfile1.getTimestamp();
            DtoDeviceTemplateProfileList updateDeviceTemplateProfiles = new DtoDeviceTemplateProfileList();
            DtoDeviceTemplateProfile updateDeviceTemplateProfile1 = new DtoDeviceTemplateProfile();
            updateDeviceTemplateProfile1.setDeviceTemplateProfileId(readDeviceTemplateProfile1.getDeviceTemplateProfileId());
            updateDeviceTemplateProfile1.setDeviceIdentification("test-device-identification-1-changed");
            updateDeviceTemplateProfiles.add(updateDeviceTemplateProfile1);
            DtoOperationResults updateResults = client.post(updateDeviceTemplateProfiles);
            assertNotNull(updateResults);
            assertEquals("DeviceTemplateProfile", updateResults.getEntityType());
            assertEquals("Update", updateResults.getOperation());
            assertNotNull(updateResults.getSuccessful());
            assertEquals(1, updateResults.getSuccessful().intValue());
            readDeviceTemplateProfile1 = client.lookup("test-device-identification-1-changed");
            assertNotNull(readDeviceTemplateProfile1);
            assertNotNull(readDeviceTemplateProfile1.getDeviceTemplateProfileId());
            assertEquals("test-device-identification-1-changed", readDeviceTemplateProfile1.getDeviceIdentification());
            deviceTemplateProfileDeviceIdentification1 = readDeviceTemplateProfile1.getDeviceIdentification();
            assertEquals("test device identification 1", readDeviceTemplateProfile1.getDeviceDescription());
            assertEquals("cacti-host-template", readDeviceTemplateProfile1.getCactiHostTemplate());
            assertNull(readDeviceTemplateProfile1.getMonarchHostProfile());
            assertNotNull(readDeviceTemplateProfile1.getTimestamp());
            assertTrue(readDeviceTemplateProfile1Timestamp.before(readDeviceTemplateProfile1.getTimestamp()));
            readDeviceTemplateProfile1Timestamp = readDeviceTemplateProfile1.getTimestamp();
            updateDeviceTemplateProfile1.setDeviceTemplateProfileId(null);
            updateDeviceTemplateProfile1.setDeviceIdentification("test-device-identification-1-changed");
            updateDeviceTemplateProfile1.setDeviceDescription("test device identification 1 changed");
            updateDeviceTemplateProfile1.setMonarchHostProfile("monarch-host-profile");
            updateResults = client.post(updateDeviceTemplateProfiles);
            assertNotNull(updateResults);
            assertEquals("DeviceTemplateProfile", updateResults.getEntityType());
            assertEquals("Update", updateResults.getOperation());
            assertNotNull(updateResults.getSuccessful());
            assertEquals(1, updateResults.getSuccessful().intValue());
            readDeviceTemplateProfile1 = client.lookup("test-device-identification-1-changed");
            assertNotNull(readDeviceTemplateProfile1);
            assertNotNull(readDeviceTemplateProfile1.getDeviceTemplateProfileId());
            assertEquals("test-device-identification-1-changed", readDeviceTemplateProfile1.getDeviceIdentification());
            assertEquals("test device identification 1 changed", readDeviceTemplateProfile1.getDeviceDescription());
            assertNull(readDeviceTemplateProfile1.getCactiHostTemplate());
            assertEquals("monarch-host-profile", readDeviceTemplateProfile1.getMonarchHostProfile());
            assertNotNull(readDeviceTemplateProfile1.getTimestamp());
            assertTrue(readDeviceTemplateProfile1Timestamp.before(readDeviceTemplateProfile1.getTimestamp()));
            Date readDeviceTemplateProfile2Timestamp = readDeviceTemplateProfile1.getTimestamp();
            DtoOperationResults clearResults = client.clear("test-device-identification-2");
            assertNotNull(clearResults);
            assertEquals("DeviceTemplateProfile", clearResults.getEntityType());
            assertEquals("Clear", clearResults.getOperation());
            assertNotNull(clearResults.getSuccessful());
            assertEquals(1, clearResults.getSuccessful().intValue());
            readDeviceTemplateProfile2 = client.lookup("test-device-identification-2");
            assertNotNull(readDeviceTemplateProfile2);
            assertNotNull(readDeviceTemplateProfile2.getDeviceTemplateProfileId());
            assertEquals("test-device-identification-2", readDeviceTemplateProfile2.getDeviceIdentification());
            assertNull(readDeviceTemplateProfile2.getCactiHostTemplate());
            assertNull(readDeviceTemplateProfile2.getMonarchHostProfile());
            assertTrue(readDeviceTemplateProfile2Timestamp.before(readDeviceTemplateProfile2.getTimestamp()));

            // delete DeviceTemplateProfiles
            DtoOperationResults deleteResults = client.delete(readDeviceTemplateProfile0.getDeviceIdentification());
            assertNotNull(deleteResults);
            assertEquals("DeviceTemplateProfile", deleteResults.getEntityType());
            assertEquals("Delete", deleteResults.getOperation());
            assertNotNull(deleteResults.getSuccessful());
            assertEquals(1, deleteResults.getSuccessful().intValue());
            readDeviceTemplateProfile0 = client.lookup(readDeviceTemplateProfile0.getDeviceIdentification());
            assertNull(readDeviceTemplateProfile0);
            deviceTemplateProfileDeviceIdentification0 = null;
            DtoDeviceTemplateProfileList deleteDeviceTemplateProfiles = new DtoDeviceTemplateProfileList();
            deleteDeviceTemplateProfiles.add(readDeviceTemplateProfile1);
            deleteDeviceTemplateProfiles.add(readDeviceTemplateProfile2);
            deleteResults = client.delete(deleteDeviceTemplateProfiles);
            assertNotNull(deleteResults);
            assertEquals("DeviceTemplateProfile", deleteResults.getEntityType());
            assertEquals("Delete", deleteResults.getOperation());
            assertNotNull(deleteResults.getSuccessful());
            assertEquals(2, deleteResults.getSuccessful().intValue());
            readDeviceTemplateProfile1 = client.lookup(readDeviceTemplateProfile1.getDeviceIdentification());
            assertNull(readDeviceTemplateProfile1);
            readDeviceTemplateProfile2 = client.lookup(readDeviceTemplateProfile2.getDeviceIdentification());
            assertNull(readDeviceTemplateProfile2);
            deviceTemplateProfileDeviceIdentification1 = null;
            deviceTemplateProfileDeviceIdentification2 = null;

            // test warning for missing delete
            deleteResults = client.delete(Arrays.asList(new String[]{"NotADeviceTemplateProfile"}));
            assertNotNull(deleteResults);
            assertEquals(new Integer(1), deleteResults.getWarning());
        } finally {
            // cleanup test
            if (deviceTemplateProfileDeviceIdentification0 != null) {
                client.delete(deviceTemplateProfileDeviceIdentification0);
            }
            if (deviceTemplateProfileDeviceIdentification1 != null) {
                client.delete(deviceTemplateProfileDeviceIdentification1);
            }
            if (deviceTemplateProfileDeviceIdentification2 != null) {
                client.delete(deviceTemplateProfileDeviceIdentification2);
            }
        }
    }
}
