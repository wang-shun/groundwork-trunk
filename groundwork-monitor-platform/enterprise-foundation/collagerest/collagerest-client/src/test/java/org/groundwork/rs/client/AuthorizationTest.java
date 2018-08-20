package org.groundwork.rs.client;

import org.groundwork.rs.dto.DtoBizAuthorization;
import org.groundwork.rs.dto.DtoBizAuthorizedServices;
import org.groundwork.rs.dto.DtoBizHostServiceInDowntime;
import org.groundwork.rs.dto.DtoBizHostServiceInDowntimeList;
import org.groundwork.rs.dto.DtoBizHostsAndServices;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoHostList;
import org.groundwork.rs.integration.IntegrationDataTool;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertNotNull;

public class AuthorizationTest extends AuthClientTest {

    private boolean enableEncryption = false;

    private final static String APP_NAME = "authTest";


    @Test
    public void testAdminPermitted() throws Exception {
        if (serverDown) return;
        enableEncryption = isEnableEncryption();
        AuthClient client = new AuthClient(getDeploymentURL());
        String username = System.getProperty(IntegrationDataTool.SYSTEM_PARAM_GWOS_REST_USER, RESTAPIACCESS_USERNAME);
        String password = System.getProperty(IntegrationDataTool.SYSTEM_PARAM_GWOS_REST_PW,
                (enableEncryption) ? SECURE_PASSWORD: RESTAPIACCESS_PASSWORD);
        AuthClient.Response response = client.login(username, password, APP_NAME);
        assert response.getStatus() == javax.ws.rs.core.Response.Status.OK;
        assert response.getToken() != null;

        // TEST Write Access Permitted
        HostClient hostClient = new HostClient(getDeploymentURL());
        DtoHost host = hostClient.lookup("localhost");
        assertNotNull(host);

        DtoHostList hosts = new DtoHostList();
        hosts.add(host);
        boolean denied = false;
        try {
            hostClient.post(hosts);
        }
        catch (CollageRestException cre) {
            denied = true;
        }
        assert denied == false;
        client.logout(APP_NAME, response.getToken());
    }

    @Test
    public void testReaderDenied() throws Exception {
        if (serverDown) return;
        enableEncryption = isEnableEncryption();
        AuthClient client = new AuthClient(getDeploymentURL());
        String username = System.getProperty(IntegrationDataTool.SYSTEM_PARAM_GWOS_REST_USER, READER_USERNAME);
        String password = System.getProperty(IntegrationDataTool.SYSTEM_PARAM_GWOS_REST_PW,
                (enableEncryption) ? READER_SECURE_PASSWORD: READER_PASSWORD);
        AuthClient.Response response = client.login(username, password, APP_NAME);
        assert response.getStatus() == javax.ws.rs.core.Response.Status.OK;
        assert response.getToken() != null;

        // TEST Read Access
        HostClient hostClient = new HostClient(getDeploymentURL());
        DtoHost host = hostClient.lookup("localhost");
        assertNotNull(host);

        DtoHostList hosts = new DtoHostList();
        hosts.add(host);
        boolean denied = false;
        try {
            hostClient.post(hosts);
        }
        catch (CollageRestException cre) {
            assert cre.getStatus() == 401;
            denied = true;
        }
        assert denied;
        client.logout(APP_NAME, response.getToken());
    }

    @Test
    public void testReaderSpecialCases() {
        if (serverDown) return;
        enableEncryption = isEnableEncryption();
        AuthClient client = new AuthClient(getDeploymentURL());
        String username = System.getProperty(IntegrationDataTool.SYSTEM_PARAM_GWOS_REST_USER, READER_USERNAME);
        String password = System.getProperty(IntegrationDataTool.SYSTEM_PARAM_GWOS_REST_PW,
                (enableEncryption) ? READER_SECURE_PASSWORD: READER_PASSWORD);
        AuthClient.Response response = client.login(username, password, APP_NAME);
        assert response.getStatus() == javax.ws.rs.core.Response.Status.OK;
        assert response.getToken() != null;

        // set host group in downtime
        BizClient bizClient = new BizClient(getDeploymentURL());

        // get host group in downtime
        DtoBizHostServiceInDowntimeList hostGroupInDowntime = new DtoBizHostServiceInDowntimeList();
        DtoBizHostServiceInDowntime localhost = new DtoBizHostServiceInDowntime("localhost");
        hostGroupInDowntime.add(localhost);

        DtoBizHostServiceInDowntimeList result = bizClient.getInDowntime(hostGroupInDowntime);
        assertNotNull(result);

        boolean denied = false;
        try {
            DtoBizHostsAndServices hostGroup = new DtoBizHostsAndServices();
            hostGroup.setHostGroupNames(Arrays.asList(new String[]{"Linux Servers"}));
            hostGroup.setSetHosts(true);
            bizClient.setInDowntime(hostGroup);
        }
        catch (CollageRestException cre) {
            assert cre.getStatus() == 401;
            denied = true;
        }
        assert denied;

        List<String> authorizedHostGroups = Arrays.asList(new String[]{"Linux Servers"});
        DtoBizAuthorization authorization = new DtoBizAuthorization(authorizedHostGroups, null);
        DtoBizAuthorizedServices authorizedServices = bizClient.getAuthorizedServices(authorization);
        assertNotNull(authorizedServices);

    }
}
