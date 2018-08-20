package org.groundwork.rs.client;

import org.groundwork.rs.dto.DtoService;
import org.groundwork.rs.dto.DtoServiceList;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class AbstractServiceTest extends AbstractClientTest {

    protected DtoServiceList buildServiceUpdate() {
        DtoServiceList services = new DtoServiceList();
        DtoService service = new DtoService();
        service.setDescription("service-100");
        service.setHostName("localhost");
        service.setMonitorServer("localhost");
        service.setDeviceIdentification("127.0.0.1");
        service.setAgentId(AGENT_84);
        service.setMonitorStatus("UP");
        service.setLastHardState("UP");
        service.setAppType("VEMA");
        service.setCheckType("ACTIVE");
        service.setStateType("HARD");
        Calendar last = new GregorianCalendar(2013, Calendar.MAY, 20, 0, 0);
        Calendar next = new GregorianCalendar(2013, Calendar.MAY, 27, 0, 0);
        Calendar stateChange = new GregorianCalendar(2013, Calendar.MAY, 25, 0, 0);
        service.setLastStateChange(stateChange.getTime());
        service.setNextCheckTime(next.getTime());
        service.setLastCheckTime(last.getTime());
        service.putProperty("Latency", new Double(175.4));
        service.putProperty("ExecutionTime", new Double(200.5));
        service.putProperty("LastPluginOutput", "1.output");
        services.add(service);

        service = new DtoService();
        service.setDescription("service-101");
        service.setHostName("localhost");
        service.setMonitorServer("localhost");
        service.setDeviceIdentification("127.0.0.1");
        service.setAgentId(AGENT_85);
        service.setMonitorStatus("UP");
        service.setLastHardState("UP");
        service.setAppType("VEMA");
        service.setCheckType("ACTIVE");
        service.setStateType("HARD");
        last = new GregorianCalendar(2013, Calendar.MAY, 21, 0, 0);
        stateChange = new GregorianCalendar(2013, Calendar.MAY, 26, 0, 0);
        next = new GregorianCalendar(2013, Calendar.MAY, 28, 0, 0);
        service.setLastStateChange(stateChange.getTime());
        service.setNextCheckTime(next.getTime());
        service.setLastCheckTime(last.getTime());
        service.putProperty("Latency", new Double(275.4));
        service.putProperty("ExecutionTime", new Double(300.5));
        service.putProperty("LastPluginOutput", "2.output");
        services.add(service);
        return services;
    }

    protected void assertServiceWritten(DtoService service) {

        if (service.getDescription().equals("service-100")) {
            assertEquals("UP", service.getMonitorStatus());
            assertEquals("localhost", service.getHostName());
            assertEquals(AGENT_84, service.getAgentId());
            assertEquals("UP", service.getLastHardState());
            assertEquals("VEMA", service.getAppType());
            assertEquals("ACTIVE", service.getCheckType());
            assertEquals("HARD", service.getStateType());
            assertEquals(new Double(175.4), (Double) service.getPropertyDouble("Latency"));
            assertEquals(new Double(200.5), (Double) service.getPropertyDouble("ExecutionTime"));
            assertEquals("1.output", service.getProperty("LastPluginOutput"));
        }
        else if (service.getDescription().equals("service-101"))  {
            assertEquals("UP", service.getMonitorStatus());
            assertEquals("localhost", service.getHostName());
            assertEquals(AGENT_85, service.getAgentId());
            assertEquals("UP", service.getLastHardState());
            assertEquals("VEMA", service.getAppType());
            assertEquals("ACTIVE", service.getCheckType());
            assertEquals("HARD", service.getStateType());
            assertEquals(new Double(275.4), (Double) service.getPropertyDouble("Latency"));
            assertEquals(new Double(300.5), (Double) service.getPropertyDouble("ExecutionTime"));
            assertEquals("2.output", service.getProperty("LastPluginOutput"));
        }
        else {
            fail("service name " + service.getDescription() + " not valid");
        }
    }

}
