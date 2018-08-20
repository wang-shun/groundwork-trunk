<?php
require_once('DAL/webservice/WSProperties.inc.php');
require_once('DAL/webservice/WSFilter.inc.php');
require_once('DAL/webservice/WSSort.inc.php');
class FoundationEventDAO {
	
	private $client;
	private $eventWebServiceURL;
	private $maxRetries = 5;
	
	public function __construct($foundationURL) {
		global $foundationModule;

		ini_set("soap.wsdl_cache_enabled", "1"); // enable WSDL cache		
		// a sample url: "http://172.28.113.131:8080/foundation-webapp/services/wshostgroup?wsdl",
		$this->eventWebServiceURL = $foundationURL."/wsevent?wsdl";
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
     * Get all events with an OperationStatus of "OPEN".
     *
     * @param unknown_type $appTypeId - only get events for this application type.
     * @param unknown_type $sortItems - sort events by these fields in the order specified
     * @param unknown_type $firstResult - row to start with
     * @param unknown_type $lastResult - upper limit on number of events returned
     * @return unknown
     */
	public function getOpenEvents($appTypeId=null, $sortItems=null, $firstResult=-1, $lastResult=-1) 
	{
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
		
	    // creates a sort if necessary
	    if ($sortItems != null)
	      $sort = $this->setupSort($sortItems);
	    		
	    $opNameProp = new Property('operationStatus.name','OPEN');
	    $opNameFilter = new StringFilter('EQ');
	    $opNameFilter->setStringProperty($opNameProp);
	    if ($appTypeId != null)
	    {
	        $appTypeProp = new Property('applicationType.applicationTypeId', $appTypeId);
	        $appTypeFilter = new IntegerFilter('EQ');
	        $appTypeFilter->setIntegerProperty($appTypeProp);
	        $filter = new Filter('AND');
	        $filter->setLeftFilter($appTypeFilter);
	        $filter->setRightFilter($opNameFilter);
	    }
	    else 
	    {
	        $filter = $opNameFilter;
	    }
		    
        // TODO: retry around the soap calls too?
        try {
    	    $soapVar = new SoapVar($filter, SOAP_ENC_OBJECT, "Filter");
            $soapParam = new SoapParam($soapVar, "Filter");
            $result = $this->client->getEventsByCriteria($soapParam, $sort, $firstResult, $lastResult); 
	        $processedEvents = $this->processEvents($result);
	        return $processedEvents;
        }
        catch (SoapFault $soapEx)
        {
	  	    // try one more time for a connection in case the connection has gone stale.
	  	    try {
	  	        $this->getSoapConnection();
          	    $result = $this->client->getEventsByCriteria($soapParam, $sort, $firstResult, $lastResult); 
        	    $processedEvents = $this->processEvents($result);
	  	        return $processedEvents;    
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
	
	public function getOpenEventsByHostGroupId($hostGroupId, $appTypeId=null, $sortItems=null, $firstResult=-1, $lastResult=-1) 
	{
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
	    // creates a sort if necessary
	    if ($sortItems != null)
	      $sort = $this->setupSort($sortItems);
	    		
	    $intProp = new Property('hostStatus.host.hostGroups.hostGroupId',$hostGroupId);
	    $hgIdFilter = new IntegerFilter('EQ');
	    $hgIdFilter->setIntegerProperty($intProp);
	    $opNameProp = new Property('operationStatus.name','OPEN');
	    $opNameFilter = new StringFilter('EQ');
	    $opNameFilter->setStringProperty($opNameProp);
	    $hgOpNameFilter = new Filter('AND');
	    $hgOpNameFilter->setLeftFilter($hgIdFilter);
	    $hgOpNameFilter->setRightFilter($opNameFilter);
	    if ($appTypeId != null)
	    {
	        $appTypeProp = new Property('applicationType.applicationTypeId', $appTypeId);
	        $appTypeFilter = new IntegerFilter('EQ');
	        $appTypeFilter->setIntegerProperty($appTypeProp);
	        $filter = new Filter('AND');
	        $filter->setLeftFilter($appTypeFilter);
	        $filter->setRightFilter($hgOpNameFilter);
	    }
	    else 
	    {
	        $filter = $hgOpNameFilter;
	    }
	    
	    try {
    	    $soapVar = new SoapVar($filter, SOAP_ENC_OBJECT, "Filter");
	        $soapParam = new SoapParam($soapVar, "Filter");
      	    $result = $this->client->getEventsByCriteria($soapParam, $sort, $firstResult, $lastResult); 
    	    $processedEvents = $this->processEvents($result);
            return $processedEvents;
	  	}	   
	  	catch(SoapFault $exception) {
	  	    // try one more time for a connection in case the connection has gone stale.
	  	    try {
	  	        $this->getSoapConnection();
          	    $result = $this->client->getEventsByCriteria($soapParam, $sort, $firstResult, $lastResult); 
        	    $processedEvents = $this->processEvents($result);
	  	        return $processedEvents;    
	  	    }
	  	    catch(SoapFault $soapEx) {
	  	        // if we still don't succeed, release the SoapClient and bail out
	  	        $this->client = null;
			    throw $soapEx;
	  	    }
	  	}
	  	catch (Exception $e) {
	  		throw $e;
	  	}
	}
	
	public function getOpenEventsByHostId($hostId, $appTypeId=null, $sortItems=null, $firstResult=-1, $lastResult=-1) 
	{
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
	    // creates a sort if necessary
	    if ($sortItems != null)
	      $sort = $this->setupSort($sortItems);
	    		
	    $intProp = new Property('hostStatus.hostStatusId',$hostId);
	    $hostIdFilter = new IntegerFilter('EQ');
	    $hostIdFilter->setIntegerProperty($intProp);
	    $opNameProp = new Property('operationStatus.name','OPEN');
	    $opNameFilter = new StringFilter('EQ');
	    $opNameFilter->setStringProperty($opNameProp);
	    $filter = new Filter('AND');
	    $filter->setLeftFilter($hostIdFilter);
	    $filter->setRightFilter($opNameFilter);
	    
	    try {
    	    $soapVar = new SoapVar($filter, SOAP_ENC_OBJECT, "Filter");
	        $soapParam = new SoapParam($soapVar, "Filter");
      	    $result = $this->client->getEventsByCriteria($soapParam, $sort, $firstResult, $lastResult); 
    	    $processedEvents = $this->processEvents($result);
	  	    return $processedEvents;
	  	}	   
	  	catch(SoapFault $exception) {
	  	    // try one more time for a connection in case the connection has gone stale.
	  	    try {
	  	        $this->getSoapConnection();
          	    $result = $this->client->getEventsByCriteria($soapParam, $sort, $firstResult, $lastResult); 
        	    $processedEvents = $this->processEvents($result);
    	  	    return $processedEvents;
	  	    }
	  	    catch(SoapFault $soapEx) {
	  	        // if we still don't succeed, release the SoapClient and bail out
	  	        $this->client = null;
			    throw $soapEx;
	  	    }
	  	}
	  	catch (Exception $e) {
	  		throw $e;
	  	}
	}
		
	public function getOpenEventsByServiceDescription($serviceDescription, $hostId, $appTypeId=null, $sortItems=null, $firstResult=-1, $lastResult=-1) 
	{
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
	    // creates a sort if necessary
	    if ($sortItems != null)
	      $sort = $this->setupSort($sortItems);
	    		
	    $intProp = new Property('hostStatus.hostStatusId',$hostId);
	    $hostIdFilter = new IntegerFilter('EQ');
	    $hostIdFilter->setIntegerProperty($intProp);
	    $descriptionProp = new Property('serviceStatus.serviceDescription',$serviceDescription);
	    $serviceDescFilter = new StringFilter('EQ');
	    $serviceDescFilter->setStringProperty($descriptionProp);
	    $opNameProp = new Property('operationStatus.name','OPEN');
	    $opNameFilter = new StringFilter('EQ');
	    $opNameFilter->setStringProperty($opNameProp);
	    $serviceOpenFilter = new Filter('AND');
	    $serviceOpenFilter->setLeftFilter($serviceDescFilter);
	    $serviceOpenFilter->setRightFilter($opNameFilter);
	    $complexFilter = new Filter('AND');
	    $complexFilter->setLeftFilter($hostIdFilter);
	    $complexFilter->setRightFilter($serviceOpenFilter);
	    
	    try {
    	    $soapVar = new SoapVar($complexFilter, SOAP_ENC_OBJECT, "Filter");
    	    $soapParam = new SoapParam($soapVar, "Filter");
      	    $result = $this->client->getEventsByCriteria($soapParam, $sort, $firstResult, $lastResult); 
    	    $processedEvents = $this->processEvents($result);
	  	    return $processedEvents;
	  	}	   
	  	catch(SoapFault $exception) {
	  	    // try one more time for a connection in case the connection has gone stale.
	  	    try {
	  	        $this->getSoapConnection();
          	    $result = $this->client->getEventsByCriteria($soapParam, $sort, $firstResult, $lastResult); 
        	    $processedEvents = $this->processEvents($result);
	  	        return $processedEvents;    
	  	    }
	  	    catch(SoapFault $soapEx) {
	  	        // if we still don't succeed, release the SoapClient and bail out
	  	        $this->client = null;
			    throw $soapEx;
	  	    }
	  	}
	  	catch (Exception $e) {
	  		throw $e;
	  	}
	}
		
	private function processEvents($events)
	{
	    $processedEvents = array();
	    $processedEvents['Count'] = $events->TotalCount;
	    $counter = 0;
	    if ($events->TotalCount>1 && count($events->LogMessage)) {
	       foreach ($events->LogMessage as $message)
	       {
	           $processedMessage['LogMessageID'] = $message->LogMessageID;
	           $processedMessage['ApplicationName'] = $message->ApplicationName;
	           $processedMessage['TextMessage'] = $message->TextMessage;
	           $processedMessage['MessageCount'] = $message->MessageCount;
               $timestamp = strtotime($message->FirstInsertDate);
               $processedMessage['FirstInsertDate'] = $timestamp;
               $timestamp = strtotime($message->LastInsertDate);
               $processedMessage['LastInsertDate'] = $timestamp;
               $timestamp = strtotime($message->ReportDate);
               $processedMessage['ReportDate'] = $timestamp;
	           $processedMessage['Device'] = $message->Device;
	           $processedMessage['MonitorStatus'] = $message->MonitorStatus;
	           $processedMessage['Severity'] = $message->Severity;
	           $processedMessage['Host'] = $message->Host;
	           $properties = array();
	          if (isset($message->PropertyTypeBinding))
              {
                  foreach($message->PropertyTypeBinding as $propertyType => $props)
                  {
                      if (sizeof($props)>1) 
                      {
           	             foreach ($props as $propName)
           	             {
                              if (strcmp($propertyType, 'DateProperty')==0)
                              {
                                $timestamp = strtotime($propName->value);
                                $properties[$propName->name] = $timestamp;
              	              }
                  	          else 
              	             {
               		           $properties[$propName->name] = $propName->value;
                    	     }
              	          }
                      }                      
              	      else 
              	      {
      	                 if (strcmp($propertyType, 'DateProperty')==0)
      	                 {
      	                     $timestamp = strtotime($props->value);
      	                     $properties[$props->name] = $timestamp;
      	                 }
      	                 else {
                             $properties[$props->name] = $props->value;
     	                 }
              	       }
                    }
              } 
    	       $processedMessage['Properties'] = $properties;
	           $processedEvents['Messages'][$counter] = $processedMessage;
	           $counter++;
	       }
	    }
	    else if ($events->TotalCount>0) {
	       $processedMessage['LogMessageID'] = $events->LogMessage->LogMessageID;
	       $processedMessage['ApplicationName'] = $events->LogMessage->ApplicationName;
	       $processedMessage['TextMessage'] = $events->LogMessage->TextMessage;
	       $processedMessage['MessageCount'] = $events->LogMessage->MessageCount;
           $timestamp = strtotime($events->LogMessage->FistInsertDate);
           $processedMessage['FirstInsertDate'] = $timestamp;
           $timestamp = strtotime($events->LogMessage->LastInsertDate);
           $processedMessage['LastInsertDate'] = $timestamp;
           $timestamp = strtotime($events->LogMessage->ReportDate);
           $processedMessage['ReportDate'] = $timestamp;
	       $processedMessage['Device'] = $events->LogMessage->Device;
	       $processedMessage['MonitorStatus'] = $events->LogMessage->MonitorStatus;
	       $processedMessage['Severity'] = $events->LogMessage->Severity;
	       $processedMessage['Host'] = $events->LogMessage->Host;
	       $properties = array();
	       if (isset($events->LogMessage->PropertyTypeBinding))
	       {
               foreach($events->LogMessage->PropertyTypeBinding as $propertyType => $props)
               {
                  if (sizeof($props)>1) 
                  {                         
          	         foreach ($props as $propName)
          	         {
                          if (strcmp($propertyType, 'DateProperty')==0)
                          {
                            $timestamp = strtotime($propName->value);
                            $properties[$propName->name] = $timestamp;
            	          }
          	          
          	             else 
          	             {
                            $properties[$propName->name] = $propName->value;
                         }
          	          }
                      
                  }
                  else 
                  {
  	                 if (strcmp($propertyType, 'DateProperty')==0)
  	                 {
  	                     $timestamp = strtotime($props->value);
  	                     $properties[$props->name] = $timestamp;
  	                 }
  	                 else {
                         $properties[$props->name] = $props->value;
  	                 }
                  }
               }
	       }
	       $processedMessage['Properties'] = $properties;
	       $processedEvents['Messages'][0] = $processedMessage;
	    }
	    return $processedEvents;
	}
	
	// setup the Sort object for the webservice
	private function setupSort($sortItems)
	{
       $sort = null;
	    if ($sortItems != null && count($sortItems)>0)
	    {
           foreach ($sortItems as $field=>$asc)
           {
               $sortItem = new SortItem($asc, $field);
               if ($sort != null)
               {
                   $sort->addSortItem($sortItem);
               }
               else {
                   $sort = new Sort($sortItem);
               }
           }
	    }
	    return $sort;
	}
	
	private function getSoapConnection()
	{
        // loop until a connection is made or max retries is reached
		$retries = 0;
		while ($retries < $this->maxRetries)
		{
    	  	try {
    			@($this->client = new SoapClient(
    		    	$this->eventWebServiceURL,
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