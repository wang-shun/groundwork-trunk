package com.groundworkopensource.portal.model;

public class EntityType implements java.io.Serializable{
	
	// Making default 1 as HostGroup
	private long entityTypeId=1;
	
	private String entityType;

	public long getEntityTypeId() {
		return entityTypeId;
	}

	public void setEntityTypeId(long entityTypeId) {
		this.entityTypeId = entityTypeId;
	}

	public String getEntityType() {
		return entityType;
	}

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

}
