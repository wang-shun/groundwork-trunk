<?php
/*
Copyright 2005 GroundWork Open Source Solutions, Inc. ("GroundWork")  
All rights reserved. Use is subject to GroundWork commercial license

	Collage PHP API
	Author: Taylor Dondich (tdondich@itgroundwork.com)
	Version:	1.0
	
	Description:
		Collage API is a PHP API to access the Collage Database
		Engine.  It uses adoDB for database abstraction so it can handle 
		any database on the other side.  This API Requires PHP 5 for 
		proper OO implementation.
	Changelog:
		2005-05-09:	Started work on basic functionality.
		2005-05-10:	First version
		2005-05-20:	Included many support functions (for all data retrieval)
				Also refined Event Query to provide rows and offset retrieval
		2005-06-03:	Further refinement / documentation

*/

/* Needed Includes */
// This assumes you have adodb in your include_path, if not, then point to where it resides.
include_once('adodb/adodb.inc.php');


/* Error Defiitions */
define('UNKNOWN_ERROR', 1000);
define('NOT_CONNECTED', 1001);
define('NO_SERVICES_FOR_HOSTGROUP', 1002);
define('INVALID_HOSTGROUP', 1003);
define('NO_HOSTS_IN_HOSTGROUP', 1004);
define('INVALID_HOSTNAME', 1005);
define('INVALID_TIME_FIELD', 1006);
define('DATE_RANGE_INVALID', 1007);
define('INVALID_DEVICE_IDENTIFICATION', 1008);
define('INVALID_MONITOR_SERVER', 1009);
define('INVALID_MONITOR_STATUS_ID', 1010);
define('INVALID_PRIORITY_ID', 1011);
define('INVALID_TYPE_RULE_ID', 1012);
define('INVALID_COMPONENT_ID', 1013);
define('INVALID_SEVERITY_ID', 1014);
define('INVALID_OPERATION_STATUS_ID', 1015);
define('INVALID_MESSAGE_FILTER_ID', 1016);
define('INVALID_CONSOLIDATION_CRITERIA_ID', 1017);
define('INVALID_LOG_PERF_DATA_ID', 1018);
define('INVALID_CHECK_TYPE_ID', 1019);
define('INVALID_LOG_MESSAGE_ID', 1020);
define('INVALID_OPERATION_STATUS_ID', 1021);
define('INVALID_STATE_TYPE_ID', 1022);
define('INVALID_SERVICE_AVAILABILITY_ID', 1023);
define('INVALID_HOST_AVAILABILITY_ID', 1024);
define('INVALID_MONITOR_SERVER_ID', 1025);
define('INVALID_DEVICE_ID', 1026);
define('INVALID_SCHEMA', 1027);
define('NO_HOSTGROUPS', 1028);
define('INVALID_ENTITY_TYPE', 1029);
define('INVALID_APPLICATION_TYPE_ID', 1030);
define('NO_CATEGORIES', 1031);
define('INVALID_CATEGORY', 1032);
define('NO_APPLICATION_TYPES_FOUND',1033);
define('INVALID_HOST_ID', 1034);
define('NO_HOSTS',1035);
define('NO_SERVICES',1036);
define('INVALID_MONITOR_STATUS', 1037);

// Entity Type constants
define('HOST_STATUS', 1);
define('SERVICE_STATUS', 2);
define('LOG_MESSAGE', 3);



/*
This is our Database object.  It allows a direct SQL query to foundation.  The connection can be controlled
manually or automatically.
*/
class CollageDB {
	// Database connection parameters (we don't want public scope)
	private $dbConnection;
	private $isManualConnection = false;
	private $type;
	private $server;
	private $username;
	private $pwd;
	private $database;
	private $error_num;
	private $applicationTypeID;
	private $appTypeTables = array("Host","HostGroup", "ServiceStatus","HostStatus", "LogMessage", "ApplicationEntityProperty");
	
	function __construct($type, $server, $username, $password, $database, $applicationType = NULL) {
		$this->type = $type;
		$this->server = $server;
		$this->username = $username;
		$this->pwd = $password;
		$this->database = $database;	
	}
	
	function __destruct()
	{
		if ($this->dbConnection != null && $this->dbConnection != false && $this->dbConnection->IsConnected())
		   $this->dbConnection->close();
	}
	public function setApplicationType($applicationType = NULL) {
		if($applicationType == NULL) {
			// remove applicationTypeID
			$this->applicationTypeID = NULL;
			// Act on all application types
		}
		else {
			// Check in database if application type exists.
			$query = "SELECT * FROM ApplicationType WHERE Name = '$applicationType'";
			$result = $this->selectQuery($query);
			// If not, act as if there was no application type provided (default)		
			if ($result == false || $result == null) { 
				$this->applicationTypeID = NULL;	
			}
			// If so, set $this->applicationTypeID = the ID of the application
			else {
				$this->applicationTypeID = $result[0]['ApplicationTypeID'];
			}
		}
			
	}
	
	public final function getApplicationType() {
		return $this->applicationTypeID;
	}
	
	
	function selectQuery($query, $propQuery = NULL) {
		
		$this->error_num = NULL;
		$returnedResults = NULL;
		
		// if the connection isn't being controlled externally, get a connection
		if (!$this->isManualConnection)
        {
    	    // make sure we're not already connected
            if (!$this->dbConnection || !$this->dbConnection->IsConnected())
            {
        		// get a connection
        		$this->dbConnection = ADONewConnection($this->type);
        		if ($this->dbConnection)
            		@$this->dbConnection->PConnect($this->server, $this->username,
        							$this->pwd,$this->database);
        							
        		if(!$this->dbConnection || !$this->dbConnection->IsConnected()) {
        			$this->error_num = NOT_CONNECTED;
        			return null;
        		}
        		$this->dbConnection->SetFetchMode(ADODB_FETCH_ASSOC);	// Associative Arrays returned
            }
	    
        }
        else 
        {
            if (!$this->dbConnection || !$this->dbConnection->IsConnected())
            {
                // if we fail to get a connection, return null.  The error_num value can be
                // checked by the caller.  It would be better to throw an exception of course,
                // but we're not set up for that...
                if ($this->openConnection() == null)
                    return null;
            }
        }
		
        // do the query and process the results
        if ($this->dbConnection != null && $this->dbConnection != false && $this->dbConnection->IsConnected())
        {
    		$result = $this->dbConnection->Execute($query);
    		if($result != false) {
    			while (! $result->EOF)
    			{
    				$returnedResults[] = $result->fields;
    				$result->MoveNext();
    			}
    		}
    		else 
		{
    		    // clean up
        		if (!$this->isManualConnection) 
        		  $this->dbConnection->close();
    			return null;
    		}
    		$result->close();
    		// if the connection isn't being controlled manually, we will close it now.
    		if (!$this->isManualConnection) 
    		  $this->dbConnection->close();
        }
        else {
            $this->error_num = NOT_CONNECTED;
            return null;
        }
		
		return $returnedResults;
	}
	
	public function openConnection()
	{
		// make sure we're not already connected
		if (!$this->dbConnection || !$this->dbConnection->IsConnected()) {
			// we're not already connected so get a connection
			$this->dbConnection = ADONewConnection($this->type);
			if ($this->dbConnection) {
				@$this->dbConnection->PConnect($this->server, $this->username, $this->pwd, $this->database);
			}
			if (!$this->dbConnection || !$this->dbConnection->IsConnected()) {
				$this->error_num = NOT_CONNECTED;
				return NULL;
			}
			$this->dbConnection->SetFetchMode(ADODB_FETCH_ASSOC);	// Associative Arrays returned
			$this->isManualConnection = true;
		}
		
	}
	
	public function closeConnection()
	{
		$this->dbConnection->close();
    	$this->isManualConnection = false;
	}
	
	function get_error_num() {
		return $this->error_num;
	}
	
}

// This class is the basis for all our Query classes, it contains simple constructor and error retrieval
class Collage {
	protected $dbInstance, $error_num;
	
