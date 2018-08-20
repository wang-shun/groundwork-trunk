package org.groundwork.rs.conversion;

import com.groundwork.collage.model.CategoryEntity;
import org.groundwork.rs.dto.DtoCategoryEntity;
import org.groundwork.rs.dto.DtoDepthType;

public class CategoryEntityConverter {


    public final static DtoCategoryEntity convert(CategoryEntity categoryEntity, DtoDepthType depthType) {
        DtoCategoryEntity dto = new DtoCategoryEntity();
        dto.setId(categoryEntity.getCategoryEntityID());
        if (categoryEntity.getEntityType() != null) {
            dto.setEntityTypeId(categoryEntity.getEntityType().getEntityTypeId());
            dto.setEntityTypeName(categoryEntity.getEntityType().getName());
        }
        dto.setObjectID(categoryEntity.getObjectID());
        if (depthType == DtoDepthType.Deep) {
            dto.setEntityType(EntityTypeConverter.convert(categoryEntity.getEntityType()));
        }
        return dto;
    }    
}
