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
package org.groundwork.foundation.bs;

public class BusinessServiceType
{

    /**
	 * Business Service Classes
	 */
	private static final String CategoryBusinessServiceClass =
		"org.groundwork.foundation.bs.category.CategoryService";
    private static final String CustomGroupAutocompleteBusinessServiceId =
        "customGroupAutocompleteService";
    private static final String ServiceGroupAutocompleteBusinessServiceId =
        "serviceGroupAutocompleteService";
	private static final String DeviceBusinessServiceClass =
		"org.groundwork.foundation.bs.device.DeviceService";
	private static final String HostBusinessServiceClass =
		"org.groundwork.foundation.bs.host.HostService";
    private static final String HostAutocompleteBusinessServiceId =
        "hostAutocompleteService";
	private static final String HostGroupBusinessServiceClass =
		"org.groundwork.foundation.bs.hostgroup.HostGroupService";
    private static final String HostGroupAutocompleteBusinessServiceId =
        "hostGroupAutocompleteService";
	private static final String LogMessageBusinessServiceClass =
		"org.groundwork.foundation.bs.logmessage.LogMessageService";
    private static final String LogMessageWindowBusinessServiceClass =
        "org.groundwork.foundation.bs.logmessage.LogMessageWindowService";
	private static final String ConsolidationBusinessServiceClass =
		"org.groundwork.foundation.bs.logmessage.ConsolidationService";
	private static final String MetadataBusinessServiceClass = 
		"org.groundwork.foundation.bs.metadata.MetadataService";
	private static final String MonitorBusinessServiceClass = 
		"org.groundwork.foundation.bs.monitorserver.MonitorServerService";
	private static final String StatisticsBusinessServiceClass = 
		"org.groundwork.foundation.bs.statistics.StatisticsService";
	private static final String StatusBusinessServiceClass = 
		"org.groundwork.foundation.bs.status.StatusService";
    private static final String StatusAutocompleteBusinessServiceId =
        "statusAutocompleteService";
	private static final String FoundationSessionServiceClass =
		"org.groundwork.foundation.bs.foundationsession.FoundationSessionService";
	private static final String PerformanceDataBusinessClass = 
		"org.groundwork.foundation.bs.performancedata.PerformanceDataService";
	private static final String EventBusinessServiceClass = 
		"org.groundwork.foundation.bs.events.EventService";	
	private static final String ActionBusinessServiceClass = 
		"org.groundwork.foundation.bs.actions.ActionService";
	private static final String EntityPublisherClass = 
		"org.groundwork.foundation.bs.events.EntityPublisher";	
	private static final String PerformanceDataPublisherClass = 
		"org.groundwork.foundation.bs.events.PerformanceDataPublisher";	
	private static final String RRDServiceClass = 
		"org.groundwork.foundation.bs.rrd.RRDService";
	private static final String PluginServiceClass = 
		"org.groundwork.foundation.bs.plugin.PluginService";
    private static final String AuditLogServiceClass =
        "org.groundwork.foundation.bs.auditlog.AuditLogService";
    private static final String HostIdentityServiceClass =
        "org.groundwork.foundation.bs.hostidentity.HostIdentityService";
    private static final String HostIdentityAutocompleteBusinessServiceId =
        "hostIdentityAutocompleteService";
    private static final String HostBlacklistServiceClass =
        "org.groundwork.foundation.bs.hostblacklist.HostBlacklistService";
    private static final String DeviceTempateProfileServiceClass =
        "org.groundwork.foundation.bs.devicetemplateprofile.DeviceTemplateProfileService";
    private static final String BizServicesClass =
        "com.groundwork.collage.biz.BizServices";
    private static final String RTMMServicesClass =
        "com.groundwork.collage.biz.RTMMServices";
    private static final String CollectorConfigServiceClass =
        "org.groundwork.foundation.bs.collector.CollectorConfigService";
    private static final String CommentServiceClass =
        "org.groundwork.foundation.bs.comment.CommentService";
    private static final String SuggestionsServiceClass =
        "com.groundwork.collage.biz.SuggestionsService";

    // Hashmap of business service types - Must be initialized before instantiating
	// BusinessServiceType instances
    private static java.util.HashMap<String, BusinessServiceType> _table_ = 
    	new java.util.HashMap<String, BusinessServiceType>(18);
    
