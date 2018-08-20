/*
 * Copyright 2012 GroundWork Open Source, Inc. ("GroundWork") All rights
 * reserved. This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package com.groundwork.agents.vema.rhev.servlet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.reflect.InvocationTargetException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.rpc.ServiceException;

import org.apache.axis.message.SOAPHeaderElement;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.groundwork.foundation.ws.api.WSFoundationException;
import org.groundwork.foundation.ws.impl.HostSoapBindingStub;
import org.groundwork.foundation.ws.impl.WSHostServiceLocator;
import org.groundwork.foundation.ws.model.impl.Host;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;
import org.itgroundwork.foundation.joxbeans.VemaMonitoring;

import com.groundwork.agents.vema.api.VemaConstants;
import com.groundwork.agents.vema.base.VemaBaseHost;
import com.groundwork.agents.vema.base.VemaBaseQuery;
import com.groundwork.agents.vema.collector.impl.MonitorAgentCollectorService;
import com.groundwork.agents.vema.configuration.VEMAGwosConfiguration;
import com.groundwork.agents.vema.servlet.VemaBaseServlet;
import com.groundwork.agents.vema.utils.SharedSecretProtector;
import com.groundwork.agents.vema.rhev.connector.VemaRhev;
import com.groundwork.agents.vema.utils.ParamBox;
import com.wutka.jox.JOXBeanOutputStream;

import com.groundwork.agents.vema.rhev.restapi.*;

/**
 * Servlet implementation class RhevServlet
 */




public class VemaRhevServlet extends VemaBaseServlet
{
	private static final long				serialVersionUID	 = 1L;
    private static final String cloudHubVersion = "CloudHub for RHEV 1.0.2";

	VEMAGwosConfiguration					vGwosConfig			 = null;
    ParamBox                                parambox             = new ParamBox();
	VemaRhev								vema				 = null;

	private static org.apache.log4j.Logger	log					 = Logger.getLogger( VemaRhevServlet.class );

	/**
	 * Default constructor.
	 */
	public VemaRhevServlet()
	{
        // which has nothing to do
	}

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init( ServletConfig config ) throws ServletException
	{
		this.vema        = new VemaRhev();
		this.macService  = new MonitorAgentCollectorService();

        log.warn( "\n"
        + "---------------------------------------------------------------------------\n"
        + cloudHubVersion + "\n"
        + "---------------------------------------------------------------------------\n"
        + "\n"
        );

		try
		{
			super.init( 
					config, 
					vema, 
					macService,
					VemaConstants.RHEV_CONFIG_FILE,
					VemaConstants.RHEV_PROFILE_FILE,
					VemaConstants.HYPERVISOR_RHEV, 
					VemaConstants.CONNECTOR_RHEV, 
					VemaConstants.MGMT_SERVER_RHEV,
                    VemaConstants.APPLICATIONTYPE_RHEV
					);
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}

        // traceRhevConnect();
	}

	public void destroy()
	{
		super.destroy();
		log.warn("\nDeconstruct RHEV Virtualization service: NOW\n");

	}

