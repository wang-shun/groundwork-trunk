package com.groundwork.downtime.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.groundwork.dashboard.NocConfiguration;
import com.groundwork.dashboard.configuration.DashboardConfigurationException;
import com.groundwork.downtime.DowntimeContext;
import com.groundwork.downtime.DowntimeException;
import com.groundwork.downtime.DowntimeMaintenanceWindow;
import com.groundwork.downtime.DowntimeService;
import com.groundwork.downtime.DtoDowntime;
import org.apache.log4j.Logger;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.ws.rs.core.Cookie;
import java.io.File;
import java.io.FileWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.net.URLEncoder;
import java.util.*;

/**
 * This service makes requests to the RealStuff Downtime API over Http
 * The login approach of piggybacking on the JSESSIONID is not yet working
 * Instead, full JBoss credentials are required in its current state
 */
public class DowntimeHttpService implements DowntimeService {

    private static Logger log = Logger.getLogger(DowntimeHttpService.class);

    public static final String GATEWAY_LOGIN_URL = "josso/signon/usernamePasswordLogin.do";
    public static final String GATEWAY_REDIRECT_LOGIN = "portal/initiatessologin";
    public static final String GATEWAY_LOGOUT_URL = "/portal/classic/?portal:componentId=UIPortal&portal:action=Logout";

    public static final String DOWNTIME_URL = "nms-rstools/php/rstools/index.php?r=downTime/api/downtimes/";
    public static final String DOWNTIME_PING_URL = "nms-rstools/php/rstools/index.php";
    public static final String DOWNTIME_RANGE_URL = "&range=starttime='%s' and endtime = '%s'";
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    private Map<String, List<DowntimeMaintenanceWindow>> cache = new HashMap<>();
    private DateTime cacheLastAccess = null;

    @Override
    public Map<String, List<DowntimeMaintenanceWindow>> range(DowntimeContext context, Date startRange, Date endRange) throws DowntimeException {
        Integer expiration = NocConfiguration.getIntegerProperty(NocConfiguration.NOC_DOWNTIME_CACHE_SECONDS);
        if (expiration > 0) {
            DateTime now = new DateTime();
            if (cacheLastAccess != null && now.isBefore(cacheLastAccess.plusSeconds(expiration))) {
                if (log.isDebugEnabled()) {
                    log.debug("returning cached downtimes, size: " + cache.size());
                }
                return cache;
            }
            if (log.isInfoEnabled()) {
                log.info("downtimes cache expired, size: " + cache.size());
            }
            return cache;
        }
        ClientResponse<DtoDowntime> response = null;
        Map<String, List<DowntimeMaintenanceWindow>> transitions = new HashMap<>();
        try {
            StringBuffer url = new StringBuffer();
            url.append(DOWNTIME_URL);
            url.append(encode(String.format(DOWNTIME_RANGE_URL, DATE_TIME_FORMATTER.print(new DateTime(startRange)), DATE_TIME_FORMATTER.print(new DateTime(endRange)))));
            ClientRequest request = new ClientRequest(makeURL(context.getGroundworkServer(), url.toString()));
            request.header("Content-Type", "application/json");
            request.header("Accept", "application/json");
            for (Cookie cookie : context.getCredentials()) {
                request.cookie(cookie);
            }
            request.followRedirects(true);
            response = request.get();
            int status = response.getStatus();
            if (log.isDebugEnabled()) {
                log.debug("http status:" + status);
            }
            String payload = (String) response.getEntity(String.class);
            ObjectMapper mapper = new ObjectMapper();
            mapper.setTimeZone(TimeZone.getDefault());
            List<DtoDowntime> downTimes = mapper.readValue(payload, new TypeReference<List<DtoDowntime>>() {
            });
            for (DtoDowntime downtime : downTimes) {  // new window is added to transitions
                TransitionWindowCalculator.calculateTransitionWindow(transitions, downtime, startRange, endRange);
            }
            Map<String, List<DowntimeMaintenanceWindow>> transitionsWithGaps = new HashMap<>();
            for (Map.Entry<String, List<DowntimeMaintenanceWindow>> entry : transitions.entrySet()) {
                List<DowntimeMaintenanceWindow> withGaps = TransitionWindowCalculator.addGapsToWindowList(entry.getValue(), startRange, endRange);
                transitionsWithGaps.put(entry.getKey(), withGaps);
            }
            synchronized (cache) {
                cache = transitionsWithGaps;
                cacheLastAccess = new DateTime();
            }
            return transitionsWithGaps;
        } catch (Exception e) {
            log.error(e);
            throw new DowntimeException(e);
        } finally {
            if (response != null) {
                response.releaseConnection();
            }
        }
    }

