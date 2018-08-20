package com.groundwork.collage.model;

public interface PluginPlatform extends AttributeData {
	
	  /** the name that identifies this entity in the system: "PLUGIN" */
    static final String ENTITY_TYPE_CODE = "PLUGIN_PLATFORM";

	/** Spring bean interface id */
	static final String INTERFACE_NAME = "com.groundwork.collage.model.PluginPlatform";
	
	/** Hibernate component name that this entity service using */
	static final String COMPONENT_NAME = "com.groundwork.collage.model.impl.PluginPlatform";	
	
	static final String HP_ID = "platformId";
	static final String HP_NAME = "name";
	static final String HP_DESCRIPTION = "description";

	/** Entity Property Constants */
	static final String EP_ID = "PlatformId";
	static final String EP_NAME = "Name";
	static final String EP_DESCRIPTION = "Description";
	
	public Integer getPlatformId();
	public Integer getArch();

	public void setArch(Integer arch);	

}
