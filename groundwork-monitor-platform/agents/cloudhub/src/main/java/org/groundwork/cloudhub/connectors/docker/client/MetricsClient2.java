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

public class MetricsClient2 extends BaseDockerClient implements IMetricsClient {

    private static Logger log = Logger.getLogger(MetricsClient2.class);
    private static DateTimeFormatter utcParser = ISODateTimeFormat.dateTimeParser().withZoneUTC();
    private List<MetricFaultInfo> metricFaults = new LinkedList<>();
    private int statsSampleSize;

    public MetricsClient2(DockerConnection connection, int statsSampleSize) {
        super(connection, 2);
        this.statsSampleSize = statsSampleSize;
    }

    protected String makeDockerEngineMetric2Connection() {
        StringBuffer containerApi = new StringBuffer();
        containerApi.append(REST_STATS_API);
        containerApi.append("?count=");
        containerApi.append(statsSampleSize);
        return makeConnectionString(connection.getServer(), containerApi.toString(), REST_API20_END_POINT);
    }

    protected String makeContainerMetric2Connection(String containerId) {
        StringBuffer containerApi = new StringBuffer();
        containerApi.append(REST_STATS_API);
        containerApi.append(containerId);
        containerApi.append("?type=docker&count=");
        containerApi.append(statsSampleSize);
        return makeConnectionString(connection.getServer(), containerApi.toString(), REST_API20_END_POINT);
    }

    @Override
    public List<DockerMetricInfo> retrieveDockerEngineMetrics(String engine, Set<String> queries) {
        return retrieveMetrics(engine, null, queries, false);
    }

    @Override
    public List<DockerMetricInfo> retrieveContainerMetrics(String containerName, String containerId, Set<String> queries) {
        return retrieveMetrics(containerName, containerId, queries, true);
    }

    protected List<DockerMetricInfo> retrieveMetrics(String name, String id, Set<String> queries, boolean isContainer) {
        java.net.URLConnection urlConnection = null;
        InputStream stream = null;
        List<DockerMetricInfo> metrics = new ArrayList<DockerMetricInfo>();
        try {
            String connectionString = (isContainer) ? makeContainerMetric2Connection(id) :
                    makeDockerEngineMetric2Connection();
            URL url = new URL(connectionString);
            urlConnection = url.openConnection();
            urlConnection.connect();
            stream = urlConnection.getInputStream();
            StringWriter writer = new StringWriter();
            drain(new InputStreamReader(stream), writer);
            String payload = writer.toString();
            String now = now();
            ReadContext context = JsonPath.parse(payload);
            //List<Map<String, Object>> stats = context.read((isContainer ?  "$.*" : "$.*"));
            Map<String, Object> root = context.read("$");
            if (root.size() > 0) {
                String cid = (id == null) ? "/" : root.keySet().iterator().next();
                String base = "$.['" + cid + "'][" + (statsSampleSize - 1) + "].";
                String first = "$.['" + cid + "'][0].";
                for (String query : queries) {
                    try {
                        if (query.startsWith("network")) {
                            String networkQuery = query.replace("network.", "network.interfaces[?(@.name=='eth0')].");
                            JSONArray array = context.read(base + networkQuery, JSONArray.class);

                            if (array.size() > 0) {
                                Integer value = (Integer) array.get(0);
                                metrics.add(new DockerMetricInfo(query, name, now, value, "", query));
                            }
                        } else {
                            Long value = context.read(base + query, Long.class);
                            if (query.startsWith("cpu.usage")) {
                                value = calculateCPUUsage(context, first, base, query, value);
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

    @Override
    public List<MetricFaultInfo> getMetricFaults() {
        return metricFaults;
    }

    @Override
    public void clearMetricFaults() {
        metricFaults.clear();
    }

    protected Long getValue(JSONArray array, int index) {
        Object value = array.get(index);
        Long longValue = (value instanceof Integer) ? new Long((Integer) value) : (Long) value;
        return longValue;
    }

    static Long calculateCPUUsage(ReadContext context, String first, String base, String query, Long value) {
        Long firstValue = context.read(first + query, Long.class);
        String tsStart = context.read(first + "timestamp", String.class);
        String tsLast = context.read(base + "timestamp", String.class);
        long msFirst = utcParser.parseDateTime(tsStart).getMillis() * 10000;
        long msLast = utcParser.parseDateTime(tsLast).getMillis() * 10000;
        value = (value - firstValue) / (msLast - msFirst);
        return value;
    }
}
