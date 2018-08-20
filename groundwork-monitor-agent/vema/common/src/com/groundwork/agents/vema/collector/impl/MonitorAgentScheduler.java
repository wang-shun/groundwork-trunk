package com.groundwork.agents.vema.collector.impl;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import javax.xml.rpc.ServiceException;

import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.api.WSFoundationException;

import com.groundwork.agents.vema.api.Vema;
import com.groundwork.agents.vema.configuration.VEMAGwosConfiguration;
import com.groundwork.agents.vema.monitorAgent.MonitorAgentClient;


public class MonitorAgentScheduler implements Runnable
{
	private static org.apache.log4j.Logger	log					= Logger.getLogger( MonitorAgentScheduler.class );
	private VEMAGwosConfiguration			vGwosConfig			= null;
	private MonitorAgentClient				monitorAgentClient	= null;

	private ScheduledExecutorService		_scheduler			= Executors
		.newScheduledThreadPool(
				1,
				new ThreadFactory()
				{
					public Thread newThread( Runnable task )
					{
						Thread thread = new Thread( task );
						thread.setName( "monitorAgent_tomcat_collector" );
						thread.setDaemon( true );
						return thread;
					}
				} );
	
	private boolean shutdownMonitor    = false;
	private boolean isRunningScheduler = false;

	/**
	 * Shutsdown the collector service
	 */
	public void queueShutdown()
	{
		shutdownMonitor = true;
	}

	/**
	 * Starts the collector Service
	 * 
	 * @throws ServiceException
	 * @throws RemoteException
	 * @throws WSFoundationException
	 */

	public MonitorAgentScheduler( Vema vema, String gwosConfigFilename, 
			String vemaMonitorProfileFilename, String hypervisorVmware,
			String connectorVmware, String mgmtServerVmware, String applicationTypeVmware )
	{
		monitorAgentClient = new MonitorAgentClient( 
				vema, 
				null, // this is the gwConfig bean ...which has exception code in the MAC call
				hypervisorVmware, 
				connectorVmware, 
				mgmtServerVmware,
				applicationTypeVmware,
				gwosConfigFilename, 
				vemaMonitorProfileFilename );
		
		shutdownMonitor    = false;
		isRunningScheduler = false;
	}
	
	boolean isRunning()
	{ 
		return this.isRunningScheduler;
	}

	public void run()
	{
		int counter = 0;
		
		while( true )
		{
			this.isRunningScheduler = true;
			
			if(counter++ > 0) // try restarting if necessary every few minutes.
				try { Thread.sleep( 15 * 1000L ); } catch ( Exception e ) {}

			try
			{
				if (monitorAgentClient.isRunning() == true)
				{
					if( this.shutdownMonitor == true )
					{
						monitorAgentClient.setIsRunning(false); // queues a shutdown
						for( int i = 0; i < 30; i++ )
						{
							try { Thread.sleep( 1000L ); } catch ( Exception e ) {}
							if( monitorAgentClient.isRunning() )
								 continue;
							else break;
						}
						_scheduler.shutdownNow();
						this.isRunningScheduler = false;
					}
				}
				else
				{
					_scheduler.execute( monitorAgentClient );
				}
			}
			catch( Exception e )
			{
				log.error( "Monitor status failed. Loop #" + counter + " Message  " + e );
			}
		}
	}

	public boolean testConnection( Properties prop )
	{
		// TODO Auto-generated method stub
		return false;
	}
}
