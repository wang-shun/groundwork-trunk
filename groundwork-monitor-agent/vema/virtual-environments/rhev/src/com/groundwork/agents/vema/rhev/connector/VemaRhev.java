package com.groundwork.agents.vema.rhev.connector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Date;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPFaultException;

import org.apache.log4j.Logger;

import com.groundwork.agents.vema.api.Vema;
import com.groundwork.agents.vema.api.VemaConstants;
import com.groundwork.agents.vema.api.VemaConstants.ConnectionState;
import com.groundwork.agents.vema.base.VemaBaseHost;
import com.groundwork.agents.vema.base.VemaBaseMetric;
import com.groundwork.agents.vema.base.VemaBaseObject;
import com.groundwork.agents.vema.base.VemaBaseObjectTree;
import com.groundwork.agents.vema.base.VemaBaseQuery;
import com.groundwork.agents.vema.base.VemaBaseSynthetic;
import com.groundwork.agents.vema.base.VemaBaseVM;
import com.groundwork.agents.vema.exception.VEMAException;
import com.groundwork.agents.vema.rest.RESTbox;
import com.groundwork.agents.vema.utils.aPad;
import com.groundwork.agents.vema.utils.ParamBox;
import com.groundwork.agents.vema.utils.Conversion;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;	
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.KeyStore;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.BasicClientConnectionManager;

import sun.misc.Sort;

import com.groundwork.agents.vema.rhev.restapi.*;

// import java.io.BufferedReader;       // were used in examples, may not be now
// import java.io.InputStreamReader;
// import java.io.File;
// import javax.xml.parsers.*;

/**
 * <pre>
 * VemaRhev
 *
 * A utility-application to scan virtual-machine  processing cloud
 * APIs ( application-programming-interfaces ), translating the results
 * obtained, on a periodic basis, to the format( s ) that may directly
 * be used by the GroundWorkOpenSystems ( GWOS ) systems management
 * software.
 *
 *
 * The authorization parameters are:
 *
 * accessauth            [req]: establish the authorization node
 * url | system          [req]: url of the web service
 * user[name]            [req]: username for the authentication
 * pass[word]            [req]: password for the authentication
 * vm[name]              [req]: name of the vm to address
 *
 * OPERATING DESIGN:
 *
 * </pre>
 */

public class VemaRhev implements Vema
{
	private String             vemaVM;             // virtual machine within URL (unused!)

    private boolean doHosts              = true;
    private boolean doVMs                = true;
    private boolean doStorageDomains     = false;
    private boolean doNetworks           = false;
    private boolean doResourcePools      = false;
    private boolean doClusters           = false;
    private boolean doDataCenters        = false;

	private API                rhevApi;            // for the "top" level of the REST API containing
	private Hosts              rhevHosts;          // list of hosts...
	private VMs                rhevVms;            // list of vm's
	private StorageDomains     rhevStorageDomains; // list of storage domains
	private Networks           rhevNetworks;       // list of logical networks
	private Groups             rhevGroups;         // list of groups
	private Disks              rhevDisks;          // list of "disks"
	private DataCenters        rhevDataCenters;    // list of data centrs
	private VmPools            rhevVmPools ;       // list of VmPools 
	private Clusters           rhevClusters;       // list of clusters
	
	private RESTbox            restBox = null;     // for the REST API access class
    private ConcurrentHashMap<String,              // to implement category->element->parameter->value
            ConcurrentHashMap<String,              //
            ConcurrentHashMap<String, String>>> rhevMap = null; 
    
    private ConcurrentHashMap<String, String> i2n = 
    	new ConcurrentHashMap<String, String>();
	
	private ArrayList<String>  hostFilters;
	private ArrayList<String>  VMFilters;

    private ConnectionState    vemaState       = ConnectionState.NASCENT;
    private int                vemaRetries     = 0;
    private static final int   MAXRETRIES      = 3;  //internal, arbitrary
    private static final long  RETRYGAP        = 5L * 1000L;  // 5 secs...

    private static String      VMS             = "vms";
    private static String      HOSTS           = "hosts";
    private static String      DATACENTERS     = "datacenters";
    private static String      VMPOOLS         = "vmpools";
    private static String      STORAGEDOMAINS  = "storagedomains";
    private static String      NETWORKS        = "networks";
    private static String      GROUPS          = "groups";
    private static String      DISKS           = "disks";
    private static String      CLUSTERS        = "clusters";

    private int                minSkippedBeforeDefunct = 0;  // settable
    private int                minSkippedBeforeDelete  = 1;  // settable
    private int                minTimeBeforeDefunct    = 0 * 60; // seconds
    private int                minTimeBeforeDelete     = 1 * 60; // seconds

    private static org.apache.log4j.Logger log = Logger.getLogger( VemaRhev.class );
    
	public VemaRhev()	// constructor
	{
        log.debug( "inside VemaRhev() constructor" );
	}
	

    public void setCollectionMode( boolean doHosts, boolean doVMs, boolean doStorageDomains, boolean doNetworks, boolean doResourcePools, boolean doClusters, boolean doDataCenters)
    {
        this.doHosts           = doHosts;
        this.doVMs             = doVMs;
        this.doStorageDomains  = doStorageDomains;
        this.doNetworks        = doNetworks;
        this.doResourcePools   = doResourcePools;
        this.doClusters        = doClusters;
        this.doDataCenters     = doDataCenters;
    }

    public String getStateString()
    {
        return
            vemaState == ConnectionState.NASCENT      ? "Nascent connection (untried)"
        :   vemaState == ConnectionState.CONNECTING   ? "Connecting to service"
        :   vemaState == ConnectionState.CONNECTED    ? "Connected"
        :   vemaState == ConnectionState.FAILED       ? "Connection failed"
        :   vemaState == ConnectionState.DISCONNECTED ? "Disconnected"
        :   vemaState == ConnectionState.TIMEDOUT     ? "Timed out (" + vemaRetries + ")"
        :   /* ... else ... */                          "getStateString() ... unknown state";
    }

    public ConnectionState getConnectionState()
    {
        if( vemaState == ConnectionState.CONNECTED )
            if( restBox.isConnectionOK() == false )
                vemaState = ConnectionState.DISCONNECTED;

    	return vemaState;
    }
    
    public void setDefunctCriteria( int minSkipped, int minTime, int dieSkipped, int dieTime )
    {
    	minSkippedBeforeDefunct = minSkipped;
    	minTimeBeforeDefunct    = minTime;
    	minSkippedBeforeDelete  = dieSkipped;
    	minTimeBeforeDelete     = dieTime;
    }

    private void connect()
    {
        switch( vemaState )
        {
            case       CONNECTED:   return;
            case          FAILED:   return;
            case        TIMEDOUT:   return;
            case      CONNECTING:   // fallthru to DEFAULT
            case    DISCONNECTED:   // fallthru to DEFAULT
            case         NASCENT:   // fallthru to DEFAULT
            default:
                vemaState = 
                    restBox.isConnectionOK()
                    ? ConnectionState.CONNECTED
                    : ConnectionState.FAILED
                    ;
                // fall thru switch block
        }
        return;
    }

    public void connect( String url, String login, String pass, String vm ) throws VEMAException
    {
        log.info( "RHEV connect( url, login, pass, vm ) DEPRECATED and updated to connect( host," 
            + " login, pass, realm, port, protocol, restbase," 
            + " certspath, keystorepass )" );

        throw new VEMAException( "connect( url, login, pass, vm ) deprecated" );
    }
    
    public void connect( ParamBox paramBox )
    {
        try
        {
            connect(
                paramBox.get( "vema", "api", "fqhost"     ),
                paramBox.get( "vema", "api", "user"       ),
                paramBox.get( "vema", "api", "password"   ),
                paramBox.get( "vema", "api", "realm"      ),
                paramBox.get( "vema", "api", "port"       ),
                paramBox.get( "vema", "api", "protocol"   ),
                paramBox.get( "vema", "api", "baseuri"    ),
                paramBox.get( "vema", "api", "certsfile"  ),
                paramBox.get( "vema", "api", "certspass"  )    		
            );
        }
        catch ( Exception e )
        {
            log.error( e );
        }
    }

	private void connect( String host, String login, String password,
			String realm, String port, String protocol, String restbase,
			String certspath, String keystorepass ) throws VEMAException
	{
		try 
		{
			log.debug("\ninfo: connect( "  
                    + "\n   host     = '" + ( host         == null ? "undef" : host         ) + "'"
                    + "\n   login    = '" + ( login        == null ? "undef" : login        ) + "'"
                    + "\n   pass     = '" + ( password     == null ? "undef" : "*******"    ) + "'"
                    + "\n   realm    = '" + ( realm        == null ? "undef" : realm        ) + "'"
                    + "\n   port     = '" + ( port         == null ? "undef" : port         ) + "'"
                    + "\n   protocol = '" + ( protocol     == null ? "undef" : protocol     ) + "'"
                    + "\n   restbase = '" + ( restbase     == null ? "undef" : restbase     ) + "'"
                    + "\n   certpath = '" + ( certspath    == null ? "undef" : certspath    ) + "'"
                    + "\n   keypass  = '" + ( keystorepass == null ? "undef" : keystorepass ) + "'"
                    + "\n)\n"
                );

			restBox = new RESTbox( 
                host,           // 
                login,          // 
                password,       // 
                realm,          // 
                port,           // 
                protocol,       // 
				restbase,       // 
                certspath,      // 
                keystorepass    // 
            );
		}

		catch( Exception e )
		{
			log.debug("\nconnect( "  
                    + "\n   host     = '" + ( host         == null ? "undef" : host         ) + "'"
                    + "\n   login    = '" + ( login        == null ? "undef" : login        ) + "'"
                    + "\n   pass     = '" + ( password     == null ? "undef" : "*******"    ) + "'"
                    + "\n   realm    = '" + ( realm        == null ? "undef" : realm        ) + "'"
                    + "\n   port     = '" + ( port         == null ? "undef" : port         ) + "'"
                    + "\n   protocol = '" + ( protocol     == null ? "undef" : protocol     ) + "'"
                    + "\n   restbase = '" + ( restbase     == null ? "undef" : restbase     ) + "'"
                    + "\n   certpath = '" + ( certspath    == null ? "undef" : certspath    ) + "'"
                    + "\n   keypass  = '" + ( keystorepass == null ? "undef" : keystorepass ) + "'"
                    + "\n)\n"
                );

			log.error( "connect() - couldn't instantiate REST object" );
		}

        log.debug( "past connect({parameters})" );
        connect();
        log.debug( "past connect()" );
	}

    public String formatGetListHost( ConcurrentHashMap<String, VemaBaseHost> glh )
    {
        StringBuilder s = new StringBuilder( 10000 );  // hint at initial sizing

        s.append( "\n" );
        s.append( "-------------------------------------------\n" );
        s.append( "           getListHost object:\n" );
        s.append( "-------------------------------------------\n" );
        for( String key : glh.keySet() )
        {
            s.append( "- - - - - - - - - - - - - - - - - - - - - - \n" );
            s.append( String.format( "%-40s: (getListHost formatted output)\n%s", key, glh.get( key ).formatSelf() ));
        }
        s.append( "-------------------------------------------\n" );

        return s.toString();
    }

