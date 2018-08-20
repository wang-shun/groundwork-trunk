package org.groundwork.cloudhub.openstack;

import org.apache.log4j.Logger;
import org.groundwork.cloudhub.ServerConfigurator;
import org.groundwork.cloudhub.configuration.OpenStackConnection;
import org.groundwork.cloudhub.connectors.openstack.client.AuthClient;
import org.groundwork.cloudhub.connectors.openstack.client.CapabilityInfo;
import org.groundwork.cloudhub.connectors.openstack.client.CeilometerClient;
import org.groundwork.cloudhub.connectors.openstack.client.HypervisorInfo;
import org.groundwork.cloudhub.connectors.openstack.client.MetricInfo;
import org.groundwork.cloudhub.connectors.openstack.client.MetricMetaInfo;
import org.groundwork.cloudhub.connectors.openstack.client.NovaClient;
import org.groundwork.cloudhub.connectors.openstack.client.TenantInfo;
import org.groundwork.cloudhub.connectors.openstack.client.TokenSessionManager;
import org.groundwork.cloudhub.connectors.openstack.client.VmInfo;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.junit.Assert;
import org.junit.Test;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Tests of Ceilometer client internals
 *
 */
public class CeilometerTest {

    private static Logger log = Logger.getLogger(CeilometerTest.class);

    public static String TENANT_ID = ServerConfigurator.KILO_OPENSTACK_TENANT_ID;
    public static String TENANT_NAME = ServerConfigurator.KILO_OPENSTACK_TENANT_NAME;
    public final String TEMP_TOKEN = "";

    private String[] meters = { "disk.read.bytes", "cpu_util" };
    private String[] allMeters = { "disk.read.bytes", "storage.objects.size", "disk.root.size", "disk.ephemeral.size", "cpu_util", "memory" };

    private ServerConfigurator.OpenStackTestType openStackTestType = ServerConfigurator.OpenStackTestType.kilo;

    public OpenStackConnection createConnection() {
        OpenStackConnection connection = new OpenStackConnection();
        switch(openStackTestType) {
            case icehouse:
                connection.setServer(ServerConfigurator.ICEHOUSE_OPENSTACK_SERVER);
                connection.setTenantName(ServerConfigurator.ICEHOUSE_OPENSTACK_TENANT_NAME);
                connection.setTenantId(ServerConfigurator.ICEHOUSE_OPENSTACK_TENANT_ID);
                connection.setUsername(ServerConfigurator.ICEHOUSE_OPENSTACK_USERNAME);
                connection.setPassword(ServerConfigurator.ICEHOUSE_OPENSTACK_PASSWORD);
                TENANT_ID = ServerConfigurator.ICEHOUSE_OPENSTACK_TENANT_ID;
                TENANT_NAME = ServerConfigurator.ICEHOUSE_OPENSTACK_TENANT_NAME;
                break;
            case juno:
                connection.setServer(ServerConfigurator.JUNO_OPENSTACK_SERVER);
                connection.setTenantName(ServerConfigurator.JUNO_OPENSTACK_TENANT_NAME);
                connection.setTenantId(ServerConfigurator.JUNO_OPENSTACK_TENANT_ID);
                connection.setUsername(ServerConfigurator.JUNO_OPENSTACK_USERNAME);
                connection.setPassword(ServerConfigurator.JUNO_OPENSTACK_PASSWORD);
                TENANT_ID = ServerConfigurator.JUNO_OPENSTACK_TENANT_ID;
                TENANT_NAME = ServerConfigurator.JUNO_OPENSTACK_TENANT_NAME;
                break;
            case kilo:
                connection.setServer(ServerConfigurator.KILO_OPENSTACK_SERVER);
                connection.setTenantName(ServerConfigurator.KILO_OPENSTACK_TENANT_NAME);
                connection.setTenantId(ServerConfigurator.KILO_OPENSTACK_TENANT_ID);
                connection.setUsername(ServerConfigurator.KILO_OPENSTACK_USERNAME);
                connection.setPassword(ServerConfigurator.KILO_OPENSTACK_PASSWORD);
                TENANT_ID = ServerConfigurator.KILO_OPENSTACK_TENANT_ID;
                TENANT_NAME = ServerConfigurator.KILO_OPENSTACK_TENANT_NAME;
                break;
        }
        return connection;
    }

