package org.groundwork.rs.conversion;

import com.groundwork.collage.model.Category;
import com.groundwork.collage.model.CategoryEntity;
import org.groundwork.rs.dto.DtoCategory;
import org.groundwork.rs.dto.DtoDepthType;

public class CategoryConverter {

    public final static DtoCategory convert(Category category, DtoDepthType depthType) {
        DtoCategory dto = new DtoCategory();
        dto.setId(category.getCategoryId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setAgentId(category.getAgentId());
        if (category.getApplicationType() != null) {
            dto.setAppType(category.getApplicationType().getName());
        }
        dto.setEntityTypeName(category.getEntityType().getName());
        if ((depthType == DtoDepthType.Deep) || (depthType == DtoDepthType.Full)) {
            dto.setApplicationType(ApplicationTypeConverter.convert(category.getApplicationType(), DtoDepthType.Shallow));
            dto.setEntityType(EntityTypeConverter.convert(category.getEntityType()));
            if (category.getParents() != null) {
                for (Category parent : category.getParents()) {
                    dto.addParent(convert(parent, DtoDepthType.Shallow));
                }
            }
            if (category.getChildren() != null) {
                for (Category child : category.getChildren()) {
                    dto.addChild(convert(child, ((depthType != DtoDepthType.Full) ? DtoDepthType.Shallow : DtoDepthType.Full)));
                }
            }
            if (category.getCategoryEntities() != null) {
                for (CategoryEntity entity : category.getCategoryEntities()) {
                    dto.addEntity(CategoryEntityConverter.convert(entity, DtoDepthType.Shallow));
                }
            }
        } else {
            if (category.getParents() != null) {
                for (Category parent : category.getParents()) {
                    dto.addParentName(parent.getName());
                }
            }
            if (category.getChildren() != null) {
                for (Category child : category.getChildren()) {
                    dto.addChildName(child.getName());
                }
            }
        }
        dto.setRoot(category.isRoot());
        return dto;
    }

}
