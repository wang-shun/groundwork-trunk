<?php
/**
 * @author Robin Dandridge
 * @package DAL
 * @version 1.0
 *
 */

/**
 * include necessary files
 */
require_once('DAL/DAOFactory.inc.php');
require_once('DAL/DALException.inc.php');

/**
 * Provides access to the Services (ServiceStatus) stored in the Foundation Database.
 * 
     * data is returned in the following format for multiple services:
        Array
        (
            [Count] => 550
            [Services] => Array
                (
                    [1] => Array
                        (
                            [ServiceStatusID] => 1
                            [ApplicationTypeID] => 100
                            [Description] => Current Load
                            [Host] => stdClass Object
                                (
                                    [HostID] => 1
                                    [ApplicationTypeID] => 100
                                    [Device] => stdClass Object
                                        (
                                            [DeviceID] => 1
                                            [Name] => 127.0.0.1
                                            [Identification] => 127.0.0.1
                                        )
        
                                    [Name] => localhost
                                    [MonitorStatus] => stdClass Object
                                        (
                                            [MonitorStatusID] => 7
                                            [Name] => UP
                                            [Description] => Status UP
                                        )
        
                                    [LastCheckTime] => 2007-04-18T18:23:54.000Z
                                    [PropertyTypeBinding] => stdClass Object
                                        (
                                            [StringProperty] => stdClass Object
                                                (
                                                    [name] => LastPluginOutput
                                                    [value] => OK - 127.0.0.1: rta 0.058ms, lost 0%
                                                )
        
                                            [IntegerProperty] => Array
                                                (
                                                    [0] => stdClass Object
                                                        (
                                                            [name] => CurrentNotificationNumber
                                                            [value] => 0
                                                        )
        
                                                    [1] => stdClass Object
                                                        (
                                                            [name] => ScheduledDowntimeDepth
                                                            [value] => 0
                                                        )
        
                                                )
        
                                            [LongProperty] => Array
                                                (
                                                    [0] => stdClass Object
                                                        (
                                                            [name] => TimeDown
                                                            [value] => 0
                                                        )
        
                                                    [1] => stdClass Object
                                                        (
                                                            [name] => TimeUnreachable
                                                            [value] => 0
                                                        )
        
                                                    [2] => stdClass Object
                                                        (
                                                            [name] => TimeUp
                                                            [value] => 1176920634
                                                        )
        
                                                )
        
                                            [DoubleProperty] => Array
                                                (
                                                    [0] => stdClass Object
                                                        (
                                                            [name] => ExecutionTime
                                                            [value] => 0
                                                        )
        
                                                    [1] => stdClass Object
                                                        (
                                                            [name] => Latency
                                                            [value] => 0
                                                        )
        
                                                    [2] => stdClass Object
                                                        (
                                                            [name] => PercentStateChange
                                                            [value] => 0
                                                        )
        
                                                )
        
                                            [BooleanProperty] => Array
                                                (
                                                    [0] => stdClass Object
                                                        (
                                                            [name] => isAcknowledged
                                                            [value] =>
                                                        )
        
                                                    [1] => stdClass Object
                                                        (
                                                            [name] => isChecksEnabled
                                                            [value] => 1
                                                        )
        
                                                    [2] => stdClass Object
                                                        (
                                                            [name] => isEventHandlersEnabled
                                                            [value] => 1
                                                        )
        
                                                    [3] => stdClass Object
                                                        (
                                                            [name] => isFailurePredictionEnabled
                                                            [value] => 1
                                                        )
        
                                                    [4] => stdClass Object
                                                        (
                                                            [name] => isFlapDetectionEnabled
                                                            [value] => 1
                                                        )
        
                                                    [5] => stdClass Object
                                                        (
                                                            [name] => isHostFlapping
                                                            [value] =>
                                                        )
        
                                                    [6] => stdClass Object
                                                        (
                                                            [name] => isNotificationsEnabled
                                                            [value] => 1
                                                        )
        
                                                    [7] => stdClass Object
                                                        (
                                                            [name] => isPassiveChecksEnabled
                                                            [value] => 1
                                                        )
        
                                                    [8] => stdClass Object
                                                        (
                                                            [name] => isProcessPerformanceData
                                                            [value] => 1
                                                        )
        
                                                )
        
                                            [DateProperty] => stdClass Object
                                                (
                                                    [name] => LastStateChange
                                                    [value] => 2007-04-11T19:01:50.000Z
                                                )
        
                                        )
        
                                )
        
                            [MonitorStatus] => stdClass Object
                                (
                                    [MonitorStatusID] => 1
                                    [Name] => OK
                                    [Description] => Status OK
                                )
        
                            [LastCheckTime] => 2007-04-18T23:34:20.000Z
                            [NextCheckTime] => 2007-04-18T23:39:20.000Z
                            [MetricType] =>
                            [Domain] =>
                            [StateType] => stdClass Object
                                (
                                    [StateTypeID] => 2
                                    [Name] => HARD
                                    [Description] => State Hard
                                )
        
                            [CheckType] => stdClass Object
                                (
                                    [CheckTypeID] => 1
                                    [Name] => ACTIVE
                                    [Description] => Active Check
                                )
        
                            [LastHardState] => stdClass Object
                                (
                                    [MonitorStatusID] => 1
                                    [Name] => OK
                                    [Description] => Status OK
                                )
        
                            [LastStateChange] => 2007-04-18T18:28:44.000Z
                            [LastPluginOutput] => OK - load average: 1.76, 1.42, 1.49
                            [CurrentNotificationNumber] => 0
                            [RetryNumber] => 1
                            [ScheduledDowntimeDepth] => 0
                            [TimeCritical] => 1176920320
                            [TimeOK] => 1176939260
                            [TimeUnknown] => 0
                            [TimeWarning] => 1176920624
                            [ExecutionTime] => 50
                            [Latency] => 532
                            [PercentStateChange] => 0
                            [isAcceptPassiveChecks] => 1
                            [isChecksEnabled] => 1
                            [isEventHandlersEnabled] => 1
                            [isFailurePredictionEnabled] => 1
                            [isFlapDetectionEnabled] => 1
                            [isNotificationsEnabled] => 1
                            [isObsessOverService] => 1
                            [isProblemAcknowledged] =>
                            [isProcessPerformanceData] => 1
                            [isServiceFlapping] =>
                        )
                )
        )
   * For a single service, data is returned in the same way except that the service is returned as an Object
   * rather than an array.  The Object contains a count as TotalCount (which is always 1) and then the 
   * ServiceStatus object as above.
        stdClass Object
        (
            [TotalCount] => 1
            [ServiceStatus] => stdClass Object
                (
                    [ServiceStatusID] => 22
                    [ApplicationTypeID] => 100
                    [Description] => ssh_disk_root
                    [Host] => stdClass Object
        ...
                )
        
        )

 * @package DAL
 *
 */
