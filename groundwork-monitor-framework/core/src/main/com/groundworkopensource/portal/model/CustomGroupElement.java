package com.groundworkopensource.portal.model;

public class CustomGroupElement implements java.io.Serializable {
	
	private long elementId;
	
	private long groupId;	
	
	private long entityTypeId;
	
	private String elementName = null;

	public long getElementId() {
		return elementId;
	}

	public void setElementId(long elementId) {
		this.elementId = elementId;
	}

	public long getGroupId() {
		return groupId;
	}

	public void setGroupId(long groupId) {
		this.groupId = groupId;
	}
	
	public long getEntityTypeId() {
		return entityTypeId;
	}

	public void setEntityTypeId(long entityTypeId) {
		this.entityTypeId = entityTypeId;
	}
	
	public String getElementName() {
		return elementName;
	}

	public void setElementName(String elementName) {
		this.elementName = elementName;
	}

}
