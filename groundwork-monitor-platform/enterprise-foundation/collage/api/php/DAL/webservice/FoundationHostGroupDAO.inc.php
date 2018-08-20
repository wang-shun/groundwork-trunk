<?php
require_once('DAL/webservice/WSProperties.inc.php');
require_once('DAL/webservice/WSFilter.inc.php');

class FoundationHostGroupDAO {
	
	private $client;
	private $hostgroupWebServiceURL;
	private $maxRetries = 5;
	
	public function __construct($foundationURL) {
		global $foundationModule;

		ini_set("soap.wsdl_cache_enabled", "1"); // enable WSDL cache		
		// a sample url: "http://172.28.113.131:8080/foundation-webapp/services/wshostgroup?wsdl",
		$this->hostgroupWebServiceURL = $foundationURL."/wshostgroup?wsdl";
		try {
		    $this->getSoapConnection();
		}
		catch(SoapFault $soapEx)
		{
		    throw $soapEx;
		}
		catch (Exception $ex)
		{
		    throw $ex;
		}
	}
	
	/**
	 * Gets all the hostgroups currently defined in the foundation database.
	 *
	 * @return unknown
	 */
	public function getHostGroups($deep=false) {
		try {
	  		if (!isset($this->client))
		      $this->getSoapConnection();
		    
    	    //TODO: pass in a SortCriteria object instead of null as the last object 	
    	    // the default returned is order by name ASCENDING
    		$returnedHostGroups = @$this->client->getHostGroupsByCriteria(null, null, -1, -1, $deep);
    	    if (isset($returnedHostGroups)) {
    	        return $this->getHostGroupDetails($returnedHostGroups, $deep);
    	    }
	    	// no hostgroups - return empty array
	    	else {
	    		return array();
	    	}
		}
		catch(SoapFault $soapFault)
		{
	  	    // try one more time for a connection in case the connection has gone stale.
	  	    try {
	  	        $this->getSoapConnection();
        		$returnedHostGroups = @$this->client->getHostGroupsByCriteria(null, null, -1, -1, $deep);
        	    if (isset($returnedHostGroups)) {
        	        return $this->getHostGroupDetails($returnedHostGroups, $deep);
                }
                // should never get here - no hosts - return an empty array
                else {
                   return array();
                }
	  	    }
	  	    catch(SoapFault $soapEx) {
	  	        // if we still don't succeed, release the SoapClient and bail out
	  	        $this->client = null;
			    throw $soapEx;
	  	    }
		}
		catch (Exception $ex)
		{
		    throw $ex;
		}
	}

	public function getHostGroupByID($hostGroupID, $deep=false) {
		try {
	  		if (!isset($this->client))
		      $this->getSoapConnection();
		    
		    //TODO: pass in a SortCriteria object instead of null as the last object - need to figure out HOW.	
		    // the default returned is order by name ASCENDING
    	    $intProp = new Property('hostGroupId', $hostGroupID);
    	    $idFilter = new IntegerFilter('EQ');
    	    $idFilter->setIntegerProperty($intProp);
    	    $returnedHostGroups = $this->client->getHostGroupsByCriteria($idFilter, null, -1, -1, $deep);
			// make sure hostgroups were returned
			$hostgroup = array();
			if (isset($returnedHostGroups) && ($returnedHostGroups->TotalCount > 0)) {
	    		$hostgroup['HostGroupID'] = $returnedHostGroups->HostGroup->HostGroupID;
	    		$hostgroup['Name'] = $returnedHostGroups->HostGroup->Name;
	    		$hostgroup['Description'] = $returnedHostGroups->HostGroup->Description;
	    		$hostgroup['ApplicationTypeID'] = $returnedHostGroups->HostGroup->ApplicationTypeID;
	    		$hostgroup['ApplicationName'] = $returnedHostGroups->HostGroup->ApplicationName;
	    		if ($deep == true) {
	    		    $hostgroup['Hosts'] = $this->processHostsForHostGroup($returnedHostGroups->HostGroup->Hosts);
	    		}
			}
			return $hostgroup;
		}
		catch(SoapFault $soapFault)
		{
	  	    // try one more time for a connection in case the connection has gone stale.
	  	    try {
	  	        $this->getSoapConnection();
        		$returnedHostGroups = @$this->client->getHostGroupsByCriteria(null, null, -1, -1, $deep);
    			// make sure hostgroups were returned
    			$hostgroup = array();
    			if (isset($returnedHostGroups) && ($returnedHostGroups->TotalCount > 0)) {
    	    		$hostgroup['HostGroupID'] = $returnedHostGroups->HostGroup->HostGroupID;
    	    		$hostgroup['Name'] = $returnedHostGroups->HostGroup->Name;
    	    		$hostgroup['Description'] = $returnedHostGroups->HostGroup->Description;
    	    		$hostgroup['ApplicationTypeID'] = $returnedHostGroups->HostGroup->ApplicationTypeID;
    	    		$hostgroup['ApplicationName'] = $returnedHostGroups->HostGroup->ApplicationName;
    	    		if ($deep == true) {
    	    		    $hostgroup['Hosts'] = $this->processHostsForHostGroup($returnedHostGroups->HostGroup->Hosts);
    	    		}
    			}
    			return $hostgroup;
	  	    }
	  	    catch(SoapFault $soapEx) {
	  	        // if we still don't succeed, release the SoapClient and bail out
	  	        $this->client = null;
			    throw $soapEx;
	  	    }
		}
		catch (Exception $ex)
		{
		    throw $ex;
		}
	    
	}
	
