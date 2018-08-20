package org.groundwork.rs.conversion;

import com.groundwork.collage.model.impl.StatisticProperty;
import org.groundwork.rs.dto.DtoStatistic;

public class StatisticConverter {

    public final static DtoStatistic convert(StatisticProperty property) {
        DtoStatistic dto = new DtoStatistic();
        dto.setName(property.getName());
        dto.setCount(property.getCount());
        return dto;
    }
    
}
