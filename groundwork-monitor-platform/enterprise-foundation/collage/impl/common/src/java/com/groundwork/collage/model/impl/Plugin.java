package com.groundwork.collage.model.impl;

import com.groundwork.collage.model.PropertyType;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** @author Hibernate CodeGenerator */
public class Plugin extends PropertyExtensibleAbstract implements Serializable, com.groundwork.collage.model.Plugin {
	
	/* Built-In (Non-dynamic) Properties - The Properties which have a description (hibernate property)
	 * defined are properties that can be used to query the entity.
	 * We distinquish between "filterable" properties and properties that are returned in 
	 * property maps.
	 */	
	private static final PropertyType PROP_ID = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_ID,
								HP_ID, // Description is hibernate property name
								PropertyType.DataType.INTEGER, 
								Plugin.ENTITY_TYPE_CODE,
								true);		
	
	private static final PropertyType PROP_NAME = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_NAME,
								HP_NAME, // Description is hibernate property name
								PropertyType.DataType.STRING, 
								Plugin.ENTITY_TYPE_CODE,
								true);	

	private static final PropertyType PROP_DESCRIPTION = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_DESCRIPTION,
								HP_DESCRIPTION, // Description is hibernate property name
								PropertyType.DataType.STRING, 
								Plugin.ENTITY_TYPE_CODE,
								true);	
	
    /** Built-In property list - For now its static - Once app type properties are supported we may have
     * to have an instance variable **/
	private static List<PropertyType> BUILT_IN_PROPERTIES = null;	

    /** identifier field */
    private Integer pluginId;

    /** persistent field */
    private String name;

    /** nullable persistent field */
    private String url;

    /** nullable persistent field */
    private String dependencies;

    /** persistent field */
    private Date lastUpdateTimestamp;
    
    /** nullable persistent field */
	private String description;
	
private String checksum = null;
	
	private String lastUpdatedBy = null;

    /** persistent field */
    private com.groundwork.collage.model.PluginPlatform pluginPlatform;

    /** full constructor */
    public Plugin(Integer pluginId, String name, String url, String dependencies, Date lastUpdateTimestamp, PluginPlatform pluginPlatform) {
        this.pluginId = pluginId;
        this.name = name;
        this.url = url;
        this.dependencies = dependencies;
        this.lastUpdateTimestamp = lastUpdateTimestamp;
        this.pluginPlatform = pluginPlatform;
    }

    /** default constructor */
    public Plugin() {
    }

    /** minimal constructor */
    public Plugin(Integer pluginId, String name, Date lastUpdateTimestamp, com.groundwork.collage.model.PluginPlatform pluginPlatform) {
        this.pluginId = pluginId;
        this.name = name;
        this.lastUpdateTimestamp = lastUpdateTimestamp;
        this.pluginPlatform = pluginPlatform;
    }
    
    public Integer getID ()
	{
		return getPluginId();
	}

    public Integer getPluginId() {
        return this.pluginId;
    }

    public void setPluginId(Integer pluginId) {
        this.pluginId = pluginId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    // GDMA-381: Build URL dynamically as the stored URL may no longer reflect the current scheme/server/port
    public String getExternalUrl(HttpServletRequest request) {
        try {
            URL storedUrl = new URL(getUrl());
            URL externalUrl = new URL(request.getScheme(), request.getServerName(), request.getServerPort(), storedUrl.getFile());
            return externalUrl.toString();
        } catch (NullPointerException | MalformedURLException e) {
            log.error("ERROR: Unable to build external URL for plugin '" + name + "'.  Reverting to stored URL");
            return getUrl();
        }
    }

    public Date getLastUpdateTimestamp() {
        return this.lastUpdateTimestamp;
    }

    public void setLastUpdateTimestamp(Date lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    public com.groundwork.collage.model.PluginPlatform getPluginPlatform() {
        return this.pluginPlatform;
    }

    public void setPluginPlatform(com.groundwork.collage.model.PluginPlatform pluginPlatform) {
        this.pluginPlatform = pluginPlatform;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("pluginId", getPluginId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof Plugin) ) return false;
        Plugin castOther = (Plugin) other;
        return new EqualsBuilder()
            .append(this.getPluginId(), castOther.getPluginId())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getPluginId())
            .toHashCode();
    }

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	  /** 
     * This method overrides the property by the same name in
     * PropertyExtensibleAbstract to make it possible to get the value of one
     * of the named property getters 
     *
     * @throws IllegalArgumentException 
     *
     *  if unable to find PropertyType with the key provided, or if the key
     *  does not corresponding to one of the declared get/sets
     */
    public Object getProperty(String key) throws IllegalArgumentException
    {
    	if (key == null || key.length() == 0)
    	{
    		throw new IllegalArgumentException("Invalid null / empty property key.");
    	}
    	
        if (key.equalsIgnoreCase(EP_ID)) {
            return this.getID();
        }
        else if (key.equalsIgnoreCase(EP_NAME)) {
            return this.getName();
        }
        else if (key.equalsIgnoreCase(EP_DESCRIPTION)) {
            return this.getDescription();
        } 	
        else {
            return super.getProperty(key);
        }
    }   
    
	/* (non-Javadoc)
	 * @see com.groundwork.collage.model.impl.PropertyExtensibleAbstract#getBuiltInProperties()
	 */
	@Override
	public List<PropertyType> getBuiltInProperties()
	{
		if (BUILT_IN_PROPERTIES != null)
			return BUILT_IN_PROPERTIES;
			
		BUILT_IN_PROPERTIES = new ArrayList<PropertyType>(10);
		
		// Add non-volatile properties		
		BUILT_IN_PROPERTIES.add(PROP_ID);
		BUILT_IN_PROPERTIES.add(PROP_NAME);
		BUILT_IN_PROPERTIES.add(PROP_DESCRIPTION);		
		
		return BUILT_IN_PROPERTIES;
	}

	/* (non-Javadoc)
	 * @see com.groundwork.collage.model.impl.PropertyExtensibleAbstract#getEntityTypeCode()
	 */
	@Override
	public String getEntityTypeCode()
	{
		return ENTITY_TYPE_CODE;
	}

	/* (non-Javadoc)
	 * @see com.groundwork.collage.model.impl.PropertyExtensibleAbstract#getPropertyValueInstance(java.lang.String, java.lang.Object)
	 */
	@Override
	public PropertyValue getPropertyValueInstance(String name, Object value)
	{
		return new EntityPropertyValue(pluginId, getEntityTypeId(), name, value);
	}

	/* (non-Javadoc)
	 * @see com.groundwork.collage.model.PropertyExtensible#getComponentProperties()
	 */
	public List<PropertyType> getComponentProperties()
	{
		// Filterable properties are the same as the built-in properties
		return getBuiltInProperties();
	}

	public String getDependencies() {
		return dependencies;
	}

	public void setDependencies(String dependencies) {
		this.dependencies = dependencies;
	}

	public String getChecksum() {
		return checksum;
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	public String getLastUpdatedBy() {
		return lastUpdatedBy;
	}

	public void setLastUpdatedBy(String lastUpdatedBy) {
		this.lastUpdatedBy = lastUpdatedBy;
	}

}
