package org.groundwork.connectors.solarwinds;

import org.groundwork.connectors.solarwinds.status.MonitorProperty;
import org.groundwork.rs.client.HostClient;
import org.groundwork.rs.client.ServiceClient;
import org.groundwork.rs.dto.DtoService;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.junit.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class ServiceBridgeClientTest extends AbstractBridgeClientTest {


    @Test
    public void testPost() throws Exception {
        ClientRequest request = new ClientRequest(getApiUrl() + "/services");
        request.formParameter("Host", "newHost");
        request.formParameter("Service", "local_disk");
        request.formParameter("IP", "127.0.0.1");
        request.formParameter("State", "Up");
        request.formParameter("Message", "Greetings Earthlings!");
        request.formParameter("Perf", "Performance data 2048");
        request.formParameter("TimeStamp", "07/05/2014 02:46 PM");
        request.formParameter("AgentID", "solar-22");
        ClientResponse response = request.post();
        assert response.getResponseStatus().getStatusCode() == 200;
        assert response.getEntity(String.class).toString().startsWith("OK");

        ServiceClient serviceClient = new ServiceClient(REST_API_URL);
        DtoService service = serviceClient.lookup("local_disk", "newHost");
        assert service != null;
        assert service.getAppType().equals(SolarWindsConfiguration.instance().getAppType());
        assert service.getDescription().equals("local_disk");
        assert service.getHostName().equals("newHost");
        assert service.getDeviceIdentification().equals("127.0.0.1");
        assert service.getMonitorStatus().equals("UP");
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(service.getLastCheckTime());
        assert calendar.get(Calendar.MONTH) == 6;
        assert calendar.get(Calendar.DAY_OF_MONTH) == 5;
        assert calendar.get(Calendar.YEAR) == 2014;
        assert calendar.get(Calendar.HOUR_OF_DAY) == 14;
        assert calendar.get(Calendar.MINUTE) == 46;
        assert service.getProperty(MonitorProperty.LastPluginOutput.value()).equals("Greetings Earthlings!, SW_Status=Up");
        assert service.getProperty(MonitorProperty.PerformanceData.value()).equals("Performance data 2048");
        assert service.getAgentId().equals("solar-22");

        assert serviceClient.lookup("local_disk", "newHost") != null;

        HostClient hostClient = new HostClient(REST_API_URL);
        hostClient.delete("newHost");
        assert serviceClient.lookup("local_disk", "newHost") == null;
        assert hostClient.lookup("newHost") == null;
    }

    @Test
    public void testFailedData() throws Exception {
        ClientRequest request = new ClientRequest(getApiUrl() + "/services");
        request.formParameter("Host", "hostname");
        request.formParameter("Service", "servicename");
        request.formParameter("IP", "127.0.0.1");
        request.formParameter("State", "Critical");
        request.formParameter("Message", "What 98%");// URLEncoder.encode("message 98", "UTF-8").replace("+", "%20"));
        request.formParameter("Perf", "Perf=1");
        request.formParameter("TimeStamp", "3/10/14 10:00:53 AM");
        request.formParameter("AgentID", "Netmon03");
        ClientResponse response = request.post();
        assert response.getResponseStatus().getStatusCode() == 200;
        assert response.getEntity(String.class).toString().startsWith("OK");
//        "payload - Timestamp=3/10/14 10:00:53 AM&AgentID=Netmon03&Host=hostname&Service=servicename&Message=message&State=Critical&Perf=Perf=1&IP=0.0.0.0"
    }

    @Test
    public void testMoreData() throws Exception {
        ClientRequest request = new ClientRequest(getApiUrl() + "/services");
        request.formParameter("Host", "Host1000");
        request.formParameter("Service", "FastEthernet0/0 - Fa0/0");
        request.formParameter("IP", "72.1.68.138");
        request.formParameter("State", "OK");
        request.formParameter("Message", "Interface Status Reset");// URLEncoder.encode("message 98", "UTF-8").replace("+", "%20"));
        request.formParameter("Perf", "Perf=1");
        request.formParameter("TimeStamp", "3/11/14 8:57:11 AM");
        request.formParameter("AgentID", "Netmon03");
        request.formParameter("Hostgroups", "HG4, HG5");
        ClientResponse response = request.post();
        assert response.getResponseStatus().getStatusCode() == 200;
        assert response.getEntity(String.class).toString().startsWith("OK");

   //     "Timestamp=3/11/14 8:57:11 AM&AgentID=Netmon03&Host=128PAI1-rtr-1760&Service=FastEthernet0/0 - Fa0/0&Message=Interface Status Reset&State=OK&IP=72.1.68.138"
    }

    @Test
    public void testHostGroupAgain() throws Exception {
        ClientRequest request = new ClientRequest(getApiUrl() + "/services");
        request.formParameter("Host", "Barracuda");
        request.formParameter("Service", "Outbound Queue Depth");
        request.formParameter("IP", "72.1.68.138");
        request.formParameter("State", "OK");
        request.formParameter("Message", "Barracuda Outbound Queue Depth: 594");
        request.formParameter("Perf", "594");
        request.formParameter("TimeStamp", "3/17/14 9:57:12 AM");
        request.formParameter("AgentID", "Netmon01");
        request.formParameter("Hostgroups", "Barracuda");
        ClientResponse response = request.post();
        assert response.getResponseStatus().getStatusCode() == 200;
        assert response.getEntity(String.class).toString().startsWith("OK");
//    "Timestamp=3/17/14 9:57:12 AM&AgentID=Netmon01&HostGroup=Barracuda&Host=Barracuda&Service=Outbound Queue Depth&Message=Barracuda Outbound Queue Depth: 594&State=OK&Perf=594"

    }


}
