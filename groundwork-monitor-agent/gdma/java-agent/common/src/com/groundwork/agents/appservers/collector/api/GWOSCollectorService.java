package com.groundwork.agents.appservers.collector.api;

import java.util.HashMap;
import java.util.Properties;

public interface GWOSCollectorService {
	
	public void start();
	
	public void shutdown();
	
	public HashMap<String, Object> autoDiscoverComponents();
	
	public HashMap<String, Object> autoDiscoverComponents(Properties prop);
	
	public boolean testConnection(Properties prop);

}
