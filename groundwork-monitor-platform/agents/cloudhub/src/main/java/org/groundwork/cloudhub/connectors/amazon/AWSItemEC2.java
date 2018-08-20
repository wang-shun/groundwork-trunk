package org.groundwork.cloudhub.connectors.amazon;

import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.EbsInstanceBlockDevice;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceBlockDeviceMapping;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.Tag;
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

final class AWSItemEC2 extends AWSItem {

    private final static String CLOUDWATCH_DIMENSION_EC2 = "InstanceId";
    
    private Instance ec2;
    
    public AWSItemEC2(Instance ec2) {
        this.ec2 = ec2;
    }
    
    @Override
    public String getDisplayName() {
        final String PREFIX = AmazonConfigurationProvider.PREFIX_EC2_INSTANCE;
        
        for (Tag tag : ec2.getTags()) {
            if ("name".equalsIgnoreCase(tag.getKey())) {
                return PREFIX + tag.getValue();
            }
        }
        String privateDNS = ec2.getPrivateDnsName();
        if (privateDNS != null && !privateDNS.isEmpty()) {
            return PREFIX + privateDNS;
        }
        // We should never get here, but just in case...
        return PREFIX + ec2.getInstanceId();
    }
    
    @Override
    public void resolveRelationState(AWSInventory inventory) {
        AWSItem azItem = inventory.get(ec2.getPlacement().getAvailabilityZone(), AWSItemAZ.class);
        if (azItem != null) {
            azItem.markInUse();
        }
    }
    
