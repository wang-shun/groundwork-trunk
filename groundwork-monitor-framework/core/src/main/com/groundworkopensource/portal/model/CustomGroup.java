package com.groundworkopensource.portal.model;
import java.util.ArrayList;
import java.util.List;


public class CustomGroup implements java.io.Serializable{
	
	private long groupId;
	
	private String groupName;
	
	private EntityType entityType = new EntityType();
	
	private List<CustomGroupElement> elements = new ArrayList<CustomGroupElement>();
	
	private List<CustomGroup> parents = new ArrayList<CustomGroup>();
	
	private String groupState;
	
	private String createdBy = null;
	
	private String createdTimeStamp = null;
	
	private String lastModifiedTimeStamp = null;
	
	private boolean selected = false;
	
	private List<String> selectedChildren = new ArrayList<String>();
	
	private List<String> selectedParents = new ArrayList<String>();
	
	private String bubbleUpStatus = null;

	public long getGroupId() {
		return groupId;
	}

	public void setGroupId(long groupId) {
		this.groupId = groupId;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public EntityType getEntityType() {
		return entityType;
	}

	public void setEntityType(EntityType entityType) {
		this.entityType = entityType;
	}

	public List<CustomGroupElement> getElements() {
		return elements;
	}

	public void setElements(List<CustomGroupElement> elements) {
		this.elements = elements;
	}

	public List<CustomGroup> getParents() {
		return parents;
	}

	public void setParents(List<CustomGroup> parents) {
		this.parents = parents;
	}
	
	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	
	public String getGroupState() {
		return groupState;
	}

	public void setGroupState(String groupState) {
		this.groupState = groupState;
	}

	public String getCreatedTimeStamp() {
		return createdTimeStamp;
	}

	public void setCreatedTimeStamp(String createdTimeStamp) {
		this.createdTimeStamp = createdTimeStamp;
	}

	public String getLastModifiedTimeStamp() {
		return lastModifiedTimeStamp;
	}

	public void setLastModifiedTimeStamp(String lastModifiedTimeStamp) {
		this.lastModifiedTimeStamp = lastModifiedTimeStamp;
	}
	
	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	
	public List<String> getSelectedChildren() {
		return selectedChildren;
	}

	public void setSelectedChildren(List<String> selectedChildren) {
		this.selectedChildren = selectedChildren;
	}

	public List<String> getSelectedParents() {
		return selectedParents;
	}

	public void setSelectedParents(List<String> selectedParents) {
		this.selectedParents = selectedParents;
	}
	
	public String getBubbleUpStatus() {
		return bubbleUpStatus;
	}

	public void setBubbleUpStatus(String bubbleUpStatus) {
		this.bubbleUpStatus = bubbleUpStatus;
	}
	

}
