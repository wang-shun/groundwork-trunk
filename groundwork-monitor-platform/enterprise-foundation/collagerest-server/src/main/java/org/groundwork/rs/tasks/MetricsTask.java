package org.groundwork.rs.tasks;

import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.metrics.CollageMetrics;
import com.groundwork.collage.model.impl.StateStatistics;
import com.groundwork.collage.model.impl.StatisticProperty;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.statistics.StatisticsService;
import org.groundwork.rs.auth.AuthService;

import java.io.Closeable;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MetricsTask implements Closeable {

    protected static Log log = LogFactory.getLog(MetricsTask.class);

    private static final String ENTITY_PREFIX = "entity=gwstat.";
    private static final String TOKEN_ENTITY = ENTITY_PREFIX + "apiTokensCount";
    private static final String HOSTS_COUNT_ENTITY = ENTITY_PREFIX + "hostsCount";
    private static final String SERVICES_COUNT_ENTITY = ENTITY_PREFIX + "servicesCount";
    private static final String EVENTS_COUNT_ENTITY = ENTITY_PREFIX + "eventsCount";
    private static final String TOTAL_SUFFIX = ".total";

    private static final String STATUS_PREFIX = "state=";
    private static final String HOSTGROUP_PREFIX = "hostGroup=";
    private static final String APPNAME_PREFIX = "appName=";

    // TODO: This could be made configurable
    // Value in seconds
    private static final int TASK_PERIOD = 60;

    private ScheduledExecutorService scheduler;

    public MetricsTask() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(new MetricsTaskThread(), TASK_PERIOD, TASK_PERIOD, TimeUnit.SECONDS);
    }

    public void close() {
        if (!scheduler.isShutdown()) scheduler.shutdownNow();
    }

    private class MetricsTaskThread implements Runnable {
       public void run() {
           CollageMetrics collageMetrics = CollageFactory.getInstance().getCollageMetrics();
           if ((collageMetrics == null) || (!collageMetrics.isMetricsEnabled())) return;

           StatisticsService statisticsService = CollageFactory.getInstance().getStatisticsService();
           if (statisticsService != null) {
               publishStatistics(collageMetrics, statisticsService);
           } else {
               if (log.isDebugEnabled()) log.debug("StatisticsService is null.  Unable to generate statistical metrics");
           }

           AuthService authService = AuthService.getInstance();
           if (authService != null) {
               publishTokenMetrics(collageMetrics, authService);
           } else {
               if (log.isDebugEnabled()) log.debug("AuthService is null.  Unable to generate API token metrics");
           }
       }
    }

    private void publishStatistics(CollageMetrics collageMetrics, StatisticsService statisticsService) {
        // Reset counters to zero out any hosts that have been removed, then compute total host count plus hosts by hostgroup
        collageMetrics.setGauges(HOSTS_COUNT_ENTITY, 0);
        collageMetrics.setGauge(HOSTS_COUNT_ENTITY + TOTAL_SUFFIX, statisticsService.getHostStatisticTotals().getTotalHosts());

        for (StateStatistics stats : statisticsService.getAllHostStatistics()) {
            String hostGroup = stats.getHostGroupName();
            collageMetrics.setGauge(HOSTS_COUNT_ENTITY + "," + HOSTGROUP_PREFIX + hostGroup, stats.getTotalHosts());
            for (StatisticProperty events : statisticsService.getEventStatisticsByHostGroupName(null, hostGroup, null, null, StatisticsService.STAT_TYPE_MONITOR_STATUS_WITH_OPEN)) {
                collageMetrics.setGauge(EVENTS_COUNT_ENTITY + "," + HOSTGROUP_PREFIX + hostGroup + "," + STATUS_PREFIX + events.getName(), events.getCount());
            }
        }

        // Reset counters to zero out any services that have been removed, then compute total service count plus services by service group
        collageMetrics.setGauges(SERVICES_COUNT_ENTITY, 0);
        collageMetrics.setGauge(SERVICES_COUNT_ENTITY + TOTAL_SUFFIX, statisticsService.getServiceStatisticTotals().getTotalServices());
        for (StateStatistics stats : statisticsService.getAllServiceStatistics()) {
            String hostGroup = stats.getHostGroupName();
            collageMetrics.setGauge(SERVICES_COUNT_ENTITY + "," + HOSTGROUP_PREFIX + hostGroup, stats.getTotalServices());
        }
    }

    private void publishTokenMetrics(CollageMetrics collageMetrics, AuthService authService) {
        Map<String, Integer> tokenCountsByApplication = authService.getAccessTokensByApplication();

        // Reset all token gauges to ensure that any applications that had their tokens released don't continue to
        // reflect non-zero values
        collageMetrics.setGauges(TOKEN_ENTITY, 0);

        for (String appName : tokenCountsByApplication.keySet()) {
            collageMetrics.setGauge(TOKEN_ENTITY + "," + APPNAME_PREFIX + appName, tokenCountsByApplication.get(appName));
        }

        collageMetrics.setGauge(TOKEN_ENTITY + TOTAL_SUFFIX, authService.listAccessTokens().size());
    }

}