    public String formatRhevMap()
    {
    	StringBuilder s = new StringBuilder( 10000 );  // hint at initial sizing
    	String   indent = "";
    	
    	for( String category : rhevMap.keySet() )
    	{
    		indent = "";
    		s.append( indent + "category: " + category + "\n" );
    		for( String element : rhevMap.get( category ).keySet() )
    		{
        		indent = "  ";
        		s.append( indent + "element: " + element + "\n" );
                ArrayList<String> keys = new ArrayList<String>();
                keys.addAll( rhevMap.get( category ).get( element ).keySet() );
                Collections.sort( keys );
    			for( String parameter : keys )
    			{
    	    		indent = "    ";
            		s.append( indent + 
            			String.format( 
            				"parameter: %-40s: %s\n", 
            				parameter, 
            				rhevMap.get( category ).get( element ).get( parameter ).toString() 
            				) );
    			}
    		}
    	}
    	s.append( "\n" );
    	return s.toString();
    }

    private void getAndCompileREST()
    {
        // first... make no assumptions about "currentness", and force a re-get of all
        // REST data.

    	if( rhevMap       != null ) rhevMap.clear();
        if( i2n           != null ) i2n.clear();

        rhevApi            = null; // whack 'em all first
        rhevHosts          = null; // tho really not necessary
        rhevVms            = null; // because object garbage
        rhevStorageDomains = null; // collection is quite well behaved
        rhevNetworks       = null; // in Java
        rhevGroups         = null;
        rhevDisks          = null;
        rhevDataCenters    = null;
        rhevVmPools        = null;
        rhevClusters       = null;
        
        rhevApi            = retrieveAPI           ( "/api"                );  // then go get new stats
        rhevHosts          = retrieveHosts         ( "/api/hosts"          );
        rhevVms            = retrieveVMs           ( "/api/vms"            );
        rhevStorageDomains = retrieveStorageDomains( "/api/storagedomains" );
        rhevNetworks       = retrieveNetworks      ( "/api/networks"       );
        rhevGroups         = retrieveGroups        ( "/api/groups"         );
        rhevDisks          = retrieveDisks         ( "/api/disks"          );
        rhevDataCenters    = retrieveDataCenters   ( "/api/datacenters"    );
        rhevVmPools        = retrieveVmPools       ( "/api/vmpools"        );
        rhevClusters       = retrieveClusters      ( "/api/clusters"       );
        
        compileAPI();              // then compile stats into rhevMap
        compileHosts();
        compileVMs();
        compileStorageDomains();
        compileNetworks();
        compileGroups();
        compileDisks();
        compileDataCenters();
        compileVmPools();
        compileClusters();

        log.debug( formatRhevMap() );  // TRACE of objects...
    }
    
    /** rhevMapAdd( category, element, parameter value )
     * 
     * Adds values to rhevMap.category.element.parameter = value
     * ...conceptually
     */
    private void rhevMapAdd( String category, String element, String parameter, String value ) throws VEMAException
    {
    	if( rhevMap == null )
    		rhevMap = new ConcurrentHashMap<String, 
    				      ConcurrentHashMap<String,
    				      ConcurrentHashMap<String, String>>>();

    	if( ! rhevMap.containsKey( category ) )
    		rhevMap.put( category, 
    				new ConcurrentHashMap<String,ConcurrentHashMap<String,String>>() );
    	
    	if( ! rhevMap.get( category ).containsKey( element ) )
    		rhevMap.get( category ).put( element, 
    				new ConcurrentHashMap<String,String>() );
    	
        // now put it down!  zapnull() ensures no nulls, but "blanks" instead;
        try 
        {
            rhevMap.get( category ).get( element ).put( parameter, zapnull( value ) );
        } 
        catch ( Exception e )
        {
            log.error( String.format( 
                "\nBad rhevMap('%s').get('%s').put('%s', '%s')\n",
                category, 
                element, 
                parameter, 
                zapnull( value ) ) 
            );
        }
    }
    
    private void rhevMapDelete( String category, String element, String parameter ) throws Exception
    {

   		throw new Exception( "PROGRAMMER: need to fix this to avoid nulls" );
   		/*
    	if(   rhevMap == null
    	||  ! rhevMap.containsKey( category  )
    	||  ! rhevMap.get( category  ).containsKey( element )
    	||  ! rhevMap.get( category ).get( element ).containsKey( parameter ) )
    		return;
    	
    	// this will trim off parameter->value pairs
    	rhevMap.get( category ).get( element ).remove( parameter );
    	
    	// this will trim out element branches that don't have parameters
    	if( rhevMap.get( category ).get( element ).size() == 0 )
    		rhevMap.get( category ).remove( element );
    	
    	// this will trim out category branches that don't have elements
    	if( rhevMap.get( category ).size() == 0)
    		rhevMap.remove( category );
    	*/
    }

    private String rhevMapGet( String category, String element, String parameter )
    {
        if( rhevMap   == null 
        ||  category  == null
        ||  element   == null
        ||  parameter == null
        || !rhevMap.containsKey( category ) 
        || !rhevMap.get( category ).containsKey( element )
        || !rhevMap.get( category ).get( element ).containsKey( parameter )
        )
            return "";

        return rhevMap.get( category ).get( element ).get( parameter );
    }

    // ----------------------------------------------------------------------
    // COMPILE section... to make all the connections and names right!
    // ----------------------------------------------------------------------
    //
    public void compileAPI() throws VEMAException
    {
        // nothing to do.  NO stats come from this level.
    }
    
    private void compileHosts() throws VEMAException
    {
        if( rhevHosts == null )
            throw new VEMAException( "no data in hosts structure" );

        String category = HOSTS;

        for( Host host : rhevHosts.getHosts() )
        {
        	String id = host.getId();
            String clusterid = host.getCluster() == null ? "" : host.getCluster().getId();

            rhevMapAdd( category, id, "id",                        id );
            rhevMapAdd( category, id, "name",                      host.getName() );
            rhevMapAdd( category, id, "address",                   host.getAddress()               == null
                                                            ? "" : host.getAddress() );
            rhevMapAdd( category, id, "certificate.organization",  host.getCertificate()           == null
                                                            ? "" : host.getCertificate().getOrganization() );
            rhevMapAdd( category, id, "cluster.id",                clusterid );
            rhevMapAdd( category, id, "cluster.name",              i2nGet(clusterid));
            rhevMapAdd( category, id, "cpu.id",                    host.getCpu()                   == null
                                                            ? "" : host.getCpu().getId() );
            rhevMapAdd( category, id, "cpu.name",                  host.getCpu()                   == null
                                                            ? "" : host.getCpu().getName() );
            rhevMapAdd( category, id, "cpu.speed",                 host.getCpu()                   == null
                                                                || host.getCpu().getSpeed()        == null
                                                            ? "" : host.getCpu().getSpeed().toString() );
            rhevMapAdd( category, id, "cpu.cores",                 host.getCpu()                   == null
                                                            ? "" : host.getCpu().getTopology().getCores().toString() );
            rhevMapAdd( category, id, "description",               host.getDescription()           == null
                                                            ? "" : host.getDescription() );
            rhevMapAdd( category, id, "memory",                    host.getMemory()                == null
                                                            ? "" : Conversion.byte2MB(
                                                                   host.getMemory().toString() ));
            rhevMapAdd( category, id, "max_sched_memory",          host.getMaxSchedulingMemory()   == null
                                                            ? "" : Conversion.byte2MB(
                                                                   host.getMaxSchedulingMemory().toString() ));
            rhevMapAdd( category, id, "active",                    host.getSummary()               == null
                                                            ? "" : host.getSummary().getActive().toString() );
            rhevMapAdd( category, id, "migrating",                 host.getSummary()               == null
                                                            ? "" : host.getSummary().getMigrating().toString() );
            rhevMapAdd( category, id, "total",                     host.getSummary()               == null
                                                            ? "" : host.getSummary().getTotal().toString() );
            rhevMapAdd( category, id, "type",                      host.getType()                  == null
                                                            ? "" : host.getType() );
            rhevMapAdd( category, id, "port",                      host.getPort()                  == null
                                                            ? "" : host.getPort().toString() );
            rhevMapAdd( category, id, "status.state",              host.getStatus()                == null
                                                            ? "" : host.getStatus().getState() );
            rhevMapAdd( category, id, "status.detail",             host.getStatus()                == null
                                                            ? "" : host.getStatus().getDetail() );

            for( Link link : host.getLinks() )
            {
            	String rel  = link.getRel();
            	String href = link.getHref();
            	if(      rel.equalsIgnoreCase( "storage" ) )  // unlike above is really "host storage" type
            	{
                    if( host.getStatus() == null 
                    || !host.getStatus().getState().equalsIgnoreCase( "up" ) )
                        continue;   // short circuit - skip storage, has a BIG timeout!

            		HostStorage hoststorage = retrieveHostStorage( href );
            		
            		int i = 0;
            		for( Storage storage : hoststorage.getStorage() )
            		{
	                    rhevMapAdd( category, id, "storage["+i+"].id",           storage.getId() );
	                    rhevMapAdd( category, id, "storage["+i+"].name",         storage.getName() );
	                    rhevMapAdd( category, id, "storage["+i+"].size",         storage.getLogicalUnits() == null
	                    		                                            ||   storage.getLogicalUnits().isEmpty()
	                                                                      ? "" : Conversion.byte2MB( 
                                                                                 storage.getLogicalUnits().get(0).getSize().toString() ));
	                    rhevMapAdd( category, id, "storage["+i+"].host.id",      storage.getHost()         == null
	                                                                      ? "" : storage.getHost().getId() );
	                    i++;
            		}
            	}
            	else if( rel.equalsIgnoreCase( "nics" ))
            	{
            		HostNics hostnics = retrieveHostNics( href );
                    int i = 0;
                    for( HostNIC hostnic : hostnics.getHostNics() )
                    {
                        String nicid = hostnic.getNetwork() == null ? "" : hostnic.getNetwork().getId();

                        rhevMapAdd( category, id, "nic["+i+"].id",            hostnic.getId() );
                        rhevMapAdd( category, id, "nic["+i+"].name",          hostnic.getName() );
                        rhevMapAdd( category, id, "nic["+i+"].network.id",    nicid );
                        rhevMapAdd( category, id, "nic["+i+"].network.name",  i2nGet( nicid ));
                        rhevMapAdd( category, id, "nic["+i+"].mac",           hostnic.getMac() == null
                                                                      ? "" :  hostnic.getMac().getAddress());
                        rhevMapAdd( category, id, "nic["+i+"].ip",            hostnic.getIp() == null
                                                                      ? "" :  hostnic.getIp().getAddress() );
                        rhevMapAdd( category, id, "nic["+i+"].mask",          hostnic.getIp() == null
                                                                      ? "" :  hostnic.getIp().getNetmask() );
                        rhevMapAdd( category, id, "nic["+i+"].gateway",       hostnic.getIp() == null
                                                                      ? "" :  hostnic.getIp().getGateway() );
                        rhevMapAdd( category, id, "nic["+i+"].speed",         hostnic.getSpeed() == null
                                                                      ? "" :  Conversion.byte2KB( hostnic.getSpeed().toString() ));
                        rhevMapAdd( category, id, "nic["+i+"].boot",          hostnic.getBootProtocol() == null
                                                                      ? "" :  hostnic.getBootProtocol() );
                        rhevMapAdd( category, id, "nic["+i+"].status.state",  hostnic.getStatus() == null
                                                                      ? "" :  hostnic.getStatus().getState() );

                        for( Link link2 : hostnic.getLinks() )
                        {
                            String rel2  = link2.getRel();
                            String href2 = link2.getHref();
                            if( rel2.equalsIgnoreCase( "statistics" ))
                            {
                                Statistics statistics = retrieveStatistics( href2 );
                                for( Statistic stat : statistics.getStatistics() )
                                {
                                    String name = stat.getName();
                                    String value = 
                                            stat.getValues().getValues()                     == null
                                    ||      stat.getValues().getValues().get( 0 )            == null
                                    ||      stat.getValues().getValues().get( 0 ).getDatum() == null
                                    ?  "" : stat.getValues().getValues().get( 0 ).getDatum().toString();

                                    rhevMapAdd( category, id, "nic["+i+"].stat."+name+".id",           stat.getId() );
                                    rhevMapAdd( category, id, "nic["+i+"].stat."+name+".name",         stat.getName() );
                                    rhevMapAdd( category, id, "nic["+i+"].stat."+name+".description",  stat.getDescription() );
                                    rhevMapAdd( category, id, "nic["+i+"].stat."+name+".type",         stat.getType().toString());
                                    rhevMapAdd( category, id, "nic["+i+"].stat."+name+".unit",         "KB/s" );
                                    rhevMapAdd( category, id, "nic["+i+"].stat."+name+".value",        Conversion.byte2KB( value ));    

                                }
                            }
                        }
                        i++;
                    }
            	}
            	else if( rel.equalsIgnoreCase( "statistics" ))
            	{
            		Statistics statistics = retrieveStatistics( href );
                    int i = 0;
                    for( Statistic stat : statistics.getStatistics() )
                    {
                    	String name = stat.getName();
                    	String value = 
                        		stat.getValues().getValues()                     == null
                        ||      stat.getValues().getValues().get( 0 )            == null
                        ||      stat.getValues().getValues().get( 0 ).getDatum() == null
                        ?  "" : stat.getValues().getValues().get( 0 ).getDatum().toString();

                        if( name.startsWith( "memory" )
                        ||  name.startsWith( "swap"   ) )
                            value = Conversion.byte2MB( value );

                    	rhevMapAdd( category, id, "stat."+name+".id",           stat.getId() );
                        rhevMapAdd( category, id, "stat."+name+".name",         stat.getName() );
                        rhevMapAdd( category, id, "stat."+name+".description",  stat.getDescription() );
                        rhevMapAdd( category, id, "stat."+name+".type",         stat.getType().toString());
                        rhevMapAdd( category, id, "stat."+name+".unit",         "KB/s" );
                        rhevMapAdd( category, id, "stat."+name+".value",        value );    

                        i++;
                    }
            	}
            	else if( rel.equalsIgnoreCase( "tags"  ))
            		continue; // do nothing
            	else if( rel.equalsIgnoreCase( "permissions"  ))
            		continue; // do nothing
            	else
            		continue; // do nothing ... though more worrisome - all cases should be above
            }
        }
    }

