package org.groundwork.rs.client;

import org.groundwork.foundation.ws.impl.WSClientConfiguration;
import org.groundwork.rs.dto.DtoBizHostServiceInDowntimeList;
import org.groundwork.rs.dto.DtoCacheState;
import org.groundwork.rs.dto.DtoCategory;
import org.groundwork.rs.dto.DtoDepthType;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoService;
import org.groundwork.rs.dto.DtoServiceGroup;
import org.groundwork.rs.integration.IntegrationDataTool;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * This test is disabled in the pom.xml (excluded)
 * It is used for performance testing against the eGain data
 * connect to server named rstools-test for performance testing
 */
public class HostGroupFilterTest extends AbstractHostTest  {

    private static final String[] AMAZON_HOST_GROUPS = {
            "AWS-RDS:storage", "AWS-AZ:us-west-2c", "AWS-AZ:us-west-2a",
            "AWS-M:us-west-2.amazonaws.com", "Linux Servers"
    };

    private static final String[] BIG_BAD_HGS = {
            "BSM:Business Objects", "ESX:esx101.intra.realstuff.ch", "ESX:esx102.intra.realstuff.ch", "ESX:esx10.intra.realstuff.ch", "ESX:esx8.intra.realstuff.ch", "linux-servers", "Linux Servers", "NET:DMZ", "NET:LAN", "STOR:ESX101-localstore", "STOR:ESX102-localstore", "STOR:ESX10-localstore1", "STOR:ESX10-localstore2", "STOR:ESX8-localstore", "VSS:esx101.intra.realstuff.ch", "VSS:esx102.intra.realstuff.ch", "VSS:esx10.intra.realstuff.ch", "VSS:esx8.intra.realstuff.ch", "windows-servers"
    };

    private static final String EGAIN_HGS =
            "hostgroup in ('ESX:savtempcloudesx01.egain.net', 'ESX:attesx07.egain.net', 'UK-SLLO1-INFRA-DMZ', 'UK-LONDON-CUS-Capita'," +
            "'STOR:SAV_SAN_103_VM_02040', 'US-ATT-INF-MISC','UK-LONDON-INF-NAS','STOR:savesxdb02:localstorage', 'UK-LONDON-INF-SFTP'," +
            "'STOR:SAV_SAN_101_VM2_2152PR','UK-LONDON-INF-MISC','STOR:SAV_SAN_103_VM3','STOR:savesx66:localstorage','STOR:SAV_SAN_103_VM2'," +
            "'UK-LONVO-DBA-BACKUP','UK-LONVO-CUS-VFE','US-SAV-INT-TEST','STOR:SAV_SAN_103_VM6','STOR:attesx11:localstorage','STOR:ATT_SAN_101_ATTOTGBKUP'," +
            "'UK-CUS','US-SAV-INF-MISC','US-SAV-CUS-LL-BEAN','US-SAV-CUS-AMERICAN-AIRLINES','US-ATT-INF-GW','US-SAV-CUS-UNIVITA-HEALTH')";

//            'US-SAV-CUS-SYMETRA',
//            'US-SAV-CUS-AJC',
//            'US-SAV-CUS-JANUS-CAPITAL',
//            'UK-SLLO1-Infra-DYNA',
//            'US-SAV-CUS-FAREPORTAL',
//            'UK-SLLO1-CUS-OUP',
//            'US-SAV-CUS-EASTLAND-SHOE',
//            'US-SAV-INF-BACKUP',
//            'STOR:SAV_SAN_202_CLOUD_PROD_06',
//            'US-SAV-CUS-PROMETHEAN',
//            'US-AWOR-CUS-Fareportal-Prod',
//            'US-ATT-INF-DC'
//    };


    private static final String[] DEFAULT_HOST_GROUPS = { "Linux Servers"};