	// Any extended classes NEED to call parent::__construct, this is required
	function __construct($passedDB) {
		$this->dbInstance = &$passedDB;	// Point to our new DB object
	}
	function isError() {
		return ($error_num ? $true : false);
	}
	// Support functions
	function getMonitorStatus($status_id = NULL) {
		$this->error_num = NULL;
		$query = "SELECT * FROM MonitorStatus";
		if($status_id != NULL)
			$query .= " WHERE MonitorStatusID = '$status_id'";
		$results = $this->dbInstance->selectQuery($query);
		if($status_id != NULL && !count($results)) {
			$this->error_num = INVALID_MONITOR_STATUS_ID;
			return NULL;
		}
		else {
			if($status_id)
				return $results[0];
			else
				return $results;
		}
	}
	function getMonitorStatusID($status = NULL) {
		$this->error_num = NULL;
		$query = "SELECT MonitorStatusID FROM MonitorStatus";
		if ($status != NULL) {
			$query .= " WHERE Name = '$status'";
		}
		$results = $this->dbInstance->selectQuery($query);
		if ($status != NULL && !count($results)) {
			$this->error_num = INVALID_MONITOR_STATUS;
			return NULL;
		}
		if ($status != NULL) 
			return $results[0];
		else 	
			return $results;
	}
	function getPriority($priority_id = NULL) {
		$this->error_num = NULL;
		$query = "SELECT * FROM Priority";
		if($priority_id != NULL)
			$query .= " WHERE PriorityID = '$priority_id'";
		$results = $this->dbInstance->selectQuery($query);
		if($priority_id != NULL && !count($results)) {
			$this->error_num = INVALID_PRIORITY_ID;
			return NULL;
		}
		else {
			return $results;
		}
	}
	function getTypeRule($type_id = NULL) {
		$this->error_num = NULL;
		$query = "SELECT * FROM TypeRule";
		if($type_id != NULL)
			$query .= " WHERE TypeID = '$type_id'";
		$results = $this->dbInstance->selectQuery($query);
		if($type_id != NULL && !count($results)) {
			$this->error_num = INVALID_TYPE_RULE_ID;
			return NULL;
		}
		else {
			return $results;
		}
	}	
	function getComponent($component_id = NULL) {
		$this->error_num = NULL;
		$query = "SELECT * FROM Component";
		if($component_id != NULL)
			$query .= " WHERE ComponentID = '$component_id'";
		$results = $this->dbInstance->selectQuery($query);
		if($component_id != NULL && !count($results)) {
			$this->error_num = INVALID_COMPONENT_ID;
			return NULL;
		}
		else {
			return $results;
		}
	}
	function getSeverity($severity_id = NULL) {
		$this->error_num = NULL;
		$query = "SELECT * FROM Severity";
		if($severity_id != NULL)
			$query .= " WHERE SeverityID = '$severity_id'";
		$results = $this->dbInstance->selectQuery($query);
		if($severity_id != NULL && !count($results)) {
			$this->error_num = INVALID_SEVERITY_ID;
			return NULL;
		}
		else {
			return $results;
		}
	}
	function getOperationStatus($operation_status_id = NULL) {
		$this->error_num = NULL;
		$query = "SELECT * FROM OperationStatus";
		if($operation_status_id != NULL)
			$query .= " WHERE OperationStatusID = '$operation_status_id'";
		$results = $this->dbInstance->selectQuery($query);
		if($operation_status_id != NULL && !count($results)) {
			$this->error_num = INVALID_OPERATION_STATUS_ID;
			return NULL;
		}
		else {
			return $results;
		}
	}
	function getMessageFilter($filter_id = NULL) {
		$this->error_num = NULL;
		$query = "SELECT * FROM MessageFilter";
		if($filter_id != NULL)
			$query .= " WHERE MessageFilterID = '$filter_id'";
		$results = $this->dbInstance->selectQuery($query);
		if($filter_id != NULL && !count($results)) {
			$this->error_num = INVALID_MESSAGE_FILTER_ID;
			return NULL;
		}
		else {
			return $results;
		}
	}
	function getConsolidationCriteria($criteria_id = NULL) {
		$this->error_num = NULL;
		$query = "SELECT * FROM ConsolidationCriteria";
		if($criteria_id != NULL)
			$query .= " WHERE ConsolidationCriteriaID = '$criteria_id'";
		$results = $this->dbInstance->selectQuery($query);
		if($criteria_id != NULL && !count($results)) {
			$this->error_num = INVALID_CONSOLIDATION_CRITERIA_ID;
			return NULL;
		}
		else {
			return $results;
		}
	}
	// It is recommended you always call this function with a $data_id variable.  Getting 
	// all the performance data inside a enterprise collage source will be pretty tedious.
	function getLogPerformanceData($data_id = NULL) {
		$this->error_num = NULL;
		$query = "SELECT * FROM LogPerformanceData";
		if($operation_status_id != NULL)
			$query .= " WHERE LogPerformanceDataID = '$data_id'";
		$results = $this->dbInstance->selectQuery($query);
		if($data_id != NULL && !count($results)) {
			$this->error_num = INVALID_LOG_PERF_DATA_ID;
			return NULL;
		}
		else {
			return $results;
		}
	}
	function getCheckType($check_type_id = NULL) {
		$this->error_num = NULL;
		$query = "SELECT * FROM checkType";
		if($check_type_id != NULL)
			$query .= " WHERE CheckTypeID = '$check_type_id'";
		$results = $this->dbInstance->selectQuery($query);
		if($check_type_id != NULL && !count($results)) {
			$this->error_num = INVALID_CHECK_TYPE_ID;
			return NULL;
		}
		else {
			return $results;
		}
	}
	// It is recommended you always call this function with a $message_id variable.  Getting
	// all the log message data inside a enterprise collage source will be pretty tedious.
	function getLogMessage($message_id = NULL) {
		$this->error_num = NULL;
		$query = "SELECT * FROM LogMessage";
		if($message_id != NULL)
			$query .= " WHERE LogMessageID = '$message_id'";
		$results = $this->dbInstance->selectQuery($query);
		if($message_id != NULL && !count($results)) {
			$this->error_num = INVALID_LOG_MESSAGE_ID;
			return NULL;
		}
		else {
			return $results;
		}
	}
	function getStateType($type_id = NULL) {
		$this->error_num = NULL;
		$query = "SELECT * FROM StateType";
		if($type_id != NULL)
			$query .= " WHERE StateTypeID = '$type_id'";
		$results = $this->dbInstance->selectQuery($query);
		if($type_id != NULL && !count($results)) {
			$this->error_num = INVALID_STATE_TYPE_ID;
			return NULL;
		}
		else {
			return $results;
		}
	}
	function getServiceAvailability($availability_id = NULL) {
		$this->error_num = NULL;
		$query = "SELECT * FROM ServiceAvailability";
		if($availability_id != NULL)
			$query .= " WHERE ServiceAvailiabilityID = '$availability_id'";
		$results = $this->dbInstance->selectQuery($query);
		if($availability_id != NULL && !count($results)) {
			$this->error_num = INVALID_SERVICE_AVAILABILITY_ID;
			return NULL;
		}
		else {
			return $results;
		}
	}
	function getHostAvailability($availability_id = NULL) {
		$this->error_num = NULL;
		$query = "SELECT * FROM HostAvailability";
		if($availability_id != NULL)
			$query .= " WHERE HostAvailabilityID = '$availability_id'";
		$results = $this->dbInstance->selectQuery($query);
		if($availability_id != NULL && !count($results)) {
			$this->error_num = INVALID_HOST_AVAILABILITY_ID;
			return NULL;
		}
		else {
			return $results;
		}
	}
	function getMonitorServer($server_id = NULL) {
		$this->error_num = NULL;
		$query = "SELECT * FROM MonitorServer";
		if($availability_id != NULL)
			$query .= " WHERE MonitorServerID = '$server_id'";
		$results = $this->dbInstance->selectQuery($query);
		if($server_id != NULL && !count($results)) {
			$this->error_num = INVALID_MONITOR_SERVER_ID;
			return NULL;
		}
		else {
			return $results;
		}
	}
	function getMonitorList($server_id = NULL) {
		$this->error_num = NULL;
		$query = "SELECT * FROM MonitorList";
		if($server_id != NULL)
			$query .= " WHERE MonitorServerID = '$server_id'";
		$results = $this->dbInstance->selectQuery($query);
		if($server_id != NULL && !count($results)) {
			$this->error_num = INVALID_MONITOR_SERVER_ID;
			return NULL;
		}
		else {
			return $results;
		}
	}
	function getMonitorListByDevice($device_id = NULL) {
		$this->error_num = NULL;
		$query = "SELECT * FROM MonitorList";
		if($device_id != NULL)
			$query .= " WHERE DeviceID = '$device_id'";
		$results = $this->dbInstance->selectQuery($query);
		if($device_id != NULL && !count($results)) {
			$this->error_num = INVALID_DEVICE_ID;
			return NULL;
		}
		else {
			return $results;
		}
	}
	function getDevice($device_id = NULL) {
		$this->error_num = NULL;
		$query = "SELECT * FROM Device";
		if($device_id != NULL)
			$query .= " WHERE DeviceID = '$device_id'";
		$results = $this->dbInstance->selectQuery($query);
		if($device_id != NULL && !count($results)) {
			$this->error_num = INVALID_DEVICE_ID;
			return NULL;
		}
		else {
			return $results;
		}
	}
	function getDeviceParents($device_id = NULL) {
		$this->error_num = NULL;
		$query = "SELECT * FROM DeviceParent";
		if($server_id != NULL)
			$query .= " WHERE DeviceID = '$device_id'";
		$results = $this->dbInstance->selectQuery($query);
		if($device_id != NULL && !count($results)) {
			$this->error_num = INVALID_DEVICE_ID;
			return NULL;
		}
		else {
			return $results;
		}
	}
	function getDeviceChildren($device_id = NULL) {
		$this->error_num = NULL;
		$query = "SELECT * FROM DeviceChild";
		if($server_id != NULL)
			$query .= " WHERE DeviceID = '$device_id'";
		$results = $this->dbInstance->selectQuery($query);
		if($device_id != NULL && !count($results)) {
			$this->error_num = INVALID_DEVICE_ID;
			return NULL;
		}
		else {
			return $results;
		}
	}
	function getSchemaInfo() {
		$this->error_num = NULL;
		$query = "SELECT * FROM SchemaInfo";
		$results = $this->dbInstance->selectQuery($query);
		if($device_id != NULL && !count($results)) {
			$this->error_num = UNKNOWN_ERROR;	// We should always have schema info
			return NULL;
		}
		else {
			return $results[0];
		}
	}	
	function getEntityTypeID($entity_type = NULL) {
		$this->error_num = NULL;
		$query = "SELECT * FROM EntityType";
		if ($entity_type != NULL)
			$query .= " WHERE Name = '$entity_type'";
		$results = $this->dbInstance->selectQuery($query);
		if ($entity_type != NULL && !count($results)) {
			$this->error_num = INVALID_ENTITY_TYPE;
			return NULL;
		}
		else {
			return $results;
		}
	}
	function getEntityTypeName($entityTypeID = NULL) {
		$query = "SELECT Name FROM EntityType";
		if ($entityTypeID != NULL)
			$query .= " WHERE EntityTypeID = '$entityTypeID'";
		$results = $this->dbInstance->selectQuery($query);
		if ($entityTypeID != NULL && !count($results)) {
			$this->error_NUM = INVALID_ENTITY_TYPE;
			return NULL;
		}
		return $results;		
	}
	function getApplicationType($application_type_id = NULL) {
		$query = "SELECT * FROM ApplicationType WHERE ApplicationTypeID = '".$application_type_id."'";
		$results = $this->dbInstance->selectQuery($query);
		if (!count($results)) {
			$this->ERROR_NUM = INVALID_APPLICATION_TYPE_ID;
			return NULL;
		}
		return $results[0];
	}
	function getApplicationTypeByName($application_type_name) {
		$query = "SELECT * FROM ApplicationType WHERE Name = '".$application_type_name."'";
		$results = $this->dbInstance->selectQuery($query);
		if (!count($results)) {
			$this->ERROR_NUM = INVALID_APPLICATION_TYPE_ID;
			return NULL;
		}
		return $results[0];
	}
	function getApplicationTypes() {
		$query = "SELECT * FROM ApplicationType";
		$results = $this->dbInstance->selectQuery($query);
		if (!count($results)) {
			$this->error_NUM = NO_APPLICATION_TYPES_FOUND;
			return NULL;
		}
		return $results;
	}
	function get_error_num() {
		return $this->error_num;
	}
	function get_propertyIDs($entity_type_id, $application_type_id = NULL) {
		$query = "SELECT PropertyTypeID FROM ApplicationEntityProperty WHERE EntityTypeID = '".$entity_type_id."'";
		if ($application_type_id != NULL) {
			$query .= " AND ApplicationTypeID = '".$application_type_id."'";
		}
		else if ($this->dbInstance->getApplicationType() != NULL) {
			$query .= " AND ApplicationTypeID = '".$this->dbInstance->getApplicationType()."'";
		}
		$tempAppEntProperties = $this->dbInstance->selectQuery($query);
		if ($tempAppEntProperties == NULL)
			return NULL;
		// put just the property ids into an array.
		$appEntPropIDs = array();
		$counter = 0;
		foreach ($tempAppEntProperties as $appEnt) {
			$appEntPropIDs[$counter] = $appEnt['PropertyTypeID'];
			$counter++;
		}
		return $appEntPropIDs;
	}
	
