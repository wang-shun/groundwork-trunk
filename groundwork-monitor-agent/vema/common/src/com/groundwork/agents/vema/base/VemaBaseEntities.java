package com.groundwork.agents.vema.base;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.groundwork.agents.vema.api.GWOSEntity;
import com.groundwork.agents.vema.api.VemaConstants;
import com.groundwork.agents.vema.monitorAgent.MonitorAgentClient;
import com.groundwork.agents.vema.gwos.GWOSHostGroup;

/* 130613.rlynch:
 * Thought here is this... that at the topmost level, there are lists of the various
 * entities, and that they are related to each other by way of keys, which the VMs 
 * have in abundance.  Using keys, they're relatively easy to look up, if need be.
 */

public class VemaBaseEntities extends GWOSEntity
{
	private static Logger log = Logger.getLogger(VemaBaseEntities.class);
	
	private ConcurrentHashMap<String, VemaBaseHost> baseHosts    = 
		new ConcurrentHashMap<String, VemaBaseHost>();
	
	private ConcurrentHashMap<String, VemaBaseVM>   baseVMs      = 
		new ConcurrentHashMap<String, VemaBaseVM>();
	
	private ConcurrentHashMap<String, VemaBaseStorage>  baseStorage  = 
		new ConcurrentHashMap<String, VemaBaseStorage>();
	
//	private ConcurrentHashMap<String, VemaBaseResource> baseResource = 
//		new ConcurrentHashMap<String, VemaBaseResource>();
	
	private ConcurrentHashMap<String, VemaBaseNetwork> baseNetworks     = 
		new ConcurrentHashMap<String, VemaBaseNetwork>();

	
	// -------------------------------------------------------------
	// HOSTs
	// -------------------------------------------------------------
	
	public List<String> getHostNames()
	{
		ArrayList<String> results = new ArrayList<String>();
		results.addAll( baseHosts.keySet() );
		return results;
	}
	
	public VemaBaseHost getHost( String hostname )
	{
		return baseHosts.get( hostname );
	}
	
	public void addHost(String host, VemaBaseHost hosto)
	{
		if     ( host  == null ) log.error( "Host name is (null)" );
		else if( hosto == null ) log.error( "Host object is (null)" );
		else                     baseHosts.put( host, hosto );
	}
	
	public ConcurrentHashMap<String, VemaBaseHost> getHostHash()
	{
		return baseHosts;
	}
	
	public int sizeHosts()
	{
		return baseHosts.size();
	}
	
	public boolean hasHosts()
	{
		return baseHosts.size() != 0;
	}
	
	// -------------------------------------------------------------
	// VMs
	// -------------------------------------------------------------
	
	public List<String> getVMNames()
	{
		ArrayList<String> results = new ArrayList<String>();
		results.addAll( baseVMs.keySet() );
		return results;
	}
	
	public void addVM(String vm, VemaBaseHost vmo)
	{
		if     ( vm  == null ) log.error( "VM name is (null)" );
		else if( vmo == null ) log.error( "VM object is (null)" );
		else                   baseHosts.put( vm, vmo );
	}

	public List<String> getStorageNames()
	{
		ArrayList<String> results = new ArrayList<String>();
		results.addAll( baseStorage.keySet() );
		return results;
	}

	//	public List<String> getResourceNames()
//	{
//		ArrayList<String> results = new ArrayList<String>();
//		results.addAll( baseResource.keySet() );
//		return results;
//	}
	public List<String> getNetworkNames()
	{
		ArrayList<String> results = new ArrayList<String>();
		results.addAll( baseNetworks.keySet() );
		return results;
	}	
}