    private void compileVMs() throws VEMAException
    {
        if( rhevVms == null )
            throw new VEMAException( "no data in VMs structure" );

        String category = VMS;

        for( VM vm : rhevVms.getVMs() )
        {
        	String id         = vm.getId();
            String hostid     = vm.getHost()     == null ? "" : vm.getHost().getId();
            String clusterid  = vm.getCluster()  == null ? "" : vm.getCluster().getId();
            String templateid = vm.getTemplate() == null ? "" : vm.getTemplate().getId();
            String vmpoolid   = vm.getVmPool()   == null ? "" : vm.getVmPool().getId();

            rhevMapAdd( category, id, "id",                        id );
            rhevMapAdd( category, id, "name",                      vm.getName() );
            rhevMapAdd( category, id, "type",                      vm.getType() );
            rhevMapAdd( category, id, "status.state",              vm.getStatus().getState() );
            rhevMapAdd( category, id, "status.detail",             vm.getStatus().getDetail() );
            rhevMapAdd( category, id, "memory",                    vm.getMemory()                  == null
                                                            ? "" : Conversion.byte2MB( vm.getMemory().toString() ));
            rhevMapAdd( category, id, "cpu.cores",                 vm.getCpu()                     == null
                                                            ? "" : vm.getCpu().getTopology().getCores().toString() );
            rhevMapAdd( category, id, "os.type",                   vm.getOs()                      == null
                                                            ? "" : vm.getOs().getType() );
            rhevMapAdd( category, id, "display.type",              vm.getDisplay()                 == null
                                                            ? "" : vm.getDisplay().getType() );
            rhevMapAdd( category, id, "display.address",           vm.getDisplay()                 == null
                                                            ? "" : vm.getDisplay().getAddress() );
            rhevMapAdd( category, id, "display.port",              vm.getDisplay().getPort()       == null
                                                            ? "" : vm.getDisplay().getPort().toString() );
            rhevMapAdd( category, id, "display.secure_port",       vm.getDisplay().getSecurePort() == null
                                                            ? "" : vm.getDisplay().getSecurePort().toString() );
            rhevMapAdd( category, id, "display.monitors",          vm.getDisplay().getMonitors()   == null
                                                            ? "" : vm.getDisplay().getMonitors().toString() );
            rhevMapAdd( category, id, "host.id",                   hostid );
            rhevMapAdd( category, id, "host.name",                 i2nGet( hostid ));
            rhevMapAdd( category, id, "cluster.id",                clusterid );
            rhevMapAdd( category, id, "cluster.name",              i2nGet( clusterid ));
            rhevMapAdd( category, id, "template.id",               templateid );
            rhevMapAdd( category, id, "template.name",             i2nGet( templateid ));
            rhevMapAdd( category, id, "start_time",                vm.getStartTime()               == null
                                                            ? "" : vm.getStartTime().toString() );
            rhevMapAdd( category, id, "origin",                    vm.getOrigin()                  == null
                                                            ? "" : vm.getOrigin() );
            rhevMapAdd( category, id, "memory_policy.guaranteed",  vm.getMemoryPolicy()            == null
                                                            ? "" : Conversion.byte2MB( vm.getMemoryPolicy().getGuaranteed().toString() ));
            rhevMapAdd( category, id, "vmpool.id",                 vmpoolid );
            rhevMapAdd( category, id, "vmpool.name",               i2nGet( vmpoolid ));

            if( vm.getGuestInfo() != null )
            {
                IPs ips = vm.getGuestInfo().getIps(); //                arrgghh....
                int ii = 0;
                for( IP ip : ips.getIPs() )
                {
                    rhevMapAdd( category, id, "ip["+ii+"]",                 ip.getAddress() );
                    ii++;
                }
            }

            for( Link link : vm.getLinks() )
            {
            	String rel  = link.getRel();
            	String href = link.getHref();
            	if(      rel.equalsIgnoreCase( "disks" ) )
            	{
            		Disks disks = retrieveDisks( href );
                    int i = 0;
                    for( Disk disk : disks.getDisks() )
                    {
                        rhevMapAdd( category, id, "disk["+i+"].id",               disk.getId() );
                        rhevMapAdd( category, id, "disk["+i+"].name",             disk.getName() );
                        rhevMapAdd( category, id, "disk["+i+"].size",             Conversion.byte2MB( disk.getSize().toString()) );
                        rhevMapAdd( category, id, "disk["+i+"].provisioned_size", Conversion.byte2MB( disk.getProvisionedSize().toString()));
                        rhevMapAdd( category, id, "disk["+i+"].actual_size",      Conversion.byte2MB( disk.getActualSize().toString()) );
                        rhevMapAdd( category, id, "disk["+i+"].status.state",     disk.getStatus().getState());
                        i++;
                    }
            	}
            	else if( rel.equalsIgnoreCase( "nics" ))
            	{
            		Nics nics = retrieveNics( href );
                    int i = 0;
                    for( NIC nic : nics.getNics() )
                    {
                        String vmid = nic.getVm() == null ? "" : nic.getVm().getId();
                        String netid = nic.getNetwork() == null ? "" : nic.getNetwork().getId();

                        rhevMapAdd( category, id, "nic["+i+"].id",               nic.getId() );
                        rhevMapAdd( category, id, "nic["+i+"].name",             nic.getName() );
                        rhevMapAdd( category, id, "nic["+i+"].vm.id",            vmid );
                        rhevMapAdd( category, id, "nic["+i+"].vm.name",          i2nGet( vmid ));
                        rhevMapAdd( category, id, "nic["+i+"].network.id",       netid );
                        rhevMapAdd( category, id, "nic["+i+"].network.name",     i2nGet( netid ));
                        rhevMapAdd( category, id, "nic["+i+"].mac",              nic.getMac() == null
                                                                          ? "" : nic.getMac().getAddress());
                        rhevMapAdd( category, id, "nic["+i+"].active",           nic.isActive() == null
                                                                          ? "" : nic.isActive().toString());
                        i++;
                    }
            	}
            	else if( rel.equalsIgnoreCase( "statistics" ))
            	{
            		Statistics statistics = retrieveStatistics( href );
                    int i = 0;
                    for( Statistic stat : statistics.getStatistics() )
                    {
                    	String name = stat.getName();

                    	rhevMapAdd( category, id, "stat."+name+".id",           stat.getId() );
                        rhevMapAdd( category, id, "stat."+name+".name",         stat.getName() );
                        rhevMapAdd( category, id, "stat."+name+".description",  stat.getDescription() );
                        rhevMapAdd( category, id, "stat."+name+".type",         stat.getType().toString());
                        rhevMapAdd( category, id, "stat."+name+".unit",         stat.getUnit().toString());
                        String value = 
                        		stat.getValues().getValues()                     == null
                        ||      stat.getValues().getValues().get( 0 )            == null
                        ||      stat.getValues().getValues().get( 0 ).getDatum() == null
                        ?  "" : stat.getValues().getValues().get( 0 ).getDatum().toString()
                        		;

                        if( name.startsWith( "memory" )
                        ||  name.startsWith( "swap"   ) )
                            value = Conversion.byte2MB( value );

                        rhevMapAdd( category, id, "stat."+name+".value",        value );
                        i++;
                    }
            	}
            	else if( rel.equalsIgnoreCase( "cdroms"  ))
            		continue; // do nothing
            	else if( rel.equalsIgnoreCase( "snapshots"  ))
            		continue; // do nothing
            	else if( rel.equalsIgnoreCase( "tags"  ))
            		continue; // do nothing
            	else if( rel.equalsIgnoreCase( "permissions"  ))
            		continue; // do nothing
            	else
            		continue; // do nothing ... though more worrisome - all cases should be above
            }
        }
    }

