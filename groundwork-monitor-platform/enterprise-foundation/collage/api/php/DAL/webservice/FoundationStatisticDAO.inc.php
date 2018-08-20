<?php


/*
 * This class interacts with the Foundation Statistic web service.
 */
class FoundationStatisticDAO {
	
	private $client;
	private $statisticWebServiceURL;
	private $maxRetries = 5;
	
	public function __construct($foundationURL) {
		global $foundationModule;
		
		ini_set("soap.wsdl_cache_enabled", "1"); // enable WSDL cache
		
		// a sample url: "http://172.28.113.131:8080/foundation-webapp/services/wsstatistics?wsdl",
		$this->statisticWebServiceURL = $foundationURL."/wsstatistics?wsdl";
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
	
	/*
	 * gets statistics for all services in the system.
	*/
	function getAllServiceStatistics() {
        try {
        	if (!isset($this->client))
                $this->getSoapConnection();
    		$statistics = @$this->client->getStatistics("TOTALS_FOR_SERVICES", "", "");
			// should put the following:
			// if (is_soap_fault($statistics))
			//   throw new Exception("SOAP Fault: (faultcode: {$statistics->faultcode}, faultstring: {$statistics->faultstring})"); 
		    $stats = $statistics->StateStatistics;
		    $serviceStats = array();
		    if (isset($stats)) {
		    	$serviceStats['Name'] = $stats->Name;
		    	$serviceStats['TotalServices'] = $stats->TotalServices;
		    	$serviceStats['TotalHosts'] = $stats->TotalHosts;
		    	foreach($stats->Statistic as $statistic) {
		    		$serviceStats[$statistic->name] = $statistic->count;
		    	}
	    	}
	    	return $serviceStats;
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
	 * gets statistics for all hosts in the system.
	*/
	function getAllHostStatistics() {
        try {
        	if (!isset($this->client))
                $this->getSoapConnection();
			$statistics = @$this->client->getStatistics("TOTALS_FOR_HOSTS", "", "");
			// should put the following:
			// if (is_soap_fault($statistics))
			//   throw new Exception("SOAP Fault: (faultcode: {$statistics->faultcode}, faultstring: {$statistics->faultstring})"); 
		    $stats = $statistics->StateStatistics;
		    $hostStats = array();
		    if (isset($stats)) {
		    	$hostStats['Name'] = $stats->Name;
		    	$hostStats['TotalServices'] = $stats->TotalServices;
		    	$hostStats['TotalHosts'] = $stats->TotalHosts;
		    	foreach($stats->Statistic as $statistic) {
		    		$hostStats[$statistic->name] = $statistic->count;
		    	}
	    	}
	    	return $hostStats;
	  	}
	  	catch(SoapFault $exception) {
	  		// TODO: Logging?
			throw $exception;
	  	}
	  	catch (Exception $e) {
	  		throw $e;
	  	}
	}
	
	/**
	 * gets statistics for all hostgroups.
	*/
	function getHostGroupStatistics() {
        try {
        	if (!isset($this->client))
                $this->getSoapConnection();
			$statistics = @$this->client->getStatistics("ALL_HOSTS", "", "");
			// should put the following:
			// if (is_soap_fault($statistics))
			//   throw new Exception("SOAP Fault: (f   aultcode: {$statistics->faultcode}, faultstring: {$statistics->faultstring})"); 
		    $stats = $statistics->StateStatistics;
		    $hostStats = array();
		    if (isset($stats)) {
		    	foreach($stats as $stat) {
			    	$hostStats[$stat->Name]['TotalServices'] = $stat->TotalServices;
			    	$hostStats[$stat->Name]['TotalHosts'] = $stat->TotalHosts;
			    	foreach($stat->Statistic as $statistic) {
			    		$hostStats[$stat->Name][$statistic->name] = $statistic->count;
			    	}
		    	}
	    	}
	    	return $hostStats;
	  	}
	  	catch(SoapFault $exception) {
	  		// TODO: Logging?
			throw $exception;
	  	}
	  	catch (Exception $e) {
	  		throw $e;
	  	}
	}
	
	/**
	 * gets statistics for all hosts in the specified hostgroup.
	 * @param $hostGroupID - the ID of the hostgroup to get stats for 
	*/
	public function getHostGroupHostStatistics($hostGroupID) {
		if (!isset($hostGroupID)) {
			return null;
		}
        try {
        	if (!isset($this->client))
                $this->getSoapConnection();
			$statistics = @$this->client->getStatistics("HOSTS_FOR_HOSTGROUPID", $hostGroupID, "");
			// should put the following:
			// if (is_soap_fault($statistics))
			//   throw new Exception("SOAP Fault: (faultcode: {$statistics->faultcode}, faultstring: {$statistics->faultstring})"); 
		    $stats = $statistics->StateStatistics;
		    $hostStats = null;
		    if (isset($stats)) {
		    	$hostStats = array();
		    	$hostStats['HostGroupName'] = $stats->Name;
		    	$hostStats['TotalServices'] = $stats->TotalServices;
		    	$hostStats['TotalHosts'] = $stats->TotalHosts;
		    	foreach($stats->Statistic as $statistic) {
					    		$hostStats[$statistic->name] = $statistic->count;
		    	}
	    	}
	    	
	    	return $hostStats;
	  	}
	  	catch(SoapFault $exception) {
	  		// TODO: Logging?
	  		throw $exception;
	  	}
	  	catch (Exception $e) {
	  		throw $e;
	  	}
		
	}
	
	/**
	 * gets statistics for all hosts in the specified hostgroup.
	 * @param $hostGroupName - the name of the hostgroup to get stats for 
	 */
	public function getHostGroupHostStatisticsByName($hostGroupName) {
		if (!isset($hostGroupName)) {
			return null;
		}
        try {
        	if (!isset($this->client))
                $this->getSoapConnection();
			$statistics = @$this->client->getStatistics("HOSTS_FOR_HOSTGROUPNAME", $hostGroupName, "");
			// should put the following:
			// if (is_soap_fault($statistics))
			//   throw new Exception("SOAP Fault: (faultcode: {$statistics->faultcode}, faultstring: {$statistics->faultstring})"); 
		    $stats = $statistics->StateStatistics;
		    $hostStats = null;
		    if (isset($stats)) {
		    	$hostStats = array();
		    	$hostStats['HostGroupName'] = $stats->Name;
		    	$hostStats['TotalServices'] = $stats->TotalServices;
		    	$hostStats['TotalHosts'] = $stats->TotalHosts;
		    	foreach($stats->Statistic as $statistic) {
					    		$hostStats[$statistic->name] = $statistic->count;
		    	}
	    	}
	    	
	    	return $hostStats;
	  	}
	  	catch(SoapFault $exception) {
	  		// TODO: Logging?
	  		throw $exception;
	  	}
	  	catch (Exception $e) {
	  		throw $e;
	  	}		
	}
		
	/**
	 * gets statistics for all services in the specified hostgroup.
	 * @param $hostGroupID - the id of the hostgroup to retrieve statistics for
	*/
	public function getHostGroupServiceStatistics($hostGroupID) {
		if (!isset($hostGroupID)) {
			return null;
		}
        try {
        	if (!isset($this->client))
                $this->getSoapConnection();
			$statistics = @$this->client->getStatistics("SERVICES_FOR_HOSTGROUPID", $hostGroupID, "");
			// should put the following:
			// if (is_soap_fault($statistics))
			//   throw new Exception("SOAP Fault: (faultcode: {$statistics->faultcode}, faultstring: {$statistics->faultstring})"); 
	    	$stats = $statistics->StateStatistics;
	    	$serviceStats = null;
		    if (isset($stats)) {
	    		$serviceStats = array();
		    	$serviceStats['HostGroupName'] = $stats->Name;
		    	$serviceStats['TotalServices'] = $stats->TotalServices;
		    	$serviceStats['TotalHosts'] = $stats->TotalHosts;
		    	foreach($stats->Statistic as $statistic) {
		    		$serviceStats[$statistic->name] = $statistic->count;
		    	}
	    	}
    	
    		return $serviceStats;
	  	}
	  	catch(SoapFault $exception) {
	  		// TODO: Logging?
	  		throw $exception;
	  	}
	  	catch (Exception $e) {
	  		//print("DEBUG: AN UNEXPECTED EXCEPTION OCCURRED! ".$e->getMessage());
	  		throw $exception;
	  	}				
	}
	
	/**
	 * gets statistics for all services in the specified hostgroup.
	 * @param $hostGroupName - the name of the hostgroup to retrieve statistics for
	*/
	public function getHostGroupServiceStatisticsByName($hostGroupName) {
		if (!isset($hostGroupName)) {
			return null;
		}
        try {
        	if (!isset($this->client))
                $this->getSoapConnection();
			$statistics = @$this->client->getStatistics("SERVICES_FOR_HOSTGROUPNAME", $hostGroupName, "");
			// should put the following:
			// if (is_soap_fault($statistics))
			//   throw new Exception("SOAP Fault: (faultcode: {$statistics->faultcode}, faultstring: {$statistics->faultstring})"); 
	    	$stats = $statistics->StateStatistics;
	    	$serviceStats = null;
		    if (isset($stats)) {
	    		$serviceStats = array();
		    	$serviceStats['HostGroupName'] = $stats->Name;
		    	$serviceStats['TotalServices'] = $stats->TotalServices;
		    	$serviceStats['TotalHosts'] = $stats->TotalHosts;
		    	foreach($stats->Statistic as $statistic) {
		    		$serviceStats[$statistic->name] = $statistic->count;
		    	}
	    	}
    	
    		return $serviceStats;
	  	}
	  	catch(SoapFault $exception) {
	  		// TODO: Logging?
	  		throw $exception;
	  	}
	  	catch (Exception $e) {
	  		//print("DEBUG: AN UNEXPECTED EXCEPTION OCCURRED! ".$e->getMessage());
	  		throw $exception;
	  	}				
	}
		
	/**
	 * gets statistics for all services for the specified host.
	 * @param $hostName - the name of the host to retrieve service statistics for
	*/
	public function getHostServiceStatistics($hostName) {
		if ($hostName == null || sizeof($hostName)==0) {
			return null;
		}
        try {
        	if (!isset($this->client))
                $this->getSoapConnection();
			$statistics = @$this->client->getStatistics("TOTALS_FOR_SERVICES_BY_HOSTNAME", $hostName, "");
			// should put the following:
			// if (is_soap_fault($statistics))
			//   throw new Exception("SOAP Fault: (faultcode: {$statistics->faultcode}, faultstring: {$statistics->faultstring})"); 
	    	$stats = $statistics->StateStatistics;
	    	$serviceStats = null;
		    if (isset($stats)) {
	    		$serviceStats = array();
		    	$serviceStats['Name'] = $stats->Name;
		    	$serviceStats['TotalServices'] = $stats->TotalServices;
		    	$serviceStats['TotalHosts'] = $stats->TotalHosts;
		    	foreach($stats->Statistic as $statistic) {
		    		$serviceStats[$statistic->name] = $statistic->count;
		    	}
	    	}
    	
    		return $serviceStats;
	  	}
	  	catch(SoapFault $exception) {
	  		// TODO: Logging?
	  		throw $exception;
	  	}
	  	catch (Exception $e) {
	  		//print("DEBUG: AN UNEXPECTED EXCEPTION OCCURRED! ".$e->getMessage());
	  		throw $exception;
	  	}
				
	}
	
	/**
	 * gets nagios related statistics for the hostgroup specified.
	 * @param $hostGroupID - the id of the hostgroup to retrieve statistics for
	*/
	public function getHostGroupNagiosStatistics($hostGroupID) {
		if (!isset($hostGroupID)) {
			return null;
		}
        try {
        	if (!isset($this->client))
                $this->getSoapConnection();
			$statistics = @$this->client->getNagiosStatistics("HOSTGROUPID", $hostGroupID);
		    $stats = $statistics->NagiosStatisticCollection;
		    $nagiosStats = null;
		    if (isset($stats)) {
			    $nagiosStats = array();
		    	foreach($stats as $statistic) {
		    		$nagiosStats[$statistic->PropertyName]['HostsEnabled'] = $statistic->HostStatisticEnabled;
		    		$nagiosStats[$statistic->PropertyName]['HostsDisabled'] = $statistic->HostStatisticDisabled;
		    		$nagiosStats[$statistic->PropertyName]['ServicesEnabled'] = $statistic->ServiceStatisticEnabled;
		    		$nagiosStats[$statistic->PropertyName]['ServicesDisabled'] = $statistic->ServiceStatisticDisabled;
		    	}
	    	}
	    	return $nagiosStats;
		}
	  	catch(SoapFault $exception) {
	  		// TODO: Logging?
	  		throw $exception;
	  	}
	  	catch (Exception $e) {
	  		//print("DEBUG: AN UNEXPECTED EXCEPTION OCCURRED! ".$e->getMessage());
	  		throw $exception;
	  	}
	}

	/**
	 * gets nagios related statistics for the hostgroup specified.
	 * @param $hostGroupName - the id of the hostgroup to retrieve statistics for
	*/
	public function getHostGroupNagiosStatisticsByName($hostGroupName) {
		if (!isset($hostGroupName)) {
			return null;
		}
        try {
        	if (!isset($this->client))
                $this->getSoapConnection();
			$statistics = @$this->client->getNagiosStatistics("HOSTGROUPNAME", $hostGroupName);
		    $stats = $statistics->NagiosStatisticCollection;
		    $nagiosStats = null;
		    if (isset($stats)) {
			    $nagiosStats = array();
		    	foreach($stats as $statistic) {
		    		$nagiosStats[$statistic->PropertyName]['HostsEnabled'] = $statistic->HostStatisticEnabled;
		    		$nagiosStats[$statistic->PropertyName]['HostsDisabled'] = $statistic->HostStatisticDisabled;
		    		$nagiosStats[$statistic->PropertyName]['ServicesEnabled'] = $statistic->ServiceStatisticEnabled;
		    		$nagiosStats[$statistic->PropertyName]['ServicesDisabled'] = $statistic->ServiceStatisticDisabled;
		    	}
	    	}
	    	return $nagiosStats;
		}
	  	catch(SoapFault $exception) {
	  		// TODO: Logging?
	  		throw $exception;
	  	}
	  	catch (Exception $e) {
	  		//print("DEBUG: AN UNEXPECTED EXCEPTION OCCURRED! ".$e->getMessage());
	  		throw $exception;
	  	}
	}
	
	/**
	 * gets nagios related statistics for the host specified.
	 * @param $hostName - the name of the host to retrieve statistics for
	*/
	public function getHostNagiosStatistics($hostName) {
		if (!isset($hostName)) {
			return null;
		}
        try {
        	if (!isset($this->client))
                $this->getSoapConnection();
			$statistics = @$this->client->getNagiosStatistics("HOSTNAME", $hostName);
		    $stats = $statistics->NagiosStatisticCollection;
		    $nagiosStats = null;
		    if (isset($stats)) {
			    $nagiosStats = array();
		    	foreach($stats as $statistic) {
		    		$nagiosStats[$statistic->PropertyName]['ServicesEnabled'] = $statistic->ServiceStatisticEnabled;
		    		$nagiosStats[$statistic->PropertyName]['ServicesDisabled'] = $statistic->ServiceStatisticDisabled;
		    	}
	    	}
	    	return $nagiosStats;
		}
	  	catch(SoapFault $exception) {
	  		// TODO: Logging?
	  		throw $exception;
	  	}
	  	catch (Exception $e) {
	  		//print("DEBUG: AN UNEXPECTED EXCEPTION OCCURRED! ".$e->getMessage());
	  		throw $exception;
	  	}
	}	
	
	/**
	 * gets nagios related statistics totals for all hosts and services
	*/
	public function getAllNagiosStatistics() 
	{
        try {
        	if (!isset($this->client))
                $this->getSoapConnection();
			$statistics = @$this->client->getNagiosStatistics("SYSTEM", "");
		    $stats = $statistics->NagiosStatisticCollection;
		    $nagiosStats = null;
		    if (isset($stats)) {
			    $nagiosStats = array();
		    	foreach($stats as $statistic) {
		    		$nagiosStats[$statistic->PropertyName]['HostsEnabled'] = $statistic->HostStatisticEnabled;
		    		$nagiosStats[$statistic->PropertyName]['HostsDisabled'] = $statistic->HostStatisticDisabled;
		    		$nagiosStats[$statistic->PropertyName]['ServicesEnabled'] = $statistic->ServiceStatisticEnabled;
		    		$nagiosStats[$statistic->PropertyName]['ServicesDisabled'] = $statistic->ServiceStatisticDisabled;
		    	}
	    	}
	    	return $nagiosStats;
		}
	  	catch(SoapFault $exception) {
	  		// TODO: Logging?
	  		throw $exception;
	  	}
	  	catch (Exception $e) {
	  		//print("DEBUG: AN UNEXPECTED EXCEPTION OCCURRED! ".$e->getMessage());
	  		throw $exception;
	  	}
	}	
	
	/*
	 * gets hostgroup counts for host states in the system.  The counts returned represent
	 * the number hostgroups which have hosts in the particular state.
	*/
	public function getHostGroupCountsByHost() {
        try {
        	if (!isset($this->client))
                $this->getSoapConnection();
			$statistics = @$this->client->getStatistics("HOSTGROUP_STATE_COUNTS_HOST", "", "");
  		    $stats = $statistics->StatisticCollection;
			// should put the following:
			// if (is_soap_fault($statistics))
			//   throw new Exception("SOAP Fault: (faultcode: {$statistics->faultcode}, faultstring: {$statistics->faultstring})"); 
		    $hostGroupStats = array();
		    if (isset($stats)) {
		    	foreach($stats as $statistic) {
		    		$hostGroupStats[$statistic->name] = $statistic->count;
		    	}
	    	}
	    	return $hostGroupStats;
	  	}
	  	catch(SoapFault $exception) {
	  		// TODO: Logging?
			throw $exception;
	  	}
	  	catch (Exception $e) {
	  		throw $e;
	  	}		
	}

	/**
	 * gets hostgroup counts for service states in the system.  The counts returned represent
	 * the number hostgroups which have services in the particular state.
	*/
	public function getHostGroupCountsByService() 
	{
        try {
        	if (!isset($this->client))
                $this->getSoapConnection();
			$statistics = @$this->client->getStatistics("HOSTGROUP_STATE_COUNTS_SERVICE", "", "");
  		    $stats = $statistics->StatisticCollection;
			// should put the following:
			// if (is_soap_fault($statistics))
			//   throw new Exception("SOAP Fault: (faultcode: {$statistics->faultcode}, faultstring: {$statistics->faultstring})"); 
		    $hostGroupStats = array();
		    if (isset($stats)) {
		    	foreach($stats as $statistic) {
		    		$hostGroupStats[$statistic->name] = $statistic->count;
		    	}
	    	}
	    	return $hostGroupStats;
	  	}
	  	catch(SoapFault $exception) {
	  		// TODO: Logging?
			throw $exception;
	  	}
	  	catch (Exception $e) {
	  		throw $e;
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
    		    	$this->statisticWebServiceURL,
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