    private static final String HOST_GROUP_BIG_BAD_QUERY1 = "hostgroup like 'BSM:Business Objects', 'ESX:esx101.intra.realstuff.ch' OR hostgroup like 'ESX:esx102.intra.realstuff.ch' OR hostgroup like 'ESX:esx10.intra.realstuff.ch' OR hostgroup like 'ESX:esx8.intra.realstuff.ch' OR hostgroup like 'linux-servers' OR hostgroup like 'Linux Servers' OR hostgroup like 'NET:DMZ' OR hostgroup like 'NET:LAN' OR hostgroup like 'STOR:ESX101-localstore' OR hostgroup like 'STOR:ESX102-localstore' OR hostgroup like 'STOR:ESX10-localstore1' OR hostgroup like 'STOR:ESX10-localstore2' OR hostgroup like 'STOR:ESX8-localstore' OR hostgroup like 'VSS:esx101.intra.realstuff.ch' OR hostgroup like 'VSS:esx102.intra.realstuff.ch' OR hostgroup like 'VSS:esx10.intra.realstuff.ch' OR hostgroup like 'VSS:esx8.intra.realstuff.ch' OR hostgroup like 'windows-servers'";
    private static final String HOST_GROUP_BIG_BAD_QUERY2 = "hostgroup = 'BSM:Business Objects' OR hostgroup = 'ESX:esx101.intra.realstuff.ch' OR hostgroup = 'ESX:esx102.intra.realstuff.ch' OR hostgroup = 'ESX:esx10.intra.realstuff.ch' OR hostgroup = 'ESX:esx8.intra.realstuff.ch' OR hostgroup = 'linux-servers' OR hostgroup = 'Linux Servers' OR hostgroup = 'NET:DMZ' OR hostgroup = 'NET:LAN' OR hostgroup = 'STOR:ESX101-localstore' OR hostgroup = 'STOR:ESX102-localstore' OR hostgroup = 'STOR:ESX10-localstore1' OR hostgroup = 'STOR:ESX10-localstore2' OR hostgroup = 'STOR:ESX8-localstore' OR hostgroup = 'VSS:esx101.intra.realstuff.ch' OR hostgroup = 'VSS:esx102.intra.realstuff.ch' OR hostgroup = 'VSS:esx10.intra.realstuff.ch' OR hostgroup = 'VSS:esx8.intra.realstuff.ch' OR hostgroup = 'windows-servers'";
    private static final String HOSTGROUP_IN_QUERY = "hostgroup in ('BSM:Business Objects','ESX:esx101.intra.realstuff.ch','ESX:esx102.intra.realstuff.ch','ESX:esx10.intra.realstuff.ch','ESX:esx8.intra.realstuff.ch','linux-servers','Linux Servers','NET:DMZ','NET:LAN','STOR:ESX101-localstore','STOR:ESX102-localstore','STOR:ESX10-localstore1','STOR:ESX10-localstore2','STOR:ESX8-localstore','VSS:esx101.intra.realstuff.ch','VSS:esx102.intra.realstuff.ch','VSS:esx10.intra.realstuff.ch','VSS:esx8.intra.realstuff.ch','windows-servers')";


    @Test
    public void testFilterByDefaultHostGroups() throws Exception {
        if (serverDown) return;
                HostClient client = new HostClient(getDeploymentURL());

        //List<String> hostGroupsList = Arrays.asList(DEFAULT_HOST_GROUPS);
        List<String> hostGroupsList = Arrays.asList(BIG_BAD_HGS);
        long start = System.currentTimeMillis();
        List<DtoHost> hosts = client.filterByHostGroups(hostGroupsList, DtoDepthType.Simple);
        long stop = System.currentTimeMillis();
        System.out.println("execution elapsed time ms: " + (stop - start));
        for (DtoHost host : hosts) {
            System.out.println("--Host: " + host.getHostName());
        }
    }

    @Test
    public void testFilterByAmazonHostGroups() throws Exception {
        if (serverDown) return;
        HostClient client = new HostClient(getDeploymentURL());

        List<String> hostGroupsList = Arrays.asList(AMAZON_HOST_GROUPS);
        long start = System.currentTimeMillis();
        List<DtoHost> hosts = client.filterByHostGroups(hostGroupsList, DtoDepthType.Simple);
        long stop = System.currentTimeMillis();
        System.out.println("execution elapsed time ms: " + (stop - start));
        for (DtoHost host : hosts) {
            System.out.println("--Host: " + host.getHostName());
        }
    }

    @Test
    public void testFilterByDefaultHostGroups2() throws Exception {
        if (serverDown) return;
        HostClient client = new HostClient(getDeploymentURL());

        List<String> hostGroupsList = Arrays.asList(DEFAULT_HOST_GROUPS);
        long start = System.currentTimeMillis();
        List<DtoHost> hosts = client.filterByHostGroups(hostGroupsList, DtoDepthType.Simple);
        long stop = System.currentTimeMillis();
        System.out.println("execution elapsed time ms: " + (stop - start));
        for (DtoHost host : hosts) {
            System.out.println("--Host: " + host.getHostName());
        }
    }