    private void compileStorageDomains() throws VEMAException
    {
        if( rhevStorageDomains == null )
            throw new VEMAException( "no data in StorageDomains structure" );

        String category = STORAGEDOMAINS;    // kind of a constant...

        for( StorageDomain sd : rhevStorageDomains.getStorageDomains() )
        {
        	String id           = sd.getId();
            String hostid       = sd.getHost()       == null ? "" : sd.getHost().getId();
            String datacenterid = sd.getDataCenter() == null ? "" : sd.getDataCenter().getId();

            rhevMapAdd( category, id, "id",              id );
            rhevMapAdd( category, id, "name",            sd.getName() );
            rhevMapAdd( category, id, "status.state",    sd.getStatus()     == null
                                                  ? "" : sd.getStatus().getState() );
            rhevMapAdd( category, id, "status.detail",   sd.getStatus()     == null
                                                  ? "" : sd.getStatus().getDetail() );
            rhevMapAdd( category, id, "available",       sd.getAvailable()  == null
                                                  ? "" : Conversion.byte2MB( sd.getAvailable().toString() ));
            rhevMapAdd( category, id, "committed",       sd.getCommitted()  == null
                                                  ? "" : Conversion.byte2MB( sd.getCommitted().toString() ));
            rhevMapAdd( category, id, "used",            sd.getUsed()       == null
                                                  ? "" : Conversion.byte2MB( sd.getUsed().toString() ));
            rhevMapAdd( category, id, "datacenter.id",   datacenterid );
            rhevMapAdd( category, id, "datacenter.name", i2nGet( datacenterid ));
            rhevMapAdd( category, id, "description",     sd.getDescription() );
            rhevMapAdd( category, id, "host.id",         hostid );
            rhevMapAdd( category, id, "host.name",       i2nGet( hostid ));
            rhevMapAdd( category, id, "storage_format",  sd.getStorageFormat() );
            rhevMapAdd( category, id, "storage.name",    sd.getStorage()    == null
                                                                       ? "" : sd.getStorage().getName() );

            // there ARE links, but no statistics or other terribly useful "for monitoring" info on them.
        }
    }

    private void compileNetworks() throws VEMAException
    {
        if( rhevNetworks == null )
            throw new VEMAException( "no data in Networks structure" );

        String category = NETWORKS;    // kind of a constant...

        for( Network net : rhevNetworks.getNetworks() )
        {
        	String id = net.getId();
            rhevMapAdd( category, id, "id",               id );
            rhevMapAdd( category, id, "name",             net.getName());
        }
    }

    private void compileGroups() throws VEMAException
    {
        if( rhevGroups == null )
            throw new VEMAException( "no data in Groups structure" );

        String category = GROUPS;    // kind of a constant...

        for( Group grp : rhevGroups.getGroups() )
        {
        	String id = grp.getId();
            rhevMapAdd( category, id, "id",                id );
            rhevMapAdd( category, id, "name",              grp.getName() );
        }
    }

    private void compileDisks() throws VEMAException
    {
        if( rhevDisks == null )
            throw new VEMAException( "no data in Disks structure" );

        String category = DISKS;    // kind of a constant...

        for( Disk disk : rhevDisks.getDisks() )
        {
        	String id = disk.getId();

            rhevMapAdd( category, id, "id",                  id );
            rhevMapAdd( category, id, "name",                disk.getName() );
            rhevMapAdd( category, id, "alias",               disk.getAlias() );
            rhevMapAdd( category, id, "image_id",            disk.getImageId() );
            rhevMapAdd( category, id, "size",                disk.getSize() == null
                                                      ? "" : Conversion.byte2MB( disk.getSize().toString() ));
            rhevMapAdd( category, id, "provisioned_size",    disk.getProvisionedSize() == null
                                                      ? "" : Conversion.byte2MB( disk.getProvisionedSize().toString() ));
            rhevMapAdd( category, id, "actual_size",         disk.getActualSize() == null
                                                      ? "" : Conversion.byte2MB( disk.getActualSize().toString() ));
            rhevMapAdd( category, id, "status.state",        disk.getStatus() == null
                                                      ? "" : disk.getStatus().getState() );
            rhevMapAdd( category, id, "status.detail",       disk.getStatus() == null
                                                      ? "" : disk.getStatus().getDetail() );
            rhevMapAdd( category, id, "interface",           disk.getInterface() );
            rhevMapAdd( category, id, "format",              disk.getFormat() );

            for( Link link : disk.getLinks() )
            {
            	String rel  = link.getRel();
            	String href = link.getHref();
            	if( rel.equalsIgnoreCase( "statistics" ))
            	{
            		Statistics statistics = retrieveStatistics( href );
                    int i = 0;
                    for( Statistic stat : statistics.getStatistics() )
                    {
                    	String name = stat.getName();

                    	rhevMapAdd( category, id, "stat."+name+".id",           stat.getId() );
                        rhevMapAdd( category, id, "stat."+name+".name",         stat.getName() );
                        rhevMapAdd( category, id, "stat."+name+".description",  stat.getDescription() );
                        rhevMapAdd( category, id, "stat."+name+".type",         stat.getType().toString());
                        rhevMapAdd( category, id, "stat."+name+".unit",         stat.getUnit().toString());
                        String value = 
                        		stat.getValues().getValues()                     == null
                        ||      stat.getValues().getValues().get( 0 )            == null
                        ||      stat.getValues().getValues().get( 0 ).getDatum() == null
                        ?  "" : stat.getValues().getValues().get( 0 ).getDatum().toString()
                        		;

                        if( name.startsWith( "memory" )
                        ||  name.startsWith( "swap"   ) )
                            value = Conversion.byte2MB( value );

                        rhevMapAdd( category, id, "stat."+name+".value",        value );
                        i++;
                    }
            	}
            	else
            		continue; // do nothing ... though more worrisome - all cases should be above
            }
        }
    }

    private void compileDataCenters() throws VEMAException
    {
        if( rhevDataCenters == null )
            throw new VEMAException( "no data in DataCenters structure" );

        String category = DATACENTERS;    // kind of a constant...

        for( DataCenter dc : rhevDataCenters.getDataCenters() )
        {
        	String id = dc.getId();
            rhevMapAdd( category, id, "id",                        id );
            rhevMapAdd( category, id, "name",                      dc.getName() );
            rhevMapAdd( category, id, "storage_type",              dc.getStorageType() );
            rhevMapAdd( category, id, "description",               dc.getDescription() );
            rhevMapAdd( category, id, "storage_format",            dc.getStorageFormat() );
            rhevMapAdd( category, id, "status.state",              dc.getStatus() == null
                                                           ? "" :  dc.getStatus().getState() );
            rhevMapAdd( category, id, "status.detail",             dc.getStatus() == null
                                                           ? "" :  dc.getStatus().getDetail() );
        }
    }

    private void compileVmPools() throws VEMAException
    {
        if( rhevVmPools == null )
            throw new VEMAException( "no data in VmPools structure" );

        String category = VMPOOLS;    // kind of a constant...

        for( VmPool vmpool : rhevVmPools.getVmPools() )
        {
        	String id = vmpool.getId();
            String clusterid = vmpool.getCluster() == null ? "" : vmpool.getCluster().getId();

            rhevMapAdd( category, id, "id",           id );
            rhevMapAdd( category, id, "name",         vmpool.getName() );
            rhevMapAdd( category, id, "description",  vmpool.getDescription() );
            rhevMapAdd( category, id, "cluster.id",   clusterid );
            rhevMapAdd( category, id, "cluster.name", i2nGet(clusterid) );
            rhevMapAdd( category, id, "size",         vmpool.getSize()    == null
            		                           ? "" : vmpool.getSize().toString() );

        }
    }

    private void compileClusters() throws VEMAException
    {
        if( rhevClusters == null )
            throw new VEMAException( "no data in Clusters structure" );

        String category = CLUSTERS;    // kind of a constant...

        for( Cluster clu : rhevClusters.getClusters() )
        {
        	String id = clu.getId();
            rhevMapAdd( category, id, "id",            id );
            rhevMapAdd( category, id, "name",          clu.getName() );
            rhevMapAdd( category, id, "description",   clu.getDescription() );
            rhevMapAdd( category, id, "cpu.id",        clu.getCpu() == null
                                               ? "" :  clu.getCpu().getId() );
            rhevMapAdd( category, id, "datacenter.id", clu.getDataCenter() == null
                                               ? "" :  clu.getDataCenter().getId() );
        }
    }

