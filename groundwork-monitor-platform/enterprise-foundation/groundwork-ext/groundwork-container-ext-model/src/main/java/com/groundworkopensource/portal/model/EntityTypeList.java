package com.groundworkopensource.portal.model;

import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;

@XmlRootElement(name = "entitytype_list")
public class EntityTypeList {
	private List<EntityType> list;

	public EntityTypeList() {
	}

	public EntityTypeList(List<EntityType> list) {
		this.list = list;
	}

	@XmlElement(name = "entitytype")
	public List<EntityType> getList() {
		return list;
	}

	public void setList(List<EntityType> list) {
		this.list = list;
	}
}