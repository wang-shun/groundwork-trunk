package org.groundwork.foundation.bs.events;

import org.groundwork.foundation.bs.BusinessService;

import java.util.concurrent.ConcurrentHashMap;

public interface EntityPublisher extends BusinessService {
	
	// Notify Attribute Constants
	public static final String NOTIFY_ATTR_HOST_ID = "HOSTID";
	public static final String NOTIFY_ATTR_SERVICESTATUS_ID = "SERVICESTATUSID";
	public static final String NOTIFY_ATTR_HOSTGROUP_ID = "HOSTGROUPID";
	public static final String NOTIFY_ATTR_SERVICEGROUP_ID = "SERVICEGROUPID";
    public static final String NOTIFY_ATTR_CUSTOMGROUP_ID = "CUSTOMGROUPID";
	public void publishEntity(String message);

	public ConcurrentHashMap<String, String> getDistinctEntityMap() ;

	public void setDistinctEntityMap(ConcurrentHashMap<String, String> distinctEntityMap) ;

	public void addEntityPublisherListener(EntityPublisherListener entityPublisherListener);

	public void removeEntityPublisherListener(EntityPublisherListener entityPublisherListener);

}