    /**
     * retrieveUniversal( type, entrypoint )
     *
     * issues REST call on [entrypoint], and parses the returned XML by way of [type]
     * into a similarly named [type] object.  Handy for making code more brief, and 
     * easier to understand.
     *
     * 130215.rlynch
     * */
    private Object retrieveUniversal( String type, String entrypoint ) throws VEMAException
	{
		String xml = null;
		try
		{
            log.debug( "restBox.getXML( " + entrypoint + " ) ...call" );
			xml = restBox.getXML( entrypoint );
            // log.info( "restBox.getXML( " + entrypoint + " ) ...returned" );
            // log.debug( "XML = \n'" + xml + "'" );
			// -------------------------------------------------------
			//			System.setProperty( "jaxb.debug", "true" );
			// -------------------------------------------------------
			JAXBContext context = JAXBContext.newInstance( API.class );
			Unmarshaller     um = context.createUnmarshaller();

			// -------------------------------------------------------
			// below recommended (along with "jaxb.debug" above) for debugging.
			// ... it really didn't do anything to help cure the problem.
			// -------------------------------------------------------
			// um.setEventHandler(  new javax.xml.bind.helpers.DefaultValidationEventHandler() );

			// ----------------------------------------------------------------------------
			// the code below does NOT work.  Yet, it is the way recommended in literature
			// ----------------------------------------------------------------------------
			// API api = (API) um.unmarshal( new StreamSource( new StringReader( foo )));
            
            StreamSource ss = new StreamSource( new StringReader( xml ) );

            /* ---------------------------------------------------- */
            /* LEVEL 0 - the base level, which must always respond! */
            /* ---------------------------------------------------- */
            if     ( type.equals( "API"            )) return um.unmarshal( ss, API           .class ).getValue();

            /* ---------------------------------------------------- */
            /* LEVEL 1 - are stored in class variables for otherwise use */
            /* ---------------------------------------------------- */
            else if( type.equals( "Hosts"          )) return um.unmarshal( ss, Hosts         .class ).getValue();
            else if( type.equals( "VMs"            )) return um.unmarshal( ss, VMs           .class ).getValue(); 
            else if( type.equals( "StorageDomains" )) return um.unmarshal( ss, StorageDomains.class ).getValue(); 
            else if( type.equals( "Networks"       )) return um.unmarshal( ss, Networks      .class ).getValue(); 
            else if( type.equals( "Groups"         )) return um.unmarshal( ss, Groups        .class ).getValue(); 
            else if( type.equals( "Disks"          )) return um.unmarshal( ss, Disks         .class ).getValue(); 
            else if( type.equals( "DataCenters"    )) return um.unmarshal( ss, DataCenters   .class ).getValue(); 
            else if( type.equals( "Clusters"       )) return um.unmarshal( ss, Clusters      .class ).getValue(); 

            /* ---------------------------------------------------- */
            /* LEVEL 2 = no specific variables in the object to set */
            /* ---------------------------------------------------- */
            else if( type.equals( "Action"         )) return um.unmarshal( ss, Action        .class ).getValue();
            else if( type.equals( "Cluster"        )) return um.unmarshal( ss, Cluster       .class ).getValue();
            else if( type.equals( "CPU"            )) return um.unmarshal( ss, CPU           .class ).getValue();
            else if( type.equals( "Cluster"        )) return um.unmarshal( ss, Cluster       .class ).getValue();
            else if( type.equals( "DataCenter"     )) return um.unmarshal( ss, DataCenter    .class ).getValue();
            else if( type.equals( "Disk"           )) return um.unmarshal( ss, Disk          .class ).getValue();
            else if( type.equals( "Domain"         )) return um.unmarshal( ss, Domain        .class ).getValue();
            else if( type.equals( "Group"          )) return um.unmarshal( ss, Group         .class ).getValue();
            else if( type.equals( "Host"           )) return um.unmarshal( ss, Host          .class ).getValue();
            else if( type.equals( "HostStorage"    )) return um.unmarshal( ss, HostStorage   .class ).getValue();
            else if( type.equals( "HostNics"       )) return um.unmarshal( ss, HostNics      .class ).getValue();
            else if( type.equals( "Network"        )) return um.unmarshal( ss, Network       .class ).getValue();
            else if( type.equals( "NIC"            )) return um.unmarshal( ss, NIC           .class ).getValue();
            else if( type.equals( "Nics"           )) return um.unmarshal( ss, Nics          .class ).getValue();
            else if( type.equals( "Statistic"      )) return um.unmarshal( ss, Statistic     .class ).getValue();
            else if( type.equals( "Statistics"     )) return um.unmarshal( ss, Statistics    .class ).getValue();
            else if( type.equals( "Status"         )) return um.unmarshal( ss, Status        .class ).getValue();
            else if( type.equals( "Storage"        )) return um.unmarshal( ss, Storage       .class ).getValue();
            else if( type.equals( "StorageDomain"  )) return um.unmarshal( ss, StorageDomain .class ).getValue();
            else if( type.equals( "User"           )) return um.unmarshal( ss, User          .class ).getValue();
            else if( type.equals( "Template"       )) return um.unmarshal( ss, Template      .class ).getValue();
            else if( type.equals( "Value"          )) return um.unmarshal( ss, Value         .class ).getValue();
            else if( type.equals( "Values"         )) return um.unmarshal( ss, Values        .class ).getValue();
            else if( type.equals( "Version"        )) return um.unmarshal( ss, Version       .class ).getValue();
            else if( type.equals( "VM"             )) return um.unmarshal( ss, VM            .class ).getValue();
            else if( type.equals( "VmPool"         )) return um.unmarshal( ss, VmPool        .class ).getValue();
            else if( type.equals( "VmPools"        )) return um.unmarshal( ss, VmPools       .class ).getValue();
            else if( type.equals( "VmSummary"      )) return um.unmarshal( ss, VmSummary     .class ).getValue();
            else if( type.equals( "VmTypes"        )) return um.unmarshal( ss, VmTypes       .class ).getValue();

            else 
                throw new VEMAException( String.format( "retrieveUniversal( %s, %s ) args unknown", type, entrypoint ));
		}
		catch( javax.xml.bind.UnmarshalException exc )
		{
			log.error( "\nUnMarshalException (data to help figure out why:)\n"
					+ "Message:   '" + exc.getMessage()          + "'\n"
					+ "String:    '" + exc.toString()            + "'\n"
					+ "LocMssg:   '" + exc.getLocalizedMessage() + "'\n"
					+ "ErrorCode: '" + exc.getErrorCode()        + "'\n"
					+ "EntryPoint:'" + entrypoint                + "'\n"
					+ "XML:\n"       + xml                       + "\n" 
					);
		}
		catch( Exception exc )
		{
            log.error( "restBox.getXML( " + entrypoint + " ) ...call may have failed" );
			log.error( "stack trace: (failed connection) getMessage("
					+ exc.getMessage()          + ") getLocalizedMessage("
					+ exc.getLocalizedMessage() + ") toString("
					+ exc.toString()            + ")"
					);
			exc.printStackTrace();
		}
		return( null );  // must throw an object type of null... should never get here.
	}

	private API retrieveAPI( String entrypoint ) throws VEMAException
	{
        API api = null;
        if(( api = (API) retrieveUniversal( "API", (entrypoint == null ? "" : entrypoint) )) == null )
            throw new VEMAException( "retrieveUniversal( API ) call failed" );
        return api;
	}

	private Hosts retrieveHosts( String entrypoint ) throws VEMAException
	{
        Hosts hosts = null;
        if((hosts = (Hosts) retrieveUniversal( "Hosts", (entrypoint == null ? "" : entrypoint) )) == null )
            throw new VEMAException( "Couldn't retrieve Hosts" );
        for( Host host : hosts.getHosts() )
            i2n.put( zapnull( host.getId() ), zapnull( host.getName() ) );
        return hosts;
    }

	private Statistics retrieveStatistics( String entrypoint ) throws VEMAException
	{
        Statistics statistics = null;
        if((statistics = (Statistics) retrieveUniversal( "Statistics", (entrypoint == null ? "" : entrypoint) )) == null )
            throw new VEMAException( "Couldn't retrieve Statistics" );
        return statistics;
    }

	private VMs retrieveVMs( String entrypoint ) throws VEMAException
	{
        VMs vms = null;
        if((vms = (VMs) retrieveUniversal( "VMs", (entrypoint == null ? "" : entrypoint) )) == null )
            throw new VEMAException( "Couldn't retrieve VMs" );
        for( VM vm : vms.getVMs() )
            i2n.put( zapnull( vm.getId() ), zapnull( vm.getName() ) );
        return vms;
    }

	private StorageDomains retrieveStorageDomains( String entrypoint ) throws VEMAException
	{
        StorageDomains sd = null;
        if((sd = (StorageDomains) retrieveUniversal( "StorageDomains", (entrypoint == null ? "" : entrypoint) )) == null )
            throw new VEMAException( "Couldn't retrieve StorageDomains" );
        for( StorageDomain storagedomain : sd.getStorageDomains() )
            i2n.put( zapnull( storagedomain.getId() ), zapnull( storagedomain.getName() ) );
        return sd;
    }

	private Networks retrieveNetworks( String entrypoint ) throws VEMAException
	{
        Networks net = null;
        if((net = (Networks) retrieveUniversal( "Networks", (entrypoint == null ? "" : entrypoint) )) == null )
            throw new VEMAException( "Couldn't retrieve Networks" );
        for( Network network : net.getNetworks() )
            i2n.put( zapnull( network.getId() ), zapnull( network.getName() ) );
        return net;
    }

	private Groups retrieveGroups( String entrypoint ) throws VEMAException
	{
        Groups groups = null;
        if((groups = (Groups) retrieveUniversal( "Groups", (entrypoint == null ? "" : entrypoint) )) == null )
            throw new VEMAException( "Couldn't retrieve Groups" );
        for( Group group : groups.getGroups() )
            i2n.put( zapnull( group.getId() ), zapnull( group.getName() ) );
        return groups;
    }

	private Disks retrieveDisks( String entrypoint ) throws VEMAException
	{
        Disks disks = null;
        if((disks = (Disks)retrieveUniversal( "Disks", (entrypoint == null ? "" : entrypoint) )) == null )
            throw new VEMAException( "Couldn't retrieve Disks" );
        for( Disk disk : disks.getDisks() )
            i2n.put( zapnull( disk.getId() ), zapnull( disk.getName() ) );
        return disks;
    }

	private Storage retrieveStorage( String entrypoint ) throws VEMAException
	{
        Storage storage = null;
        if((storage = (Storage)retrieveUniversal( "Storage", (entrypoint == null ? "" : entrypoint) )) == null )
            throw new VEMAException( "Couldn't retrieve Storage" );
        return storage;
    }

	private HostStorage retrieveHostStorage( String entrypoint ) throws VEMAException
	{
        HostStorage hostStorage = null;
        if((hostStorage = (HostStorage)retrieveUniversal( "HostStorage", (entrypoint == null ? "" : entrypoint) )) == null )
            throw new VEMAException( "Couldn't retrieve HostStorage" );
        return hostStorage;
    }

	private Nics retrieveNics( String entrypoint ) throws VEMAException
	{
        Nics nics = null;
        if((nics = (Nics)retrieveUniversal( "Nics", (entrypoint == null ? "" : entrypoint) )) == null )
            throw new VEMAException( "Couldn't retrieve Nics" );
        for( NIC nic : nics.getNics() )
            i2n.put( zapnull( nic.getId() ), zapnull( nic.getName() ) );
        return nics;
    }

	private HostNics retrieveHostNics( String entrypoint ) throws VEMAException
	{
        HostNics hostnics = null;
        if((hostnics = (HostNics)retrieveUniversal( "HostNics", (entrypoint == null ? "" : entrypoint) )) == null )
            throw new VEMAException( "Couldn't retrieve HostNics" );
        for( HostNIC hostnic : hostnics.getHostNics() )
            i2n.put( zapnull( hostnic.getId() ), zapnull( hostnic.getName() ) );
        return hostnics;
    }

	private DataCenters retrieveDataCenters( String entrypoint ) throws VEMAException
	{
        DataCenters dcs = null;
        if((dcs = (DataCenters) retrieveUniversal( "DataCenters", (entrypoint == null ? "" : entrypoint) )) == null )
            throw new VEMAException( "Couldn't retrieve DataCenters" );
        for( DataCenter dc : dcs.getDataCenters() )
            i2n.put( zapnull( dc.getId() ), zapnull( dc.getName() ) );
        return dcs;
    }

