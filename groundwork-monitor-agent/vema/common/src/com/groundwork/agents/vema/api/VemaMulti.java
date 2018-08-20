package com.groundwork.agents.vema.api;


import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.groundwork.agents.vema.api.VemaConstants.ConnectionState;
import com.groundwork.agents.vema.base.VemaBaseHost;
import com.groundwork.agents.vema.base.VemaBaseObject;
import com.groundwork.agents.vema.base.VemaBaseQuery;
import com.groundwork.agents.vema.base.VemaBaseHostURI;
import com.groundwork.agents.vema.exception.VEMAException;

public interface VemaMulti
{
	/**
	 * Makes the connections to the named machines / authentication handles. Sequentially.
	 * @throws VEMAException
	 */
    void connect() throws VEMAException;
    /**
     * @collect    - initiate a round of statistics collection.
     */
    void collect() throws VEMAException;
    
    /**
     * For checking connection state to the virtual management server.
     * Special state SEMICONNECTED supported for partial device connectivity.
     * @return (enum) ConnectionState 
     */
    ConnectionState getConnectionState();

    /**
     * @disconnect - breaks the connection. Closes resources. Cleans up after itself. 
     */
    void disconnect() throws VEMAException;

    /**
     * Grabs the TREE of hosts-and-vms
     * @param priorVBH
     * @param hostQueries
     * @param vmQueries
     * @return
     */
    public ConcurrentHashMap<String, VemaBaseHost> getListHost( List<VemaBaseQuery>hostQueries, List<VemaBaseQuery>vmQueries );
   
    /**
     * Prints key internal members;
     * 
     * @return
     */
    public String getInternals();

    /**
     * @param hostTree
     * @return
     */
    public ConcurrentHashMap<String, VemaBaseObject> getHostAndVM( ConcurrentHashMap< String, VemaBaseHost> hostTree );
}
