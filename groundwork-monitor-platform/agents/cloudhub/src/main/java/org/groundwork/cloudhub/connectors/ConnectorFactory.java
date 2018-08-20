package org.groundwork.cloudhub.connectors;

import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.configuration.ConfigurationProvider;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.connectors.amazon.AmazonConfigurationProvider;
import org.groundwork.cloudhub.connectors.amazon.AmazonConnector;
import org.groundwork.cloudhub.connectors.azure.AzureConfigurationProvider;
import org.groundwork.cloudhub.connectors.azure.AzureConnector;
import org.groundwork.cloudhub.connectors.cisco.CiscoConfigurationProvider;
import org.groundwork.cloudhub.connectors.cisco.CiscoConnector;
import org.groundwork.cloudhub.connectors.cloudera.ClouderaConfigurationProvider;
import org.groundwork.cloudhub.connectors.cloudera.ClouderaConnector;
import org.groundwork.cloudhub.connectors.docker.DockerConfigurationProvider;
import org.groundwork.cloudhub.connectors.docker.DockerConnector;
import org.groundwork.cloudhub.connectors.icinga2.Icinga2ConfigurationProvider;
import org.groundwork.cloudhub.connectors.icinga2.Icinga2Connector;
import org.groundwork.cloudhub.connectors.loadtest.LoadTestConfigurationProvider;
import org.groundwork.cloudhub.connectors.loadtest.LoadTestConnector;
import org.groundwork.cloudhub.connectors.nedi.NediConfigurationProvider;
import org.groundwork.cloudhub.connectors.nedi.NediConnector;
import org.groundwork.cloudhub.connectors.netapp.NetAppConfigurationProvider;
import org.groundwork.cloudhub.connectors.netapp.NetAppConnector;
import org.groundwork.cloudhub.connectors.nsx.NSXConfigurationProvider;
import org.groundwork.cloudhub.connectors.nsx.NSXConnector;
import org.groundwork.cloudhub.connectors.opendaylight.OpenDaylightConfigurationProvider;
import org.groundwork.cloudhub.connectors.opendaylight.OpenDaylightConnector;
import org.groundwork.cloudhub.connectors.openshift.OpenShiftConfigurationProvider;
import org.groundwork.cloudhub.connectors.openshift.OpenShiftConnector;
import org.groundwork.cloudhub.connectors.openstack.OpenStackConfigurationProvider;
import org.groundwork.cloudhub.connectors.openstack.OpenStackConnector;
import org.groundwork.cloudhub.connectors.rhev.RhevConfigurationProvider;
import org.groundwork.cloudhub.connectors.rhev.RhevConnector;
import org.groundwork.cloudhub.connectors.vmware.VMwareConfigurationProvider;
import org.groundwork.cloudhub.connectors.vmware.VMwareConnector;
import org.groundwork.cloudhub.inventory.InventoryType;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service(ConnectorFactory.NAME)
public class ConnectorFactory implements BeanFactoryAware {

    public static final String NAME = "ConnectorFactory";

    @Value("${vmware.connector.impl}")
    private String vmwareConnectorImpl;