    public void traceRhevConnect()
    {
		log.debug( "calling vemarhev.connect()" );
		try
		{
            parambox.put( "vema", "api", "fqhost",     "eng-rhev-m-1.groundwork.groundworkopensource.com" );
            parambox.put( "vema", "api", "user",       "admin" );
            parambox.put( "vema", "api", "password",   "#m3t30r1t3" );
            parambox.put( "vema", "api", "type",       "rhev" );
            parambox.put( "vema", "api", "baseuri",    "/api" );
            parambox.put( "vema", "api", "sslenabled", "on" );
            parambox.put( "vema", "api", "realm",      "internal" );
            parambox.put( "vema", "api", "certsfile",  "/usr/java/latest/jre/lib/security/cacerts" );
            parambox.put( "vema", "api", "certspass",  "changeit" );
            parambox.put( "vema", "api", "protocol",   "https" );
            parambox.put( "vema", "api", "port",       "443" );

			vema.connect( parambox );
            //	previously:
			//		"eng-rhev-m-1.groundwork.groundworkopensource.com",
			//		"admin", 
			//		"#m3t30r1t3", 
			//		"internal", 
			//		"443", 
			//		"https", 
			//		"/api",
			//		"/usr/java/latest/jre/lib/security/cacerts", 
			//		"changeit" 
			//		);

			log.debug( "vemarhev.getStateString() = '"
					+ vema.getStateString() + "'" );

			ConcurrentHashMap<String, VemaBaseHost> glh = null;

			glh = vema.getListHost( glh, null, null );

			log.debug( "Got past getListHost()" );
			log.debug( vema.formatGetListHost( glh ) );
		}
		catch( Exception e )
		{
			log.info( "cause:" + e.getLocalizedMessage() + " " );
			log.debug( "stack:" + "\n" + e.getStackTrace()[0].toString() + "\n"
					+ e.getStackTrace()[1].toString() + "\n"
					+ e.getStackTrace()[2].toString() + "\n"
					+ e.getStackTrace()[3].toString() + "\n"
					+ e.getStackTrace()[4].toString() + "\n"
					+ e.getStackTrace()[5].toString() + "\n"
					+ e.getStackTrace()[6].toString() + "\n"
					+ e.getStackTrace()[7].toString() );
		}
    }
	/**
	 * Creates the config files for vema stuff
	 */
	public void createConfigFiles( VemaMonitoring vemaBean, VEMAGwosConfiguration gwosBean )
	{
		try
		{
			// First back up the file

			File   f                  = null;  // generic "file object" for rename process
			String rhevMonitoringName = VemaConstants.CONFIG_FILE_PATH
					                  + VemaConstants.RHEV_PROFILE_FILE
                                      + VemaConstants.CONFIG_FILE_EXTN;

			if( (f = new File( rhevMonitoringName ) ) != null && f.exists() )
				f.renameTo( new File( rhevMonitoringName + "_old.backup" ) ); 

			FileOutputStream fileOutVema = new FileOutputStream( rhevMonitoringName );

			JOXBeanOutputStream joxOutVema = new JOXBeanOutputStream( fileOutVema, true );
			joxOutVema.writeObject( "vema-monitoring", vemaBean );

			String gwosFileName = VemaConstants.CONFIG_FILE_PATH
					            + VemaConstants.RHEV_CONFIG_FILE
                                + VemaConstants.CONFIG_FILE_EXTN ;

			// Just encrypt before storing it in the file
			String rawPassword = gwosBean.getVirtualEnvPassword();
			gwosBean.setVirtualEnvPassword( SharedSecretProtector.encrypt( rawPassword ) );

			if( (f = new File( gwosFileName )) != null && f.exists())    // ensure existing 
				f.renameTo( new File( gwosFileName + "_old.backup" ) );  // is backed up

			FileOutputStream fileOutGwos = new FileOutputStream( gwosFileName );

			JOXBeanOutputStream joxOutGwos = new JOXBeanOutputStream( fileOutGwos );
			joxOutGwos.writeObject( "vema", gwosBean );
		}
		catch( FileNotFoundException    fnfe ) 
        { 
            log.error( "FNF: " + fnfe.getMessage()  + " cause: (" + fnfe.getCause() + ")" );
            fnfe.printStackTrace(); 
        }
		catch( IOException              ioe  ) 
        { 
            log.error( "IOE: " + ioe.getMessage()  + " cause: (" + ioe.getCause() + ")" );
            ioe.printStackTrace(); 
        }
		catch( GeneralSecurityException gse  ) 
        { 
            log.error( "GSE: " + gse.getMessage()  + " cause: (" + gse.getCause() + ")" );
            gse.printStackTrace(); 
        }
		catch( Exception                e    ) 
        { 
            log.error( "EXC: " + e.getMessage() + " cause: (" + e.getCause() + ")" );
            e.printStackTrace(); 
        }
	}

	/**
	 * Tests the groundwork connection. Returns true if the connection is valid
	 * else returns false
	 */
	public boolean testGWOSConnection( HttpServletRequest request )
	{
		boolean status = false;
		VEMAGwosConfiguration configBean = this.xferReqValuesToBean( request );
		if (configBean != null)
		{
			Host[] hosts = null;
			try
			{
				String portName = "wshost";
				String endPoint = ( configBean.isGwosSSLEnabled() ? "https://" : "http://" ) 
				                +   configBean.getGwosServer()
				                +   configBean.getWsEndpoint()
				                +   "/"
				                +   portName;

				HostSoapBindingStub wsHost = (HostSoapBindingStub) this
						.hostLocator( portName, endPoint.toString() ).gethost();

				// Add Authentication header
				SOAPHeaderElement authentication = 
                    new SOAPHeaderElement(
                            endPoint.toString(),
                            HostSoapBindingStub.TAG_HEADER_AUTHENTICATION );

				SOAPHeaderElement user = 
                    new SOAPHeaderElement(
                            endPoint.toString(),
                            HostSoapBindingStub.TAG_HEADER_USER,
                            configBean.getWsUser() );

				SOAPHeaderElement password = 
                    new SOAPHeaderElement(
                            endPoint.toString(),
                            HostSoapBindingStub.TAG_HEADER_SECRET,
                            configBean.getWsPassword() );

				authentication.setPrefix( HostSoapBindingStub.HEADER_PREFIX );
				authentication.addChild( user );
				authentication.addChild( password );
				wsHost.setHeader( authentication );

				// Since this is the test, just lookup local host
				WSFoundationCollection col = wsHost.hostLookup( "localhost" );
				if((hosts = col.getHost()) != null)
					status = true;
			}
			catch( Exception exc )
			{
				log.error( exc.getMessage() );
			}
		} // end if
		return status;
	}

