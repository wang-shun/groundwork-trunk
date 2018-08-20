/*
 * Copyright 2012 GroundWork , Inc. ("GroundWork")
 * All rights reserved.
*/
package org.groundwork.cloudhub.connectors.openstack.client;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.cloudhub.configuration.OpenStackConnection;
import org.groundwork.cloudhub.connectors.ConnectorConstants;
import org.groundwork.cloudhub.connectors.base.MetricFaultInfo;
import org.groundwork.cloudhub.connectors.openstack.HypervisorState;
import org.groundwork.cloudhub.connectors.openstack.HypervisorStatus;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.groundwork.cloudhub.metrics.BaseQuery;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.core.Response;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NovaClient extends BaseOpenStackClient {

    protected static Log log = LogFactory.getLog(NovaClient.class);

    public static final String NOVA_HYPERVISORS = ":%s/v2/%s/os-hypervisors";
    public static final String NOVA_SERVERS = ":%s/v2/%s/servers";
    public static final String NOVA_VMS = ":%s/v2/%s/servers/detail";
    public static final String NOVA_HYPERVISOR_STATISTICS = ":%s/v2/%s/os-hypervisors/statistics";
    public static final String NOVA_SERVER_DIAGS_V2_URL = ":%s/v2/%s/servers/%s/diagnostics";
    public static final String HYPERVISOR_SERVERS = ":%s/v2/%s/os-hypervisors/%s/servers";

    private static ConcurrentHashMap<String,Pattern> patterns = new ConcurrentHashMap<>();
    private List<MetricFaultInfo> metricFaults = new LinkedList<>();

    public NovaClient(OpenStackConnection connection) {
        super(connection, false);
    }

    public List<HypervisorInfo> listHypervisors() throws ConnectorException {
        Response.Status responseStatus = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<String> response = null;

        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                TokenSessionManager.Credentials credentials = tokenSessionManager.getCredentials(connection.getServer());
                if (credentials == null) {
                    throw new ConnectorException("Exception retrieving credentials");
                }
                String apiPath = String.format(NOVA_HYPERVISORS, connection.getNovaPort(), credentials.getTenantInfo().tenantId);
                String url = build(apiPath);
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get(String.class);
                if (response.getResponseStatus() == Response.Status.OK) {
                    String payload = response.getEntity();
                    ReadContext context = JsonPath.parse(payload);
                    //List<String> hypervisorNames = context.read("$.hypervisors.[*].hypervisor_hostname");
                    JsonReader reader = Json.createReader(new StringReader(payload));
                    JsonObject object = reader.readObject();
                    JsonArray hypervisors = object.getJsonArray("hypervisors");
                    List<HypervisorInfo> result = new ArrayList<>();
                    for (int ix = 0; ix < hypervisors.size(); ix++) {
                        JsonObject server = hypervisors.getJsonObject(ix);
                        String name = server.getString("hypervisor_hostname");
                        String state =  (server.keySet().contains("state")) ? server.getString("state") : HypervisorState.up.name();
                        String status = ((server.keySet().contains("status"))) ? server.getString("status") : HypervisorStatus.enabled.name();
                        if (state != null && state.equalsIgnoreCase(HypervisorState.up.name())) {
                            HypervisorInfo info = new HypervisorInfo(name, name,
                                    HypervisorState.mapToState(state), HypervisorStatus.mapToStatus(status));
                            result.add(info);
                        }
                    }
                    return result;
                }
                else if (response.getResponseStatus() == Response.Status.NOT_FOUND) {
                    // return an empty list for not found exception
                    return new ArrayList<HypervisorInfo>();
                }
                else if (response.getResponseStatus() == Response.Status.UNAUTHORIZED && retry < 1) {
                    log.info(RETRY_AUTH);
                    responseStatus = response.getResponseStatus();
                    response.releaseConnection();
                    tokenSessionManager.removeToken(connection.getServer());
                    continue;
                }
                responseStatus = response.getResponseStatus();
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
        if (responseStatus == null)
            responseStatus = Response.Status.SERVICE_UNAVAILABLE;
        throw new ConnectorException(String.format("Exception executing list hosts with status code of %d, reason: %s",
                 responseStatus.getStatusCode(), responseStatus.getReasonPhrase()));
    }

    public List<VmInfo> listVirtualMachines(String hypervisor) throws ConnectorException {
        Map<String,ServerLinkInfo> links = listVirtualMachinesLinks(hypervisor);
        List<VmInfo> all = listAllVirtualMachines(hypervisor);
        List<VmInfo> result = new ArrayList<>();
        for (VmInfo vm : all) {
            if (links.containsKey(vm.id)) {
                vm.hypervisor = hypervisor;
                result.add(vm);
            }
        }
        return result;
    }

    public Map<String, ServerLinkInfo> listVirtualMachinesLinks(String hypervisor) throws ConnectorException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<String> response = null;

        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                TokenSessionManager.Credentials credentials = tokenSessionManager.getCredentials(connection.getServer());
                if (credentials == null) {
                    throw new ConnectorException("Exception retrieving credentials");
                }
                String apiPath = String.format(HYPERVISOR_SERVERS, connection.getNovaPort(), credentials.getTenantInfo().tenantId, hypervisor);
                String url = build(apiPath);
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get(String.class);
                if (response.getResponseStatus() == Response.Status.OK) {
                    String payload = response.getEntity();
                    JsonReader reader = Json.createReader(new StringReader(payload));
                    JsonObject object = reader.readObject();
                    JsonArray hypervisors = object.getJsonArray("hypervisors");
                    Map<String, ServerLinkInfo> result = new HashMap<>();
                    if (hypervisors != null && hypervisors.size() > 0) {
                        JsonObject hyper = hypervisors.getJsonObject(0);
                        if (hyper != null) {
                            JsonArray servers = hyper.getJsonArray("servers");
                            if (servers != null) {
                                for (int ix = 0; ix < servers.size(); ix++) {
                                    JsonObject server = servers.getJsonObject(ix);
                                    String uuid = server.getString("uuid");
                                    ServerLinkInfo link = new ServerLinkInfo(uuid, server.getString("name")); // image name
                                    result.put(uuid, link);
                                }
                            }
                        }

                    }
                    return result;
                }
                else if (response.getResponseStatus() == Response.Status.NOT_FOUND) {
                    // return an empty list for not found exception
                    return new HashMap<String,ServerLinkInfo>();
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
        throw new ConnectorException(String.format("Exception executing list servers with status code of %d, reason: %s",
                status.getStatusCode(), status.getReasonPhrase()));
    }


    public List<VmInfo> listAllVirtualMachines(String defaultHypervisor) throws ConnectorException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<String> response = null;

        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                TokenSessionManager.Credentials credentials = tokenSessionManager.getCredentials(connection.getServer());
                if (credentials == null) {
                    throw new ConnectorException("Exception retrieving credentials");
                }
                String apiPath = String.format(NOVA_VMS, connection.getNovaPort(), credentials.getTenantInfo().tenantId);
                String url = build(apiPath);
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get(String.class);
                if (response.getResponseStatus() == Response.Status.OK) {
                    String payload = response.getEntity();
                    JsonReader reader = Json.createReader(new StringReader(payload));
                    JsonObject object = reader.readObject();
                    JsonArray servers = object.getJsonArray("servers");
                    List<VmInfo> result = new ArrayList<VmInfo>();
                    for (int ix = 0; ix < servers.size(); ix++) {
                        JsonObject server = servers.getJsonObject(ix);
                        VmInfo info = new VmInfo(server.getString("id"),
                                server.getString("name"), defaultHypervisor, server.getString("status"));
                        result.add(info);
                    }
                    return result;
                }
                else if (response.getResponseStatus() == Response.Status.NOT_FOUND) {
                    // return an empty list for not found exception
                    return new ArrayList<VmInfo>();
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
        throw new ConnectorException(String.format("Exception executing list servers with status code of %d, reason: %s",
                status.getStatusCode(), status.getReasonPhrase()));
    }


    public List<MetricInfo> getHyperVisorStatistics(String hypervisor, Set<String> queries) throws ConnectorException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<String> response = null;

        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                TokenSessionManager.Credentials credentials = tokenSessionManager.getCredentials(connection.getServer());
                if (credentials == null) {
                    throw new ConnectorException("Exception retrieving credentials");
                }
                String apiPath = String.format(NOVA_HYPERVISOR_STATISTICS, connection.getNovaPort(), credentials.getTenantInfo().tenantId);
                String url = build(apiPath);
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get(String.class);
                if (response.getResponseStatus() == Response.Status.OK) {
                    String payload = response.getEntity();
                    ReadContext context = JsonPath.parse(payload);
                    Map<String, Object> statistics = context.read("$.hypervisor_statistics");
                    List<MetricInfo> results = new ArrayList<MetricInfo>();
                    String now = now();
                    if (statistics.size() > 0)
                    {
                        for (String query : queries) {
                            if (!query.startsWith(ConnectorConstants.SYNTHETIC_PREFIX)) {
                                Object queryValue = statistics.get(query);
                                if (queryValue != null) {
                                    String value = Integer.toString((Integer) queryValue);
                                    results.add(new MetricInfo(query, query, hypervisor, now, value, determineMetricType(query)));
                                }
                                else {
                                    log.error("OpenStack Metric not found for query: " + query);
                                    metricFaults.add(new MetricFaultInfo(query, hypervisor));
                                }
                            }
                        }
                    }
                    return results;
                }
                else if (response.getResponseStatus() == Response.Status.NOT_FOUND) {
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
        throw new ConnectorException(String.format("Exception executing list hosts with status code of %d, reason: %s",
                status.getStatusCode(), status.getReasonPhrase()));
    }

    public List<MetricInfo> getNovaServerDiagnostics(String serverId, String serverName, Collection<BaseQuery> queries) throws Exception {    // pass in server(VM), not hypervisor

        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<String> response = null;

        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                TokenSessionManager.Credentials credentials = tokenSessionManager.getCredentials(connection.getServer());
                if (credentials == null) {
                    throw new ConnectorException("Exception retrieving credentials");
                }
                String apiPath = String.format(NOVA_SERVER_DIAGS_V2_URL, connection.getNovaPort(),
                                                    credentials.getTenantInfo().tenantId,
                                                    serverId);
                String url = build(apiPath);
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get(String.class);
                if (response.getResponseStatus() == Response.Status.OK) {
                    String payload = response.getEntity();
                    if (payload == null || payload.trim().length() == 0) {
                        throw new ConnectorException("Diagnostics not supported. Not at KILO release of OpenStack");
                    }
                    JsonReader reader = Json.createReader(new StringReader(payload));
                    JsonObject statistics = reader.readObject();
                    List<MetricInfo> results = new ArrayList<MetricInfo>();
                    String now = now();
                    for (BaseQuery query : queries) {
                        if (!query.isCeilometer() && !query.getQuery().startsWith(ConnectorConstants.SYNTHETIC_PREFIX)) {
                            JsonNumber queryValue = null;
                            String meter = query.getQuery();
                            if (query.isRegex()) {
                                try {
                                    Pattern pattern = patterns.get(query.getQuery());
                                    if (pattern == null) {
                                        pattern = Pattern.compile(query.getQuery());
                                        patterns.put(query.getQuery(), pattern);
                                    }
                                    for (String key : statistics.keySet()) {
                                        Matcher matcher = pattern.matcher(key);
                                        if (matcher.matches()) {
                                            queryValue = statistics.getJsonNumber(key);
                                            meter = key;
                                            String value = queryValue.toString();
                                            results.add(new MetricInfo(query.getQuery(), meter, serverId, now, value, determineMetricType(query.getQuery())));
                                        }
                                    }
                                }
                                catch (Exception e) {
                                    log.error("Failed to match regex for query " + query.getQuery() + ", error " + e.toString());
                                    metricFaults.add(new MetricFaultInfo(meter, serverName));
                                }
                            }
                            else {
                                if (statistics.containsKey(query.getQuery())) {
                                    queryValue = statistics.getJsonNumber(query.getQuery());
                                }
                                else {
                                    if (log.isInfoEnabled()) {
                                        log.info("Failed to find query '" + query.getQuery() + "' for server: " + serverName);
                                    }
                                    metricFaults.add(new MetricFaultInfo(meter, serverName));
                                }
                                if (queryValue != null) {
                                    String value = queryValue.toString();
                                    results.add(new MetricInfo(query.getQuery(), meter, serverId, now, value, determineMetricType(query.getQuery())));
                                }
                            }
                        }
                    }
                    return results;
                }
                else if (response.getResponseStatus() == Response.Status.NOT_FOUND) {
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
            if (e instanceof ConnectorException) {
                throw e;
            }
            throw new ConnectorException(e);
        }
        finally {
            if (response != null)
                response.releaseConnection();
        }
        if (status == null)
            status = Response.Status.SERVICE_UNAVAILABLE;
        throw new ConnectorException(String.format("Exception executing list hosts with status code of %d, reason: %s",
                status.getStatusCode(), status.getReasonPhrase()));
    }

    protected String determineMetricType(String query) {
        if (query.toLowerCase().contains("_mb")) {
            return "MB";
        }
        if (query.toLowerCase().contains("_gb")) {
            return "GB";
        }
        return "count";
    }

    public List<MetricFaultInfo> getMetricFaults() {
        return metricFaults;
    }
}
