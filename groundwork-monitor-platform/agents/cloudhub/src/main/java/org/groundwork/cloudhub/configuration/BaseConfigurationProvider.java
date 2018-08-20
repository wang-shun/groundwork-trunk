package org.groundwork.cloudhub.configuration;

import org.groundwork.agents.configuration.GWOSVersion;
import org.groundwork.agents.utils.SharedSecretProtector;
import org.groundwork.cloudhub.exceptions.CloudHubException;
import org.groundwork.agents.monitor.DeleteServiceInfo;
import org.groundwork.cloudhub.inventory.InventoryType;
import org.groundwork.cloudhub.monitor.CloudhubAgentInfo;
import org.groundwork.cloudhub.monitor.CloudhubMonitorAgentClient;
import org.groundwork.cloudhub.monitor.MonitorAgentSynchronizerService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseConfigurationProvider implements ConfigurationProvider {

    @Override
    public String encryptPassword(ConnectionConfiguration configuration) throws CloudHubException {
        if (configuration.getConnection() instanceof BaseSecureMonitorConnection) {
            try {
                BaseSecureMonitorConnection connection = ((BaseSecureMonitorConnection) configuration.getConnection());
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
        if (configuration.getConnection() instanceof BaseSecureMonitorConnection) {
            try {
                BaseSecureMonitorConnection connection = ((BaseSecureMonitorConnection) configuration.getConnection());
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
    public boolean isValidManagementServerHostGroup(String hostGroupName) {
        return true;
    }

    @Override
    public boolean isHostAlsoHostGroup(MonitorAgentSynchronizerService.SynchronizedResource resource) {
        return true;
    }

    @Override
    public boolean isLogicalView(String hostGroupName) {
        return false;
    }

    @Override
    public void migrateConfiguration(ConnectionConfiguration configuration) {
        // default version
        if (configuration.getGwos().getGwosVersion().startsWith("7.x")) {
            configuration.getGwos().setGwosVersion(GWOSConfiguration.DEFAULT_VERSION);
        }
        // default merge hosts
        if (!configuration.getGwos().isMergeHostsSet()) {
            if (GWOSVersion.determineVersion(configuration.getGwos().getGwosVersion()).equals(GWOSVersion.version_71)) {
                configuration.getGwos().setMergeHosts(GWOSConfiguration.DEFAULT_GWOS_71_MERGE_HOSTS);
            } else {
                configuration.getGwos().setMergeHosts(GWOSConfiguration.DEFAULT_GWOS_70_MERGE_HOSTS);
            }
        }
    }


    private Object lock = new Object();
    private Map<String, InventoryType> prefixMap = null;

    @Override
    public InventoryType prefixToInventoryType(String name) {
        if (prefixMap == null) {
            synchronized (lock) {
                prefixMap = new HashMap<>();
                initPrefixMap(prefixMap);
            }
        }
        if (name != null) {
            for (String prefix : prefixMap.keySet()) {
                if (name.startsWith(prefix)) {
                    return prefixMap.get(prefix);
                }
            }
        }
        return defaultInventoryType();
    }

    protected abstract void initPrefixMap(Map<String,InventoryType> prefixMap);

    protected InventoryType defaultInventoryType() {
        return InventoryType.VirtualMachine;
    }

    @Override
    public String getCloudhubMonitorAgentBeanName() {
        return CloudhubMonitorAgentClient.NAME;
    }

    @Override
    public List<DeleteServiceInfo> createDeleteServiceList(List<String> services) {
        List<DeleteServiceInfo> result = new ArrayList<>();
        for (String service : services) {
            result.add(new DeleteServiceInfo(service));
        }
        return result;
    }

    @Override
    public String ensureHypervisorView(String name) {
        return name;
    }

    @Override
    public boolean supports(SupportsFeature feature) {
        switch (feature) {
            case Profiles:
                return true;
        }
        return false;
    }

    @Override
    public boolean isPrimaryMetric(String serviceType) {
        return true;
    }

    @Override
    public boolean isSimpleHostGroupName(String hgName) {
        return false;
    }

    @Override
    public String getHostGroupDescription(CloudhubAgentInfo agentInfo, String hostName) {
        return agentInfo.getHyperVisorName();
    }

}
