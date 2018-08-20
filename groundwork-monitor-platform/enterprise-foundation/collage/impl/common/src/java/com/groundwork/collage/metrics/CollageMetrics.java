package com.groundwork.collage.metrics;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Timer;
import com.codahale.metrics.jvm.BufferPoolMetricSet;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import metrics_influxdb.HttpInfluxdbProtocol;
import metrics_influxdb.InfluxdbReporter;
import metrics_influxdb.api.measurements.MetricMeasurementTransformer;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Pong;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class CollageMetrics implements AutoCloseable{

    protected static Log log = LogFactory.getLog(CollageMetrics.class);

    private static final String METRICS_ENABLED = "collage.metrics.enabled";
    private static final String JVM_ENABLED = "collage.metrics.jvm.enabled";
    private static final String CSV_PERIOD = "collage.metrics.csv.period";
    private static final String LOG_PERIOD = "collage.metrics.log.period";
    private static final String INFLUXDB_PERIOD = "collage.metrics.influxdb.period";
    private static final String INFLUXDB_URL = "collage.metrics.influxdb.url";
    private static final String INFLUXDB_DB = "collage.metrics.influxdb.db";

    private static final String SEPARATOR = ".";

    private boolean metricsEnabled = false;

    private MetricRegistry registry;
    private List<AutoCloseable> reporters = new ArrayList<>();

    private void registerAll(String prefix, MetricSet metricSet, MetricRegistry registry) {
        for (Map.Entry<String, Metric> entry : metricSet.getMetrics().entrySet()) {
            if (entry.getValue() instanceof MetricSet) {
                registerAll(prefix + "." + entry.getKey(), (MetricSet) entry.getValue(), registry);
            } else {
                registry.register(prefix + "." + entry.getKey(), entry.getValue());
            }
        }
    }

    public CollageMetrics(Properties properties) {

        metricsEnabled = Boolean.parseBoolean(properties.getProperty(METRICS_ENABLED, "false"));
        if (metricsEnabled) {

            registry = new MetricRegistry();

            boolean jvmEnabled = Boolean.parseBoolean(properties.getProperty(JVM_ENABLED, "false"));
            if (jvmEnabled) {
                registerAll("gc", new GarbageCollectorMetricSet(), registry);
                registerAll("buffers", new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()), registry);
                registerAll("memory", new MemoryUsageGaugeSet(), registry);
                registerAll("threads", new ThreadStatesGaugeSet(), registry);
            }

            long csvPeriod = parsePeriod(CSV_PERIOD, properties.getProperty(CSV_PERIOD, "0"));
            if (csvPeriod > 0) {
                if (log.isDebugEnabled()) log.debug(CSV_PERIOD);
                final CsvReporter reporter = CsvReporter.forRegistry(registry)
                        .formatFor(Locale.US)
                        .convertRatesTo(TimeUnit.SECONDS)
                        .convertDurationsTo(TimeUnit.MILLISECONDS)
                        .build(new File("/usr/local/groundwork/logs/metrics"));
                reporters.add(reporter);
                reporter.start(csvPeriod, TimeUnit.SECONDS);
            }

            long logPeriod = parsePeriod(LOG_PERIOD, properties.getProperty(LOG_PERIOD, "0"));
            if (logPeriod > 0) {
                if (log.isDebugEnabled()) log.debug(LOG_PERIOD);
                final ConsoleReporter reporter = ConsoleReporter.forRegistry(registry)
                        .convertRatesTo(TimeUnit.SECONDS)
                        .convertDurationsTo(TimeUnit.MILLISECONDS)
                        .build();
                reporters.add(reporter);
                reporter.start(logPeriod, TimeUnit.SECONDS);
            }

            long influxDBPeriod = parsePeriod(INFLUXDB_PERIOD, properties.getProperty(INFLUXDB_PERIOD, "0"));
            if (influxDBPeriod > 0) {
                if (log.isDebugEnabled()) log.debug(INFLUXDB_PERIOD);

                try {
                    URL influxDBURL = new URL(properties.getProperty(INFLUXDB_URL, "http://localhost:8086"));
                    String dbName = properties.getProperty(INFLUXDB_DB, "_groundwork_metrics");

                    String influxDBUser = StringUtils.substringBefore(influxDBURL.getUserInfo(), ":");
                    String influxDBPass = StringUtils.substringAfter(influxDBURL.getUserInfo(), ":");

                    // Create an influxDB connection so that we can create the metrics database if it does not already
                    // exist.  It would be a nice enhancement to the InfluxDBReporter if it could do this instead.
                    createInfluxDb(influxDBURL, influxDBUser, influxDBPass, dbName);

                    final ScheduledReporter reporter =
                            InfluxdbReporter.forRegistry(registry)
                                    .protocol(new HttpInfluxdbProtocol(influxDBURL.getProtocol(), influxDBURL.getHost(), influxDBURL.getPort(), influxDBUser, influxDBPass, dbName))
                                    .filter(jvmEnabled ? new JvmMetricFilter() : MetricFilter.ALL)
                                    .transformer(new CollageMetricMeasurementTransformer())
                                    .build();
                    reporters.add(reporter);
                    reporter.start(influxDBPeriod, TimeUnit.SECONDS);
                } catch (MalformedURLException e) {
                    log.error("Unable to process value for " + INFLUXDB_URL + ".  Disabling influx metrics");
                }

            }
        }
    }

    private void createInfluxDb(URL url, String user, String pass, String dbName)  {
        InfluxDB influxDB = (StringUtils.isNotBlank(user) && StringUtils.isNotBlank(pass)) ?
                InfluxDBFactory.connect(url.toString(), user, pass) : InfluxDBFactory.connect(url.toString());

        // Create the DB if it does not exist
        List<String> databases = influxDB.describeDatabases();
        if (!databases.contains(dbName)) {
            if (log.isWarnEnabled()) log.warn("Database " + dbName + " does not exist.  Creating...");
            influxDB.createDatabase(dbName);
        }

        if (log.isDebugEnabled()) {
            Pong pong = influxDB.ping();
            log.debug("connected to " + url);
            log.debug("version=" + pong.getVersion());
            log.debug("responseTime=" + pong.getResponseTime());
        }
        influxDB.close();
    }

    // The following filter is to prevent non-integer JVM metrics from being included.  This is being done as the
    // influx reporter currently attempts to put all value types (strings/floats/ints) into a single "value" field
    // which results in large amounts of HTTP/400 BAD REQUEST responses from influxdb.  We've identified a small list
    // of specific non-integer metrics and are hard-coding those here until an improved influxdb reporter is available.
    private class JvmMetricFilter implements MetricFilter {
       @Override
       public boolean matches(String filterString, Metric filterMetric)  {
           return !(filterString.equals("threads.deadlocks") || filterString.endsWith("usage"));
       }
    }

    private class CollageMetricMeasurementTransformer implements MetricMeasurementTransformer {
        @Override
        public Map<String, String> tags(String metricName) {
            Map<String, String> tags = new HashMap<>();

            // If metricName contains "=" then we attempt to treat it as "tagName=tagValue,..."
            if (metricName.contains("=")) {
               for (String tagPair : metricName.split(",")) {
                   String tag[] = tagPair.split("=");
                   tags.put(tag[0], tag[1]);
               }
            } else {
                String entity;
                String measurement;
                if (StringUtils.contains(metricName, SEPARATOR)) {
                    entity = StringUtils.substringBeforeLast(metricName, SEPARATOR);
                    measurement = StringUtils.substringAfterLast(metricName, SEPARATOR);
                } else {
                    entity = metricName;
                    measurement = "default";
                }
                tags.put("entity", entity);
                tags.put("measurement", measurement);
            }
            return tags;
        }

        @Override
        public String measurementName(String metricName) {
            return "collage";
        }
    }

    private long parsePeriod(String name, String period) {
        try {
            return StringUtils.isBlank(period) ? 0 : Long.parseLong(period);
        } catch (NumberFormatException e) {
            log.error("Non-numeric value for " + name + ": " + period + ".  Disabling this metric reporter.");
            return 0;
        }
    }

    // Reporters must be closed manually as they spawn threads that may not be GC'd properly on a redeployment
    public void close() {
        if (log.isDebugEnabled()) log.debug("Closing CollageMetrics");
        for (AutoCloseable reporter : reporters) {
            try {
                reporter.close();
            } catch (Exception e){
                log.error("Unable to close CollageMetrics reporter: " + reporter.getClass().getSimpleName());
            }
        }
    }

    public CollageTimer startTimer(String className, String method) {
        if (!metricsEnabled) return null;
        if ((StringUtils.isBlank(className)) || (StringUtils.isBlank(method))) throw new RuntimeException("Invalid entity or measurement");
        String name = "class=" + className + ",method=" + method;
        Timer timer = registry.timer(name);
        return new CollageTimer(timer.time());
    }

    public void stopTimer(CollageTimer timer) {
        if ((!metricsEnabled) || (timer == null)) return;
        timer.stop();
    }

    public void setGauge(String name, long value) {
        if (!metricsEnabled) return;
        if (StringUtils.isBlank(name)) throw new RuntimeException("Invalid gauge");
        if (registry.getGauges().containsKey(name)) {
            ((CollageGauge) registry.getGauges().get(name)).setValue(value);
        } else {
            CollageGauge gauge = new CollageGauge();
            gauge.setValue(value);
            registry.register(name, gauge);
        }
    }

    public void setGauges(final String startsWith, long value) {
        if (!metricsEnabled) return;
        if (StringUtils.isBlank(startsWith)) throw new RuntimeException("Invalid gauges");
        Map<String, Gauge> gauges = registry.getGauges(new MetricFilter() {
            @Override
            public boolean matches(String filterString, Metric filterMetric)  {
                return filterString.startsWith(startsWith);
            }
        });
        if (gauges == null) return;
        for (Gauge gauge : gauges.values()) {
            ((CollageGauge) gauge).setValue(value);
        }
    }

    public boolean isMetricsEnabled() {
        return metricsEnabled;
    }

}