class ServiceDAL {
	static private $dataServiceFactory;
	static private $foundationURL;
		
    /**
     * create an instance of a ServiceDAL
     *
     * @param string $foundationURL - MUST be provided
     */
	public function __construct($foundationURL) 
	{
	    if ($foundationURL == null)
	       throw new DALException("A path to the Foundation Webservice must be provided");
	    $this->foundationURL = $foundationURL;
	}
	
    /**
     * Get all the services
     * @return list of services
     */
	public function getServices() {
		try {
			if (ServiceDAL::$dataServiceFactory == null) {
					ServiceDAL::$dataServiceFactory = @DAOFactory::getDAO($this->foundationURL, DAOFactory::FOUNDATIONWS_SERVICE);
			}
			return @ServiceDAL::$dataServiceFactory->getServices();
		}
		catch(SoapFault $exception) {
			throw new DALException("An error occurred while connecting to the foundation webservice.".$exception->getMessage());
		}
		catch(Exception $e) {
			throw new DALException("An unexpected error occurred.");
		}
	}
	
    /**
     * Get services with the description provided
     *
     * @param string $serviceDescription
     * @return service 
     */
	public function getServicesByDescription($serviceDescription) {
	    if ($serviceDescription == null)
	       throw new DALException("A service description must be provided");
		try {
			if (ServiceDAL::$dataServiceFactory == null) {
					ServiceDAL::$dataServiceFactory = @DAOFactory::getDAO($this->foundationURL, DAOFactory::FOUNDATIONWS_SERVICE);
			}
			return @ServiceDAL::$dataServiceFactory->getServicesByDescription($serviceDescription);
		}
		catch(SoapFault $exception) {
			throw new DALException("An error occurred while connecting to the foundation webservice.".$exception->getMessage());
		}
		catch(Exception $e) {
			throw new DALException("An unexpected error occurred.");
		}
	}
	
