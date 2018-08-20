/*
 * Copyright 2012 GroundWork , Inc. ("GroundWork") 
 * All rights reserved. 
*/
package com.groundwork.agents.vema.gwos;

/**
 * @author rruttimann@gwos.com
 * Created: Jun 22, 2012
 */
public class EntityAttribute {
	
	private String name	= null;
	private String value = null;
	
	/**
	 * Default constructor requires both values set
	 * @param name
	 * @param value
	 */
	public EntityAttribute(String name, String value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * Setters and Getters for the name value members
	 */
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	

}