	private VmPools retrieveVmPools( String entrypoint ) throws VEMAException
	{
        VmPools vmpools = null;
        if((vmpools = (VmPools) retrieveUniversal( "VmPools", (entrypoint == null ? "" : entrypoint) )) == null )
            throw new VEMAException( "Couldn't retrieve VmPools" );
        for( VmPool vmp : vmpools.getVmPools() )
            i2n.put( zapnull( vmp.getId() ), zapnull( vmp.getName() ) );
        return vmpools;
    }

	private Clusters retrieveClusters( String entrypoint ) throws VEMAException
	{
        Clusters clus = null;
        if((clus = (Clusters) retrieveUniversal( "Clusters", (entrypoint == null ? "" : entrypoint) )) == null )
            throw new VEMAException( "Couldn't retrieve Clusters" );
        for( Cluster clu : clus.getClusters() )
            i2n.put( zapnull( clu.getId() ), zapnull( clu.getName() ) );
        return clus;
    }

    public ConcurrentHashMap<String, VemaBaseObject> getHostAndVM( ConcurrentHashMap< String, VemaBaseHost> hostTree )
    {
    	    ConcurrentHashMap<String, VemaBaseObject> result = 
    	new ConcurrentHashMap<String, VemaBaseObject>();
    	
    	if( hostTree == null )
    		return result;  // the empty list.
    	
    	for( String hostkey : hostTree.keySet() )
    	{
    		VemaBaseHost   hosto = hostTree.get( hostkey );
    		String      hostname = hosto.getHostName();
    		String hostGroupName = hosto.getHostGroup();
    		
    		result.put(hostname, 
    				new VemaBaseObject( 
    						hostname, 
    						VemaBaseObject.VemaObjectEnum.HOST, 
    						hosto, 
    						null ));

    		for( String vmkey : hosto.getVMPool().keySet() )
    		{
    			VemaBaseVM vmo = hosto.getVM( vmkey );
    			String  vmname = vmo.getVMName();
    			result.put(vmname, 
    					new VemaBaseObject( 
    							vmname, 
    							VemaBaseObject.VemaObjectEnum.VM, 
    							null, 
    							vmo ));
    		}
    	}
    	// return result.size() == 0 ? null : result;  // using NULL as hard flag
    	return result;
    }
    
    /**
     * print out the vema-object internal variables that are
     * of significant importance in debugging.  
     * 
     * @return
     */
    public String getInternals()
    {
    	StringBuffer output = new StringBuffer( 5000 );
    	aPad a = new aPad();

    	if( restBox == null )
    	{
        	output.append(a.Pad("]", "Vema URL   [ " + "(undef)" + " ]\n"));
            output.append(a.Pad("]", "Vema Login [ " + "(undef)" + " ]\n"));
            output.append(a.Pad("]", "Vema Passwd[ " + "(undef)" + " ]\n"));
    	}
    	else
    	{
        	output.append(a.Pad("]", "restBox.URL   [ " + restBox.getHost()  + " ]\n"));
            output.append(a.Pad("]", "restBox.Login [ " + restBox.getLogin() + " ]\n"));
            output.append(a.Pad("]", "restBox.Passwd[ " + "*******"          + " ]\n"));
    	}
        output.append(a.Pad("]", "Vema vmSeed[ " + vemaVM + " ]\n"));
        output.append(a.Pad("]", "Vema STATE [ " + vemaState + " ]\n"));

    	if( hostFilters != null )
        	for( String filter : hostFilters )
        		output.append(a.Pad("]", "host Filter[ " + filter + " ]\n"));
        
        if( VMFilters != null )
        	for( String filter : VMFilters )
        		output.append(a.Pad("]", "VM   Filter[ " + filter + " ]\n"));

    	return output.toString();
    }

    private String zapnull( String value )
    {
        return value == null ? "" : value;
    }

    private String i2nGet( String key )
    {
        if( key            == null ) return "(null)";
        if( key.isEmpty()          ) return "";
        if( i2n.get( key ) == null ) return "";
                                     return i2n.get( key );
    }

    public  ConcurrentHashMap<String, VemaBaseHost> getListHost( 
    		ConcurrentHashMap<String, VemaBaseHost> priorVBH, 
    		List<VemaBaseQuery>hostQueries, 
    		List<VemaBaseQuery>vmQueries )
    {
    	return getListHost( priorVBH, hostQueries, vmQueries, true, true, true );
    }

