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

package com.groundwork.collage;

import com.groundwork.collage.exception.CollageException;
import com.groundwork.collage.metrics.CollageMetrics;
import com.groundwork.collage.query.QueryTranslator;
import com.groundwork.collage.util.Autocomplete;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.BusinessServiceType;
import org.groundwork.foundation.bs.actions.ActionService;
import org.groundwork.foundation.bs.auditlog.AuditLogService;
import org.groundwork.foundation.bs.category.CategoryService;
import org.groundwork.foundation.bs.collector.CollectorConfigService;
import org.groundwork.foundation.bs.comment.CommentService;
import org.groundwork.foundation.bs.device.DeviceService;
import org.groundwork.foundation.bs.devicetemplateprofile.DeviceTemplateProfileService;
import org.groundwork.foundation.bs.events.EntityPublisher;
import org.groundwork.foundation.bs.events.EventService;
import org.groundwork.foundation.bs.events.PerformanceDataPublisher;
import org.groundwork.foundation.bs.host.HostService;
import org.groundwork.foundation.bs.hostblacklist.HostBlacklistService;
import org.groundwork.foundation.bs.hostgroup.HostGroupService;
import org.groundwork.foundation.bs.hostidentity.HostIdentityService;
import org.groundwork.foundation.bs.logmessage.ConsolidationService;
import org.groundwork.foundation.bs.logmessage.LogMessageService;
import org.groundwork.foundation.bs.logmessage.LogMessageWindowService;
import org.groundwork.foundation.bs.metadata.MetadataService;
import org.groundwork.foundation.bs.monitorserver.MonitorServerService;
import org.groundwork.foundation.bs.performancedata.PerformanceDataService;
import org.groundwork.foundation.bs.plugin.PluginService;
import org.groundwork.foundation.bs.rrd.RRDService;
import org.groundwork.foundation.bs.statistics.StatisticsService;
import org.groundwork.foundation.bs.status.StatusService;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Helper class to initialize the system. Single point of access to service frameworks such as
 * spring, hibernate.
 * 
 * CollageService
 * @author <a href="mailto:rruttimann@itgroundwork.com"> Roger Ruttimann</a>
 * @author <a href="mailto:philippe.paravicini@eCommerceStudio.com">Philippe Paravicini</a>
 * @version $Id: CollageFactory.java 17984 2010-09-10 22:52:11Z ashanmugam $
 */
public class CollageFactory implements CollageAccessor
{
    Log log = LogFactory.getLog(this.getClass());

    private Properties foundationProperties;
    private static Boolean autoCreateUnknownProps;    	
	
    /**
     * Query queue for pending or running hibernate sessions
     * submitted asynchronously.
     */
    private static ConcurrentHashMap<String,QueryObjectWrapper> querySessionMap = new ConcurrentHashMap<String, QueryObjectWrapper>(); 

    /** 
     * key that provides access to java Properties object containing environment variables 
     */
    public final static String FOUNDATION_PROPERTIES = "FoundationProperties";

    /* list of environment variables */
    public final static String KEY_AUTO_CREATE_UNKNOWN_PROPERTIES = "org.groundwork.collage.AutoCreateUnknownProperties";
    
    /* List of error messages */
    private final static String ERROR_NULL_SESSION_OBJECT = "QueryObjectWrapper Object is null and can't be added to query map.";

    /* list of entities */
    public final static String MONITOR_SERVER   = "com.groundwork.collage.model.MonitorServer";
    public final static String DEVICE           = "com.groundwork.collage.model.Device";
    public final static String HOST             = "com.groundwork.collage.model.Host";
    public final static String HOST_STATUS      = "com.groundwork.collage.model.HostStatus";
    public final static String SERVICE_STATUS   = "com.groundwork.collage.model.ServiceStatus";
    public final static String LOG_MESSAGE      = "com.groundwork.collage.model.LogMessage";
    public final static String APPLICATION_TYPE = "com.groundwork.collage.model.ApplicationType";
    public final static String PROPERTY_TYPE    = "com.groundwork.collage.model.PropertyType";
    public final static String CATEGORY			= "com.groundwork.collage.model.Category";
    public final static String CATEGORY_ENTITY	= "com.groundwork.collage.model.CategoryEntity";
    public final static String CONSOLIDATION_CRITERIA	= "com.groundwork.collage.model.ConsolidationCriteria";
    public final static String AUDIT_LOG        = "com.groundwork.collage.model.AuditLog";

