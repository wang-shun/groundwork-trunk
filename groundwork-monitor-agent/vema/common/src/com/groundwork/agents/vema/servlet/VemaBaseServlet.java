/*
 * Copyright 2012 GroundWork Open Source, Inc. ( "GroundWork" ) All rights
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

package com.groundwork.agents.vema.servlet;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;

import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.xml.rpc.ServiceException;
import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.api.WSFoundationException;

import org.itgroundwork.foundation.joxbeans.Hypervisor;
import org.itgroundwork.foundation.joxbeans.Metric;
import org.itgroundwork.foundation.joxbeans.VM;
import org.itgroundwork.foundation.joxbeans.VemaMonitoring;

import com.groundwork.agents.vema.api.VemaConstants;
import com.groundwork.agents.vema.api.Vema;
import com.groundwork.agents.vema.base.VemaBaseState;
import com.groundwork.agents.vema.collector.impl.MonitorAgentCollector;
import com.groundwork.agents.vema.configuration.MonitorAgentConfigXMLToBean;
import com.groundwork.agents.vema.configuration.VEMAGwosConfiguration;

import com.wutka.jox.JOXBeanInputStream;

/**
 * Servlet implementation class VemaBaseServlet
 */
public abstract class VemaBaseServlet extends HttpServlet
{
	private static org.apache.log4j.Logger	log					= Logger.getLogger( VemaBaseServlet.class );

	private static final long				serialVersionUID	= 1L;

	protected String			     vemaMonitorProfileFilename = null;
	protected String                        gwosConfigFilename  = null;
	protected Vema							vema				= null;
	protected MonitorAgentCollector         macService          = null;

