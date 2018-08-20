package org.groundwork.rs.dto;

public class DtoEntityProperty {

    private String propertyType;
    private String entityType;
    private int sortOrder;

    public DtoEntityProperty() {}

    public DtoEntityProperty(String propertyType, String entityType, int sortOrder) {
        this.propertyType = propertyType;
        this.entityType  = entityType;
        this.sortOrder = sortOrder;
    }

    public DtoEntityProperty(String propertyType, String entityType) {
        this.propertyType = propertyType;
        this.entityType  = entityType;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
