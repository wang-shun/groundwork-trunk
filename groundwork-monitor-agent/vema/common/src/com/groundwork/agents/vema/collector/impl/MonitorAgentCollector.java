package com.groundwork.agents.vema.collector.impl;

import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import com.groundwork.agents.vema.api.Vema;


/**
 * 
 * This interface is used to create a Scheduler to run the Synchronizer for GWOS HostList and Vema HostList
 * 
 * @author rvardhineedi
 *
 */
public interface MonitorAgentCollector 
{
	public void start(Vema vema, String gwosConfigFilename, String vemaMonitorProfileFilename, String hypervisorVmware, String connectorVmware, String mgmtServerVmware, String applicationTypeVmware );
	public void shutdown();
	
	public HashMap<String, Long> autoDiscoverComponents();
	public HashMap<String, Long> autoDiscoverComponents(Properties prop);
	
	public boolean testConnection(Properties prop);
}
