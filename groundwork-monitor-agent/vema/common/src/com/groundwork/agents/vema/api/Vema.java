/*
 * Copyright 2012 GroundWork , Inc. ("GroundWork") 
 * All rights reserved. 
*/
package com.groundwork.agents.vema.api;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.groundwork.agents.vema.utils.ParamBox;
import com.groundwork.agents.vema.api.VemaConstants.ConnectionState;
import com.groundwork.agents.vema.base.VemaBaseHost;
import com.groundwork.agents.vema.base.VemaBaseObject;
import com.groundwork.agents.vema.base.VemaBaseObjectTree;
import com.groundwork.agents.vema.base.VemaBaseQuery;
import com.groundwork.agents.vema.exception.VEMAException;

/* 
 * 
 */

public interface Vema
{
    /* @connect  - establishes connection with vSphere, ESXi, RHEV-M server
     * @url      - the URL reference to the TCP/IP vSphere/ESXi server
     * @login    - authorized user name
     * @password - ... for the user name
     * @vm       - virtual-machine entry-point name on [url] machine
     *             (can - and SHOULD - normally be 'null' )
     *
     * It is NORMAL for this connection to take a substatial [10-20 seconds] amount of time
     * due to setup bracketing within the VIM25 API support module. 
     */
    // void connect( String url, String login, String password, String vm ) throws VEMAException;
    
	void connect( ParamBox parambox ) throws VEMAException;
    
	
	/*
     * @collect      - initiate a round of statistics collection.
     * 
     * @previousVema - the result of a prior collect() call.  This is used to 
     *                 compute the 'delta' or change between calls.  The changes
     *                 are used in turn for sending warning/critical events and
     *                 for state changes.
     *                 
     *                 pass in a (null) 
     */
    VemaBaseObjectTree collect( VemaBaseObjectTree previous ) throws VEMAException;
    /**
     * For checking connection state to the virtual management server.
     * @return (enum) ConnectionState 
     */

    /*
     * @getConnectionState() - returns the state of the connection
     * 
     * the method may trigger a "is connected()?" underlying query, which could take
     * a bit of time (seconds).  
     */
    ConnectionState getConnectionState();

    /* @disconnect - breaks the connection. Closes resources. Cleans up after itself. 
     */
    void disconnect() throws VEMAException;
    
    /* @setCollectionMode() - set collection modes;
     * 
     * doHosts - collect host objects (hypervisors)
     * doVMs   - collect VM   objects (virtual machines)
     * doStorageDomains - collect Storage objects
     * doNetworks       - collect Network objects
     * doResourcePools  - collect Resource Pools
     * doClusters       - collect Cluster information (may be moot... see implementation)
     * doDataCenters    - collect Datacenter information
     */
    void setCollectionMode(
    		boolean doHosts, 
    		boolean doVMs,
		    boolean doStorageDomains,
		    boolean doNetworks,
		    boolean doResourcePools,
		    boolean doClusters,
		    boolean doDataCenters
    );
    
    /**
     * @formatGetListHost()  Helper method to format the older TREE of hosts-and-vms
     * @param hostHash - the tree returnd by getListHost()
     */
    public String formatGetListHost( ConcurrentHashMap< String, VemaBaseHost >hosthash );
    
    public ConcurrentHashMap<String, VemaBaseHost> getListHost( ConcurrentHashMap<String, VemaBaseHost> priorVBH, List<VemaBaseQuery>hostQueries, List<VemaBaseQuery>vmQueries );
   
    /**
     * Prints key internal members;
     * good for use with log4j
     * 
     * @return
     */
    public String getInternals();

    /**
     * Return a unified list of hosts and vms as monitored objects. 
     * 
     * @param hostTree
     * @return
     */
    public ConcurrentHashMap<String, VemaBaseObject> getHostAndVM( ConcurrentHashMap< String, VemaBaseHost> hostTree );
}
