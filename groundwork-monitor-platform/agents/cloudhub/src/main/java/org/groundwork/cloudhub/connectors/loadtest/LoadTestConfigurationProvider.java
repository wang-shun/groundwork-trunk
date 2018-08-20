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

package org.groundwork.cloudhub.connectors.loadtest;

import org.groundwork.cloudhub.configuration.BaseConfigurationProvider;
import org.groundwork.cloudhub.configuration.ConfigurationProvider;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.configuration.LoadTestConfiguration;
import org.groundwork.cloudhub.inventory.InventoryType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * LoadTestConfigurationProvider
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@Service(LoadTestConfigurationProvider.NAME)
public class LoadTestConfigurationProvider extends BaseConfigurationProvider implements ConfigurationProvider {

    public static final String NAME = "LoadTestConfigurationProvider";

    @Value("${synchronizer.services.loadtest.enabled:false}")
    protected Boolean serviceSyncEnabled = false;

    private static final String MGMT_SERVER_LOAD_TEST = "Load Test Management Server";
    private static final String HYPERVISOR_LOAD_TEST = "Load Test Hypervisor";

    private static final String CONNECTION_LOAD_TEST = "loadtest";

    private static final String APPLICATION_TYPE_LOAD_TEST = "VEMA";

    private static final String PREFIX_LOADTEST_MGMT_SERVER = "LOADTEST:";
    private static final String PREFIX_LOADTEST_HYPERVISOR = "LOAD:";

    @Override
    public ConnectionConfiguration createConfiguration() {
        ConnectionConfiguration newConfiguration = new LoadTestConfiguration();
        newConfiguration.getCommon().setApplicationType(getApplicationType());
        return newConfiguration;
    }

    @Override
    public Class getImplementingClass() {
        return LoadTestConfiguration.class;
    }

    @Override
    public String getHypervisorDisplayName() {
        return HYPERVISOR_LOAD_TEST;
    }

    @Override
    public String getManagementServerDisplayName() {
        return MGMT_SERVER_LOAD_TEST;
    }

    @Override
    public String getConnectorName() {
        return CONNECTION_LOAD_TEST;
    }

    @Override
    public String getApplicationType() {
        return APPLICATION_TYPE_LOAD_TEST;
    }

    @Override
    public String getPrefix(PrefixType prefixType) {
        switch (prefixType) {
            case ManagementServer:
                return PREFIX_LOADTEST_MGMT_SERVER;
            case Hypervisor:
                return PREFIX_LOADTEST_HYPERVISOR;
            default:
                return "";
        }
    }

    protected void initPrefixMap(Map<String,InventoryType> prefixMap) {
        prefixMap.put(LoadTestConfigurationProvider.PREFIX_LOADTEST_HYPERVISOR, InventoryType.Hypervisor);
        prefixMap.put(LoadTestConfigurationProvider.PREFIX_LOADTEST_MGMT_SERVER, InventoryType.Hypervisor);
    }

    @Override
    public void migrateConfiguration(ConnectionConfiguration configuration) {
        // base configuration provider migrations
        super.migrateConfiguration(configuration);
        // default application configuration
        if (configuration.getCommon().getApplicationType() == null) {
            configuration.getCommon().setApplicationType(getApplicationType());
        }
    }

    @Override
    public boolean isSynchronizeServicesEnabled() {
        return serviceSyncEnabled;
    }

}