	function get_propertyName($propID) {
		$query = "SELECT Name FROM PropertyType WHERE PropertyType.PropertyTypeID = '".$propID."'";
		$result = $this->dbInstance->selectQuery($query);
		if ($result == NULL) {
			return NULL;
		}
		return $result;
	}
	
	function getTargetProperties($propList) {
		// Match the property in propList with the value contained in targetProps
		// this method makes no mysql queries.
		$targetProps = NULL;
		for ($counter = 0; $counter < count($propList); $counter++) {
			if ($propList[$counter]['isDate']) {	
				$targetProps[$counter]['field'] = 'ValueDate';	
			}
			else if ($propList[$counter]['isBoolean']) {	
				$targetProps[$counter]['field'] = 'ValueBoolean';	
			}
			else if ($propList[$counter]['isString']) {	
				$targetProps[$counter]['field'] = 'ValueString';	
			}
			else if ($propList[$counter]['isDouble']) {	
				$targetProps[$counter]['field'] = 'ValueDouble';	
			}
			else if ($propList[$counter]['isInteger']) {	
				$targetProps[$counter]['field'] = 'ValueInteger';	
			}
			else if ($propList[$counter]['isLong']) {	
				$targetProps[$counter]['field'] = 'ValueLong';	
			}
			$targetProps[$counter]['PropertyTypeID'] = $propList[$counter]['PropertyTypeID'];
			$targetProps[$counter]['Name'] = $propList[$counter]['Name'];
		}
		return $targetProps;		
	}
}
class CollageCategoryQuery extends Collage {
	function getRootCategories() {
		$query = "SELECT DISTINCT * FROM Category WHERE Category.CategoryID NOT IN (SELECT CategoryHierarchy.CategoryId FROM CategoryHierarchy)";
		$result = $this->dbInstance->selectQuery($query);
		if(!count($result)) {	// We didn't get a result
			$this->error_num = NO_CATEGORIES;
			return NULL;
		}
		return $result;				
	}
	function getCategoriesForCategoryName($categoryName) {
		$query = "SELECT * FROM CategoryHierarchy WHERE CategoryHierarchy.ParentID IN (SELECT Category.CategoryID FROM Category WHERE Category.Name = '".$categoryName."')";
		$result = $this->dbInstance->selectQuery($query);		
		if(!count($result)) {	// We didn't get a result
			$this->error_num = NO_CATEGORIES;
			return NULL;
		}
		return $result;				
	}
	function getCategoriesForCategoryID($categoryID) {
		$result = $this->dbInstance->selectQuery("SELECT * FROM CategoryHierarchy WHERE ParentID = '".$categoryID."'");
		if(!count($result)) {	// We didn't get a result
			$this->error_num = NO_CATEGORIES;
			return NULL;
		}
		return $result;				
	}
	function getCategoryByName($categoryName) {
		$result = $this->dbInstance->selectQuery("SELECT * FROM Category WHERE Name = '".$categoryName."'");
		if(!count($result)) {	// We didn't get a result
			$this->error_num = INVALID_CATEGORY;
			return NULL;
		}
		return $result[0];
	}
	function getCategoryByID($categoryID) {
		$query = "SELECT * FROM Category WHERE CategoryID = '".$categoryID."'";
		$result = $this->dbInstance->selectQuery($query);
		if(!count($result)) {	// We didn't get a result
			$this->error_num = INVALID_CATEGORY;
			return NULL;
		}
		return $result[0];		
	}
	function getEntityTypesForCategoryID($categoryID) {
		$query = "SELECT DISTINCT EntityTypeID FROM CategoryEntity WHERE CategoryID = '".$categoryID."'";
		$result = $this->dbInstance->selectQuery($query);
		if(!count($result)) {	// We didn't get a result
			$this->error_num = NO_CATEGORIES;
			return NULL;
		}
		return $result;				
	}
	function getEntityTypesForCategoryName($categoryName) {
		$query = "SELECT DISTINCT EntityTypeID FROM CategoryEntity WHERE CategoryEntity.CategoryID IN (SELECT Category.CategoryID FROM Category WHERE Category.Name = '".$categoryName."')";
		$result = $this->dbInstance->selectQuery($query);
		if(!count($result)) {	// We didn't get a result
			$this->error_num = NO_CATEGORIES;
			return NULL;
		}
		return $result;				
	}
	function getObjectsForEntityTypeForCategoryID($categoryID, $entityTypeID, $nested=false) {
		$query = "SELECT ObjectID FROM CategoryEntity WHERE (CategoryID = '".$categoryID."' AND EntityTypeID = '".$entityTypeID."')";
		$result = $this->dbInstance->selectQuery($query);
		if(!count($result)) {	// We didn't get a result
			$this->error_num = NO_CATEGORIES;
			return NULL;
		}
		return $result;				
	}
	function getObjectsForEntityTypeForCategoryName($categoryName, $entityTypeName, $nested=false) {
		$query = "SELECT ObjectID FROM CategoryEntity WHERE CategoryEntity.CategoryID IN (SELECT Category.CategoryID FROM Category, CategoryEntity WHERE (Category.Name = '".$categoryName."' AND CategoryEntity.EntityTypeID IN (SELECT EntityTypeID FROM EntityType WHERE Name = '".$entityTypeName."')))";
		$result = $this->dbInstance->selectQuery($query);
		if(!count($result)) {	// We didn't get a result
			$this->error_num = NO_CATEGORIES;
			return NULL;
		}
		return $result;				
	}
}

class CollageHostGroupQuery extends Collage {	
	
	/**
	 * This function will return all ServiceStatus information for all 
	 * services for all Hosts which are assigned to the Host Group defined by $hostGroup.
	 * 
	 * @param $hostGroup
	 */
	// NOTE: This function does not appear to be used anywhere
	function getServicesForHostGroup($hostGroup) {
		$this->error_num = NULL;
		$listOfServices = NULL;
		$tempHostIDs = $this->getHostsForHostGroup($hostGroup);
		$numOfHosts = count($tempHostIDs);
		$tempHostQuery = new CollageHostQuery($this->dbInstance);
		for($counter = 0; $counter < $numOfHosts; $counter++) {
			$results = $tempHostQuery->getServicesForHost($tempHostIDs[$counter]['HostName']);
			if($results)
			foreach($results as $tempRecord) {
				$listOfServices[] = $tempRecord;
			}
		}
		return $listOfServices;
	}
	
