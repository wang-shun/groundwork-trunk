package org.groundwork.rs.conversion;

import com.groundwork.collage.model.impl.StateStatistics;
import com.groundwork.collage.model.impl.StatisticProperty;
import org.groundwork.rs.dto.DtoStateStatistic;

public class StateStatisticConverter {

    public final static DtoStateStatistic convert(StateStatistics stat) {
        DtoStateStatistic dto = new DtoStateStatistic();
        dto.setTotalHosts(stat.getTotalHosts());
        dto.setTotalServices(stat.getTotalServices());
        dto.setName(stat.getHostGroupName());
        // dto.setbubbleUpStatus( TODO: calculate
        dto.setAvailability(stat.getAvailability());
        if (stat.getStatisticProperties().size() > 0) {
            for (StatisticProperty property : stat.getStatisticProperties()) {
                dto.addProperty(StatisticConverter.convert(property));
            }
        }
        return dto;
    }
    
}
