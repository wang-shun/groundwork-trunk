package org.groundwork.cloudhub.connectors.docker.client;

import org.groundwork.cloudhub.connectors.base.BaseConnectorClient;
import org.groundwork.cloudhub.configuration.DockerConnection;

public abstract class BaseDockerClient extends BaseConnectorClient {

    protected DockerConnection connection = null;
    public final static String REST_API1_END_POINT = "/api/v1.2";
    public final static String REST_API2_END_POINT = "/api/v2.1";
    public final static String REST_API20_END_POINT = "/api/v2.0";
    public final static String REST_INVENTORY_API = "/docker"; //"/containers/docker";
    public final static String REST_CONTAINER_API = "/containers";
    public final static String REST_ENGINE_API = "/containers/";
    public final static String REST_MACHINE_API = "/machine";
    public final static String REST_VERSION_API = "/version";
    public final static String DOCKER_PREFIX = "/docker/";
    public final static String REST_STATS_API = "/stats/";

    protected String restEndPoint;
    protected int apiLevel = 2;

    public BaseDockerClient(DockerConnection connection, int apiLevel) {
        this.connection = connection;
        this.apiLevel = apiLevel;
        this.restEndPoint = REST_API1_END_POINT;
    }

    protected String makeInventoryConnection() {
        StringBuffer containerApi = new StringBuffer();
        containerApi.append(REST_INVENTORY_API);
        return makeConnectionString(connection.getServer(), containerApi.toString());
    }

    protected String makeDockerEngineMetricsConnection() {
        StringBuffer containerApi = new StringBuffer();
        containerApi.append(REST_ENGINE_API);
        return makeConnectionString(connection.getServer(), containerApi.toString());
    }

    protected String makeDockerEngineMachineConnection() {
        StringBuffer containerApi = new StringBuffer();
        containerApi.append(REST_MACHINE_API);
        return makeConnectionString(connection.getServer(), containerApi.toString());
    }

    protected String makeDockerVersionConnection() {
        StringBuffer containerApi = new StringBuffer();
        containerApi.append(REST_VERSION_API);
        return makeConnectionString(connection.getServer(), containerApi.toString(), REST_API2_END_POINT);
    }

    protected String makeContainerMetricConnection(String containerId) {
        StringBuffer containerApi = new StringBuffer();
        if (this.apiLevel == 1) {
            containerApi.append(REST_CONTAINER_API);
        }
        containerApi.append(DOCKER_PREFIX);
        containerApi.append(containerId);
        return makeConnectionString(connection.getServer(), containerApi.toString());
    }

    /**
     * Join a base URL with a relative path to create a full URL
     *
     * @param hostName the hostName such as localhost or localhost:8080
     * @param subPath the api group such as /statistics or /topology
     * @return the correctly joined path respecting path separators
     */
    protected String makeConnectionString(String hostName, String subPath, String endPoint) {
        StringBuilder result = new StringBuilder();
        if (hostName == null) hostName = "";
        if (subPath == null) subPath = "";
        String scheme = "http://";
        result.append(scheme);
        result.append(hostName);
        result.append(concatenatePaths(result.toString(), endPoint));
        result.append(concatenatePaths(result.toString(), subPath));
        return result.toString();
    }

    protected String makeConnectionString(String hostName, String subPath) {
        return this.makeConnectionString(hostName, subPath, restEndPoint);
    }
    
}