    @Test
    public void allMetricsTest() throws Exception {
        Map<String, VmInfo> vms = new HashMap<>();
        OpenStackConnection connection = createConnection();
        TenantInfo tenantInfo = new TenantInfo(TEMP_TOKEN, TENANT_ID, TENANT_NAME);
        AuthClient auth = new AuthClient(connection);
        AuthClient.AuthResponse response = auth.login(tenantInfo);
        assert response.success();

        NovaClient novaClient = new NovaClient(connection);
        for (HypervisorInfo server : novaClient.listHypervisors()) {
            for (VmInfo vm : novaClient.listVirtualMachines(server.name)) {
                vms.put(vm.id, vm);
            }
        }
        CeilometerClient ceilometer = new CeilometerClient(connection);

        // ****************************
        // WARNING: as of KILO, this api returns thousands of metrics
        // *******************************
        List<MetricMetaInfo> metaMetrics = ceilometer.retrieveMetricDescriptions();
        for (MetricMetaInfo meta : metaMetrics) {
            assert meta.meter != null;
            assert meta.resource != null;
            assert meta.source != null;
            assert meta.type != null;
            assert meta.unit != null;
        }
        Map<String,String> results = new TreeMap<>();
        int count = 0;
        for (MetricMetaInfo meta : metaMetrics) {
            List<MetricInfo> metrics = ceilometer.retrieveMetrics(meta.meter);
            if (metrics.size() > 0) {
                System.out.println("Meter is used: " + meta.meter);
                count++;
            }
            for (MetricInfo metric : metrics) {
                assert metric.meter.equals((meta.meter));
                assert metric.metric != null;
                assert metric.resource != null;
                assert metric.unit != null;
                assert metric.timestamp != null;
                VmInfo vm = vms.get(metric.resource);
                String vmName = (vm == null) ? metric.resource : vm.name;
                Double value = Double.parseDouble(metric.metric);
                if (value > 0.0) {
                    results.put(metric.resource + "-" + metric.meter, metric.toString(vmName));
                }
            }
        }
        System.out.println("Found " + metaMetrics.size() + " meters. Meters actually active: " + count);
        String fileName =
                "/usr/local/groundwork/config/cloudhub/statistics/openstack-stats-" + openStackTestType.name() + ".txt";
        BufferedWriter fileWriter = new BufferedWriter(new FileWriter(fileName));
        for (String value : results.values()) {
            fileWriter.write(value);
            fileWriter.newLine();
        }
        fileWriter.close();
    }


    @Test
    public void metricsTest() throws Exception {
        OpenStackConnection connection = createConnection();
        TenantInfo tenantInfo = new TenantInfo(TEMP_TOKEN, TENANT_ID, TENANT_NAME);
        AuthClient auth = new AuthClient(connection);
        AuthClient.AuthResponse response = auth.login(tenantInfo);
        assert response.success();
        CeilometerClient ceilometer = new CeilometerClient(connection);
        int count = 0;
        for (String meter : allMeters) {
            List<MetricInfo> metrics = ceilometer.retrieveMetrics(meter);
            if (log.isDebugEnabled()) {
                log.debug("meter: " + meter);
            }
            for (MetricInfo metric : metrics) {
                if (log.isDebugEnabled()) {
                    log.debug(metric);
                }
                assert metric.meter.equals((meter));
                assert metric.metric != null;
                assert metric.resource != null;
                assert metric.unit != null;
                assert metric.timestamp != null;
            }
            count++;
            log.debug("");
        }
        assert count == 6;
    }

