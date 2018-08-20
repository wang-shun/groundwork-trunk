package org.groundwork.rs.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.DefaultHttpClient;
import org.groundwork.foundation.ws.impl.WSClientConfiguration;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.integration.IntegrationDataTool;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
import org.junit.BeforeClass;

import javax.ws.rs.core.Response;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

public class AbstractClientTest {

    protected static boolean serverDown = false;
    protected static Log log = LogFactory.getLog(AbstractClientTest.class);
    protected static String deploymentUrl;

    protected static final TimeZone INTEGRATION_TEST_DATA_TIMEZONE = TimeZone.getTimeZone("PST8PDT");

    private static final String PING_SERVICE = "/hosts/localhost";
    static public final String AGENT_84 = "5437840f-a908-49fd-88bd-e04543a69e84";
    static public final String AGENT_85 = "5437840f-a908-49fd-88bd-e04543a69e85";


    public static String getDeploymentURL() {
        return deploymentUrl;
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
        deploymentUrl = System.getProperty(IntegrationDataTool.SYSTEM_PARAM_GWOS_REST_API, IntegrationDataTool.DEFAULT_DEPLOYMENT_URL);
        System.out.println("**** Unit Test starting with URL " + deploymentUrl + "... ");
        String userName = System.getProperty(IntegrationDataTool.SYSTEM_PARAM_GWOS_REST_USER);
        String password = System.getProperty(IntegrationDataTool.SYSTEM_PARAM_GWOS_REST_PW);
        String app = System.getProperty(IntegrationDataTool.SYSTEM_PARAM_GWOS_REST_APP, BaseRestClient.APP_NAME);
        if (userName != null && password != null) {
            AuthClient authClient = new AuthClient(deploymentUrl);
            AuthClient.Response response = authClient.login(userName, password, app);
            assert response.getStatus() == Response.Status.OK;
        }

        // ensure basic connection point works
//        String ping = DEPLOYMENT_URL + PING_SERVICE;
//        ClientResponse<String> response = null;
//        try {
//            ClientRequest request = createSingleClientRequest(ping);
//            response = request.get(String.class);
//            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
//        } catch (HttpHostConnectException e) {
//            if (e.toString().endsWith("refused")) {
//                log.warn("Could not connect to " + ping + ". Server is not running. Continuing with tests ...");
//                serverDown = true;
//            } else {
//                e.printStackTrace();
//                throw e;
//            }
//        }
//        finally {
//            if (response != null)
//                response.releaseConnection();
//        }
    }

    protected DtoHost lookupHost(String hostName) throws Exception {
        HostClient client = new HostClient(deploymentUrl);
        return client.lookup(hostName);
    }

    protected Date parseDate(String date) {
        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            format.setTimeZone(INTEGRATION_TEST_DATA_TIMEZONE);
            return format.parse(date);
        } catch (Exception e) {
        }
        return null;
    }

    protected void assertDatesEqual(int year, int month, int day, int hour, int minute, Date actual) {
        assertDatesEqual(year, month, day, hour, minute, -1, actual);
    }

    protected void assertDatesEqual(int year, int month, int day, int hour, int minute, int seconds, Date actual) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(INTEGRATION_TEST_DATA_TIMEZONE);
        calendar.setTime(actual);
        assertEquals(year, calendar.get(Calendar.YEAR));
        assertEquals(month, calendar.get(Calendar.MONTH));
        assertEquals(day, calendar.get(Calendar.DAY_OF_MONTH));
        assertEquals(hour, calendar.get(Calendar.HOUR));
        assertEquals(minute, calendar.get(Calendar.MINUTE));
        if (seconds != -1)
            assertEquals(seconds, calendar.get(Calendar.SECOND));
    }

    protected void assertDatesEqual(Date test, Date actual) {
        Calendar calendarTest = Calendar.getInstance();
        calendarTest.setTime(test);
        Calendar calendarActual = Calendar.getInstance();
        calendarActual.setTime(actual);
        assertEquals(calendarTest.get(Calendar.YEAR), calendarActual.get(Calendar.YEAR));
        assertEquals(calendarTest.get(Calendar.MONTH), calendarActual.get(Calendar.MONTH));
        assertEquals(calendarTest.get(Calendar.DAY_OF_MONTH), calendarActual.get(Calendar.DAY_OF_MONTH));
        assertEquals(calendarTest.get(Calendar.HOUR), calendarActual.get(Calendar.HOUR));
        assertEquals(calendarTest.get(Calendar.MINUTE), calendarActual.get(Calendar.MINUTE));
        assertEquals(calendarTest.get(Calendar.SECOND), calendarActual.get(Calendar.SECOND));
    }

    private static ClientRequest createSingleClientRequest(String url) {
        String username = WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_USERNAME);
        String password = WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_PASSWORD);
        Credentials credentials = new UsernamePasswordCredentials(username, password);
        DefaultHttpClient httpClient = new DefaultHttpClient();
        httpClient.getCredentialsProvider().setCredentials(org.apache.http.auth.AuthScope.ANY, credentials);
        ApacheHttpClient4Executor executor = new ApacheHttpClient4Executor(httpClient);
        ClientRequest request = new ClientRequest(url, executor);
        request = request.followRedirects(true);
        return request;
    }

}
