package org.groundwork.cloudhub.connectors.amazon;

import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.rds.model.DescribeDBInstancesRequest;
import com.amazonaws.services.rds.model.DescribeDBInstancesResult;
import com.amazonaws.services.rds.model.ListTagsForResourceRequest;
import com.amazonaws.services.rds.model.ListTagsForResourceResult;
import com.amazonaws.services.rds.model.Tag;
import org.groundwork.cloudhub.gwos.GwosStatus;
import org.groundwork.cloudhub.inventory.DataCenterInventory;
import org.groundwork.cloudhub.inventory.InventoryContainerNode;
import org.groundwork.cloudhub.inventory.InventoryOptions;
import org.groundwork.cloudhub.inventory.VirtualMachineNode;
import org.groundwork.cloudhub.metrics.BaseHost;
import org.groundwork.cloudhub.metrics.BaseQuery;
import org.groundwork.cloudhub.metrics.BaseVM;
import org.groundwork.cloudhub.metrics.MonitoringState;
import org.groundwork.cloudhub.metrics.SourceType;

import java.util.List;
import java.util.Map;

final class AWSItemRDS extends AWSItem {

    protected final static String CLOUDWATCH_DIMENSION_RDS = "DBInstanceIdentifier";

    private DBInstance rds;

    public AWSItemRDS(DBInstance rds) {
        this.rds = rds;
    }

    @Override
    public String getDisplayName() {
        return AmazonConfigurationProvider.PREFIX_RDS_INSTANCE + rds.getDBInstanceIdentifier();
    }

    @Override
    public void resolveRelationState(AWSInventory inventory) {
        AWSItem azItem = inventory.get(rds.getAvailabilityZone(), AWSItemAZ.class);
        if (azItem != null) {
            azItem.markInUse();
        }
        String secondaryAZ = rds.getSecondaryAvailabilityZone();
        if (secondaryAZ != null && !secondaryAZ.isEmpty()) {
            azItem = inventory.get(secondaryAZ, AWSItemAZ.class);
            if (azItem != null) {
                azItem.markInUse();
            }
        }
    }

    @Override
    public void populateHostGroupDataCenterInventory(DataCenterInventory inventory,
                                                     InventoryOptions options, AWSInventory awsInventory) {

    }

