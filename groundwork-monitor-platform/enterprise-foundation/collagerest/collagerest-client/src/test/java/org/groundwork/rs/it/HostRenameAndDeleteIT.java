package org.groundwork.rs.it;

import org.groundwork.rs.client.DeviceClient;
import org.groundwork.rs.client.HostClient;
import org.groundwork.rs.client.HostGroupClient;
import org.groundwork.rs.client.HostIdentityClient;
import org.groundwork.rs.dto.DtoDevice;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoHostGroup;
import org.groundwork.rs.dto.DtoHostGroupList;
import org.groundwork.rs.dto.DtoHostIdentity;
import org.groundwork.rs.dto.DtoHostIdentityList;
import org.groundwork.rs.dto.DtoHostList;
import org.groundwork.rs.dto.DtoOperationResults;
import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Special case tests
 *  1. Renaming a host causes cache invalidation error
 *  2. Test out enabling Host->HostGroup collection cache (http://jira/browse/GWMON-13283)
 *  3. Rename to an existing host, check for duplicate error
 */
public class HostRenameAndDeleteIT extends AbstractIntegrationTest {

    public static final String HOST_NAME = "docker1-host-1";
    public static final String HOST_DESCRIPTION = "docker1-host-1-desc";
    public static final String NEW_HOST_NAME = "docker2-host-1";
    public static final String NEW_HOST_DESCRIPTION = "docker2-host-1-desc";
    public static final String DEVICE_NAME = HOST_NAME;
    public static final String NEW_DEVICE_NAME = NEW_HOST_NAME;

    //@Test
    // TODO: seeing intermittent errors in host guava cache after deleting. Re-enable this test when cache invalidation is in place
    public void testRenameHost() throws Exception {

        HostClient hostClient = new HostClient(getDeploymentURL());
        DeviceClient deviceClient = new DeviceClient(getDeploymentURL());

        try {
            DtoHostList hosts = new DtoHostList();
            DtoHost host = new DtoHost();
            host.setHostName(HOST_NAME);
            host.setDescription(HOST_DESCRIPTION);
            host.setDeviceIdentification(DEVICE_NAME);
            host.setDeviceDisplayName(DEVICE_NAME);
            host.setAgentId(AGENT_ID);
            hosts.add(host);

            hostClient.post(hosts);

            DtoHost renamedHost = hostClient.rename(HOST_NAME, NEW_HOST_NAME, NEW_HOST_DESCRIPTION, NEW_DEVICE_NAME);
            assert renamedHost != null;
            assert renamedHost.getHostName().equals(NEW_HOST_NAME);
            assert renamedHost.getDescription().equals(NEW_HOST_DESCRIPTION);
            assert renamedHost.getDeviceIdentification().equals(NEW_DEVICE_NAME);
            assert renamedHost.getDeviceDisplayName().equals(NEW_DEVICE_NAME);

            assert hostClient.lookup(HOST_NAME) == null;

            DtoHost host2 = hostClient.lookup(NEW_HOST_NAME);
            assert host2 != null;
            assert host2.getHostName().equals(NEW_HOST_NAME);
            assert host2.getDescription().equals(NEW_HOST_DESCRIPTION);
            assert host2.getDeviceIdentification().equals(NEW_DEVICE_NAME);
            assert host2.getDeviceDisplayName().equals(NEW_DEVICE_NAME);

            assert deviceClient.lookup(DEVICE_NAME) == null;

            DtoDevice device = deviceClient.lookup(NEW_DEVICE_NAME);
            assert device != null;
            assert device.getIdentification().equals(NEW_DEVICE_NAME);
            assert device.getDisplayName().equals(NEW_DEVICE_NAME);
        }
        catch (Exception e) {
            e.printStackTrace();
        } finally {
            deviceClient.delete(NEW_DEVICE_NAME);
            assert hostClient.lookup(NEW_HOST_NAME) == null;
            assert deviceClient.lookup(NEW_DEVICE_NAME) == null;
        }

    }


    /*  HostGroup->Host Collection Cache bug
        @see http://jira/browse/GWMON-13283
        @since 7.2.1
        Enabling the HostGroup->Host collection cache will as of (Dec 4, 2017) break this test
        Hosts need to be cleared out of the HostGroup cache upon deletions of hosts
		<collection-cache collection="com.groundwork.collage.model.impl.HostGroup.hosts"       usage="read-write"/>
      */
    @Test
    public void expectDeletingHostWillRemoveItFromHostGroup() {
        HostClient hostClient = new HostClient(getDeploymentURL());

        // setup host
        DtoHost host = new DtoHost();
        host.setHostName(HostIT.ZHOST_001);
        host.setDescription("First of my servers");
        host.setMonitorStatus("UP");
        host.setAppType("NAGIOS");
        host.setAgentId(AGENT_ID);
        host.setDeviceIdentification("10.1.10.180");
        DtoHostList hosts = new DtoHostList();
        hosts.add(host);
        hostClient.post(hosts);
        assertThat(hostClient.lookup(HostIT.ZHOST_001)).isNotNull();

        // setup host group
        HostGroupClient hostGroupClient = new HostGroupClient(getDeploymentURL());
        DtoHostGroup hostGroup = new DtoHostGroup();
        hostGroup.setName(HostIT.ZHG_001);
        hostGroup.setAgentId(AGENT_ID);
        hostGroup.addHost(host);
        DtoHostGroupList hostGroups = new DtoHostGroupList();
        hostGroups.add(hostGroup);
        hostGroupClient.post(hostGroups);
        DtoHostGroup verify = hostGroupClient.lookup(HostIT.ZHG_001);
        assertThat(verify).isNotNull();

        // delete host
        hostClient.delete(HostIT.ZHOST_001);
        assertThat(hostClient.lookup(HostIT.ZHOST_001)).isNull();

        //TODO: lookup() fails here when two lines below are uncommented in hibernate.cfg.xml
        // 	<collection-cache collection="com.groundwork.collage.model.impl.HostGroup.hosts"       usage="read-write"/>
        //  <collection-cache collection="com.groundwork.collage.model.impl.Host.hostGroups"       usage="read-write"/>
        //  ERROR [org.groundwork.rs.resources.HostGroupResource] (http-localhost/127.0.0.1:8080-10)
        //   Unexpected exception: org.hibernate.ObjectNotFoundException:
        //   No row with the given identifier exists: [com.groundwork.collage.model.impl.Host#3708]:
        //   org.hibernate.ObjectNotFoundException: No row with the given identifier exists: [com.groundwork.collage.model.impl.Host#3708]
        DtoHostGroup hg = hostGroupClient.lookup(HostIT.ZHG_001);
        assertNotNull(hg);
        assertTrue(hg.getHosts() == null);

        hostGroupClient.delete(HostIT.ZHG_001);
        assertNull(hostGroupClient.lookup(HostIT.ZHG_001));

    }

    public static final String DUPE = "docker2-dupe";

    //@Test
    // TODO: seeing INTERMITTENT errors with 500 instead of 400 dupe key error
    public void testRenameHostDupeKey() throws Exception {

        HostClient hostClient = new HostClient(getDeploymentURL());
        DeviceClient deviceClient = new DeviceClient(getDeploymentURL());

        try {
            DtoHostList hosts = new DtoHostList();
            DtoHost host = new DtoHost();
            host.setHostName(HOST_NAME);
            host.setDescription(HOST_DESCRIPTION);
            host.setDeviceIdentification(DEVICE_NAME);
            host.setDeviceDisplayName(DEVICE_NAME);
            host.setAgentId(AGENT_ID);
            hosts.add(host);
            DtoHost dupe = new DtoHost();
            dupe.setHostName(DUPE);
            dupe.setDescription(DUPE);
            dupe.setDeviceIdentification(DUPE);
            dupe.setDeviceDisplayName(DUPE);
            dupe.setAgentId(AGENT_ID);
            hosts.add(dupe);

            hostClient.post(hosts);

            try {
                Thread.sleep(200);
                DtoHost dupedHost = hostClient.rename(HOST_NAME, DUPE, NEW_HOST_DESCRIPTION, DUPE);
            }
            catch (Exception e) {
                assert e.getMessage().contains("400");
            }
        } finally {
            deviceClient.delete(DEVICE_NAME);
            deviceClient.delete(DUPE);
            assert deviceClient.lookup(DEVICE_NAME) == null;
            assert deviceClient.lookup(DUPE) == null;
        }
    }

    //@Test
    // TODO: seeing errors in host guava cache after deleting. Re-enable this test when cache invalidation is in place
    public void testHostIdentityLinkage() {
        // create clients
        DeviceClient deviceClient = new DeviceClient(getDeploymentURL());
        HostIdentityClient hostIdentityClient = new HostIdentityClient(getDeploymentURL());
        HostClient client = new HostClient(getDeploymentURL());

        // create host identity
        DtoHostIdentityList dtoHostIdentities = new DtoHostIdentityList();
        DtoHostIdentity dtoHostIdentity = new DtoHostIdentity("test-host-identity-link",
                Arrays.asList(new String[]{"test-host-identity-link-alias"}));
        dtoHostIdentities.add(dtoHostIdentity);
        DtoOperationResults results = hostIdentityClient.post(dtoHostIdentities);
        assert results.getSuccessful() == 1;

        // validate host identity
        dtoHostIdentity = hostIdentityClient.lookup("test-host-identity-link");
        assert dtoHostIdentity != null;
        assert dtoHostIdentity.getHostName().equals("test-host-identity-link");
        assert dtoHostIdentity.getHost() == false;

        // create test host using alias for host name
        DtoHostList dtoHosts = new DtoHostList();
        DtoHost dtoHost = new DtoHost();
        dtoHost.setHostName("Test-Host-Identity-Link-Alias");
        dtoHost.setMonitorStatus("PENDING");
        dtoHost.setDeviceIdentification("test-host-identity-link-device");
        dtoHost.setAppType("SEL");
        dtoHost.setAgentId(AGENT_ID);
        dtoHosts.add(dtoHost);
        results = client.post(dtoHosts);
        assert results != null;
        assert results.getSuccessful() == 1;

        // validate host and host identity
        dtoHost = client.lookup("TEST-HOST-IDENTITY-LINK-ALIAS");
        assert dtoHost != null;
        assert dtoHost.getHostName().equals("test-host-identity-link");
        dtoHostIdentity = hostIdentityClient.lookup("test-host-identity-link");
        assert dtoHostIdentity != null;
        assert dtoHostIdentity.getHostName().equals("test-host-identity-link");
        assert dtoHostIdentity.getHost() == true;

        // delete test host
        results = deviceClient.delete("test-host-identity-link-device");
        assert results != null;
        assert results.getSuccessful() == 1;

        // validate host identity
        dtoHostIdentity = hostIdentityClient.lookup("test-host-identity-link");
        assert dtoHostIdentity != null;
        assert dtoHostIdentity.getHostName().equals("test-host-identity-link");
        assert dtoHostIdentity.getHost() == false;

        // cleanup
        results = hostIdentityClient.delete("test-host-identity-link");
        assert results != null;
        assert results.getSuccessful() == 1;
    }
}
