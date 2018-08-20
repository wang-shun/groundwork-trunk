package org.groundwork.cloudhub.connectors.amazon;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.rds.model.DBSecurityGroup;
import com.amazonaws.services.rds.model.DescribeDBSecurityGroupsResult;
import org.groundwork.cloudhub.connectors.CollectionMode;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.groundwork.cloudhub.inventory.DataCenterInventory;
import org.groundwork.cloudhub.inventory.InventoryBrowser;
import org.groundwork.cloudhub.inventory.InventoryOptions;
import org.groundwork.cloudhub.metrics.BaseQuery;
import org.groundwork.cloudhub.metrics.BaseVM;
import org.groundwork.cloudhub.metrics.MonitoringState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AWSInventory  implements InventoryBrowser {

    @SuppressWarnings("rawtypes")
    private HashMap<Class, HashMap<String, AWSItem>> inventoryTypeMap = new HashMap<Class, HashMap<String, AWSItem>>();
    private AWSConnection connection;
    private String accountNumber;
    private CollectionMode collectionMode;

    public AWSInventory(AWSConnection connection, CollectionMode mode, boolean collectExtra) {
        try {
            this.connection = connection;
            this.collectionMode = mode;
            AWSItemEC2.gatherInventory(this, connection);

            if (mode.isDoNetworks()) {
                AWSItemELB.gatherInventory(this, connection);
            }

            if (mode.isDoStorageDomains()) {
                AWSItemRDS.gatherInventory(this, connection);
            }

            if (mode.isDoHosts()) {
                AWSItemAZ.gatherInventory(this, connection);
            }

            // CLOUDHUB-296: 7.1.1: Only gather EBS inventory to be merged with EC2 metrics (7.1.1)
            // but never add EBS as a host or hostGroup (in 7.1.0, EBS was both host + hostGroup)
            if (collectExtra && mode.isDoStorageDomains()) {
                AWSItemEBS.gatherInventory(this, connection);
            }

            if (mode.isDoNetworks()) {
                AWSItemVPC.gatherInventory(this, connection);
            }
            if (mode.isDoResourcePools()) {
                AWSItemPG.gatherInventory(this, connection);
                AWSItemRG.gatherInventory(this, connection);
            }
            for (HashMap<String, AWSItem> inventoryTypeMapEntry : inventoryTypeMap.values()) {
                for (Map.Entry<String, AWSItem> itemEntry : inventoryTypeMapEntry.entrySet()) {
                    itemEntry.getValue().resolveRelationState(this);
                }
            }
            initializeAccountInfo();

        } catch (AmazonClientException x) {
            throw new ConnectorException(x.getMessage(), x);
        }
    }
    
    public void put(String name, AWSItem item) {
        HashMap<String, AWSItem> itemMap = inventoryTypeMap.get(item.getClass());
        if (itemMap == null) {
            itemMap = new HashMap<String, AWSItem>();
            inventoryTypeMap.put(item.getClass(), itemMap);
        }
        itemMap.put(name, item);
    }
    
    public <T> AWSItem get(String name, T classType) {
        HashMap<String, AWSItem> itemMap = inventoryTypeMap.get(classType);
        if (itemMap == null) {
            return null;
        }
        return itemMap.get(name);
    }
    
    @Override
    public DataCenterInventory gatherInventory(InventoryOptions options) {
    
        DataCenterInventory dcInventory = new DataCenterInventory(options);
        
        try {

            for (HashMap<String, AWSItem> inventoryTypeMapEntry : inventoryTypeMap.values()) {
                for (Map.Entry<String, AWSItem> itemEntry : inventoryTypeMapEntry.entrySet()) {
                    itemEntry.getValue().populateHostGroupDataCenterInventory(dcInventory, options, this);
                }
            }
            
            for (HashMap<String, AWSItem> inventoryTypeMapEntry : inventoryTypeMap.values()) {
                for (Map.Entry<String, AWSItem> itemEntry : inventoryTypeMapEntry.entrySet()) {
                    itemEntry.getValue().populateHostDataCenterInventory(dcInventory, options, this);
                }
            }
        } catch (AmazonClientException x) {
            throw new ConnectorException(x.getMessage(), x);
        }
        
        return dcInventory;
    }
    
    public void collectMetrics(MonitoringState monitoringState, MonitoringState priorState,
            List<BaseQuery> awsQueries, List<BaseQuery> gwQueries, List<BaseQuery> customQueries, AWSConnection connection) {
        
        HashMap<String, BaseVM> priorVMMap = new HashMap<String, BaseVM>();

        gwQueries.clear(); // remove all info.*, not supported in 7.2.0 or any prior releases (r28757)
        
        try {
            for (Map.Entry<Class, HashMap<String, AWSItem>> entry : inventoryTypeMap.entrySet()) {
                Class key = entry.getKey();
                if (key.getName().equals(AWSItemEBS.class.getName())) {
                    continue;
                }
                Map<String, AWSItem> inventoryTypeMapEntry = entry.getValue();
                for (Map.Entry<String, AWSItem> itemEntry : inventoryTypeMapEntry.entrySet()) {
                    itemEntry.getValue().collectHostGroupMetrics(monitoringState, priorState,
                            awsQueries, gwQueries, this, connection, priorVMMap);
                }
            }
        
            for (HashMap<String, AWSItem> inventoryTypeMapEntry : inventoryTypeMap.values()) {
                for (Map.Entry<String, AWSItem> itemEntry : inventoryTypeMapEntry.entrySet()) {
                    itemEntry.getValue().collectHostMetrics(monitoringState, priorState,
                            awsQueries, gwQueries, customQueries, this, connection, priorVMMap);
                }
            }
        } catch (AmazonClientException x) {
            throw new ConnectorException(x.getMessage(), x);
        }
    }

    public AWSConnection getConnection() {
        return connection;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    private void initializeAccountInfo() {
        DescribeDBSecurityGroupsResult dbResult = connection.getRDSClient().describeDBSecurityGroups();
        List<DBSecurityGroup> dbSecurityGroups = dbResult.getDBSecurityGroups();
        if (dbSecurityGroups != null && dbSecurityGroups.size() > 0) {
            accountNumber = dbSecurityGroups.get(0).getOwnerId();
            return;
        }

        DescribeSecurityGroupsResult result = connection.getEC2Client().describeSecurityGroups();
        List<SecurityGroup> securityGroups = result.getSecurityGroups();
        if (securityGroups != null && securityGroups.size() > 0) {
            accountNumber = securityGroups.get(0).getOwnerId();
            return;
        }

    }

    public CollectionMode getCollectionMode() {
        return collectionMode;
    }

    public void setCollectionMode(CollectionMode collectionMode) {
        this.collectionMode = collectionMode;
    }
}