    @Test
    public void testHostGroupBadQuery() throws Exception {
        if (serverDown) return;
        HostClient client = new HostClient(getDeploymentURL());

        long start = System.currentTimeMillis();
        List<DtoHost> hosts = client.query(HOST_GROUP_BIG_BAD_QUERY2, DtoDepthType.Simple);
        long stop = System.currentTimeMillis();
        System.out.println("execution elapsed time ms: " + (stop - start));
        for (DtoHost host : hosts) {
            System.out.println("--Host: " + host.getHostName());
        }
    }

    @Test
    public void testHostGroupHostQueryInClause() throws Exception {
        if (serverDown) return;
        HostClient client = new HostClient(getDeploymentURL());

        long start = System.currentTimeMillis();
        List<DtoHost> hosts = client.query(EGAIN_HGS, DtoDepthType.Simple);
//        List<DtoHost> hosts = client.query(HOSTGROUP_IN_QUERY, DtoDepthType.Simple);
        long stop = System.currentTimeMillis();
        System.out.println("execution elapsed time ms: " + (stop - start));
        for (DtoHost host : hosts) {
            System.out.println("--Host: " + host.getHostName());
        }
    }

    @Test
    public void testHostGroupServiceQueryInClause() throws Exception {
        if (serverDown) return;
        ServiceClient client = new ServiceClient(getDeploymentURL());

        long start = System.currentTimeMillis();
        List<DtoService> services = client.query(HOSTGROUP_IN_QUERY);
        long stop = System.currentTimeMillis();
        System.out.println("execution elapsed time ms: " + (stop - start));
        for (DtoService service : services) {
            System.out.println("--Service: " + service.getHostName() + ", " + service.getDescription());
        }
    }

    @Test
    public void testRetrieveServiceGroups() throws Exception {
        if (serverDown) return;
        ServiceGroupClient client = new ServiceGroupClient(getDeploymentURL());

        long start = System.currentTimeMillis();
        List<DtoServiceGroup> serviceGroups = client.list();
        long stop = System.currentTimeMillis();
        System.out.println("execution elapsed time ms: " + (stop - start));
        for (DtoServiceGroup serviceGroup : serviceGroups) {
            System.out.println("--ServiceGroup: " + serviceGroup.getName());
        }
    }

    @Test
    public void testRetrieveCategories() throws Exception {
        if (serverDown) return;
        CategoryClient client = new CategoryClient(getDeploymentURL());

        long start = System.currentTimeMillis();
        List<DtoCategory> categories = client.list();
        long stop = System.currentTimeMillis();
        System.out.println("execution elapsed time ms: " + (stop - start));
        for (DtoCategory serviceGroup : categories) {
            System.out.println("--Category: " + serviceGroup.getName());
        }
    }

    @Test
    public void testCacheStatistics() throws Exception {
        if (serverDown) return;
        long start = System.currentTimeMillis();
        CacheStatisticsClient client = new CacheStatisticsClient(getDeploymentURL(), MediaType.APPLICATION_JSON_TYPE);
        List<DtoCacheState> caches = client.list();
        long stop = System.currentTimeMillis();
        System.out.println("execution elapsed time ms: " + (stop - start));
        for (DtoCacheState cache : caches) {
            System.out.println(cache.getCacheName() + ", size: " + cache.getMaxElementsInMemory() + ", used: " + cache.getObjectCount() +
                    ", hits: " + cache.getCacheHits() + ", misses: " + cache.getCacheMisses());
        }
    }

    String login() {
        boolean enableEncryption = isEnableEncryption();
        AuthClient client = new AuthClient(getDeploymentURL());
        String username = System.getProperty(IntegrationDataTool.SYSTEM_PARAM_GWOS_REST_USER, "RESTAPIACCESS");
        String password = System.getProperty(IntegrationDataTool.SYSTEM_PARAM_GWOS_REST_PW,
                (enableEncryption) ? "7UZZVvnLbuRNk12Yk5H33zeYdWQpnA7j9shir7QfJgwh": "RESTAPIACCESSPASSWORD");
        AuthClient.Response response = client.login(username, password, AuthClient.APP_NAME);
        return response.getToken();
    }

    void logout(String token) {
        AuthClient client = new AuthClient(getDeploymentURL());
        client.logout(AuthClient.APP_NAME, token);
    }

    boolean isEnableEncryption() {
        String strEncryptionEnabled = WSClientConfiguration.getProperty(WSClientConfiguration.ENCRYPTION_ENABLED);
        if (strEncryptionEnabled == null) {
            strEncryptionEnabled = "true";
        } // end if
        return Boolean.parseBoolean(strEncryptionEnabled);
    }

