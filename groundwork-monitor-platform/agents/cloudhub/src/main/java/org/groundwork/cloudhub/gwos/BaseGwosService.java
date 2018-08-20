package org.groundwork.cloudhub.gwos;

import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.configuration.ConfigurationProvider;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.connectors.ConnectorConstants;
import org.groundwork.cloudhub.connectors.ConnectorFactory;
import org.groundwork.cloudhub.metrics.BaseHost;
import org.groundwork.cloudhub.metrics.BaseVM;
import org.groundwork.cloudhub.monitor.CloudhubAgentInfo;

import javax.annotation.Resource;

public abstract class BaseGwosService extends BaseRestGwosService {

    private static Logger log = Logger.getLogger(BaseGwosService.class);

    public static final String RS_LEGACY_ENDPOINT_BASE_DEFAULT = "/foundation-webapp/restwebservices";

    protected static String JSONFORMAT_PARAM = "dataInJSONFormat";

    protected ConnectionConfiguration connection;
    protected CloudhubAgentInfo agentInfo;

    @Resource(name = ConnectorFactory.NAME)
    protected ConnectorFactory connectorFactory;

    public BaseGwosService() {
    }

    public BaseGwosService(ConnectionConfiguration configuration, CloudhubAgentInfo agentInfo) {
        this.connection = configuration;
        this.agentInfo = agentInfo;
    }

    public String buildHostGroupName(CloudhubAgentInfo agentInfo, String entityScope, String hostGroupName) {

        ConfigurationProvider provider = connectorFactory.getConfigurationProvider(agentInfo.getVirtualSystem());
        if (provider == null) {
            log.error("Unknown virtual system for managementServerType: '" + agentInfo.getManagementServerName() + "'");
            return hostGroupName;
        }
        if (provider.isSimpleHostGroupName(hostGroupName)) {
            return hostGroupName;
        }
        // strip off port
        int pos = hostGroupName.indexOf(":");
        hostGroupName = (pos > -1) ? hostGroupName.substring(0, pos) : hostGroupName;

        String finalHostGroupName = null;
        if (isExternalPrefix(hostGroupName, agentInfo.getVirtualSystem(), provider)) {
            return hostGroupName.replaceFirst("-", ":");
        }
        if (entityScope.compareTo(ConnectorConstants.ENTITY_MGMT_SERVER) == 0) {
            finalHostGroupName = provider.getPrefix(ConfigurationProvider.PrefixType.ManagementServer) + hostGroupName;
        } else if (entityScope.compareTo(ConnectorConstants.ENTITY_HYPERVISOR) == 0) {
            finalHostGroupName = provider.getPrefix(ConfigurationProvider.PrefixType.Hypervisor) + hostGroupName;
        } else {
            log.error("ALERT: entityScope = '" + entityScope + "'");
            finalHostGroupName = hostGroupName;
        }
        return finalHostGroupName;
    }

    /**
     * for prior to 7.1.0
     *
     * @param virtualSystem
     * @param hostName
     * @param action
     * @param description
     * @param username
     */
    public void auditLogHost(VirtualSystem virtualSystem, String hostName, String action, String description, String username) {
        log.info(String.format("AuditLog host: %s, action: %s, description: %s, user: %s",
                (hostName == null) ? "(unknown)" : hostName, action, description, username));
    }

    /**
     * for prior to 7.1.0
     *
     * @param virtualSystem
     * @param hostName
     * @param action
     * @param description
     * @param username
     * @param service
     */
    public void auditLogService(VirtualSystem virtualSystem, String hostName, String action, String description, String username, String service) {
        log.info(String.format("AuditLog service: %s, host: %s, action: %s, description: %s, user: %s",
                service, (hostName == null) ? "(unknown)" : hostName, action, description, username));
    }

    protected boolean isExternalPrefix(String hostGroupName, VirtualSystem virtualSystem, ConfigurationProvider provider) {
        if (ConnectorFactory.isSpecialHost(hostGroupName)) {
            return true;
        }
        if (virtualSystem.equals(VirtualSystem.VMWARE) || virtualSystem.equals(VirtualSystem.REDHAT)
                || virtualSystem.equals(VirtualSystem.OPENSTACK)) {
            if (hostGroupName.startsWith(provider.getPrefix(ConfigurationProvider.PrefixType.Storage)) ||
                    hostGroupName.startsWith(provider.getPrefix(ConfigurationProvider.PrefixType.Network)) ||
                    hostGroupName.startsWith(provider.getPrefix(ConfigurationProvider.PrefixType.ResourcePool))) {
                return true;
            }
        }
        return false;
    }

    protected String formatHostPluginOutput(BaseHost host) {
        return String.format("Status %s %s / %s",
                (host.getRunState() == null) ? "" : host.getRunState(),
                (host.getLastUpdate() == null) ? "" : host.getLastUpdate(),
                (host.getRunExtra() == null) ? "" : host.getRunExtra());
    }

    protected String formatVMPluginOutput(BaseVM vm) {
        return String.format("Status %s %s / %s",
                (vm.getRunState() == null) ? "" : vm.getRunState(),
                (vm.getLastUpdate() == null) ? "" : vm.getLastUpdate(),
                (vm.getRunExtra() == null) ? "" : vm.getRunExtra());
    }

    protected String getDeviceIdentificationFromVm(BaseVM vm) {
        String deviceIdentification = ((vm.getGwosHostName() != null) ? vm.getGwosHostName() : vm.getVMName());
        if (vm.getIpAddress() != null && vm.getIpAddress().length() > 0) {
            deviceIdentification = vm.getIpAddress();
        } else if (vm.getMacAddress() != null && vm.getMacAddress().length() > 0) {
            deviceIdentification = vm.getMacAddress();
        }
        return deviceIdentification;
    }

    protected String getDeviceIdentificationFromHost(BaseHost host) {
        String deviceIdentification = ((host.getGwosHostName() != null) ? host.getGwosHostName() : host.getHostName());
        if (host.getIpAddress() != null && host.getIpAddress().length() > 0) {
            deviceIdentification = host.getIpAddress();
        } else if (host.getMacAddress() != null && host.getMacAddress().length() > 0) {
            deviceIdentification = host.getMacAddress();
        }
        return deviceIdentification;
    }

    protected String stripVMPrefix(String name) {
        for (String prefix : ConnectorConstants.VM_PREFIXES) {
            if (name.startsWith(prefix)) {
                return name.substring(prefix.length());
            }
        }
        return name;
    }

    protected String stripHostGroupPrefix(String name) {
        int pos = name.indexOf(':');
        if (pos > 0) {
            return name.substring(pos + 1);
        }
        return name;

    }

    public boolean isHostNameBlackListed(String name) {
        return false;
    }

    public boolean isFeatureEnabled(GwosService.GroundworkFeature feature) {
        return false;
    }

    public ConnectionConfiguration getConnection() {
        return connection;
    }

    public void setConnection(ConnectionConfiguration connection) {
        this.connection = connection;
    }

    public CloudhubAgentInfo getAgentInfo() {
        return agentInfo;
    }

    public void setAgentInfo(CloudhubAgentInfo agentInfo) {
        this.agentInfo = agentInfo;
    }

}

