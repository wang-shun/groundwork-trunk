<?php

/**
 * @author Robin Dandridge
 * @package DAL
 * @version 1.0
 *
 */

/**
 * Include necessary files
 */
require_once('DAL/DAOFactory.inc.php');
require_once('DAL/DALException.inc.php');

/**
 * The EventDAL is used to get events, or messages that have been stored in the
 * Foundation Database.
 * Events are returned in the following format:
    Array
    (
        [Count] => 583
        [Messages] => Array
            (
                [0] => Array
                    (
                        [LogMessageID] => 584
                        [ApplicationName] => NAGIOS
                        [TextMessage] => CRITICAL - 172.28.113.54: Host unreachable @ 172.28.113.227. rta nan, lost 100%
                        [MessageCount] => 1
                        [FirstInsertDate] => 1175081596
                        [LastInsertDate] => 1175081594
                        [ReportDate] => 1175081596
                        [Device] => stdClass Object
                            (
                                [DeviceID] => 17
                                [Name] => 172.28.113.54
                                [Identification] => 172.28.113.54
                            )
    
                        [MonitorStatus] => stdClass Object
                            (
                                [MonitorStatusID] => 2
                                [Name] => DOWN
                                [Description] => Status DOWN
                            )
    
                        [Severity] => stdClass Object
                            (
                                [SeverityID] => 8
                                [Name] => CRITICAL
                                [Description] => GroundWork Severity CRITICAL. Also MIB standard
                            )
    
                        [Host] => stdClass Object
                            (
                                [HostID] => 16
                                [ApplicationTypeID] => 100
                                [Device] => stdClass Object
                                    (
                                        [DeviceID] => 17
                                        [Name] => 172.28.113.54
                                        [Identification] => 172.28.113.54
                                    )
    
                                [Name] => 172.28.113.54
                                [MonitorStatus] => stdClass Object
                                    (
                                        [MonitorStatusID] => 2
                                        [Name] => DOWN
                                        [Description] => Status DOWN
                                    )
    
                                [LastCheckTime] => 2007-03-28T11:56:29.000Z
                                [PropertyTypeBinding] => stdClass Object
                                    (
                                        [StringProperty] => stdClass Object
                                            (
                                                [name] => LastPluginOutput
                                                [value] => CRITICAL - 172.28.113.54: Host unreachable @ 172.28.113.227. rta nan, lost 100%
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
                                                        [value] => 1175081594
                                                    )
    
                                                [1] => stdClass Object
                                                    (
                                                        [name] => TimeUnreachable
                                                        [value] => 0
                                                    )
    
                                                [2] => stdClass Object
                                                    (
                                                        [name] => TimeUp
                                                        [value] => 1175081195
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
                                                        [value] => 10.79
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
                                                [value] => 2007-03-28T11:33:14.000Z
                                            )
    
                                    )
    
                            )
    
                        [Properties] => Array
                            (
                                [ErrorType] => HOST ALERT
                                [SubComponent] => 172.28.113.54
                            )
    
                    )  
           )
    )
 * @package DAL
 *
 */
class EventDAL {
	static private $dataEventFactory;
	static private $foundationURL;
		
    /**
     * create an instance of the EventDAL
     *
     * @param string $foundationURL - path to foundation MUST be provided
     * 
     */
	public function __construct($foundationURL) 
	{
	    if ($foundationURL == null)
	       throw new DALException("A path to the Foundation Webservice must be provided");
	    $this->foundationURL = $foundationURL;
	}
	
    /**
     * gets all "Open" events or events that have not been "Accepted" and 
     * therefore cleared from the console screen.
     *
     * @param integer $appTypeId if provided, allows filtering by application type.  
     * @see MetaDataDAL to get application types defined in the Foundation DB.
     * @param list $sortItems a comma separated list of fields to sort on, if provided.
     * @param integer $firstResult the row to begin retrieving events from
     * @param integer $lastResult the total number of events to retrieve
     * @return A list of open events
     */
	public function getAllOpenEvents($appTypeId=null, $sortItems=null, $firstResult=-1, $lastResult=-1) {
		try {
			if (EventDAL::$dataEventFactory == null) {
					EventDAL::$dataEventFactory = @DAOFactory::getDAO($this->foundationURL, DAOFactory::FOUNDATIONWS_EVENT);
			}
			return @EventDAL::$dataEventFactory->getOpenEvents($appTypeId, $sortItems, $firstResult, $lastResult);
		}
		catch(SoapFault $exception) {
			throw new DALException("An error occurred while connecting to the foundation webservice.".$exception->getMessage());
		}
		catch(Exception $e) {
			throw new DALException("An unexpected error occurred. ".$e->getMessage());
		}
	}
	