    /**
     * Get services with the description provided, associated with host provided
     *
     * @param string $serviceDescription Description field of service
     * @param string $hostName Host that service is associated with
     * @return service
     */
	public function getService($serviceDescription, $hostName) {
	    if ($serviceDescription == null || $hostName == null)
	       throw new DALException("A service description and host name must be provided.");
		try {
			if (ServiceDAL::$dataServiceFactory == null) {
					ServiceDAL::$dataServiceFactory = @DAOFactory::getDAO($this->foundationURL, DAOFactory::FOUNDATIONWS_SERVICE);
			}
			return @ServiceDAL::$dataServiceFactory->getService($serviceDescription, $hostName);
		}
		catch(SoapFault $exception) {
			throw new DALException("An error occurred while connecting to the foundation webservice.".$exception->getMessage());
		}
		catch(Exception $e) {
			throw new DALException("An unexpected error occurred.");
		}
	}
	
    /**
     * get service with the id provided
     *
     * @param integer $serviceId
     * @return service
     */
	public function getServiceById($serviceId) {
	    if ($serviceId == null || $serviceId == 0)
            throw new DALException("A service ID must be provided.");
	    
	    try {
			if (ServiceDAL::$dataServiceFactory == null) {
					ServiceDAL::$dataServiceFactory = @DAOFactory::getDAO($this->foundationURL, DAOFactory::FOUNDATIONWS_SERVICE);
			}
			return @ServiceDAL::$dataServiceFactory->getServiceById($serviceId);
		}
		catch(SoapFault $exception) {
			throw new DALException("An error occurred while connecting to the foundation webservice.".$exception->getMessage());
		}
		catch(Exception $e) {
			throw new DALException("An unexpected error occurred.");
		}
	}
	
    /**
     * get services associated with the host specified by hostId
     *
     * @param integer $hostId
     * @return list of services
     */
	public function getServicesByHostId($hostId) {
	    if ($hostId == null || $hostId ==0)
	       throw new DALException("A host ID must be provided");
		try {
			if (ServiceDAL::$dataServiceFactory == null) {
					ServiceDAL::$dataServiceFactory = @DAOFactory::getDAO($this->foundationURL, DAOFactory::FOUNDATIONWS_SERVICE);
			}
			return @ServiceDAL::$dataServiceFactory->getServicesByHostId($hostId);
		}
		catch(SoapFault $exception) {
			throw new DALException("An error occurred while connecting to the foundation webservice.".$exception->getMessage());
		}
		catch(Exception $e) {
			throw new DALException("An unexpected error occurred.");
		}
	}
	
    /**
     * get services associated with the host specified by hostName
     *
     * @param string $hostName
     * @return list of services
     */
	public function getServicesByHostName($hostName, $sort=null, $firstResult=-1, $maxResults=-1) {
	    if ($hostName == null)
	       throw new DALException("A host name must be provided.");
		try {
			if (ServiceDAL::$dataServiceFactory == null) {
					ServiceDAL::$dataServiceFactory = @DAOFactory::getDAO($this->foundationURL, DAOFactory::FOUNDATIONWS_SERVICE);
			}
			return @ServiceDAL::$dataServiceFactory->getServicesByHostName($hostName, $sort, $firstResult, $maxResults);
		}
		catch(SoapFault $exception) {
			throw new DALException("An error occurred while connecting to the foundation webservice.".$exception->getMessage());
		}
		catch(Exception $e) {
			throw new DALException("An unexpected error occurred.");
		}
	}
	
