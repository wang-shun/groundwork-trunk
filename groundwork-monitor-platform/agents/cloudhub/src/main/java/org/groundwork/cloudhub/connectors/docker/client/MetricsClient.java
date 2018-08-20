/*
 * Copyright 2012 GroundWork , Inc. ("GroundWork")
 * All rights reserved.
 */
package org.groundwork.cloudhub.connectors.docker.client;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import net.minidev.json.JSONArray;
import org.apache.log4j.Logger;
import org.groundwork.cloudhub.configuration.DockerConnection;
import org.groundwork.cloudhub.connectors.base.MetricFaultInfo;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MetricsClient extends BaseDockerClient implements IMetricsClient {

    private static Logger log = Logger.getLogger(MetricsClient.class);
    private List<MetricFaultInfo> metricFaults = new LinkedList<>();
    private DateTimeFormatter utcParser = ISODateTimeFormat.dateTimeParser().withZoneUTC();

    public MetricsClient(DockerConnection connection, int apiLevel) {
        super(connection, apiLevel);
    }

    public List<DockerMetricInfo> retrieveDockerEngineMetrics(String engine, Set<String> queries) {
        return retrieveMetrics(engine, null, queries, false);
    }

    public List<DockerMetricInfo> retrieveContainerMetrics(String containerName, String containerId, Set<String> queries) {
        return retrieveMetrics(containerName, containerId, queries, true);
    }

    protected List<DockerMetricInfo> retrieveMetrics(String name, String id, Set<String> queries, boolean isContainer) {
        java.net.URLConnection urlConnection = null;
        InputStream stream = null;
        List<DockerMetricInfo> metrics = new ArrayList<DockerMetricInfo>();
        try {
            String connectionString = (isContainer) ? makeContainerMetricConnection(id) :
                    makeDockerEngineMetricsConnection();
            URL url = new URL(connectionString);
            urlConnection = url.openConnection();
            urlConnection.connect();
            stream = urlConnection.getInputStream();
            StringWriter writer = new StringWriter();
            drain(new InputStreamReader(stream), writer);
            String payload = writer.toString();
            String now = now();
            ReadContext context = JsonPath.parse(payload);
            List<Map<String, Object>> stats;
            if (this.apiLevel == 1 || !isContainer) {
                stats = context.read("$.stats");
            } else {
                stats = context.read("$.*.stats");
            }
            if (stats.size() > 0) {
                String base;
                String first;
                if (isContainer && this.apiLevel == 2) {
                    base = "$.*.stats[(@.length-1)].";
                    first = "$.*.stats[(@.length-3)].";
                } else {
                    base = "$.stats[(@.length-1)].";
                    first = "$.stats[(@.length-3)].";
                }
                for (String query : queries) {
                    //String base = "$.stats[" + (stats.size() - 1) + "].";
                    try {
                        // @since 7.1.1 - Upgrade to 2.x API changed the structure of the stats payload
                        if (isContainer && this.apiLevel == 2) {
                            JSONArray array = context.read(base + query, JSONArray.class);
                            if (array != null && array.size() > 0) {
                                Object val = array.get(0);
                                Long value = (val instanceof Integer) ? new Long((Integer) val) : (Long) val;
                                if (query.startsWith("cpu.usage")) {
                                    JSONArray firstArray = context.read(first + query, JSONArray.class);
                                    if (firstArray != null && firstArray.size() > 0) {
                                        Object firstVal = firstArray.get(0);
                                        Long firstValue = (firstVal instanceof Integer) ? new Long((Integer) firstVal) : (Long) firstVal;
                                        String tsStart = (String) context.read(first + "timestamp", JSONArray.class).get(0);
                                        String tsLast = (String) context.read(base + "timestamp", JSONArray.class).get(0);
                                        long msFirst = utcParser.parseDateTime(tsStart).getMillis() * 10000;
                                        long msLast = utcParser.parseDateTime(tsLast).getMillis() * 10000;
                                        value = (value - firstValue) / (msLast - msFirst);
                                    }
                                }
                                metrics.add(new DockerMetricInfo(query, name, now, value, "", query));
                            }
                        } else {
                            Long value = context.read(base + query, Long.class);
                            if (query.startsWith("cpu.usage")) {
                                Long firstValue = context.read(first + query, Long.class);
                                String tsStart = context.read(first + "timestamp", String.class);
                                String tsLast = context.read(base + "timestamp", String.class);
                                long msFirst = utcParser.parseDateTime(tsStart).getMillis() * 10000;
                                long msLast = utcParser.parseDateTime(tsLast).getMillis() * 10000;
                                value = (value - firstValue) / (msLast - msFirst);
                            }
                            metrics.add(new DockerMetricInfo(query, name, now, value, "", query));
                        }
                    } catch (Exception e) {
                        metricFaults.add(new MetricFaultInfo(query, name, isContainer));
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to retrieve metrics for " + name, e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (urlConnection != null) {
                ((HttpURLConnection) urlConnection).disconnect();
            }
        }
        return metrics;
    }

    public List<MetricFaultInfo> getMetricFaults() {
        return metricFaults;
    }

    public void clearMetricFaults() {
        metricFaults.clear();
    }
}
