/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2015  GroundWork Open Source Solutions info@groundworkopensource.com

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

package org.groundwork.cloudhub.connectors.icinga2.client;

import org.codehaus.jackson.JsonNode;
import org.groundwork.cloudhub.configuration.Icinga2Connection;
import org.jboss.resteasy.util.GenericType;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Icinga2InventoryClient
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class Icinga2InventoryClient extends BaseIcinga2Client {

    private static final String ICINGA2_STATUS_API = "/v1/status/IcingaApplication";
    private static final String HOSTS_API = "/v1/objects/hosts";
    private static final String HOST_GROUPS_API = "/v1/objects/hostgroups";
    private static final String SERVICES_API = "/v1/objects/services";
    private static final String SERVICE_GROUPS_API = "/v1/objects/servicegroups";
    private static final String COMMENTS_API = "/v1/objects/comments";

    private static final String ICINGA2_APPLICATION_VERSION = "2.4";

    /**
     * Construct Icinga2 inventory client.
     *
     * @param server server host name
     * @param port server port
     * @param user authentication user
     * @param password authentication password
     * @param trustSSLCACertificate trusted SSL CA certificate
     * @param trustSSLCACertificateKeystore trusted SSL CA certificate keystore
     * @param trustSSLCACertificateKeystorePassword trusted SSL CA certificate keystore password
     * @param trustAllSSL trust all SSL certificates
     */
    public Icinga2InventoryClient(String server, int port, String user, String password, File trustSSLCACertificate,
                                  File trustSSLCACertificateKeystore, String trustSSLCACertificateKeystorePassword,
                                  boolean trustAllSSL) {
        super(server, port, user, password, trustSSLCACertificate, trustSSLCACertificateKeystore,
                trustSSLCACertificateKeystorePassword, trustAllSSL);
    }

    /**
     * Construct Icinga2 inventory client from connection configuration.
     *
     * @param connection connection configuration
     */
    public Icinga2InventoryClient(Icinga2Connection connection) {
        super(connection);
    }

    /**
     * Icinga2 status query.
     *
     * @return status JSON
     */
    public JsonNode getStatus() {
        String url = build(ICINGA2_STATUS_API);
        return jsonNode(clientRequest(url, "Icinga2 status query", new GenericType<JsonNode>(){}));
    }

    /**
     * Icinga2 hosts inventory query.
     *
     * @return hosts JSON list
     */
    public List<JsonNode> getHosts() {
        String url = build(HOSTS_API);
        return jsonNodesList(clientRequest(url, "Icinga2 hosts objects query", new GenericType<JsonNode>(){}));
    }

    /**
     * Icinga2 host inventory query.
     *
     * @param hostName host name
     * @return host JSON
     */
    public JsonNode getHost(String hostName) {
        String url = buildUrlWithPath(HOSTS_API, hostName);
        return jsonNode(clientRequest(url, String.format("Icinga2 host objects query [%s]", hostName),
                new GenericType<JsonNode>(){}));
    }

    /**
     * Icinga2 host groups inventory query.
     *
     * @return host groups JSON list
     */
    public List<JsonNode> getHostGroups() {
        String url = buildUrlWithQueryParams(HOST_GROUPS_API, "?meta=used_by");
        return jsonNodesList(clientRequest(url, "Icinga2 hostgroups objects query", new GenericType<JsonNode>(){}));
    }

    /**
     * Icinga2 host group inventory query.
     *
     * @param name host group name
     * @return host group JSON
     */
    public JsonNode getHostGroup(String name) {
        String url = buildUrlWithPathAndQueryParams(HOST_GROUPS_API, name, "?meta=used_by");
        return jsonNode(clientRequest(url, String.format("Icinga2 host group objects query [%s]", name),
                new GenericType<JsonNode>(){}));
    }

    /**
     * Icinga2 services inventory query.
     *
     * @return services JSON list
     */
    public List<JsonNode> getServices() {
        String url = build(SERVICES_API);
        return jsonNodesList(clientRequest(url, "Icinga2 services objects query", new GenericType<JsonNode>(){}));
    }

    /**
     * Icinga2 service inventory query.
     *
     * @param hostName service host name
     * @param serviceDescription service description
     * @return service JSON
     */
    public JsonNode getService(String hostName, String serviceDescription) {
        String serviceName = hostName + "!" + serviceDescription;
        String url = buildUrlWithPath(SERVICES_API, serviceName);
        return jsonNode(clientRequest(url, String.format("Icinga2 service objects query [%s]", serviceName),
                new GenericType<JsonNode>(){}));
    }

    /**
     * Icinga2 service groups inventory query.
     *
     * @return service groups JSON list
     */
    public List<JsonNode> getServiceGroups() {
        String url = buildUrlWithQueryParams(SERVICE_GROUPS_API, "?meta=used_by");
        return jsonNodesList(clientRequest(url, "Icinga2 service groups objects query", new GenericType<JsonNode>(){}));
    }

    /**
     * Icinga2 service group inventory query.
     *
     * @param name service group name
     * @return service group JSON
     */
    public JsonNode getServiceGroup(String name) {
        String url = buildUrlWithPathAndQueryParams(SERVICE_GROUPS_API, name, "?meta=used_by");
        return jsonNode(clientRequest(url, String.format("Icinga2 service group objects query [%s]", name),
                new GenericType<JsonNode>(){}));
    }

    /**
     * Icinga2 comments inventory query.
     *
     * @return comments JSON list
     */
    public List<JsonNode> getComments() {
        String url = build(COMMENTS_API);
        return jsonNodesList(clientRequest(url, "Icinga2 comments objects query", new GenericType<JsonNode>(){}));
    }

    /**
     * Icinga2 comment inventory query.
     *
     * @param id comment id
     * @return comment JSON
     */
    public JsonNode getComment(String id) {
        String url = buildUrlWithPath(COMMENTS_API, id);
        return jsonNode(clientRequest(url, String.format("Icinga2 comment objects query [%s]", id),
                new GenericType<JsonNode>(){}));
    }

    /**
     * Unwrap JSON list from query results.
     *
     * @param jsonResults JSON query results
     * @return JSON list
     */
    private static List<JsonNode> jsonNodesList(JsonNode jsonResults) {
        if ((jsonResults == null) || !jsonResults.has("results") ||
                !jsonResults.get("results").isArray() || (jsonResults.get("results").size() == 0)) {
            return Collections.EMPTY_LIST;
        }
        List<JsonNode> jsonList = new ArrayList<JsonNode>();
        for (Iterator<JsonNode> iter = jsonResults.get("results").getElements(); iter.hasNext();) {
            jsonList.add(iter.next());
        }
        return jsonList;
    }

    /**
     * Unwrap JSON object from query results.
     *
     * @param jsonResults JSON query results
     * @return JSON object
     */
    private static JsonNode jsonNode(JsonNode jsonResults) {
        if ((jsonResults == null) || !jsonResults.has("results") ||
                !jsonResults.get("results").isArray() || (jsonResults.get("results").size() == 0)) {
            return null;
        }
        return jsonResults.get("results").get(0);
    }

    /**
     * Check API status and version.
     *
     * @param version returned application version or null
     * @return check status
     */
    public boolean checkStatus(String [] version) {
        version = ((version != null) ? version : new String[1]);
        version[0] = "UNKNOWN";
        JsonNode status = getStatus();
        if ((status == null) || !status.isObject() || !status.has("status")) {
            return false;
        }
        status = status.get("status");
        if (!status.isObject() || !status.has("icingaapplication")) {
            return false;
        }
        JsonNode application = status.get("icingaapplication");
        if (!application.isObject() || !application.has("app")) {
            return false;
        }
        application = application.get("app");
        if (!application.isObject() || !application.has("version")) {
            return false;
        }
        JsonNode applicationVersion = application.get("version");
        if (!applicationVersion.isValueNode()) {
            return false;
        }
        version[0] = applicationVersion.asText();
        if (version[0] == null) {
            return false;
        }
        version[0] = version[0].trim();
        return version[0].startsWith("v" + ICINGA2_APPLICATION_VERSION + ".");
    }
}