	public function getHostGroupByName($hostGroupName, $deep=false) {
		try {
	  		if (!isset($this->client))
		      $this->getSoapConnection();
		    
    	    $stringProp = new Property('name', $hostGroupName);
    	    $filter = new StringFilter('EQ');
    	    $filter->setStringProperty($stringProp);
		    //TODO: pass in a SortCriteria object instead of null as the last object - need to figure out HOW.	
		    // the default returned is order by name ASCENDING
		    $result = @$this->client->getHostGroupsByCriteria($filter, null, -1, -1, $deep);
			// make sure hostgroups were returned
			$hostgroup = array();
			if (isset($result) && ($result->TotalCount > 0)) {
	    		$hostgroup['HostGroupID'] = $result->HostGroup->HostGroupID;
	    		$hostgroup['Name'] = $result->HostGroup->Name;
	    		$hostgroup['Description'] = $result->HostGroup->Description;
	    		$hostgroup['ApplicationTypeID'] = $result->HostGroup->ApplicationTypeID;
	    		$hostgroup['ApplicationName'] = $result->HostGroup->ApplicationName;
	    		if ($deep == true) {
	    		    $hostgroup['Hosts'] = $this->processHostsForHostGroup($result->HostGroup->Hosts);
	    		}
			}
			return $hostgroup;
		}
		catch(SoapFault $soapFault)
		{
	  	    // try one more time for a connection in case the connection has gone stale.
	  	    try {
	  	        $this->getSoapConnection();
    		    $result = @$this->client->getHostGroupsByCriteria($filter, null, -1, -1, $deep);
    			// make sure hostgroups were returned
    			$hostgroup = array();
    			if (isset($result) && ($result->TotalCount > 0)) {
    	    		$hostgroup['HostGroupID'] = $result->HostGroup->HostGroupID;
    	    		$hostgroup['Name'] = $result->HostGroup->Name;
    	    		$hostgroup['Description'] = $result->HostGroup->Description;
    	    		$hostgroup['ApplicationTypeID'] = $result->HostGroup->ApplicationTypeID;
    	    		$hostgroup['ApplicationName'] = $result->HostGroup->ApplicationName;
    	    		if ($deep == true) {
    	    		    $hostgroup['Hosts'] = $this->processHostsForHostGroup($result->HostGroup->Hosts);
    	    		}
    			}
    			return $hostgroup;
	  	    }
	  	    catch(SoapFault $soapEx) {
	  	        // if we still don't succeed, release the SoapClient and bail out
	  	        $this->client = null;
			    throw $soapEx;
	  	    }
		}
		catch (Exception $ex)
		{
		    throw $ex;
		}
	}
	
	public function getHostGroupsByFilter($filter=null, $sort=null, $firstResult=-1, $maxResults=-1, $deep=false) {    
		try {
	  		if (!isset($this->client))
		      $this->getSoapConnection();

		    // Necessary for complex / nested filters
    	    $soapVar = new SoapVar($filter, SOAP_ENC_OBJECT, "Filter");
    	    $soapParam = new SoapParam($soapVar, "Filter");
    	    
    	    // to get ALL hosts, pass in a null filter criteria		 		
    	    $returnedHostGroups = $this->client->getHostGroupsByCriteria($soapParam, $sort, $firstResult, $maxResults, $deep);
    	    if (isset($returnedHostGroups)) {
    	        return $this->getHostGroupDetails($returnedHostGroups, $deep);
    	    }
    	    // no hostgroups - return empty array 
    	    else {
    	    	return array();
    	    }
		}
		catch(SoapFault $soapFault)
		{
	  	    // try one more time for a connection in case the connection has gone stale.
	  	    try {
	  	        $this->getSoapConnection();
        	    $returnedHostGroups = $this->client->getHostGroupsByCriteria($soapParam, $sort, $firstResult, $maxResults, $deep);
        	    if (isset($returnedHostGroups)) {
        	        return $this->getHostGroupDetails($returnedHostGroups, $deep);
        	    }
        	    // no hostgroups - return empty array 
        	    else {
        	    	return array();
        	    }
	  	    }
	  	    catch(SoapFault $soapEx) {
	  	        // if we still don't succeed, release the SoapClient and bail out
	  	        $this->client = null;
			    throw $soapEx;
	  	    }
		}
		catch (Exception $ex)
		{
		    throw $ex;
		}
	}
	