    @Override
    public boolean ping(DowntimeContext context) {
        ClientResponse<DtoDowntime> response = null;
        try {
            StringBuffer url = new StringBuffer();
            url.append(DOWNTIME_PING_URL);
            ClientRequest request = new ClientRequest(makeURL(context.getGroundworkServer(), url.toString()));
            for (Cookie cookie : context.getCredentials()) {
                request.cookie(cookie);
            }
            request.followRedirects(true);
            response = request.get();
            int status = response.getStatus();
            String payload = (String) response.getEntity(String.class);
            if (payload.length() == 0) {
                return false;
            }
            if (!payload.contains("RSTools")) {
                return false;
            }
        } catch (Exception e) {
            log.error(e);
            return false;
        } finally {
            if (response != null) {
                response.releaseConnection();
            }
        }
        return true;
    }

    @Override
    public List<DowntimeMaintenanceWindow> lookup(String hostName, String serviceName, Map<String, List<DowntimeMaintenanceWindow>> maintenanceWindows) {
        if (maintenanceWindows == null) {
            return createNoneScheduledWindow();
        }
        List<DowntimeMaintenanceWindow> windows = maintenanceWindows.get(TransitionWindowCalculator.makeKey(hostName, serviceName));
        if (windows == null) {
            return createNoneScheduledWindow();
        }
        return windows;
    }

    protected List<DowntimeMaintenanceWindow> createNoneScheduledWindow() {
        DowntimeMaintenanceWindow window = new DowntimeMaintenanceWindow(
                DowntimeMaintenanceWindow.MaintenanceStatus.None, 0.00f, "None Scheduled");
        List<DowntimeMaintenanceWindow> windows = new LinkedList<>();
        windows.add(window);
        return windows;
    }

    @Override
    public List<DtoDowntime> list(DowntimeContext context) throws DashboardConfigurationException {
        return listInternal(context, null);
    }


    protected List<DtoDowntime> listInternal(DowntimeContext context, String serviceGroup) throws DashboardConfigurationException {
        ClientResponse<DtoDowntime> response = null;
        try {
            // r=downTime/api/downtimes&range=starttime='2018-03-01 10:00:00' and endtime = '2018-03-30 10:47:01'
            String path = ((serviceGroup) == null) ? DOWNTIME_URL : DOWNTIME_URL + "sg=" + encode(serviceGroup);
            ClientRequest request = new ClientRequest(makeURL(context.getGroundworkServer(), path));
            request.header("Content-Type", "application/json");
            request.header("Accept", "application/json");
            for (Cookie cookie : context.getCredentials()) {
                request.cookie(cookie);
            }
            request.followRedirects(true);
            response = request.get();
            int status = response.getStatus();
            Map headers = response.getHeaders();
            String payload = (String) response.getEntity(String.class);
//            FileWriter fw = new FileWriter(new File("/home/ec2-user/dst/response.txt"));
//            fw.write(payload);
//            fw.close();
            ObjectMapper mapper = new ObjectMapper();
            List<DtoDowntime> downtimes = mapper.readValue(payload, new TypeReference<List<DtoDowntime>>() {
            });
            //List<Cookie> phpCookies = parseCookies((List<String>) headers.get("Set-Cookie"));
            //cookies.addAll(phpCookies);
            return downtimes;
        } catch (Exception e) {
            e.printStackTrace();
            throw new DowntimeException(e);
        } finally {
            if (response != null) {
                response.releaseConnection();
            }
        }
    }

    @Override
    public DowntimeContext relogin(DowntimeContext context) throws DowntimeException {
        return login(context.getGroundworkServer(), context.getUsername(), context.getPassword());
    }

    @Override
    public DowntimeContext login(String groundworkServer, String username, String password) throws DowntimeException {
        try {
            ClientRequest request = new ClientRequest(makeURL(groundworkServer, GATEWAY_LOGIN_URL));
            request.formParameter("josso_username", username);
            request.formParameter("josso_password", password);
            request.formParameter("josso_cmd", "login");
            request.formParameter("josso_back_to", makeURL(groundworkServer, GATEWAY_REDIRECT_LOGIN));
            request.followRedirects(true);
            ClientResponse response = request.post();
            int status = response.getStatus();
            Map<String, Object> headers = response.getHeaders();
            String result = (String) response.getEntity(String.class);
            List<Cookie> newCookies = parseCookies((List<String>) headers.get("Set-Cookie"));
            response.releaseConnection();
            return new DowntimeContext(groundworkServer, newCookies, true, username, password);
        } catch (Exception e) {
            throw new DowntimeException(e);
        }
    }

