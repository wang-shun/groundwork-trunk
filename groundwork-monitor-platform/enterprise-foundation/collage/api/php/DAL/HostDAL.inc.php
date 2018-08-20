<?php
/**
 * @author Robin Dandridge
 * @package DAL
 * @version 1.0
 */

/**
 * include necessary files
 */
require_once('DAL/DAOFactory.inc.php');
require_once('DAL/DALException.inc.php');

/**
 * Provides access to Host and HostStatus information stored in the Foundation Database.
 * Host data is returned in the following format:
Array
(
    [Count] => 2
    [Hosts] => Array
        (
            [172.28.111.17] => Array
                (
                    [HostID] => 2
                    [Name] => 172.28.111.17
                    [ApplicationTypeID] => 100
                    [LastCheckTime] => 04-18-2007 14:54:17PM
                    [Device] => stdClass Object
                        (
                            [DeviceID] => 3
                            [Name] => 172.28.111.17
                            [Identification] => 172.28.111.17
                        )

                    [MonitorStatus] => stdClass Object
                        (
                            [MonitorStatusID] => 7
                            [Name] => UP
                            [Description] => Status UP
                        )

                    [LastPluginOutput] => OK - 172.28.111.17: rta 73.889ms, lost 0%
                    [CurrentNotificationNumber] => 0
                    [ScheduledDowntimeDepth] => 0
                    [TimeDown] => 1176762847
                    [TimeUnreachable] => 0
                    [TimeUp] => 1176933257
                    [ExecutionTime] => 0
                    [Latency] => 0
                    [PercentStateChange] => 0
                    [isAcknowledged] =>
                    [isChecksEnabled] => 1
                    [isEventHandlersEnabled] => 1
                    [isFailurePredictionEnabled] => 1
                    [isFlapDetectionEnabled] => 1
                    [isHostFlapping] =>
                    [isNotificationsEnabled] => 1
                    [isPassiveChecksEnabled] => 1
                    [isProcessPerformanceData] => 1
                    [LastStateChange] => 04-16-2007 15:34:16PM
                )

            [172.28.111.18] => Array
                (
                    [HostID] => 3
                    [Name] => 172.28.111.18
                    [ApplicationTypeID] => 100
                    [LastCheckTime] => 04-18-2007 14:56:25PM
                    [Device] => stdClass Object
                        (
                            [DeviceID] => 4
                            [Name] => 172.28.111.18
                            [Identification] => 172.28.111.18
                        )

                    [MonitorStatus] => stdClass Object
                        (
                            [MonitorStatusID] => 7
                            [Name] => UP
                            [Description] => Status UP
                        )

                    [LastPluginOutput] => OK - 172.28.111.18: rta 62.015ms, lost 0%
                    [CurrentNotificationNumber] => 0
                    [ScheduledDowntimeDepth] => 0
                    [TimeDown] => 1176846162
                    [TimeUnreachable] => 0
                    [TimeUp] => 1176933385
                    [ExecutionTime] => 0
                    [Latency] => 0
                    [PercentStateChange] => 0
                    [isAcknowledged] =>
                    [isChecksEnabled] => 1
                    [isEventHandlersEnabled] => 1
                    [isFailurePredictionEnabled] => 1
                    [isFlapDetectionEnabled] => 1
                    [isHostFlapping] =>
                    [isNotificationsEnabled] => 1
                    [isPassiveChecksEnabled] => 1
                    [isProcessPerformanceData] => 1
                    [LastStateChange] => 04-17-2007 14:42:42PM
                )
        )

)
 * @package DAL
 */
class HostDAL {
	static private $dataHostFactory;
	static private $foundationURL;
		
    /**
     * Create a HostDAL instance
     *
     * @param string $foundationURL - cannot be null
     */
	public function __construct($foundationURL) 
	{
	    if ($foundationURL == null)
	       throw new DALException("A path to the Foundation Webservice must be provided");
	    $this->foundationURL = $foundationURL;
	}
	
    /**
     * Get all the hosts - no filters or restrictions
     *
     * @return a list of Hosts
     */
	public function getHosts() {
		try {
			if (HostDAL::$dataHostFactory == null) {
					HostDAL::$dataHostFactory = @DAOFactory::getDAO($this->foundationURL, DAOFactory::FOUNDATIONWS_HOST);
			}
			return @HostDAL::$dataHostFactory->getHosts();
		}
		catch(SoapFault $exception) {
			throw new DALException("An error occurred while connecting to the foundation webservice.".$exception->getMessage());
		}
		catch(Exception $e) {
			throw new DALException("An unexpected error occurred.");
		}
	}
	