	/**
	 * Get all open events for the hostgroup specified by hostgroup id
	 *
	 * @param integer $hostGroupId The Id of the hostgroup to retrieve events for
     * @param integer $appTypeId if provided, allows filtering by application type
     * @param list $sortItems a comma separated list of fields to sort on, if provided.
     * @param integer $firstResult the row to begin retrieving events from
     * @param integer $lastResult the total number of events to retrieve
	 * @return A list of open events for the HostGroup ID
	 */
	public function getOpenEventsByHostGroupId($hostGroupId, $appTypeId=null, $sortItems=null, $firstResult=-1, $lastResult=-1) {
		try {
			if (EventDAL::$dataEventFactory == null) {
					EventDAL::$dataEventFactory = @DAOFactory::getDAO($this->foundationURL, DAOFactory::FOUNDATIONWS_EVENT);
			}
			return @EventDAL::$dataEventFactory->getOpenEventsByHostGroupId($hostGroupId, $appTypeId, $sortItems, $firstResult, $lastResult);
		}
		catch(SoapFault $exception) {
			throw new DALException("An error occurred while connecting to the foundation webservice. ".$exception->getMessage());
		}
		catch(Exception $e) {
			throw new DALException("An unexpected error occurred. ".$e->getMessage());
		}
	}
	
    /**
     * Get all open events for the host specified by hostId
     *
     * @param integer $hostId The Id of the host that this service is associated with
     * @param integer $appTypeId if provided, allows filtering by application type
     * @param list $sortItems a comma separated list of fields to sort on, if provided.
     * @param integer $firstResult the row to begin retrieving events from
     * @param integer $lastResult the total number of events to retrieve
	 * @return A list of Open events for the host specified
     */
	public function getOpenEventsByHostId($hostId, $appTypeId=null, $sortItems=null, $firstResult=-1, $lastResult=-1) {
		try {
			if (EventDAL::$dataEventFactory == null) {
					EventDAL::$dataEventFactory = @DAOFactory::getDAO($this->foundationURL, DAOFactory::FOUNDATIONWS_EVENT);
			}
			return @EventDAL::$dataEventFactory->getOpenEventsByHostId($hostId, $appTypeId, $sortItems, $firstResult, $lastResult);
		}
		catch(SoapFault $exception) {
			throw new DALException("An error occurred while connecting to the foundation webservice. ".$exception->getMessage());
		}
		catch(Exception $e) {
			throw new DALException("An unexpected error occurred. ".$e->getMessage());
		}
	}
	
    /**
     * Get all open events for the service specified by the service Description and hostId
     *
     * @param string $serviceDescription Description of the service
     * @param integer $hostId The Id of the host that this service is associated with
     * @param integer $appTypeId if provided, allows filtering by application type
     * @param list $sortItems a comma separated list of fields to sort on, if provided.
     * @param integer $firstResult the row to begin retrieving events from
     * @param integer $lastResult the total number of events to retrieve
	 * @return A list of Open events
     */
	public function getOpenEventsByServiceDescription($serviceDescription, $hostId, $appTypeId=null, $sortItems=null, $firstResult=-1, $lastResult=-1) {
		try {
			if (EventDAL::$dataEventFactory == null) {
					EventDAL::$dataEventFactory = @DAOFactory::getDAO($this->foundationURL, DAOFactory::FOUNDATIONWS_EVENT);
			}
			return @EventDAL::$dataEventFactory->getOpenEventsByServiceDescription($serviceDescription, $hostId, $appTypeId, $sortItems, $firstResult, $lastResult);
		}
		catch(SoapFault $exception) {
			throw new DALException("An error occurred while connecting to the foundation webservice. ".$exception->getMessage());
		}
		catch(Exception $e) {
			throw new DALException("An unexpected error occurred. ".$e->getMessage());
		}
	}
}
?>