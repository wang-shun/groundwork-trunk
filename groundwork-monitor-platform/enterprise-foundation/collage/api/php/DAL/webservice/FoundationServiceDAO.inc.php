<?php
require_once('DAL/webservice/WSProperties.inc.php');
require_once('DAL/webservice/WSFilter.inc.php');
require_once('WSSort.inc.php');

class FoundationServiceDAO {
	
	private $client;
	private $serviceWebServiceURL;
	private $maxRetries = 5;
	
	public function __construct($foundationURL) {
		global $foundationModule;

		ini_set("soap.wsdl_cache_enabled", "1"); // enable WSDL cache		
		// a sample url: "http://172.28.113.131:8080/foundation-webapp/services/wshostgroup?wsdl",
		$this->serviceWebServiceURL = $foundationURL."/wsservice?wsdl";
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
	
	public function getServices() {
        try {
        	if (!isset($this->client))
                $this->getSoapConnection();
	    		
    	    $returnedServices = $this->client->getServicesByCriteria(null, null, -1, -1);
            if (isset($returnedServices)) {
                return $this->getServiceDetails($returnedServices);
    	    }
    	    // should never get here - no hosts - return an empty array
    	    else {
    	    	return array();
    	    }
        }
	  	catch(SoapFault $exception) {
	  	    // try one more time for a connection in case the connection has gone stale.
	  	    try {
	  	        $this->getSoapConnection();
        	    $returnedServices = $this->client->getServicesByCriteria(null, null, -1, -1);
                if (isset($returnedServices)) {
                    return $this->getServiceDetails($returnedServices);
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
		
	public function getServicesByDescription($serviceDescription) {
        try {
        	if (!isset($this->client))
                $this->getSoapConnection();
	    		
    	    $stringProp = new Property('serviceDescription', $serviceDescription);
    	    $filter = new StringFilter('EQ');
    	    $filter->setStringProperty($stringProp);
    	    $returnedServices = $this->client->getServicesByCriteria($filter, null, -1, -1);
            if (isset($returnedServices)) {
                return $this->getServiceDetails($returnedServices);
    	    }
    	    // should never get here - no hosts - return an empty array
    	    else {
    	    	return array();
    	    }
        }
	  	catch(SoapFault $exception) {
	  	    // try one more time for a connection in case the connection has gone stale.
	  	    try {
	  	        $this->getSoapConnection();
        	    $returnedServices = $this->client->getServicesByCriteria($filter, null, -1, -1);
                if (isset($returnedServices)) {
                    return $this->getServiceDetails($returnedServices);
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
	 * Retrieve service status by service description and host name.  This function will return null or 
	 * a single service status which matches.
	 */
	public function getService($serviceDescription, $hostName)
	{
        try {
        	if (!isset($this->client))
                $this->getSoapConnection();
    	    $stringProp = new Property('serviceDescription', $serviceDescription);
    	    $filter1 = new StringFilter('EQ');
    	    $filter1->setStringProperty($stringProp);
    	    
    	    $stringProp = new Property('host.hostName', $hostName);
    	    $filter2 = new StringFilter('EQ');
    	    $filter2->setStringProperty($stringProp);
    	    
    		$filter = new Filter('AND');
    		$filter->setLeftFilter($filter1);
    		$filter->setRightFilter($filter2);
    		
       	    $soapVar = new SoapVar($filter, SOAP_ENC_OBJECT, "Filter");
       	    $soapParam = new SoapParam($soapVar, "Filter");
    	    	    
       	    $returnedServices = $this->client->getServicesByCriteria($soapParam, null, -1, -1);
            if (isset($returnedServices)) {
                return $this->getSingleServiceDetail($returnedServices);
    	    }
    	    // should never get here - no hosts - return an empty array
    	    else {
    	    	return array();
    	    }
        }
	  	catch(SoapFault $exception) {
	  	    // try one more time for a connection in case the connection has gone stale.
	  	    try {
	  	        $this->getSoapConnection();
        	    $returnedServices = $this->client->getServicesByCriteria($soapParam, null, -1, -1);
                if (isset($returnedServices)) {
                    return $this->getServiceDetails($returnedServices);
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
	
	public function getServiceById($servicestatusId) {
        try {
        	if (!isset($this->client))
                $this->getSoapConnection();
    	    $intProp = new Property('serviceStatusId', $servicestatusId);
    	    $filter = new IntegerFilter('EQ');
    	    $filter->setIntegerProperty($intProp);
            $returnedServices = $this->client->getServicesByCriteria($filter, null, -1, -1);
            if (isset($returnedServices)) {
            	return $this->getSingleServiceDetail($returnedServices);
    	    }
    	    // should never get here - no hosts - return an empty array
    	    else {
    	    	return array();
    	    }
        }
	  	catch(SoapFault $exception) {
	  	    // try one more time for a connection in case the connection has gone stale.
	  	    try {
	  	        $this->getSoapConnection();
        	    $returnedServices = $this->client->getServicesByCriteria($filter, null, -1, -1);
                if (isset($returnedServices)) {
                    return $this->getServiceDetails($returnedServices);
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
	
	public function getServicesByHostId($hostId) {
        try {
        	if (!isset($this->client))
                $this->getSoapConnection();
    	    $intProp = new Property('host.hostId', $hostId);
    	    $filter = new IntegerFilter('EQ');
    	    $filter->setIntegerProperty($intProp);
    	    $returnedServices = $this->client->getServicesByCriteria($filter, null, -1, -1);
            if (isset($returnedServices)) {
                return $this->getServiceDetails($returnedServices);
    	    }
    	    // should never get here - no hosts - return an empty array
    	    else {
    	    	return array();
    	    }
        }
	  	catch(SoapFault $exception) {
	  	    // try one more time for a connection in case the connection has gone stale.
	  	    try {
	  	        $this->getSoapConnection();
        	    $returnedServices = $this->client->getServicesByCriteria($filter, null, -1, -1);
                if (isset($returnedServices)) {
                    return $this->getServiceDetails($returnedServices);
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
	
	public function getServicesByHostName($hostName, $sort=null, $firstResult=-1, $maxResults=-1) {
        try {
        	if (!isset($this->client))
        	   $this->getSoapConnection();
    	    $strProp = new Property('host.hostName', $hostName);
    	    $filter = new StringFilter('EQ');
    	    $filter->setStringProperty($strProp);
    	    
    	    $returnedServices = $this->client->getServicesByCriteria($filter, $sort, $firstResult, $maxResults);
            if (isset($returnedServices)) {
                return $this->getServiceDetails($returnedServices);
    	    }
    	    // should never get here - no hosts - return an empty array
    	    else {
    	    	return array();
    	    }
        }
	  	catch(SoapFault $exception) {
	  	    // try one more time for a connection in case the connection has gone stale.
	  	    try {
	  	        $this->getSoapConnection();
        	    $returnedServices = $this->client->getServicesByCriteria($filter, $sort, $firstResult, $maxResults);
                if (isset($returnedServices)) {
                    return $this->getServiceDetails($returnedServices);
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
	
	public function getServicesByHostGroupName($hostGroupName) {
        try {
        	if (!isset($this->client))
                $this->getSoapConnection();
    	    $strProp = new Property('host.hostGroups.name', $hostGroupName);
    	    $filter = new StringFilter('EQ');
    	    $filter->setStringProperty($strProp);
    	    $returnedServices = $this->client->getServicesByCriteria($filter, null, -1, -1);
            if (isset($returnedServices)) {
                return $this->getServiceDetails($returnedServices);
    	    }
    	    // should never get here - no hosts - return an empty array
    	    else {
    	    	return array();
    	    }
        }
	  	catch(SoapFault $exception) {
	  	    // try one more time for a connection in case the connection has gone stale.
	  	    try {
	  	        $this->getSoapConnection();
        	    $returnedServices = $this->client->getServicesByCriteria($filter, null, -1, -1);
                if (isset($returnedServices)) {
                    return $this->getServiceDetails($returnedServices);
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
	
	public function getServicesByHostGroupId($hostGroupId) {
        try {
        	if (!isset($this->client))
                $this->getSoapConnection();
    	    $intProp = new Property('host.hostGroups.hostGroupId', $hostGroupId);
    	    $filter = new IntegerFilter('EQ');
    	    $filter->setIntegerProperty($intProp);
    	    $returnedServices = $this->client->getServicesByCriteria($filter, null, -1, -1);
            if (isset($returnedServices)) {
                return $this->getServiceDetails($returnedServices);
    	    }
    	    // should never get here - no hosts - return an empty array
    	    else {
    	    	return array();
    	    }
        }
	  	catch(SoapFault $exception) {
	  	    // try one more time for a connection in case the connection has gone stale.
	  	    try {
	  	        $this->getSoapConnection();
        	    $returnedServices = $this->client->getServicesByCriteria($filter, null, -1, -1);
                if (isset($returnedServices)) {
                    return $this->getServiceDetails($returnedServices);
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
	
 
	public function getTroubledServices($firstResult=-1, $maxResults=-1)
	{
        try {
        	if (!isset($this->client)){ 
                $this->getSoapConnection();
        	}

       	    // Sort by lastStateChange - TODO:  Probably should make a parameter.
    	    $sort = new Sort(new SortItem(true, 'lastStateChange'));
    	    $returnedServices = $this->client->getTroubledServices($sort, $firstResult, $maxResults);
            if (isset($returnedServices)) {
                return $this->getServiceDetails($returnedServices);
    	    }
    	    // should never get here - no hosts - return an empty array
    	    else {
    	    	return array();
    	    }
        }
	  	catch(SoapFault $exception) {
	  	    // try one more time for a connection in case the connection has gone stale.
	  	    try {
	  	        $this->getSoapConnection();
        	    $returnedServices = $this->client->getServicesByCriteria($soapParam, $sort, $firstResult, $maxResults);
                if (isset($returnedServices)) {
                    return $this->getServiceDetails($returnedServices);
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
	 * Returns an array of Services specified by service id parameter.  If the a service or services do not
	 * exist then it is just not returned, but no exception is thrown.
	 */
	public function getServicesByIds($serviceIds, $sort=null, $firstResult=-1, $maxResults=-1)
	{
	    if (!isset($serviceIds) || (count($serviceIds) == 0))
			return null;
			
        try {
        	if (!isset($this->client))
                $this->getSoapConnection();
	    		
    		// Build filter and sort by service description
    		$filter = null;
    		foreach ($serviceIds as $serviceId)
    		{
    		    // Just ignore an empty service Id
    		    if ($serviceId == null)
    		        continue;
    		        
    		    $intProp = new Property('serviceStatusId', $serviceId);
    			$filterServiceId = new IntegerFilter('EQ');
    			$filterServiceId->setIntegerProperty($intProp);
    			
    			if ($filter != null)
    			{
    				$tempFilter = new Filter('OR');
    				$tempFilter->setLeftFilter($filter);
    				$tempFilter->setRightFilter($filterServiceId);
    				
    				$filter = $tempFilter;
    			}
    			else {
    			    $filter = $filterServiceId;
    			}
    		}
    
    		if ($sort == null)
    		    $sort = new Sort(new SortItem(true, 'serviceDescription'));
    	    
    	    $soapVar = new SoapVar($filter, SOAP_ENC_OBJECT, "Filter");
    	    $soapParam = new SoapParam($soapVar, "Filter");
    	    
    	    $returnedServices = $this->client->getServicesByCriteria($soapParam, $sort, $firstResult, $maxResults);
    	    if (isset($returnedServices)) {
            	return $this->getServiceDetails($returnedServices);
    	    }
    	    // should never get here - no hosts - return an empty array
    	    else {
    	    	return array();
    	    }
        }
	  	catch(SoapFault $exception) {
	  	    // try one more time for a connection in case the connection has gone stale.
	  	    try {
	  	        $this->getSoapConnection();
        	    $returnedServices = $this->client->getServicesByCriteria($soapParam, $sort, $firstResult, $maxResults);
                if (isset($returnedServices)) {
                    return $this->getServiceDetails($returnedServices);
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
	
	public function getServicesByFilter($filter=null, $sort=null, $firstResult=-1, $maxResults=-1) {    
        try {
        	if (!isset($this->client))
                $this->getSoapConnection();
	    		
    	    // Necessary for complex / nested filters
    	    $soapVar = new SoapVar($filter, SOAP_ENC_OBJECT, "Filter");
    	    $soapParam = new SoapParam($soapVar, "Filter");
    	     
    	    // to get All Services, pass in a null filter criteria		 		
    	    $returnedServices = $this->client->getServicesByCriteria($soapParam, $sort, $firstResult, $maxResults);
    	    if (isset($returnedServices)) {
    	        return $this->getServiceDetails($returnedServices);
    	    }
    	    // should never get here - no hosts - return an empty array
    	    else {
    	    	return array();
    	    }
        }
	  	catch(SoapFault $exception) {
	  	    // try one more time for a connection in case the connection has gone stale.
	  	    try {
	  	        $this->getSoapConnection();
        	    $returnedServices = $this->client->getServicesByCriteria($soapParam, $sort, $firstResult, $maxResults);
                if (isset($returnedServices)) {
                    return $this->getServiceDetails($returnedServices);
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
	
	private function getServiceDetails($returnedServices) {
       $serviceArray = $returnedServices->ServiceStatus;
       $results = array();
	   $services = array();	  
	   $results['Count'] = $returnedServices->TotalCount;
	   // $serviceArray should have some value, but check anyway
	   if (isset($serviceArray)) {
	       // if there is more than one service, process them
	       if (count($serviceArray) > 1) {
		      foreach($serviceArray as $service) {
		          $services[$service->ServiceStatusID]['ServiceStatusID'] = $service->ServiceStatusID;
		          $services[$service->ServiceStatusID]['ApplicationTypeID'] = $service->ApplicationTypeID;
		          $services[$service->ServiceStatusID]['Description'] = $service->Description;
				  $services[$service->ServiceStatusID]['Host'] = $service->Host;
		          $services[$service->ServiceStatusID]['MonitorStatus'] = $service->MonitorStatus;
		          $services[$service->ServiceStatusID]['LastCheckTime'] = $service->LastCheckTime;
		          $services[$service->ServiceStatusID]['NextCheckTime'] = $service->NextCheckTime;
		          $services[$service->ServiceStatusID]['MetricType'] = $service->MetricType;
		          $services[$service->ServiceStatusID]['Domain'] = $service->Domain;
		          $services[$service->ServiceStatusID]['StateType'] = $service->StateType;
		          $services[$service->ServiceStatusID]['CheckType'] = $service->CheckType;
		          $services[$service->ServiceStatusID]['LastHardState'] = $service->LastHardState;
		          $services[$service->ServiceStatusID]['LastStateChange'] = $service->LastStateChange;
		          
    	  	 	if (isset($service->PropertyTypeBinding))
                {
                   foreach($service->PropertyTypeBinding as $propertyType => $properties)
                    {
    					if (sizeof($properties)>1) 
                      	{
                      	    if (strcmp($propertyType, 'DateProperty')==0)
                      	    {
                      	        foreach ($properties as $propName)
                      	        {
                                   $timestamp = strtotime($propName->value);
                                   $services[$service->ServiceStatusID][$propName->name] = $timestamp;
                      	        }
                      	    }
                      	    else 
                      	    {
                             	foreach ($properties as $propName)
                              	{
                              		$services[$service->ServiceStatusID][$propName->name] = $propName->value;
                              	}
                          	}                      
                      	}
                      	else 
                      	{
              	             if (strcmp($propertyType, 'DateProperty')==0)
              	             {
              	                 $timestamp = strtotime($properties->value);
              	                 $services[$service->ServiceStatusID][$properties->name] = $timestamp;
              	             }
              	             else {
                      		    $services[$service->ServiceStatusID][$properties->name] = $properties->value;
              	             }
                      	}
                    }
                 }
		      }
			}
			// if there's only one service, it is returned as a single element
			else {
				$services[$serviceArray->ServiceStatusID]['ServiceStatusID'] = $serviceArray->ServiceStatusID;
		        $services[$serviceArray->ServiceStatusID]['ApplicationTypeID'] = $serviceArray->ApplicationTypeID;
				$services[$serviceArray->ServiceStatusID]['Description'] = $serviceArray->Description;
				$services[$serviceArray->ServiceStatusID]['Host'] = $serviceArray->Host;
                $services[$serviceArray->ServiceStatusID]['MonitorStatus'] = $serviceArray->MonitorStatus;
		        $services[$serviceArray->ServiceStatusID]['LastCheckTime'] = $serviceArray->LastCheckTime;
		        $services[$serviceArray->ServiceStatusID]['NextCheckTime'] = $serviceArray->NextCheckTime;
	            $services[$serviceArray->ServiceStatusID]['MetricType'] = $serviceArray->MetricType;
	            $services[$serviceArray->ServiceStatusID]['Domain'] = $serviceArray->Domain;
	            $services[$serviceArray->ServiceStatusID]['StateType'] = $serviceArray->StateType;
	            $services[$serviceArray->ServiceStatusID]['CheckType'] = $serviceArray->CheckType;
	            $services[$serviceArray->ServiceStatusID]['LastHardState'] = $serviceArray->LastHardState;
	            $services[$serviceArray->ServiceStatusID]['LastStateChange'] = $serviceArray->LastStateChange;
	            
    	  	 	if (isset($serviceArray->PropertyTypeBinding))
                {
                    foreach($serviceArray->PropertyTypeBinding as $propertyType => $properties)
                    {
    					if (sizeof($properties)>1) 
                      	{
                      	    if (strcmp($propertyType, 'DateProperty')==0)
                      	    {
                      	        foreach ($properties as $propName)
                      	        {
                                   $timestamp = strtotime($propName->value);
                                   $services[$serviceArray->ServiceStatusID][$propName->name] = $timestamp;
                      	        }
                      	    }
                      	    else 
                      	    {
                             	foreach ($properties as $propName)
                              		$services[$serviceArray->ServiceStatusID][$propName->name] = $propName->value;
                          	}                      
                      	}
                      	else 
                      	{
              	             if (strcmp($propertyType, 'DateProperty')==0)
              	             {
              	                 $timestamp = strtotime($properties->value);
              	                 $services[$serviceArray->ServiceStatusID][$properties->name] = $timestamp;
              	             }
              	             else 
              	             {
                      		    $services[$serviceArray->ServiceStatusID][$properties->name] = $properties->value;
              	             }
                      	}
                    }
                }
 			}
		}
		
		$results['Services'] = $services;
		return  $results;
	}
	
	private function getSingleServiceDetail($returnedServices) 
	{	
       $serviceArray = $returnedServices->ServiceStatus;
	   $service = array();	   
	   // $serviceArray should have some value, but check anyway
	   if (isset($serviceArray)) {

			$service['ServiceStatusID'] = $serviceArray->ServiceStatusID;
	        $service['ApplicationTypeID'] = $serviceArray->ApplicationTypeID;
			$service['Description'] = $serviceArray->Description;
			$service['Host'] = $serviceArray->Host;
            $service['MonitorStatus'] = $serviceArray->MonitorStatus;
            $timestamp = strtotime($serviceArray->LastCheckTime);
            $service['LastCheckTime'] = $timestamp;
            $timestamp = strtotime($serviceArray->NextCheckTime);
            $service['NextCheckTime'] = $timestamp;
            $service['MetricType'] = $serviceArray->MetricType;
            $service['Domain'] = $serviceArray->Domain;
            $service['StateType'] = $serviceArray->StateType;
            $service['CheckType'] = $serviceArray->CheckType;
            $timestamp = strtotime($serviceArray->LastHardState);
            $service['LastHardState'] = $timestamp;
            $timestamp = strtotime($serviceArray->LastStateChange);
            $service['LastStateChange'] = $timestamp;
                        
	  	 	if (isset($serviceArray->PropertyTypeBinding))
            {
               foreach($serviceArray->PropertyTypeBinding as $propertyType => $properties)
                {
					if (sizeof($properties)>1) 
                  	{
                  	    if (strcmp($propertyType, 'DateProperty')==0)
                  	    {
                  	        foreach ($properties as $propName)
                  	        {
                               $timestamp = strtotime($propName->value);
                               $service[$propName->name] = $timestamp;
                  	        }
                  	    }
                  	    else 
                  	    {
                         	foreach ($properties as $propName)
                          	{
                          		$service[$propName->name] = $propName->value;
                          	}
                      	}                      
                  	}
                  	else 
                  	{
          	             if (strcmp($propertyType, 'DateProperty')==0)
          	             {
          	                 $timestamp = strtotime($properties->value);
          	                 $service[$properties->name] = $timestamp;
          	             }
          	             else {
                  		    $service[$properties->name] = $properties->value;
          	             }
                  	}
                }
             }
		}
		return $service;
	}

	private function getSoapConnection()
	{
        // loop until a connection is made or max retries is reached
		$retries = 0;
		while ($retries < $this->maxRetries)
		{
    	  	try {
    			@($this->client = new SoapClient(
    			    $this->serviceWebServiceURL,
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