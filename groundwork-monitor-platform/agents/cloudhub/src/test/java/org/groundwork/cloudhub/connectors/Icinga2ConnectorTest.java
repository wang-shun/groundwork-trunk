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
import org.groundwork.cloudhub.configuration.Icinga2Configuration;
import org.groundwork.cloudhub.monitor.MonitorAgentConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Icinga2ConnectorTest
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MonitorAgentConfiguration.class})
public class Icinga2ConnectorTest extends AbstractAgentTest {

    private static Logger log = Logger.getLogger(Icinga2ConnectorTest.class);

    @Test
    public void testIcinga2Connector() {
        Icinga2Configuration icinga2Configuration = null;
        try {
            icinga2Configuration = ServerConfigurator.createIcinga2Server(configurationService);
            MonitorConnector connector = connectorFactory.getMonitorConnector(icinga2Configuration);
            connector.connect(icinga2Configuration.getConnection());
            connector.suspend();
            connector.unsuspend();
            connector.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            if (icinga2Configuration != null) {
                configurationService.deleteConfiguration(icinga2Configuration);
            }
        }
    }
}