    @Override
    public void populateHostDataCenterInventory(DataCenterInventory inventory,
            InventoryOptions options, AWSInventory awsInventory) {

        VirtualMachineNode vmNode = new VirtualMachineNode(getDisplayName(), ec2.getInstanceId());
        vmNode.setStatus(getStatus());
        inventory.getVirtualMachines().put(vmNode.getName(), vmNode);
        
        if (options.isViewHypervisors()) {
            String azId = ec2.getPlacement().getAvailabilityZone();
            AWSItem azItem = awsInventory.get(azId, AWSItemAZ.class);
            if (azItem != null) {
                InventoryContainerNode azNode = inventory.getHypervisors().get(azItem.getDisplayName());
                if (azNode != null) {
                    azNode.getVms().put(vmNode.getName(), vmNode);
                }
            }
        }

        // CLOUDHUB-296: 7.1.1: removed - no longer group EC2 under EBS datastores
        //if (options.isViewDatastores()) {

        if (options.isViewNetworks()) {
            AWSItem vpcItem = awsInventory.get(ec2.getVpcId(), AWSItemVPC.class);
            if (vpcItem != null) {
                InventoryContainerNode vpcNode = inventory.getNetworks().get(vpcItem.getDisplayName());
                if (vpcNode != null) {
                    vpcNode.getVms().put(vmNode.getName(), vmNode);
                }
            }
        }
        if (options.isViewResourcePools()) {
            AWSItem pg = awsInventory.get(ec2.getPlacement().getGroupName(), AWSItemPG.class);
            if (pg != null) {
                InventoryContainerNode pgNode = inventory.getResourcePools().get(pg.getDisplayName());
                if (pgNode != null) {
                    pgNode.getVms().put(vmNode.getName(), vmNode);
                }
            }
        }

        if (options.isViewTaggedGroups() && options.getGroupTag() != null) {
            List<Tag> tags = ec2.getTags();
            for (Tag tag: tags) {
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
    
    @Override
    public void collectHostMetrics(MonitoringState currentState, MonitoringState priorState,
            List<BaseQuery> awsQueries, List<BaseQuery> gwQueries, List<BaseQuery> customQueries,
            AWSInventory awsInventory, AWSConnection connection, Map<String, BaseVM> priorVMMap) {
        
        String displayName = getDisplayName();
        BaseVM ec2Node = new AmazonVM(displayName, AmazonVM.AmazonNodeType.EC2);
        BaseVM priorNode = priorVMMap.get(displayName);
        if (priorNode != null) {
            ec2Node.setPrevRunState(priorNode.getRunState());
        }
        ec2Node.setRunExtra(ec2.getState().getName());
        ec2Node.setRunState(getStatus());
        populateMetrics(gwQueries, ec2Node, priorNode);
        populateAWSMetrics(awsQueries, ec2Node, priorNode, connection, CLOUDWATCH_DIMENSION_EC2, ec2.getInstanceId(), null);

        if (awsInventory.getCollectionMode().isDoCustom()) {
            populateAWSMetrics(customQueries, ec2Node, priorNode, connection, CLOUDWATCH_DIMENSION_EC2, ec2.getInstanceId(), SourceType.custom);
        }

        AWSItem azItem = awsInventory.get(ec2.getPlacement().getAvailabilityZone(), AWSItemAZ.class);
        if (azItem != null) {
            BaseHost hostGroup = currentState.hosts().get(azItem.getDisplayName());
            if (hostGroup != null) {
                hostGroup.putVM(displayName, ec2Node);
            }
        }
        
        AWSItem vpcItem = awsInventory.get(ec2.getVpcId(), AWSItemVPC.class);
        if (vpcItem != null) {
            BaseHost hostGroup = currentState.hosts().get(vpcItem.getDisplayName());
            if (hostGroup != null) {
                hostGroup.putVM(displayName, ec2Node);
            }
        }
        
        AWSItem pgItem = awsInventory.get(ec2.getPlacement().getGroupName(), AWSItemPG.class);
        if (pgItem != null) {
            BaseHost hostGroup = currentState.hosts().get(pgItem.getDisplayName());
            if (hostGroup != null) {
                hostGroup.putVM(displayName, ec2Node);
            }
        }

        // CLOUDHUB-296: 7.1.1: don't add EC2 to ebsItem HG (no longer group EBS), instead add EBS metrics to EC2 node
        for (InstanceBlockDeviceMapping blockDevMap : ec2.getBlockDeviceMappings()) {
            EbsInstanceBlockDevice ebs = blockDevMap.getEbs();
            if (ebs != null) {
                AWSItem ebsItem = awsInventory.get(ebs.getVolumeId(), AWSItemEBS.class);
                if (ebsItem != null) {
                    ebsItem.populateAWSMetrics(awsQueries, ec2Node, priorNode, connection, AWSItemEBS.CLOUDWATCH_DIMENSION_EBS, ebs.getVolumeId(), SourceType.storage);
                }
            }
        }
    }
    
    @Override
    public String getStatus() {
        String state = ec2.getState().getName().toLowerCase();
        if ("running".equals(state)) {
            return GwosStatus.UP.status;
        } else if ("pending".equals(state)) {
            return GwosStatus.PENDING.status;
        } else if ("stopping".equals(state) || "shutting-down".equals(state)) {
            // While stuck in the stopping state, we consider it unreachable.
            return GwosStatus.UNREACHABLE.status;
        } else if ("stopped".equals(state) || "terminated".equals(state)) {
            return GwosStatus.SCHEDULED_DOWN.status;
        }
        // Return a warning status if we can't interpret the state.
        return GwosStatus.WARNING.status;
    }
    
    private void populateMetrics(List<BaseQuery> gwQueries, BaseVM ec2Node, BaseVM priorNode) {
        
        ec2Node.setBootDate(ec2.getLaunchTime());
        if (isEnableInfoRetrieval()) {
            for (BaseQuery query : gwQueries) {
                String config = query.getQuery();
                if ("info.ec2.architecture".equals(config)) {
                    addMetric(ec2Node, priorNode, query, ec2.getArchitecture());
                } else if ("info.ec2.isebsoptimized".equals(config)) {
                    Boolean ebsOpt = ec2.getEbsOptimized();
                    if (ebsOpt != null) {
                        addMetric(ec2Node, priorNode, query, ebsOpt.toString());
                    }
                } else if ("info.ec2.hypervisor".equals(config)) {
                    addMetric(ec2Node, priorNode, query, ec2.getHypervisor());
                } else if ("info.ec2.imageid".equals(config)) {
                    addMetric(ec2Node, priorNode, query, ec2.getImageId());
                } else if ("info.ec2.instanceid".equals(config)) {
                    addMetric(ec2Node, priorNode, query, ec2.getInstanceId());
                } else if ("info.ec2.instancetype".equals(config)) {
                    addMetric(ec2Node, priorNode, query, ec2.getInstanceType());
                } else if ("info.ec2.detailedmonitoring".equals(config)) {
                    addMetric(ec2Node, priorNode, query, ec2.getMonitoring().getState());
                } else if ("info.ec2.tenancy".equals(config)) {
                    addMetric(ec2Node, priorNode, query, ec2.getPlacement().getTenancy());
                } else if ("info.ec2.platform".equals(config)) {
                    String platform = ec2.getPlatform();
                    if (platform == null) {
                        platform = "linux";
                    }
                    addMetric(ec2Node, priorNode, query, platform);
                } else if ("info.ec2.privatedns".equals(config)) {
                    addMetric(ec2Node, priorNode, query, ec2.getPrivateDnsName());
                } else if ("info.ec2.privateip".equals(config)) {
                    addMetric(ec2Node, priorNode, query, ec2.getPrivateIpAddress());
                } else if ("info.ec2.publicdns".equals(config)) {
                    String publicDns = ec2.getPublicDnsName();
                    if (publicDns != null) {
                        addMetric(ec2Node, priorNode, query, publicDns);
                    }
                } else if ("info.ec2.publicip".equals(config)) {
                    String publicIp = ec2.getPublicIpAddress();
                    if (publicIp != null) {
                        addMetric(ec2Node, priorNode, query, publicIp);
                    }
                } else if ("info.ec2.rootdevice".equals(config)) {
                    addMetric(ec2Node, priorNode, query, ec2.getRootDeviceType());
                } else if ("info.ec2.srcdestchecking".equals(config)) {
                    Boolean srcDestCheck = ec2.getSourceDestCheck();
                    if (srcDestCheck != null) {
                        addMetric(ec2Node, priorNode, query, srcDestCheck.toString());
                    }
                } else if ("info.ec2.enhancednetworking".equals(config)) {
                    String sriovNetSupport = ec2.getSriovNetSupport();
                    if (sriovNetSupport != null) {
                        addMetric(ec2Node, priorNode, query, sriovNetSupport);
                    }
                } else if ("info.ec2.subnetid".equals(config)) {
                    addMetric(ec2Node, priorNode, query, ec2.getSubnetId());
                } else if ("info.ec2.vpcid".equals(config)) {
                    addMetric(ec2Node, priorNode, query, ec2.getVpcId());
                }
            }
        }
    }
    
    public static void gatherInventory(AWSInventory inventory, AWSConnection connection) {
        DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
        for (;;) {
            DescribeInstancesResult instances = connection.getEC2Client().describeInstances(describeInstancesRequest);
            List<Reservation> reservationList = instances.getReservations();
            for (Reservation reservation : reservationList) {
                for (Instance instance : reservation.getInstances()) {
                    AWSItemEC2 itemEC2 = new AWSItemEC2(instance);
                    inventory.put(instance.getInstanceId(), itemEC2);
                }
            }
            
            String nextBatchId = instances.getNextToken();
            if (nextBatchId == null) {
                break;
            }
            describeInstancesRequest.setNextToken(nextBatchId);
        }
    }
    
}
