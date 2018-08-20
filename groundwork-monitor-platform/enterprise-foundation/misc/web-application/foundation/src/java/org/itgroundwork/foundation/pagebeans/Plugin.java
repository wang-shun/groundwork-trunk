package org.itgroundwork.foundation.pagebeans;

import java.util.Date;


public class Plugin {

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

    /** persistent field */
    private PluginPlatform pluginPlatform;
    
    private boolean selected = false;

    /** full constructor */
    public Plugin(Integer pluginId, String name, String url,String dependencies, Date lastUpdateTimestamp, PluginPlatform pluginPlatform) {
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
    public Plugin(Integer pluginId, String name, Date lastUpdateTimestamp, PluginPlatform pluginPlatform) {
        this.pluginId = pluginId;
        this.name = name;
        this.lastUpdateTimestamp = lastUpdateTimestamp;
        this.pluginPlatform = pluginPlatform;
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

   

    public Date getLastUpdateTimestamp() {
        return this.lastUpdateTimestamp;
    }

    public void setLastUpdateTimestamp(Date lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    public PluginPlatform getPluginPlatform() {
        return this.pluginPlatform;
    }

    public void setPluginPlatform(PluginPlatform pluginPlatform) {
        this.pluginPlatform = pluginPlatform;
    }

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	

	public String getDependencies() {
		return dependencies;
	}

	public void setDependencies(String dependencies) {
		this.dependencies = dependencies;
	}

 

}