	/**
	 * Tests the Virtual environment connection. Returns true if the connection
	 * is valid else returns false
	 */
	public boolean testVirtEnvConnection( HttpServletRequest request )
	{
		boolean status = false;
		VEMAGwosConfiguration configBean = this.xferReqValuesToBean( request );

		String url  = ( configBean.isVirtualEnvSSLEnabled() ? "https://" : "http://" )
                    +   configBean.getVirtualEnvServer()
                    +   "/"
                    +   configBean.getVirtualEnvURI();

		VemaRhev vema = null;

		try
		{
			// 120829.rlynch: TODO: this needs to be made lighter weight.
			// it is quite wasteful to set up a local variable
			// for the (vema) object, to connect, and to get
			// statistics ... then just throw them away.
			//
			// the 'problem' is that the built-in list of minimum
			// VemaBaseQuery types that are supported by default in
			// .getListHost() method is significant.
			//
			// Roger & Bob think a new method ".canConnect()" should
			// do. And be lighter weight.

			vema = new VemaRhev();

            parambox.put( "vema", "api", "fqhost",     configBean.getVirtualEnvServer() != null
                                                     ? configBean.getVirtualEnvServer()
                                                     : "" ); // "eng-rhev-m-1.groundwork.groundworkopensource.com" );

            parambox.put( "vema", "api", "user",       configBean.getVirtualEnvUser()  != null
                                                     ? configBean.getVirtualEnvUser()
                                                     : "" ); // "admin" );

            parambox.put( "vema", "api", "password",   configBean.getVirtualEnvPassword()  != null
                                                     ? configBean.getVirtualEnvPassword()
                                                     : "" ); // "#m3t30r1t3" );

            parambox.put( "vema", "api", "realm",      configBean.getVirtualEnvRealm()  != null
                                                     ? configBean.getVirtualEnvRealm()
                                                     : "internal" );

            parambox.put( "vema", "api", "port",       configBean.getVirtualEnvPort()  != null
                                                     ? configBean.getVirtualEnvPort()
                                                     : "443" );

            parambox.put( "vema", "api", "protocol",   configBean.getVirtualEnvProtocol()  != null
                                                     ? configBean.getVirtualEnvProtocol()
                                                     : "https" );

            parambox.put( "vema", "api", "baseuri",    configBean.getVirtualEnvURI()  != null
                                                     ? configBean.getVirtualEnvURI()
                                                     : "/api" );

            parambox.put( "vema", "api", "certsfile",  configBean.getCertificateStore()  != null
                                                     ? configBean.getCertificateStore()
                                                     : "/usr/java/latest/jre/lib/security/cacerts" );

            parambox.put( "vema", "api", "certspass",  configBean.getCertificatePassword()  != null
                                                     ? configBean.getCertificatePassword()
                                                     : "changeit" );

            parambox.put( "vema", "api", "sslenabled", configBean.isVirtualEnvSSLEnabled()
                                                     ? "true"
                                                     : "false" );

            parambox.put( "vema", "api", "type",       "rhev" );

			vema.connect( parambox );
			/* vema.connect(
                    configBean.getVirtualEnvServer(),     // "eng-rhev-m-1.groundwork.groundworkopensource.com",
                    configBean.getVirtualEnvUser(),       // "admin", 
                    configBean.getVirtualEnvPassword(),   // "#m3t30r1t3", 
                    configBean.getVirtualEnvRealm(),      // "internal", 
                    configBean.getVirtualEnvPort(),       // "443", 
                    configBean.getVirtualEnvProtocol(),   // "https", 
                    configBean.getVirtualEnvURI(),        // "/api",
                    configBean.getVirtualEnvCertsFile(),  // "/usr/java/latest/jre/lib/security/cacerts", 
                    configBean.getVirtualEnvCertsPass()   // "changeit" 
					); */

			ConcurrentHashMap<String, VemaBaseHost> hosts = vema.getListHost(
					new ConcurrentHashMap<String, VemaBaseHost>(),
					new ArrayList<VemaBaseQuery>(),
					new ArrayList<VemaBaseQuery>() );

			if (hosts != null)
				status = true;
		}
		catch( Exception exc )
		{
			log.error( exc.getMessage() );
		}
		finally
		{
			vema.disconnect();
		}

		return status;
	}

	/**
	 * Gets the WSHost Webservice locator for the given port and end point
	 * 
	 * @param portName
	 * @param endPoint
	 * @return
	 */
	public WSHostServiceLocator hostLocator( String portName, String endPoint )
	{
		WSHostServiceLocator hostLocator = new WSHostServiceLocator();
		try
		{
			hostLocator.setEndpointAddress( portName, endPoint );
		}
		catch( Exception exc )
		{
			log.error( exc.getMessage() );
		}

		return hostLocator;
	}
}
