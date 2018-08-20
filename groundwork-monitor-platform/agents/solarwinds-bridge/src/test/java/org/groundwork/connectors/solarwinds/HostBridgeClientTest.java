package org.groundwork.connectors.solarwinds;

import org.groundwork.connectors.solarwinds.status.MonitorProperty;
import org.groundwork.rs.client.HostClient;
import org.groundwork.rs.client.HostGroupClient;
import org.groundwork.rs.dto.DtoDepthType;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoHostGroup;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.junit.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class HostBridgeClientTest extends AbstractBridgeClientTest {

    @Test
    public void testPost() throws Exception {
        ClientRequest request = new ClientRequest(getApiUrl() + "/hosts");
        request.formParameter("Host", "newHost");
        request.formParameter("IP", "127.0.0.1");
        request.formParameter("State", "Up");
        request.formParameter("Message", "Greetings Earthlings");
        request.formParameter("Perf", "Performance data 1024");
        request.formParameter("TimeStamp", "07/05/2014 02:46 PM");
        request.formParameter("AgentID", "solar-22");
        request.formParameter("Hostgroups", "HG1,HG2, HG3");
        ClientResponse response = request.post();
        assert response.getResponseStatus().getStatusCode() == 200;
        assert response.getEntity(String.class).toString().startsWith("OK");

        HostClient hostClient = new HostClient(REST_API_URL);
        DtoHost host = hostClient.lookup("newHost", DtoDepthType.Deep);
        assert host != null;
        assert host.getAppType().equals(SolarWindsConfiguration.instance().getAppType());
        assert host.getHostName().equals("newHost");
        assert host.getDeviceIdentification().equals("127.0.0.1");
        assert host.getMonitorStatus().equals("UP");
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(host.getLastCheckTime());
        assert calendar.get(Calendar.MONTH) == 6;
        assert calendar.get(Calendar.DAY_OF_MONTH) == 5;
        assert calendar.get(Calendar.YEAR) == 2014;
        assert calendar.get(Calendar.HOUR_OF_DAY) == 14;
        assert calendar.get(Calendar.MINUTE) == 46;
        assert host.getProperty(MonitorProperty.LastPluginOutput.value()).equals("Greetings Earthlings, SW_Status=Up");
        assert host.getProperty(MonitorProperty.PerformanceData.value()).equals("Performance data 1024");
        assert host.getAgentId().equals("solar-22");
        assert host.getHostGroups().size() == 4;

        HostGroupClient hostGroupClient = new HostGroupClient(REST_API_URL);
        DtoHostGroup hostGroup = hostGroupClient.lookup("HG1");
        assert hostGroup != null;
        assert hostGroup.getName().equals("HG1");
        assert hostGroup.getHosts().size() == 1;
        assert hostGroup.getHosts().get(0).getHostName().equals("newHost");
        assert hostGroup.getAgentId().equals("solar-22");
        hostGroup = hostGroupClient.lookup("HG2");
        assert hostGroup != null;
        assert hostGroup.getHosts().size() == 1;
        assert hostGroup.getName().equals("HG2");
        assert hostGroup.getHosts().get(0).getHostName().equals("newHost");
        assert hostGroup.getAgentId().equals("solar-22");
        hostGroup = hostGroupClient.lookup("HG3");
        assert hostGroup != null;
        assert hostGroup.getHosts().size() == 1;
        assert hostGroup.getName().equals("HG3");
        assert hostGroup.getAgentId().equals("solar-22");
        assert hostGroup.getHosts().get(0).getHostName().equals("newHost");
        hostGroup = hostGroupClient.lookup("Solarwinds");
        assert hostGroup != null;
        assert hostGroup.getHosts().size() == 1;
        assert hostGroup.getName().equals("Solarwinds");
        assert hostGroup.getAgentId().equals("solar-22");
        assert hostGroup.getHosts().get(0).getHostName().equals("newHost");

        hostClient.delete("newHost");
        assert hostClient.lookup("newHost") == null;

        hostGroupClient.delete("HG1");
        hostGroupClient.delete("HG2");
        hostGroupClient.delete("HG3");
        hostGroupClient.delete("Solarwinds");

        assert hostGroupClient.lookup("HG1") == null;
        assert hostGroupClient.lookup("HG1") == null;
        assert hostGroupClient.lookup("HG1") == null;
        assert hostGroupClient.lookup("Solarwinds") == null;

    }


}