    // Pre-defined business service types
    public static final BusinessServiceType CategoryBusinessService =
    	new BusinessServiceType(CategoryBusinessServiceClass);
    public static final BusinessServiceType CustomGroupAutocompleteBusinessService =
        new BusinessServiceType(CustomGroupAutocompleteBusinessServiceId);
    public static final BusinessServiceType ServiceGroupAutocompleteBusinessService =
        new BusinessServiceType(ServiceGroupAutocompleteBusinessServiceId);
    public static final BusinessServiceType DeviceBusinessService =
    	new BusinessServiceType(DeviceBusinessServiceClass);
    public static final BusinessServiceType HostBusinessService = 
    	new BusinessServiceType(HostBusinessServiceClass);
    public static final BusinessServiceType HostAutocompleteBusinessService =
        new BusinessServiceType(HostAutocompleteBusinessServiceId);
    public static final BusinessServiceType HostGroupBusinessService =
    	new BusinessServiceType(HostGroupBusinessServiceClass);
    public static final BusinessServiceType HostGroupAutocompleteBusinessService =
        new BusinessServiceType(HostGroupAutocompleteBusinessServiceId);
    public static final BusinessServiceType LogMessageBusinessService =
    	new BusinessServiceType(LogMessageBusinessServiceClass);
    public static final BusinessServiceType LogMessageWindowBusinessService =
        new BusinessServiceType(LogMessageWindowBusinessServiceClass);
    public static final BusinessServiceType ConsolidationBusinessService =
    	new BusinessServiceType(ConsolidationBusinessServiceClass);
    public static final BusinessServiceType MetadataBusinessService = 
    	new BusinessServiceType(MetadataBusinessServiceClass);
    public static final BusinessServiceType MonitorBusinessService = 
    	new BusinessServiceType(MonitorBusinessServiceClass);
    public static final BusinessServiceType StatisticsBusinessService = 
    	new BusinessServiceType(StatisticsBusinessServiceClass);
    public static final BusinessServiceType StatusBusinessService = 
    	new BusinessServiceType(StatusBusinessServiceClass);
    public static final BusinessServiceType StatusAutocompleteBusinessService =
        new BusinessServiceType(StatusAutocompleteBusinessServiceId);
    public static final BusinessServiceType PerformanceDataBusinessService =
    	new BusinessServiceType(PerformanceDataBusinessClass);
    protected static final BusinessServiceType FoundationSessionService =
    	new BusinessServiceType(FoundationSessionServiceClass);
    public static final BusinessServiceType EventBusinessService =
    	new BusinessServiceType(EventBusinessServiceClass);
    public static final BusinessServiceType ActionBusinessService =
    	new BusinessServiceType(ActionBusinessServiceClass);
    public static final BusinessServiceType EntityPublisher =
    	new BusinessServiceType(EntityPublisherClass);
    public static final BusinessServiceType PerformanceDataPublisher =
    	new BusinessServiceType(PerformanceDataPublisherClass);
    public static final BusinessServiceType RRDBusinessService=
    	new BusinessServiceType(RRDServiceClass);
    public static final BusinessServiceType PluginBusinessService=
    	new BusinessServiceType(PluginServiceClass);
    public static final BusinessServiceType AuditLogBusinessService =
        new BusinessServiceType(AuditLogServiceClass);
    public static final BusinessServiceType HostIdentityBusinessService =
        new BusinessServiceType(HostIdentityServiceClass);
    public static final BusinessServiceType HostIdentityAutocompleteBusinessService =
        new BusinessServiceType(HostIdentityAutocompleteBusinessServiceId);
    public static final BusinessServiceType HostBlacklistBusinessService =
        new BusinessServiceType(HostBlacklistServiceClass);
    public static final BusinessServiceType DeviceTemplateProfileBusinessService =
        new BusinessServiceType(DeviceTempateProfileServiceClass);
    public static final BusinessServiceType BizBusinessService =
        new BusinessServiceType(BizServicesClass);
    public static final BusinessServiceType RTMMBusinessService =
        new BusinessServiceType(RTMMServicesClass);
    public static final BusinessServiceType CollectorConfigBusinessService =
        new BusinessServiceType(CollectorConfigServiceClass);
    public static final BusinessServiceType CommentBusinessService =
        new BusinessServiceType(CommentServiceClass);
    public static final BusinessServiceType SuggestionsBusinessService =
        new BusinessServiceType(SuggestionsServiceClass);

    private java.lang.String _value_;    

    // Constructor
    private BusinessServiceType(java.lang.String value) {
        _value_ = value;
        _table_.put(_value_, this);
    }

    public java.lang.String getValue() { return _value_;}
    
    public static BusinessServiceType fromValue(java.lang.String value)
          throws java.lang.IllegalArgumentException {
    	BusinessServiceType enumeration = (BusinessServiceType)
            _table_.get(value);
        if (enumeration==null) throw new java.lang.IllegalArgumentException();
        return enumeration;
    }
    
    public static BusinessServiceType fromString(java.lang.String value)
          throws java.lang.IllegalArgumentException {
        return fromValue(value);
    }
    
    public boolean equals(java.lang.Object obj) {return (obj == this);}
    
    public int hashCode() { return toString().hashCode();}
    
    public java.lang.String toString() { return _value_;}    
}