	/**
	 * This function will return all HostGroups.
	 * 
	 */	
	function getHostGroups() {
		$this->error_num = NULL;
		$listOfHostGroups = NULL;
		$results = $this->dbInstance->selectQuery("SELECT * FROM HostGroup ORDER BY Name ASC");
		if(!count($results)) {	// We didn't get a result
			$this->error_num = NO_HOSTGROUPS;
			return NULL;
		}
		foreach($results as $tempRecord) {
			$listOfHostGroups[] = $tempRecord;
		}
		return $listOfHostGroups;
	}
	
	/**
	 * Will return all associated Hosts that are assigned to the 
	 * HostGroup defined by $hostGroup.
	 *
	 * @param string $hostGroup
	 * @return all hosts for the specified hostgroup.
	 */
	function getHostsForHostGroup($hostGroup) {
		$this->error_num = NULL;
		// First retrieve hostgroupid
		$tempHostGroup = $this->getHostGroup($hostGroup);
		$tempHostGroupID = $tempHostGroup['HostGroupID'];
		// Now that we have a hostgroup id, we must retrieve all hosts inside that hostgroup
		$results = $this->dbInstance->selectQuery("SELECT H.HostID, H.HostName FROM Host H INNER JOIN HostGroupCollection HGC ON H.HostID=HGC.HostID WHERE HGC.HostGroupID='$tempHostGroupID' ORDER BY H.HostName ASC");
		if(!count($results)) {	// We didn't get a result
			$this->error_num = NO_HOSTS_IN_HOSTGROUP;
			return NULL;
		}
		return $results;
	}
	
	/**
	 * Will return HostGroup specified by $hgName.
	 *
	 * @param string $hgName
	 * @return unknown
	 */
	function getHostGroup($hgName) {
		$this->error_num = NULL;
		$results = $this->dbInstance->selectQuery("SELECT * FROM HostGroup WHERE Name = '$hgName'");
		if(!$results) {
			$this->error_num = INVALID_HOSTGROUP;
			return NULL;
		}
		else {
			$tempHostGroup = $results[0];
		}
		return $tempHostGroup;
	}
	
	/**
	 * Will return HostGroup specified by $hgID.
	 *
	 * @param number $hgID
	 * @return unknown
	 */
	function getHostGroupByID($hgID) {
		$this->error_num = NULL;
		$results = $this->dbInstance->selectQuery("SELECT * FROM HostGroup WHERE HostGroupID = '$hgID'");
		if (!$results) {
			$this->error_num = INVALID_HOSTGROUP;
			return NULL;
		}
		return $results[0];
	}
	
	/**
	 * Returns a count of the HostGroups currently defined in the database.
	 *
	 * @return unknown
	 */
	function getCount() {
		$this->error_num = NULL;
		$results = $this->dbInstance->selectQuery("SELECT COUNT(HostGroupID) AS rowcount FROM HostGroup");
		if (!$results) {
			$this->error_num = NO_HOSTGROUPS;
			return NULL;
		}
		return $results[0]['rowcount'];
	}
	
	/**
	 * Determines if the host specified by $hostID is a member of 
	 * the hostgroup specified by $hostGroupID.  If it is, the function
	 * returns true and false otherwise.
	 *
	 * @param unknown_type $hostID
	 * @param unknown_type $hostGroupID
	 * @return unknown
	 */
	function hostInHostGroup($hostID, $hostGroupID) {
		$this->error_num = NULL;
		$result = $this->dbInstance->selectQuery("SELECT HostID from HostGroupCollection WHERE HostID='$hostID' AND HostGroupID = '$hostGroupID'");
		if (!$result) {
			$this->error_num = NO_HOSTS_IN_HOSTGROUP;
			return false;
		}
		return true;
	}
		
	/**
	 * Determines if the service specified by $serviceID is a member of 
	 * the hostgroup specified by $hostGroupID.  If it is, the function
	 * returns true and false otherwise.
	 *
	 * @param unknown_type $serviceID
	 * @param unknown_type $hostGroupID
	 * @return unknown
	 */
	function serviceInHostGroup($serviceID, $hostGroupID) {
		$this->error_num = NULL;
		$result = $this->dbInstance->selectQuery("SELECT * FROM HostGroupCollection, ServiceStatus where HostGroupCollection.HostID = ServiceStatus.HostID AND HostGroupCollection.HostGroupID=$hostGroupID AND ServiceStatus.ServiceStatusID = $serviceID");
		if (!$result) {
			$this->error_num = SV_NO_HOSTS_IN_HOSTGROUP;
			return false;
		}
		return true;
	}
}

class CollageHostQuery  extends Collage {
	
	static private $hoststatusPropertyIDs;
	/**
	 * Returns the host specified by the host name in $host
	 *
	 * @param string $host
	 * @return unknown
	 */
	function getHost($host) {
		$this->error_num = NULL;
		$tempHostData = $this->dbInstance->selectQuery("SELECT * FROM Host WHERE HostName = '$host'");
		if(!count($tempHostData)) {
			$this->error_num = INVALID_HOSTNAME;
			return NULL;
		}
		else {
			return $tempHostData[0];	// Return first record (which is our host)
		}
	}
	
	/**
	 * Returns the host specified by the host id in $id
	 *
	 * @param number $id
	 * @return unknown
	 */
	function getHostByID($id) {
		$this->error_num = NULL;
		$tempHostData = $this->dbInstance->selectQuery("SELECT * FROM Host WHERE HostID = '$id'");
		if(!count($tempHostData)) {
			$this->error_num = INVALID_HOSTNAME;
			return NULL;
		}
		else {
			return $tempHostData[0];	// Return first record (which is our host)
		}
	}
	
	/**
	 * Returns the host associated with the device specified by $deviceID
	 *
	 * @param unknown_type $deviceID
	 * @return unknown
	 */
	function getHostByDeviceID($deviceID) {
		$this->error_num = NULL;
		$tempHostData = $this->dbInstance->selectQuery("SELECT * FROM Host WHERE DeviceID = '$deviceID'");
		if(!count($tempHostData)) {
			$this->error_num = INVALID_HOSTNAME;
			return NULL;
		}
		else {
			return $tempHostData[0];
		}
	}
	
	/**
	 * Returns the ids of the services associated with the host specified by 
	 * the host id in $hostID.
	 *
	 * @param unknown_type $hostID
	 * @return unknown
	 */
	function getServiceIDsForHost($hostID) {
		$this->error_num = NULL;
		$serviceIDs = $this->dbInstance->selectQuery("SELECT ServiceStatusID FROM ServiceStatus WHERE HostID = '$hostID' ORDER BY ServiceDescription ASC");
		if (!count($serviceIDs)) {		
			$this->error_num = INVALID_HOST_ID;		
			return NULL;
		}
		return $serviceIDs;
	}
	
	/**
	 * Returns the services that belong to the host specified by the 
	 * host name in $host.
	 *
	 * @param string $host
	 * @return unknown
	 */
	function getServicesForHost($host) {
       $query ="Select ss.*,
                    pt.Name,
                    CASE WHEN pt.isDate = 1 THEN ssp.ValueDate
                    WHEN pt.isBoolean = 1 THEN ssp.ValueBoolean
                    WHEN pt.isString = 1 THEN ssp.ValueString
                    WHEN pt.isDouble = 1 THEN ssp.ValueDouble
                    WHEN pt.isInteger = 1 THEN ssp.ValueInteger
                    WHEN pt.isLong = 1 THEN ssp.ValueLong
                    END AS Value
                    from ServiceStatus ss
                    inner join Host h ON h.HostID = ss.HostID
                    left join ServiceStatusProperty ssp ON ss.ServiceStatusID = ssp.ServiceStatusID
                    left join PropertyType pt on ssp.PropertyTypeID = pt.PropertyTypeID
                    where h.HostName='$host'
                    ORDER BY ss.ServiceDescription ASC";
	    $results = $this->dbInstance->selectQuery($query);
	    $service = array();
	    $returnServices = array();
	    if ($results == null)
	    {
	        return $returnServices;
	    }
	    $currentServiceID = -99;
        foreach ($results as $result) {
            if ($currentServiceID != $result['ServiceStatusID']) {
                $currentServiceID = $result['ServiceStatusID'];
                $service = $result;
                $service[$result['Name']] = $result['Value'];
                $returnServices[] = $service;
            }
            else {
                $service[$result['Name']] = $result['Value'];
            }
        }
	    return $returnServices;
	}
	
	/**
	 * Returns all hosts in the database.
	 *
	 * @return unknown
	 */
	function getHosts() {
		$this->error_num = NULL;
		return $this->dbInstance->selectQuery("SELECT * FROM Host ORDER BY HostName ASC");
	}
	
	/**
	 * Returns the latest host status for the host name specified in $host
	 *
	 * @param string $host
	 * @return unknown
	 */
	function getHostStatusForHost($host) {
		// This returns the LATEST Host Status for the host (Not all Status logs)
		$query = "Select hs.*, 
					pt.Name, 
					CASE WHEN pt.isDate = 1 THEN hsp.ValueDate
					WHEN pt.isBoolean = 1 THEN hsp.ValueBoolean
					WHEN pt.isString = 1 THEN hsp.ValueString
					WHEN pt.isDouble = 1 THEN hsp.ValueDouble
					WHEN pt.isInteger = 1 THEN hsp.ValueInteger
					WHEN pt.isLong = 1 THEN hsp.ValueLong
					END AS Value
					from Host h
					inner join HostStatus hs ON h.HostID = hs.HostStatusID
					left join HostStatusProperty hsp ON hs.HostStatusID = hsp.HostStatusID
					left join PropertyType pt on hsp.PropertyTypeID = pt.PropertyTypeID
					where h.HostName = '$host'";

		$results = $this->dbInstance->selectQuery($query);
	    if ($results == null)
	       return array();
	    $returnArray = $results[0];
		$returnArray[$results[0]['Name']] = $results[0]['Value'];
		for ($index = 1; $index < sizeof($results); $index++) {
			$returnArray[$results[$index]['Name']] = $results[$index]['Value']; 
		}
		return $returnArray;
	}
	
