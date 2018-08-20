/*
 * Copyright 2012 GroundWork , Inc. ("GroundWork") 
 * All rights reserved. 
 */
package com.groundwork.agents.vema.configuration;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import com.groundwork.agents.vema.exception.VEMAConfigurationException;
import com.groundwork.agents.vema.api.VemaConstants;

public class VEMAGwosConfiguration
{
	/**
	 * Default constants to connect to the GroundWork Server
	 */
	private static String  DEFAULT_GWOS_PORT             = "4913";
	private static String  DEFAULT_GWOS_SERVER           = "";
	private static boolean DEFAULT_GWOS_SSL_ENABLED      = false;
	private static String  DEFAULT_WS_ENDPOINT_URI       = "/foundation-webapp/services";
	private static String  DEFAULT_WS_HOSTGROUP_ENDPOINT = "wshostgroup";
	private static String  DEFAULT_WS_HOST_ENDPOINT      = "wshost";
	private static String  DEFAULT_WS_USER               = "wsuser";
	private static String  DEFAULT_WS_PASSWORD           = "wsuser";
	private static String  DEFAULT_CONFIG_PATH           = VemaConstants.CONFIG_FILE_PATH;
	
    private static String  ORIGINAL_VMWARE_CONFIG_TYPE1  = VemaConstants.CONFIG_FILE_PATH 
                                                         + VemaConstants.VEMA_CONFIG_FILE
                                                         + VemaConstants.CONFIG_FILE_EXTN;

    private static String  ORIGINAL_VMWARE_CONFIG_TYPE2  = VemaConstants.CONFIG_FILE_PATH 
                                                         + VemaConstants.VEMA_CONFIG_CANONICAL
                                                         + VemaConstants.CONFIG_FILE_EXTN;

	private static String  DEFAULT_VMWARE_CONFIG_FILE    = VemaConstants.VMWARE_CONFIG_FILE;
	private static boolean DEFAULT_VMWARE_SSL_ENABLED    = true;
	private static String  DEFAULT_VMWARE_VSS_SERVER     = "";
	private static String  DEFAULT_VMWARE_URI            = "sdk";
	private static String  DEFAULT_VMWARE_USER           = "vmware-dev";
	private static String  DEFAULT_VMWARE_PASSWORD       = "";
	private static String  DEFAULT_VMWARE_REALM          = "";

	private static String  DEFAULT_RHEV_CONFIG_FILE      = VemaConstants.RHEV_CONFIG_FILE;
	private static boolean DEFAULT_RHEV_SSL_ENABLED      = true;
	private static String  DEFAULT_RHEV_VSS_SERVER       = "";
	private static String  DEFAULT_RHEV_URI              = "api";
	private static String  DEFAULT_RHEV_USER             = "admin";
	private static String  DEFAULT_RHEV_PASSWORD         = "";
	private static String  DEFAULT_RHEV_REALM            = "internal";

	private static String  TYPE_VMWARE                   = VemaConstants.CONNECTOR_VMWARE;
	private static String  TYPE_RHEV                     = VemaConstants.CONNECTOR_RHEV;
	
	private VemaConstants.VSystem vSystem                ; // not set

	private        String  gwosPort                      = null;
	private        String  gwosServer                    = null;
	private        boolean gwosSSLEnabled                = false;

	private        String  wsEndpoint                    = null;
	private        String  wsHostName                    = null;
	private        String  wsUser                        = null;
	private        String  wsPassword                    = null;

	private        String  pathToConfigurationFile       = null;
	private        String  configurationFile             = null;

	/* Virtual Environemnt management settings */
	private        boolean virtualEnvSSLEnabled          = true;  // nominally TRUE
	private        String  virtualEnvType                = null;
	private        String  virtualEnvServer              = null;
	private        String  virtualEnvURI                 = null;
	private        String  virtualEnvUser                = null;
	private        String  virtualEnvPassword            = null;
	private        String  virtualEnvRealm               = null;  // 130227.rlynch added
	private        String  virtualEnvPort                = null;  // 130227.rlynch added
	private        String  virtualEnvProtocol            = null;  // 130227.rlynch added
	private        String  certificateStore              = null;  // 130227.rlynch added
	private        String  certificatePassword           = null;  // 130227.rlynch added

	private        int     checkInterval                 = 5;      // minutes
    private        int     syncInterval                  = 2;      // minutes
    private        int     comaInterval                  = 15;     // minutes
	
