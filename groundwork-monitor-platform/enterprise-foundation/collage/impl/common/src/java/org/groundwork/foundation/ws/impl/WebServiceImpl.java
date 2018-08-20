/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2007  GroundWork Open Source Solutions info@groundworkopensource.com

 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of version 2 of the GNU General Public License
 *     as published by the Free Software Foundation.

 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.

 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.groundwork.foundation.ws.impl;

import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.impl.CollageConvert;
import com.groundwork.collage.metrics.CollageMetrics;
import com.groundwork.collage.metrics.CollageTimer;
import org.groundwork.foundation.bs.actions.ActionService;
import org.groundwork.foundation.bs.category.CategoryService;
import org.groundwork.foundation.bs.device.DeviceService;
import org.groundwork.foundation.bs.host.HostService;
import org.groundwork.foundation.bs.hostgroup.HostGroupService;
import org.groundwork.foundation.bs.hostidentity.HostIdentityService;
import org.groundwork.foundation.bs.logmessage.ConsolidationService;
import org.groundwork.foundation.bs.logmessage.LogMessageService;
import org.groundwork.foundation.bs.metadata.MetadataService;
import org.groundwork.foundation.bs.monitorserver.MonitorServerService;
import org.groundwork.foundation.bs.performancedata.PerformanceDataService;
import org.groundwork.foundation.bs.rrd.RRDService;
import org.groundwork.foundation.bs.statistics.StatisticsService;
import org.groundwork.foundation.bs.status.StatusService;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public abstract class WebServiceImpl extends HibernateDaoSupport
{
	private CollageFactory _collageFactory =  CollageFactory.getInstance();

    private CollageMetrics collageMetrics = null;

    private CollageMetrics getCollageMetrics() {
        if (collageMetrics == null) {
            collageMetrics = _collageFactory.getCollageMetrics();
        }
        return collageMetrics;
    }

    public CollageTimer startMetricsTimer() {
        StackTraceElement element = Thread.currentThread().getStackTrace()[2];
        String className = element.getClassName().substring(element.getClassName().lastIndexOf('.') + 1);
        CollageMetrics collageMetrics = getCollageMetrics();
        return (collageMetrics == null ? null : collageMetrics.startTimer(className, element.getMethodName()));
    }

    public void stopMetricsTimer(CollageTimer timer) {
        CollageMetrics collageMetrics = getCollageMetrics();
        if (collageMetrics != null) getCollageMetrics().stopTimer(timer);
    }

	protected CollageConvert getConverter()
	{
		return new CollageConvert();
	}
	
	protected MetadataService getMetadataService ()
    {
		return _collageFactory.getMetadataService();
    }
    
	protected CategoryService getCategoryService ()
    {
		return _collageFactory.getCategoryService();
    }    

	protected DeviceService getDeviceService ()
    {
		return _collageFactory.getDeviceService();
    }  	

	protected HostService getHostService ()
    {
		return _collageFactory.getHostService();
    }

    protected HostIdentityService getHostIdentityService ()
    {
        return _collageFactory.getHostIdentityService();
    }

    protected HostGroupService getHostGroupService ()
    {
		return _collageFactory.getHostGroupService();
    }      
	
	protected LogMessageService getLogMessageService ()
    {
		return _collageFactory.getLogMessageService();
    }  
	
	protected ConsolidationService getConsolidationService ()
    {
		return _collageFactory.getConsolidationService();
    }  
	
	protected MonitorServerService getMonitorServerService ()
    {
		return _collageFactory.getMonitorServerService();
    }  
	
	protected StatisticsService getStatisticsService ()
    {
		return _collageFactory.getStatisticsService();
    }  
	
	protected StatusService getStatusService ()
    {
		return _collageFactory.getStatusService();
    }  
	
	protected PerformanceDataService getPerformanceDataService ()
    {
		return _collageFactory.getPerformanceDataService();
    }  	
	
	protected ActionService getActionService ()
    {
		return _collageFactory.getActionService();
    }  		
	
	protected RRDService getRRDService ()
    {
		return _collageFactory.getRRDService();
    }  		
}
