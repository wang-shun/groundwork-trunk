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
 * Icinga2ConfigurationTest
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MonitorAgentConfiguration.class})
public class Icinga2ConfigurationTest extends AbstractAgentTest {

    private static Logger log = Logger.getLogger(Icinga2ConfigurationTest.class);

    @Test
    public void testIcinga2Configuration() {
        // read configuration from test data
        ConnectionConfiguration configuration =
                configurationService.readConfiguration("./src/test/testdata/icinga2/cloudhub-icinga2-1.xml");
        assert configuration != null;
        assert configuration.getCommon().getDisplayName().equals("__Icinga2 Unit Test__");
        assert configuration.getCommon().getApplicationType().equals("ICINGA2");
        assert configuration.getGwos().getWsUsername().equals("REMOTEAPIACCESS");
        assert configuration.getConnection().getServer().equals("demo70.groundwork.groundworkopensource.com");
        assert configuration.getConnection() instanceof Icinga2Connection;
        assert ((Icinga2Connection)configuration.getConnection()).getPort().equals("5665");
        assert ((Icinga2Connection)configuration.getConnection()).getUsername().equals("root");
        assert ((Icinga2Connection)configuration.getConnection()).getPassword().equals("fc764746b29dfa82");
        assert ((Icinga2Connection)configuration.getConnection()).getTrustSSLCACertificate().equals("/usr/local/groundwork/config/cloudhub/icinga2/ca.crt");
        assert ((Icinga2Connection)configuration.getConnection()).getTrustSSLCACertificateKeystore().equals("/usr/local/groundwork/config/cloudhub/icinga2/icinga2-keystore.jks");
        assert ((Icinga2Connection)configuration.getConnection()).getTrustSSLCACertificateKeystorePassword().equals("icinga2");
        assert ((Icinga2Connection)configuration.getConnection()).isTrustAllSSL();
        assert ((Icinga2Connection)configuration.getConnection()).isMetricsGraphed();
        // save configuration
        configurationService.saveConfiguration(configuration);
        File configurationFile = new File(configuration.getCommon().getPathToConfigurationFile(),
                configuration.getCommon().getConfigurationFile());
        assert configurationFile.isFile();
        List<? extends ConnectionConfiguration> configurations =
                configurationService.listConfigurations(VirtualSystem.ICINGA2);
        assert configurations != null;
        assert !configurations.isEmpty();
        ConnectionConfiguration savedConfiguration = null;
        for (ConnectionConfiguration listConfiguration : configurations) {
            if (configuration.getCommon().getDisplayName().equals("__Icinga2 Unit Test__")) {
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
