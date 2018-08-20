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

import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.StringReader;
import java.util.List;
import java.util.Map;

/**
 * ConnectorConfigClientTest
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class CollectorConfigClientTest extends AbstractClientTest {

    private static final String IDENTITY = CollectorConfigClient.COLLECTOR_CONFIG_IDENTITY_SECTION_KEY;
    private static final String AGENT_TYPE = CollectorConfigClient.COLLECTOR_CONFIG_IDENTITY_SECTION_AGENT_TYPE_KEY;
    private static final String PREFIX = CollectorConfigClient.COLLECTOR_CONFIG_IDENTITY_SECTION_PREFIX_KEY;
    private static final String HOST_NAME = CollectorConfigClient.COLLECTOR_CONFIG_IDENTITY_SECTION_HOST_NAME_KEY;

    private static final String TEST_TEMPLATE_FILE_NAME = "__test_client_template__.yaml";
    private static final String TEST_CONFIG_FILE_NAME = "__test_client_config__.yaml";
    private static final String EOL = System.getProperty("line.separator");
    private static final String TEST_TEMPLATE =
            IDENTITY + ":" + EOL +
                    "    " + AGENT_TYPE + ": p" + EOL +
                    "    " + PREFIX + ": px" + EOL +
                    "    template: template" + EOL +
                    "" + EOL +
                    "template: template" + EOL;
    private static final String TEST_CONFIG =
            IDENTITY + ":"+ EOL +
                    "    config: config" + EOL +
                    "" + EOL +
                    "config: config" + EOL;

    @Test
    public void testCollectorConfigClient() {
        if (serverDown) return;
        CollectorConfigClient client = new CollectorConfigClient(getDeploymentURL());

        // silently cleanup from prior failures
        client.deleteCollectorConfig(TEST_CONFIG_FILE_NAME);
        client.deleteCollectorConfigTemplate(TEST_TEMPLATE_FILE_NAME);

        // load and validate test template
        client.putCollectorConfigTemplate(TEST_TEMPLATE_FILE_NAME, TEST_TEMPLATE);
        String testTemplateContent = client.getCollectorConfigTemplate(TEST_TEMPLATE_FILE_NAME);
        assert testTemplateContent != null;
        Map<String,Object> testTemplateMap = (Map<String,Object>)(new Yaml()).load(new StringReader(testTemplateContent));
        assert testTemplateMap != null;
        assert testTemplateMap.containsKey(IDENTITY);
        assert ((Map<String,Object>)testTemplateMap.get(IDENTITY)).containsKey(AGENT_TYPE);
        assert ((Map<String,Object>)testTemplateMap.get(IDENTITY)).get(AGENT_TYPE).equals("p");
        assert ((Map<String,Object>)testTemplateMap.get(IDENTITY)).containsKey(PREFIX);
        assert ((Map<String,Object>)testTemplateMap.get(IDENTITY)).get(PREFIX).equals("px");
        assert ((Map<String,Object>)testTemplateMap.get(IDENTITY)).containsKey("template");
        assert ((Map<String,Object>)testTemplateMap.get(IDENTITY)).get("template").equals("template");
        assert testTemplateMap.containsKey("template");
        assert testTemplateMap.get("template").equals("template");
        List<String> templates = client.listCollectorConfigTemplateFileNames();
        assert templates != null;
        assert templates.contains(TEST_TEMPLATE_FILE_NAME);

        // load and validate test configuration
        client.putCollectorConfig(TEST_CONFIG_FILE_NAME, TEST_CONFIG);
        String testConfigContent = client.getCollectorConfig(TEST_CONFIG_FILE_NAME);
        assert testConfigContent != null;
        Map<String,Object> testConfigMap = (Map<String,Object>)(new Yaml()).load(new StringReader(testConfigContent));
        assert testConfigMap != null;
        assert testConfigMap.containsKey(IDENTITY);
        assert ((Map<String,Object>)testConfigMap.get(IDENTITY)).containsKey("config");
        assert ((Map<String,Object>)testConfigMap.get(IDENTITY)).get("config").equals("config");
        assert testConfigMap.containsKey("config");
        assert testConfigMap.get("config").equals("config");
        List<String> configs = client.listCollectorConfigFileNames();
        assert configs != null;
        assert configs.contains(TEST_CONFIG_FILE_NAME);

        // create from test template and validate test configuration
        testConfigContent = client.createCollectorConfig(TEST_CONFIG_FILE_NAME, "p", "test", "localhost", null, null,
                null, null);
        assert testConfigContent != null;
        testConfigMap = (Map<String,Object>)(new Yaml()).load(new StringReader(testConfigContent));
        assert testConfigMap != null;
        assert testConfigMap.containsKey(IDENTITY);
        assert ((Map<String,Object>)testConfigMap.get(IDENTITY)).containsKey(AGENT_TYPE);
        assert ((Map<String,Object>)testConfigMap.get(IDENTITY)).get(AGENT_TYPE).equals("p");
        assert ((Map<String,Object>)testConfigMap.get(IDENTITY)).containsKey(PREFIX);
        assert ((Map<String,Object>)testConfigMap.get(IDENTITY)).get(PREFIX).equals("test");
        assert ((Map<String,Object>)testConfigMap.get(IDENTITY)).containsKey(HOST_NAME);
        assert ((Map<String,Object>)testConfigMap.get(IDENTITY)).get(HOST_NAME).equals("localhost");
        assert ((Map<String,Object>)testConfigMap.get(IDENTITY)).containsKey("template");
        assert ((Map<String,Object>)testConfigMap.get(IDENTITY)).get("template").equals("template");
        assert testConfigMap.containsKey("template");
        assert testConfigMap.get("template").equals("template");
        testConfigContent = client.getCollectorConfig(TEST_CONFIG_FILE_NAME);
        assert testConfigContent != null;
        testConfigMap = (Map<String,Object>)(new Yaml()).load(new StringReader(testConfigContent));
        assert testConfigMap != null;
        assert testConfigMap.containsKey(IDENTITY);
        assert ((Map<String,Object>)testConfigMap.get(IDENTITY)).containsKey(AGENT_TYPE);
        assert ((Map<String,Object>)testConfigMap.get(IDENTITY)).get(AGENT_TYPE).equals("p");
        assert ((Map<String,Object>)testConfigMap.get(IDENTITY)).containsKey(PREFIX);
        assert ((Map<String,Object>)testConfigMap.get(IDENTITY)).get(PREFIX).equals("test");
        assert ((Map<String,Object>)testConfigMap.get(IDENTITY)).containsKey(HOST_NAME);
        assert ((Map<String,Object>)testConfigMap.get(IDENTITY)).get(HOST_NAME).equals("localhost");
        assert ((Map<String,Object>)testConfigMap.get(IDENTITY)).containsKey("template");
        assert ((Map<String,Object>)testConfigMap.get(IDENTITY)).get("template").equals("template");
        assert testConfigMap.containsKey("template");
        assert testConfigMap.get("template").equals("template");
        configs = client.listCollectorConfigFileNames();
        assert configs != null;
        assert configs.contains(TEST_CONFIG_FILE_NAME);

        // delete test configuration
        client.deleteCollectorConfig(TEST_CONFIG_FILE_NAME);
        configs = client.listCollectorConfigFileNames();
        assert configs != null;
        assert !configs.contains(TEST_CONFIG_FILE_NAME);

        // delete test template
        client.deleteCollectorConfigTemplate(TEST_TEMPLATE_FILE_NAME);
        templates = client.listCollectorConfigTemplateFileNames();
        assert templates != null;
        assert !templates.contains(TEST_TEMPLATE_FILE_NAME);
    }
}