	private        String  wsHostGroupName;
	
    // constructor method
	public VEMAGwosConfiguration() throws Exception
    {
		this.gwosPort                = DEFAULT_GWOS_PORT;
		this.gwosServer              = DEFAULT_GWOS_SERVER;
		this.gwosSSLEnabled          = DEFAULT_GWOS_SSL_ENABLED;
		this.wsEndpoint              = DEFAULT_WS_ENDPOINT_URI;
		this.wsUser                  = DEFAULT_WS_USER;
		this.wsPassword              = DEFAULT_WS_PASSWORD;
		
		this.wsHostName              = DEFAULT_WS_HOST_ENDPOINT;
		this.wsHostGroupName         = DEFAULT_WS_HOSTGROUP_ENDPOINT;
	}
	
//	----------------------------------------------------------------------------------
//	130426.rlynch:
//	DO NOT ENABLE - unless you can figure out why enabling this breaks the writing of 
//	XML files through JoxBeans.   It cost me MUCH time to figure out.  I have yet to 
//	know what about ENUMS causes the problem.  Basically... when this is included,
//	the JOXBeanOutputWriter dumps the entire XML schema for this application!  A bug.
//	----------------------------------------------------------------------------------
//	public VemaConstants.VSystem getVSystem()
//	{
//		return this.vSystem;
//	}
	
	public void setVSystem( VemaConstants.VSystem vSystem ) throws Exception
	{
		this.vSystem = vSystem;
		
		if( this.virtualEnvType != null)   // short circuit if already set
			return;
		
		switch( vSystem )
		{
		case VMWARE:
			if( this.configurationFile    == null ) this.configurationFile    = DEFAULT_VMWARE_CONFIG_FILE;
			if( this.virtualEnvServer     == null ) this.virtualEnvServer     = DEFAULT_VMWARE_VSS_SERVER;
			if( this.virtualEnvURI        == null ) this.virtualEnvURI        = DEFAULT_VMWARE_URI;
			if( this.virtualEnvUser       == null ) this.virtualEnvUser       = DEFAULT_VMWARE_USER;
			if( this.virtualEnvPassword   == null ) this.virtualEnvPassword   = DEFAULT_VMWARE_PASSWORD;
			if( this.virtualEnvRealm      == null ) this.virtualEnvRealm      = DEFAULT_VMWARE_REALM;
			break;
		case REDHAT:
			if( this.configurationFile    == null ) this.configurationFile    = DEFAULT_RHEV_CONFIG_FILE;
			if( this.virtualEnvServer     == null ) this.virtualEnvServer     = DEFAULT_RHEV_VSS_SERVER;
			if( this.virtualEnvURI        == null ) this.virtualEnvURI        = DEFAULT_RHEV_URI;
			if( this.virtualEnvUser       == null ) this.virtualEnvUser       = DEFAULT_RHEV_USER;
			if( this.virtualEnvPassword   == null ) this.virtualEnvPassword   = DEFAULT_RHEV_PASSWORD;
			if( this.virtualEnvRealm      == null ) this.virtualEnvRealm      = DEFAULT_RHEV_REALM;
			break;
		default:
			throw new Exception("technology must be [vmware|rhev]");
		}
	}

    // constructor method
	public VEMAGwosConfiguration(String gwosPort, String gwosServer,
			boolean gwosSSLEnabled, String wsEndpoint, String wsUser,
			String wsPassword)
    {
		this.gwosPort                = gwosPort;
		this.gwosServer              = gwosServer;
		this.gwosSSLEnabled          = gwosSSLEnabled;
		this.wsEndpoint              = wsEndpoint;
		this.wsUser                  = wsUser;
		this.wsPassword              = wsPassword;
	}

    // constructor method
	public VEMAGwosConfiguration(String pathToConfigurationFile) 
    {
		this.pathToConfigurationFile = pathToConfigurationFile;
		// setting the filename can cause problems.
//		this.configurationFile       = DEFAULT_CONFIG_FILE_NAME;
	}

    // constructor method
	public VEMAGwosConfiguration(String pathToConfigurationFile, String configurationFile) 
    {
		this.pathToConfigurationFile = pathToConfigurationFile;
		this.configurationFile       = configurationFile;
	}

