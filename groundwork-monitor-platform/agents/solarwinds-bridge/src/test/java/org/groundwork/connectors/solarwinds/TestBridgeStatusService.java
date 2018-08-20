package org.groundwork.connectors.solarwinds;

import org.groundwork.connectors.solarwinds.monitor.BridgeStatusService;

public class TestBridgeStatusService {

    //@Test
    public void testThread() throws Exception {
        BridgeStatusService.startHeartbeat(10, "test");
        System.in.read();
    }
}