    /* DAO list */
    public final static String COMMON_DAO	    = "com.groundwork.collage.CommonDAO";    
    public final static String FOUNDATION_DAO	= "org.groundwork.foundation.dao.FoundationDAO";

    /* list of services */
    public final static String ADMIN_SERVICE          	= "com.groundwork.collage.CollageAdmin";
    public final static String ADMIN_METADATA_SERVICE  	= "com.groundwork.collage.CollageAdminMetadata";
    public final static String ADMIN_APPLICATION_SERVICE = "com.groundwork.collage.CollageAdminApplication";
    public final static String QUERY_TRANSLATOR_SERVICE = "com.groundwork.collage.query.QueryTranslator";
    public final static String COLLAGE_METRICS_SERVICE = "com.groundwork.collage.CollageMetrics";
    public final static String FOUNDATION_LIFECYCLE_MANAGER_SERVICE = "com.groundwork.collage.FoundationLifecycleManager";

    /* Auxiliary classes*/
    public final static String QUERY_OBJECT_WRAPPER = "com.groundwork.collage.QueryObjectWrapper";
    
    /* Performance Data Persistence class */
    public final static String PERFORMANCE_DATA = "com.groundwork.collage.model.LogPerformanceData";
    
    /** 
     * The name denoting the Hibernate Session Factory in the Spring configuration 
     */
    public final static String HIBERNATE_SESSION_FACTORY = "hibernateSessionFactory";

    /** 
     * The name denoting the Spring Transaction Manager in the Spring configuration 
     */
    public final static String TRANSACTION_MANAGER = "hibernateTransactionManager";
    
    /** Boolean indicating factory has been initialized */
    private boolean _isInitialized = false;
    
    /** Singleton collage factory */  
    private static CollageFactory _collageFactory = null;
    
    /**
     * Wrapper around Spring and hibernate classes
     */
    private CollageFactory() 
    {
        super();
    } 
    
    public static CollageFactory getInstance()
    {
    	if (_collageFactory == null)
    		_collageFactory = new CollageFactory();
    
    	return _collageFactory;
    }
    
    /**
     * initializeSystem()
     * Loads the assembly files found in the class path; preferred way to initialize system.
     */
    public synchronized void initializeSystem() throws CollageException
    {
    	if (_isInitialized == true)
    		return;
    	
        ClassLoader loader = null;
        try
        {
            ////////////////////////////////////////////////////////////////////
            // necessary for running collage inside of a portlet application
            loader = Thread.currentThread().getContextClassLoader();
            ClassLoader loader2 = SpringAccessor.class.getClassLoader();
            Thread.currentThread().setContextClassLoader(loader2);
            /////////////////////////////////////////////////////////////////////
        }
        catch (CollageException ce)
        {
            System.out.println("Failed to load Spring assemblies. Error: " + ce);
            throw new CollageException("Failed to load Spring assemblies", ce);
        }
        finally
        {
            if (loader != null)
                Thread.currentThread().setContextClassLoader(loader);
        }
        
        _isInitialized = true;        
    }

    /**
     * Returns an instance of an object that implements the given interface
     * @param interfaceName Name of the interface to retrieve.
     * @return Object
     * @throws CollageException
     */
    public Object getAPIObject(String interfaceName)
    throws CollageException
    {
        this.initializeSystem();
        return SpringAccessor.getBean(interfaceName);
    }

    public Properties getFoundationProperties()
    {
      if (foundationProperties == null) {
        foundationProperties = (Properties)this.getAPIObject(FOUNDATION_PROPERTIES);
        if (foundationProperties == null) foundationProperties = new Properties();
      }
      return foundationProperties;
    }