	/**
	 * loadConfiguration Loads the configuration file (XML) defined in the
	 * constructor. Calling this method is only necessary if the configuration
	 * parameters were not defined in the constructor or by using the default
	 * constructor
	 * 
	 * @throws VEMAConfigurationException
	 */
	public void loadConfiguration() throws VEMAConfigurationException 
    {
	}

	/**
	 * getVirtualEnvURL
	 * 
	 * @return fully qualified URL to management server that can be used to
	 *         connect to management server
	 */
	public String getVirtualEnvURL() throws VEMAConfigurationException 
    {
		if( (this.virtualEnvType.compareToIgnoreCase(TYPE_VMWARE) == 0) 
        ||  (this.virtualEnvType.compareToIgnoreCase(TYPE_RHEV)   == 0) )
        {
			return (( this.virtualEnvSSLEnabled ? "https://" : "http://" )
                    + this.getVirtualEnvServer()
                    + "/"
                    + this.getVirtualEnvURI()
                   );
		} 
        else 
        {
			throw new VEMAConfigurationException(
					"Only VSphere Server/ESXI and RHEV is supported.");
		}
	}

	public String  getGwosPort()                { return gwosPort; } 
	public String  getGwosServer()              { return gwosServer; } 
	public boolean isGwosSSLEnabled()           { return gwosSSLEnabled; } 
	public String  getWsEndpoint()              { return wsEndpoint; } 
	public String  getWsUser()                  { return wsUser; } 
	public String  getWsPassword()              { return wsPassword; } 
	public String  getVirtualEnvType()          { return virtualEnvType; } 
	public boolean isVirtualEnvSSLEnabled()     { return virtualEnvSSLEnabled; } 
	public String  getVirtualEnvServer()        { return virtualEnvServer; } 
	public String  getVirtualEnvURI()           { return virtualEnvURI; } 
	public String  getVirtualEnvUser()          { return virtualEnvUser; } 
	public String  getVirtualEnvPassword()      { return virtualEnvPassword; } 
	public String  getVirtualEnvRealm()         { return virtualEnvRealm; } 
	public String  getVirtualEnvPort()          { return virtualEnvPort; } 
	public String  getVirtualEnvProtocol()      { return virtualEnvProtocol; } 
	public String  getCertificateStore()        { return certificateStore;   } 
	public String  getCertificatePassword()     { return certificatePassword;   } 
	public int     getCheckInterval()           { return checkInterval; } 
	public int     getSyncInterval()            { return syncInterval; } 
	public int     getComaInterval()            { return comaInterval; } 
	public String  getWsHostName()              { return wsHostName; } 
	public String  getWsHostGroupName()         { return wsHostGroupName; } 
	public String  getPathToConfigurationFile() { return pathToConfigurationFile; } 
	public String  getConfigurationFile()       { return configurationFile; } 

	public void setGwosPort(String port)                    { this.gwosPort                = port; } 
	public void setGwosServer(String server)                { this.gwosServer              = server; } 
	public void setGwosSSLEnabled(boolean sslEnabled)       { this.gwosSSLEnabled          = sslEnabled; } 
	public void setWsEndpoint(String endpoint)              { this.wsEndpoint              = endpoint; } 
	public void setWsUser(String user)                      { this.wsUser                  = user; } 
	public void setWsPassword(String password)              { this.wsPassword              = password; } 
	public void setVirtualEnvSSLEnabled(boolean vsslEnable) { this.virtualEnvSSLEnabled    = vsslEnable; } 
	public void setVirtualEnvServer(String vEnvServer)      { this.virtualEnvServer        = vEnvServer; } 
	public void setVirtualEnvURI(String vEnvURI)            { this.virtualEnvURI           = vEnvURI; } 
	public void setVirtualEnvUser(String vEnvUser)          { this.virtualEnvUser          = vEnvUser; } 
	public void setVirtualEnvPassword(String vEnvPassword)  { this.virtualEnvPassword      = vEnvPassword; } 
	public void setVirtualEnvRealm(String realm)            { this.virtualEnvRealm         = realm; }
	public void setVirtualEnvPort(String port)              { this.virtualEnvPort          = port; }
	public void setVirtualEnvProtocol(String protocol)      { this.virtualEnvProtocol      = protocol; }
	public void setCertificateStore(String certsfile)       { this.certificateStore        = certsfile; }
	public void setCertificatePassword(String certspass)    { this.certificatePassword     = certspass; }
	public void setCheckInterval(int checkInterval)         { this.checkInterval           = checkInterval; } 
	public void setSyncInterval(int syncInterval)           { this.syncInterval            = syncInterval; } 
	public void setComaInterval(int comaInterval)           { this.comaInterval            = comaInterval; } 
	public void setWsHostName(String wsHostName)            { this.wsHostName              = wsHostName; } 
	public void setWsHostGroupName(String wsHostGroupName)  { this.wsHostGroupName         = wsHostGroupName; }
	public void setPathToConfigurationFile( String path )   { this.pathToConfigurationFile = path; }
	public void setConfigurationFile( String file )         { this.configurationFile       = file; }