	private function getHostGroupDetails ($returnedHostGroups, $deep=false)
	{
	    $hgs = $returnedHostGroups->HostGroup;
	    $results = array();
   		$hostgroups = array();
   		$results['Count'] = $returnedHostGroups->TotalCount;
   		// $hgs should have some value, but check anyway
   		if (isset($hgs)) {
   			// if there is more than one hostgroup, process the hostgroups
   			if (count($hgs) > 1) {
   				$counter = 0;
   				foreach($hgs as $hg) {
					$hostgroups[$counter]['HostGroupID'] = $hg->HostGroupID;
					$hostgroups[$counter]['Name'] = $hg->Name;
					$hostgroups[$counter]['Description'] = $hg->Description;
          	    	$hostgroups[$counter]['ApplicationTypeID'] = $hg->ApplicationTypeID;
          	    	$hostgroups[$counter]['ApplicationName'] = $hg->ApplicationName;
          	    	if ($deep == true) {
          	    	    $hostgroups[$counter]['Hosts'] = $this->processHostsForHostGroup($hg->Hosts);
          	    	}
    				$counter++;
   				}
   			}
   			// if there's only one hostgroup, it is returned as a single object
   			else {
   				$hostgroups[0]['HostGroupID'] = $hgs->HostGroupID;
   				$hostgroups[0]['Name'] = $hgs->Name;
   				$hostgroups[0]['Description'] = $hgs->Description;
   		        $hostgroups[0]['ApplicationTypeID'] = $hgs->ApplicationTypeID;
   		        $hostgroups[0]['ApplicationName'] = $hgs->ApplicationName;
    	    	if ($deep == true) {
    	    	    $hostgroups[0]['Hosts'] = $this->processHostsForHostGroup($hgs->Hosts);
    	    	}
   			}
   		}
   		
   		$results['HostGroups'] = $hostgroups;
   		return $results;
	}
	
	private function processHostsForHostGroup($hosts) 
	{
        $hostgroup_hosts = array();
        if (isset($hosts)) {
             $count = 0;
             if (count($hosts) > 1)
             {
                 foreach($hosts as $host) {
                     $hostgroup_hosts[$count]['HostID'] = $host->HostID;
                     $hostgroup_hosts[$count]['ApplicationTypeID'] = $host->ApplicationTypeID;
                     $host_device = $host->Device;
                     $device['DeviceID'] = $host_device->DeviceID;
                     $device['Name'] = $host_device->Name;
                     $device['Identification'] = $host_device->Identification;
                     $hostgroup_hosts[$count]['Device'] = $device;
                     $hostgroup_hosts[$count]['Name'] = $host->Name;
                     $hostgroup_hosts[$count]['MonitorStatus'] = $host->MonitorStatus;
                     $count++;
                 }
             }
             else {
                 $hostgroup_hosts[0]['HostID'] = $hosts->HostID;
                 $hostgroup_hosts[0]['Name'] = $hosts->Name;
                 $hostgroup_hosts[0]['ApplicationTypeID'] = $hosts->ApplicationTypeID;
                 $host_device = $hosts->Device;
                 $device['DeviceID'] = $host_device->DeviceID;
                 $device['Name'] = $host_device->Name;
                 $device['Identification'] = $host_device->Identification;
                 $hostgroup_hosts[0]['Device'] = $device;
                 $hostgroup_hosts[0]['MonitorStatus'] = $hosts->MonitorStatus;
             }
        }
        return $hostgroup_hosts;
	}

	private function getSoapConnection()
	{
        // loop until a connection is made or max retries is reached
		$retries = 0;
		while ($retries < $this->maxRetries)
		{
    	  	try {
    			@($this->client = new SoapClient(
    		    	$this->hostgroupWebServiceURL,
    		    	array('trace'=>1, 'connection_timeout'=>300)
    			));		
    			$retries = $this->maxRetries;
    	  	}
    	  	catch(SoapFault $exception) {
    	  	    $retries++;
    	  	    if ($retries >= $this->maxRetries)
   	  	            throw $exception;
    	  	}
    	  	catch (Exception $e) {
    	  	    $retries++;
    	  	    if ($retries >= $this->maxRetries)
    	  		   throw $e;
    	  	}
		}
	}
}
	
?>