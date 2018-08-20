package org.groundwork.downtime;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.groundwork.downtime.DtoDowntime;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.junit.Test;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class DowntimePrototypeTest {

//    public static final String GATEWAY_LOGIN_URL = "http://localhost/josso/signon/usernamePasswordLogin.do";
//    public static final String GATEWAY_REDIRECT_LOGIN = "http://localhost/portal/initiatessologin";
//    public static final String GATEWAY_LOGOUT_URL = "http://localhost/portal/classic/?portal:componentId=UIPortal&portal:action=Logout";

    public static final String GATEWAY_LOGIN_URL = "http://charon.groundwork.groundworkopensource.com/josso/signon/usernamePasswordLogin.do";
    public static final String GATEWAY_REDIRECT_LOGIN = "http://charon.groundwork.groundworkopensource.com/portal/initiatessologin";
    public static final String GATEWAY_LOGOUT_URL = "http://charon.groundwork.groundworkopensource.com/portal/classic/?portal:componentId=UIPortal&portal:action=Logout";

    public static final String DOWNTIME_URL
            = "http://charon.groundwork.groundworkopensource.com/nms-rstools/php/rstools/index.php?r=downTime/api/downtimes/";

    public static final String DOWNTIME_UI_URL
            = "http://charon.groundwork.groundworkopensource.com/portal/classic/config/downtimes/downtimes-list";
    public static final String DOWNTIME_IFRAME = "http://charon.groundwork.groundworkopensource.com/nms-rstools/php/rstools/index.php?r=downTime/default/list&gwuid=admin";
    //public static final String DOWNTIME_IFRAME = "http://charon.groundwork.groundworkopensource.com/nms-rstools/php/rstools/protected/securetest";

    public static final String DOWNTIME_LOGIN = "http://charon.groundwork.groundworkopensource.com/josso/signon/login.do?josso_cmd=login_optional&josso_back_to=http://charon.groundwork.groundworkopensource.com/nms-rstools/josso_security_check&josso_partnerapp_id=nms-rstools";
    public static final String ASSERTION_CHECK = "http://charon.groundwork.groundworkopensource.com/nms-rstools/josso_security_check?josso_assertion_id=708CD8516D6EB74A";

    public static final String REDIRECT = "http://charon.groundwork.groundworkopensource.com/josso/signon/login.do?josso_cmd=login_optional&josso_back_to=http://charon.groundwork.groundworkopensource.com/nms-rstools/josso_security_check&josso_partnerapp_id=nms-rstools";
    @Test
    public void backdoorTest() throws Exception {
        String JSESSIONID = "fN+nj1b1VYIQ0u1blP3W3OGo";
        Cookie jsession = new Cookie("JSESSIONID", JSESSIONID, "/", null);
        List<Cookie> cookies = new ArrayList<>();
        cookies.add(jsession);
        String result = makeRequest("http://charon.groundwork.groundworkopensource.com/nms-rstools/php/rstools/index.php?r=downTime/downtimeschedule/admin&recurring", cookies, false);
        //String result = makeRequest(DOWNTIME_IFRAME, cookies, false);
        String result2 = makeRequest(result, cookies, false);
        System.out.println(result2);
        String result3 = makeRequest(result2, cookies, false);
        System.out.println(result3);
        String result4 = makeRequest(result3, cookies, false);
        System.out.println(result4);
        String result5 = makeRequest(result4, cookies, false);
        System.out.println(result4);

    }

    private String makeRequest(String url, List<Cookie> cookies, boolean follow) throws Exception {
        ClientRequest request = new ClientRequest(url);
        //ClientRequest request = new ClientRequest(DOWNTIME_UI_URL);
        //request.header("Content-Type", "text/html");
        request.header("Accept", "text/html");
        for (Cookie cookie : cookies) {
            request.cookie(cookie);
        }
        request.followRedirects(follow);
        ClientResponse<DtoDowntime> response = request.get();
        int status = response.getStatus();
        Map headers = response.getHeaders();
        List<Cookie> phpCookies = parseCookies((List<String>)headers.get("Set-Cookie"));
        //cookies.addAll(phpCookies);
        addCookies(phpCookies, cookies);
        String payload = (String)response.getEntity(String.class);
        System.out.println("PAYLOAD: " + payload);
        response.releaseConnection();
        Object loc = headers.get("Location");
        if (loc != null) {
            ArrayList<String> locations = (ArrayList<String>)loc;
            return locations.get(0);
        }
        return null;
    }

    private void addCookies(List<Cookie> phpCookies, List<Cookie> cookies) {
        List<Cookie> newones = new ArrayList<>();
        for (Cookie php : phpCookies) {
            boolean found = false;
            for (Cookie cookie : cookies) {
                if (php.getName().equals(cookie.getName()) && php.getPath().equals(cookie.getPath())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                newones.add(php);
            }
        }
        cookies.addAll(newones);
    }

    @Test
    public void testDowntime() throws Exception {
        List<Cookie> cookies = login();
//        ClientRequest request = new ClientRequest(DOWNTIME_URL);
        ClientRequest request = new ClientRequest("http://charon.groundwork.groundworkopensource.com/nms-rstools/php/rstools/index.php?r=downTime/api/downtimes/?sg=test-service");
        //ClientRequest request = new ClientRequest("http://charon.groundwork.groundworkopensource.com/nms-rstools/php/rstools/index.php?r=downTime/api/downtimes/?S=1509515400&d=2000");
        request.header("Content-Type", "application/json");
        request.header("Accept", "application/json");
        for (Cookie cookie : cookies) {
            request.cookie(cookie);
        }
        request.followRedirects(true);
        ClientResponse<DtoDowntime> response = request.get();
        int status = response.getStatus();
        Map headers = response.getHeaders();
        String payload = (String)response.getEntity(String.class);
        //List<DtoDowntime> downtimes = response.getEntity(new GenericType<List<DtoDowntime>>(){});
        ObjectMapper mapper = new ObjectMapper();
        List<DtoDowntime> downtimes = mapper.readValue(payload, new TypeReference<List<DtoDowntime>>(){});

        List<Cookie> phpCookies = parseCookies((List<String>)headers.get("Set-Cookie"));
        cookies.addAll(phpCookies);
        response.releaseConnection();

//        request = new ClientRequest(DOWNTIME_URL);
//        request.header("Content-Type", "application/json");
//        request.header("Accept", "application/json");
//        for (Cookie cookie : cookies) {
//            request.cookie(cookie);
//        }
//        request.followRedirects(true);
//        response = request.get();
//        status = response.getStatus();
//        headers = response.getHeaders();
//        String result = (String)response.getEntity(String.class);
//        assert result != null;
        logout(cookies);
    }

    private void logout(List<Cookie> cookies) throws Exception {
        ClientRequest request = new ClientRequest(GATEWAY_LOGOUT_URL);
        request.followRedirects(true);
        for (Cookie cookie : cookies) {
            request.cookie(cookie);
        }
        ClientResponse response = request.get();
        int status = response.getStatus();
        Map<String,Object> headers = response.getHeaders();
        String result = (String)response.getEntity(String.class);
        //List<Cookie> newCookies = parseCookies((List<String>)headers.get("Set-Cookie"));
        response.releaseConnection();

    }

    private List<Cookie> login() throws Exception {
        ClientRequest request = new ClientRequest(GATEWAY_LOGIN_URL);
        request.header("custom-header", "value");
        request.formParameter("josso_username", "admin");
        request.formParameter("josso_password", "admin");
        request.formParameter("josso_cmd", "login");
        request.formParameter("josso_back_to", GATEWAY_REDIRECT_LOGIN);
        request.followRedirects(true);
        ClientResponse response = request.post();
        int status = response.getStatus();
        Map<String,Object> headers = response.getHeaders();
        String result = (String)response.getEntity(String.class);
        List<Cookie> newCookies = parseCookies((List<String>)headers.get("Set-Cookie"));
        response.releaseConnection();
        return newCookies;
    }

    @Test
    public void testJOSSOFailure() throws Exception {
        ClientRequest request = new ClientRequest(GATEWAY_LOGIN_URL);
        request.header("custom-header", "value");
        request.formParameter("josso_username", "admin");
        request.formParameter("josso_password", "adminbad"); // bad password
        request.formParameter("josso_cmd", "login");
        request.formParameter("josso_back_to", GATEWAY_REDIRECT_LOGIN);
        ClientResponse response = request.post();
        int status = response.getStatus();
        assertEquals(status, 200);
        Map<String,String> headers = response.getHeaders();
        String result = (String)response.getEntity(String.class);
        assertTrue(result.contains("Invalid Authentication Information"));
    }

    @Test
    public void testLogin2() throws Exception {
        ClientRequest request = new ClientRequest(GATEWAY_LOGIN_URL);
        request.header("custom-header", "value");
        request.formParameter("josso_username", "admin");
        request.formParameter("josso_password", "admin");
        request.formParameter("josso_cmd", "login");
        request.formParameter("josso_back_to", GATEWAY_REDIRECT_LOGIN);
        request.followRedirects(true);
        ClientResponse response = request.post();
        int status = response.getStatus();
        assertEquals(status, 302);
        Map<String,Object> headers = response.getHeaders();
        String result = (String)response.getEntity(String.class);
        assertFalse(result.contains("Invalid Authentication Information"));
        assertTrue(headers.containsKey("Set-Cookie"));
        List<Cookie> newCookies = parseCookies((List<String>)headers.get("Set-Cookie"));
        assert newCookies.size() == 2;
        response.releaseConnection();

        request = new ClientRequest("http://localhost/portal/classic/status");
        request.followRedirects(true);
        for (Cookie cookie : newCookies) {
            request.cookie(cookie);
        }
        result = request("http://localhost/portal/classic/status", newCookies);
        assertTrue(result.contains("My GroundWork"));
        assertTrue(result.contains("Getting started"));

        result = request("http://localhost/portal/classic/status", newCookies);
        assertTrue(result.contains("My GroundWork"));
        assertTrue(result.contains("Entire Network"));
        response.releaseConnection();

    }

    private String request(String url, List<Cookie> cookies) throws Exception {
        ClientRequest request = new ClientRequest(url);
        request.followRedirects(true);
        for (Cookie cookie : cookies) {
            request.cookie(cookie);
        }
        request.accept(MediaType.TEXT_HTML_TYPE);
        //request.header();
        ClientResponse response = request.get();
        int status = response.getStatus();
        Map<String,Object> headers = response.getHeaders();
        String result = (String)response.getEntity(String.class);
        assertEquals(status, 200);
        response.releaseConnection();
        return result;
    }

    @Test
    public void testParse() throws Exception {
        String c1 = "JSESSIONID=D8BEC1D9B56AD7FE6A72FA4D6F8291B5; Path=/josso";
        String c2 = "JOSSO_SESSIONID_josso=05F9B645B913A4A0BC969A9BFD07DF1E; Path=/josso";
        String[] pair = c1.split("=");
        List<HttpCookie> cookies = HttpCookie.parse(c1);

    }

    @Test
    public void testLogin() throws Exception {

        DefaultHttpClient client = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(GATEWAY_LOGIN_URL);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("josso_username", "admin"));
        params.add(new BasicNameValuePair("josso_password", "admin"));
        params.add(new BasicNameValuePair("josso_cmd", "admin"));
        httpPost.setEntity(new UrlEncodedFormEntity(params));
        HttpResponse response = client.execute(httpPost);
        assertThat(response.getStatusLine().getStatusCode(), equalTo(302));
        response.getAllHeaders();
    }

    private List<Cookie> parseCookies(List<String> cookies) {
        List<Cookie> newCookies = new ArrayList<>();
        if (cookies == null) return newCookies;
        for (String cookie : cookies) {
            List<HttpCookie> cook = HttpCookie.parse(cookie);
            assert cook != null;
            if (cook.size() == 0) continue;
            HttpCookie c = cook.get(0);
            newCookies.add(new Cookie(c.getName(), c.getValue(), c.getPath(), null));
        }
        return newCookies;
    }

    private final String DOWNTIME_RECORD = "{\n" +
            "    \"iddowntimeschedule\": 1,\n" +
            "    \"fixed\": \"fixme\",\n" +
            "    \"host\": \"test-57\",\n" +
            "    \"service\": \"test-service\",\n" +
            "    \"hostgroup\": \"HG-1\",\n" +
            "    \"servicegroup\": \"SG-1\",\n" +
            "    \"author\": \"admin\",\n" +
            "    \"description\": \"David DownTime\",\n" +
            "    \"start\": \"2017-10-31 22:50:00\",\n" +
            "    \"end\": \"2017-11-03 23:50:00\",\n" +
            "    \"duration\": 4380,\n" +
            "    \"apptype\": \"mixed\"\n" +
            "  }";

    private final String DOWNTIME_RECORDS = "[{\n" +
            "    \"iddowntimeschedule\": 1,\n" +
            "    \"fixed\": \"fixme\",\n" +
            "    \"host\": \"test-57\",\n" +
            "    \"service\": \"test-service\",\n" +
            "    \"hostgroup\": \"HG-1\",\n" +
            "    \"servicegroup\": \"SG-1\",\n" +
            "    \"author\": \"admin\",\n" +
            "    \"description\": \"David DownTime\",\n" +
            "    \"start\": \"2017-10-31 22:50:00\",\n" +
            "    \"end\": \"2017-11-03 23:50:00\",\n" +
            "    \"duration\": 4380,\n" +
            "    \"apptype\": \"mixed\"\n" +
            "  }]";

    @Test
    public void testMapper() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DtoDowntime downtime = mapper.readValue(DOWNTIME_RECORD, DtoDowntime.class);
        assert downtime != null;
        List<DtoDowntime> downtimes = mapper.readValue(DOWNTIME_RECORDS, new TypeReference<List<DtoDowntime>>(){});
        assert downtimes != null;
    }


}
