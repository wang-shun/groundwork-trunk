package org.groundwork.rs.conversion;

import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.biz.model.RTMMHostGroup;
import com.groundwork.collage.model.Host;
import com.groundwork.collage.model.HostGroup;
import com.groundwork.collage.model.impl.StateStatistics;
import com.groundwork.collage.model.impl.StatisticProperty;
import org.groundwork.foundation.bs.statistics.StatisticsService;
import org.groundwork.rs.dto.DtoDepthType;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoHostGroup;

import java.util.List;
import java.util.Set;

public class HostGroupConverter {

    public static DtoHostGroup convert(HostGroup hostGroup, DtoDepthType depth) {
        DtoHostGroup dto = new DtoHostGroup();
        dto.setId(hostGroup.getHostGroupId());
        dto.setName(hostGroup.getName());
        dto.setDescription(hostGroup.getDescription());
        dto.setAlias(hostGroup.getAlias());
        dto.setAgentId(hostGroup.getAgentId());
        dto.setAppType(hostGroup.getApplicationType().getName());
        dto.setAppTypeDisplayName(hostGroup.getApplicationType().getDisplayName());
        Set<Host> hostSet = hostGroup.getHosts();
        switch (depth) {
            case Simple:
                break;
            case Shallow:
                for (Host host : hostSet) {
                    dto.addHost(HostConverter.convert(host, DtoDepthType.Simple));
                }
                break;
            case Deep:
            case Full:
            case Sync:
                for (Host host : hostSet) {
                    dto.addHost(HostConverter.convert(host, depth));
                }
                dto.setApplicationType(ApplicationTypeConverter.convert(hostGroup.getApplicationType(), DtoDepthType.Shallow));
                StatisticsService statisticService = CollageFactory.getInstance().getStatisticsService();
                StateStatistics stateStatistics = statisticService.getServiceStatisticsByHostGroupName(dto.getName());
                if (stateStatistics != null) {
                    List<StatisticProperty> stats = stateStatistics.getStatisticProperties();
                    for (StatisticProperty stat : stats) {
                        dto.addStatistic(StatisticConverter.convert(stat));
                    }
                }
                dto.setBubbleUpStatus(HostGroupStatusSupport.calculateBubbleUpStatus(dto));
                break;
        }
        return dto;
    }

    /**
     * Convert RTMM host group to DTO host group instance.
     *
     * @param hostGroup RTMM host group
     * @return DTO host group instance
     */
    public static DtoHostGroup convert(RTMMHostGroup hostGroup) {
        DtoHostGroup dto = new DtoHostGroup();
        dto.setId(hostGroup.getId());
        for (Integer hostId : hostGroup.getHostIds()) {
            DtoHost dtoHost = new DtoHost();
            dtoHost.setId(hostId);
            dto.addHost(dtoHost);
        }
        dto.setName(hostGroup.getName());
        dto.setAlias(hostGroup.getAlias());
        dto.setAppType(hostGroup.getAppTypeName());
        // TODO: consider adding bubble up status for RTMM path
        return dto;
    }
}
