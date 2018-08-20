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
package com.groundwork.agents.vema.vmware.servlet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

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
import com.groundwork.agents.vema.utils.ParamBox;
import com.groundwork.agents.vema.vmware.connector.VemaVMware;
import com.wutka.jox.JOXBeanOutputStream;

/**
 * Servlet implementation class VMWareServlet
 */
public class VMWareServlet extends VemaBaseServlet
{
	private static final long serialVersionUID = 1L;
    private static final String cloudHubVersion = "CloudHub for VMware 1.0.2";

	private static org.apache.log4j.Logger log = Logger.getLogger(VMWareServlet.class);

	VEMAGwosConfiguration vGwosConfig = null;
    ParamBox                 parambox = new ParamBox();

	VemaVMware vema = null;

	/**
	 * Default constructor.
	 */
	public VMWareServlet() { }

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init( ServletConfig config ) throws ServletException
	{
		vema       = new VemaVMware();
		macService = new MonitorAgentCollectorService();

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
					VemaConstants.VMWARE_CONFIG_FILE,
                    VemaConstants.VMWARE_PROFILE_FILE,
					VemaConstants.HYPERVISOR_VMWARE, 
					VemaConstants.CONNECTOR_VMWARE, 
					VemaConstants.MGMT_SERVER_VMWARE,
					VemaConstants.APPLICATIONTYPE_VMWARE
					);
		}
		catch( WSFoundationException e )
		{
            log.error( e.toString() );
			// e.printStackTrace();
		}
		catch( RemoteException e )
		{
			log.error( e.toString() );
            // e.printStackTrace();
		}
		catch( ServiceException e )
		{
			log.error( e.toString() );
            // e.printStackTrace();
		}
		catch( Exception e )
		{
			log.error( e.toString() );
            // e.printStackTrace();
		}
	}

	public void destroy()
    {
		super.destroy();
		log.warn("\nDeconstruct VMware Virtualization service: NOW\n");
	}

	/**
	 * Creates the config files for vema stuff
	 */
	public void createConfigFiles(VemaMonitoring vemaBean,
			VEMAGwosConfiguration gwosSettings)
    {
		try
        {
			// First backup the file
			String vmwareMonitoringProfile 
                    = VemaConstants.CONFIG_FILE_PATH
					+ VemaConstants.VMWARE_PROFILE_FILE
					+ VemaConstants.CONFIG_FILE_EXTN;

            String vemaConfigName
                    = VemaConstants.CONFIG_FILE_PATH
                    + VemaConstants.VEMA_CONFIG_FILE
					+ VemaConstants.CONFIG_FILE_EXTN;

			String vmwareConfigName 
					= VemaConstants.CONFIG_FILE_PATH
		            + VemaConstants.VMWARE_CONFIG_FILE
		            + VemaConstants.CONFIG_FILE_EXTN;


			File orig = new File(vemaConfigName);
			if (orig.exists())
            {
				orig.renameTo(new File( vmwareConfigName) );
			}

			File file = new File( vmwareConfigName );
			if (file.exists())
            {
				file.renameTo(new File( vmwareConfigName  + "_old" + ".backup"));
			}

			// Just encrypt before storing it in the file
			String sharedSecret = gwosSettings.getVirtualEnvPassword();
			gwosSettings.setVirtualEnvPassword(SharedSecretProtector.encrypt(sharedSecret));

			FileOutputStream fileOutGwos = new FileOutputStream(vmwareConfigName);
			JOXBeanOutputStream joxOutGwos = new JOXBeanOutputStream(fileOutGwos);
			joxOutGwos.writeObject("vema", gwosSettings );

			File fileObj = new File( vmwareMonitoringProfile );
			if (fileObj.exists())
            {
				fileObj.renameTo(new File(vmwareMonitoringProfile + "_old" + ".backup"));
			}
			FileOutputStream fileOutVema = new FileOutputStream( vmwareMonitoringProfile );
			JOXBeanOutputStream joxOutVema = new JOXBeanOutputStream( fileOutVema, true );
			joxOutVema.writeObject("vema-monitoring", vemaBean );
		}
        catch (FileNotFoundException    e) { log.error(e.getMessage()); }
        catch (IOException              e) { log.error(e.getMessage()); }
        catch (GeneralSecurityException e) { log.error(e.getMessage()); }
        catch (Exception                e) { log.error(e.getMessage()); }
	}

	/**
	 * Tests the groundwork connection. Returns true if the connection is valid
	 * else returns false
	 */
	public boolean testGWOSConnection(HttpServletRequest request)
    {
		boolean status = false;
		VEMAGwosConfiguration configBean = this.xferReqValuesToBean(request);
		if (configBean != null)
        {
			Host[] hosts = null;
			try
            {
				String endPoint = null;
				String portName = "wshost";

                endPoint = (configBean.isGwosSSLEnabled()) ? "https://" : "http://"
                         + configBean.getGwosServer()
                         + configBean.getWsEndpoint()
                         + "/"
                         + portName;

				HostSoapBindingStub wsHost = (HostSoapBindingStub) this
						.hostLocator(portName, endPoint.toString()).gethost();
				// Add Authentication header
				SOAPHeaderElement authentication = new SOAPHeaderElement(
						endPoint,
						HostSoapBindingStub.TAG_HEADER_AUTHENTICATION);
				SOAPHeaderElement user = new SOAPHeaderElement(
						endPoint,
						HostSoapBindingStub.TAG_HEADER_USER,
						configBean.getWsUser());
				SOAPHeaderElement password = new SOAPHeaderElement(
						endPoint,
						HostSoapBindingStub.TAG_HEADER_SECRET,
						configBean.getWsPassword());
				authentication.setPrefix(HostSoapBindingStub.HEADER_PREFIX);
				authentication.addChild(user);
				authentication.addChild(password);
				wsHost.setHeader(authentication);
				// Since this is the test, just lookup local host
				WSFoundationCollection col = wsHost.hostLookup("localhost");
				hosts = col.getHost();
				if (hosts != null)
					 status = true; // THIS IS INTENTIONAL!
				else status = true; // since it was found that 'null' can be returned
				// without there actually being an error.  This happens when the sysop
				// deletes 'localhost'.   THe XML that comes back doesn't have  <host>
				// section, which in turn returns a null here.
				
			}
            catch (Exception exc)
            {
				log.error(exc.getMessage());
			}
		}
		return status;
	}

	/**
	 * Tests the Virtual environment connection. Returns true if the connection
	 * is valid else returns false
	 */
	public boolean testVirtEnvConnection(HttpServletRequest request)
    {
		boolean status = false;
		VEMAGwosConfiguration configBean = this.xferReqValuesToBean(request);

        String url  = (configBean.isVirtualEnvSSLEnabled() ? "https://" : "http://" )
                    + configBean.getVirtualEnvServer()
                    + "/"
                    + configBean.getVirtualEnvURI()
                    ;

		String vmName = "demo";
		VemaVMware vema = null;

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

			vema = new VemaVMware();
			
            parambox.put( "vema", "api", "fqhost",     configBean.getVirtualEnvServer() != null
                                                     ? configBean.getVirtualEnvServer()
                                                     : "" /* "eng-vsphere4" */ );

            parambox.put( "vema", "api", "user",       configBean.getVirtualEnvUser()  != null
                                                     ? configBean.getVirtualEnvUser()
                                                     : "" /* "administrator" */ );

            parambox.put( "vema", "api", "password",   configBean.getVirtualEnvPassword()  != null
                                                     ? configBean.getVirtualEnvPassword()
                                                     : "" /* "aZ0i+4XNm05tGc2x5FFCcg==" */ );

//            parambox.put( "vema", "api", "realm",      configBean.getVirtualEnvRealm()  != null
//                                                     ? configBean.getVirtualEnvRealm()
//                                                     : "internal" );
//
            parambox.put( "vema", "api", "port",       configBean.getVirtualEnvPort()  != null
                                                     ? configBean.getVirtualEnvPort()
                                                     : "443" );

            parambox.put( "vema", "api", "protocol",   configBean.getVirtualEnvProtocol()  != null
                                                     ? configBean.getVirtualEnvProtocol()
                                                     : "https" );

            parambox.put( "vema", "api", "baseuri",    configBean.getVirtualEnvURI()  != null
                                                     ? configBean.getVirtualEnvURI()
                                                     : "/sdk" );

//            parambox.put( "vema", "api", "certsfile",  configBean.getVirtualEnvCertsFile()  != null
//                                                     ? configBean.getVirtualEnvCertsFile()
//                                                     : "/usr/java/latest/jre/lib/security/cacerts" );
//
//            parambox.put( "vema", "api", "certspass",  configBean.getVirtualEnvCertsPass()  != null
//                                                     ? configBean.getVirtualEnvCertsPass()
//                                                     : "changeit" );
//
            parambox.put( "vema", "api", "sslenabled", configBean.isVirtualEnvSSLEnabled()
                                                     ? "true"
                                                     : "false" );

            parambox.put( "vema", "api", "type",       "vmware" );

            vema.connect( parambox );
			//vema.connect(url.toString(), configBean.getVirtualEnvUser(),
			//		configBean.getVirtualEnvPassword(), vmName);

			ConcurrentHashMap<String, VemaBaseHost> hosts = vema.getListHost(
					new ConcurrentHashMap<String, VemaBaseHost>(),
					new ArrayList<VemaBaseQuery>(),
					new ArrayList<VemaBaseQuery>());

			if (hosts != null)
            {
				status = true;
			}
		}
        catch (Exception exc)
        {
			log.error(exc.getMessage());
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
