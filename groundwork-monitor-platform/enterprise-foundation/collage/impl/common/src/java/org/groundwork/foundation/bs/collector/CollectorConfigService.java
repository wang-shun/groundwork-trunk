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

package org.groundwork.foundation.bs.collector;

import java.util.List;
import java.util.Map;

/**
 * CollectorConfigService
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public interface CollectorConfigService {

    public static final String COLLECTOR_CONFIG_IDENTITY_SECTION_KEY = "identity";

    public static final String COLLECTOR_CONFIG_IDENTITY_SECTION_AGENT_TYPE_KEY = "agentType";
    public static final String COLLECTOR_CONFIG_IDENTITY_SECTION_PREFIX_KEY = "prefix";
    public static final String COLLECTOR_CONFIG_IDENTITY_SECTION_HOST_NAME_KEY = "hostName";
    public static final String COLLECTOR_CONFIG_IDENTITY_SECTION_IPV4_KEY = "ipv4";
    public static final String COLLECTOR_CONFIG_IDENTITY_SECTION_IPV6_KEY = "ipv6";
    public static final String COLLECTOR_CONFIG_IDENTITY_SECTION_MAC_KEY = "mac";
    public static final String COLLECTOR_CONFIG_IDENTITY_SECTION_CHARACTERISTIC_KEY = "characteristic";

    /**
     * Get collector YAML config file names. File names include
     * the ".yaml" extension and are returned in case insensitive
     * sorted order.
     * 
     * @return list of YAML config file names
     */
    List<String> getCollectorConfigFileNames();
    
    /**
     * Get collector YAML config file. File name must include the
     * ".yaml" extension.
     *
     * @param fileName YAML config file name
     * @return YAML config file string or null if not found
     */
    String getCollectorConfig(String fileName);

    /**
     * Put collector YAML config file. File name must include the
     * ".yaml" extension. Put will fail if specified file content
     * is not YAML parsable.
     *
     * @param fileName YAML config file name
     * @param content YAML config file content
     * @return put success
     */
    boolean putCollectorConfig(String fileName, String content);

    /**
     * Delete collector YAML config file. File name must include
     * the ".yaml" extension.
     *
     * @param fileName YAML config file name
     * @return delete success
     */
    boolean deleteCollectorConfig(String fileName);

    /**
     * Get collector YAML config template file names. File names
     * include the ".yaml" extension and are returned in case
     * insensitive sorted order.
     *
     * @return list of YAML config template file names
     */
    List<String> getCollectorConfigTemplateFileNames();

    /**
     * Get collector YAML config template file. File name must
     * include the ".yaml" extension.
     *
     * @param templateFileName YAML config template file name
     * @return YAML config template file string or null if not found
     */
    String getCollectorConfigTemplate(String templateFileName);

    /**
     * Find collector YAML config template file based on identity
     * properties matching those in template. The agent type and
     * prefix identity properties are used to match the template.
     *
     * @param identityProperties identity properties to match template
     * @return YAML config template file name
     */
    String findCollectorConfigTemplate(Map<String,String> identityProperties);

    /**
     * Put collector YAML config template file. File name must
     * include the ".yaml" extension. Put will fail if specified
     * file content is not YAML parsable.
     *
     * @param templateFileName YAML config template file name
     * @param content YAML config template file content
     * @return put success
     */
    boolean putCollectorConfigTemplate(String templateFileName, String content);

    /**
     * Delete collector YAML config template file. File name must
     * include the ".yaml" extension.
     *
     * @param templateFileName YAML config template file name
     * @return delete success
     */
    boolean deleteCollectorConfigTemplate(String templateFileName);

    /**
     * Create collector YAML config file from template and specified
     * identity properties merged into the template. File name must
     * include the ".yaml" extension.
     *
     * @param templateFileName YAML config template file name
     * @param identityProperties identity properties to merge into template
     * @param fileName YAML config file name
     * @return created YAML config file string or null if not created
     */
    String createCollectorConfig(String templateFileName, Map<String,String> identityProperties, String fileName);

    /**
     * Parse collector YAML config file content into a string map.
     *
     * @param content YAML config file content
     * @return parsed config string map or null if not parsable
     */
    Map<String,Object> parseCollectorConfigContent(String content);

    /**
     * Format parsed string map into collector YAML config file content.
     *
     * @param parsed parsed config string map
     * @param flow use flow formatting for collections
     * @return YAML config file content
     */
    String formatCollectorConfigContent(Map<String,Object> parsed, boolean flow);
}
