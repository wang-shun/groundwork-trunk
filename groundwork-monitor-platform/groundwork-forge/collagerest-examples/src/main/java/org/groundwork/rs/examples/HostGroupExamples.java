package org.groundwork.rs.examples;

import org.groundwork.rs.client.HostGroupClient;
import org.groundwork.rs.dto.DtoDepthType;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoHostGroup;
import org.groundwork.rs.dto.DtoHostGroupList;
import org.groundwork.rs.dto.DtoHostList;
import org.groundwork.rs.dto.DtoOperationResult;
import org.groundwork.rs.dto.DtoOperationResults;

import java.util.List;

public class HostGroupExamples extends HostExamples {

    public HostGroupExamples(FoundationConnection connection) {
        super(connection);
    }

    public void listHostGroups() {
        HostGroupClient hostGroupClient = new HostGroupClient(connection.getDeploymentUrl());

        // list out all hosts at default depth (Shallow)
        System.out.println("--- Listing hosts ....");
        List<DtoHostGroup> hostGroups = hostGroupClient.list();
        for (DtoHostGroup hostGroup : hostGroups) {
            System.out.println("\thost = " + hostGroup.getName());
        }
    }

    public void lookupHostGroupDeep() {
        HostGroupClient client = new HostGroupClient(connection.getDeploymentUrl());
        // return more indepth information
        DtoHostGroup hostGroup = client.lookup("Linux Servers", DtoDepthType.Deep);
    }

    public void queryHostGroups() {
        HostGroupClient client = new HostGroupClient(connection.getDeploymentUrl());
        List<DtoHostGroup> hostGroups = client.query("name like 'Eng%'");
        if (connection.isEnableAsserts()) {
            assert 3 == hostGroups.size();
            for (DtoHostGroup hostGroup : hostGroups) {
                assert hostGroup.getId() != null;
                assert hostGroup.getName().startsWith("Eng");
            }
        }
    }

    public void hostGroupMaintenance() {
        HostGroupClient hostGroupClient = new HostGroupClient(connection.getDeploymentUrl());
        DtoHostGroupList hostGroupUpdates = buildHostGroupUpdate();
        DtoOperationResults results = hostGroupClient.post(hostGroupUpdates);
        if (connection.isEnableAsserts()) assert (1 == results.getCount().intValue());
        for (DtoOperationResult result : results.getResults()) {
            if (connection.isEnableAsserts()) assert(result.getStatus().equals(DtoOperationResult.SUCCESS));
        }

        DtoHostGroup hostGroup = hostGroupClient.lookup("groupA");
        if (connection.isEnableAsserts()) assert(null != hostGroup);

        // reset data for next test
        hostGroupClient.delete("groupA");
        hostGroup = hostGroupClient.lookup("groupA");
        if (connection.isEnableAsserts()) assert(null == hostGroup);
        executeHostDelete("host-100,host-101");
        executeDeviceDelete("192.168.5.50,192.168.5.51");

    }

    public DtoHostGroupList buildHostGroupUpdate() {
        DtoHostGroupList hostGroups = new DtoHostGroupList();
        DtoHostGroup hostGroupA = new DtoHostGroup();
        hostGroupA.setName("groupA");
        hostGroupA.setDescription("Group A");
        hostGroupA.setAlias("A");
        hostGroupA.setAgentId("5437840f-a908-49fd-88bd-e04543a69e84");
        hostGroupA.setAppType("NAGIOS");
        DtoHostList hosts = buildHostUpdate();
        for (DtoHost host : hosts.getHosts()) {
            hostGroupA.addHost(host);
        }
        hostGroups.add(hostGroupA);
        return hostGroups;
    }




}