    @Test
    public void novaClientTest() throws Exception {
        OpenStackConnection connection = createConnection();
        TenantInfo tenantInfo = new TenantInfo(TEMP_TOKEN, TENANT_ID, TENANT_NAME);
        AuthClient auth = new AuthClient(connection);
        AuthClient.AuthResponse response = response = auth.login(tenantInfo);
        assert response.success();
        NovaClient nova = new NovaClient(connection);
        List<HypervisorInfo> hypervisors = nova.listHypervisors();
        String hypervisorName = null;
        for (HypervisorInfo hypervisor : hypervisors) {
            assert hypervisor.id != null;
            assert hypervisor.name != null;
            hypervisorName = hypervisor.name;
            if (log.isDebugEnabled()) {
                log.debug("hypervisor " + hypervisor.name + " : " + hypervisor.id);
            }
        }
        assert hypervisors.size() == 1;
        List<VmInfo> vms = nova.listVirtualMachines(hypervisorName);
        for (VmInfo vm : vms) {
            assert vm.id != null;
            assert vm.name != null;
            assert vm.hypervisor != null && vm.hypervisor.equals(hypervisorName);
            if (log.isDebugEnabled()) {
                log.debug("vm " + vm.name + " : " + vm.id + " - " + vm.hypervisor);
            }
        }
        assert vms.size() >= 3;
        Set<String> names = new HashSet<>();
        names.add("running_vms");
        names.add("free_ram_mb");
        names.add("free_disk_gb");
        List<MetricInfo> metrics = nova.getHyperVisorStatistics(hypervisorName, names);
        for (MetricInfo metric : metrics) {
            if (log.isDebugEnabled()) {
                log.debug(metric);
            }
            assert metric.meter != null;
            assert metric.metric != null;
            assert metric.resource != null;
            assert metric.unit != null;
            assert metric.timestamp != null;
        }
        auth.logout(AuthClient.DEMO_BASE_URL);
    }

    @Test
    public void setServerTest() throws Exception {
        OpenStackConnection connection = new OpenStackConnection();

        connection.setServer("http://test.com");
        assert connection.getServer().equals("test.com");

        connection.setServer("https://test.com");
        assert connection.getServer().equals("test.com");

        connection.setServer("test.com");
        assert connection.getServer().equals("test.com");

    }


    public void listResources(TenantInfo tenant) throws Exception {
        ClientRequest request = new ClientRequest("http://agno.groundwork.groundworkopensource.com:8777/v2/resources");
        request = request.followRedirects(true);
        request.accept(MediaType.APPLICATION_JSON);
        request.header("X-Auth-Token", tenant.accessToken);
        ClientResponse<String> response = request.get(String.class);
        if (response.getResponseStatus() == Response.Status.OK) {
            String payload = response.getEntity();
            JsonReader reader = Json.createReader(new StringReader(payload));
            JsonObject object = reader.readObject();
            JsonArray servers = object.getJsonArray("servers");
            List<HypervisorInfo> result = new ArrayList<HypervisorInfo>();

            System.out.println(response.getEntity());
            return;
        }
        System.out.println("entity = " + response.getEntity(String.class));
        //throw new IOException("Failed to get API list: " + response.getResponseStatus());
    }


    public void listMetrics2(TenantInfo tenant) throws Exception {
        //ClientRequest request = new ClientRequest("http://agno.groundwork.groundworkopensource.com:8777/v2/meters/disk.root.size");
    //    ClientRequest request = new ClientRequest("http://agno.groundwork.groundworkopensource.com:8777/v2/meters/cpu_util");

        // ram_util
        String Q = "http://agno.groundwork.groundworkopensource.com:8777/v2/meters/memory.usage" +
                    "?q.field=timestamp&q.op=gt&q.value=2014-04-11T14:40:00";

        ClientRequest request = new ClientRequest(Q);
        //   "http://agno.groundwork.groundworkopensource.com:8777/v2/meters/cpu_util?q.field=resource_id&q.value=5cb9256a-924c-4eac-bcf9-80522ecd9a15");
        request = request.followRedirects(true);
        request.accept(MediaType.APPLICATION_JSON);
        request.header("X-Auth-Token", tenant.accessToken);
        ClientResponse<String> response = request.get(String.class);
        if (response.getResponseStatus() == Response.Status.OK) {
            String payload = response.getEntity();
            //System.out.println(payload);
            JsonReader reader = Json.createReader(new StringReader(payload));
            JsonArray array = reader.readArray();
            for (int ix = 0; ix < array.size(); ix++) {
                JsonObject sample = array.getJsonObject(ix);
                double volume = sample.getJsonNumber("counter_volume").doubleValue();
                String resource = sample.getString("resource_id");
                String timestamp = sample.getString("timestamp");
                String unit = sample.getString("counter_unit");
                System.out.format("%s: %s - %f %s\n", resource, timestamp, volume, unit);
            }
            return;
        }
        System.out.println("entity = " + response.getEntity(String.class));
        //throw new IOException("Failed to get API list: " + response.getResponseStatus());
    }

