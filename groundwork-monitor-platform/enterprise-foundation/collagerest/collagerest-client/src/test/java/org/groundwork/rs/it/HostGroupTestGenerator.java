package org.groundwork.rs.it;

import org.assertj.core.api.AutoCloseableSoftAssertions;
import org.groundwork.rs.dto.DtoDepthType;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoHostGroup;
import org.groundwork.rs.dto.DtoHostGroupList;
import org.groundwork.rs.dto.DtoHostList;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class HostGroupTestGenerator extends IntegrationTestGenerator {

    public final static String DESC = "Description ";
    public final static String APP_TYPE = "VEMA";

    public static DtoHostGroupList buildHostGroupInserts(IntegrationTestContext<DtoHostGroup> context) {
        return buildHostGroupInserts(context, null);
    }

    public static DtoHostGroupList buildHostGroupInserts(IntegrationTestContext<DtoHostGroup> context, IntegrationTestContext<DtoHost> hosts) {
        int start = (hosts == null) ? 0 : hosts.getStart();
        // if not reusing children host count should be greater than hostgroup count
        int countPerGroup = (hosts == null) ? 0 : (context.getReuseChildren() ? hosts.getCount() : hosts.getCount() / context.getCount());
        DtoHostGroupList hostGroups = new DtoHostGroupList();
        int max =  context.getStart() + context.getCount();
        for (int ix = context.getStart(); ix < max; ix++) {
            DtoHostGroup hostGroup = new DtoHostGroup();
            String index = String.format(FORMAT_NUMBER_SUFFIX, ix);
            String groupName = context.getPrefix() + context.getDelimiter() + index;
            hostGroup.setName(groupName);
            hostGroup.setDescription(DESC + groupName);
            hostGroup.setAppType(APP_TYPE);
            hostGroup.setAlias("hg-alias");
            hostGroup.setAgentId(context.getAgentId());
            if (hosts != null) {
                for (int hx = 0; hx < countPerGroup; hx++) {
                    String hostName = hosts.formatNameKey(start + hx);
                    hostGroup.addHost(hosts.lookupResult(hostName));
                }
                start = (context.getReuseChildren()) ? hosts.getStart() : start + countPerGroup;
            }
            hostGroups.add(hostGroup);
            context.addResult(groupName, hostGroup);
        }
        return hostGroups;
    }

    protected static final String SKIP_INSERTED_HG_FIELDS[] =  {"id", "appTypeDisplayName", "hosts", "bubbleUpStatus", "applicationType", "statistics"};

    public static void assertHostGroups(List<DtoHostGroup> dtoHostGroups, IntegrationTestContext<DtoHostGroup> context) {
        assertHostGroups(dtoHostGroups, context, DtoDepthType.Shallow);
    }

    public static void assertHostGroups(List<DtoHostGroup> dtoHostGroups, IntegrationTestContext<DtoHostGroup> context, DtoDepthType depthType) {
        try (AutoCloseableSoftAssertions soft = new AutoCloseableSoftAssertions()) {
            for (DtoHostGroup group : dtoHostGroups) {
                DtoHostGroup groupBefore = context.lookupResult(group.getName());
                if (groupBefore == null && context.getSkipUnmatchedResults()) {
                    continue;
                }
                assertThat(groupBefore).isNotNull();
                //Collections.sort(group.getHosts(), new CompareHosts());
                assertThat(group).as("Comparing Bulk HostGroup %s", group.getName()).isEqualToIgnoringGivenFields(groupBefore, SKIP_INSERTED_HG_FIELDS);
                if (depthType == DtoDepthType.Shallow ||  depthType == DtoDepthType.Full) {
                    assertThat(group.getHosts()).as("Comparing Hosts in HostGroup %s", group.getName()).usingElementComparator(new HostComparator()).hasSameElementsAs(groupBefore.getHosts());
                }
            }
        }
    }

    public static void addHostsToHostGroup(DtoHostGroup hostGroup, DtoHostList hosts) {
        for (DtoHost host : hosts.getHosts()) {
            hostGroup.addHost(host);
        }
    }

    public static List<String> reduceToNames(List<DtoHostGroup> hostGroups) {
        List<String> names = new ArrayList<>();
        for (DtoHostGroup hg : hostGroups) {
            names.add(hg.getName());
        }
        return names;
    }


}
