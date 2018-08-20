package org.groundwork.connectors.solarwinds;

import org.groundwork.connectors.solarwinds.status.MonitorProperty;
import org.groundwork.connectors.solarwinds.status.MonitorStatus;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class SolarWindsConfigurationTest {

    @Test
    public void testLoadConfiguration() throws Exception {
        SolarWindsConfiguration configuration = SolarWindsConfiguration.instance();
        assert !configuration.isAuditMode();
        assert configuration.isValidAgent("bogus");
        assert configuration.isValidAgent(null);
        assert configuration.isValidAgent("localhost");
        assert configuration.getUnknownHost().equals("SW_Unknown_Host");
        assert configuration.getUnknownService().equals("SW_Unknown_Service");
        assert configuration.isProcessUnknownHosts();
        assert configuration.isProcessUnknownServices();
        assert !configuration.isNotificationsEnabled();
        //assert configuration.getHostPrefix().equals("SW_");
        assert configuration.getBridgeService().equals("Bridge_Status");
        assert configuration.getDefaultHostGroup().equals("Solarwinds");
        assert configuration.isAddToDefaultHostGroup();
        assert configuration.isStatusSuffix();
        assert configuration.getRestApiEndpoint().equals("http://localhost:8080/foundation-webapp/api");
        assert configuration.translateStatus("Up").equals(MonitorStatus.UP);
        assert configuration.translateStatus("Down").equals(MonitorStatus.DOWN);
        assert configuration.translateStatus("Not Present").equals(MonitorStatus.UNKNOWN);
    }

    @Test
    public void testProperties() throws Exception {
        assert false == Boolean.parseBoolean(null);
        assert MonitorProperty.LastPluginOutput.value().equals("LastPluginOutput");
        assert MonitorProperty.PerformanceData.value().equals("PerformanceData");
        assert MonitorStatus.SCHEDULED_CRITICAL.value().equals("SCHEDULED CRITICAL");
        assert MonitorStatus.SUSPENDED.value().equals("SUSPENDED");
    }

    @Test
    public void testSolarWindsDateFormat() throws Exception {
        DateFormat dateFormat = new SimpleDateFormat(AbstractBridgeResource.SOLAR_WIND_DATE_FORMAT);
        Date date = dateFormat.parse("07/04/2014 4:21 PM");
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        assert calendar.get(Calendar.MONTH) == 6;
        assert calendar.get(Calendar.DAY_OF_MONTH) == 4;
        assert calendar.get(Calendar.YEAR) == 2014;
        assert calendar.get(Calendar.HOUR_OF_DAY) == 16;
        assert calendar.get(Calendar.MINUTE) == 21;

        date = dateFormat.parse("4/3/2014 11:32 AM");
        calendar.setTime(date);
        assert calendar.get(Calendar.MONTH) == 3;
        assert calendar.get(Calendar.DAY_OF_MONTH) == 3;
        assert calendar.get(Calendar.YEAR) == 2014;
        assert calendar.get(Calendar.HOUR_OF_DAY) == 11;
        assert calendar.get(Calendar.MINUTE) == 32;

    }
}
