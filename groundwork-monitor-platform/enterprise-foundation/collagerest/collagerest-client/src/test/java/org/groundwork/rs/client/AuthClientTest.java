package org.groundwork.rs.client;

import org.groundwork.foundation.ws.impl.WSClientConfiguration;
import org.groundwork.rs.integration.IntegrationDataTool;
import org.junit.Test;

public class AuthClientTest  extends AbstractClientTest {

    public static final String RESTAPIACCESS_USERNAME = "RESTAPIACCESS";
    public static final String RESTAPIACCESS_PASSWORD = "RESTAPIACCESSPASSWORD";
    public static final String SECURE_PASSWORD = "7UZZVvnLbuRNk12Yk5H33zeYdWQpnA7j9shir7QfJgwh";
    public static final String READER_USERNAME = "RESTAPIREADERACCESS";
    public static final String READER_PASSWORD = "RESTAPIACCESSREADERPASSWORD";
    public static final String READER_SECURE_PASSWORD = "5p4LyKS3ZfAtsg1AZQ19BDeozC3nU2NEahP542yZUVur6C811TP3ezF";

    private boolean enableEncryption = false;

    @Test
    public void testAuthentication() throws Exception {
        if (serverDown) return;
        boolean failed = false;
        enableEncryption = isEnableEncryption();
        AuthClient client = new AuthClient(getDeploymentURL());
        try {
            AuthClient.Response response = client.login("bad", "bad", "badapp");
        }
        catch (CollageRestException e) {
            failed = true;
        }
        assert failed;

        String username = System.getProperty(IntegrationDataTool.SYSTEM_PARAM_GWOS_REST_USER, RESTAPIACCESS_USERNAME);
        String password = System.getProperty(IntegrationDataTool.SYSTEM_PARAM_GWOS_REST_PW,
                (enableEncryption) ? SECURE_PASSWORD : RESTAPIACCESS_PASSWORD);
        AuthClient.Response response = client.login(username, password, "cloudhub");
        assert response.getStatus() == javax.ws.rs.core.Response.Status.OK;
        assert response.getToken() != null;
        client.logout("cloudhub", response.getToken());
    }

    @Test
    public void testReaderAuthentication() throws Exception {
        if (serverDown) return;
        boolean failed = false;
        enableEncryption = isEnableEncryption();
        AuthClient client = new AuthClient(getDeploymentURL());

        String username = System.getProperty(IntegrationDataTool.SYSTEM_PARAM_GWOS_REST_USER, READER_USERNAME);
        String password = System.getProperty(IntegrationDataTool.SYSTEM_PARAM_GWOS_REST_PW,
                (enableEncryption) ? READER_SECURE_PASSWORD: READER_PASSWORD);
        AuthClient.Response response = client.login(username, password, "cloudhub2");
        assert response.getStatus() == javax.ws.rs.core.Response.Status.OK;
        assert response.getToken() != null;
        client.logout("cloudhub2", response.getToken());
    }

    boolean isEnableEncryption() {
        String strEncryptionEnabled = WSClientConfiguration.getProperty(WSClientConfiguration.ENCRYPTION_ENABLED);
        if (strEncryptionEnabled == null) {
            strEncryptionEnabled = "true";
        } // end if
        return Boolean.parseBoolean(strEncryptionEnabled);
    }
}