    public boolean isAutoCreateUnknownProperties()
    {
      if (CollageFactory.autoCreateUnknownProps == null)
        CollageFactory.autoCreateUnknownProps = Boolean.valueOf(this.getFoundationProperties().getProperty(KEY_AUTO_CREATE_UNKNOWN_PROPERTIES));

      return CollageFactory.autoCreateUnknownProps.booleanValue();
    }

    /** 
     * This is used to override this flag programmatically - this method is
     * not part of the CollageAccessor interface, and is only exposed here so
     * that we can test both scenarios, irrespective of the value in the
     * configuration file; by passing a null we can force a reload of the value
     * in the configuration file
     */
    public void setAutoCreateUnknownProperties(Boolean enabled)
    {
      CollageFactory.autoCreateUnknownProps = enabled;
    }
    
    
    /**
     * load a given spring assembly into an existing Bean factory or creates
     * a new factory if it doesn't exists.
     * @param assemblyPath	Path to spring assembly. Must be in classpath usually in tha package META-INF directory
     * @throws CollageException
     */
    public void loadSpringAssembly(String assemblyPath) throws CollageException
    {
    	SpringAccessor.loadAssembly(assemblyPath);
    }

    public void unloadSpringAssembly() {
        SpringAccessor.unloadAssembly();
    }
    
    /**
     * Retrieves a QueryObjectWrapper object from an internal query object store
     * @param sessionObjectID
     * @return QueryObjectWrapper that encapsulates a hibernateSession
     */
    public QueryObjectWrapper getQuerySessionObjectByID(int sessionObjectID)
    {
    	return (QueryObjectWrapper)querySessionMap.get(new Integer(sessionObjectID).toString());
    }
    
    /**
     * Adds a given object to the Query Object store
     * @param sessionObject QueryObjectWrapper that encapsulates a hibernateSession
     * @return
     */
    public int setQuerySessionObject(QueryObjectWrapper sessionObject)
    {
    	
    	if (sessionObject == null)
    	{
    		log.error(ERROR_NULL_SESSION_OBJECT);
    		return 0;	// failed to create an object
    	}
    	
    	// Generate the Hash
    	Integer hash = new Integer(sessionObject.hashCode());
    	
    	// Add it to the map
    	querySessionMap.put(hash.toString(), sessionObject);
    	
    	// return the hash for this object
    	return hash.intValue();
    }
    
    /**
     * removeQuerySessionObject
     */
    public void removeQuerySessionObject(int SessionObjectID)
    {
    	querySessionMap.remove(new Integer(SessionObjectID).toString());
    }

    public MetadataService getMetadataService ()
    {
		return (MetadataService)this.getAPIObject(BusinessServiceType.MetadataBusinessService.getValue());					
    }
    
    public CategoryService getCategoryService ()
    {
		return (CategoryService)this.getAPIObject(BusinessServiceType.CategoryBusinessService.getValue());
    }

    public Autocomplete getCustomGroupAutocompleteService ()
    {
        return (Autocomplete)this.getAPIObject(BusinessServiceType.CustomGroupAutocompleteBusinessService.getValue());
    }

    public Autocomplete getServiceGroupAutocompleteService ()
    {
        return (Autocomplete)this.getAPIObject(BusinessServiceType.ServiceGroupAutocompleteBusinessService.getValue());
    }

    public DeviceService getDeviceService ()
    {
		return (DeviceService)this.getAPIObject(BusinessServiceType.DeviceBusinessService.getValue());
    }  	

    public HostService getHostService ()
    {
		return (HostService)this.getAPIObject(BusinessServiceType.HostBusinessService.getValue());
    }

    public Autocomplete getHostAutocompleteService ()
    {
        return (Autocomplete)this.getAPIObject(BusinessServiceType.HostAutocompleteBusinessService.getValue());
    }

    public HostGroupService getHostGroupService ()
    {
    	return (HostGroupService)this.getAPIObject(BusinessServiceType.HostGroupBusinessService.getValue());
    }

    public Autocomplete getHostGroupAutocompleteService ()
    {
        return (Autocomplete)this.getAPIObject(BusinessServiceType.HostGroupAutocompleteBusinessService.getValue());
    }

    public LogMessageService getLogMessageService ()
    {
    	return (LogMessageService)this.getAPIObject(BusinessServiceType.LogMessageBusinessService.getValue());
    }

