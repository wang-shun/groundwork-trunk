<?php
require_once('WSProperties.inc.php');
require_once('WSFilter.inc.php');
require_once('WSSort.inc.php');
class FoundationMetaDataDAO {
	
	private $client;
	private $commonWebServiceURL;
	private $maxRetries = 5;
	
	
	public function __construct($foundationURL) 
	{

		ini_set("soap.wsdl_cache_enabled", "1"); // enable WSDL cache		
		// a sample url: "http://172.28.113.131:8080/foundation-webapp/services/wshostgroup?wsdl",
		$this->commonWebServiceURL = $foundationURL."/wscommon?wsdl";
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
	
	public function getApplicationTypes()
	{
        try {
        	if (!isset($this->client))
                $this->getSoapConnection();
       	    $appTypes = $this->client->getAttributeData("APPLICATION_TYPES");
      	    $processedAppTypes['TotalCount'] = $appTypes->TotalCount;
      	    foreach($appTypes->AttributeData as $apptype) 
      	    {
                $processedApp[$apptype->Name]['Name'] = $apptype->Name;
                $processedApp[$apptype->Name]['ApplicationTypeID'] = $apptype->AttributeID;
                $processedApp[$apptype->Name]['Description'] = $apptype->Description;
      	    }
      	    $processedAppTypes['ApplicationTypes'] = $processedApp;
            return $processedAppTypes;	    
	  	}
		catch(SoapFault $soapFault)
		{
	  	    // try one more time for a connection in case the connection has gone stale.
	  	    try {
	  	        $this->getSoapConnection();
           	    $appTypes = $this->client->getAttributeData("APPLICATION_TYPES");
          	    $processedAppTypes['TotalCount'] = $appTypes->TotalCount;
          	    foreach($appTypes->AttributeData as $apptype) 
          	    {
                    $processedApp[$apptype->Name]['Name'] = $apptype->Name;
                    $processedApp[$apptype->Name]['ApplicationTypeID'] = $apptype->AttributeID;
                    $processedApp[$apptype->Name]['Description'] = $apptype->Description;
          	    }
          	    $processedAppTypes['ApplicationTypes'] = $processedApp;
                return $processedAppTypes;	    
	  	    }
	  	    catch(SoapFault $soapEx) {
	  	        // if we still don't succeed, release the SoapClient and bail out
	  	        $this->client = null;
			    throw $soapEx;
	  	    }
  	    }
  	    catch(Exception $exception) {
  	        throw $exception;
  	    }
  	    
	}

	private function getSoapConnection()
	{
        // loop until a connection is made or max retries is reached
		$retries = 0;
		while ($retries < $this->maxRetries)
		{
    	  	try {
    			@($this->client = new SoapClient(
    		    	$this->commonWebServiceURL,
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