    static String token = null;

    @Before
    public void beforeTest() {
        token = login();
    }

    @After
    public void afterTest() {
        logout(token);
    }

    @Test
    public void setAndClearDownTimes() throws Exception {
        if (serverDown) return;
        // Host Group of 1134
        //testHostsAndServicesInDowntime(Arrays.asList(new String[]{"*"}), Arrays.asList(new String[]{"*"}), Arrays.asList(new String[]{"Auto-Registration"}), true, false);
        //testHostsAndServicesInDowntime(Arrays.asList(new String[]{"*"}), Arrays.asList(new String[]{"*"}), Arrays.asList(new String[]{"US-SAV-CUS-FARMERS"}), true, true);
        // Host Group of 3
        //testHostsAndServicesInDowntime(Arrays.asList(new String[]{"*"}), Arrays.asList(new String[]{"*"}), Arrays.asList(new String[]{"US-SAV-INF-SENTIMENTANALYZER"}), true, false);
        //testHostsAndServicesInDowntime(Arrays.asList(new String[]{"*"}), Arrays.asList(new String[]{"*"}), Arrays.asList(new String[]{"STOR:wil-ds"}), true, true);
        testHostsAndServicesInDowntime(Arrays.asList(new String[]{"*"}), Arrays.asList(new String[]{"*"}), Arrays.asList(new String[]{"NET:VM Network"}), true, true);
    }

    public void testHostsAndServicesInDowntime(List<String> hosts, List<String> services, List<String> hostGroups,
                                               boolean setHosts, boolean setServices) {
        BizClient bizClient = new BizClient(getDeploymentURL());
        EventClient eventClient = new EventClient(getDeploymentURL());

        // run test using JSON and XML
        bizClient.setMediaType(MediaType.APPLICATION_JSON_TYPE);
        eventClient.setMediaType(MediaType.APPLICATION_JSON_TYPE);

        // set service in downtime
        long start = System.currentTimeMillis();
        DtoBizHostServiceInDowntimeList serviceInDowntime = bizClient.setInDowntime(hosts, services, hostGroups, null, setHosts, setServices);
        long stop = System.currentTimeMillis();
        System.out.println("SET execution elapsed time ms: " + (stop - start) + " size = " + serviceInDowntime.size());

        // get service in downtime
        start = System.currentTimeMillis();
        serviceInDowntime = bizClient.getInDowntime(serviceInDowntime);
        stop = System.currentTimeMillis();
        System.out.println("GET execution elapsed time ms: " + (stop - start)+ " size = " + serviceInDowntime.size());

        // clear service in downtime
//        start = System.currentTimeMillis();
//        serviceInDowntime = bizClient.clearInDowntime(serviceInDowntime);
//        stop = System.currentTimeMillis();
//        System.out.println("CLEAR execution elapsed time ms: " + (stop - start) + " size = " + serviceInDowntime.size());
//
//        // clear downtime event log messages
//        start = System.currentTimeMillis();
//        List<DtoEvent> downtimeEvents = eventClient.query("applicationType.name = 'DOWNTIME'");
//        stop = System.currentTimeMillis();
//        System.out.println("QUERY EVENTS execution elapsed time ms: " + (stop - start) + " size = " + downtimeEvents.size());
//        assert downtimeEvents != null;
//        //assert downtimeEvents.size() == 22;
//        List<String> downtimeEventIds = new ArrayList<String>(downtimeEvents.size());
//        for (DtoEvent downtimeEvent : downtimeEvents) {
//            downtimeEventIds.add(downtimeEvent.getId().toString());
//        }
//        start = System.currentTimeMillis();
//        DtoOperationResults results = eventClient.delete(downtimeEventIds);
//        stop = System.currentTimeMillis();
//        System.out.println("DEL EVENTS execution elapsed time ms: " + (stop - start) + " size = " + results.getCount());
//        assert results != null;
//        assert results.getCount() == downtimeEvents.size();
//        assert results.getFailed() == 0;
//        assert results.getSuccessful() == downtimeEvents.size();
    }

    public void testUpdate() throws SQLException {
//        String hql = "update Survey set name = :newName where name = :name";
//        Query query = session.createQuery(hql);
//        query.setString("name","Survey");
//        query.setString("newName","Corp");
//        int rowCount = query.executeUpdate();
//        System.out.println("Rows affected: " + rowCount);

    }
}
