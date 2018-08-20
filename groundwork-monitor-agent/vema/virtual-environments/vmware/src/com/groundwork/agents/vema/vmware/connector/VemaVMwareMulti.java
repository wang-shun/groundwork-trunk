package com.groundwork.agents.vema.vmware.connector;

/**
 * VemaRhevMulti - encapsulates VemaRhev (single VMware management host query class) into
 *               a multi-host query class. 
 *
 * Differences between VemaRhevMulti and VemaRhev:
 * -------------------------------------------------------------------------------
 *  
 * -------------------------------------------------------------------------------
 *  MULTI:  connect()                       the URI and authentication
 *  Single: connect( url, user, pass, vm )  information now comes in during
 *                                          instantiation.
 *
 *  MULTI:  getListHost(           hostFilters, vmFilters )
 *  Single: getListHost( prevList, hostFilters, vmFilters )
 *
 *                                          The MULTI method keeps its own history
 *                                          of Vema() object lists internally so 
 *                                          doesn't need to have them passed in.
 *
 *  MULTI:  getConnectionState()            No difference except the MULTI version
 *  Single: getConnectionState()            supports the SEMICONNECTED state.
 */

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.groundwork.agents.vema.api.VemaConstants.ConnectionState;
import com.groundwork.agents.vema.api.VemaMulti;
import com.groundwork.agents.vema.api.Vema;
import com.groundwork.agents.vema.base.VemaBaseHost;
import com.groundwork.agents.vema.base.VemaBaseHostURI;
import com.groundwork.agents.vema.base.VemaBaseObject;
import com.groundwork.agents.vema.base.VemaBaseQuery;
import com.groundwork.agents.vema.exception.VEMAException;

public class VemaVMwareMulti implements VemaMulti
{
    private static org.apache.log4j.Logger log = Logger.getLogger( VemaVMwareMulti.class );
	private static class VemaHandle
	{
		private Vema                                    vema;
		private VemaBaseHostURI                         hostURI;
        private ConcurrentHashMap<String, VemaBaseHost> hostTree;

		public VemaHandle( VemaBaseHostURI host, Vema v )
		{
			hostURI = host;
			vema    = v;
		}

        public ConcurrentHashMap<String, VemaBaseHost> getHostTree() 
        { 
            return hostTree; 
        }

        public void setHostTree( ConcurrentHashMap<String, VemaBaseHost> tree )
        { 
            this.hostTree = tree;
        }

        public Vema   getVema()     { return this.vema;                  }
		public String getUri()      { return this.hostURI.getUri();      }
		public String getUser()     { return this.hostURI.getUser();     }
		public String getPassword() { return this.hostURI.getPassword(); }
		public String getVm()       { return this.hostURI.getVm();       }
	}
	private ArrayList<VemaHandle> handleList = new ArrayList<VemaHandle>();
	
    public VemaVMwareMulti( ArrayList<VemaBaseHostURI> hostList )
    {
        for( VemaBaseHostURI hostURI : hostList )
            handleList.add( new VemaHandle( hostURI, new VemaVMware() ) );
    }

    public void remove( int index )
    {
        handleList.remove( index );
    }
	
	public void connect() throws VEMAException
	{
        for( VemaHandle handle : handleList )
        {
            try
            {
            	throw new VEMAException( "need to change to connect(parambox) call");
//                handle.getVema().connect( 
//                handle.getUri(),
//                handle.getUser(),
//                handle.getPassword(),
//                handle.getVm() );
            }
            catch( Exception e )
            {
                log.debug( String.format( "Cannot connect '%s': %s / %s / %s\n", 
                    handle.getUri(), 
                    handle.getUser(), 
                    "*******", // handle.getPassword(), 
                    handle.getVm() ));
            }
        }
	}

	public void collect() throws VEMAException
	{
        return;  // ?? OK, so I don't know what this was supposed to do.
	}

	public ConnectionState getConnectionState()
	{
        boolean connected = false;
        boolean otherwise = false;

        for( VemaHandle handle : handleList )
            if( handle.getVema().getConnectionState() == ConnectionState.CONNECTED )
                 connected = true;
            else otherwise = true;

        if( connected ) if( otherwise ) return ConnectionState.SEMICONNECTED;
                        else            return ConnectionState.CONNECTED;
        else            if( otherwise ) return ConnectionState.DISCONNECTED;
                        else            return ConnectionState.NASCENT;
	}

	public void disconnect() throws VEMAException
	{
        for( VemaHandle handle : handleList )
            handle.getVema().disconnect();
	}

	public ConcurrentHashMap<String, VemaBaseHost> getListHost( 
			List<VemaBaseQuery> hostQueries, List<VemaBaseQuery> vmQueries )
	{
        ConcurrentHashMap<String, VemaBaseHost> output = new ConcurrentHashMap<String, VemaBaseHost>();

		for( VemaHandle handle : handleList )
        {
            ConcurrentHashMap<String, VemaBaseHost> result = 
                handle.getVema().getListHost( handle.getHostTree(), hostQueries, vmQueries );

            handle.setHostTree( result );

            for( String host : result.keySet() )
                output.put( host, result.get( host ) );
        }
        return output;
	}

	public String getInternals()
	{
        return handleList.size() > 0 
            ? handleList.get(0).getVema().getInternals() 
            : "";
	}

	public ConcurrentHashMap<String, VemaBaseObject> getHostAndVM(
			ConcurrentHashMap<String, VemaBaseHost> hostTree )
	{
        return handleList.size() > 0 
            ? handleList.get(0).getVema().getHostAndVM( hostTree ) 
            : null;
	}
}
