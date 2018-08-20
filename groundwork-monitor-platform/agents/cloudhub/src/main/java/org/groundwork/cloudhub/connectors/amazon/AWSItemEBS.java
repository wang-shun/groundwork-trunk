package org.groundwork.cloudhub.connectors.amazon;

import com.amazonaws.services.ec2.model.DescribeVolumesRequest;
import com.amazonaws.services.ec2.model.DescribeVolumesResult;
import com.amazonaws.services.ec2.model.Volume;
import org.groundwork.cloudhub.gwos.GwosStatus;
import org.groundwork.cloudhub.metrics.BaseHost;
import org.groundwork.cloudhub.metrics.BaseQuery;
import org.groundwork.cloudhub.metrics.BaseVM;
import org.groundwork.cloudhub.metrics.MonitoringState;
import org.groundwork.cloudhub.metrics.SourceType;

import java.util.List;
import java.util.Map;

final class AWSItemEBS extends AWSItem {

    protected final static String CLOUDWATCH_DIMENSION_EBS = "VolumeId";

    private Volume ebs;
    private String tagName;

    public AWSItemEBS(Volume ebs) {
        this.ebs = ebs;
//        for (Tag tag : ebs.getTags()) {
//            if ("name".equalsIgnoreCase(tag.getKey())) {
//                tagName = tag.getValue();
//                return;
//            }
//        }
        tagName = ebs.getVolumeId();
    }
    
    @Override
    public String getDisplayName() {
        return tagName;
    }

    @Override
    public void collectHostGroupMetrics(MonitoringState currentState, MonitoringState priorState,
            List<BaseQuery> awsQueries, List<BaseQuery> gwQueries,
            AWSInventory awsInventory, AWSConnection connection, Map<String, BaseVM> priorVMMap) {

        String displayName = getDisplayName();
        BaseHost ebsNode = new BaseHost(displayName);
        BaseHost priorNode = priorState.hosts().get(displayName);
        if (priorNode != null) {
            ebsNode.setPrevRunState(priorNode.getRunState());
            priorVMMap.putAll(priorNode.getVMPool());
        }
        
        ebsNode.setRunExtra(ebs.getState());
        ebsNode.setRunState(getStatus());
        currentState.hosts().put(ebsNode.getHostName(), ebsNode);
        
        populateMetrics(gwQueries, ebsNode, priorNode);
        populateAWSMetrics(awsQueries, ebsNode, priorNode, connection, CLOUDWATCH_DIMENSION_EBS, getDisplayName(), SourceType.storage);
    }
    
    @Override
    public String getStatus() {
        String state = ebs.getState().toLowerCase();
        if ("available".equals(state) || "in-use".equals(state)) {
            return GwosStatus.UP.status;
        } else if ("creating".equals(state)) {
            return GwosStatus.PENDING.status;
        } else if ("deleting".equals(state)) {
            // While stuck in the "Deleting" state, we consider it unreachable.
            return GwosStatus.UNREACHABLE.status;
        } else if ("deleted".equals(state)) {
            return GwosStatus.SCHEDULED_DOWN.status;
        } else if ("error".equals(state)) {
            return GwosStatus.UNSCHEDULED_DOWN.status;
        }
        // Return a warning status if we can't interpret the state.
        return GwosStatus.WARNING.status;
    }
    
    private void populateMetrics(List<BaseQuery> gwQueries, BaseHost ebsNode, BaseHost priorNode) {

        if (isEnableInfoRetrieval()) {
            for (BaseQuery query : gwQueries) {
                String config = query.getQuery();
                if ("info.ebs.createtime".equals(config)) {
                    addMetric(ebsNode, priorNode, query, dateFormatter.format(ebs.getCreateTime()), SourceType.storage);
                } else if ("info.ebs.isencrypted".equals(config)) {
                    Boolean encrypted = ebs.getEncrypted();
                    if (encrypted != null) {
                        addMetric(ebsNode, priorNode, query, encrypted.toString(), SourceType.storage);
                    }
                } else if ("info.ebs.iops".equals(config)) {
                    Integer iops = ebs.getIops();
                    if (iops != null) {
                        addMetric(ebsNode, priorNode, query, iops.toString(), SourceType.storage);
                    }
                } else if ("info.ebs.volumesize".equals(config)) {
                    addMetric(ebsNode, priorNode, query, ebs.getSize() + " Gb", SourceType.storage);
                } else if ("info.ebs.volumetype".equals(config)) {
                    addMetric(ebsNode, priorNode, query, ebs.getVolumeType(), SourceType.storage);
                }
            }
        }
    }
    
    public static void gatherInventory(AWSInventory inventory, AWSConnection connection) {
        DescribeVolumesRequest describeVolumesRequest = new DescribeVolumesRequest();
        for (;;) {
            DescribeVolumesResult volumes = connection.getEC2Client().describeVolumes(describeVolumesRequest);
            List<Volume> volumeList = volumes.getVolumes();
            for (Volume volume : volumeList) {
                AWSItemEBS itemEBS = new AWSItemEBS(volume);
                inventory.put(volume.getVolumeId(), itemEBS);
            }
            
            String nextBatchId = volumes.getNextToken();
            if (nextBatchId == null) {
                break;
            }
            describeVolumesRequest.setNextToken(nextBatchId);
        }
    }
}