    @Override
    public void logout(DowntimeContext context) throws DowntimeException {
        try {
            // http://localhost/josso/signon/logout.do?josso_back_to=http://localhost:8080/portal/classic/?portal%3AcomponentId%3DUIPortal%26portal%3Aaction%3DLogout&josso_partnerapp_id=portal
            //ClientRequest request = new ClientRequest(makeURL(context.getGroundworkServer(), GATEWAY_LOGOUT_URL));
            ClientRequest request = new ClientRequest(makeURL(context.getGroundworkServer(), "/josso/signon/logout.do?josso_partnerapp_id=portal"));
            request.followRedirects(true);
            for (Cookie cookie : context.getCredentials()) {
                request.cookie(cookie);
            }
            ClientResponse response = request.get();
            int status = response.getStatus();
            Map<String, Object> headers = response.getHeaders();
            String result = (String) response.getEntity(String.class);
            response.releaseConnection();
            context.setLoggedOn(false);
        } catch (Exception e) {
            throw new DowntimeException(e);
        }
    }


    protected String makeURL(String base, String path) {
        if (base.endsWith("/"))
            return base + path;
        else
            return base + "/" + path;
    }

    protected DowntimeContext createContext(String groundworkServer, javax.servlet.http.Cookie[] credentials) {
        List<javax.ws.rs.core.Cookie> cookies = new ArrayList<>();
        for (javax.servlet.http.Cookie servletCookie : credentials) {
            //if (servletCookie.getName().equals("JSESSIONID")) {
            javax.ws.rs.core.Cookie cookie = new javax.ws.rs.core.Cookie(servletCookie.getName(), servletCookie.getValue(),
                    servletCookie.getPath(), servletCookie.getDomain(), servletCookie.getVersion());
            cookies.add(cookie);
            //}
        }
        return new DowntimeContext(groundworkServer, cookies);
    }

    protected List<Cookie> debugRequest(String groundworkServer, javax.servlet.http.Cookie[] cookies) {
        ClientResponse<DtoDowntime> response = null;
        try {
            ClientRequest request = new ClientRequest(makeURL(groundworkServer, GATEWAY_REDIRECT_LOGIN));
            DowntimeContext context = createContext(groundworkServer, cookies);
            for (Cookie cookie : context.getCredentials()) {
                request.cookie(cookie);
            }
            request.followRedirects(true);
            response = request.get();
            int status = response.getStatus();
            System.out.println("** status " + status);
            Map headers = response.getHeaders();
            String payload = (String) response.getEntity(String.class);
            FileWriter fw = new FileWriter(new File("/home/ec2-user/dst/redirect.txt"));
            fw.write(payload);
            fw.close();
            List<Cookie> newCookies = parseCookies((List<String>) headers.get("Set-Cookie"));
            for (Cookie c : newCookies) {
                debugCookie(c);
            }
            return newCookies;
        } catch (Exception e) {
            e.printStackTrace();
            throw new DowntimeException(e);
        } finally {
            if (response != null) {
                response.releaseConnection();
            }
        }
    }

    protected List<Cookie> parseCookies(List<String> cookies) {
        List<Cookie> newCookies = new ArrayList<>();
        if (cookies == null)
            return newCookies;
        for (String cookie : cookies) {
            List<HttpCookie> cook = HttpCookie.parse(cookie);
            assert cook != null;
            if (cook.size() == 0) continue;
            HttpCookie c = cook.get(0);
            newCookies.add(new Cookie(c.getName(), c.getValue(), c.getPath(), null));
        }
        return newCookies;
    }

    protected String debugCookie(Cookie c) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(">>> cookie: ");
        buffer.append(c.getName());
        buffer.append(", ");
        buffer.append(c.getValue());
        buffer.append(", ");
        buffer.append(c.getPath());
        buffer.append(", ");
        buffer.append(c.getDomain());
        return buffer.toString();
    }

    protected String encode(String param) throws UnsupportedEncodingException {
        return URLEncoder.encode(param, "UTF-8").replace("+", "%20");
    }

}
