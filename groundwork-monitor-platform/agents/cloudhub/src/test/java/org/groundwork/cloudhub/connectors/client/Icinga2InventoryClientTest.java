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

package org.groundwork.cloudhub.connectors.client;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.groundwork.cloudhub.connectors.icinga2.Icinga2MonitorInventory;
import org.groundwork.cloudhub.connectors.icinga2.client.Icinga2InventoryClient;
import org.groundwork.cloudhub.connectors.icinga2.client.Icinga2ObjectMapper;
import org.groundwork.cloudhub.inventory.MonitorInventoryDifference;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoHostGroup;
import org.groundwork.rs.dto.DtoService;
import org.groundwork.rs.dto.DtoServiceGroup;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

/**
 * Icinga2InventoryClientTest
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class Icinga2InventoryClientTest {

    private static Logger log = Logger.getLogger(Icinga2InventoryClientTest.class);

    private String host = "demo70.groundwork.groundworkopensource.com";
    private int port = 5665;
    private String user = "root";
    private String password = "fc764746b29dfa82";
    private boolean trustAllSSL = true;

    private ObjectMapper jsonObjectMapper = new Icinga2ObjectMapper();
    private Icinga2MonitorInventory monitorInventory = new Icinga2MonitorInventory(null, null, null);
    private Icinga2MonitorInventory monitorInventory2 = new Icinga2MonitorInventory(null, null, null);

    @Test
    public void testIcinga2InventoryClient() throws Exception {
        log.debug("Create inventory client...");
        Icinga2InventoryClient client = new Icinga2InventoryClient(host, port, user, password, null, null, null, trustAllSSL);

        log.debug("Execute inventory status query...");
        JsonNode jsonStatus = client.getStatus();
        assert jsonStatus != null;
        log.debug("JSON status: " + jsonObjectMapper.writeValueAsString(jsonStatus));
        String [] version = new String[1];
        assert client.checkStatus(version);
        assert version[0] != null;

        log.debug("Execute inventory host queries...");
        List<JsonNode> jsonHosts = client.getHosts();
        assert jsonHosts != null;
        assert !jsonHosts.isEmpty();
        log.debug("JSON host: " + jsonObjectMapper.writeValueAsString(jsonHosts.get(0)));
        assert jsonHosts.get(0).has("attrs");
        assert jsonHosts.get(0).get("attrs").isObject();
        assert jsonHosts.get(0).get("attrs").has("name");
        String hostName = jsonHosts.get(0).get("attrs").get("name").asText();
        JsonNode jsonHost = client.getHost(hostName);
        assert jsonHost != null;
        assert jsonHost.has("attrs");
        assert jsonHost.get("attrs").isObject();
        assert jsonHost.get("attrs").has("name");
        assert jsonHost.get("attrs").get("name").asText().equals(hostName);

        for (JsonNode addJsonHost : jsonHosts) {
            monitorInventory.addHost(addJsonHost, null);
            monitorInventory2.addHost(addJsonHost, null);
        }
        DtoHost dtoHost = monitorInventory.getHosts().get(hostName);
        assert dtoHost != null;
        assert dtoHost.getHostName() != null;
        assert dtoHost.getHostName().equals(hostName);

        log.debug("Execute inventory hostgroup queries...");
        List<JsonNode> jsonHostGroups = client.getHostGroups();
        assert jsonHostGroups != null;
        assert !jsonHostGroups.isEmpty();
        log.debug("JSON hostgroup: " + jsonObjectMapper.writeValueAsString(jsonHostGroups.get(0)));
        assert jsonHostGroups.get(0).has("attrs");
        assert jsonHostGroups.get(0).get("attrs").isObject();
        assert jsonHostGroups.get(0).get("attrs").has("name");
        String hostGroupName = jsonHostGroups.get(0).get("attrs").get("name").asText();
        JsonNode jsonHostGroup = client.getHostGroup(hostGroupName);
        assert jsonHostGroup != null;
        assert jsonHostGroup.has("attrs");
        assert jsonHostGroup.get("attrs").isObject();
        assert jsonHostGroup.get("attrs").has("name");
        assert jsonHostGroup.get("attrs").get("name").asText().equals(hostGroupName);

        for (JsonNode addJsonHostGroup : jsonHostGroups) {
            monitorInventory.addHostGroup(addJsonHostGroup);
            monitorInventory2.addHostGroup(addJsonHostGroup);
        }
        DtoHostGroup dtoHostGroup = monitorInventory.getHostGroups().get(hostGroupName);
        assert dtoHostGroup != null;
        assert dtoHostGroup.getName() != null;
        assert dtoHostGroup.getName().equals(hostGroupName);

        log.debug("Execute inventory service queries...");
        List<JsonNode> jsonServices = client.getServices();
        assert jsonServices != null;
        assert !jsonServices.isEmpty();
        log.debug("JSON service: " + jsonObjectMapper.writeValueAsString(jsonServices.get(0)));
        assert jsonServices.get(0).has("attrs");
        assert jsonServices.get(0).get("attrs").isObject();
        assert jsonServices.get(0).get("attrs").has("host_name");
        hostName = jsonServices.get(0).get("attrs").get("host_name").asText();
        assert jsonServices.get(0).get("attrs").has("name");
        String serviceDescription = jsonServices.get(0).get("attrs").get("name").asText();
        JsonNode jsonService = client.getService(hostName, serviceDescription);
        assert jsonService != null;
        assert jsonService.has("attrs");
        assert jsonService.get("attrs").isObject();
        assert jsonService.get("attrs").has("host_name");
        assert jsonService.get("attrs").get("host_name").asText().equals(hostName);
        assert jsonService.get("attrs").has("name");
        assert jsonService.get("attrs").get("name").asText().equals(serviceDescription);

        for (JsonNode addJsonService : jsonServices) {
            monitorInventory.addService(addJsonService);
            monitorInventory2.addService(addJsonService);
        }
        String serviceInventoryName = hostName + "!" + serviceDescription;
        if (monitorInventory.getSyntheticServiceMappings().containsKey(serviceInventoryName)) {
            serviceInventoryName = monitorInventory.getSyntheticServiceMappings().get(serviceInventoryName).iterator().next();
        }
        DtoService dtoService = monitorInventory.getServices().get(serviceInventoryName);
        assert dtoService != null;
        assert dtoService.getHostName() != null;
        assert dtoService.getHostName().equals(hostName);
        assert dtoService.getDescription() != null;
        assert dtoService.getDescription().startsWith(serviceDescription);

        log.debug("Execute inventory servicegroup queries...");
        List<JsonNode> jsonServiceGroups = client.getServiceGroups();
        assert jsonServiceGroups != null;
        assert !jsonServiceGroups.isEmpty();
        log.debug("JSON servicegroup: " + jsonObjectMapper.writeValueAsString(jsonServiceGroups.get(0)));
        assert jsonServiceGroups.get(0).has("attrs");
        assert jsonServiceGroups.get(0).get("attrs").isObject();
        assert jsonServiceGroups.get(0).get("attrs").has("name");
        String serviceGroupName = jsonServiceGroups.get(0).get("attrs").get("name").asText();
        JsonNode jsonServiceGroup = client.getServiceGroup(serviceGroupName);
        assert jsonServiceGroup != null;
        assert jsonServiceGroup.has("attrs");
        assert jsonServiceGroup.get("attrs").isObject();
        assert jsonServiceGroup.get("attrs").has("name");
        assert jsonServiceGroup.get("attrs").get("name").asText().equals(serviceGroupName);

        for (JsonNode addJsonServiceGroup : jsonServiceGroups) {
            monitorInventory.addServiceGroup(addJsonServiceGroup);
            monitorInventory2.addServiceGroup(addJsonServiceGroup);
        }
        DtoServiceGroup dtoServiceGroup = monitorInventory.getServiceGroups().get(serviceGroupName);
        assert dtoServiceGroup != null;
        assert dtoServiceGroup.getName() != null;
        assert dtoServiceGroup.getName().equals(serviceGroupName);

        log.debug("Execute inventory comment queries...");
        List<JsonNode> jsonComments = client.getComments();
        assert jsonComments != null;
        if (!jsonComments.isEmpty()) {
            log.debug("JSON comment: " + jsonObjectMapper.writeValueAsString(jsonComments.get(0)));
            assert jsonComments.get(0).has("name");
            String commentName = jsonComments.get(0).get("name").asText();
            JsonNode jsonComment = client.getComment(commentName);
            assert jsonComment != null;
            assert jsonComment.has("name");
            assert jsonComment.get("name").asText().equals(commentName);
        }

        for (JsonNode addJsonComment : jsonComments) {
            monitorInventory.addComment(addJsonComment);
            monitorInventory2.addComment(addJsonComment);
        }

        log.debug("Test inventory differences...");
        Collection<MonitorInventoryDifference.Difference> difference =
                MonitorInventoryDifference.difference(monitorInventory, monitorInventory2);
        assert difference != null;
        assert difference.isEmpty();
    }
}
