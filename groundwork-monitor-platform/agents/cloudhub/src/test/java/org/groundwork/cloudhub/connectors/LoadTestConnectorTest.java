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

package org.groundwork.cloudhub.connectors;

import org.apache.log4j.Logger;
import org.groundwork.cloudhub.AbstractAgentTest;
import org.groundwork.cloudhub.ServerConfigurator;
import org.groundwork.cloudhub.configuration.LoadTestConfiguration;
import org.groundwork.cloudhub.inventory.DataCenterInventory;
import org.groundwork.cloudhub.inventory.InventoryContainerNode;
import org.groundwork.cloudhub.inventory.VirtualMachineNode;
import org.groundwork.cloudhub.monitor.MonitorAgentConfiguration;
import org.groundwork.rs.dto.profiles.CloudHubProfile;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * LoadTestConnectorTest
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MonitorAgentConfiguration.class})
public class LoadTestConnectorTest extends AbstractAgentTest {

    private static Logger log = Logger.getLogger(LoadTestConnectorTest.class);

    @Test
    public void testLoadTestConnector() {
        LoadTestConfiguration loadTestConfiguration = null;
        try {
            loadTestConfiguration = new LoadTestConfiguration();
            ServerConfigurator.setupLoadTestConnection(loadTestConfiguration.getConnection());
            ServerConfigurator.setupLocalGroundworkServer(loadTestConfiguration.getGwos());
            configurationService.saveConfiguration(loadTestConfiguration);
            MonitoringConnector connector = connectorFactory.getMonitoringConnector(loadTestConfiguration);
            ManagementConnector management = connectorFactory.getManagementConnector(loadTestConfiguration);
            connector.connect(loadTestConfiguration.getConnection());
            management.openConnection(loadTestConfiguration.getConnection());
            DataCenterInventory inventory = management.gatherInventory();
            assert inventory != null;
            assert inventory.getHypervisors().size() == 2;
            InventoryContainerNode hypervisor = inventory.getHypervisors().get("loadtest-hypervisor-0");
            assert hypervisor != null;
            assert hypervisor.getName().equals("loadtest-hypervisor-0");
            assert hypervisor.getVms().size() == 5;
            VirtualMachineNode vm = hypervisor.getVms().get("loadtest-vm-0");
            assert vm != null;
            assert inventory.getVirtualMachines().size() == 10;
            vm = inventory.getVirtualMachines().get("loadtest-vm-0");
            assert vm != null;
            CloudHubProfile profile = management.readCloudProfile();
            assert profile != null;
            assert profile.getHypervisor() != null;
            assert profile.getHypervisor().getMetrics() != null;
            assert profile.getHypervisor().getMetrics().size() == 10;
            assert profile.getHypervisor().getMetrics().get(0).getName().equals("loadtest-hypervisor-metric-0");
            assert profile != null;
            assert profile.getVm() != null;
            assert profile.getVm().getMetrics() != null;
            assert profile.getVm().getMetrics().size() == 10;
            assert profile.getVm().getMetrics().get(0).getName().equals("loadtest-vm-metric-0");
            connector.disconnect();
            management.closeConnection();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            if (loadTestConfiguration != null) {
                configurationService.deleteConfiguration(loadTestConfiguration);
            }
        }
    }
}
