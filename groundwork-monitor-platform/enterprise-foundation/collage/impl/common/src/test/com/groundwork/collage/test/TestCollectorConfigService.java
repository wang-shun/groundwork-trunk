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

package com.groundwork.collage.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.groundwork.foundation.bs.collector.CollectorConfigService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TestCollectorConfigService
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class TestCollectorConfigService extends TestCase {

    private static final String IDENTITY = CollectorConfigService.COLLECTOR_CONFIG_IDENTITY_SECTION_KEY;
    private static final String AGENT_TYPE = CollectorConfigService.COLLECTOR_CONFIG_IDENTITY_SECTION_AGENT_TYPE_KEY;
    private static final String PREFIX = CollectorConfigService.COLLECTOR_CONFIG_IDENTITY_SECTION_PREFIX_KEY;

    private static final String TEST_TEMPLATE_FILE_NAME = "__test_template__.yaml";
    private static final String TEST_CONFIG_FILE_NAME = "__test_config__.yaml";
    private static final String EOL = System.getProperty("line.separator");
    private static final String BLOCK_TEST_TEMPLATE =
            IDENTITY + ":" + EOL +
            "    " + AGENT_TYPE + ": p" + EOL +
            "    " + PREFIX + ": px" + EOL +
            "    template: template" + EOL +
            "" + EOL +
            "template: template" + EOL;
    private static final String FLOW_TEST_TEMPLATE =
            IDENTITY + ": {" + AGENT_TYPE + ": p, " + PREFIX + ": px, template: template}" + EOL +
            "template: template" + EOL;
    private static final String TEST_CONFIG =
            IDENTITY + ":"+ EOL +
            "    config: config" + EOL +
            "" + EOL +
            "config: config" + EOL;

    private CollectorConfigService collectorConfigService;

    public TestCollectorConfigService(String x) {
        super(x);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();

        // run all tests
        suite = new TestSuite(TestCollectorConfigService.class);

        // or a subset thereoff
        //suite.addTest(new TestCollectorConfigService("testCollectorConfigService"));

        return suite;
    }

    public void setUp() throws Exception
    {
        super.setUp();

        // Retrieve business service
        collectorConfigService = collage.getCollectorConfigService();
        assertNotNull(collectorConfigService);
    }

    /**
     * Test collector config service
     */
    public void testCollectorConfigService() {
        testCollectorConfigService(BLOCK_TEST_TEMPLATE, false);
        testCollectorConfigService(FLOW_TEST_TEMPLATE, true);
    }

    /**
     * Test collector config service with test template content.
     *
     * @param testTemplate test template content
     * @param flow test template content flow or block style
     */
    public void testCollectorConfigService(String testTemplate, boolean flow) {

        // silently cleanup from prior failures
        collectorConfigService.deleteCollectorConfig(TEST_CONFIG_FILE_NAME);
        collectorConfigService.deleteCollectorConfigTemplate(TEST_TEMPLATE_FILE_NAME);

        // load and validate test template
        assert collectorConfigService.getCollectorConfigTemplateFileNames() != null;
        assert collectorConfigService.putCollectorConfigTemplate(TEST_TEMPLATE_FILE_NAME, testTemplate);
        String testTemplateContent = collectorConfigService.getCollectorConfigTemplate(TEST_TEMPLATE_FILE_NAME);
        assert testTemplateContent != null;
        Map<String,Object> testTemplateMap = collectorConfigService.parseCollectorConfigContent(testTemplateContent);
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
        List<String> templates = collectorConfigService.getCollectorConfigTemplateFileNames();
        assert templates != null;
        assert templates.contains(TEST_TEMPLATE_FILE_NAME);

        // load and validate test configuration
        assert collectorConfigService.getCollectorConfigFileNames() != null;
        assert collectorConfigService.putCollectorConfig(TEST_CONFIG_FILE_NAME, TEST_CONFIG);
        String testConfigContent = collectorConfigService.getCollectorConfig(TEST_CONFIG_FILE_NAME);
        assert testConfigContent != null;
        Map<String,Object> testConfigMap = collectorConfigService.parseCollectorConfigContent(testConfigContent);
        assert testConfigMap != null;
        assert testConfigMap.containsKey(IDENTITY);
        assert ((Map<String,Object>)testConfigMap.get(IDENTITY)).containsKey("config");
        assert ((Map<String,Object>)testConfigMap.get(IDENTITY)).get("config").equals("config");
        assert testConfigMap.containsKey("config");
        assert testConfigMap.get("config").equals("config");
        List<String> configs = collectorConfigService.getCollectorConfigFileNames();
        assert configs != null;
        assert configs.contains(TEST_CONFIG_FILE_NAME);

        // create from test template and validate test configuration
        Map<String,String> identityProperties = new HashMap<String,String>();
        identityProperties.put(AGENT_TYPE, "p");
        identityProperties.put("test", "test");
        String templateFileName = collectorConfigService.findCollectorConfigTemplate(identityProperties);
        assert TEST_TEMPLATE_FILE_NAME.equals(templateFileName);
        testConfigContent = collectorConfigService.createCollectorConfig(templateFileName, identityProperties,
                TEST_CONFIG_FILE_NAME);
        assert testConfigContent != null;
        assert (testConfigContent.contains("{") && testConfigContent.contains("}")) == flow;
        testConfigMap = collectorConfigService.parseCollectorConfigContent(testConfigContent);
        assert testConfigMap != null;
        assert testConfigMap.containsKey(IDENTITY);
        assert ((Map<String,Object>)testConfigMap.get(IDENTITY)).containsKey(AGENT_TYPE);
        assert ((Map<String,Object>)testConfigMap.get(IDENTITY)).get(AGENT_TYPE).equals("p");
        assert ((Map<String,Object>)testConfigMap.get(IDENTITY)).containsKey(PREFIX);
        assert ((Map<String,Object>)testConfigMap.get(IDENTITY)).get(PREFIX).equals("px");
        assert ((Map<String,Object>)testConfigMap.get(IDENTITY)).containsKey("template");
        assert ((Map<String,Object>)testConfigMap.get(IDENTITY)).get("template").equals("template");
        assert ((Map<String,Object>)testConfigMap.get(IDENTITY)).containsKey("test");
        assert ((Map<String,Object>)testConfigMap.get(IDENTITY)).get("test").equals("test");
        assert testConfigMap.containsKey("template");
        assert testConfigMap.get("template").equals("template");
        testConfigContent = collectorConfigService.getCollectorConfig(TEST_CONFIG_FILE_NAME);
        assert testConfigContent != null;
        assert (testConfigContent.contains("{") && testConfigContent.contains("}")) == flow;
        testConfigMap = collectorConfigService.parseCollectorConfigContent(testConfigContent);
        assert testConfigMap != null;
        assert testConfigMap.containsKey(IDENTITY);
        assert ((Map<String,Object>)testConfigMap.get(IDENTITY)).containsKey(AGENT_TYPE);
        assert ((Map<String,Object>)testConfigMap.get(IDENTITY)).get(AGENT_TYPE).equals("p");
        assert ((Map<String,Object>)testConfigMap.get(IDENTITY)).containsKey(PREFIX);
        assert ((Map<String,Object>)testConfigMap.get(IDENTITY)).get(PREFIX).equals("px");
        assert ((Map<String,Object>)testConfigMap.get(IDENTITY)).containsKey("template");
        assert ((Map<String,Object>)testConfigMap.get(IDENTITY)).get("template").equals("template");
        assert ((Map<String,Object>)testConfigMap.get(IDENTITY)).containsKey("test");
        assert ((Map<String,Object>)testConfigMap.get(IDENTITY)).get("test").equals("test");
        assert testConfigMap.containsKey("template");
        assert testConfigMap.get("template").equals("template");
        configs = collectorConfigService.getCollectorConfigFileNames();
        assert configs != null;
        assert configs.contains(TEST_CONFIG_FILE_NAME);

        // delete test configuration
        assert collectorConfigService.deleteCollectorConfig(TEST_CONFIG_FILE_NAME);
        configs = collectorConfigService.getCollectorConfigFileNames();
        assert configs != null;
        assert !configs.contains(TEST_CONFIG_FILE_NAME);

        // delete test template
        assert collectorConfigService.deleteCollectorConfigTemplate(TEST_TEMPLATE_FILE_NAME);
        templates = collectorConfigService.getCollectorConfigTemplateFileNames();
        assert templates != null;
        assert !templates.contains(TEST_TEMPLATE_FILE_NAME);
    }
}