	function getMonitorStatusForHostStatus($hostName) {
	   $query = "Select ms.Name From Host h Inner join HostStatus hs ON h.HostID = hs.HostStatusID Inner Join MonitorStatus ms ON hs.MonitorStatusID = ms.MonitorStatusID Where h.HostName ='$hostName'";    
	   $monitorStatus = $this->dbInstance->selectQuery($query);
	   if ($monitorStatus == null)
	       return null;
	   return $monitorStatus[0];
	}
	
	/**
	 * Allows hoststatus information to be retrieved for any combination
	 * of hoststatus properties.
	 *
	 * @param CollageFilter $filter
	 * @return unknown
	 */
	// TODO: there are 3 queries made here...
	function getHostsByFilter($filter)
	{		
		if (!count($filter)) { 
			// TODO: add error code here.
			return NULL;	
		}
		$appEntPropIDs = $this->get_propertyIDs(HOST_STATUS);

		// TODO: Move this into an utility method?
		// Get all of the properties from the property table that match this ent/app type.
		$query = "SELECT * FROM PropertyType WHERE (";
		$size = count($appEntPropIDs);
		for ($counter = 0; $counter < $size; $counter++) {
			$query .= "PropertyTypeID = '".$appEntPropIDs[$counter]."'";
			if ($counter < $size - 1)
				$query .= " OR ";
			else 
				$query .= ")";
		}
		$propList = $this->dbInstance->selectQuery($query);
		$targetProps = $this->getTargetProperties($propList);
				
		$hostStatusFields = array("MonitorStatusID","ApplicationTypeID","CheckType");
		
		/* Let's call our new filter method */
		$querySubset = $filter->createQuery("HostStatus", $hostStatusFields, "HostStatusProperty", $targetProps);
		
		// only fields from the HostStatus table, no HostStatusProperty fields requested
		if (stripos($querySubset, "HostStatusProperty") === false)
			$query = "SELECT DISTINCT HostStatus.HostStatusID, Host.HostName
						 FROM HostStatus 
						 Inner join Host ON HostStatus.HostStatusID = Host.HostID WHERE ".$querySubset;
		// only fields from the HostStatusProperty table, no HostStatus fields requested
		else if (stripos($querySubset, "HostStatus") === false)
			$query = "SELECT DISTINCT HostStatusProperty.HostStatusID, Host.HostName FROM HostStatusProperty
						 Inner join Host ON HostStatusProperty.HostStatusID = Host.HostID WHERE ". $querySubset;
		// fields from both HostStatusProperty and HostStatus were requested
		else
			$query = "SELECT DISTINCT HostStatus.HostStatusID, Host.HostName 
						FROM HostStatus
						Inner join HostStatusProperty ON HostStatus.HostStatusID = HostStatusProperty.HostStatusID 
						Inner join Host ON HostStatus.HostStatusID = Host.HostID WHERE ".$querySubset;

		$hosts = $this->dbInstance->selectQuery($query);
		// return the host ID's.
		return $hosts;
	}
	
	/**
	 * Returns the Device information which is associated with 
	 * the host name provided in $host.
	 *
	 * @param string $host
	 * @return unknown
	 */
	function getDeviceForHost($host) {
		$this->error_num = NULL;
		$tempDevice = $this->dbInstance->selectQuery("Select d.* from Device d, Host h WHERE d.DeviceID = h.DeviceID AND h.HostID = 1");
		return $tempDevice[0];
	}
	
	/**
	 * Returns a count of all hosts in the database.
	 *
	 * @return unknown
	 */
	function getCount() {
		$this->error_num = NULL;
		$results = $this->dbInstance->selectQuery("SELECT COUNT(HostID) AS rowcount FROM Host");
		if (!$results) {
			$this->error_num = NO_HOSTS;
			return NULL;
		}
		return $results[0]['rowcount'];
	}
}

class CollageEventQuery  extends Collage {
	private function getEventsForSomething($fieldToSearch, $fieldValue, $timeField, $fromDate, $toDate, $rows, $offset) {
		$date_expr = '/^\d{4}-\d{2}-\d{2}$/';
		if(isset($timeField) && ($timeField != 'FirstInsertDate' && $timeField != 'LastInsertDate')) {
			$this->error_num = INVALID_TIME_FIELD;
			return NULL;
		}
		else if(isset($timeField) && (!preg_match($date_expr, $fromDate) || !preg_match($date_expr, $toDate))) {
			$this->error_num = DATE_RANGE_INVALID;
			return NULL;
		}
		else if(!isset($timeField)) {
			$query = "SELECT * FROM LogMessage, ApplicationType WHERE $fieldToSearch = '$fieldValue'";
			if(isset($timeField)) {
				$query .= " AND ".$timeField." >= '$fromDate' AND ".$timeField." <= '$toDate'";
			}
			$propIDs = $this->get_propertyIDs(LOG_MESSAGE);
			$appTypeID = $this->dbInstance->getApplicationType();
			if ($appTypeID) {
				$query .= " AND ApplicationType.ApplicationTypeID = '$appTypeID'";
			}
			$query .= " ORDER BY LogMessageID DESC";			
			if(isset($rows)) {
				if(isset($offset)) {
					$query .= " LIMIT ${offset},${rows}";
				}
				else {
					$query .= " LIMIT ${rows}";
				}
			}
			$tempEvents = $this->dbInstance->selectQuery($query);
		
			if (count($tempEvents)) {
				foreach ($tempEvents as &$event) {
					$query = "SELECT * FROM LogMessageProperty, PropertyType WHERE LogMessageProperty.LogMessageID = '".$event['LogMessageID']."'";
					$numOfPropIDs = count($propIDs);
					if ($numOfPropIDs) {
							$query .= " AND (";
						for($counter = 0; $counter < $numOfPropIDs; $counter++) {
							$query .= " (LogMessageProperty.PropertyTypeID = '".$propIDs[$counter]."' AND PropertyType.PropertyTypeID = '".$propIDs[$counter]."')";
							if($counter < ($numOfPropIDs - 1)) {
								$query .= " OR ";
							}
						}
					}
					$query .=")";
					$tempResult = $this->dbInstance->selectQuery($query);
					// now put the returned property names and corresponding values into the return array
					foreach ($tempResult as $result) {		
						if ($result['isDate'])
							$event[$result['Name']] = $result['ValueDate'];
						else if ($result['isBoolean'])
							$event[$result['Name']] = $result['ValueBoolean'];
						else if ($result['isString']) 
							$event[$result['Name']] = $result['ValueString'];				
						else if ($result['isDouble'])
							$event[$result['Name']] = $result['ValueDouble'];
						else if ($result['isInteger'])
							$event[$result['Name']] = $result['ValueInteger'];
						else if ($result['isLong'])
							$event[$result['Name']] = $result['ValueLong'];
					}
				}
			}
			return $tempEvents;
		}
	}
	
