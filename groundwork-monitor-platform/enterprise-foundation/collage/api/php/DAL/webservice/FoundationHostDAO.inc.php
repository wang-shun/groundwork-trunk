<?php
require_once('DAL/webservice/WSProperties.inc.php');
require_once('DAL/webservice/WSFilter.inc.php');

class FoundationHostDAO {
	
	private $client;
	private $hostWebServiceURL;
	private $maxRetries = 5;
	
	public function __construct($foundationURL) {
		global $foundationModule;

		ini_set("soap.wsdl_cache_enabled", "1"); // enable WSDL cache		
		// a sample url: "http://172.28.113.131:8080/foundation-webapp/services/wshostgroup?wsdl",
		$this->hostWebServiceURL = $foundationURL."/wshost?wsdl";
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
	
	public function getHosts() {
		try {
	  		if (!isset($this->client))
		      $this->getSoapConnection();
		      
		    // to get ALL hosts, pass in a null filter criteria	
            $returnedHosts = $this->client->getHostsByCriteria(null, null, -1, -1);
            if (isset($returnedHosts)) {
                return $this->getHostDetails($returnedHosts);
            }
            // should never get here - no hosts - return an empty array
            else {
               return array();
            }
		}
		catch(SoapFault $soapFault)
		{
	  	    // try one more time for a connection in case the connection has gone stale.
	  	    try {
	  	        $this->getSoapConnection();
                $returnedHosts = $this->client->getHostsByCriteria(null, null, -1, -1);
                if (isset($returnedHosts)) {
                    return $this->getHostDetails($returnedHosts);
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
	
	public function getHostsByFilter($filter=null, $sort=null, $firstResult=-1, $maxResults=-1) {    
		try {
	  		if (!isset($this->client))
		      $this->getSoapConnection();
		      
            // Necessary for complex / nested filters
            $soapVar = new SoapVar($filter, SOAP_ENC_OBJECT, "Filter");
            $soapParam = new SoapParam($soapVar, "Filter");
            
            // to get ALL hosts, pass in a null filter criteria		 		
            $returnedHosts = $this->client->getHostsByCriteria($soapParam, $sort, $firstResult, $maxResults);
            if (isset($returnedHosts)) {
                return $this->getHostDetails($returnedHosts);
            }
            // should never get here - no hosts - return an empty array
            else {
                return array();
            }
		}
		catch(SoapFault $soapFault)
		{
	  	    // try one more time for a connection in case the connection has gone stale.
	  	    try {
	  	        $this->getSoapConnection();
                $returnedHosts = $this->client->getHostsByCriteria($soapParam, $sort, $firstResult, $maxResults);
                if (isset($returnedHosts)) {
                    return $this->getHostDetails($returnedHosts);
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
	
	public function getHostsByHostGroupID($hostgroupID, $sort=null, $firstResult=-1, $maxResults=-1) {
		try {
	  		if (!isset($this->client))
		      $this->getSoapConnection();
		      
    	    $intProp = new Property('hostGroups.hostGroupID', $hostgroupID);
    	    $filter = new IntegerFilter('EQ');
    	    $filter->setIntegerProperty($intProp);
    	    $returnedHosts = $this->client->getHostsByCriteria($filter, $sort, $firstResult, $maxResults);
            if (isset($returnedHosts)) {
                return $this->getHostDetails($returnedHosts);
    	    }
    	    // should never get here - no hosts - return an empty array
    	    else {
    	    	return array();
    	    }
		}
		catch(SoapFault $soapFault)
		{
	  	    // try one more time for a connection in case the connection has gone stale.
	  	    try {
	  	        $this->getSoapConnection();
                $returnedHosts = $this->client->getHostsByCriteria($filter, $sort, $firstResult, $maxResults);
                if (isset($returnedHosts)) {
                    return $this->getHostDetails($returnedHosts);
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
	
	public function getHostsByHostGroupName($hostgroupName, $sort=null, $firstResult=-1, $maxResults=-1) {
		try {
		    if (!isset($this->client))
		      $this->getSoapConnection();
		      
    	    $stringProp = new Property('hostGroups.name', $hostgroupName);
    	    $filter = new StringFilter('EQ');
    	    $filter->setStringProperty($stringProp);
    	    	    
    	    $returnedHosts = $this->client->getHostsByCriteria($filter, $sort, $firstResult, $maxResults);
            if (isset($returnedHosts)) {
                return $this->getHostDetails($returnedHosts);
    	    }
    	    // should never get here - no hosts - return an empty array
    	    else {
    	    	return array();
    	    }
		}
		catch(SoapFault $soapFault)
		{
	  	    // try one more time for a connection in case the connection has gone stale.
	  	    try {
	  	        $this->getSoapConnection();
                $returnedHosts = $this->client->getHostsByCriteria(filter, $sort, $firstResult, $maxResults);
                if (isset($returnedHosts)) {
                    return $this->getHostDetails($returnedHosts);
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
	
	public function getHostsByService($serviceDescription) {
		try {
	  		if (!isset($this->client))
		      $this->getSoapConnection();

		    $stringProp = new Property('serviceStatuses.serviceDescription', $serviceDescription);
    	    $filter = new StringFilter('EQ');
    	    $filter->setStringProperty($stringProp);
    	    $returnedHosts = $this->client->getHostsByCriteria($filter, null, -1, -1);
            if (isset($returnedHosts)) {
                return $this->getHostDetails($returnedHosts);
    	    }
    	    // should never get here - no hosts - return an empty array
    	    else {
    	    	return array();
    	    }
		}
		catch(SoapFault $soapFault)
		{
	  	    // try one more time for a connection in case the connection has gone stale.
	  	    try {
	  	        $this->getSoapConnection();
                $returnedHosts = $this->client->getHostsByCriteria($filter, null, -1, -1);
                if (isset($returnedHosts)) {
                    return $this->getHostDetails($returnedHosts);
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
	
	public function getHostsByMonitorServerName($monitorServerName)  {
		try {
	  		if (!isset($this->client))
		      $this->getSoapConnection();
		      
    	    $stringProp = new Property('device.monitorServers.monitorServerName', $monitorServerName);
    	    $filter = new StringFilter('EQ');
    	    $filter->setStringProperty($stringProp);
    	    $returnedHosts = $this->client->getHostsByCriteria($filter, null, -1, -1);
            if (isset($returnedHosts)) {
                return $this->getHostDetails($returnedHosts);
    	    }
    	    // should never get here - no hosts - return an empty array
    	    else {
    	    	return array();
    	    }
		}
		catch(SoapFault $soapFault)
		{
	  	    // try one more time for a connection in case the connection has gone stale.
	  	    try {
	  	        $this->getSoapConnection();
                $returnedHosts = $this->client->getHostsByCriteria($filter, null, -1, -1);
                if (isset($returnedHosts)) {
                    return $this->getHostDetails($returnedHosts);
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
	
	public function getHostByHostName($hostName)  {
		try {
	  		if (!isset($this->client))
		      $this->getSoapConnection();
		      
    	    $stringProp = new Property('hostName', $hostName);
    	    $filter = new StringFilter('EQ');
    	    $filter->setStringProperty($stringProp);
    	    $returnedHosts = $this->client->getHostsByCriteria($filter, null, -1, -1);
            if (isset($returnedHosts)) {
            	return $this->getSingleHostDetail($returnedHosts);
    	    }
    	    // should never get here - no hosts - return an empty array
    	    else {
    	    	return array();
    	    }
		}
		catch(SoapFault $soapFault)
		{
	  	    // try one more time for a connection in case the connection has gone stale.
	  	    try {
	  	        $this->getSoapConnection();
                $returnedHosts = $this->client->getHostsByCriteria($filter, null, -1, -1);
                if (isset($returnedHosts)) {
                    return $this->getHostDetails($returnedHosts);
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
	
	public function getHostByHostId($hostId)  {
		try {
	  		if (!isset($this->client))
		      $this->getSoapConnection();
		      
    	    $intProp = new Property('hostId', $hostId);
    	    $filter = new IntegerFilter('EQ');
    	    $filter->setIntegerProperty($intProp);
    	    $returnedHosts = $this->client->getHostsByCriteria($filter, null, -1, -1);
            if (isset($returnedHosts)) {
            	return $this->getSingleHostDetail($returnedHosts);
    	    }
    	    // should never get here - no hosts - return an empty array
    	    else {
    	    	return array();
    	    }
		}
		catch(SoapFault $soapFault)
		{
	  	    // try one more time for a connection in case the connection has gone stale.
	  	    try {
	  	        $this->getSoapConnection();
                $returnedHosts = $this->client->getHostsByCriteria($filter, null, -1, -1);
                if (isset($returnedHosts)) {
                    return $this->getHostDetails($returnedHosts);
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
	
    // TODO: this method belongs on a deviceDAO, not here
	public function getHostsForDeviceIdentification($deviceIdentification)  {
		try {
	  		if (!isset($this->client))
		      $this->getSoapConnection();
		      
    	    $stringProp = new Property('device.identification', $deviceIdentification);
    	    $filter = new StringFilter('EQ');
    	    $filter->setStringProperty($stringProp);
    	    $returnedHosts = $this->client->getHostsByCriteria($filter, null, -1, -1);
            if (isset($returnedHosts)) {
                return $this->getHostDetails($returnedHosts);
    	    }
    	    // should never get here - no hosts - return an empty array
    	    else {
    	    	return array();
    	    }
		}
		catch(SoapFault $soapFault)
		{
	  	    // try one more time for a connection in case the connection has gone stale.
	  	    try {
	  	        $this->getSoapConnection();
                $returnedHosts = $this->client->getHostsByCriteria($filter, null, -1, -1);
                if (isset($returnedHosts)) {
                    return $this->getHostDetails($returnedHosts);
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
	
    // TODO: this method belongs on a deviceDAO, not here
	public function getHostsForDeviceId($deviceId)  {
		try {
	  		if (!isset($this->client))
		      $this->getSoapConnection();
		      
    	    $intProp = new Property('device.deviceId', $deviceId);
    	    $filter = new IntegerFilter('EQ');
    	    $filter->setIntegerProperty($intProp);
    	    $returnedHosts = $this->client->getHostsByCriteria($filter, null, -1, -1);
            if (isset($returnedHosts)) {
                return $this->getHostDetails($returnedHosts);
    	    }
    	    // should never get here - no hosts - return an empty array
    	    else {
    	    	return array();
    	    }
		}
		catch(SoapFault $soapFault)
		{
	  	    // try one more time for a connection in case the connection has gone stale.
	  	    try {
	  	        $this->getSoapConnection();
                $returnedHosts = $this->client->getHostsByCriteria($filter, null, -1, -1);
                if (isset($returnedHosts)) {
                    return $this->getHostDetails($returnedHosts);
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
	
	// Returns hosts which do not have a monitor status of UP (ID 7)
	// and do not have a monitor status of PENDING (ID 8)
    // This logic should be moved to a WS call of getTroubledHosts()
    public function getTroubledHosts($firstResult=-1, $maxResults=-1)  {
	  	if (!isset($this->client))
	  	{
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
	  	$intProp = new Property('hostStatus.hostMonitorStatus.monitorStatusId', 7); // Hard-wired ID
	    $filter1 = new IntegerFilter('NE');
	    $filter1->setIntegerProperty($intProp);
	    
	    $intProp = new Property('hostStatus.hostMonitorStatus.monitorStatusId', 8);// Hard-wired ID
	    $filter2 = new IntegerFilter('NE');
	    $filter2->setIntegerProperty($intProp);
	    
		$filter = new Filter('AND');
		$filter->setLeftFilter($filter1);
		$filter->setRightFilter($filter2);
		
        try {
            $soapVar = new SoapVar($filter, SOAP_ENC_OBJECT, "Filter");
            $soapParam = new SoapParam($soapVar, "Filter");
            
            // TODO:  Issue unable to sort by a specific dynamic property value, so we are
            //  currently sorting by name
            $sort = new Sort(new SortItem(true, 'hostName'));
            $returnedHosts = $this->client->getHostsByCriteria($soapParam, $sort,  $firstResult, $maxResults);
            if (isset($returnedHosts)) {
                return $this->getHostDetails($returnedHosts);
            }
            // should never get here - no hosts - return an empty array
            else {
                return array();
            }
        }
		catch(SoapFault $soapFault)
		{
	  	    // try one more time for a connection in case the connection has gone stale.
	  	    try {
	  	        $this->getSoapConnection();
                $returnedHosts = $this->client->getHostsByCriteria($soapParam, $sort, $firstResult, $maxResults);
                if (isset($returnedHosts)) {
                    return $this->getHostDetails($returnedHosts);
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
	
	/**
	 * Returns an array which contains a total count and an array of Hosts specified by host name parameter.  If the a host or hosts do not
	 * exist then it is just not returned, but no exception is thrown.
	 */
	public function getHostsByHostNames($hostNames, $sort=null, $firstResult=-1, $maxResults=-1) 
	{
		if (!isset($hostNames) || (count($hostNames) == 0))
			return null;
			
	  	if (!isset($this->client))
	  	{
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
	    		
		// Build filter and sort by host name
		$filter = null;
		foreach ($hostNames as $hostName)
		{
			$stringProp = new Property('hostName', $hostName);
			$filterHostName = new StringFilter('EQ');
			$filterHostName->setStringProperty($stringProp);
			
			if ($filter != null)
			{
				$tempFilter = new Filter('OR');
				$tempFilter->setLeftFilter($filter);
				$tempFilter->setRightFilter($filterHostName);
				
				$filter = $tempFilter;
			}
			else {
				$filter = $filterHostName;
			}
		}

		if ($sort == null)
		    $sort = new Sort(new SortItem(true, 'hostName'));
	    
        try {
		    $soapVar = new SoapVar($filter, SOAP_ENC_OBJECT, "Filter");
	        $soapParam = new SoapParam($soapVar, "Filter");
	    
	        $returnedHosts = $this->client->getHostsByCriteria($soapParam, $sort, $firstResult, $maxResults);
            if (isset($returnedHosts)) {
        	   return $this->getHostDetails($returnedHosts);
	        }
	        // should never get here - no hosts - return an empty array
	        else {
	    	  return array();
	        }
        }
		catch(SoapFault $soapFault)
		{
	  	    // try one more time for a connection in case the connection has gone stale.
	  	    try {
	  	        $this->getSoapConnection();
                $returnedHosts = $this->client->getHostsByCriteria($soapParam, $sort, $firstResult, $maxResults);
                if (isset($returnedHosts)) {
                    return $this->getHostDetails($returnedHosts);
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
        catch (Exception $ex) {
            throw $ex;
        }
	}
	
	/**
	 * Returns an array which contains a total count and an array of Hosts specified by host ids parameter.  If the a host or hosts do not
	 * exist then it is just not returned, but no exception is thrown.
	 */
	public function getHostsByIds($hostIds, $firstResult, $maxResults) 
	{
	    if (!isset($hostIds) || (count($hostIds) == 0))
			return null;
			
	  	if (!isset($this->client))
	  	{
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
	    		
		// Build filter and sort by host name
		$filter = null;
		foreach ($hostIds as $hostId)
		{
		    $intProp = new Property('hostId', $hostId);
			$filterHostId = new IntegerFilter('EQ');
			$filterHostId->setStringProperty($intProp);
			
			if ($filter != null)
			{
				$tempFilter = new Filter('OR');
				$tempFilter->setLeftFilter($filter);
				$tempFilter->setRightFilter($filterHostId);
				
				$filter = $tempFilter;
			}
			else {
			    $filter = $filterHostId;
			}
		}

		$sort = new Sort(new SortItem(true, 'hostName'));
	    try {
            $soapVar = new SoapVar($filter, SOAP_ENC_OBJECT, "Filter");
            $soapParam = new SoapParam($soapVar, "Filter");
            
            $returnedHosts = $this->client->getHostsByCriteria($soapParam, $sort, $firstResult, $maxResults);
            if (isset($returnedHosts)) {
            	return $this->getHostDetails($returnedHosts);
            }
            // should never get here - no hosts - return an empty array
            else {
            	return array();
            }
	    }
		catch(SoapFault $soapFault)
		{
	  	    // try one more time for a connection in case the connection has gone stale.
	  	    try {
	  	        $this->getSoapConnection();
                $returnedHosts = $this->client->getHostsByCriteria($soapParam, $sort, $firstResult, $maxResults);
                if (isset($returnedHosts)) {
                    return $this->getHostDetails($returnedHosts);
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
	    catch (Exception $ex) {
	        throw $ex;
	    }
	}
	
	private function getHostDetails($returnedHosts) {
       $hostArray = $returnedHosts->Host;
       $results = array();
	   $hosts = array();
	   $results['Count'] = $returnedHosts->TotalCount;	   
	   // $hostArray should have some value, but check anyway
	   if (isset($hostArray)) {
	       // if there is more than one host, process the hosts
	       if (count($hostArray) > 1) {
		      foreach($hostArray as $host) {
		          // TODO: The complete Device and MonitorStatus objects are returned with the host,
		          // should it be included here?  If so, HOW?  current representation is FLAT.
		          $hosts[$host->Name]['HostID'] = $host->HostID;
		          $hosts[$host->Name]['Name'] = $host->Name;
		          $hosts[$host->Name]['ApplicationTypeID'] = $host->ApplicationTypeID;
                  $timestamp = strtotime($host->LastCheckTime);
                  $hosts[$host->Name]['LastCheckTime'] = $timestamp;
		          $hosts[$host->Name]['Device'] = $host->Device;
		          $hosts[$host->Name]['MonitorStatus'] = $host->MonitorStatus;

		          if (isset($host->PropertyTypeBinding))
                  {
                      foreach($host->PropertyTypeBinding as $propertyType => $properties)
                      {
                          if (sizeof($properties)>1) 
                          {
                              if (strcmp($propertyType, 'DateProperty')==0)
                              {
                  	             foreach ($properties as $propName)
                  	             {
                                    $timestamp = strtotime($propName->value);
                                    $hosts[$host->Name][$propName->name] = $timestamp;
                  	             }
                  	          }
                  	          else 
                  	          {
                                foreach ($properties as $propName)
                      	        {
                   		           $hosts[$host->Name][$propName->name] = $propName->value;
                      	        }
                  	          }
                          }                      
                  	      else 
                  	      {
          	                 if (strcmp($propertyType, 'DateProperty')==0)
          	                 {
          	                     $timestamp = strtotime($properties->value);
          	                     $hosts[$host->Name][$properties->name] = $timestamp;
          	                 }
          	                 else {
                  		        $hosts[$host->Name][$properties->name] = $properties->value;
          	                 }
                  	       }
                        }
                  } 
		      }
			}
			// if there's only one host, it is returned as a single object
			else {
				$hosts[$hostArray->Name]['HostID'] = $hostArray->HostID;
				$hosts[$hostArray->Name]['Name'] = $hostArray->Name;
		        $hosts[$hostArray->Name]['ApplicationTypeID'] = $hostArray->ApplicationTypeID;
                $timestamp = strtotime($hostArray->LastCheckTime);
                $hosts[$hostArray->Name]['LastCheckTime'] = $timestamp;
                $hosts[$hostArray->Name]['Device'] = $hostArray->Device;
                $hosts[$hostArray->Name]['MonitorStatus'] = $hostArray->MonitorStatus;
                
    	  	 	if (isset($hostArray->PropertyTypeBinding))
                {
                    foreach($hostArray->PropertyTypeBinding as $propertyType => $properties)
                    {
    					if (sizeof($properties)>1) 
                      	{
                      	    if (strcmp($propertyType, 'DateProperty')==0)
                      	    {
                      	        foreach ($properties as $propName)
                      	        {
                                   $timestamp = strtotime($propName->value);                                   
                                   $hosts[$hostArray->Name][$propName->name] = $timestamp;
                      	        }
                      	    }
                      	    else 
                      	    {
                             	foreach ($properties as $propName)
                              		$hosts[$hostArray->Name][$propName->name] = $propName->value;
                          	}                      
                      	}
                      	else 
                      	{
              	             if (strcmp($propertyType, 'DateProperty')==0)
              	             {
              	                 $timestamp = strtotime($properties->value);
              	                 $hosts[$hostArray->Name][$properties->name] = $timestamp;
              	             }
              	             else 
              	             {
                      		    $hosts[$hostArray->Name][$properties->name] = $properties->value;
              	             }
                      	}
                    }
                }
			}
		}
		
		$results['Hosts'] = $hosts;
		return $results;
	}
	
	private function getSingleHostDetail($returnedHosts) 
	{	
		$hostArray = $returnedHosts->Host;
	   	$host = array();	   
	  	 // $$hostArray should have some value, but check anyway
	  	 if (isset($hostArray)) {

	  	 	$host['HostID'] = $hostArray->HostID;
	  	 	$host['Name'] = $hostArray->Name;
	  	 	$host['ApplicationTypeID'] = $hostArray->ApplicationTypeID;
            $timestamp = strtotime($hostArray->LastCheckTime);
            $host['LastCheckTime'] = $timestamp;
	  	 	$host['Device'] = $hostArray->Device;
	  	 	$host['MonitorStatus'] = $hostArray->MonitorStatus;
                
	  	 	if (isset($hostArray->PropertyTypeBinding))
            {
               foreach($hostArray->PropertyTypeBinding as $propertyType => $properties)
                {
					if (sizeof($properties)>1) 
                  	{
                  	    if (strcmp($propertyType, 'DateProperty')==0)
                  	    {
                  	        foreach ($properties as $propName)
                  	        {
                               $timestamp = strtotime($propName->value);
                               $host[$propName->name] = $timestamp;
                  	        }
                  	    }
                  	    else 
                  	    {
                         	foreach ($properties as $propName)
                          	{
                          		$host[$propName->name] = $propName->value;
                          	}
                      	}                      
                  	}
                  	else 
                  	{
          	             if (strcmp($propertyType, 'DateProperty')==0)
          	             {
          	                 $timestamp = strtotime($properties->value);
          	                 $host[$properties->name] = $timestamp;
          	             }
          	             else {
                  		    $host[$properties->name] = $properties->value;
          	             }
                  	}
                }
             }
		}
		
		return $host;
	}

	private function getSoapConnection()
	{
        // loop until a connection is made or max retries is reached
		$retries = 0;
		while ($retries < $this->maxRetries)
		{
    	  	try {
    			@($this->client = new SoapClient(
    		    	$this->hostWebServiceURL,
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