/*
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

package org.groundwork.cloudhub.configuration;

import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.AbstractAgentTest;
import org.groundwork.cloudhub.monitor.MonitorAgentConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.util.List;

/**
 * LoadTestConfigurationTest
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MonitorAgentConfiguration.class})
public class LoadTestConfigurationTest extends AbstractAgentTest {

    private static Logger log = Logger.getLogger(LoadTestConfigurationTest.class);

    @Test
    public void testLoadTestConfiguration() {
        // read configuration from test data
        ConnectionConfiguration configuration =
                configurationService.readConfiguration("./src/test/testdata/loadtest/cloudhub-loadtest-1.xml");
        assert configuration != null;
        assert configuration.getCommon().getDisplayName().equals("__Load Test Unit Test__");
        assert configuration.getCommon().getApplicationType().equals("NETAPP");
        assert configuration.getGwos().getWsUsername().equals("REMOTEAPIACCESS");
        assert configuration.getConnection() instanceof LoadTestConnection;
        assert ((LoadTestConnection)configuration.getConnection()).getHosts() == 150;
        assert ((LoadTestConnection)configuration.getConnection()).getHostGroups() == 15;
        assert ((LoadTestConnection)configuration.getConnection()).getServices() == 15;
        assert ((LoadTestConnection)configuration.getConnection()).getHostsDownPercent() == Float.parseFloat("1.2");
        assert ((LoadTestConnection)configuration.getConnection()).getServicesCriticalPercent() == Float.parseFloat("3.6");
        // save configuration
        configurationService.saveConfiguration(configuration);
        File configurationFile = new File(configuration.getCommon().getPathToConfigurationFile(),
                configuration.getCommon().getConfigurationFile());
        assert configurationFile.isFile();
        List<? extends ConnectionConfiguration> configurations =
                configurationService.listConfigurations(VirtualSystem.LOADTEST);
        assert configurations != null;
        assert !configurations.isEmpty();
        ConnectionConfiguration savedConfiguration = null;
        for (ConnectionConfiguration listConfiguration : configurations) {
            if (configuration.getCommon().getDisplayName().equals("__Load Test Unit Test__")) {
                savedConfiguration = configuration;
                break;
            }
        }
        assert savedConfiguration != null;
        // delete configuration
        configurationService.deleteConfiguration(savedConfiguration);
        assert !configurationFile.exists();
    }
}