	protected String						hypervisorVema		= null;
	protected String						connectorVema		= null;
	protected String						mgmtServerVema		= null;
	protected String						applicationTypeVema = null;
//	protected String						entityHypervisor	= null;  // should go away!
//	protected String						entityMgmtServer	= null;  // should go away!

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public VemaBaseServlet()
	{
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see Servlet#init( ServletConfig )
	 */
	// public void init( ServletConfig config) throws ServletException {

	public void init( ServletConfig config, 
            Vema vema,
			MonitorAgentCollector macService, 
            String gwosConfigFilename,
            String vemaMonitorProfileFilename, 
            String hypervisorVema, 
            String connectorVema, 
            String mgmtServerVema,
            String applicationTypeVema )
			throws ServletException, WSFoundationException, RemoteException,
			ServiceException
	{
		super.init( config );
		
		this.vemaMonitorProfileFilename = vemaMonitorProfileFilename;
		this.vema                       = vema;
		this.macService                 = macService;
		this.connectorVema              = connectorVema;
		this.hypervisorVema             = hypervisorVema;
		this.mgmtServerVema             = mgmtServerVema;
		this.gwosConfigFilename         = gwosConfigFilename;
		this.applicationTypeVema        = applicationTypeVema;
	
		log.info( "init( "
				+ "monprofilename='"  + vemaMonitorProfileFilename + "', "
				+ "gwosConfigfile='"  + gwosConfigFilename + "', "
				+ "connector='"       + connectorVema + "', "
				+ "hypervisor='"      + hypervisorVema + "', "
				+ "mgmtServer='"      + mgmtServerVema + "', "
				+ "applicationType='" + applicationTypeVema + "', "
                );
		
		if( macService != null )
		{
			macService.start( 
                this.vema, 
                this.gwosConfigFilename, 
                this.vemaMonitorProfileFilename, 
                this.hypervisorVema, 
                this.connectorVema, 
                this.mgmtServerVema,
                this.applicationTypeVema
                );
		}
		else
		{
			log.error( "Not configured yet -- not monitoring" );
			return;
		}
		log.debug( "CloudHub Base servlet initialization closure" );
	}

	/**
	 * @see HttpServlet#doGet( HttpServletRequest request, HttpServletResponse
	 *      response )
	 */
	protected void doGet( HttpServletRequest request, HttpServletResponse response) 
        throws ServletException, IOException
	{
		log.error( "Get not supported for CloudHub" );
	}

	/**
	 * @see HttpServlet#doPost( HttpServletRequest request, HttpServletResponse
	 *      response )
	 */
	protected void doPost( HttpServletRequest request, HttpServletResponse response ) 
        throws ServletException, IOException
	{
		String        action = request.getParameter( "action" );
		RequestDispatcher rd = null;
		
		if( action != null )
		{
			// index.html matches here: 
			if( action.equalsIgnoreCase( "create_from_ui_index_page" ) )
			{
				if( request.getParameter( "new" ) != null) 
					rd = getServletContext()
						.getRequestDispatcher( "/newConnectionWizard.jsp" );
				else
				{
					VemaMonitoring vemaBean = MonitorAgentConfigXMLToBean
							.vemaXMLToBean( this.vemaMonitorProfileFilename );
                    log.debug( "doPost: profilefile = '" + this.vemaMonitorProfileFilename + "'" );
					if( vemaBean != null )
						request.getSession().setAttribute( "vemaBean", vemaBean );

					VEMAGwosConfiguration gwosConfigBean = 
                        MonitorAgentConfigXMLToBean
							.gwosConfigXMLToBean( this.gwosConfigFilename);

                    log.debug( "doPost: config file = '" + this.gwosConfigFilename  + "'" );
					if( gwosConfigBean != null )
						request.getSession().setAttribute( "configBean", gwosConfigBean );

					rd = getServletContext().getRequestDispatcher( "/modifyConnectionWizard.jsp" );
					
					log.debug( "Modified Connection Wizard" );
				}
			} 
			
			// newConnectionWizard.jsp matches here:
			else if( action.equalsIgnoreCase( "create_from_ui_conn_page" ) )
			{
				if( request.getParameter( "next" ) != null  // if  "next" button has been pressed
				&&  this.testGWOSConnection( request) )      // AND the GWOS connection works
				{
					VEMAGwosConfiguration bean = 
                        ( VEMAGwosConfiguration ) request
                        	.getSession()
                        		.getAttribute( "configBean" );

					if( bean != null )
					{
                        String monitoringProfileFilename 
                        		= bean.getVirtualEnvType()
                        		+ VemaConstants.PROFILE_CANONICAL_BASE
                        		+ VemaConstants.CONFIG_FILE_EXTN;

						String responseString = 
                            this.performPost(
								bean.getWsUser(), 
                                bean.getWsPassword(),
                                bean.getVirtualEnvType(),
                                monitoringProfileFilename, 
                                bean.getGwosServer(),
								bean.isGwosSSLEnabled()
                                );

						int statusBegIndex = responseString.indexOf( "<code>" );
						int statusEndIndex = responseString.indexOf( "</code>" );

						if( statusBegIndex != -1 && statusEndIndex != -1 )
						{
							int code = Integer.parseInt(
                                            responseString.substring(
                                                statusBegIndex + "<code>".length(),
                                                statusEndIndex) );
                            // TODO
                            // 130426.rlynch: not sure why this is being done.  Never used.
						}
						else
						{
							JOXBeanInputStream joIn = new JOXBeanInputStream(
									new ByteArrayInputStream(
                                        responseString.getBytes()) );
							VemaMonitoring vemaBean = ( VemaMonitoring) 
                                    joIn.readObject( VemaMonitoring.class );

							request.getSession().setAttribute( "vemaBean", vemaBean );
						}
					}
					rd = getServletContext().getRequestDispatcher( "/assignThresholds.jsp" );
				}
// TODO - this needs to change to avoid RACE CONDITION where upper threads are looking
//        for the configuration file, and finding none, replace stuff, making the 
//        vmware API call fail.
				else // implied if( request.getParameter( "test" ) != null || !valid gwos connection)
				{
					if(      !this.testGWOSConnection( request )) 
                         request.setAttribute( "message", "Groundwork Connection Failed" );
					else if( !this.testVirtEnvConnection( request ) ) 
                         request.setAttribute( "message", "CloudHub Connection Failed" );
					else request.setAttribute( "message", "Connection Successful" );

					rd = getServletContext().getRequestDispatcher( "/newConnectionWizard.jsp" );
				}
			}
			
			// selectServices.jsp matches here:
			else if( action.equalsIgnoreCase( "create_from_ui_select_page" ) )
			{
				String[] servicesArr = request.getParameterValues( "services" );
				List<String> services = Arrays.asList( servicesArr );
				request.getSession().setAttribute( "selectedComponents",
						services );
				log.debug( "# of selected components is : " + services.size() );
				rd = getServletContext().getRequestDispatcher( "/assignThresholds.jsp" );

			}
			
			// modifyConnectionWizard.jsp (with parameter 'flow' also set)
			// assignThresholds.jsp       (without 'flow' parameter
			else if( action.equalsIgnoreCase( "create_from_ui_assign_page" ) )
			{
                String[] ss = null;  // general purpose string a

				// alias
				String[] aliasArr = request.getParameterValues( "hyp_alias" );

				// Is Monitored
				String[] isMonitoredArr = { "none" };
				if( ( ss = request.getParameterValues( "hyp_monitored" )) != null) 
					isMonitoredArr = ss;
				List<String> monitoredList = Arrays.asList( isMonitoredArr );

				// Is Graphed
				String[] isGraphedArr = { "none" };
				if( ( ss = request.getParameterValues( "hyp_graphed" )) != null) 
					isGraphedArr = ss;
				List<String> graphedList = Arrays.asList( isGraphedArr );

				// Alias
				String[] vm_aliasArr = request.getParameterValues( "vm_alias" );

				// Is Monitored
				String[] vm_isMonitoredArr = { "none" };
				if( ( ss = request.getParameterValues( "vm_monitored" )) != null) 
					vm_isMonitoredArr = ss;
				List<String> vm_monitoredList = Arrays.asList( vm_isMonitoredArr );

				// Is Graphed
				String[] vm_isGraphedArr = { "none" };
				if( ( ss = request.getParameterValues( "vm_graphed" )) != null) 
					vm_isGraphedArr = ss;
				List<String> vm_graphedList = Arrays.asList( vm_isGraphedArr );

				// Enumeration<String> params = request.getParameterNames();
				VemaMonitoring vemaBean = ( VemaMonitoring) request.getSession().getAttribute( "vemaBean" );

				if( vemaBean != null )
				{
					Hypervisor     hypvisor = vemaBean.getHypervisor();
					VM                   vm = vemaBean.getVm();
					Metric[]     hypMetrics = hypvisor.getMetric();
					List<Metric>    hypList = new ArrayList<Metric>( Arrays.asList( hypMetrics) );
					List<Metric> hypAddList = new ArrayList<Metric>();
					
					int             vmcount = 0;
					int            hypcount = 0;

					for( Metric metric : hypList )
					{
                        metric.setGraphed( false );   // default action
                        metric.setMonitored( false ); // default action

						for( int index = 0; index < aliasArr.length; index++ )
						{
							String metricName = aliasArr[index];

							if( metric.getName() != null
							&&  metric.getName().equalsIgnoreCase( metricName) )
							{
                                metric.setMonitored( monitoredList.contains( metricName) );
                                metric.setGraphed(     graphedList.contains( metricName) );
                                
                                if(monitoredList.contains( metricName )
                                ||   graphedList.contains( metricName ) )
                                	hypcount++;
                                
                                // only do conversion work ON MATCH
                                double criticalThreshold = Double.parseDouble( 
                                        request.getParameter( 
                                            "hyp_criticalThreshold_" + ( index + 1)) );

                                double warningThreshold  = Double.parseDouble(
                                        request.getParameter( 
                                            "hyp_warningThreshold_" + ( index + 1)) );

								metric.setCriticalThreshold( criticalThreshold );
								metric.setWarningThreshold(  warningThreshold  );
                                break;  // shortcircuit!  if found, do stuff, then quit!
							}
                            else // if( metric.getName == null || not the metric match )
                                continue;
						}
                        hypAddList.add( metric ); // always add
					}

					hypMetrics             = hypAddList.toArray( new Metric[ hypAddList.size() ] );
					Metric[]     vmMetrics = vm.getMetric();
					List<Metric> vmList    = new ArrayList<Metric>( Arrays.asList( vmMetrics) );
					List<Metric> vmAddList = new ArrayList<Metric>();

					for( Metric metric : vmList )
					{
                        metric.setGraphed( false );   // default action
                        metric.setMonitored( false ); // default action

						for( int index = 0; index < vm_aliasArr.length; index++ )
						{
							String metricName        = vm_aliasArr[index];

							if( metric.getName() != null
							&&  metric.getName().equalsIgnoreCase( metricName) )
							{
                                // only do conversion work ON MATCH
                                double criticalThreshold = Double.parseDouble( 
                                    request.getParameter( 
                                        "vm_criticalThreshold_" + ( index + 1) ) );

                                double warningThreshold  = Double.parseDouble( 
                                    request.getParameter( 
                                        "vm_warningThreshold_" + ( index + 1) ) );

                                metric.setMonitored(vm_monitoredList.contains( metricName) );
                                metric.setGraphed(  vm_graphedList  .contains( metricName) );

                                if(vm_monitoredList.contains( metricName )
                                ||   vm_graphedList.contains( metricName ) )
                                	vmcount++;
                                
								metric.setCriticalThreshold( criticalThreshold );
								metric.setWarningThreshold(  warningThreshold  );
                                break;  // shortcircuit!  if found, do stuff, then quit!
							} 
						} 
                        vmAddList.add( metric ); // always do this
					} 

					vmMetrics = vmAddList.toArray( new Metric[vmAddList.size()] );
					hypvisor.setMetric( hypMetrics );
					vm      .setMetric( vmMetrics );
					VEMAGwosConfiguration gwosSettings = null;

                    String flowControl = 
                    		request.getParameter( "flow" ) == null 
	                        ? "new" 
	                        : request.getParameter( "flow" );

					if( flowControl.equalsIgnoreCase( "new" ) )
                         gwosSettings = ( VEMAGwosConfiguration)request.getSession().getAttribute( "configBean" );
					else gwosSettings = this.xferReqValuesToBean( request );

                    log.info(  "hypchecked: " + hypcount + "; vmchecked: " + vmcount + "" );
                    log.debug( "flowControl = '" + flowControl + "'" );
                    log.debug( "configBean():\n" + gwosSettings.formatSelf() );

					this.createConfigFiles( vemaBean, gwosSettings );
					rd = getServletContext().getRequestDispatcher( "/confirm.jsp" );

					log.debug( "Test this is inside the do post method in base servlet" );
					VemaBaseState.setGWOSConfigurationUpdated( true );
					
					/* Read the config and set it */
					//VemaBaseState.setSuspendMonitorAgentCollector( true or false );
				}
			} 

			// uploadProfile.jsp matches here:
			else if( action.equalsIgnoreCase( "create_from_ui_export_page" ) )
			{
				// 130430.rlynch:
				// NO CODE YET.  What's it supposed to do?
			}
			
			else
			{
				// 130430.rlynch:
				// MAYBE NO CODE NECESSARY.  Certainly though ...kind of a loose end.
			}
			
			if( rd != null )
			{
				rd.forward( request, response );
			}
		}
		else
		{
			rd = getServletContext().getRequestDispatcher( "/index.html" );
			rd.forward( request, response );
		}
	}

	/**
	 * Perform post
	 * 
	 * @param userName
	 * @param password
	 * @param appServerName
	 * @param mbeanAtts
	 * @param gwServerName
	 * @return
	 */
	private String performPost( String userName, String password, String vmType,
			String deltaMonitoringStub, String gwServerName, boolean isGWServerSecured )
	{
		// "deltaMonitoringStub" was renamed from "clientMonitoringProfile" from having
		// a conversation 130429.rlynch::arul, where it was revealed that the purpose
		// of this variable was more for some future, where "deltas" or "differential" 
		// file updates would be supported.
		// 
		// no discussion was had regarding the kinetics of such.
		//
		String      response = null;
		DataOutputStream out = null;

		try
		{   // connect
			URI uri = new URI(
					isGWServerSecured ? "https" : "http",
					null,
					"//"
                    + gwServerName
                    + "/foundation-webapp/restwebservices/vemaProfile/checkUpdates",
					null, 
                    null );

			URL url = uri.toURL();
			HttpURLConnection connection = ( HttpURLConnection) url.openConnection();
			log.debug( "URL: " + url.toString() );

			// initialize the connection
			connection.setDoOutput(        true );
			connection.setDoInput(         true );
			connection.setRequestMethod(   "POST" );
			connection.setUseCaches(       false );
			connection.setRequestProperty( "Content-type", "application/x-www-form-urlencoded" );
			connection.setRequestProperty( "Connection", "Keep-Alive" );

			out = new DataOutputStream( connection.getOutputStream() );

            String message = 
                  "username="  + userName 
                + "&password=" + password
                + "&vmtype="   + vmType 
                + "&client-monitoring-profile=" 
                + ( deltaMonitoringStub != null 
                    ? URLEncoder.encode( deltaMonitoringStub, "UTF-8" ) 
                    : "" );

			out.writeBytes( message );

            log.debug( "URL writeBytes: '" + message + "'" );

			out.flush();
			out.close();

			BufferedReader inStream = new BufferedReader(
                new InputStreamReader( connection.getInputStream()) );

			StringBuffer sb = new StringBuffer( 10000 );  // give a hint!
			String        s = null;

			while( ( s = inStream.readLine()) != null )
				sb.append( s );

			response = sb.toString();
			connection.disconnect();
			
			if( response.contains( "balloon" ) && response.contains( "perfcounter" ) ) // unique to VMWARE
				if( vmType.equalsIgnoreCase( VemaConstants.CONNECTOR_VMWARE ) )
					/* do nothing */ ;
				else if( vmType.equalsIgnoreCase( VemaConstants.CONNECTOR_RHEV ))
				{
					// well, if we get a VMWARE file and we're actually the RHEV configurator, 
					// then obviously this isn't right.  It is a known-limitation in GroundWork
					// server 6.7, and resolved by 7.0 release.  IN the meantime, let's see if 
					// we can find a canonical file in the more well-known location. 
					//
					// that location will be found at VemaConstants.CONFIG_FILE_PATH, and we'll 
					// use the underbar version of the filename as input.  
					//
					// ALERT:  looking for "rhev_monitoring_profile.xml" in "/usr/local/groundwork/config" folder
					
					String canonicalRHEVprofile = 
							  VemaConstants.CONFIG_FILE_PATH
							+ vmType
							+ VemaConstants.PROFILE_CANONICAL_BASE
							+ VemaConstants.CONFIG_FILE_EXTN
							;
					
					log.debug( "Looking for '" + canonicalRHEVprofile + "' (GroundWork 6.7 adaptation)" );
					try
					{
						FileInputStream   fis = new FileInputStream( canonicalRHEVprofile );
						InputStreamReader isr = new InputStreamReader( fis );
						BufferedReader    br  = new BufferedReader( isr );
						
						sb = new StringBuffer( 10000 );  // give a hint.
						while( (s = br.readLine()) != null )
							sb.append( s );

						response = sb.toString();  // now we should be good.
						
						br.close();  // you may think you want to nest these (above, at 
						isr.close(); // declaration), and have "autoclose" chain react
						fis.close(); // thru objects, but no.  Roger recommends this instead.
					}
					catch( FileNotFoundException fnfe )
					{
						throw new Exception( "Couldn't find '" + canonicalRHEVprofile + "' (need a copy!)" );
					}
					
				}
				else
				{
					throw new Exception( "vmType = '" + vmType + "' - unsupported for GroundWork 6.7 and before");
				}
		}
		catch ( Exception e )
		{
			log.error( "Got Exception: " + e );

			response = "<code>6</code>"
					 + "<message>" 
                     + e.getMessage() + " (or Invalid GroundWork Server Name)"
					 + "</message>";
		}

        log.debug( "response: \n" + response + "\n" );
		return response;
	}

	/**
	 * @see Servlet#destroy()
	 */
	public void destroy()
	{
		super.destroy();
		log.warn( "\n" 
				+ "----------------------------------------------------\n"
				+ "- Shutting down Virtualization service immediately -\n"
				+ "----------------------------------------------------\n"
				+ "\n"
				);
	}

	/**
	 * Helper for transfering request values to bean
	 * 
	 * @param request
	 * @return
	 */
	protected VEMAGwosConfiguration xferReqValuesToBean( HttpServletRequest request )
	{
		VEMAGwosConfiguration bean = ( VEMAGwosConfiguration) request.getSession().getAttribute( "configBean" );
		String      virtualEnvType = request.getParameter( "virtualEnvType" );

		if( bean != null )
		{
			String s = null;

			// -----------------------------------------------------------
			// groundwork server connectivity ( stats reporting)
			// -----------------------------------------------------------
			bean.setGwosServer(         ( String) request.getParameter( "groundwork.server.name"     ) );
			bean.setGwosPort(           ( String) request.getParameter( "groundwork.server.port"     ) );
            s                         = ( String) request.getParameter( "groundwork.server.sslEnabled" );
            bean.setGwosSSLEnabled( s != null && s.equalsIgnoreCase( "on" ) );
			// -----------------------------------------------------------
            // web services connectivity
			// -----------------------------------------------------------
            bean.setWsUser(             ( String) request.getParameter( "groundwork.webservices.username" ) );
			bean.setWsPassword(         ( String) request.getParameter( "groundwork.webservices.password" ) );
			bean.setWsHostName(         ( String) request.getParameter( "groundwork.webservices.hostname" ) );
			bean.setWsHostGroupName(    ( String) request.getParameter( "groundwork.webservices.hostgroupname"));
			bean.setWsEndpoint(         ( String) request.getParameter( "groundwork.webservices.endpoint" ) );


			// -----------------------------------------------------------
			// virtual server connectivity information
			// -----------------------------------------------------------
			bean.setVirtualEnvServer(   ( String) request.getParameter( "virtualEnv.serverName" ) );
			bean.setVirtualEnvURI(      ( String) request.getParameter( "virtualEnv.uri"        ) );
			bean.setVirtualEnvPort(     ( String) request.getParameter( "virtualEnv.port"       ) );
			bean.setVirtualEnvUser(     ( String) request.getParameter( "virtualEnv.username"   ) );
			bean.setVirtualEnvPassword( ( String) request.getParameter( "virtualEnv.password"   ) );
			bean.setVirtualEnvProtocol( ( String) request.getParameter( "virtualEnv.protocol"   ) );
			bean.setVirtualEnvRealm(    ( String) request.getParameter( "virtualEnv.realm"      ) );
			s                         = ( String) request.getParameter( "virtualEnv.sslEnabled" );
            bean.setVirtualEnvSSLEnabled( s != null && s.equalsIgnoreCase( "on" ) );

			// -----------------------------------------------------------
            // CloudHub polling intervals
			// -----------------------------------------------------------
			bean.setCheckInterval(  (String) request.getParameter( "check.interval" ) != null 
				? Integer.parseInt( (String) request.getParameter( "check.interval" ))
				: 5 );
			
			bean.setComaInterval(   (String) request.getParameter( "coma.interval" ) != null 
				? Integer.parseInt( (String) request.getParameter( "coma.interval" ))
				: 15 ); 

			bean.setSyncInterval(   (String) request.getParameter( "sync.interval" ) != null 
				? Integer.parseInt( (String) request.getParameter( "sync.interval" ))
				: 2 );

			// -----------------------------------------------------------
			// certificates & authentication pointers
			// -----------------------------------------------------------
			bean.setCertificatePassword( (String) request.getParameter( "certificate.password" ));
			bean.setCertificateStore(   ( String) request.getParameter( "certificate.store" ));

            Exception e = new Exception( "bananas");

			if(log.isDebugEnabled())
				log.debug( 
            "\nStack:\n" + 
            e.getStackTrace()[0].toString() + "\n" + 
            e.getStackTrace()[1].toString() + "\n" + 
            e.getStackTrace()[2].toString() + "\n" + 
            e.getStackTrace()[3].toString() + "\n" + 
            e.getStackTrace()[4].toString() + "\n" + 
            e.getStackTrace()[5].toString() + "\n" + 
            e.getStackTrace()[6].toString() + "\n" + 
            e.getStackTrace()[7].toString() + "\n" + 
            "\n" + 
"bean.getGwosServer()          '" + ( bean.getGwosServer()       == null ? "null" : bean.getGwosServer() ) + "'\n" + 
"bean.getGwosPort()            '" + ( bean.getGwosPort()         == null ? "null" : bean.getGwosPort() ) + "'\n" + 
"bean.isGwosSSLEnabled()       '" + ( bean.isGwosSSLEnabled()    ? "true" : "false" ) + "'\n" + 
"bean.getWsUser()              '" + ( bean.getWsUser()           == null ? "null" : bean.getWsUser() ) + "'\n" + 
"bean.getWsPassword()          '" + ( bean.getWsPassword()       == null ? "null" : bean.getWsPassword() ) + "'\n" + 
"bean.getWsHostName()          '" + ( bean.getWsHostName()       == null ? "null" : bean.getWsHostName() ) + "'\n" + 
"bean.getWsHostGroupName()     '" + ( bean.getWsHostGroupName()  == null ? "null" : bean.getWsHostGroupName() ) + "'\n" + 
"bean.getWsEndpoint()          '" + ( bean.getWsEndpoint()       == null ? "null" : bean.getWsEndpoint() ) + "'\n" + 
"bean.getVirtualEnvServer()    '" + ( bean.getVirtualEnvServer() == null ? "null" : bean.getVirtualEnvServer() ) + "'\n" + 
"bean.getVirtualEnvURI()       '" + ( bean.getVirtualEnvURI()    == null ? "null" : bean.getVirtualEnvURI() ) + "'\n" + 
"bean.getVirtualEnvPort()      '" + ( bean.getVirtualEnvPort()   == null ? "null" : bean.getVirtualEnvPort() ) + "'\n" + 
"bean.getVirtualEnvUser()      '" + ( bean.getVirtualEnvUser()   == null ? "null" : bean.getVirtualEnvUser() ) + "'\n" + 
"bean.getVirtualEnvPassword()  '" + ( bean.getVirtualEnvPassword() == null ? "null" : bean.getVirtualEnvPassword() ) + "'\n" + 
"bean.getVirtualEnvProtocol()  '" + ( bean.getVirtualEnvProtocol() == null ? "null" : bean.getVirtualEnvProtocol() ) + "'\n" + 
"bean.getVirtualEnvRealm()     '" + ( bean.getVirtualEnvRealm()    == null ? "null" : bean.getVirtualEnvRealm() ) + "'\n" + 
"bean.setVirtualEnvSSLEnabled()'" + ( bean.isVirtualEnvSSLEnabled()        ? "true" : "false" ) + "'\n" + 
"bean.getCheckInterval()       '" +   bean.getCheckInterval()  + "'\n" + 
"bean.getComaInterval()        '" +   bean.getComaInterval()   + "'\n" + 
"bean.getSyncInterval()        '" +   bean.getSyncInterval()   + "'\n" + 
"bean.getCertificatePassword() '" + ( bean.getCertificatePassword() == null ? "null" : bean.getCertificatePassword() ) + "'\n" + 
"bean.getCertificateStore()    '" + ( bean.getCertificateStore()    == null ? "null" : bean.getCertificateStore() ) + "'\n" + 
            "" );
		}
		
		request.getSession().setAttribute( "configBean", bean );
		return bean;
	}
	/**
	 * Creates the config files to the /usr/local/groundwork/config folder
	 * 
	 * @param vemaBean
	 * @param gwosSettings
	 */
	protected abstract void createConfigFiles( VemaMonitoring vemaBean,
			VEMAGwosConfiguration gwosSettings );

	/**
	 * Tests the GWOS Connection
	 * 
	 * @param request
	 * @return
	 */
	protected abstract boolean testGWOSConnection( HttpServletRequest request );

	/**
	 * Tests the virtual env connection
	 * 
	 * @param request
	 * @return
	 */
	protected abstract boolean testVirtEnvConnection( HttpServletRequest request );
}
