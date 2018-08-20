/*
 * 
 * Copyright 2010 GroundWork Open Source, Inc. ("GroundWork") All rights
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
package com.groundwork.agents.vema.collector.impl;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.xml.rpc.ServiceException;
import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.api.WSFoundationException;
import com.groundwork.agents.vema.api.Vema;
import com.groundwork.agents.vema.configuration.VEMAGwosConfiguration;
import com.groundwork.agents.vema.monitorAgent.MonitorAgentClient;
import com.groundwork.agents.vema.collector.impl.MonitorAgentScheduler;

/**
 * This class creates a Scheduler to synchronize the GWOS and Vema Host List..
 * 
 * 
 * @author rvardhineedi
 * 
 */

public class MonitorAgentCollectorService implements MonitorAgentCollector 
{
	private static org.apache.log4j.Logger	        log	= Logger.getLogger( MonitorAgentCollectorService.class );
	private VEMAGwosConfiguration           vGwosConfig = null;
	private MonitorAgentScheduler monitorAgentScheduler = null;
	private ScheduledExecutorService         _scheduler = Executors
			.newScheduledThreadPool(1, new ThreadFactory() 
			{
				public Thread newThread(Runnable task) 
				{
					Thread thread = new Thread(task);
					thread.setName("monitorAgent_tomcat_collector");
					thread.setDaemon(true);
					return thread;
				}
			});

	/**
	 * Shutsdown the collector service
	 */
	public void shutdown() 
	{
		if( monitorAgentScheduler != null )
		{
			monitorAgentScheduler.queueShutdown();
			for( int i = 0; i < 30; i++ )
			{
				// every second for 30 seconds, check... if running.
				try { Thread.sleep( 1000L ); } catch ( Exception e ) { }
				if( !monitorAgentScheduler.isRunning() )
					break;
			}
			try { _scheduler.shutdownNow(); } catch ( Exception e ) { }
		}
		else
			log.info( "shutdown() call before instantiation?" );
	}
	
	/**
	 * Starts the collector Service
	 * 
	 * @throws ServiceException
	 * @throws RemoteException
	 * @throws WSFoundationException
	 */

	public void start(Vema vema, String gwosConfigFilename, String vemaMonitorProfileFilename, String hypervisorVmware, String connectorVmware, String mgmtServerVmware, String applicationTypeVmware )
	{
		monitorAgentScheduler = new MonitorAgentScheduler(
				vema, 
				gwosConfigFilename,
				vemaMonitorProfileFilename, 
				hypervisorVmware, 
				connectorVmware, 
				mgmtServerVmware,
				applicationTypeVmware
				);
		try 
		{
			_scheduler.execute(monitorAgentScheduler);
		} 
		catch (Exception e) 
		{
			log.info("Getting thread info for Monitor status failed. Message  " + e);
		}
	}

	public boolean testConnection(Properties prop) 
	{
		// TODO Auto-generated method stub
		return false;
	}

	public HashMap<String, Long> autoDiscoverComponents()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public HashMap<String, Long> autoDiscoverComponents(Properties prop) 
	{
		// TODO Auto-generated method stub
		return null;
	}
	
}