    @Override
    public void populateHostDataCenterInventory(DataCenterInventory inventory,
                                                InventoryOptions options, AWSInventory awsInventory) {

        VirtualMachineNode vmNode = new VirtualMachineNode(getDisplayName(), rds.getDBInstanceIdentifier());
        vmNode.setStatus(getStatus());
        inventory.getVirtualMachines().put(vmNode.getName(), vmNode);

        if (options.isViewDatastores()) {
            InventoryContainerNode storageNode = inventory.getDatastores().get(AmazonConfigurationProvider.RDS_HOST_GROUP);
            if (storageNode == null) {
                storageNode = new InventoryContainerNode(AmazonConfigurationProvider.RDS_HOST_GROUP);
                inventory.getDatastores().put(AmazonConfigurationProvider.RDS_HOST_GROUP, storageNode);
            }
            if (storageNode.getVms().get(getDisplayName()) == null) {
                storageNode.putVM(getDisplayName(), vmNode);
            }
        }

        if (options.isViewHypervisors()) {
            String azId = rds.getAvailabilityZone();
            AWSItem azItem = awsInventory.get(azId, AWSItemAZ.class);
            if (azItem != null) {
                InventoryContainerNode azNode = inventory.getHypervisors().get(azItem.getDisplayName());
                if (azNode != null) {
                    azNode.getVms().put(vmNode.getName(), vmNode);
                }
            }
        }

        if (options.isViewNetworks()) {
            if (rds.getDBSubnetGroup() != null) {
                AWSItem vpcItem = awsInventory.get(rds.getDBSubnetGroup().getVpcId(), AWSItemVPC.class);
                if (vpcItem != null) {
                    InventoryContainerNode vpcNode = inventory.getNetworks().get(vpcItem.getDisplayName());
                    if (vpcNode != null) {
                        vpcNode.getVms().put(vmNode.getName(), vmNode);
                    }
                }
            }
        }
        if (options.isViewResourcePools()) {
            String repMaster = rds.getReadReplicaSourceDBInstanceIdentifier();
            if (repMaster != null && !repMaster.isEmpty()) {
                AWSItem rgItem = awsInventory.get(repMaster, AWSItemRG.class);
                if (rgItem != null) {
                    InventoryContainerNode repNode = inventory.getResourcePools().get(rgItem.getDisplayName());
                    if (repNode != null) {
                        repNode.getVms().put(vmNode.getName(), vmNode);
                    }
                }
            }
        }

        // CLOUDHUB-296: 7.1.1  added Tagging support
        if (options.isViewTaggedGroups() && options.getGroupTag() != null) {

            String zone = rds.getAvailabilityZone();
            if (zone != null) {
                // ARNs require regions, not availability zones
                // see: https://stelligent.com/2014/05/02/list-all-the-availability-zones/
                int length = zone.length();
                char lastChar = zone.charAt(length - 1);
                if (Character.isAlphabetic(lastChar)) {
                    zone = zone.substring(0, length - 1);
                }
                String arn = String.format("arn:aws:rds:%s:%s:db:%s", zone, awsInventory.getAccountNumber(), rds.getDBInstanceIdentifier());
                ListTagsForResourceResult tagsList = awsInventory.getConnection().getRDSClient().listTagsForResource(
                        new ListTagsForResourceRequest().withResourceName(arn));
                for (Tag tag : tagsList.getTagList()) {
                    if (tag.getKey().equalsIgnoreCase(options.getGroupTag())) {
                        InventoryContainerNode taggedGroup = inventory.getTaggedGroups().get(tag.getValue());
                        if (taggedGroup == null) {
                            taggedGroup = new InventoryContainerNode(tag.getValue());
                            inventory.getTaggedGroups().put(tag.getValue(), taggedGroup);
                        }
                        taggedGroup.getVms().put(vmNode.getName(), vmNode);
                    }
                }
            }
        }

    }

    @Override
    public void collectHostMetrics(MonitoringState currentState, MonitoringState priorState,
                                   List<BaseQuery> awsQueries, List<BaseQuery> gwQueries, List<BaseQuery> customQueries,
                                   AWSInventory awsInventory, AWSConnection connection, Map<String, BaseVM> priorVMMap) {

        String displayName = getDisplayName();
        BaseVM rdsNode = new AmazonVM(displayName, AmazonVM.AmazonNodeType.RDS);
        BaseVM priorNode = priorVMMap.get(displayName);
        if (priorNode != null) {
            rdsNode.setPrevRunState(priorNode.getRunState());
        }
        rdsNode.setRunExtra(rds.getDBInstanceStatus());
        rdsNode.setRunState(getStatus());
        populateMetrics(gwQueries, rdsNode, priorNode);
        populateAWSMetrics(awsQueries, rdsNode, priorNode, connection, CLOUDWATCH_DIMENSION_RDS, rds.getDBInstanceIdentifier(), SourceType.storage);

        AWSItem azItem = awsInventory.get(rds.getAvailabilityZone(), AWSItemAZ.class);
        if (azItem != null) {
            BaseHost hostGroup = currentState.hosts().get(azItem.getDisplayName());
            if (hostGroup != null) {
                hostGroup.putVM(displayName, rdsNode);
            }
        }
        String secondaryAZ = rds.getSecondaryAvailabilityZone();
        if (secondaryAZ != null && !secondaryAZ.isEmpty()) {
            azItem = awsInventory.get(secondaryAZ, AWSItemAZ.class);
            if (azItem != null) {
                BaseHost hostGroup = currentState.hosts().get(azItem.getDisplayName());
                if (hostGroup != null) {
                    hostGroup.putVM(displayName, rdsNode);
                }
            }
        }

        if (rds.getDBSubnetGroup() != null) {
            AWSItem vpcItem = awsInventory.get(rds.getDBSubnetGroup().getVpcId(), AWSItemVPC.class);
            if (vpcItem != null) {
                BaseHost hostGroup = currentState.hosts().get(vpcItem.getDisplayName());
                if (hostGroup != null) {
                    hostGroup.putVM(displayName, rdsNode);
                }
            }
        }

        String repMaster = rds.getReadReplicaSourceDBInstanceIdentifier();
        if (repMaster != null && !repMaster.isEmpty()) {
            AWSItem rgItem = awsInventory.get(repMaster, AWSItemRG.class);
            if (rgItem != null) {
                BaseHost hostGroup = currentState.hosts().get(rgItem.getDisplayName());
                if (hostGroup != null) {
                    hostGroup.putVM(displayName, rdsNode);
                }
            }
        }
    }