	/*
		$filter - a fully populated CollageFilter object.
		$numOfResults - the number of results returned from final query.
		$rows - an optional limit on the number of rows to return.
		$offset - where to start when returning the result set.
		$fieldSortOne - the field to sort by.  This must be preceeded by the table name as it is possible
		to sort by a field from a foreign table.  For example, Host.HostName will cause the results to be
		sorted by host name.
		$fieldSortTwo - a secondary field to sort by.  Follows the same prerequisites as $fieldSortOne with the addition that 
		this field will not be checked or used if $fieldSortOne is not set.  
		$sortOrder - ASC or DESC.  The order in which to sort the results.
	*/
	function getEventsByFilter($filter, &$numOfResults = NULL, $rows = NULL, $offset = NULL, $fieldSortOne = NULL, $sortOrder = NULL, $fieldSortTwo=NULL) {
		if (!count($filter)) { 
			// return error code?
			return null;	
		}
		$appEntPropIDs = $this->get_propertyIDs(LOG_MESSAGE);
		
		if (isset($fieldSortOne)) {		
			$foreignTableNameOne = strtok($fieldSortOne,".");
			$foreignFieldNameOne =	strtok(".");
		}
		
		if (isset($fieldSortTwo)) {		
			$foreignTableNameTwo = strtok($fieldSortTwo,".");
			$foreignFieldNameTwo =	strtok(".");
		}
		
		// TODO: move this into a utility function?
		// Get all of the properties from the property table that match this ent/app type.
		$query = "SELECT * FROM PropertyType WHERE (";
		$size = count($appEntPropIDs);
		for ($counter = 0; $counter < $size; $counter++) {
			$query .= "PropertyTypeID = '".$appEntPropIDs[$counter]."'";
			if ($counter < $size - 1)
				$query .= " OR ";
			else 
				$query .= ")";
		}
		$propList = $this->dbInstance->selectQuery($query);

		$targetProps = $this->getTargetProperties($propList);
		
		$logMessageFields = array("SeverityID","ApplicationSeverityID","PriorityID","ComponentID","DeviceID","HostStatusID","MonitorStatusID","ServiceStatusID","OperationStatusID","TypeID","TextMessage","FirstInsertDate","LastInsertDate","ReportDate","MsgCount","ApplicationName","ApplicationTypeID");
		$logMessageFKeys = array("Severity" => "SeverityID","Piority" => "PriorityID","Component" => "ComponentID","Host" => "DeviceID","Device" => "DeviceID","MonitorStatus" => "MonitorStatusID","ServiceStatus" => "ServiceStatusID","HostStatus" => "HostStatusID","OperationStatus" => "OperationStatusID","TypeRule" => "TypeID","ApplicationType" => "ApplicationTypeID");
		
		/* Let's call our new filter method */
		$querySubset = $filter->createQuery("LogMessage", $logMessageFields, "LogMessageProperty", $targetProps);
		if (!$querySubset) {
			// what is appropriate behavior here?  return error?  or null?
			//print("<br/>OH NO!  NO QUERY SUBSET RETURNED!");
		}
		
		$query = "SELECT LogMessageID FROM ";
		
		// only fields from the LogMessage table, no LogMessageProperty fields requested
		if (stripos($querySubset, "LogMessageProperty") === false) {
			// if the first order by field is not set OR it's set to LogMessage and there's no value for
			// the second order by field OR it's set to LogMessage and the second order by field is
			// set to LogMessage also, do the basic query.
			if (!isset($fieldSortOne) || (($foreignTableNameOne == "LogMessage") && (!isset($fieldSortTwo))) || (($foreignTableNameOne == "LogMessage") && (isset($fieldSortTwo) && $foreignTableNameTwo == "LogMessage")))  {
				$query = "SELECT LogMessageID FROM LogMessage WHERE " . $querySubset;
			}
			// otherwise...
			else {
				$query .= "LogMessage";
				// check the first order by field.  If it's not LogMessage, add it to the query
				if (isset($fieldSortOne) && ($foreignTableNameOne != "LogMessage")) {
					$query .= ", $foreignTableNameOne";
				}
				// now the second order by field.  Make sure it's set and if so, and it's not LogMessage, add it to the query
				if (isset($fieldSortTwo) && ($foreignTableNameTwo != "LogMessage")) {
					$query .= ", ".$foreignTableNameTwo;
				}
				$query .= " WHERE " .$querySubset;
				$keys = array_keys($logMessageFKeys);
				if (in_array($foreignTableNameOne, $keys)) {
					$query .= " AND (";
					// use the appropriate key in the foreign table
					$query .= $foreignTableNameOne."." . $logMessageFKeys[$foreignTableNameOne] . "=";
					// that corresponds to the FK in the LogMessage table.
					$query .= "LogMessage.". $logMessageFKeys[$foreignTableNameOne] .")";
				}
				if (isset($fieldSortTwo)) {
					if (in_array($foreignTableNameTwo, $keys)) {
						$query .= " AND (";
						// use the appropriate key in the foreign table
						$query .= $foreignTableNameTwo."." . $logMessageFKeys[$foreignTableNameTwo] . "=";
						// that corresponds to the FK in the LogMessage table.
						$query .= "LogMessage.". $logMessageFKeys[$foreignTableNameTwo] .")";
					}
				}
			}
			$countQuery = "SELECT COUNT(LogMessageID) AS rowcount FROM LogMessage WHERE " . $querySubset;
		}
		// only fields from the LogMessageProperty table, no LogMessage fields requested
		else if (stripos($querySubset, "LogMessage") === false) {
			// if the first order by field is not set OR it's set to LogMessageProperty and there's no value for
			// the second order by field OR it's set to LogMessageProperty and the second order by field is
			// set to LogMessageProperty also, do the basic query.
			if (!isset($fieldSortOne) || (($foreignTableNameOne == "LogMessageProperty") && (!isset($fieldSortTwo))) || (($foreignTableNameOne == "LogMessageProperty") && (isset($fieldSortTwo) && $foreignTableNameTwo == "LogMessageProperty")))  {
				$query = "SELECT LogMessageID FROM LogMessageProperty, PropertyType WHERE " . $querySubset;
			}
			else {
				$query .= "LogMessageProperty, PropertyType";
				// check the first order by field.  If it's not LogMessageProperty, add it to the query
				if (isset($fieldSortOne) && ($foreignTableNameOne != "LogMessageProperty")) {
					$query .= ", $foreignTableNameOne";
				}
				// now the second order by field.  Make sure it's set and if so, and it's not 
				// LogMessageProperty, add it to the query
				if (isset($fieldSortTwo) && ($foreignTableNameTwo != "LogMessageProperty")) {
					$query .= ", ".$foreignTableNameTwo;
				}
				$query .= " WHERE " .$querySubset . " AND (";
				$keys = array_keys($logMessageFKeys);
				if (in_array($foreignTableNameOne, $keys)) {
					// use the appropriate key in the foreign table
					$query .= $foreignTableNameOne."." . $logMessageFKeys[$foreignTableNameOne] . "=";
					// that corresponds to the FK in the LogMessage table.
					$query .= "LogMessage.". $logMessageFKeys[$foreignTableNameOne] .")";
				}
			}
			$countQuery = "SELECT COUNT(LogMessageID) AS rowcount FROM LogMessageProperty, PropertyType WHERE " . $querySubset;
		}
		// fields from both LogMessageProperty and LogMessage were requested
		else {
			// if the first order by field is not set OR it's set to LogMessageProperty or LogMessage and there's no value for
			// the second order by field OR it's set to LogMessageProperty or LogMessage and the second order by field is
			// set to LogMessageProperty or LogMessage also, do the basic query.
			if (!isset($fieldSortOne) || ((($foreignTableNameOne == "LogMessageProperty") || ($foreignTableNameOne == "LogMessage")) && (!isset($fieldSortTwo))) || ((($foreignTableNameOne == "LogMessageProperty") || ($foreignTableNameOne == "LogMessageProperty")) && (isset($fieldSortTwo) && ($foreignTableNameTwo == "LogMessageProperty") || ($foreignTableNameOne == "LogMessageProperty"))))  {
				$query = "SELECT DISTINCT LogMessage.LogMessageID FROM LogMessage, LogMessageProperty, PropertyType WHERE " . $querySubset;
			}
			else {
				$query = "SELECT LogMessage.LogMessageID FROM LogMessage, LogMessageProperty, PropertyType";
				// check the first order by field.  If it's not LogMessageProperty or LogMessage, add it to the query
				if (isset($fieldSortOne) && ($foreignTableNameOne != "LogMessage") && ($foreignTableNameOne != "LogMessageProperty") ) {
					$query .= ", $foreignTableNameOne";
				}
				// now the second order by field.  Make sure it's set and if so, and it's not 
				// LogMessageProperty or LogMessage, add it to the query
				if (isset($fieldSortTwo) && (($foreignTableNameTwo != "LogMessage") && ($foreignTableNameTwo != "LogMessageProperty"))) {				
					$query .= ", ".$foreignTableNameTwo;				
				}
				$query .= " WHERE " .$querySubset . " AND (";				
				$keys = array_keys($logMessageFKeys);
				if (in_array($foreignTableNameOne, $keys)) {
					// use the appropriate key in the foreign table
					$query .= $foreignTableNameOne."." . $logMessageFKeys[$foreignTableNameOne] . "=";
					// that corresponds to the FK in the LogMessage table.
					$query .= "LogMessage.". $logMessageFKeys[$foreignTableNameOne] .")";
				}
			}
			$countQuery = "SELECT COUNT(LogMessageID) AS rowcount FROM LogMessage, LogMessageProperty, PropertyType WHERE " . $querySubset;
		}

		if ($fieldSortOne != NULL) {
			$query .= " ORDER BY $fieldSortOne";
			if ($fieldSortTwo != NULL) {
				$query .= ", $fieldSortTwo";
			}
			$query .=  " " .$sortOrder;
		}
		else {
			$query .= " ORDER BY LogMessageID DESC";
		}
		// if necessary, limit by rows and offset parameters
		if (isset($rows)) {
			if (isset($offset)) {
				$query .= " LIMIT ${offset},${rows}";
			}
			else {
				$query .= " LIMIT ${rows}";
			}
		}
		$events = $this->dbInstance->selectQuery($query);

		$tempCount = $this->dbInstance->selectQuery($countQuery);
		if ($tempCount) {
			$numOfResults = $tempCount[0]['rowcount'];
		}
		else {
			$numOfResults = 0;
		}
		// return the events.
		return $events;
	}
	
	function randstring($lenstr) {
		mt_srand((double)microtime()*1000000);
		while ($i<$lenstr) {
			$value .= chr(mt_rand(97,122));
			$i++;
		}
		return $value;
	}
	
	/**
	 * Return the events that have been logged for the device specified in $identification
	 *
	 * @param unknown_type $identification
	 * @param unknown_type $timeField
	 * @param unknown_type $fromDate
	 * @param unknown_type $toDate
	 * @param unknown_type $rows
	 * @param unknown_type $offset
	 * @return unknown
	 */
	// TODO: we make 2 queries here
	function getEventsForDevice($identification, $timeField = NULL, $fromDate = NULL, $toDate = NULL, $rows = NULL, $offset = NULL) {
		$this->error_num = NULL;
		// Retrieve DeviceID
		$tempDeviceInfo = $this->dbInstance->selectQuery("SELECT * FROM Device WHERE Identification = '$identification'");
		if(!count($tempDeviceInfo)) {
			$this->error_num = INVALID_DEVICE_IDENTIFICATION;
			return NULL;
		}
		else {
			// We've got identification, return events
			return $this->getEventsForSomething('DeviceID', $tempDeviceInfo[0]['DeviceID'], $timeField, $fromDate, $toDate, $rows, $offset);
		}

	}
	