    private BeanFactory beanFactory;
    private static final Map<String,MonitoringConnector> connectors = new ConcurrentHashMap<>();
    private static final Map<String,MonitorConnector> monitorConnectors = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
        vmwareConnectorImpl = (vmwareConnectorImpl == null) ? VMwareConnector.NAME : vmwareConnectorImpl;
    }

    public MonitoringConnector getMonitoringConnector(ConnectionConfiguration config) {
        return getMonitoringConnector(config, null);
    }

    public MonitoringConnector getMonitoringConnector(ConnectionConfiguration config, String overrideBean) {
        return getMonitoringConnector(config.getCommon().getAgentId(), config.getCommon().getVirtualSystem(), overrideBean);
    }

    public MonitoringConnector getMonitoringConnector(String agentId, VirtualSystem virtualSystem) {
        return getMonitoringConnector(agentId, virtualSystem, null);
    }

    public MonitoringConnector getMonitoringConnector(String agentId, VirtualSystem virtualSystem, String overrideBean) {
        if (overrideBean != null) {
            return (MonitoringConnector)beanFactory.getBean(overrideBean);
        }
        MonitoringConnector connector = connectors.get(agentId);
        if (connector != null) {
            return connector;
        }
        switch (virtualSystem) {
            case REDHAT:
                connector = (MonitoringConnector)beanFactory.getBean(RhevConnector.NAME);
                break;
            case VMWARE:
                connector = (MonitoringConnector)beanFactory.getBean(vmwareConnectorImpl);
                break;
            case OPENSTACK:
                connector = (MonitoringConnector) beanFactory.getBean(OpenStackConnector.NAME);
                break;
            case OPENSHIFT:
                connector = (MonitoringConnector) beanFactory.getBean(OpenShiftConnector.NAME);
                break;
            case DOCKER:
                connector = (MonitoringConnector) beanFactory.getBean(DockerConnector.NAME);
                break;
            case OPENDAYLIGHT:
                connector = (MonitoringConnector) beanFactory.getBean(OpenDaylightConnector.NAME);
                break;
            case NSX:
                connector = (MonitoringConnector) beanFactory.getBean(NSXConnector.NAME);
                break;
            case CISCO:
                connector = (MonitoringConnector) beanFactory.getBean(CiscoConnector.NAME);
                break;
            case AMAZON:
                connector = (MonitoringConnector) beanFactory.getBean(AmazonConnector.NAME);
                break;
            case LOADTEST:
                connector = (MonitoringConnector) beanFactory.getBean(LoadTestConnector.NAME);
                break;
            case NETAPP:
                connector = (MonitoringConnector) beanFactory.getBean(NetAppConnector.NAME);
                break;
            case CLOUDERA:
                connector = (MonitoringConnector) beanFactory.getBean(ClouderaConnector.NAME);
                break;
            case AZURE:
                connector = (MonitoringConnector) beanFactory.getBean(AzureConnector.NAME);
                break;
            case NEDI:
                connector = (MonitoringConnector) beanFactory.getBean(NediConnector.NAME);
                break;
            default:
                break;
        }
        if (connector != null) {
            connectors.put(agentId, connector);
        }
        return connector;
    }

    public ManagementConnector getManagementConnector(ConnectionConfiguration config) {
        return getManagementConnector(config, null);
    }

    public ManagementConnector getManagementConnector(ConnectionConfiguration config, String overrideBean) {
        if (overrideBean != null) {
            return (ManagementConnector)beanFactory.getBean(overrideBean);
        }
        ManagementConnector connector = (ManagementConnector)connectors.get(config.getCommon().getAgentId());
        if (connector != null) {
            return connector;
        }
        switch (config.getCommon().getVirtualSystem()) {
            case REDHAT:
                connector = (ManagementConnector)beanFactory.getBean(RhevConnector.NAME);
                break;
            case VMWARE:
                connector = (ManagementConnector)beanFactory.getBean(vmwareConnectorImpl);
                break;
            case OPENSTACK:
                connector = (ManagementConnector) beanFactory.getBean(OpenStackConnector.NAME);
                break;
            case OPENSHIFT:
                connector = (ManagementConnector) beanFactory.getBean(OpenShiftConnector.NAME);
                break;
            case DOCKER:
                connector = (ManagementConnector) beanFactory.getBean(DockerConnector.NAME);
                break;
            case OPENDAYLIGHT:
                connector = (ManagementConnector) beanFactory.getBean(OpenDaylightConnector.NAME);
                break;
            case NSX:
                connector = (ManagementConnector) beanFactory.getBean(NSXConnector.NAME);
                break;
            case CISCO:
                connector = (ManagementConnector) beanFactory.getBean(CiscoConnector.NAME);
                break;
            case AMAZON:
                connector = (ManagementConnector) beanFactory.getBean(AmazonConnector.NAME);
                break;
            case LOADTEST:
                connector = (ManagementConnector) beanFactory.getBean(LoadTestConnector.NAME);
                break;
            case NETAPP:
                connector = (ManagementConnector) beanFactory.getBean(NetAppConnector.NAME);
                break;
            case CLOUDERA:
                connector = (ManagementConnector) beanFactory.getBean(ClouderaConnector.NAME);
                break;
            case AZURE:
                connector = (ManagementConnector) beanFactory.getBean(AzureConnector.NAME);
                break;
            case NEDI:
                connector = (ManagementConnector) beanFactory.getBean(NediConnector.NAME);
                break;
            default:
                break;
        }
        if (connector != null) {
             connectors.put(config.getCommon().getAgentId(), (MonitoringConnector)connector);
        }
        return connector;
    }

    public MonitorConnector getMonitorConnector(ConnectionConfiguration config) {
        MonitorConnector connector = monitorConnectors.get(config.getCommon().getAgentId());
        if (connector != null) {
            return connector;
        }
        switch (config.getCommon().getVirtualSystem()) {
            case ICINGA2:
                connector = (MonitorConnector) beanFactory.getBean(Icinga2Connector.NAME);
                break;
            default:
                break;
        }
        if (connector != null) {
            monitorConnectors.put(config.getCommon().getAgentId(), connector);
        }
        return connector;
    }

    public ConfigurationProvider getConfigurationProvider(VirtualSystem virtualSystem) {
        ConfigurationProvider provider = null;
        switch (virtualSystem) {
            case REDHAT:
                provider = (ConfigurationProvider)beanFactory.getBean(RhevConfigurationProvider.NAME);
                break;
            case VMWARE:
                provider = (ConfigurationProvider)beanFactory.getBean(VMwareConfigurationProvider.NAME);
                break;
            case OPENSTACK:
                provider = (ConfigurationProvider) beanFactory.getBean(OpenStackConfigurationProvider.NAME);
                break;
            case OPENSHIFT:
                provider = (ConfigurationProvider) beanFactory.getBean(OpenShiftConfigurationProvider.NAME);
                break;
            case DOCKER:
                provider = (ConfigurationProvider) beanFactory.getBean(DockerConfigurationProvider.NAME);
                break;
            case OPENDAYLIGHT:
                provider = (ConfigurationProvider) beanFactory.getBean(OpenDaylightConfigurationProvider.NAME);
                break;
            case NSX:
                provider = (ConfigurationProvider) beanFactory.getBean(NSXConfigurationProvider.NAME);
                break;
            case CISCO:
                provider = (ConfigurationProvider) beanFactory.getBean(CiscoConfigurationProvider.NAME);
                break;
            case AMAZON:
                provider = (ConfigurationProvider) beanFactory.getBean(AmazonConfigurationProvider.NAME);
                break;
            case LOADTEST:
                provider = (ConfigurationProvider) beanFactory.getBean(LoadTestConfigurationProvider.NAME);
                break;
            case NETAPP:
                provider = (ConfigurationProvider) beanFactory.getBean(NetAppConfigurationProvider.NAME);
                break;
            case CLOUDERA:
                provider = (ConfigurationProvider) beanFactory.getBean(ClouderaConfigurationProvider.NAME);
                break;
            case AZURE:
                provider = (ConfigurationProvider) beanFactory.getBean(AzureConfigurationProvider.NAME);
                break;
            case NEDI:
                provider = (ConfigurationProvider) beanFactory.getBean(NediConfigurationProvider.NAME);
                break;
            case ICINGA2:
                provider = (ConfigurationProvider) beanFactory.getBean(Icinga2ConfigurationProvider.NAME);
                break;
            default:
                break;
        }
        return provider;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    public static final boolean isSpecialHostGroup(String name) {
        for (String prefix : ConnectorConstants.SPECIAL_PREFIXES) {
            if (name.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    public static final boolean isSpecialHost(String name) {
        for (String prefix : ConnectorConstants.VM_PREFIXES) {
            if (name.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    public boolean isHypervisor(String name) {
        for (String prefix : ConnectorConstants.VM_PREFIXES) {
            if (name.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Given a virtual system, return the management server prefix associated with the virtual system
     *
     * @param virtualSystem
     * @return the management server prefix for the given virtual system
     */
    public String mapToManagementServerPrefix(VirtualSystem virtualSystem) {
        ConfigurationProvider provider = getConfigurationProvider(virtualSystem);
        if (provider != null) {
            return provider.getPrefix(ConfigurationProvider.PrefixType.ManagementServer);
        }
        return "";
    }

    public String makePrefix(VirtualSystem virtualSystem, InventoryType inventoryType) {
        String prefix = "";
        ConfigurationProvider provider = getConfigurationProvider(virtualSystem);
        if (provider != null) {
            switch (inventoryType) {
                case Hypervisor:
                case Host:
                    return provider.getPrefix(ConfigurationProvider.PrefixType.Hypervisor);
                case Datastore:
                    return provider.getPrefix(ConfigurationProvider.PrefixType.Storage);
                case Network:
                    return provider.getPrefix(ConfigurationProvider.PrefixType.Network);
                case ResourcePool:
                    return provider.getPrefix(ConfigurationProvider.PrefixType.ResourcePool);
            }
        }
        return prefix;
    }

}