    @Test
    public void listMetricsTest() throws Exception {
        OpenStackConnection connection = createConnection();
        TenantInfo tenantInfo = new TenantInfo(TEMP_TOKEN, TENANT_ID, TENANT_NAME);
        AuthClient auth = new AuthClient(connection);
        AuthClient.AuthResponse response = response = auth.login(tenantInfo);
        assert response.success();
        listMetrics(response.getTenantInfo());
    }
    public void listMetrics(TenantInfo tenant) throws Exception {
        ClientRequest request = new ClientRequest("http://agno.groundwork.groundworkopensource.com:8777/v2/meters");
        request = request.followRedirects(true);
        request.accept(MediaType.APPLICATION_JSON);
        request.header("X-Auth-Token", tenant.accessToken);
        ClientResponse<String> response = request.get(String.class);
        if (response.getResponseStatus() == Response.Status.OK) {
            System.out.println(response.getEntity());
            return;
        }
        System.out.println("entity = " + response.getEntity(String.class));
        //throw new IOException("Failed to get API list: " + response.getResponseStatus());
    }

    private void debugMetrics(String meter, List<MetricInfo> metrics) {
        if (log.isDebugEnabled()) {
            log.debug("meter: " + meter);
            for (MetricInfo metric : metrics) {
                log.debug(metric);
            }
            log.debug("");
        }
    }

    @Test
    public void tokenExpireTest() throws Exception {
        OpenStackConnection connection = createConnection();
        TenantInfo tenantInfo = new TenantInfo(TEMP_TOKEN, TENANT_ID, TENANT_NAME);
        AuthClient auth = new AuthClient(connection);
        AuthClient.AuthResponse response = auth.login(tenantInfo);
        assert response.success();
        assert response.getTenantInfo().accessToken != null;
        if (log.isDebugEnabled()) {
            log.debug("token = " + response.getTenantInfo().accessToken);
        }

        CeilometerClient ceilometer = new CeilometerClient(connection);
        int count = 0;
        for (String meter : meters) {
            List<MetricInfo> metrics = ceilometer.retrieveMetrics(meter);
            debugMetrics(meter, metrics);
            count++;
        }
        assert count == 2;
        count = 0;
        try {
            // Set a bad token. Ensure that we retry with configured credentials
            TokenSessionManager mgr = auth.getTokenSessionManager();
            mgr.setToken(AuthClient.DEMO_BASE_URL, "bad");
            for (String meter : meters) {
                List<MetricInfo> metrics = ceilometer.retrieveMetrics(meter);
                debugMetrics(meter, metrics);
                count++;
            }
            assert auth.getTokenSessionManager().getToken(connection.getServer()) != null;
            auth.logout(connection.getServer());
            assert auth.getTokenSessionManager().getToken(connection.getServer()) == null;
        }
        catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
        finally {
            assert count == 2;
        }
    }

    @Test
    public void testCapabilities() throws Exception {
        Map<String, VmInfo> vms = new HashMap<>();
        OpenStackConnection connection = createConnection();
        TenantInfo tenantInfo = new TenantInfo(TEMP_TOKEN, TENANT_ID, TENANT_NAME);
        AuthClient auth = new AuthClient(connection);
        AuthClient.AuthResponse response = auth.login(tenantInfo);
        assert response.success();

        NovaClient novaClient = new NovaClient(connection);
        for (HypervisorInfo server : novaClient.listHypervisors()) {
            for (VmInfo vm : novaClient.listVirtualMachines(server.name)) {
                vms.put(vm.id, vm);
            }
        }
        CeilometerClient ceilometer = new CeilometerClient(connection);
        CapabilityInfo caps = ceilometer.retrieveCapabilities();
        assert caps.getApiCapabilities().size() > 0;
        assert caps.getStorageCapabilities().size() > 0;
        assert caps.getAlarmStorageCapabilities().size() > 0;
    }

}