    public  ConcurrentHashMap<String, VemaBaseHost> getListHost( 
    		ConcurrentHashMap<String, VemaBaseHost> priorVBH, 
    		List<VemaBaseQuery>                     hostQueries, 
    		List<VemaBaseQuery>                     vmQueries, 
    		boolean                                 onlyRequestedMetrics,
    		boolean                                 crushMetricsIfDown,
    		boolean                                 deleteDefunctMembers )
    {
        ConcurrentHashMap<String, VemaBaseHost>   hostPool = new ConcurrentHashMap<String, VemaBaseHost>();
        ConcurrentHashMap<String, VemaRhevVM>       vmPool = new ConcurrentHashMap<String, VemaRhevVM>();
        ConcurrentHashMap<String, VemaBaseQuery> queryPool = new ConcurrentHashMap<String, VemaBaseQuery>();
        
        // log.info("debug 1");
        long startTime = System.currentTimeMillis();  // capture "now"
        StringBuffer timeStamp = new StringBuffer(100);
        
        if( priorVBH == null )	// safety check BEFORE any return()
        	priorVBH = new ConcurrentHashMap<String, VemaBaseHost>();

        if( vemaState != ConnectionState.CONNECTED )
        {
            log.error( "getListHost(): not connected" );
            return priorVBH;
        }

		VemaRhevVM dummyVM = new VemaRhevVM("dummy");

        for( VemaBaseQuery accessor : dummyVM.getDefaultMetricList() )
            queryPool.put( accessor.getQuery(), accessor);
        
        for( VemaBaseQuery accessor : dummyVM.getDefaultConfigList() )
            queryPool.put( accessor.getQuery(), accessor);
        
        for( VemaBaseQuery accessor : dummyVM.getDefaultSyntheticList() )
            queryPool.put( accessor.getQuery(), accessor);
        
        if( vmQueries != null )
            for( VemaBaseQuery accessor : vmQueries ) // LAST to OVERRIDE the above defaults
                queryPool.put( accessor.getQuery(), accessor);
        
        // log.info("debug 2");

        VemaRhevHost dummyHost = new VemaRhevHost("dummy");
        
        for( VemaBaseQuery accessor : dummyHost.getDefaultMetricList() )
            queryPool.put( accessor.getQuery(), accessor);
        
        for( VemaBaseQuery accessor : dummyHost.getDefaultConfigList() )
            queryPool.put( accessor.getQuery(), accessor);
        
        for( VemaBaseQuery accessor : dummyHost.getDefaultSyntheticList() )
            queryPool.put( accessor.getQuery(), accessor);
        
        if( hostQueries != null )
            for( VemaBaseQuery accessor : hostQueries ) // LAST to ensure OVERRIDE capability.
                queryPool.put( accessor.getQuery(), accessor);

        timeStamp.append( "vmcall(" 
				+ Double.toString( (System.currentTimeMillis() - startTime) / 1000.0 ) + ") ");

        // log.info("debug 3");
        // ------------------------------------------------------------
        // Basic method: go get REST API tree of data
        // ... decode it into our HOSTLIST structure
        // ... patch together all the symoblic references
        // ... drop it out to the bottom clean-up code.
        // ------------------------------------------------------------

        getAndCompileREST();  // DOES ALL THE WORK!

        // log.info("debug 4");
        if( rhevMap              == null 
        ||  rhevMap.get( VMS   ) == null 
        ||  rhevMap.get( HOSTS ) == null )
        {
            throw new VEMAException( "GetListHost : no/incomplete data in RHEV map object." );
        }

// VMS VMS VMS VMS VMS VMS VMS VMS VMS VMS VMS VMS
// _______________________________________________
//      __      ____  __     
//      \ \    / /  \/  |    
//       \ \  / /| \  / |___ 
//        \ \/ / | |\/| / __|
//         \  /  | |  | \__ \
//          \/   |_|  |_|___/
// _______________________________________________

        String    category = VMS;
        StringBuilder    s = new StringBuilder();
        s.append( "GetListHost(), building " + category + ":\n" );

        // process all the VMs
        for( String element : rhevMap.get( category ).keySet() )
        {
            s.append( "bing: " + element + "\n" );
        	ConcurrentHashMap<String, String> 
        		        server = rhevMap.get(category).get( element );

            String name        = zapnull( server.get( "name"         ));
            String description = zapnull( server.get( "description"  ));
            String textDate    = zapnull( server.get( "start_time"   ));
            String macaddress  = zapnull( server.get( "nic[0].mac"   ));
            String ipaddress   = zapnull( server.get( "ip[0]"        ));
            String clustername = zapnull( server.get( "cluster.name" ));
            String hostname    = zapnull( server.get( "host.name"    ));
            String vmpoolname  = zapnull( server.get( "vmpool.name"  ));
            String statusstate = zapnull( server.get( "status.state" ));

            if( log.isDebugEnabled() )
                log.debug( ""
                + "element          = '" + element     + "'\n"
                + "vmo.name         = '" + name        + "'\n"
                + "vmo.description  = '" + description + "'\n"
                + "vmo.textDate     = '" + textDate    + "'\n"
                + "vmo.macaddress   = '" + macaddress  + "'\n"
                + "vmo.ipaddress    = '" + ipaddress   + "'\n"
                + "vmo.clustername  = '" + clustername + "'\n"
                + "vmo.hostname     = '" + hostname    + "'\n"
                + "vmo.vmpoolname   = '" + vmpoolname  + "'\n"
                + "vmo.statusstate  = '" + statusstate + "'\n"
                );

            VemaRhevVM     vmo = new VemaRhevVM( name );

            vmPool.put( name, vmo );  // kind of the cart before the horse, but objects are smart
            
            vmo.setBootDate  ( textDate, VemaConstants.rhevDateFormat  );
            vmo.setGuestState( statusstate );
            vmo.setLastUpdate( );
            vmo.setHostGroup ( clustername );
            vmo.setIpAddress ( ipaddress   );
            vmo.setMacAddress( macaddress  );
            vmo.setHypervisor( hostname    );
            vmo.setVMName    ( name        );
            vmo.setVmGroup   ( vmpoolname  );

            // log.info( textDate + " // " + vmo.getBootDate() + " // " + vmo.getLastUpdate() );

            for( String parameter : server.keySet() )
            {
                VemaBaseQuery  vbq = queryPool.get( parameter );
                if( vbq == null )  // if parameter not in (pool of queries to do)
                    continue;

                if( vbq.isGraphed() == false && vbq.isMonitored() == false )
                    continue;      // short-circuit for non-used parameters

                VemaBaseMetric vbm = new VemaBaseMetric(
                    parameter,
                    vbq.getWarning(),
                    vbq.getCritical(),
                    vbq.isGraphed(),
                    vbq.isMonitored()
                    );

                if(vbq.isTraced())
                    vbm.setTrace();

                String value = server.get( parameter );

                vbm.setValue( value );

                if(  parameter.endsWith( "value" )     // RHEV specific
                && ( parameter.startsWith( "stat." )   // RHEV specific
                ||   parameter.startsWith( "nic" )     // RHEV specific
                ))
                {
                    vmo.putMetric( parameter, vbm );
                }
                else
                {
                    vmo.putConfig( parameter, vbm );
                }

                s.append( String.format( "%-80s: %s\n", 
                    category + "/" + element + "/" + parameter, value ) );
            }

            for( String query : queryPool.keySet() )
            {
                VemaBaseSynthetic vbs;
                
                // we're ONLY working with the synthetics for vms... 
                if( !query.startsWith("syn.vm.") )
                    continue;  

                if(( vbs = dummyVM.getSynthetic( query )) != null )
                {
                    VemaBaseQuery  vbq = queryPool.get( query );

                    if( vbq == null )  // indicative of bigger problems...
                        continue;

                    VemaBaseMetric vbm = new VemaBaseMetric(
                            query,
                            vbq.getWarning(),
                            vbq.getCritical(),
                            vbq.isGraphed(),
                            vbq.isMonitored()
                            );

                    String value1 = vmo.getValueByKey( vbs.getLookup1() );
                    String value2 = vmo.getValueByKey( vbs.getLookup2() );

                    if( value1 == null )
                        logonce( "Couldn't find '" + vbs.getLookup1() + "' in VM metrics" );
                    
                    if( value2 == null )
                        logonce( "Couldn't find '" + vbs.getLookup2() + "' in VM metrics" );
                    
                    if(vbq.isTraced())
                        vbm.setTrace();

                    String result = String.valueOf(vbs.compute(value1, value2)) + "%";
                    vbm.setValue( result );
                    vmo.putMetric( query, vbm );
                    // log.info( "VM metric[ " + query + " ] = '" + result + "'" );
                }
                else
                {
                    logonce( "Couldn't find synthetic rule '" + query  + "'" );
                }
            }

            // Do NOT move this above the foregoing vmObj filler-code.  The 
            // MonitorState can only be computed once all the above is done.
            vmo.setRunState( vmo.getMonitorState() );
        }
        log.debug( s );

        // log.info("debug 5");
// HOSTS HOSTS HOSTS HOSTS HOSTS HOSTS HOSTS 
// _________________________________________
//       _    _  ____   _____ _______  
//      | |  | |/ __ \ / ____|__   __| 
//      | |__| | |  | | (___    | |___ 
//      |  __  | |  | |\___ \   | / __|
//      | |  | | |__| |____) |  | \__ \
//      |_|  |_|\____/|_____/   |_|___/
// _________________________________________
//                                       
        category = HOSTS;
        s = new StringBuilder();
        s.append( "GetListHost(), building " + category + ":\n" );
        for( String element : rhevMap.get( category ).keySet() )
        {
        	ConcurrentHashMap<String, String> 
        		        server = rhevMap.get(category).get( element );

            String active      = zapnull( server.get( "active"                        ));
            String ipaddress   = zapnull( server.get( "address"                       ));
            String clustername = zapnull( server.get( "cluster.name"                  ));
            String cpucores    = zapnull( server.get( "cpu.cores"                     ));
            String cpuname     = zapnull( server.get( "cpu.name"                      ));
            String cpuspeed    = zapnull( server.get( "cpu.speed"                     ));
            String maxmemory   = zapnull( server.get( "max_sched_memory"              ));
            String memory      = zapnull( server.get( "memory"                        ));
            String name        = zapnull( server.get( "name"                          ));
            String port        = zapnull( server.get( "port"                          ));

            String cpuidle     = zapnull( server.get( "stat.cpu.current.idle.value"   ));
            String cpusystem   = zapnull( server.get( "stat.cpu.current.system.value" ));
            String cpuuser     = zapnull( server.get( "stat.cpu.current.user.value"   ));
            String cpuload5m   = zapnull( server.get( "stat.cpu.load.avg.5m.value"    ));
            String ksmload     = zapnull( server.get( "stat.ksm.cpu.current.value"    ));

            String membuffers  = zapnull( server.get( "stat.memory.buffers.value"     ));
            String memcached   = zapnull( server.get( "stat.memory.cached.value"      ));
            String memfree     = zapnull( server.get( "stat.memory.free.value"        ));
            String memshared   = zapnull( server.get( "stat.memory.shared.value"      ));
            String memtotal    = zapnull( server.get( "stat.memory.total.value"       ));
            String memused     = zapnull( server.get( "stat.memory.used.value"        ));

            String swapfree    = zapnull( server.get( "stat.swap.free.value"          ));
            String swaptotal   = zapnull( server.get( "stat.swap.total.value"         ));
            String swapused    = zapnull( server.get( "stat.swap.used.value"          ));
            String swapcached  = zapnull( server.get( "stat.swap.cached.value"        ));

            String macaddress  = zapnull( server.get( "nic[0].mac"                    ));
            String description = zapnull( server.get( "description"                   ));

            String bootdate    = "";  // there is no "date" data in RHEV for the hosts...
            String lastupdate  = "";  // which is rather odd, but there you are.

            VemaRhevHost    ho = new VemaRhevHost( name );

            hostPool.put( name, ho );  // kind of the cart before the horse, but objects are smart
            
            ho.setBootDate   ( bootdate, VemaConstants.rhevDateFormat );
            ho.setHostGroup  ( clustername  );
            ho.setIpAddress  ( ipaddress    );
            ho.setMacAddress ( macaddress   );
            ho.setLastUpdate (              );    // computed, not in data...
            ho.setDescription( description  );

            for( String parameter : server.keySet() )
            {
                VemaBaseQuery  vbq = queryPool.get( parameter );
                if( vbq == null )  // if parameter not in (pool of queries to do)
                    continue;

                if( vbq.isGraphed() == false && vbq.isMonitored() == false )
                    continue;      // short-circuit for non-used parameters

                VemaBaseMetric vbm = new VemaBaseMetric(
                    parameter,
                    vbq.getWarning(),
                    vbq.getCritical(),
                    vbq.isGraphed(),
                    vbq.isMonitored()
                    );

                if(vbq.isTraced())
                    vbm.setTrace();

                String value = server.get( parameter );

                vbm.setValue( value );

                if(  parameter.endsWith( "value" )
                && ( parameter.startsWith( "stat." )
                ||   parameter.startsWith( "nic" )
                ))
                {
                    ho.putMetric( parameter, vbm );
                }
                else
                {
                    ho.putConfig( parameter, vbm );
                }

                s.append( String.format( "%-80s: %s\n", 
                    category + "/" + element + "/" + parameter, value ) );
            }

            for( String query : queryPool.keySet() )
            {
                VemaBaseSynthetic vbs;
                
                // we're ONLY working with the synthetics for HOSTS... 
                if( !query.startsWith("syn.host.") )
                    continue;  

                if(( vbs = dummyHost.getSynthetic( query )) != null )
                {
                    VemaBaseQuery  vbq = queryPool.get( query );

                    if( vbq == null )  // indicative of bigger problems...
                        continue;

                    VemaBaseMetric vbm = new VemaBaseMetric(
                            query,
                            vbq.getWarning(),
                            vbq.getCritical(),
                            vbq.isGraphed(),
                            vbq.isMonitored()
                            );

                    String value1 = ho.getValueByKey( vbs.getLookup1() );
                    String value2 = ho.getValueByKey( vbs.getLookup2() );
                    
                    if( value1 == null )
                        logonce( "Couldn't find '" + vbs.getLookup1() + "' in HOST metrics" );
                    
                    if( value2 == null )
                        logonce( "Couldn't find '" + vbs.getLookup2() + "' in HOST metrics" );
                    
                    if(vbq.isTraced())
                        vbm.setTrace();

                    String result = String.valueOf(vbs.compute(value1, value2)) + "%";
                    vbm.setValue( result );
                    ho.putMetric( query, vbm );
                    // log.info( "host metric[ " + query + " ] = '" + result + "'" );
                }
                else
                {
                    logonce( "Couldn't find synthetic rule '" + query  + "'" );
                }
            }
            ho.setRunState( ho.getMonitorState() );
        }
        log.debug( s );
        // log.info("debug 6");
        
        timeStamp.append( "premerge(" 
                + Double.toString( (System.currentTimeMillis() - startTime) / 1000.0 ) + ") ");

        
        // -------------------------------------------------
        // LINKS the VMs to HOSTs, one at a time
        // -------------------------------------------------
        StringBuilder sb = new StringBuilder();
        sb.append( "Linking VMs to Hosts\n" );
        log.debug("debug 6a");
        for( String vmoname : vmPool.keySet() )
        {
            VemaRhevVM      vmo = vmPool.get( vmoname );
            String vmHypervisor = vmo.getHypervisor();
            VemaBaseHost     ho;
            log.debug( "debug 6b: vm='" + vmoname + "' host='" + vmHypervisor + "'" );

            if( ( ho = hostPool.get( vmHypervisor               ) ) == null 
            &&  ( ho = hostPool.get( VemaConstants.HOSTLESS_VMS ) ) == null )
            {
                ho = new VemaRhevHost( VemaConstants.HOSTLESS_VMS );
                log.debug( "debug 6c: set up a HOSTLESS list, because host '" + vmHypervisor + "' not found" );
                ho.setRunState( ((VemaRhevHost)ho).getMonitorState() );
                hostPool.put( VemaConstants.HOSTLESS_VMS, ho );  // kind of the cart before the horse, but objects are smart
            }
            sb.append( String.format( "vm: '%-30s'  hy: '%-30s' --> '%s'\n", vmoname, vmHypervisor, ho.getHostName() ) );
            ho.putVM( vmoname, vmo );
        }
        sb.append( "--- END ---\n\n" );
        log.debug( sb.toString() );

        hListMerge( priorVBH, hostPool, deleteDefunctMembers );
        hostPool = null;   // explicitly kill off objects!
        
        // ----------------------------------------------------------------------
        // now clear out the metrics that the upper code doesn't want to monitor
        // ----------------------------------------------------------------------
        // The special "object.getMergeCount() > 1" case below INITIALLY suppresses
        // the metric-crushing logic.  Thus, on new HOSTs and VMs, when they first
        // appear in the tree, their metrics will be passed along, whatever their 
        // values.  (VIM25 values).  After that, on the 2nd and subsequent merge, 
        // the metrics will be suppressed IF the getRunState is either DOWN or SUSPEND
        // ... which lightens traffic substantially, and reduces server-side load as well.
        //
        // NOTE: this is "> 1" (instead of >= 1) because these tests are done POST-merging
        //       where merging increments the merge counter.  Thus the brand new objects
        //       will have a merge-count of exactly 1, here.
        // ----------------------------------------------------------------------
        
        if( onlyRequestedMetrics || crushMetricsIfDown )  // a STRIP OUT operation!
        {
            for( VemaBaseHost hosto : priorVBH.values() )
            {
                boolean crushHostMetrics = false;
                
                if( ( crushMetricsIfDown       ) 
                &&  ( hosto.getMergeCount() > 1)  // VERY useful special case (see above)
                &&  (  hosto.getRunState().contains( "DOWN" )
                    || hosto.getRunState().contains( "SUSPEND" )))
                    crushHostMetrics = true;

                for( String metricName : hosto.getMetricPool().keySet() )
                    if( crushHostMetrics || !hosto.getMetric( metricName ).isMonitored() )
                        hosto.getMetricPool().remove(metricName);
                
                for( String configName : hosto.getConfigPool().keySet() )
                    if( crushHostMetrics || !hosto.getConfig( configName ).isMonitored() )
                        hosto.getConfigPool().remove(configName);

                for( VemaBaseVM vmo : hosto.getVMPool().values() )
                {
                    boolean crushVMMetrics = false;
                    
                    if( ( crushMetricsIfDown      ) &&
                        ( vmo.getMergeCount() > 1 ) &&  // VERY useful special case (see above)
                       (  vmo.getRunState().contains( "DOWN" )
                       || vmo.getRunState().contains( "SUSPEND" )
                      ))
                    {
                        crushVMMetrics = true;
                    }

                    for( String metricName : vmo.getMetricPool().keySet() )
                        if( crushVMMetrics || !vmo.getMetric( metricName ).isMonitored() )
                            vmo.getMetricPool().remove(metricName);

                    for( String configName : vmo.getConfigPool().keySet() )
                        if( crushVMMetrics || !vmo.getConfig( configName ).isMonitored() )
                            vmo.getConfigPool().remove(configName);
                }
            }
        }

        timeStamp.append( "end(" 
				+ Double.toString( (System.currentTimeMillis() - startTime) / 1000.0 ) + ") ");

		log.debug( "getListHost(" + ( restBox == null ? "(undef)" : restBox.getHost() ) + ") timestamps: [" + timeStamp.toString() + "]");
        // return priorVBH.size() == 0 ? null : priorVBH;   // using NULL as flag
		return priorVBH;   // which must never be null by this point in code.
    }

