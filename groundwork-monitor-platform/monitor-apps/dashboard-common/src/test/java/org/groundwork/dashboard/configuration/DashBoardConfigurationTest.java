package org.groundwork.dashboard.configuration;

import com.groundwork.dashboard.configuration.CheckedState;
import com.groundwork.dashboard.configuration.DashboardConfiguration;
import com.groundwork.dashboard.configuration.DashboardConfigurationException;
import com.groundwork.dashboard.configuration.DashboardConfigurationFactory;
import com.groundwork.dashboard.configuration.DashboardConfigurationService;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DashBoardConfigurationTest {

    @Test
    public void testDashboardCrud() throws Exception {
        DashboardConfigurationService dashboardService = DashboardConfigurationFactory.getConfigurationService(DashboardConfigurationFactory.ServiceType.NOC);
        List<DashboardConfiguration> dashboards = dashboardService.list();
        assertNotNull(dashboards);
        assertTrue(dashboards.size() > 0);

        DashboardConfiguration dash = null;
        for (DashboardConfiguration dc : dashboards) {
            if (dc.getName().equals("dashboard-test")) {
                dash = dc;
                break;
            }
        }
        assertDashboard(dash, "dashboard-test", "Test Service Groups Dashboard");

        assertTrue(dashboardService.exists("dashboard-test"));

        DashboardConfiguration clone = dashboardService.copy(dash);
        clone.setName("dashboard-clone");
        clone.setTitle("Cloned Dashboard");
        dashboardService.save(clone);

        assertTrue(dashboardService.exists("dashboard-clone"));

        DashboardConfiguration dash2 = dashboardService.read("dashboard-clone");
        assertDashboard(dash2, "dashboard-clone", "Cloned Dashboard");

        assertTrue(dashboardService.remove("dashboard-clone"));
        boolean failed = false;
        try {
            DashboardConfiguration dash3 = dashboardService.read("dashboard-clone");
        }
        catch (DashboardConfigurationException e) {
            failed = true;
        }
        assertTrue(failed);
    }

    private void assertDashboard(DashboardConfiguration dash, String name, String title) {
        assertNotNull(dash);
        assertEquals(dash.getName(), name);
        assertEquals(dash.getTitle(), title);
        assertEquals(dash.getServiceGroup(), "test-service");
        assertEquals(dash.getAutoExpand(), true);
        assertEquals(dash.getDowntimeHours(), 2);
        assertEquals(dash.getAvailabilityHours(), 24);
        assertEquals(dash.getPercentageSLA(), 90);
        assertEquals(dash.getRows(), 20);
        assertEquals(dash.getRefreshSeconds(), 60);
        assertEquals(dash.getAckFilters().size(), 2);
        assertEquals(dash.getDownTimeFilters().size(), 2);
        assertEquals(dash.getStates().size(), 6);
        assertEquals(dash.getColumns().size(), 8);
        CheckedState notAcked = dash.getAckFilters().get(1);
        assertNotNull(notAcked);
        assertEquals(notAcked.getChecked(), true);
        assertEquals(notAcked.getName(), "Not Acked");
        CheckedState inDowntime = dash.getDownTimeFilters().get(0);
        assertNotNull(inDowntime);
        assertEquals(inDowntime.getChecked(), true);
        assertEquals(inDowntime.getName(), "In Downtime");
        CheckedState ok = dash.getStates().get(0);
        assertNotNull(ok);
        assertEquals(ok.getChecked(), true);
        assertEquals(ok.getName(), "OK");
        CheckedState hostColumn = dash.getColumns().get(0);
        assertNotNull(hostColumn);
        assertEquals(hostColumn.getChecked(), true);
        assertEquals(hostColumn.getName(), "Host");

    }
}
