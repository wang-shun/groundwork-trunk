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

package org.groundwork.rs.client;

import org.groundwork.rs.dto.DtoFileList;
import org.jboss.resteasy.util.GenericType;

import javax.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * CollectorConfigClient
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class CollectorConfigClient extends BaseRestClient {

    public static final String COLLECTOR_CONFIG_IDENTITY_SECTION_KEY = "identity";

    public static final String COLLECTOR_CONFIG_IDENTITY_SECTION_AGENT_TYPE_KEY = "agentType";
    public static final String COLLECTOR_CONFIG_IDENTITY_SECTION_PREFIX_KEY = "prefix";
    public static final String COLLECTOR_CONFIG_IDENTITY_SECTION_HOST_NAME_KEY = "hostName";
    public static final String COLLECTOR_CONFIG_IDENTITY_SECTION_IPV4_KEY = "ipv4";
    public static final String COLLECTOR_CONFIG_IDENTITY_SECTION_IPV6_KEY = "ipv6";
    public static final String COLLECTOR_CONFIG_IDENTITY_SECTION_MAC_KEY = "mac";
    public static final String COLLECTOR_CONFIG_IDENTITY_SECTION_CHARACTERISTIC_KEY = "characteristic";

    /**
     * Create a Collector Config Client for performing configuration and
     * template management operations.
     * Deployment URL example:
     * <pre>http://server-name[:port]/foundation-webapp/api</pre>
     *
     * @param deploymentUrl the deployment root URL for all Rest operations
     */
    public CollectorConfigClient(String deploymentUrl) {
        super(deploymentUrl);
    }

    /**
     * Create a Collector Config Client for performing configuration and
     * template management operations.
     * Deployment URL example:
     * <pre>http://server-name[:port]/foundation-webapp/api</pre>
     * <p>Supported Media Types</p>
     * <ul>application/xml {@link javax.ws.rs.core.MediaType#APPLICATION_XML_TYPE}</ul>
     * <ul>application/json {@link javax.ws.rs.core.MediaType#APPLICATION_JSON_TYPE}</ul>
     *
     * @param deploymentUrl the deployment root URL for all Rest operations
     * @param mediaType a valid, supported internet media type (MIME) supported
     */
    public CollectorConfigClient(String deploymentUrl, MediaType mediaType) {
        this(deploymentUrl);
        this.mediaType = mediaType;
    }

    /**
     * Get collector configuration file names. File names include the
     * ".yaml" extension and are returned in case insensitive sorted order.
     *
     * @return sorted list of collector configuration file names
     */
    public List<String> listCollectorConfigFileNames() {
        String requestUrl = build("/collectors/configurations");
        String requestDescription = "list collector configurations";
        DtoFileList dtoFileList = clientRequest(requestUrl, requestDescription, new GenericType<DtoFileList>(){});
        return ((dtoFileList != null) ? dtoFileList.getFiles() : Collections.EMPTY_LIST);
    }

    /**
     * Get YAML collector configuration from file. File name must include
     * the ".yaml" extension.
     *
     * @param fileName collector configuration file name
     * @return YAML collector configuration
     */
    public String getCollectorConfig(String fileName) {
        String requestUrl = buildUrlWithPath("/collectors/configurations/", fileName);
        String requestDescription = "get collector configuration";
        byte [] content = clientRequest("GET", requestUrl, "text/yaml", null, null, requestDescription,
                new GenericType<byte []>(){});
        try {
            return new String(content, "UTF-8");
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException(uee);
        }
    }

    /**
     * Create collector configuration file from collector configuration
     * template selected by agent type and prefix identity properties.
     * File name must include the ".yaml" extension. All supplied identity
     * properties are merged into the template on create. Created YAML
     * collector configuration is returned using the UTF-8 character set.
     *
     * @param fileName collector configuration file name
     * @param agentType agent type identity property, (selects template, required)
     * @param prefix prefix identity property, (selects template, required)
     * @param hostName host name identity property, (required)
     * @param ipv4 ipv4 identity property
     * @param ipv6 ipv6 identity property
     * @param mac mac identity property
     * @param characteristic characteristic identity property
     * @return YAML collector configuration
     */
    public String createCollectorConfig(String fileName, String agentType, String prefix, String hostName,
                                        String ipv4, String ipv6, String mac, String characteristic) {
        String requestUrl = buildUrlWithPath("/collectors/configurations/", fileName);
        String requestDescription = "create collector configuration";
        List<String> identityParamNames = new ArrayList<String>();
        List<String> identityParamValues = new ArrayList<String>();
        if (!isEmpty(agentType)) {
            identityParamNames.add(COLLECTOR_CONFIG_IDENTITY_SECTION_AGENT_TYPE_KEY);
            identityParamValues.add(agentType);
        }
        if (!isEmpty(prefix)) {
            identityParamNames.add(COLLECTOR_CONFIG_IDENTITY_SECTION_PREFIX_KEY);
            identityParamValues.add(prefix);
        }
        if (!isEmpty(hostName)) {
            identityParamNames.add(COLLECTOR_CONFIG_IDENTITY_SECTION_HOST_NAME_KEY);
            identityParamValues.add(hostName);
        }
        if (!isEmpty(ipv4)) {
            identityParamNames.add(COLLECTOR_CONFIG_IDENTITY_SECTION_IPV4_KEY);
            identityParamValues.add(ipv4);
        }
        if (!isEmpty(ipv6)) {
            identityParamNames.add(COLLECTOR_CONFIG_IDENTITY_SECTION_IPV6_KEY);
            identityParamValues.add(ipv6);
        }
        if (!isEmpty(mac)) {
            identityParamNames.add(COLLECTOR_CONFIG_IDENTITY_SECTION_MAC_KEY);
            identityParamValues.add(mac);
        }
        if (!isEmpty(characteristic)) {
            identityParamNames.add(COLLECTOR_CONFIG_IDENTITY_SECTION_CHARACTERISTIC_KEY);
            identityParamValues.add(characteristic);
        }
        try {
            String identityParams = buildEncodedPostParams(identityParamNames.toArray(new String[identityParamNames.size()]),
                    identityParamValues.toArray(new String[identityParamValues.size()]));
            byte [] content = clientRequest("POST", requestUrl, "text/yaml", identityParams,
                    MediaType.APPLICATION_FORM_URLENCODED, requestDescription, new GenericType<byte []>(){});
            return new String(content, "UTF-8");
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException(uee);
        }
    }

    /**
     * Put YAML collector configuration to file. File name must include
     * the ".yaml" extension.
     *
     * @param fileName collector configuration file name
     * @param content YAML collector configuration
     */
    public void putCollectorConfig(String fileName, String content) {
        String requestUrl = buildUrlWithPath("/collectors/configurations/", fileName);
        String requestDescription = "put collector configuration";
        try {
            clientRequest("PUT", requestUrl, null, content.getBytes("UTF-8"), "text/yaml", requestDescription,
                    new GenericType<Void>(){});
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException(uee);
        }
    }

    /**
     * Delete YAML collector configuration file. File name must include
     * the ".yaml" extension.
     *
     * @param fileName collector configuration file name
     */
    public void deleteCollectorConfig(String fileName) {
        String requestUrl = buildUrlWithPath("/collectors/configurations/", fileName);
        String requestDescription = "delete collector configuration";
        clientRequest("DELETE", requestUrl, requestDescription, new GenericType<Void>(){});
    }

    /**
     * Get collector configuration template file names. File names include
     * the ".yaml" extension and are returned in case insensitive sorted order.
     *
     * @return sorted list of collector configuration template file names
     */
    public List<String> listCollectorConfigTemplateFileNames() {
        String requestUrl = build("/collectors/templates");
        String requestDescription = "list collector configuration templates";
        DtoFileList dtoFileList = clientRequest(requestUrl, requestDescription, new GenericType<DtoFileList>(){});
        return ((dtoFileList != null) ? dtoFileList.getFiles() : Collections.EMPTY_LIST);
    }

    /**
     * Get YAML collector configuration template from file. File name must
     * include the ".yaml" extension.
     *
     * @param fileName collector configuration template file name
     * @return YAML collector configuration template
     */
    public String getCollectorConfigTemplate(String fileName) {
        String requestUrl = buildUrlWithPath("/collectors/templates/", fileName);
        String requestDescription = "get collector configuration template";
        byte [] content = clientRequest("GET", requestUrl, "text/yaml", null, null, requestDescription,
                new GenericType<byte []>(){});
        try {
            return new String(content, "UTF-8");
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException(uee);
        }
    }

    /**
     * Put YAML collector configuration template to file. File name must
     * include the ".yaml" extension.
     *
     * @param fileName collector configuration template file name
     * @param content YAML collector configuration template
     */
    public void putCollectorConfigTemplate(String fileName, String content) {
        String requestUrl = buildUrlWithPath("/collectors/templates/", fileName);
        String requestDescription = "put collector configuration template";
        try {
            clientRequest("PUT", requestUrl, null, content.getBytes("UTF-8"), "text/yaml", requestDescription,
                    new GenericType<Void>(){});
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException(uee);
        }
    }

    /**
     * Delete YAML collector configuration template file. File name must
     * include the ".yaml" extension.
     *
     * @param fileName collector configuration template file name
     */
    public void deleteCollectorConfigTemplate(String fileName) {
        String requestUrl = buildUrlWithPath("/collectors/templates/", fileName);
        String requestDescription = "delete collector configuration template";
        clientRequest("DELETE", requestUrl, requestDescription, new GenericType<Void>(){});
    }
}
