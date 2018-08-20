/*
 * Copyright 2012 GroundWork , Inc. ("GroundWork")
 * All rights reserved.
*/
package org.groundwork.cloudhub.connectors.openstack.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.cloudhub.configuration.OpenStackConnection;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.core.Response;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CeilometerClient extends BaseOpenStackClient  {

    protected static final String EXCEPTION_RETRIEVING_CREDENTIALS = "Exception retrieving credentials";
    protected static Log log = LogFactory.getLog(CeilometerClient.class);

    public static final String METERS_BASE = ":%s/v2/meters/%s";
    public static final String METERS_META_BASE = ":%s/v2/meters";
    public static final String METERS_CAPABILITIES = ":%s/v2/capabilities";

    // Defaults to 10. To change this, you have to also change Ceilometer config, or it has no effect
    // https://ask.openstack.org/en/question/5882/how-to-change-the-polling-period-of-ceilometer/
    public static final int DEFAULT_METER_SAMPLE_RATE_MINUTES = 10;

    private static int meterSampleRateMinutes = DEFAULT_METER_SAMPLE_RATE_MINUTES;

    // To changing the polling period for ceilometer, see:
    //   https://ask.openstack.org/en/question/5882/how-to-change-the-polling-period-of-ceilometer/
    //   /etc/ceilometer/pipeline.yaml

    public CeilometerClient(OpenStackConnection connection) {
        super(connection, false);
        try {
            if (connection.getCeilometerSampleRateMinutes() != null) {
                meterSampleRateMinutes = Integer.parseInt(connection.getCeilometerSampleRateMinutes());
            }
        }
        catch (NumberFormatException e) {
            log.error("Error formatting Ceilometer Sample Rate: " + connection.getCeilometerSampleRateMinutes(), e);
        }
    }

    private static final String[] METRIC_QUERY_NAMES = { "q.field", "q.op", "q.value"};
    private static final String[] METRICS_UNIQUE = { "q.unique"};
    private static final String[] METRICS_UNIQUE_VALUE = { "true"};

    public List<MetricInfo> retrieveMetrics(String meter) throws ConnectorException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<String> response = null;

        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                TokenSessionManager.Credentials credentials = tokenSessionManager.getCredentials(connection.getServer());
                if (credentials == null) {
                    throw new ConnectorException(EXCEPTION_RETRIEVING_CREDENTIALS);
                }
                String[] values = {"timestamp", "gt", formatDateMinusMinutes(meterSampleRateMinutes, true)};
                String apiPath = String.format(METERS_BASE, connection.getCeilometerPort(), meter);
                String url = buildUrlWithPathAndQueryParams(apiPath, buildEncodedQueryParams(METRIC_QUERY_NAMES, values));
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get(String.class);
                List<MetricInfo> result = new ArrayList<MetricInfo>();
                if (response.getResponseStatus() == Response.Status.OK) {
                    String payload = response.getEntity();
                    JsonReader reader = Json.createReader(new StringReader(payload));
                    JsonArray array = reader.readArray();
                    for (int ix = 0; ix < array.size(); ix++) {
                        JsonObject sample = array.getJsonObject(ix);
                        String volume = sample.getJsonNumber("counter_volume").toString();
                        String resource = sample.getString("resource_id");
                        String timestamp = sample.getString("timestamp");
                        String unit = sample.getString("counter_unit");
                        result.add(new MetricInfo(meter, meter, resource, timestamp, volume, unit));
                        //System.out.format("%s: %s - %f %s\n", resource, timestamp, volume, unit);
                    }
                    if (result.size() == 0) {
                        log.debug("Ceilometer metrics, zero counters found for : " + url);
                    }
                    return result;
                }
                else if (response.getResponseStatus() == Response.Status.NOT_FOUND) {
                    log.debug("Ceilometer metrics, counters not found for : " + url);
                    // return an empty list for not found exception
                    return new ArrayList<MetricInfo>();
                }
                else if (response.getResponseStatus() == Response.Status.UNAUTHORIZED && retry < 1) {
                    log.info(RETRY_AUTH);
                    status = response.getResponseStatus();
                    response.releaseConnection();
                    tokenSessionManager.removeToken(connection.getServer());
                    continue;
                }
                status = response.getResponseStatus();
                break;
            }
        }
        catch (Exception e) {
            throw new ConnectorException(e);
        }
        finally {
            if (response != null)
                response.releaseConnection();
        }
        if (status == null)
            status = Response.Status.SERVICE_UNAVAILABLE;
        throw new ConnectorException(String.format("Exception executing retrieve metrics with status code of %d, reason: %s",
                status.getStatusCode(), status.getReasonPhrase()));
    }

    public List<MetricMetaInfo> retrieveMetricDescriptions() throws ConnectorException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<String> response = null;

        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                TokenSessionManager.Credentials credentials = tokenSessionManager.getCredentials(connection.getServer());
                if (credentials == null) {
                    throw new ConnectorException(EXCEPTION_RETRIEVING_CREDENTIALS);
                }
                String apiPath = String.format(METERS_META_BASE, connection.getCeilometerPort());

                String url = build(apiPath);

                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get(String.class);
                List<MetricMetaInfo> result = new ArrayList<>();
                Set<String> unique = new HashSet<>();
                if (response.getResponseStatus() == Response.Status.OK) {
                    String payload = response.getEntity();
                    JsonReader reader = Json.createReader(new StringReader(payload));
                    JsonArray array = reader.readArray();
                    for (int ix = 0; ix < array.size(); ix++) {
                        JsonObject meta = array.getJsonObject(ix);
                        if (!unique.contains(meta.getString("name"))) {
                            result.add(new MetricMetaInfo(
                                    meta.getString("name"),
                                    meta.getString("resource_id"),
                                    meta.getString("source"),
                                    meta.getString("meter_id"),
                                    meta.getString("type"),
                                    meta.getString("unit")));
                            //System.out.format("%s: %s - %f %s\n", resource, timestamp, volume, unit);
                            unique.add(meta.getString("name"));
                        }
                    }
                    if (result.size() == 0) {
                        log.debug("Ceilometer metrics, zero counters found for : " + url);
                    }
                    return result;
                }
                else if (response.getResponseStatus() == Response.Status.NOT_FOUND) {
                    log.debug("Ceilometer metrics, counters not found for : " + url);
                    // return an empty list for not found exception
                    return new ArrayList<MetricMetaInfo>();
                }
                else if (response.getResponseStatus() == Response.Status.UNAUTHORIZED && retry < 1) {
                    log.info(RETRY_AUTH);
                    status = response.getResponseStatus();
                    response.releaseConnection();
                    tokenSessionManager.removeToken(connection.getServer());
                    continue;
                }
                status = response.getResponseStatus();
                break;
            }
        }
        catch (Exception e) {
            throw new ConnectorException(e);
        }
        finally {
            if (response != null)
                response.releaseConnection();
        }
        if (status == null)
            status = Response.Status.SERVICE_UNAVAILABLE;
        throw new ConnectorException(String.format("Exception executing retrieve metrics with status code of %d, reason: %s",
                status.getStatusCode(), status.getReasonPhrase()));
    }

    public static int getMeterSampleRateMinutes() {
        return meterSampleRateMinutes;
    }

    public static void setMeterSampleRateMinutes(int meterSampleRateMinutes) {
        CeilometerClient.meterSampleRateMinutes = meterSampleRateMinutes;
    }

    public CapabilityInfo retrieveCapabilities() throws ConnectorException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<String> response = null;

        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                TokenSessionManager.Credentials credentials = tokenSessionManager.getCredentials(connection.getServer());
                if (credentials == null) {
                    throw new ConnectorException(EXCEPTION_RETRIEVING_CREDENTIALS);
                }
                String apiPath = String.format(METERS_CAPABILITIES, connection.getCeilometerPort());
                String url = build(apiPath);
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get(String.class);
                CapabilityInfo caps = new CapabilityInfo();
                if (response.getResponseStatus() == Response.Status.OK) {
                    String payload = response.getEntity();
                    JsonReader reader = Json.createReader(new StringReader(payload));
                    JsonObject root = reader.readObject();
                    JsonObject api = root.getJsonObject("api");
                    if (api != null) {
                        for (String key : api.keySet()) {
                            caps.getApiCapabilities().put(key, api.getBoolean(key));
                        }
                    }
                    JsonObject storage = root.getJsonObject("storage");
                    if (storage != null) {
                        for (String key : storage.keySet()) {
                            caps.getStorageCapabilities().put(key, storage.getBoolean(key));
                        }
                    }
                    JsonObject alarmStorage = root.getJsonObject("alarm_storage");
                    if (alarmStorage != null) {
                        for (String key : alarmStorage.keySet()) {
                            caps.getAlarmStorageCapabilities().put(key, alarmStorage.getBoolean(key));
                        }
                    }
                    if (caps.getApiCapabilities().size() == 0) {
                        log.debug("Ceilometer metrics, zero api capabilties found for : " + url);
                    }
                    return caps;
                }
                else if (response.getResponseStatus() == Response.Status.NOT_FOUND) {
                    log.debug("Ceilometer metrics, counters not found for : " + url);
                    // return an empty map for not found exception
                    return new CapabilityInfo();
                }
                else if (response.getResponseStatus() == Response.Status.UNAUTHORIZED && retry < 1) {
                    log.info(RETRY_AUTH);
                    status = response.getResponseStatus();
                    response.releaseConnection();
                    tokenSessionManager.removeToken(connection.getServer());
                    continue;
                }
                status = response.getResponseStatus();
                break;
            }
        }
        catch (Exception e) {
            throw new ConnectorException(e);
        }
        finally {
            if (response != null)
                response.releaseConnection();
        }
        if (status == null)
            status = Response.Status.SERVICE_UNAVAILABLE;
        throw new ConnectorException(String.format("Exception executing retrieve metrics with status code of %d, reason: %s",
                status.getStatusCode(), status.getReasonPhrase()));
    }

}