	/**
	 * Returns the events for the service specified by $serviceDescription on host
	 * specified by $hostName
	 *
	 * @param unknown_type $serviceDescription
	 * @param unknown_type $hostName
	 * @param unknown_type $timeField
	 * @param unknown_type $fromDate
	 * @param unknown_type $toDate
	 * @param unknown_type $rows
	 * @param unknown_type $offset
	 * @return unknown
	 */
	// TODO: 2 queries here
	function getEventsForService($serviceDescription, $hostName, $timeField = NULL, $fromDate = NULL, $toDate = NULL, $rows = NULL, $offset = NULL) {
		$this->error_num = NULL;
		// Retrieve service status id
		$tempServiceQuery = new CollageServiceQuery($this->dbInstance);
		$tempServiceData = $tempServiceQuery->getService($serviceDescription, $hostName);
		if($tempServiceData == NULL) {
			$this->error_num = $tempServiceQuery->get_error_num();
			unset($tempServiceQuery);
			return NULL;
		}
		else {
			unset($tempServiceQuery);
			return $this->getEventsForSomething('ServiceStatusID', $tempServiceData['ServiceStatusID'], $timeField, $fromDate, $toDate, $rows, $offset);
		}
	}
	
	/**
	 * Returns the events that have been logged for the host specified by $hostName
	 *
	 * @param unknown_type $hostName
	 * @param unknown_type $timeField
	 * @param unknown_type $fromDate
	 * @param unknown_type $toDate
	 * @param unknown_type $rows
	 * @param unknown_type $offset
	 * @return unknown
	 */
	// TODO: 2 queries
	function getEventsForHost($hostName, $timeField = NULL, $fromDate = NULL, $toDate = NULL, $rows = NULL, $offset = NULL) {
		$this->error_num = NULL;
		$tempHostQuery = new CollageHostQuery($this->dbInstance);
		$tempDeviceData = $tempHostQuery->getDeviceForHost($hostName);
		if($tempDeviceData == NULL) {
			$this->error_num = $tempHostQuery->get_error_num();
			unset($tempHostQuery);
			return NULL;
		}
		else {
			unset($tempHostQuery);
			return $this->getEventsForSomething('DeviceID', $tempDeviceData['DeviceID'], $timeField, $fromDate, $toDate, $rows, $offset);
		}
	}
	
	/**
	 * Returns the event with the event ID specified by $eventID
	 *
	 * @param unknown_type $eventID
	 * @return unknown
	 */
	// todo: 2 queries...
	function getEventByID($eventID) {
		$this->error_num = NULL;
		$query = "SELECT * FROM LogMessage WHERE LogMessageID = '$eventID'";
		$tempEventData = $this->dbInstance->selectQuery($query);
		if(!count($tempEventData)) {
			$this->error_num = INVALID_LOG_MESSAGE_ID;
			return NULL;
		}
		$result = $this->getLogMessageProperties($tempEventData[0]);
		return $result;
	}
	
	// TODO: multiple queries
	function getLogMessageProperties(&$returnEventArray) {
		// get all the properties for entity type ServiceStatus
		$appEntPropIDs = $this->get_propertyIDs(LOG_MESSAGE);
		$query = "SELECT * FROM LogMessageProperty, PropertyType WHERE LogMessageProperty.LogMessageID = '".$returnEventArray['LogMessageID']. "' AND (";
		$size = sizeof($appEntPropIDs);
		for ($i = 0; $i < $size; $i++) { 
			$query .= "(PropertyType.PropertyTypeID = '$appEntPropIDs[$i]' AND LogMessageProperty.PropertyTypeID = '$appEntPropIDs[$i]')";	
			if ($i < ($size-1))
				$query .= " OR ";
			else 
				$query .= ")";
		}
		$tempStatus = $this->dbInstance->selectQuery($query);

		if(count($tempStatus)) {
		// now put the returned property names and corresponding values into the return array
			foreach ($tempStatus as $status) {		
				if ($status['isDate'])
					$returnEventArray[$status['Name']] = $status['ValueDate'];
				else if ($status['isBoolean'])
					$returnEventArray[$status['Name']] = $status['ValueBool'];
				else if ($status['isString']) 
					$returnEventArray[$status['Name']] = $status['ValueString'];				
				else if ($status['isDouble'])
					$returnEventArray[$status['Name']] = $status['ValueDouble'];
				else if ($status['isInteger'])
					$returnEventArray[$status['Name']] = $status['ValueInteger'];
				else if ($status['isLong'])
					$returnEventArray[$status['Name']] = $status['ValueLong'];
			}
		}
		return $returnEventArray;
	}
	
	/**
	 * Returns a count of all events that have been logged in the database.
	 *
	 * @return unknown
	 */
	function getNumberOfEvents() {
		$this->error_num = NULL;
		$tempResult = $this->dbInstance->selectQuery("SELECT COUNT(*) FROM LogMessage");
		if(!count($tempResult)) {
			$this->error_num = INVALID_SCHEMA;
		}
		else {
			return $tempResult[0]['COUNT(*)'];
		}
	}
}

// Builds a binary tree respresentation of a query
class CollageFilter {
	
	private $filter = NULL;
	private $operation = NULL;
	private $leftChild = NULL;
	private $rightChild = NULL;
	
	function setFilter($key, $op, $value) {
		$this->filter = array('key' => $key, 'op' => $op, 'value' => $value);
		$this->operation = NULL;
		$this->leftChild = NULL;
		$this->rightChild = NULL;
	}
	
	function getFilter() {
		return $this->filter;
	}
	
	function setOperation($operator, $leftChild, $rightChild) {
		$this->operation = $operator;
		$this->leftChild = $leftChild;
		$this->rightChild = $rightChild;
		$this->filter = NULL;
	}
	
	function getLeftChild() {
		return $this->leftChild;
	}
	
	function getRightChild() {
		return $this->rightChild;
	}
			
	// recursive query substring creation
	function createQuery($mainTableName, $listofFields, $propertyTable, $targetProps) {
		
		if(isset($this->operation)) {
			if(!isset($this->leftChild) || !isset($this->rightChild)) {
				return false;
			}
			// We're an operation, so we need to do recursive deep calls
			$tempQuery = "(";
			$returnQuery = $this->leftChild->createQuery($mainTableName, $listofFields, $propertyTable, $targetProps);
			if(!$returnQuery) {
				return false;
			}
			$tempQuery .= $returnQuery;
			$tempQuery = $tempQuery . " " . $this->operation . " ";
			$returnQuery = $this->rightChild->createQuery($mainTableName, $listofFields, $propertyTable, $targetProps);
			if(!$returnQuery)
				return false;
			$tempQuery .= $returnQuery;
			$tempQuery .= ")";
		}
		else {
			// We're a filter
			$tempFilter = $this->filter;
			// Perform any eval and changes to tempfilter only
			if(in_array($tempFilter['key'], $listofFields)) {
				$tempFilter['key'] = $mainTableName . "." . $tempFilter['key'];
				$tempQuery = $tempFilter['key']." ".$tempFilter['op']." '".$tempFilter['value']."'";
			}
			else {
				// Oh noes!
				// We need to get our PropertyType ID
				$found = 0;
				if(count($targetProps)) {
					foreach($targetProps as $property) {
						if($property['Name'] == $tempFilter['key']) {
							$tempFilter['key'] = $propertyTable . "." .$property['field'];
							$found = 1;
							$tempQuery = "(" . $tempFilter['key']." ".$tempFilter['op']." '".$tempFilter['value']."' AND " .  $propertyTable . ".PropertyTypeID = '" . $property['PropertyTypeID'] . "')";
							break;
						}
					}
				}
				if(!$found)
					return false;
			}
		}
		return $tempQuery;
	}
}

class CollageServiceQuery extends Collage {

	static private $servicestatusPropertyIDs;
	
	/**
	 * Returns the service associated with the host specified in
	 * $hostName and service with description (name) $serviceDescription
	 *
	 * @param string $serviceDescription
	 * @param string $hostName
	 * @return unknown
	 */
	function getService($serviceDescription, $hostName) {
		$query ="Select ss.*,
					pt.Name,
					CASE WHEN pt.isDate = 1 THEN ssp.ValueDate
					WHEN pt.isBoolean = 1 THEN ssp.ValueBoolean
					WHEN pt.isString = 1 THEN ssp.ValueString
					WHEN pt.isDouble = 1 THEN ssp.ValueDouble
					WHEN pt.isInteger = 1 THEN ssp.ValueInteger
					WHEN pt.isLong = 1 THEN ssp.ValueLong
					END AS Value
					from ServiceStatus ss
					inner join Host h ON h.HostID = ss.HostID
					left join ServiceStatusProperty ssp ON ss.ServiceStatusID = ssp.ServiceStatusID
					left join PropertyType pt on ssp.PropertyTypeID = pt.PropertyTypeID
					where ss.ServiceDescription = '$serviceDescription' and h.HostName='$hostName'";
		$results = $this->dbInstance->selectQuery($query);
		$returnArray = $results[0];
		$returnArray[$results[0]['Name']] = $results[0]['Value'];
		for ($index = 1; $index < sizeof($results); $index++) {
			$returnArray[$results[$index]['Name']] = $results[$index]['Value']; 
		}
		return $returnArray;

	}
	