    /**
     * get all the hosts for a hostgroup specified by hostgroupId
     *
     * @param integer $hostGroupId Id of the hostgroup to get hosts for
     * @param integer $sort associative array of field to sort by => true for ASC, false if not
     * @param integer $firstResult lower limit for row to begin retrieving hosts
     * @param integer $maxResults upper limit for row to end at when retrieving hosts
     * @return a list of hosts
     */
    public function getHostsByHostGroupID($hostGroupId, $sort=null, $firstResult=-1, $maxResults=-1) {
		try {
			if (HostDAL::$dataHostFactory == null) {
					HostDAL::$dataHostFactory = @DAOFactory::getDAO($this->foundationURL, DAOFactory::FOUNDATIONWS_HOST);
			}
			return @HostDAL::$dataHostFactory->getHostsByHostGroupID($hostGroupId, $sort, $firstResult, $firstResult);
		}
		catch(SoapFault $exception) {
			throw new DALException("An error occurred while connecting to the foundation webservice.".$exception->getMessage());
		}
		catch(Exception $e) {
			throw new DALException("An unexpected error occurred.");
		}
	}
	
    /**
     * get all the hosts for the hostgroup specified by hostGroupName
     *
     * @param string $hostGroupName Name of the hostgroup to get hosts for
     * @param integer $sort associative array of field to sort by => true for ASC, false if not
     * @param integer $firstResult lower limit for row to begin retrieving hosts
     * @param integer $maxResults upper limit for row to end at when retrieving hosts
     * @return a list of hosts
     */
    public function getHostsByHostGroupName($hostGroupName, $sort=null, $firstResult=-1, $maxResults=-1) {
	    if ($hostGroupName == null)
	       throw new DALException("A Host Group name must be provided.");
	       
		try {
			if (HostDAL::$dataHostFactory == null) {
					HostDAL::$dataHostFactory = @DAOFactory::getDAO($this->foundationURL, DAOFactory::FOUNDATIONWS_HOST);
			}
			return @HostDAL::$dataHostFactory->getHostsByHostGroupName($hostGroupName, $sort, $firstResult, $maxResults);
		}
		catch(SoapFault $exception) {
			throw new DALException("An error occurred while connecting to the foundation webservice.".$exception->getMessage());
		}
		catch(Exception $e) {
			throw new DALException("An unexpected error occurred.");
		}
	}
	
    /**
     * Get hosts associated with the service with the description provided
     *
     * @param string $serviceDescription Description of Service to get hosts for
     * @return a list of hosts
     */
	public function getHostsByService($serviceDescription) {
	    if ($serviceDescription == null)
	       throw new DALException("A service description must be provided.");
	       
		try {
			if (HostDAL::$dataHostFactory == null) {
					HostDAL::$dataHostFactory = @DAOFactory::getDAO($this->foundationURL, DAOFactory::FOUNDATIONWS_HOST);
			}
			return @HostDAL::$dataHostFactory->getHostsByService($serviceDescription);
		}
		catch(SoapFault $exception) {
			throw new DALException("An error occurred while connecting to the foundation webservice.".$exception->getMessage());
		}
		catch(Exception $e) {
			throw new DALException("An unexpected error occurred.");
		}
	}
	
    /**
     * Get the hosts for the monitor server specified
     *
     * @param string $monitorServerName
     * @return a list of hosts
     */
	public function getHostsByMonitorServerName($monitorServerName) {
	    if ($monitorServerName == null)
	       throw new DALException("A monitor server name must be provided.");
	       
		try {
			if (HostDAL::$dataHostFactory == null) {
					HostDAL::$dataHostFactory = @DAOFactory::getDAO($this->foundationURL, DAOFactory::FOUNDATIONWS_HOST);
			}
			return @HostDAL::$dataHostFactory->getHostsByMonitorServerName($monitorServerName);
		}
		catch(SoapFault $exception) {
			throw new DALException("An error occurred while connecting to the foundation webservice.".$exception->getMessage());
		}
		catch(Exception $e) {
			throw new DALException("An unexpected error occurred.");
		}
	}
	
