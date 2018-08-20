package org.groundwork.downtime;

import com.groundwork.downtime.DowntimeContext;
import com.groundwork.downtime.DowntimeMaintenanceWindow;
import com.groundwork.downtime.DowntimeService;
import com.groundwork.downtime.DowntimeServiceFactory;
import com.groundwork.downtime.DtoDowntime;
import org.junit.Test;

import javax.servlet.http.Cookie;
import java.net.HttpCookie;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DowntimeServiceTest {

    @Test
    public void testRange() throws Exception {
        String DATE1 = "2018-06-01 10:00:00";
        String DATE2 = "2018-06-30 10:47:01";
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date dt1 = formatter.parse(DATE1);
        Date dt2 = formatter.parse(DATE2);

//        DateTimeFormatter localParser = ISODateTimeFormat.dateTimeParser();
//        DateTime dt1 = localParser.parseDateTime(DATE1);
//        DateTime dt2 = localParser.parseDateTime(DATE2);

        DowntimeService service = DowntimeServiceFactory.getServiceInstance();
        DowntimeContext context = service.login("http://charon.groundwork.groundworkopensource.com", "admin", "admin");
        assertTrue(context.isLoggedOn() == true);
        Map<String, List<DowntimeMaintenanceWindow>> maintenanceWindows = service.range(context, dt1, dt2);
        service.logout(context);
        assert maintenanceWindows.size() > 0;
    }

    @Test
    public void testPing() throws Exception {
        DowntimeService service = DowntimeServiceFactory.getServiceInstance();
        DowntimeContext context = service.login("http://charon.groundwork.groundworkopensource.com", "admin", "admin");
        assertTrue(context.isLoggedOn() == true);
        assertTrue(service.ping(context));
        service.logout(context);
        assertFalse(service.ping(context));
        assertTrue(context.isLoggedOn() == false);
    }

    @Test
    public void testPingWithServerDown() throws Exception {
        DowntimeService service = DowntimeServiceFactory.getServiceInstance();
        List<javax.ws.rs.core.Cookie> cookies = new LinkedList<>();
        cookies.add(new javax.ws.rs.core.Cookie("", "", "", ""));
        DowntimeContext context = new DowntimeContext("http://charon.groundwork.groundworkopensource.com", cookies, false);
        assertFalse(service.ping(context));
    }

    @Test
    public void testDowntimeList() throws Exception {
        DowntimeService service = DowntimeServiceFactory.getServiceInstance();
        DowntimeContext context = service.login("http://charon.groundwork.groundworkopensource.com", "admin", "admin");
        assertTrue(context.isLoggedOn() == true);
        List<DtoDowntime> downtimes = service.list(context);
        service.logout(context);
        assertTrue(context.isLoggedOn() == false);
    }

    @Test
    public void testDownTimeDate() throws Exception {
        String testDate  = "2017-10-31 22:50:00";
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date dateTime = formatter.parse(testDate);
        long then = dateTime.getTime();
        System.out.println("value = " + then);
        Date back = new Date(then);
        System.out.println("back again = " + back);
        System.out.println("unix = " + (then / 1000));

    }

    private List<Cookie> parseCookies(List<String> cookies) {
        List<Cookie> newCookies = new ArrayList<>();
        for (String cookie : cookies) {
            List<HttpCookie> cook = HttpCookie.parse(cookie);
            assert cook != null;
            if (cook.size() == 0) continue;
            HttpCookie c = cook.get(0);
            Cookie httpCookie = new Cookie(c.getName(), c.getValue());
            httpCookie.setPath(c.getPath());
            httpCookie.setComment(c.getComment());
            httpCookie.setDomain(c.getDomain());
            httpCookie.setSecure(c.getSecure());
            httpCookie.setMaxAge(new Long(c.getMaxAge()).intValue());
            newCookies.add(httpCookie);
        }
        return newCookies;
    }

}