    public LogMessageWindowService getLogMessageWindowService ()
    {
        return (LogMessageWindowService)this.getAPIObject(BusinessServiceType.LogMessageWindowBusinessService.getValue());
    }

    public ConsolidationService getConsolidationService ()
    {
    	return (ConsolidationService)this.getAPIObject(BusinessServiceType.ConsolidationBusinessService.getValue());
    }  
	
    public MonitorServerService getMonitorServerService ()
    {
		return (MonitorServerService)this.getAPIObject(BusinessServiceType.MonitorBusinessService.getValue());
    }  
	
    public StatisticsService getStatisticsService ()
    {
		return (StatisticsService)this.getAPIObject(BusinessServiceType.StatisticsBusinessService.getValue());
    }  
	
    public StatusService getStatusService ()
    {
		return (StatusService)this.getAPIObject(BusinessServiceType.StatusBusinessService.getValue());
    }

    public Autocomplete getStatusAutocompleteService ()
    {
        return (Autocomplete)this.getAPIObject(BusinessServiceType.StatusAutocompleteBusinessService.getValue());
    }

    public PerformanceDataService getPerformanceDataService ()
    {
		return (PerformanceDataService)this.getAPIObject(BusinessServiceType.PerformanceDataBusinessService.getValue());
    }
    
    public EventService getEventService ()
    {
		return (EventService)this.getAPIObject(BusinessServiceType.EventBusinessService.getValue());
    }  	
    
    public EntityPublisher getEntityPublisher()
    {
		return (EntityPublisher)this.getAPIObject(BusinessServiceType.EntityPublisher.getValue());
    }  	
    
    public ActionService getActionService ()
    {
		return (ActionService)this.getAPIObject(BusinessServiceType.ActionBusinessService.getValue());
    }  	  
    
    public PerformanceDataPublisher getPerformanceDataPublisher()
    {
		return (PerformanceDataPublisher)this.getAPIObject(BusinessServiceType.PerformanceDataPublisher.getValue());
    }  	
    
    public RRDService getRRDService ()
    {
		return (RRDService)this.getAPIObject(BusinessServiceType.RRDBusinessService.getValue());
    }  	
    
    public PluginService getPluginService ()
    {
		return (PluginService)this.getAPIObject(BusinessServiceType.PluginBusinessService.getValue());
    }

    public AuditLogService getAuditLogService()
    {
        return (AuditLogService)getAPIObject(BusinessServiceType.AuditLogBusinessService.getValue());
    }

    public HostIdentityService getHostIdentityService()
    {
        return (HostIdentityService)getAPIObject(BusinessServiceType.HostIdentityBusinessService.getValue());
    }

    public Autocomplete getHostIdentityAutocompleteService ()
    {
        return (Autocomplete)this.getAPIObject(BusinessServiceType.HostIdentityAutocompleteBusinessService.getValue());
    }

    public HostBlacklistService getHostBlacklistService()
    {
        return (HostBlacklistService)getAPIObject(BusinessServiceType.HostBlacklistBusinessService.getValue());
    }

    public DeviceTemplateProfileService getDeviceTemplateProfileService()
    {
        return (DeviceTemplateProfileService)getAPIObject(BusinessServiceType.DeviceTemplateProfileBusinessService.getValue());
    }

    public CollectorConfigService getCollectorConfigService()
    {
        return (CollectorConfigService)getAPIObject(BusinessServiceType.CollectorConfigBusinessService.getValue());
    }

    public CommentService getCommentService()
    {
        return (CommentService)getAPIObject(BusinessServiceType.CommentBusinessService.getValue());
    }

    public QueryTranslator getQueryTranslator()
    {
        return (QueryTranslator)this.getAPIObject(QUERY_TRANSLATOR_SERVICE);
    }

    public CollageMetrics getCollageMetrics() { return (CollageMetrics)this.getAPIObject(COLLAGE_METRICS_SERVICE); }

    public FoundationLifecycleManager getFoundationLifecycleManager() { return (FoundationLifecycleManager) this.getAPIObject(FOUNDATION_LIFECYCLE_MANAGER_SERVICE); }
}
