package com.groundworkopensource.webapp.license.bean;

public class Parameter {
	
	private boolean selected = false;
	
	private String name = null;
	
	private Object value = null;
	
	private String type = null;
	
	public Parameter()
	{
		
	}
	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}


}