	public void setVirtualEnvType(String envtype) throws Exception
	{
		if( envtype.equalsIgnoreCase( TYPE_RHEV ))
			setVSystem( VemaConstants.VSystem.REDHAT );

		else if( envtype.equalsIgnoreCase( TYPE_VMWARE ))
			setVSystem( VemaConstants.VSystem.VMWARE );
		
//		else if( envtype.equalsIgnoreCase( TYPE_EC2 ))  // eventually
//			setVSystem( VemaConstants.VSystem.EC2 );
//		
		else
			throw new Exception("type '" + envtype + "' not in [vmware|rhev]");

		this.virtualEnvType = envtype;  // this MUST be at end!!!
	}
	
    public String formatSelf()
    {
        return formatSelf( this );
    }

	/* Private Helper methods for configuration */
    public String formatSelf( VEMAGwosConfiguration o )
    {
        StringBuilder s = new StringBuilder();

    	s.append( String.format( "%-40s: %s\n", "vSystem",                  o.vSystem ));

    	s.append( String.format( "%-40s: %s\n", "gwosPort",                 o.gwosPort ));
    	s.append( String.format( "%-40s: %s\n", "gwosServer",               o.gwosServer ));
    	s.append( String.format( "%-40s: %s\n", "gwosSSLEnabled",           o.gwosSSLEnabled ));

    	s.append( String.format( "%-40s: %s\n", "wsEndpoint",               o.wsEndpoint ));
    	s.append( String.format( "%-40s: %s\n", "wsHostName",               o.wsHostName ));
    	s.append( String.format( "%-40s: %s\n", "wsUser",                   o.wsUser ));
    	s.append( String.format( "%-40s: %s\n", "wsPassword",               o.wsPassword ));

    	s.append( String.format( "%-40s: %s\n", "pathToConfigurationFile",  o.pathToConfigurationFile ));
    	s.append( String.format( "%-40s: %s\n", "configurationFile",        o.configurationFile ));

    	s.append( String.format( "%-40s: %s\n", "virtualEnvSSLEnabled",     o.virtualEnvSSLEnabled ));
    	s.append( String.format( "%-40s: %s\n", "virtualEnvType",           o.virtualEnvType ));
    	s.append( String.format( "%-40s: %s\n", "virtualEnvServer",         o.virtualEnvServer ));
    	s.append( String.format( "%-40s: %s\n", "virtualEnvURI",            o.virtualEnvURI ));
    	s.append( String.format( "%-40s: %s\n", "virtualEnvUser",           o.virtualEnvUser ));
    	s.append( String.format( "%-40s: %s\n", "virtualEnvPassword",       o.virtualEnvPassword ));
    	s.append( String.format( "%-40s: %s\n", "virtualEnvRealm",          o.virtualEnvRealm ));
    	s.append( String.format( "%-40s: %s\n", "virtualEnvPort",           o.virtualEnvPort ));
    	s.append( String.format( "%-40s: %s\n", "virtualEnvProtocol",       o.virtualEnvProtocol ));

    	s.append( String.format( "%-40s: %s\n", "certificateStore",         o.certificateStore ));
    	s.append( String.format( "%-40s: %s\n", "certificatePassword",      o.certificatePassword ));

    	s.append( String.format( "%-40s: %d\n", "checkInterval",            o.checkInterval ));
        s.append( String.format( "%-40s: %d\n", "sync Interval",            o.syncInterval ));
        s.append( String.format( "%-40s: %d\n", "coma Interval",            o.comaInterval ));
    	
    	s.append( String.format( "%-40s: %s\n", "wsHostGroupName",          o.wsHostGroupName ));

        return s.toString();
    }
}
