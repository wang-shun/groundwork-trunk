package org.groundwork.rs.it;

import org.groundwork.rs.client.AgentClient;
import org.groundwork.rs.client.AuthClient;
import org.groundwork.rs.dto.DtoOperationResults;
import org.groundwork.rs.dto.DtoStatistic;
import org.groundwork.rs.integration.IntegrationDataTool;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.util.List;

public abstract class AbstractIntegrationTest {

    static String token;
    static String deploymentUrl;

    static final String APP_NAME = "GWOS-IT";
    public static final String AGENT_ID = "IT_AGENT";
    public static final String BULK_HOST_PREFIX = "ZHOST";
    public static final String BULK_HOST_PREFIX2 = "QHOST";
    public static final String BULK_HG_PREFIX = "ZHG";
    public static final String BULK_SERVICE_PREFIX = "ZSERVICE";
    public static final String BULK_SERVICE_PREFIX2 = "QSERVICE";

    @BeforeClass
    public static void beforeTest() {
        deploymentUrl = System.getProperty(IntegrationDataTool.SYSTEM_PARAM_GWOS_REST_API, IntegrationDataTool.DEFAULT_DEPLOYMENT_URL);
        token = login();
        cleanup();
    }

    @AfterClass
    public static void afterTest() {
        logout(token);
    }

    public static String getDeploymentURL() {
        return deploymentUrl;
    }

    protected static String login() {
        AuthClient client = new AuthClient(getDeploymentURL());
        String username = System.getProperty(IntegrationDataTool.SYSTEM_PARAM_GWOS_REST_USER, "RESTAPIACCESS");
        String password = System.getProperty(IntegrationDataTool.SYSTEM_PARAM_GWOS_REST_PW, "RESTAPIACCESSPASSWORD");
        AuthClient.Response response = client.login(username, password, APP_NAME);
        token = response.getToken();
        return token;
    }

    protected static void logout(String token) {
        AuthClient client = new AuthClient(getDeploymentURL());
        client.logout(APP_NAME, token);
    }

    protected static void cleanup() {
        // RESET and delete all hosts, host groups and services in case of error
        AgentClient agentClient = new AgentClient(getDeploymentURL());
        DtoOperationResults results = agentClient.delete(AGENT_ID);
    }

    public static long countStatisticsByType(List<DtoStatistic> statistics, String type) {
        for (DtoStatistic statistic : statistics) {
            if (statistic.getName().equals(type)) {
                return statistic.getCount();
            }
        }
        return 0;
    }

}