	// TODO: multiple queries
	function getServiceStatusProperties(&$returnServiceArray) {
		// get all the properties for entity type ServiceStatus
		$appEntPropIDs = $this->get_propertyIDs(SERVICE_STATUS);
		$query = "SELECT * FROM ServiceStatusProperty, PropertyType WHERE ServiceStatusProperty.ServiceStatusID = '".$returnServiceArray['ServiceStatusID']. "' AND (";
		$size = sizeof($appEntPropIDs);
		for ($i = 0; $i < $size; $i++) { 
			$query .= "(PropertyType.PropertyTypeID = '$appEntPropIDs[$i]' AND ServiceStatusProperty.PropertyTypeID = '$appEntPropIDs[$i]')";	
			if ($i < ($size-1))
				$query .= " OR ";
			else 
				$query .= ")";
		}
		//print("<br/>Query for service status properties is: ".$query."<br/>");
		$tempStatus = $this->dbInstance->selectQuery($query);
		
		if ($tempStatus == NULL)
			return NULL;
	
		// now put the returned property names and corresponding values into the return array
		foreach ($tempStatus as $status) {		
			if ($status['isDate'])
				$returnServiceArray[$status['Name']] = $status['ValueDate'];
			else if ($status['isBoolean'])
				$returnServiceArray[$status['Name']] = $status['ValueBoolean'];
			else if ($status['isString']) 
				$returnServiceArray[$status['Name']] = $status['ValueString'];				
			else if ($status['isDouble'])
				$returnServiceArray[$status['Name']] = $status['ValueDouble'];
			else if ($status['isInteger'])
				$returnServiceArray[$status['Name']] = $status['ValueInteger'];
			else if ($status['isLong'])
				$returnServiceArray[$status['Name']] = $status['ValueLong'];
		}
		return $returnServiceArray;
	}
	
	/**
	 * Returns the service associated with the service status id
	 * specified in $statusID
	 *
	 * @param unknown_type $statusID
	 * @return unknown
	 */
	function getServiceByStatusID($statusID) {
		$query ="Select ss.*,
					pt.Name,
					CASE WHEN pt.isDate = 1 THEN ssp.ValueDate
					WHEN pt.isBoolean = 1 THEN ssp.ValueBoolean
					WHEN pt.isString = 1 THEN ssp.ValueString
					WHEN pt.isDouble = 1 THEN ssp.ValueDouble
					WHEN pt.isInteger = 1 THEN ssp.ValueInteger
					WHEN pt.isLong = 1 THEN ssp.ValueLong
					END AS Value
					from ServiceStatus ss
					inner join Host h ON h.HostID = ss.HostID
					left join ServiceStatusProperty ssp ON ss.ServiceStatusID = ssp.ServiceStatusID
					left join PropertyType pt on ssp.PropertyTypeID = pt.PropertyTypeID
					where ss.ServiceStatusID = '$statusID'";
		$results = $this->dbInstance->selectQuery($query);
		$returnArray = $results[0];
		$returnArray[$results[0]['Name']] = $results[0]['Value'];
		for ($index = 1; $index < sizeof($results); $index++) {
			$returnArray[$results[$index]['Name']] = $results[$index]['Value']; 
		}
		return $returnArray;
	}
	
	function getMonitorStatusForService($serviceID) {
	   $query = "Select ms.Name From ServiceStatus ss Inner Join MonitorStatus ms ON ss.MonitorStatusID = ms.MonitorStatusID Where ss.ServiceStatusID ='$serviceID'";    
	   $monitorStatus = $this->dbInstance->selectQuery($query);
	   return $monitorStatus[0];
	}

	/**
	 * Returns all Services in the Database.
	 *
	 * @return a list of ServiceStatus's
	 */
	function getServices() {
		 $query ="Select ss.*,
					pt.Name,
					CASE WHEN pt.isDate = 1 THEN ssp.ValueDate
					WHEN pt.isBoolean = 1 THEN ssp.ValueBoolean
					WHEN pt.isString = 1 THEN ssp.ValueString
					WHEN pt.isDouble = 1 THEN ssp.ValueDouble
					WHEN pt.isInteger = 1 THEN ssp.ValueInteger
					WHEN pt.isLong = 1 THEN ssp.ValueLong
					END AS Value
					from ServiceStatus ss
					left join ServiceStatusProperty ssp ON ss.ServiceStatusID = ssp.ServiceStatusID
					left join PropertyType pt on ssp.PropertyTypeID = pt.PropertyTypeID
					ORDER BY ServiceDescription ASC";
		$results = $this->dbInstance->selectQuery($query);
		$service = array();
		$returnServices = array();
	    if ($results == null)
            return $returnServices;
	    $currentServiceID = -99;
		foreach ($results as $result) {
			if ($currentServiceID != $result['ServiceStatusID']) {
				$currentServiceID = $result['ServiceStatusID'];
				$service = $result;
				$service[$result['Name']] = $result['Value'];
				$returnServices[] = $service;
			}
			else {
				$service[$result['Name']] = $result['Value'];
			}
		}
		return $returnServices;
	}
	/**
	 * Get's a list of Services, based on filters defined in $filter against ServiceStatus
	 *
	 * @param CollageFilter $filter
	 * @return unknown
	 */
	// TODO: Multiple queries
	function getServicesByFilter($filter) {
		if (!count($filter)) { 
			return NULL;	
		}
		
		$appEntPropIDs = $this->get_propertyIDs(SERVICE_STATUS);
	
		// TODO: move this into a utility function?
		// Get all of the properties from the property table that match this ent/app type.
		$query = "SELECT * FROM PropertyType WHERE (";
		$size = count($appEntPropIDs);
		for ($counter = 0; $counter < $size; $counter++) {
			$query .= "PropertyTypeID = '".$appEntPropIDs[$counter]."'";
			if ($counter < $size - 1)
				$query .= " OR ";
			else 
				$query .= ")";
		}
		
		$propList = $this->dbInstance->selectQuery($query);
		$targetProps = $this->getTargetProperties($propList);

		$svcStatusFields = array("StateTypeID","CheckTypeID","LastHardStateID","MonitorStatusID","HostID","ApplicationTypeID","ServiceDescription");
		
		/* Let's call our new filter method */
		$querySubset = $filter->createQuery("ServiceStatus", $svcStatusFields, "ServiceStatusProperty", $targetProps);
		
		// only fields from the ServiceStatus table, no ServiceStatusProperty fields requested
		if (stripos($querySubset, "ServiceStatusProperty") === false)
			$query = "SELECT DISTINCT ServiceStatus.ServiceStatusID FROM ServiceStatus WHERE " . $querySubset;
		// only fields from the ServiceStatusProperty table, no ServiceStatus fields requested
		else if (stripos($querySubset, "ServiceStatus") === false)
			$query = "SELECT DISTINCT ServiceStatusProperty.ServiceStatusID FROM ServiceStatusProperty WHERE " . $querySubset;
		// fields from both ServiceStatusProperty and ServiceStatus were requested
		else
			$query = "SELECT DISTINCT ServiceStatusProperty.ServiceStatusID FROM ServiceStatus, ServiceStatusProperty WHERE ServiceStatus.ServiceStatusID = ServiceStatusProperty.ServiceStatusID AND " . $querySubset;
		//print("<br/>FINAL QUERY" . $query);
		
		$services = $this->dbInstance->selectQuery($query);
		// return the serviceStatus's.
		return $services;
	}	
	
	/**
	 * Returns a count of the services in the database.
	 *
	 * @return unknown
	 */
	function getCount() {
		$this->error_num = NULL;
		$results = $this->dbInstance->selectQuery("SELECT COUNT(ServiceStatusID) AS rowcount FROM ServiceStatus");
		if (!$results) {
			$this->error_num = NO_SERVICES;
			return NULL;
		}
		return $results[0]['rowcount'];
	}
}

class CollageMonitorServerQuery  extends Collage {
	function getMonitorServers() {
		$this->error_num = NULL;
		$tempMonitorServers = $this->dbInstance->selectQuery("SELECT * FROM MonitorServer");
		return $tempMonitorServers;
	}
	function getHostsForMonitorServer($MonitorServer) {
		$this->error_num = NULL;
		// Ugly, since we have deviceID's, and then we have Host ID's and from what I can tell, there's nothing to 
		// base a difference between them
		// First let's get the MonitorServerID
		$tempMonitorServerData = $this->dbInstance->selectQuery("SELECT * FROM MonitorServer WHERE MonitorServerName = '$MonitorServer'");
		if(!count($tempMonitorServerData)) {
			$this->error_num = INVALID_MONITOR_SERVER;
			return NULL;
		}
		$tempDeviceList = $this->dbInstance->selectQuery("SELECT * FROM MonitorList WHERE MonitorServerID = '".$tempMonitorServerData[0]['MonitorServerID']."'");
		$tempHostListings = NULL;
		foreach($tempDeviceList as $device) {
			$tempHostData = $this->dbInstance->selectQuery("SELECT * FROM Host WHERE DeviceID = '".$device['DeviceID']."'");
			$tempHostListings[] = $tempHostData[0];
		}
		return $tempHostListings;
	}
	function getHostGroupsForMonitorServer($MonitorServer) {
		// This is not implemented until clarification
		return NULL;
	}
}
?>