    /**
     * Get the services for the specified hostgroup
     * 
     * @todo should services returned by hostgroup be broken down by host?
     *
     * @param string $hostGroupName Name of the HostGroup to get services for
     * @return list of services
     */
	public function getServicesByHostGroupName($hostGroupName) {
	    if ($hostGroupName == null)
	       throw new DALException("A HostGroup name must be provided");
		try {
			if (ServiceDAL::$dataServiceFactory == null) {
					ServiceDAL::$dataServiceFactory = @DAOFactory::getDAO($this->foundationURL, DAOFactory::FOUNDATIONWS_SERVICE);
			}
			return @ServiceDAL::$dataServiceFactory->getServicesByHostGroupName($hostGroupName);
		}
		catch(SoapFault $exception) {
			throw new DALException("An error occurred while connecting to the foundation webservice.".$exception->getMessage());
		}
		catch(Exception $e) {
			throw new DALException("An unexpected error occurred.");
		}
	}
	
    /**
     * Get the services for the specified hostgroup
     * 
     * @todo should services returned by hostgroup be broken down by host?
     *
     * @param integer $hostGroupId ID of hostgroup to get services for
     * @return list of services
     */
	public function getServicesByHostGroupId($hostGroupId) {
	    if ($hostGroupId == null || $hostGroupId == 0)
	       throw new DALException("A HostGroup ID must be provided");
		try {
			if (ServiceDAL::$dataServiceFactory == null) {
					ServiceDAL::$dataServiceFactory = @DAOFactory::getDAO($this->foundationURL, DAOFactory::FOUNDATIONWS_SERVICE);
			}
			return @ServiceDAL::$dataServiceFactory->getServicesByHostGroupId($hostGroupId);
		}
		catch(SoapFault $exception) {
			throw new DALException("An error occurred while connecting to the foundation webservice.".$exception->getMessage());
		}
		catch(Exception $e) {
			throw new DALException("An unexpected error occurred.");
		}
	}
	
    /**
     * Get all troubled services - Services with a monitor status which is not OK.
     * Use $firstResult and $maxResults to constrain the number of services returned.
     *
     * @param $firstResult Number of service to start with - Used for pagination
     * @param $maxResults Total number of services to return based on $firstResult
     * @return list of services
     */
    public function getTroubledServices($firstResult=-1, $maxResults=-1)
	{
		try {
			if (ServiceDAL::$dataServiceFactory == null) {
				ServiceDAL::$dataServiceFactory = @DAOFactory::getDAO($this->foundationURL, DAOFactory::FOUNDATIONWS_SERVICE);
			}
			return @ServiceDAL::$dataServiceFactory->getTroubledServices($firstResult, $maxResults);
		}
		catch(SoapFault $exception) {
			throw new DALException("An error occurred while connecting to the foundation webservice.".$exception->getMessage());
		}
		catch(Exception $e) {
			throw new DALException("An unexpected error occurred.");
		}
	}
	
    /**
     * Retrieves all services specified in service id array.  If a service is not found then it is just not returned and 
     * no exception is thrown.  
     *
     * @param list $serviceIds List of ids to get detailed information for
     * @param list $sort List of fields to sort results on
     * @param integer $firstResult Number of the first service to return - use of pagination
     * @param integer $maxResults Max number of services to return starting with $firstResult
     * @return list of services
     */
    public function getServicesByIds($serviceIds, $sort, $firstResult, $maxResults)
	{
		try {
		    if (ServiceDAL::$dataServiceFactory == null) {
		        ServiceDAL::$dataServiceFactory = @DAOFactory::getDAO($this->foundationURL, DAOFactory::FOUNDATIONWS_SERVICE);
			}
			return @ServiceDAL::$dataServiceFactory->getServicesByIds($serviceIds, $sort, $firstResult, $maxResults);
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
     * @return list
     */
    public function getServicesByFilter($filter=null, $sort=null, $firstResult=-1, $maxResults=-1)
	{
		try {
		    if (ServiceDAL::$dataServiceFactory == null) {
		        ServiceDAL::$dataServiceFactory = @DAOFactory::getDAO($this->foundationURL, DAOFactory::FOUNDATIONWS_SERVICE);
			}
			return @ServiceDAL::$dataServiceFactory->getServicesByFilter($filter, $sort, $firstResult, $maxResults);
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