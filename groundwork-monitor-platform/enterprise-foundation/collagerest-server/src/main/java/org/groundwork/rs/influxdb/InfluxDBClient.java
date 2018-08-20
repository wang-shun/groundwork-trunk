/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2017  GroundWork Open Source Solutions info@groundworkopensource.com

 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of version 2 of the GNU General Public License
 *     as published by the Free Software Foundation.

 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.

 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.groundwork.rs.influxdb;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Sets;
import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.metrics.CollageMetrics;
import com.groundwork.collage.metrics.CollageTimer;
import com.groundwork.collage.model.Host;
import com.groundwork.collage.model.HostGroup;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.hostidentity.HostIdentityService;
import org.groundwork.rs.dto.DtoOperationResults;
import org.groundwork.rs.dto.DtoPerfData;
import org.groundwork.rs.dto.DtoPerfDataTimeSeries;
import org.groundwork.rs.dto.DtoPerfDataTimeSeriesValue;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Pong;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class InfluxDBClient {

    private static final Log log = LogFactory.getLog(InfluxDBClient.class);

    private static final URL INFLUXDB_URL = InfluxDBConfiguration.getURL();
    private static final String INFLUXDB_NAME = InfluxDBConfiguration.getDBName();
    private static final boolean HOSTGROUPS_ENABLED = InfluxDBConfiguration.isHostgroupsEnabled();
    private static final long HOSTGROUP_CACHE_DURATION = InfluxDBConfiguration.getHostgroupCacheDuration();
    private static final String HOST_TAGNAME = "hostname";
    private static final String TIME_FIELDNAME = "time";
    private static final String DEFAULT_METRICNAME = "metric";
    private static final String WARNING_THRESHOLD_FIELDNAME = "_wn";
    private static final String CRITICAL_THRESHOLD_FIELDNAME = "_cr";
    private static final String TRUE_STRING = Boolean.TRUE.toString();
    private static final HashSet<String> RESERVED_FIELDS = Sets.newHashSet(HOST_TAGNAME, TIME_FIELDNAME);

    private static final HostIdentityService HOST_IDENTITY_SERVICE = CollageFactory.getInstance().getHostIdentityService();

    private static CollageMetrics collageMetrics;

    private static CollageMetrics getCollageMetrics() {
        if (collageMetrics == null) {
            collageMetrics = CollageFactory.getInstance().getCollageMetrics();
        }
        return collageMetrics;
    }

    public static CollageTimer startMetricsTimer() {
        StackTraceElement element = Thread.currentThread().getStackTrace()[2];
        CollageMetrics collageMetrics = getCollageMetrics();
        return (collageMetrics == null ? null : collageMetrics.startTimer("InfluxDBClient", element.getMethodName()));
    }

    public static void stopMetricsTimer(CollageTimer timer) {
        CollageMetrics collageMetrics = getCollageMetrics();
        if (collageMetrics != null) getCollageMetrics().stopTimer(timer);
    }

    // InfluxDB objects are thread-safe and include a connection pool internally
    private static final InfluxDB INFLUX_DB = connect(INFLUXDB_URL);

    private static final LoadingCache<String, List<String>> HOSTGROUP_CACHE = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(HOSTGROUP_CACHE_DURATION, TimeUnit.SECONDS)
            .build(new CacheLoader<String, List<String>>() {
                public List<String> load(@SuppressWarnings("NullableProblems") String hostName) {
                    if (log.isDebugEnabled()) log.debug("Looking up hostgroups for host: " + hostName);

                    Host host = HOST_IDENTITY_SERVICE.getHostByIdOrHostName(hostName);
                    if (host == null) return null;

                    Set hostGroups = host.getHostGroups();
                    if ((hostGroups == null) || hostGroups.isEmpty()) return null;

                    List<String> results = new ArrayList<>();
                    for (Object rawHostGroup : hostGroups) {
                        if (!(rawHostGroup instanceof HostGroup)) continue;
                        HostGroup hostGroup = (HostGroup) rawHostGroup;
                        results.add(hostGroup.getName().trim());
                        if ((hostGroup.getAlias() != null) && !hostGroup.getAlias().isEmpty()) {
                            results.add(hostGroup.getAlias().trim());
                        }
                    }

                    if (log.isDebugEnabled()) log.debug("number of hostgroups found: " + results.size());
                    return results;
                }
            });

    private static List<String> getHostGroupsForHost(String hostName) {
        CollageTimer timer = startMetricsTimer();
        if (log.isDebugEnabled()) log.debug("Getting value from cache for host: " + hostName);
        try {
            return HOSTGROUP_CACHE.get(hostName);
        } catch (ExecutionException | CacheLoader.InvalidCacheLoadException e) {
            if (log.isDebugEnabled()) log.debug("Unable to retrieve hostgroups for host: " + hostName);
            return null;
        } finally {
            stopMetricsTimer(timer);
        }
    }

    private static InfluxDB connect(URL url) {
        CollageTimer timer = startMetricsTimer();
        if (log.isDebugEnabled()) log.debug("connecting to " + url);

        String user = StringUtils.substringBefore(url.getUserInfo(), ":");
        String pass = StringUtils.substringAfter(url.getUserInfo(), ":");

        InfluxDB influxDB = (StringUtils.isNotBlank(user) && StringUtils.isNotBlank(pass)) ?
                InfluxDBFactory.connect(url.toString(), user, pass) : InfluxDBFactory.connect(url.toString());

        // Reduce transfer size for potentially modest performance boost on large write operations
        influxDB.enableGzip();

        // Create the DB if it does not exist
        List<String> databases = influxDB.describeDatabases();
        if (!databases.contains(INFLUXDB_NAME)) {
            if (log.isWarnEnabled()) log.warn("Database " + INFLUXDB_NAME + " does not exist.  Creating...");
            influxDB.createDatabase(INFLUXDB_NAME);
        }

        if (log.isDebugEnabled()) {
            Pong pong = influxDB.ping();
            log.debug("connected to " + url);
            log.debug("version=" + pong.getVersion());
            log.debug("responseTime=" + pong.getResponseTime());
        }
        stopMetricsTimer(timer);
        return influxDB;
    }

    /**
     * Query performance data from InfluxDB.
     *
     * @param serverName  service name, (hostname tag value)
     * @param serviceName service name, (metric name)
     * @param startTime   query start time, (millis since epoch)
     * @param endTime     query end time, (millis since epoch)
     * @return JSON query results or null on error
     */
    public static DtoPerfDataTimeSeries query(String appType,
                                              String serverName,
                                              String serviceName,
                                              long startTime,
                                              long endTime,
                                              long interval) {
        CollageTimer timer = startMetricsTimer();
        if (StringUtils.isBlank(serverName)) {
            throw new WebApplicationException(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("servername required but not provided")
                            .build());
        }

        if (StringUtils.isBlank(serviceName)) {
            throw new WebApplicationException(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("servicename required but not provided")
                            .build());
        }

        DtoPerfDataTimeSeries dtoPerfDataTimeSeries =
                new DtoPerfDataTimeSeries(appType, serverName, serviceName, startTime, endTime, interval);

        // Get the metric names
        Query metricQuery = new Query("SHOW FIELD KEYS FROM \"" + serviceName + "\"", INFLUXDB_NAME);
        List<List<Object>> metrics;
        try {
            metrics = INFLUX_DB.query(metricQuery).getResults().get(0).getSeries().get(0).getValues();
        } catch (Exception e) {
            // If there is any error in the query results, handle it as no data found
            return dtoPerfDataTimeSeries;
        }
        if (metrics == null) return dtoPerfDataTimeSeries;

        for (List<Object> metricList : metrics) {
            String metricName = (String) metricList.get(0);
            String metricType = (String) metricList.get(1);
            if (!metricType.equalsIgnoreCase("float")) continue;

            // Query by metric
            DtoPerfDataTimeSeries metricData = query(appType, serverName, serviceName, metricName, startTime, endTime, interval);
            dtoPerfDataTimeSeries.add(metricData);
        }

        stopMetricsTimer(timer);
        return dtoPerfDataTimeSeries;
    }

    public static DtoPerfDataTimeSeries query(String appType,
                                              String serverName,
                                              String serviceName,
                                              String metricName,
                                              long startTime,
                                              long endTime,
                                              long interval) {
        DtoPerfDataTimeSeries dtoPerfDataTimeSeries =
                new DtoPerfDataTimeSeries(appType, serverName, serviceName, startTime, endTime, interval);

        // Need to change units of time from milliseconds to nanoseconds to match what influx API expects
        String queryString = "SELECT MEAN(\"" + metricName + "\") FROM \"" + serviceName + "\"" +
                " WHERE " + HOST_TAGNAME + "='" + serverName + "'" +
                " AND " + TIME_FIELDNAME + ">=" + TimeUnit.MILLISECONDS.toNanos(startTime) +
                " AND " + TIME_FIELDNAME + "<=" + TimeUnit.MILLISECONDS.toNanos(endTime) +
                " GROUP BY time(" + interval + "ms) fill(previous)";

        if (log.isDebugEnabled()) log.debug("querying with " + queryString);
        Query query = new Query(queryString, INFLUXDB_NAME);
        QueryResult queryResult = INFLUX_DB.query(query, TimeUnit.MILLISECONDS);

        // A null series or missing query results implies that there was no data found
        List<List<Object>> values;
        try {
            // Given that we are searching for data on a single host, we expect to get back a single result with a single series
            values = queryResult.getResults().get(0).getSeries().get(0).getValues();
        } catch (Exception e) {
            // If there is any error in the query results, handle it as no data found
            return dtoPerfDataTimeSeries;
        }
        if (values == null) return dtoPerfDataTimeSeries;

        if (log.isDebugEnabled()) log.debug("mapping to DTO");


        for (List<Object> valueRow : values) {
            if ((valueRow.size() < 2)
                    || (!(valueRow.get(0) instanceof Double)) || (!(valueRow.get(1) instanceof Double))) continue;
            long timestamp = ((Double) valueRow.get(0)).longValue();
            double value = (double) valueRow.get(1);
            dtoPerfDataTimeSeries.add(new DtoPerfDataTimeSeriesValue(metricName, timestamp, value));
        }
        return dtoPerfDataTimeSeries;
    }

    public static DtoOperationResults write(List<DtoPerfData> dtos) {
        CollageTimer timer = startMetricsTimer();
        if (log.isDebugEnabled()) log.debug("generating " + dtos.size() + " points");

        DtoOperationResults results = new DtoOperationResults("PerfData", DtoOperationResults.INSERT);
        BatchPoints batchPoints = BatchPoints.database(INFLUXDB_NAME).build();

        for (DtoPerfData dto : dtos) {

            if (dto == null) {
                String message = "No data point provided";
                results.fail("Data point unknown", message);
                log.error(message);
                continue;
            }

            if (StringUtils.isBlank(dto.getServerName()) || StringUtils.isBlank(dto.getServiceName())) {
                String message = "No Server Name or Service Name name provided";
                results.fail("Server/Service Unknown", message);
                log.error(message);
                continue;
            }
            String hostName = dto.getServerName().trim();
            String serviceName = dto.getServiceName().trim();

            if (StringUtils.isBlank(dto.getValue())) {
                String message = "No value provided for data point";
                results.fail("Value Unknown", message);
                log.error(message);
                continue;
            }
            double value;
            try {
                value = DecimalFormat.getInstance().parse(dto.getValue()).doubleValue();
            } catch (ParseException e) {
                String message = "Invalid value provided for data point: " + dto.getValue();
                results.fail("Value invalid", message);
                log.error(message);
                continue;
            }

            long time = (dto.getServerTime() != null ? dto.getServerTime() : TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));

            String metricName = DEFAULT_METRICNAME;
            if (StringUtils.isNotBlank(dto.getLabel())) {
                metricName = dto.getLabel().trim();
                // Confirm that the metric name will not collide with already defined names
                if (RESERVED_FIELDS.contains(metricName)) {
                    if (log.isWarnEnabled()) log.warn("Warning: received value '" + metricName + "' as a label name.  Renaming to avoid collision with influx reserved fieldnames");
                    metricName = metricName + "_" + DEFAULT_METRICNAME;
                }
            }

            Point.Builder point = Point
                    .measurement(serviceName)
                    .time(time, TimeUnit.SECONDS)
                    .tag(HOST_TAGNAME, hostName)
                    .addField(metricName, value);

            if (HOSTGROUPS_ENABLED) {
                List<String> hostGroups = getHostGroupsForHost(hostName);
                if (hostGroups != null) {
                    for (String hostGroup : hostGroups) {
                        point.tag(hostGroup, TRUE_STRING);
                    }
                }
            }

            if ((dto.getTagNames() != null) && !dto.getTagNames().isEmpty() && (dto.getTagValues() != null) && !dto.getTagValues().isEmpty()) {
                Iterator<String> dtoTagNamesIter = dto.getTagNames().iterator();
                Iterator<String> dtoTagValuesIter = dto.getTagValues().iterator();
                while (dtoTagNamesIter.hasNext() && dtoTagValuesIter.hasNext()) {
                    point.tag(dtoTagNamesIter.next(), dtoTagValuesIter.next());
                }
            }

            if (StringUtils.isNotBlank(dto.getWarning())) {
                double warningValue;
                try {
                    warningValue = Double.parseDouble(dto.getWarning());
                } catch (NumberFormatException e) {
                    String message = "Warning value invalid";
                    results.fail("Warning value invalid: ", dto.getWarning());
                    log.error(message);
                    continue;
                }
                point = point.addField(metricName + WARNING_THRESHOLD_FIELDNAME, warningValue);
            }

            if (StringUtils.isNotBlank(dto.getCritical())) {
                double criticalValue;
                try {
                    criticalValue = Double.parseDouble(dto.getCritical());
                } catch (NumberFormatException e) {
                    String message = "Critical value invalid";
                    results.fail("Critical value invalid: ", dto.getCritical());
                    log.error(message);
                    continue;
                }
                point = point.addField(metricName + CRITICAL_THRESHOLD_FIELDNAME, criticalValue);
            }

            batchPoints.point(point.build());
            String entity = hostName + "," + serviceName + "," + metricName;
            results.success(entity, "OK");
        }

        if (log.isDebugEnabled()) log.debug("writing");
        INFLUX_DB.write(batchPoints);

        stopMetricsTimer(timer);
        return results;
    }
}
