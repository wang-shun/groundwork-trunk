package org.groundwork.rs.it;

import org.groundwork.rs.client.CommentClient;
import org.groundwork.rs.client.HostClient;
import org.groundwork.rs.client.ServiceClient;
import org.groundwork.rs.dto.DtoDepthType;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoHostList;
import org.groundwork.rs.dto.DtoService;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CommentIT extends AbstractIntegrationTest {

    public static final String THIS_IS_A_NOTE_1 = "this is a note 1";
    public static final String THIS_IS_A_NOTE_2 = "this is a note 2";
    public static final String DST = "dst";

    @Test
    public void testComments() throws Exception {
        int total = 5;
        ServiceClient sc = new ServiceClient(getDeploymentURL());
        CommentClient cc = new CommentClient(getDeploymentURL());
        int expectedHostCount = 1;
        HostClient hc = new HostClient(getDeploymentURL());
        DtoHostList hosts = ServiceIT.populateHosts(hc, expectedHostCount);
        String hostName = hosts.getHosts().get(0).getHostName();

        // create test data for hosts and services
        IntegrationTestContext<DtoService> context = ServiceIT.populateServices(sc, total, hostName, BULK_SERVICE_PREFIX);

        // create host comments
        DtoHost host = hc.lookup(hostName);
        int hostId = host.getId();
        cc.addHostComment(hostId, THIS_IS_A_NOTE_1, DST);
        cc.addHostComment(hostId, THIS_IS_A_NOTE_2, DST);

        // test service comments
        for (int ix = 0; ix < total; ix++) {
            String serviceName = context.formatNameKey(ix + 1);
            DtoService service = sc.lookup(serviceName, hostName);
            cc.addServiceComment(service.getId(),"comment " + serviceName, DST);
        }

        // assert host comments
        host = hc.lookup(hostName, DtoDepthType.Deep);
        assertThat(host).isNotNull();
        assertThat(host.getComments().get(0).getNotes().equals(THIS_IS_A_NOTE_1) || host.getComments().get(0).getNotes().equals(THIS_IS_A_NOTE_2)).isTrue();
        assertThat(host.getComments().get(0).getAuthor()).isEqualTo(DST);
        assertThat(host.getComments().get(1).getNotes().equals(THIS_IS_A_NOTE_1) || host.getComments().get(1).getNotes().equals(THIS_IS_A_NOTE_2)).isTrue();
        assertThat(host.getComments().get(1).getAuthor()).isEqualTo(DST);
        int hostCommentId1 = host.getComments().get(0).getId();
        int hostCommentId2 = host.getComments().get(1).getId();
        // shallow shouldn't retrieve comments
        host = hc.lookup(hostName, DtoDepthType.Shallow);
        assertThat(host.getComments()).isNullOrEmpty();
        host = hc.lookup(hostName);
        assertThat(host.getComments()).isNullOrEmpty();



        // assert service comments
        int [] ids = new int[total];
        int [] cids = new int[total];
        for (int ix = 0; ix < total; ix++) {
            String serviceName = context.formatNameKey(ix + 1);
            DtoService service = sc.lookup(serviceName, hostName, DtoDepthType.Deep);
            assertThat(service.getComments().get(0).getNotes()).isEqualTo("comment " + serviceName);
            assertThat(service.getComments().get(0).getAuthor()).isEqualTo(DST);
            ids[ix] = service.getId();
            cids[ix] = service.getComments().get(0).getId();
        }

        for (int ix = 0; ix < total; ix++) {
            String serviceName = context.formatNameKey(ix + 1);
            DtoService service = sc.lookup(serviceName, hostName); // shallow
            assertThat(service.getComments()).isNullOrEmpty();
            service = sc.lookup(serviceName, hostName, DtoDepthType.Shallow);
            assertThat(service.getComments()).isNullOrEmpty();
        }

        for (int ix = 0; ix < total; ix++) {
            DtoService service = sc.lookup(ids[ix]);
            assertThat(service.getComments().get(0).getNotes()).startsWith("comment ");
            assertThat(service.getComments().get(0).getAuthor()).isEqualTo(DST);
        }

        // test delete host comments
        host = hc.lookup(hostName, DtoDepthType.Deep);
        assertThat(host.getComments().size()).isEqualTo(2);
        cc.deleteHostComment(hostId, hostCommentId1);
        host = hc.lookup(hostName, DtoDepthType.Deep);
        assertThat(host.getComments().size()).isEqualTo(1);
        cc.deleteHostComment(hostId, hostCommentId2);
        host = hc.lookup(hostName, DtoDepthType.Deep);
        assertThat(host.getComments()).isNullOrEmpty();

        // test delete service comments
        for (int ix = 0; ix < total; ix++) {
            DtoService service = sc.lookup(ids[ix]);
            assertThat(service.getComments().size()).isEqualTo(1);
            cc.deleteServiceComment(ids[ix], cids[ix]);
        }
        for (int ix = 0; ix < total; ix++) {
            DtoService service = sc.lookup(ids[ix]);
            assertThat(service.getComments()).isNullOrEmpty();
        }

        // cleanup
        hc.delete(hostName);

    }

}
