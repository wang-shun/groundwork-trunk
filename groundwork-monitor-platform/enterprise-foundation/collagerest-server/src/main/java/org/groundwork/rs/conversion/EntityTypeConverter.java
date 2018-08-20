package org.groundwork.rs.conversion;

import com.groundwork.collage.model.EntityType;
import org.groundwork.rs.dto.DtoEntityType;

public class EntityTypeConverter {

    public final static DtoEntityType convert(EntityType entityType) {
        DtoEntityType dto = new DtoEntityType();
        dto.setId(entityType.getEntityTypeId());
        dto.setName(entityType.getName());
        dto.setDescription(entityType.getDescription());
        dto.setLogicalEntity(entityType.getLogicalEntity());
        dto.setApplicationTypeSupported(entityType.getApplicationTypeSupported());
        return dto;
    }
}
