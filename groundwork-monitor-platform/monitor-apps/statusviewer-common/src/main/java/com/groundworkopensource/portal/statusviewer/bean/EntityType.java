package com.groundworkopensource.portal.statusviewer.bean;

public class EntityType {
	
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