    @Override
    protected String getStatus() {
        String state = rds.getDBInstanceStatus().toLowerCase();
        if ("available".equals(state)) {
            return GwosStatus.UP.status;
        } else if ("backing-up".equals(state) ||
                "modifying".equals(state) ||
                "rebooting".equals(state) ||
                "renaming".equals(state) ||
                "resetting-master-credentials".equals(state)) {
            return GwosStatus.SCHEDULED_DOWN.status;
        } else if ("deleting".equals(state)) {
            return GwosStatus.UNREACHABLE.status;
        } else if ("creating".equals(state)) {
            return GwosStatus.PENDING.status;
        } else if ("failed".equals(state) ||
                "incompatible-network".equals(state) ||
                "incompatible-option-group".equals(state) ||
                "incompatible-parameters".equals(state) ||
                "incompatible-restore".equals(state)) {
            return GwosStatus.UNSCHEDULED_DOWN.status;
        } else if ("storage-full".equals(state)) {
            return GwosStatus.WARNING.status;
        }
        // Return a warning status if we can't interpret the state.
        return GwosStatus.WARNING.status;
    }

    private void populateMetrics(List<BaseQuery> gwQueries, BaseVM rdsNode, BaseVM priorNode) {

        if (isEnableInfoRetrieval()) {
            for (BaseQuery query : gwQueries) {
                String config = query.getQuery();
                if ("info.rds.allocatedstorage".equals(config)) {
                    addMetric(rdsNode, priorNode, query, rds.getAllocatedStorage() + " Gb", SourceType.storage);
                } else if ("info.rds.autominorupgrade".equals(config)) {
                    Boolean upgrade = rds.getAutoMinorVersionUpgrade();
                    if (upgrade != null) {
                        addMetric(rdsNode, priorNode, query, upgrade.toString(), SourceType.storage);
                    }
                } else if ("info.rds.backupretention".equals(config)) {
                    Integer backupRetention = rds.getBackupRetentionPeriod();
                    if (backupRetention != null) {
                        addMetric(rdsNode, priorNode, query, backupRetention.toString(), SourceType.storage);
                    }
                } else if ("info.rds.charset".equals(config)) {
                    String charset = rds.getCharacterSetName();
                    if (charset != null) {
                        addMetric(rdsNode, priorNode, query, charset, SourceType.storage);
                    }
                } else if ("info.rds.rdsclass".equals(config)) {
                    addMetric(rdsNode, priorNode, query, rds.getDBInstanceClass(), SourceType.storage);
                } else if ("info.rds.dbname".equals(config)) {
                    String dbName = rds.getDBName();
                    if (dbName != null) {
                        addMetric(rdsNode, priorNode, query, dbName, SourceType.storage);
                    }
                } else if ("info.rds.endpoint".equals(config)) {
                    String endpoint = rds.getEndpoint().getAddress();
                    if (endpoint != null) {
                        Integer port = rds.getEndpoint().getPort();
                        if (port != null) {
                            endpoint += ":" + port.toString();
                        }
                        addMetric(rdsNode, priorNode, query, endpoint, SourceType.storage);
                    }
                } else if ("info.rds.engine".equals(config)) {
                    addMetric(rdsNode, priorNode, query, rds.getEngine(), SourceType.storage);
                } else if ("info.rds.engineversion".equals(config)) {
                    addMetric(rdsNode, priorNode, query, rds.getEngineVersion(), SourceType.storage);
                } else if ("info.rds.createtime".equals(config)) {
                    addMetric(rdsNode, priorNode, query, dateFormatter.format(rds.getInstanceCreateTime()), SourceType.storage);
                } else if ("info.rds.iops".equals(config)) {
                    Integer iops = rds.getIops();
                    if (iops != null) {
                        addMetric(rdsNode, priorNode, query, iops.toString(), SourceType.storage);
                    }
                } else if ("info.rds.latestrestoretime".equals(config)) {
                    addMetric(rdsNode, priorNode, query, dateFormatter.format(rds.getLatestRestorableTime()), SourceType.storage);
                } else if ("info.rds.licensemodel".equals(config)) {
                    addMetric(rdsNode, priorNode, query, rds.getLicenseModel(), SourceType.storage);
                } else if ("info.rds.masteruser".equals(config)) {
                    addMetric(rdsNode, priorNode, query, rds.getMasterUsername(), SourceType.storage);
                } else if ("info.rds.ismultiaz".equals(config)) {
                    Boolean maz = rds.getMultiAZ();
                    if (maz != null) {
                        addMetric(rdsNode, priorNode, query, maz.toString(), SourceType.storage);
                    }
                } else if ("info.rds.preferredbackup".equals(config)) {
                    String preferredBackup = rds.getPreferredBackupWindow();
                    if (preferredBackup != null) {
                        addMetric(rdsNode, priorNode, query, preferredBackup, SourceType.storage);
                    }
                } else if ("info.rds.preferredmaintenance".equals(config)) {
                    String preferredMaintenance = rds.getPreferredMaintenanceWindow();
                    if (preferredMaintenance != null) {
                        addMetric(rdsNode, priorNode, query, preferredMaintenance, SourceType.storage);
                    }
                } else if ("info.rds.ispublic".equals(config)) {
                    Boolean isPublic = rds.getPubliclyAccessible();
                    if (isPublic != null) {
                        addMetric(rdsNode, priorNode, query, isPublic.toString(), SourceType.storage);
                    }
                } else if ("info.rds.primaryaz".equals(config)) {
                    addMetric(rdsNode, priorNode, query, rds.getAvailabilityZone(), SourceType.storage);
                } else if ("info.rds.replicationmaster".equals(config)) {
                    String repMaster = rds.getReadReplicaSourceDBInstanceIdentifier();
                    if (repMaster != null && !repMaster.isEmpty()) {
                        addMetric(rdsNode, priorNode, query, repMaster, SourceType.storage);
                    }
                } else if ("info.rds.storageencrypted".equals(config)) {
                    Boolean encrypted = rds.getStorageEncrypted();
                    if (encrypted != null) {
                        addMetric(rdsNode, priorNode, query, encrypted.toString(), SourceType.storage);
                    }
                } else if ("info.rds.storagetype".equals(config)) {
                    addMetric(rdsNode, priorNode, query, rds.getStorageType(), SourceType.storage);
                }
            }
        }
    }

    public static void gatherInventory(AWSInventory inventory, AWSConnection connection) {

        DescribeDBInstancesRequest describeDBInstancesRequest = new DescribeDBInstancesRequest();
        for (; ; ) {
            DescribeDBInstancesResult rdsInstances = connection.getRDSClient().describeDBInstances(describeDBInstancesRequest);
            List<DBInstance> rdsInstanceList = rdsInstances.getDBInstances();
            for (DBInstance rdsInstance : rdsInstanceList) {
                AWSItemRDS itemRDS = new AWSItemRDS(rdsInstance);
                inventory.put(rdsInstance.getDBInstanceIdentifier(), itemRDS);
            }

            String nextBatchId = rdsInstances.getMarker();
            if (nextBatchId == null) {
                break;
            }
            describeDBInstancesRequest.setMarker(nextBatchId);
        }
    }
}
