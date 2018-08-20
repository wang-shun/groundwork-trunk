package org.groundwork.rs.plugins;

import java.util.Vector;

public class PluginUpdate implements java.io.Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8851745073643682498L;

	private String platform = null;
	
	
	
	private Vector<Plugin> plugin = null;
	
	public PluginUpdate ()
	{
		plugin = new Vector<Plugin>();
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	

	public Plugin[] getPlugin() {
		Plugin[] retPlugins = new Plugin[plugin.size()];
        if (plugin.size() > 0) plugin.copyInto(retPlugins);
        return retPlugins;

	}

	public void setPlugin(Plugin[] newPlugins) {
		plugin = new Vector<Plugin>(newPlugins.length);
        for (int i=0; i < newPlugins.length; i++)
        {
            plugin.addElement(newPlugins[i]);
        }

	}

	

}
