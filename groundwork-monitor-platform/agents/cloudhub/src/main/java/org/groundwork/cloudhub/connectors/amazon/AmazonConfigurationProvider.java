package org.groundwork.cloudhub.connectors.amazon;

import org.groundwork.agents.monitor.DeleteServiceInfo;
import org.groundwork.cloudhub.configuration.AmazonConfiguration;
import org.groundwork.cloudhub.configuration.BaseConfigurationProvider;
import org.groundwork.cloudhub.configuration.ConfigurationProvider;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.connectors.ConnectorConstants;
import org.groundwork.cloudhub.inventory.InventoryType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AmazonConfigurationProvider class to provide the configuration properties
 */
@Service(AmazonConfigurationProvider.NAME)
public class AmazonConfigurationProvider extends BaseConfigurationProvider implements ConfigurationProvider{

    @Value("${synchronizer.services.amazon.enabled:false}")
    protected Boolean serviceSyncEnabled = false;

    // Display Names
    public static String MGMT_SERVER_AMAZON            = "AMAZON AWS Endpoint";
    public static String HYPERVISOR_AMAZON             = "AMAZON AWS Availability Zone";
    
    // Connector Constant
    public static String CONNECTOR_AMAZON              = "amazon";

    // Application Type
    public static String APPLICATIONTYPE_AMAZON        = "AMAZON";

    public static final String EC2 = "hypervisor";
    public static final String STORAGE = "storage";
    public static final String NETWORK = "network";
    public static final String CUSTOM = "custom";

    // Prefixes
    public static String PREFIX_AMAZON_MGMT_SERVER      = "AWS-M:";
    public static String PREFIX_AMAZON_HYPERVISOR       = "AWS-AZ:";   // Availability Zone
    public static String PREFIX_AMAZON_STORAGE          = "AWS-RDS:";
    public static String PREFIX_AMAZON_RESOURCE_POOL    = "AWS-POOL:";   // EC2 Placement Groups and RDS replication groups
    public static String PREFIX_AMAZON_NETWORK          = "AWS-VPC:";

    public static String PREFIX_AMAZON_CLUSTER          = ConnectorConstants.PREFIX_CLUSTER;
    public static String PREFIX_AMAZON_DATACENTER       = ConnectorConstants.PREFIX_DATACENTER;
    public static String PREFIX_AMAZON_VM_NETWORK       = ConnectorConstants.PREFIX_VM_NETWORK;
    public static String PREFIX_AMAZON_VM_STORAGE       = ConnectorConstants.PREFIX_VM_STORAGE;
    
    public static String PREFIX_EC2_INSTANCE            = ""; //"EC2_";
    public static String PREFIX_RDS_INSTANCE            = ""; //"RDS_";
    public static String PREFIX_EC2_POOL                = "EC2-";
    public static String PREFIX_RDS_POOL                = "RDS-";

    public static String PREFIX_HOST_STORAGE            = "vol-";
    public static String PREFIX_HOST_NETWORK            = "vpc-";

    public static String METRIC_PREFIX_EBS              = "EBS.";
    public static String METRIC_PREFIX_EC2              = "EC2.";
    public static String METRIC_PREFIX_RDS              = "RDS.";
    public static String METRIC_PREFIX_ELB              = "ELB.";

    public static String RDS_HOST_GROUP = "storage";

    public static final String NAME = "AmazonConfigurationProvider";
    
	@Override
	public ConnectionConfiguration createConfiguration() {
		return new AmazonConfiguration();
	}

	@Override
	public Class getImplementingClass() {
		return AmazonConfiguration.class;
	}

	@Override
	public String getHypervisorDisplayName() {
		return HYPERVISOR_AMAZON;
	}

	@Override
	public String getManagementServerDisplayName() {
		return MGMT_SERVER_AMAZON;
	}

	@Override
	public String getConnectorName() {
		return CONNECTOR_AMAZON;
	}

	@Override
	public String getApplicationType() {
		return APPLICATIONTYPE_AMAZON;
	}

	@Override
	public String getPrefix(PrefixType prefixType) {
		 switch (prefixType) {
         case ManagementServer:
             return PREFIX_AMAZON_MGMT_SERVER;
         case Hypervisor:
             return PREFIX_AMAZON_HYPERVISOR;
         case Network:
             return PREFIX_AMAZON_NETWORK;
         case Cluster:
             return PREFIX_AMAZON_CLUSTER;
         case Storage:
             return PREFIX_AMAZON_STORAGE;
         case DataCenter:
             return PREFIX_AMAZON_DATACENTER;
         case ResourcePool:
             return PREFIX_AMAZON_RESOURCE_POOL;
         case VmNetwork:
             return PREFIX_AMAZON_VM_NETWORK;
         case VmStorage:
             return PREFIX_AMAZON_VM_STORAGE;
     }
     return null;
	}

    @Override
    public boolean isValidManagementServerHostGroup(String hostGroupName) {
        if (hostGroupName == null || (hostGroupName != null && hostGroupName.startsWith(PREFIX_AMAZON_HYPERVISOR))) {
            return false;
        }
        return true;
    }

    protected void initPrefixMap(Map<String,InventoryType> prefixMap) {
        prefixMap.put(AmazonConfigurationProvider.PREFIX_AMAZON_HYPERVISOR, InventoryType.Hypervisor);
        prefixMap.put(AmazonConfigurationProvider.PREFIX_AMAZON_MGMT_SERVER, InventoryType.Hypervisor);
        prefixMap.put(AmazonConfigurationProvider.PREFIX_AMAZON_NETWORK, InventoryType.Network);
        prefixMap.put(AmazonConfigurationProvider.PREFIX_AMAZON_CLUSTER, InventoryType.Cluster);
        prefixMap.put(AmazonConfigurationProvider.PREFIX_AMAZON_STORAGE, InventoryType.Datastore);
        prefixMap.put(AmazonConfigurationProvider.PREFIX_AMAZON_DATACENTER, InventoryType.DataCenter);
        prefixMap.put(AmazonConfigurationProvider.PREFIX_AMAZON_RESOURCE_POOL, InventoryType.ResourcePool);
    }

    @Override
    protected InventoryType defaultInventoryType() {
        return InventoryType.TaggedGroup;
    }

    @Override
    public boolean isLogicalView(String hostGroupName) {
        return (hostGroupName.startsWith(PREFIX_AMAZON_STORAGE));
    }

    @Override
    public List<DeleteServiceInfo> createDeleteServiceList(List<String> services) {
        List<DeleteServiceInfo> result = new ArrayList<>();
        for (String service : services) {
            if (service.startsWith(METRIC_PREFIX_EBS)) {
                result.add(new DeleteServiceInfo(service, DeleteServiceInfo.OperationType.prefixWildcard, METRIC_PREFIX_EBS));
            }
            else {
                result.add(new DeleteServiceInfo(service));
            }
        }
        return result;
    }

    @Override
    public boolean isSynchronizeServicesEnabled() {
        return serviceSyncEnabled;
    }

}