    private void hListMerge( 
        ConcurrentHashMap<String, VemaBaseHost> baseList, 
        ConcurrentHashMap<String, VemaBaseHost> newList, 
        boolean autoDelete )
    {
    	// NOTE: this "merges new into base" conceptually.
    	
    	// FIRST... clear out all the values. 
    	//                ------------------------------------------------------------
    	// 120910.rlynch: the PROBLEM with this chunk of code is that it establishes
    	//                null values in the metrics ... which doesn't sound bad, but
    	//                messes up the STATES for transition detection.  Don't do it!
    	//                ------------------------------------------------------------
//    	for( String host : baseList.keySet() )
//    	{
//    		VemaBaseHost hostObj = baseList.get(host);
//    		for( String hostMetric : hostObj.getMetricPool().keySet())
//    			hostObj.getMetric(hostMetric).setValue(null);
//
//    		for( String hostConfig : hostObj.getConfigPool().keySet())
//    			hostObj.getConfig(hostConfig).setValue(null);
//
//    		for( String vm : hostObj.getVMPool().keySet() )
//    		{
//    			VemaBaseVM vmObj = hostObj.getVM( vm );
//    			for( String vmMetric : vmObj.getMetricPool().keySet() )
//        			vmObj.getMetric(vmMetric).setValue(null);
//
//        		for( String vmConfig : vmObj.getConfigPool().keySet())
//        			vmObj.getConfig(vmConfig).setValue(null);
//    		}
//    	}
    	
    	// set up SKIP counters... to DETECT dropped/moved hosts & vms.
        // log.info( "HostListMerge 1" );
    	for( VemaBaseHost hosto : newList.values() )
    	{
    		hosto.incSkipped();
    		for( VemaBaseVM vmo : hosto.getVMPool().values() )
    			vmo.incSkipped();
    	}
    	
        // log.info( "HostListMerge 2" );
    	int added = 0;
    	// now try to merge in the newstuff...
    	for( String host : newList.keySet() )
    	{
    		// begin by ensuring target object exists, for hosts.
            // log.info( "HostListMerge 2a" );
    		if( !baseList.containsKey(host) )  // if BASE LIST doesn't have the host
    		{
    			added++;
    			baseList.put( host, new VemaRhevHost(host) );
    		}

            // log.info( "HostListMerge 2x" );
    		// and target object (virtual machines) exists, too.
    		for( String vm : newList.get(host).getVMPool().keySet() )
    		{
                // log.info( "HostListMerge 2b" );
    			if( !baseList.get(host).getVMPool().containsKey(vm) )
    			{
    				added++;
    				baseList.get(host).getVMPool().put( vm, new VemaRhevVM(vm));
    			}
                // log.info( "HostListMerge 2c" );
    		}
    		// now merge them
    		baseList.get(host).mergeInNew( newList.get(host) );
            // log.info( "HostListMerge 2z" );
    	}
    	
        // log.info( "HostListMerge 3" );
    	// DELETION OF ORPHANED OBJECTS HERE  // This code deletes 'em.
		int deleted     = 0;
		int vmcounter   = 0;
		int hostcounter = 0;
		
    	for( String host : baseList.keySet() )
    	{
    		hostcounter++;
            // log.info( "HostListMerge 3a host='" + host + "'" );
    		for( String vm : baseList.get( host ).getVMPool().keySet() )
    		{
    			vmcounter++;
                // log.info( "HostListMerge 3b vm  ='" + vm + "'" );
    			if( autoDelete
    			&&  baseList.get( host ).getVM( vm ).isStale( minSkippedBeforeDelete, minTimeBeforeDelete ))
    			{
                    // 130502.rlynch: CONFIRMED is working right.  No duplication of VMs between merges.
    				baseList.get( host ).getVMPool().remove( vm );
    				deleted++;
    				log.info( "'" + vm + "'... orphaned VM deleted (min#:" + minSkippedBeforeDelete + " minT:" + minTimeBeforeDelete + ")" );
    			}
    		}
            // log.info( "HostListMerge 3c" );
    		if( autoDelete
    		&&  baseList.get( host ).isStale( minSkippedBeforeDelete, minTimeBeforeDelete ))
    		{
    			baseList.remove( host );
    			deleted++;
				log.debug( "'" + host + "'... orphaned HOST deleted (min#:" + minSkippedBeforeDelete + " minT:" + minTimeBeforeDelete + ")" );
    		}
            // log.info( "HostListMerge 3d" );
    	}
    	log.info( "Hypervisors: (" + hostcounter 
    			+ "),  VMs: ("       + vmcounter 
    			+ "),  Added: ("     + added 
    			+ "),  Deleted: ("   + deleted 
    			+ ")" );
    }
    
    public ArrayList<VemaBaseObject> getDefunctList( ConcurrentHashMap<String, VemaBaseHost> hostTree )
    {
    	ArrayList<VemaBaseObject> out = new ArrayList<VemaBaseObject>();
    	
    	for( String host : hostTree.keySet() )
    	{
    		for( String vm : hostTree.get( host ).getVMPool().keySet() )
    			if( hostTree.get( host ).getVM( vm ).isStale( minSkippedBeforeDefunct, minTimeBeforeDefunct ))
    			{
    				out.add( new VemaBaseObject( 
    						vm, 
    						VemaBaseObject.VemaObjectEnum.VM, 
    						null, 
    						hostTree.get( host ).getVM(vm)));
    				log.debug( vm + "... orphaned VM" );
    			}
    		if( hostTree.get( host ).isStale( minSkippedBeforeDefunct, minTimeBeforeDefunct ))
    		{
    			out.add( new VemaBaseObject( 
    					host, 
    					VemaBaseObject.VemaObjectEnum.HOST, 
    					hostTree.get( host ), 
    					null));
    			
    			log.debug( host + "... orphaned HOST" );
    		}
    	}
    	return out;
    }

	private static class TrustAllTrustManager implements
			javax.net.ssl.TrustManager,
			javax.net.ssl.X509TrustManager
	{
		public java.security.cert.X509Certificate[] getAcceptedIssuers()
		{
			return null;
		}

		public boolean isServerTrusted( java.security.cert.X509Certificate[] certs )
		{
			return true;
		}

		public boolean isClientTrusted( java.security.cert.X509Certificate[] certs )
		{
			return true;
		}

		public void checkServerTrusted(
				java.security.cert.X509Certificate[] certs,
				String authType )
				throws java.security.cert.CertificateException
		{
			return;
		}

		public void checkClientTrusted(
				java.security.cert.X509Certificate[] certs,
				String authType )
				throws java.security.cert.CertificateException
		{
			return;
		}
	}

	private static void trustAllHttpsCertificates() throws Exception
	{
		// Create a trust manager that does not validate certificate chains:
		javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
		javax.net.ssl.TrustManager              tm = new TrustAllTrustManager();
		trustAllCerts[0]                           = tm;
		javax.net.ssl.SSLContext                sc = javax.net.ssl.SSLContext.getInstance( "SSL" );
		javax.net.ssl.SSLSessionContext      sslsc = sc.getServerSessionContext();
		sslsc.setSessionTimeout( 0 );
		sc.init( null, trustAllCerts, null );
		javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(
				sc.getSocketFactory());
	}

	/**
	 * Establishes session with the virtual center server.
	 *
	 * @throws Exception
	 *             the exception
	 */
	public void attach() throws Exception       // this was connect() from the VIM25 example
	{
		HostnameVerifier hv = new HostnameVerifier()
		{
			public boolean verify( String urlHostName, SSLSession session )
			{
				return true;
			}
		};

		trustAllHttpsCertificates();
		HttpsURLConnection.setDefaultHostnameVerifier( hv );
	}

	/**
	 * Disconnects the user session.
	 *
	 * @throws Exception
	 */
	public void disconnect() throws VEMAException
	{
		if ( vemaState == ConnectionState.CONNECTED )
            vemaState = ConnectionState.DISCONNECTED;
	}

    public VemaBaseObjectTree collect( VemaBaseObjectTree previous )
    {
        return previous; // TODO PLACE HOLDER - this is where the actual "action" will go.
    }

	private static void printSoapFaultException( SOAPFaultException sfe )
	{
		//log.debug( "SOAP Fault -" );
		if ( sfe.getFault().hasDetail() )
			log.debug( sfe.getFault().getDetail().getFirstChild().getLocalName() );

		if ( sfe.getFault().getFaultString() != null )
			log.debug( "\n Message: " + sfe.getFault().getFaultString() );
	}

    private ConcurrentHashMap< String, Boolean >everSeen = new ConcurrentHashMap<String, Boolean>();
    private void logonce( String message )
    {
        if(everSeen.get( message ) == null)
        {
            everSeen.put( message, true );
            log.info( message );
        }
    }

}

