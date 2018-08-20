package org.groundwork.rs.conversion;

import com.groundwork.collage.model.ApplicationEntityProperty;
import org.groundwork.rs.dto.DtoEntityProperty;

public class EntityPropertyConverter {

    public final static DtoEntityProperty convert(ApplicationEntityProperty aep) {
        DtoEntityProperty dto = new DtoEntityProperty();
        dto.setEntityType(aep.getEntityType().getName());
        dto.setPropertyType(aep.getPropertyType().getName());
        dto.setSortOrder(aep.getSortOrder());
        return dto;
    }
}
