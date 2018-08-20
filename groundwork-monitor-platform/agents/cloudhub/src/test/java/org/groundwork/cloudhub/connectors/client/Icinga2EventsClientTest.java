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
import org.groundwork.cloudhub.connectors.icinga2.client.Icinga2EventsClient;
import org.groundwork.cloudhub.connectors.icinga2.client.Icinga2EventsClientListener;
import org.groundwork.cloudhub.connectors.icinga2.client.Icinga2InventoryClient;
import org.groundwork.cloudhub.connectors.icinga2.client.Icinga2ObjectMapper;
import org.groundwork.rs.dto.DtoEvent;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoHostNotification;
import org.groundwork.rs.dto.DtoPerfData;
import org.groundwork.rs.dto.DtoService;
import org.groundwork.rs.dto.DtoServiceNotification;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

/**
 * Icinga2EventsClientTest
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class Icinga2EventsClientTest {

    private static Logger log = Logger.getLogger(Icinga2EventsClientTest.class);

    private String host = "demo70.groundwork.groundworkopensource.com";
    private int port = 5665;
    private String user = "root";
    private String password = "fc764746b29dfa82";
    private boolean trustAllSSL = true;

    private Icinga2MonitorInventory monitorInventory = new Icinga2MonitorInventory(null, null, null);

    private ObjectMapper jsonObjectMapper = new Icinga2ObjectMapper();
    private boolean error;
    private boolean closed;
    private Icinga2EventsClientListener listener = new Icinga2EventsClientListener() {

        @Override
        public void eventReceived(JsonNode jsonEvent) {
            try {
                assert jsonEvent != null;
                log.debug("JSON event received: " + jsonObjectMapper.writeValueAsString(jsonEvent));
                Collection<Object> dtoEventInventory = monitorInventory.buildEventInventory(jsonEvent, true);
                for (Object dtoObject : dtoEventInventory) {
                    if (dtoObject instanceof DtoHost) {
                        DtoHost dtoHost = (DtoHost) dtoObject;
                        log.debug("DTO host received: " + dtoHost.getHostName() +
                                " \"" + dtoHost.getLastPlugInOutput() + "\"");
                    } else if (dtoObject instanceof DtoService) {
                        DtoService dtoService = (DtoService) dtoObject;
                        log.debug("DTO service received: " + dtoService.getHostName() + " " + dtoService.getDescription() +
                                " \"" + dtoService.getLastPlugInOutput() + "\"");
                    } else if (dtoObject instanceof DtoEvent) {
                        DtoEvent dtoEvent = (DtoEvent) dtoObject;
                        assert dtoEvent.getHost() != null;
                        assert dtoEvent.getTextMessage() != null;
                        log.debug("DTO event received: " + dtoEvent.getHost() + " " + dtoEvent.getService() +
                                " \"" + dtoEvent.getTextMessage() + "\"");
                    } else if (dtoObject instanceof DtoHostNotification) {
                        DtoHostNotification dtoHostNotification = (DtoHostNotification) dtoObject;
                        assert dtoHostNotification.getHostName() != null;
                        assert dtoHostNotification.getHostOutput() != null;
                        log.debug("DTO host notification received: " + dtoHostNotification.getHostName() +
                                " \"" + dtoHostNotification.getHostOutput() + "\"");
                    } else if (dtoObject instanceof DtoServiceNotification) {
                        DtoServiceNotification dtoServiceNotification = (DtoServiceNotification) dtoObject;
                        assert dtoServiceNotification.getHostName() != null;
                        assert dtoServiceNotification.getServiceDescription() != null;
                        assert dtoServiceNotification.getServiceOutput() != null;
                        log.debug("DTO service notification received: " + dtoServiceNotification.getHostName() + " " +
                                dtoServiceNotification.getServiceDescription() +
                                " \"" + dtoServiceNotification.getServiceOutput() + "\"");
                    } else if (dtoObject instanceof DtoPerfData) {
                        DtoPerfData dtoPerfData = (DtoPerfData) dtoObject;
                        assert dtoPerfData.getServerName() != null;
                        assert dtoPerfData.getLabel() != null;
                        log.debug("DTO performance data received: " + dtoPerfData.getServerName() + " " +
                                dtoPerfData.getServiceName() +
                                " \"" + dtoPerfData.getLabel() + "\" " +
                                dtoPerfData.getValue() + " " + dtoPerfData.getWarning() + " "  + dtoPerfData.getCritical());
                    } else {
                        assert false;
                    }
                }
            } catch (Exception e) {
                log.error("Unexpected exception: "+e, e);
                error = true;
            }
        }

        @Override
        public void closed() {
            log.debug("Events client closed.");
            closed = true;
        }
    };

    @Test
    public void testIcinga2EventsClient() throws Exception {
        log.debug("Create inventory client...");
        Icinga2InventoryClient inventoryClient = new Icinga2InventoryClient(host, port, user, password, null, null, null, trustAllSSL);
        log.debug("Execute inventory host queries...");
        List<JsonNode> jsonHosts = inventoryClient.getHosts();
        assert jsonHosts != null;
        assert !jsonHosts.isEmpty();
        for (JsonNode addJsonHost : jsonHosts) {
            monitorInventory.addHost(addJsonHost, null);
        }
        log.debug("Execute inventory host group queries...");
        List<JsonNode> jsonHostGroups = inventoryClient.getHostGroups();
        assert jsonHostGroups != null;
        assert !jsonHostGroups.isEmpty();
        for (JsonNode addJsonHostGroup : jsonHostGroups) {
            monitorInventory.addHostGroup(addJsonHostGroup);
        }
        log.debug("Execute inventory service queries...");
        List<JsonNode> jsonServices = inventoryClient.getServices();
        assert jsonServices != null;
        assert !jsonServices.isEmpty();
        for (JsonNode addJsonService : jsonServices) {
            monitorInventory.addService(addJsonService);
        }
        log.debug("Create events client...");
        Icinga2EventsClient eventsClient = new Icinga2EventsClient(host, port, user, password, null, null, null, trustAllSSL,
                "CloudHub", listener);
        log.debug("Start events client...");
        eventsClient.start();
        log.debug("Sleeping...");
        Thread.sleep(30000);
        log.debug("Stop events client...");
        eventsClient.stop();
        assert !error;
        assert closed;
    }
}
