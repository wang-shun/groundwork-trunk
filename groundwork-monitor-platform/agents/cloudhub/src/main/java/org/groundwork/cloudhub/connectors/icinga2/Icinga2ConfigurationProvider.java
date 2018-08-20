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

package org.groundwork.cloudhub.connectors.icinga2;

import org.groundwork.agents.utils.SharedSecretProtector;
import org.groundwork.cloudhub.configuration.BaseConfigurationProvider;
import org.groundwork.cloudhub.configuration.ConfigurationProvider;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.configuration.Icinga2Configuration;
import org.groundwork.cloudhub.configuration.Icinga2Connection;
import org.groundwork.cloudhub.exceptions.CloudHubException;
import org.groundwork.cloudhub.inventory.InventoryType;
import org.groundwork.cloudhub.monitor.CloudhubMonitorAgentMonitorClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Icinga2ConfigurationProvider
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@Service(Icinga2ConfigurationProvider.NAME)
public class Icinga2ConfigurationProvider extends BaseConfigurationProvider implements ConfigurationProvider {

    public static final String NAME = "Icinga2ConfigurationProvider";

    @Value("${synchronizer.services.icinga.enabled:false}")
    protected Boolean serviceSyncEnabled = false;

    private static final String MONITOR_SERVER_ICINGA2 = "Icinga2 Monitor Server";

    private static final String CONNECTION_ICINGA2 = "icinga2";

    private static final String APPLICATION_TYPE_ICINGA2 = "ICINGA2";

    @Override
    public ConnectionConfiguration createConfiguration() {
        return new Icinga2Configuration();
    }

    @Override
    public Class getImplementingClass() {
        return Icinga2Configuration.class;
    }

    @Override
    public String getHypervisorDisplayName() {
        return null;
    }

    @Override
    public String getManagementServerDisplayName() {
        return MONITOR_SERVER_ICINGA2;
    }

    @Override
    public String getConnectorName() {
        return CONNECTION_ICINGA2;
    }

    @Override
    public String getApplicationType() {
        return APPLICATION_TYPE_ICINGA2;
    }

    @Override
    public String getCloudhubMonitorAgentBeanName() {
        return CloudhubMonitorAgentMonitorClient.NAME;
    }

    @Override
    public String encryptPassword(ConnectionConfiguration configuration) throws CloudHubException {
        if (configuration.getConnection() instanceof Icinga2Connection) {
            try {
                Icinga2Connection connection = ((Icinga2Connection) configuration.getConnection());
                String rawPassword = connection.getPassword();
                connection.setPassword(SharedSecretProtector.encrypt(rawPassword));
                return connection.getPassword();
            } catch (Exception e) {
                throw new CloudHubException("Could not encrypt password", e);
            }
        }
        return null;
    }

    @Override
    public String decryptPassword(ConnectionConfiguration configuration) throws CloudHubException {
        if (configuration.getConnection() instanceof Icinga2Connection) {
            try {
                Icinga2Connection connection = ((Icinga2Connection) configuration.getConnection());
                String encrypted = connection.getPassword();
                connection.setPassword(SharedSecretProtector.decrypt(encrypted));
                return connection.getPassword();
            } catch (Exception e) {
                throw new CloudHubException("Could not decrypt password", e);
            }
        }
        return null;
    }

    @Override
    public String getPrefix(PrefixType prefixType) {
        return "";
    }

    protected void initPrefixMap(Map<String, InventoryType> prefixMap) {
    }

    @Override
    public boolean isSynchronizeServicesEnabled() {
        return serviceSyncEnabled;
    }

    @Override
    public boolean supports(SupportsFeature feature) {
        switch (feature) {
            case Profiles:
                return false;
        }
        return super.supports(feature);
    }


}