    /**
     * get a host by hostname
     *
     * @param string $hostName
     * @return a list of hosts
     */
	public function getHostByHostName($hostName) {
	    if ($hostName == null)
	       throw new DALException("A host name must be provided.");
	       
		try {
			if (HostDAL::$dataHostFactory == null) {
					HostDAL::$dataHostFactory = @DAOFactory::getDAO($this->foundationURL, DAOFactory::FOUNDATIONWS_HOST);
			}
			return @HostDAL::$dataHostFactory->getHostByHostName($hostName);
		}
		catch(SoapFault $exception) {
			throw new DALException("An error occurred while connecting to the foundation webservice.".$exception->getMessage());
		}
		catch(Exception $e) {
			throw new DALException("An unexpected error occurred.");
		}
	}
	
    /**
     * Get a host by host id
     *
     * @param integer $hostId
     * @return a host
     */
	public function getHostByHostId($hostId) {
	    if ($hostId == null || $hostId == 0)
	       throw new DALException("A host ID must be provided");
	       
		try {
			if (HostDAL::$dataHostFactory == null) {
					HostDAL::$dataHostFactory = @DAOFactory::getDAO($this->foundationURL, DAOFactory::FOUNDATIONWS_HOST);
			}
			return @HostDAL::$dataHostFactory->getHostByHostId($hostId);
		}
		catch(SoapFault $exception) {
			throw new DALException("An error occurred while connecting to the foundation webservice.".$exception->getMessage());
		}
		catch(Exception $e) {
			throw new DALException("An unexpected error occurred.");
		}
	}

    /**
     * Get all troubled hosts - Hosts with a monitor status which is not UP.
     * Use $firstResult and $maxResults to constrain the number of hosts that
     * are returned at one time.    
     *
     * @param $firstResult Number of host to start with - used for pagination
     * @param $maxResults Total number of hosts to return, starting with $firstResult.
     * @return a list of hosts
     */
    public function getTroubledHosts($firstResult=-1, $maxResults=-1)
	{
		try {
			if (HostDAL::$dataHostFactory == null) {
					HostDAL::$dataHostFactory = @DAOFactory::getDAO($this->foundationURL, DAOFactory::FOUNDATIONWS_HOST);
			}
			return @HostDAL::$dataHostFactory->getTroubledHosts($firstResult, $maxResults);
		}
		catch(SoapFault $exception) {
			throw new DALException("An error occurred while connecting to the foundation webservice.".$exception->getMessage());
		}
		catch(Exception $e) {
			throw new DALException("An unexpected error occurred.");
		}
	}
	
    /**
     * Retrieves all hosts specified in host name array.  If a host is not found then it is just not returned and 
     * no exception is thrown.  
     * Use $firstResult and $maxResults to constrain the number of hosts that
     * are returned at one time.    
     *
     * @param list $hostNames Hosts to retrieve details for.
     * @param list $sort a list of fields to use to sort the list of hosts returned.
     * @param integer $firstResult Number of host to start with - used for pagination
     * @param integer $maxResults Total number of hosts to return, starting with $firstResult.
     * @return list of Hosts
     */
    public function getHostsByHostNames($hostNames, $sort, $firstResult, $maxResults)
	{
		try {
			if (HostDAL::$dataHostFactory == null) {
					HostDAL::$dataHostFactory = @DAOFactory::getDAO($this->foundationURL, DAOFactory::FOUNDATIONWS_HOST);
			}
			return @HostDAL::$dataHostFactory->getHostsByHostNames($hostNames, $sort, $firstResult, $maxResults);
		}
		catch(SoapFault $exception) {
			throw new DALException("An error occurred while connecting to the foundation webservice.".$exception->getMessage());
		}
		catch(Exception $e) {
			throw new DALException("An unexpected error occurred.");
		}
	}
	
    /**
     * Retrieve hosts by defining a filter, sort and pagination parameters.
     * 
     * @param $filter 
     * @param $sort - items to sort by
     * @param $firstResult
     * @param $maxResults
     * @return a list of hosts
     */
    public function getHostsByFilter($filter=null, $sort=null, $firstResult=-1, $maxResults=-1)
	{
		try {
			if (HostDAL::$dataHostFactory == null) {
					HostDAL::$dataHostFactory = @DAOFactory::getDAO($this->foundationURL, DAOFactory::FOUNDATIONWS_HOST);
			}
			return @HostDAL::$dataHostFactory->getHostsByFilter($filter, $sort, $firstResult, $maxResults);
		}
		catch(SoapFault $exception) {
			throw new DALException("An error occurred while connecting to the foundation webservice.".$exception->getMessage());
		}
		catch(Exception $e) {
			throw new DALException("An unexpected error occurred.");
		}
	}
}
?>