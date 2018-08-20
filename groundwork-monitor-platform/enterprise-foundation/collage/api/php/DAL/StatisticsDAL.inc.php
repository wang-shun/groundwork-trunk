<?php
/**
 * @author Robin Dandridge
 * @package DAL
 * @version 1.0
 */

/**
 * include required files.
 */
require_once('DAL/DAOFactory.inc.php');
require_once('DAL/DALException.inc.php');

/**
 * Allows the user to retrieve various statistics on Hosts, Services and HostGroups.  General
 * statistics can be retrieved such as counts of each of these entities in each state 
 * (UP, DOWN, CRITICAL, etc.).  Statistics specifically related to Nagios can also be 
 * retrieved.
 * 
 * @package DAL
 *
 */
class StatisticsDAL {
	static private $dataStatisticFactory;
	static private $foundationURL;
		
    /**
     * Create a StatisticDAL instance
     *
     * @param string $foundationURL - may not be null
     */
	public function __construct($foundationURL) 
	{
	    if ($foundationURL == null)
	       throw new DALException("A path to the Foundation Webservice must be provided");
	    $this->foundationURL = $foundationURL;
	}
	
    /**
     * returns statistics for all hosts - not broken down by hostgroup.
     * data returned is in the following format:
        Array
        (
            [Name] => _ALL_
            [TotalServices] => 550
            [TotalHosts] => 252
            [DOWN] => 124
            [UNREACHABLE] => 0
            [PENDING] => 21
            [UP] => 107
        )
     * @return statistic data for hosts
     */
	public function getAllHostStatisticData() {
		try {
			if (StatisticsDAL::$dataStatisticFactory == null) {
					StatisticsDAL::$dataStatisticFactory = @DAOFactory::getDAO($this->foundationURL, DAOFactory::FOUNDATIONWS_STATISTIC);
			}
			return @StatisticsDAL::$dataStatisticFactory->getAllHostStatistics();
		}
		catch(SoapFault $exception) {
			throw new DALException("An error occurred while connecting to the foundation webservice.".$exception->getMessage());
		}
		catch(Exception $e) {
			throw new DALException("An unexpected error occurred.");
		}
	}
	
    /**
     * returns statistics for all services - not broken down by host or hostgroup
     * data is returned in the following format:
        Array
        (
            [Name] => _ALL_
            [TotalServices] => 550
            [TotalHosts] => 252
            [CRITICAL] => 175
            [WARNING] => 4
            [UNKNOWN] => 273
            [OK] => 98
            [PENDING] => 0
        )
     * @return statistic data for services
     */
	public function getAllServiceStatisticData() {
		try {
			if (StatisticsDAL::$dataStatisticFactory == null) {
					StatisticsDAL::$dataStatisticFactory = @DAOFactory::getDAO($this->foundationURL, DAOFactory::FOUNDATIONWS_STATISTIC);
			}
			return @StatisticsDAL::$dataStatisticFactory->getAllServiceStatistics();
		}
		catch(SoapFault $exception) {
			throw new DALException("An error occurred while connecting to the foundation webservice.".$exception->getMessage());
		}
		catch(Exception $e) {
			throw new DALException("An unexpected error occurred.");
		}
	}
	
	/**
	 * returns statistics for all Hostgroups
	 * Data is returned in the following format
        Array
        (
            [TestPing] => Array
                (
                    [TotalServices] => 0
                    [TotalHosts] => 7
                    [DOWN] => 0
                    [UNREACHABLE] => 0
                    [PENDING] => 7
                    [UP] => 0
                )
        
            [Engineering] => Array
                (
                    [TotalServices] => 98
                    [TotalHosts] => 18
                    [DOWN] => 7
                    [UNREACHABLE] => 0
                    [PENDING] => 0
                    [UP] => 11
                )
        
            [TestUnix] => Array
                (
                    [TotalServices] => 6
                    [TotalHosts] => 15
                    [DOWN] => 0
                    [UNREACHABLE] => 0
                    [PENDING] => 14
                    [UP] => 1
                )
        )

	 * @return statistic data for hostgroups
	 */
	public function getAllHostGroupStatistics() 
	{
		try {
			if (StatisticsDAL::$dataStatisticFactory == null) {
					StatisticsDAL::$dataStatisticFactory = @DAOFactory::getDAO($this->foundationURL, DAOFactory::FOUNDATIONWS_STATISTIC);
			}
			return @StatisticsDAL::$dataStatisticFactory->getHostGroupStatistics();
		}
		catch(SoapFault $exception) {
			throw new DALException("An error occurred while connecting to the foundation webservice.".$exception->getMessage());
		}
		catch(Exception $e) {
			throw new DALException("An unexpected error occurred.");
		}	    
	}
	
    /**
     * gets host statistic data for a specific hostgroup
     * data is returned in the following format:
        Array
        (
            [Name] => google
            [TotalServices] => 151
            [TotalHosts] => 151
            [DOWN] => 110
            [UNREACHABLE] => 0
            [PENDING] => 0
            [UP] => 41
        )
     * @param integer $hostGroupID
     * @return statistic data for hosts in the hostgroup
     */
	public function getHostStatisticsByHostGroupID($hostGroupID) {
	    if ($hostGroupID == null || $hostGroupID == 0)
	       throw new DALException("A hostgroup id must be provided");
		try {
			if (StatisticsDAL::$dataStatisticFactory == null) {
				StatisticsDAL::$dataStatisticFactory = @DAOFactory::getDAO($this->foundationURL, DAOFactory::FOUNDATIONWS_STATISTIC);
			}
			return @StatisticsDAL::$dataStatisticFactory->getHostGroupHostStatistics($hostGroupID);
		}
		catch(SoapFault $exception) {
			throw new DALException("An error occurred while connecting to the foundation webservice.".$exception->getMessage);
		}
		catch(Exception $e) {
			throw new DALException("An unexpected error occurred.");
		}
	}
	
    /**
     * gets statistic data for hosts in a specific hostgroup
     * data is returned in the following format:
        Array
        (
            [Name] => google
            [TotalServices] => 151
            [TotalHosts] => 151
            [DOWN] => 110
            [UNREACHABLE] => 0
            [PENDING] => 0
            [UP] => 41
        )
     *
     * @param  string $hostGroupName
     * @return statistic data for hosts in hostgroup
     */
	public function getHostStatisticsByHostGroupName($hostGroupName) {
	    if ($hostGroupName == null)
	       throw new DALException("A hostgroup name must be provided");
		try {
			if (StatisticsDAL::$dataStatisticFactory == null) {
				StatisticsDAL::$dataStatisticFactory = @DAOFactory::getDAO($this->foundationURL, DAOFactory::FOUNDATIONWS_STATISTIC);
			}
			return @StatisticsDAL::$dataStatisticFactory->getHostGroupHostStatisticsByName($hostGroupName);
		}
		catch(SoapFault $exception) {
			throw new DALException("An error occurred while connecting to the foundation webservice.".$exception->getMessage);
		}
		catch(Exception $e) {
			throw new DALException("An unexpected error occurred.");
		}
	}
	
    /**
     * gets statistic data for services in a specific hostgroup
     * data is returned in the following format:
        Array
        (
            [HostGroupName] => Engineering
            [TotalServices] => 98
            [TotalHosts] => 18
            [CRITICAL] => 40
            [WARNING] => 0
            [UNKNOWN] => 50
            [OK] => 8
            [PENDING] => 0
        )
     *
     * @param integer $hostGroupID
     * @return statistic data for services in a hostgroup
     */
	public function getServiceStatisticsByHostGroupID($hostGroupID) {
	    if ($hostGroupID == null || $hostGroupID == 0)
	       throw new DALException("A hostgroup id must be provided.");
		try {
			if (StatisticsDAL::$dataStatisticFactory == null) {
				StatisticsDAL::$dataStatisticFactory = @DAOFactory::getDAO($this->foundationURL, DAOFactory::FOUNDATIONWS_STATISTIC);
			}
			return @StatisticsDAL::$dataStatisticFactory->getHostGroupServiceStatistics($hostGroupID);			
		}
		catch(SoapFault $exception) {
			throw new DALException("An error occurred while connecting to the foundation webservice: ".$exception->getMessage());
		}
		catch(Exception $e) {
			throw new DALException("An unexpected error occurred: ".$e->getMessage());
		}
	}
	
    /**
     * gets statistic data for services in a specific hostgroup
     *
     * data is returned in the following format:
        Array
        (
            [HostGroupName] => Engineering
            [TotalServices] => 98
            [TotalHosts] => 18
            [CRITICAL] => 40
            [WARNING] => 0
            [UNKNOWN] => 50
            [OK] => 8
            [PENDING] => 0
        )
     * @param string $hostGroupName
     * @return statistic data for services in a hostgroup
     */
	public function getServiceStatisticsByHostGroupName($hostGroupName) {
	    if ($hostGroupName == null)
	       throw new DALException("A hostgroup name must be provided");
		try {
			if (StatisticsDAL::$dataStatisticFactory == null) {
				StatisticsDAL::$dataStatisticFactory = @DAOFactory::getDAO($this->foundationURL, DAOFactory::FOUNDATIONWS_STATISTIC);
			}
			return @StatisticsDAL::$dataStatisticFactory->getHostGroupServiceStatisticsByName($hostGroupName);
		}
		catch(SoapFault $exception) {
			throw new DALException("An error occurred while connecting to the foundation webservice: ".$exception->getMessage());
		}
		catch(Exception $e) {
			throw new DALException("An unexpected error occurred: ".$e->getMessage());
		}
	}
	
    /**
     * gets service statistics for a host
     * data is returned in the following format:
        Array
        (
            [Name] => 172.28.113.238
            [TotalServices] => 6
            [TotalHosts] => 1
            [CRITICAL] => 1
            [WARNING] => 0
            [UNKNOWN] => 5
            [OK] => 0
            [PENDING] => 0
        )
     * @param string $hostName
     * @return statistic data for services associated with a host
     */
	public function getServiceStatisticsByHostName($hostName) 
	{
	    if ($hostName == null)
	       throw new DALException("A host name must be provided");
		try {
			if (StatisticsDAL::$dataStatisticFactory == null) {
				StatisticsDAL::$dataStatisticFactory = @DAOFactory::getDAO($this->foundationURL, DAOFactory::FOUNDATIONWS_STATISTIC);
			}
			return @StatisticsDAL::$dataStatisticFactory->getHostServiceStatistics($hostName);			
		}
		catch(SoapFault $exception) {
			throw new DALException("An error occurred while connecting to the foundation webservice: ".$exception->getMessage());
		}
		catch(Exception $e) {
			throw new DALException("An unexpected error occurred: ".$e->getMessage());
		}	    
	}
	
    /**
     * gets statistics specific to Nagios for a hostgroup
     * data is returned in the following format:
        Array
        (
            [PassiveChecks] => Array
                (
                    [HostsEnabled] => 18
                    [HostsDisabled] => 0
                    [ServicesEnabled] => 98
                    [ServicesDisabled] => 0
                )
        
            [isChecksEnabled] => Array
                (
                    [HostsEnabled] => 18
                    [HostsDisabled] => 0
                    [ServicesEnabled] => 98
                    [ServicesDisabled] => 0
                )
        
            [ScheduledDowntimeDepth] => Array
                (
                    [HostsEnabled] => 0
                    [HostsDisabled] => 18
                    [ServicesEnabled] => 0
                    [ServicesDisabled] => 98
                )
        
            [Acknowledged] => Array
                (
                    [HostsEnabled] => 0
                    [HostsDisabled] => 7
                    [ServicesEnabled] => 0
                    [ServicesDisabled] => 90
                )
        
            [isNotificationsEnabled] => Array
                (
                    [HostsEnabled] => 18
                    [HostsDisabled] => 0
                    [ServicesEnabled] => 98
                    [ServicesDisabled] => 0
                )
        
            [isEventHandlersEnabled] => Array
                (
                    [HostsEnabled] => 18
                    [HostsDisabled] => 0
                    [ServicesEnabled] => 98
                    [ServicesDisabled] => 0
                )
        
        )
     * @param integer $hostGroupID
     * @return nagios specific statistics for a hostgroup
     */
	public function getNagiosStatisticsByHostGroupID($hostGroupID) {
	    if ($hostGroupID == null || $hostGroupID ==0)
	       throw new DALException("A HostGroup ID must be provided");
		try {
			if (StatisticsDAL::$dataStatisticFactory == null) {
					StatisticsDAL::$dataStatisticFactory = @DAOFactory::getDAO($this->foundationURL, DAOFactory::FOUNDATIONWS_STATISTIC);
			}
			return @StatisticsDAL::$dataStatisticFactory->getHostGroupNagiosStatistics($hostGroupID);
		}
		catch(SoapFault $exception) {
			throw new DALException("An error occurred while connecting to the webservice.".$exception->getMessage());
		}
		catch(Exception $e) {
			throw new DALException("An unexpected error occurred.".$e->getMessage());
		}		
	}
	
    /**
     * gets statistics specific to Nagios for a hostgroup
     * data is returned in the following format:
        Array
        (
            [PassiveChecks] => Array
                (
                    [HostsEnabled] => 18
                    [HostsDisabled] => 0
                    [ServicesEnabled] => 98
                    [ServicesDisabled] => 0
                )
        
            [isChecksEnabled] => Array
                (
                    [HostsEnabled] => 18
                    [HostsDisabled] => 0
                    [ServicesEnabled] => 98
                    [ServicesDisabled] => 0
                )
        
            [ScheduledDowntimeDepth] => Array
                (
                    [HostsEnabled] => 0
                    [HostsDisabled] => 18
                    [ServicesEnabled] => 0
                    [ServicesDisabled] => 98
                )
        
            [Acknowledged] => Array
                (
                    [HostsEnabled] => 0
                    [HostsDisabled] => 7
                    [ServicesEnabled] => 0
                    [ServicesDisabled] => 90
                )
        
            [isNotificationsEnabled] => Array
                (
                    [HostsEnabled] => 18
                    [HostsDisabled] => 0
                    [ServicesEnabled] => 98
                    [ServicesDisabled] => 0
                )
        
            [isEventHandlersEnabled] => Array
                (
                    [HostsEnabled] => 18
                    [HostsDisabled] => 0
                    [ServicesEnabled] => 98
                    [ServicesDisabled] => 0
                )
        
        )
     *
     * @param string $hostGroupName
     * @return nagios statistics for a hostgroup
     */
	public function getNagiosStatisticsByHostGroupName($hostGroupName) {
	    if ($hostGroupName == null)
	       throw new DALException("A HostGroup Name must be provided");
		try {
			if (StatisticsDAL::$dataStatisticFactory == null) {
					StatisticsDAL::$dataStatisticFactory = @DAOFactory::getDAO($this->foundationURL, DAOFactory::FOUNDATIONWS_STATISTIC);
			}
			return @StatisticsDAL::$dataStatisticFactory->getHostGroupNagiosStatisticsByName($hostGroupName);
		}
		catch(SoapFault $exception) {
			throw new DALException("An error occurred while connecting to the webservice.".$exception->getMessage());
		}
		catch(Exception $e) {
			throw new DALException("An unexpected error occurred.".$e->getMessage());
		}		
	}
	
    /**
     * gets statistics specific to Nagios for a host
     * Data is returned in the following format:
        Array
        (
            [PassiveChecks] => Array
                (
                    [ServicesEnabled] => 6
                    [ServicesDisabled] => 0
                )
        
            [isChecksEnabled] => Array
                (
                    [ServicesEnabled] => 6
                    [ServicesDisabled] => 0
                )
        
            [ScheduledDowntimeDepth] => Array
                (
                    [ServicesEnabled] => 0
                    [ServicesDisabled] => 6
                )
        
            [Acknowledged] => Array
                (
                    [ServicesEnabled] => 0
                    [ServicesDisabled] => 6
                )
        
            [isNotificationsEnabled] => Array
                (
                    [ServicesEnabled] => 6
                    [ServicesDisabled] => 0
                )
        
            [isEventHandlersEnabled] => Array
                (
                    [ServicesEnabled] => 6
                    [ServicesDisabled] => 0
                )
        
        )
     * @param string $hostName
     * @return statistics for the specified host
     */
	public function getNagiosStatisticsByHostName($hostName) {
	    if ($hostName == null)
	       throw new DALException("A host name must be provided");
		try {
			if (StatisticsDAL::$dataStatisticFactory == null) {
					StatisticsDAL::$dataStatisticFactory = @DAOFactory::getDAO($this->foundationURL, DAOFactory::FOUNDATIONWS_STATISTIC);
			}
			return @StatisticsDAL::$dataStatisticFactory->getHostNagiosStatistics($hostName);
		}
		catch(SoapFault $exception) {
			throw new DALException("An error occurred while connecting to the webservice.".$exception->getMessage());
		}
		catch(Exception $e) {
			throw new DALException("An unexpected error occurred.".$e->getMessage());
		}		
	}
	
    /**
     * gets statistics specific to nagios
     * Data is returned in the following format:
        Array
        (
            [PassiveChecks] => Array
                (
                    [HostsEnabled] => 252
                    [HostsDisabled] => 0
                    [ServicesEnabled] => 550
                    [ServicesDisabled] => 0
                )
        
            [isChecksEnabled] => Array
                (
                    [HostsEnabled] => 252
                    [HostsDisabled] => 0
                    [ServicesEnabled] => 550
                    [ServicesDisabled] => 0
                )
        
            [ScheduledDowntimeDepth] => Array
                (
                    [HostsEnabled] => 0
                    [HostsDisabled] => 252
                    [ServicesEnabled] => 0
                    [ServicesDisabled] => 550
                )
        
            [Acknowledged] => Array
                (
                    [HostsEnabled] => 0
                    [HostsDisabled] => 145
                    [ServicesEnabled] => 0
                    [ServicesDisabled] => 452
                )
        
            [isNotificationsEnabled] => Array
                (
                    [HostsEnabled] => 252
                    [HostsDisabled] => 0
                    [ServicesEnabled] => 550
                    [ServicesDisabled] => 0
                )
        
            [isEventHandlersEnabled] => Array
                (
                    [HostsEnabled] => 252
                    [HostsDisabled] => 0
                    [ServicesEnabled] => 550
                    [ServicesDisabled] => 0
                )
        
        )
     * @return a list of statistics
     */
	public function getAllNagiosStatisticData() {
		try {
			if (StatisticsDAL::$dataStatisticFactory == null) {
					StatisticsDAL::$dataStatisticFactory = @DAOFactory::getDAO($this->foundationURL, DAOFactory::FOUNDATIONWS_STATISTIC);
			}
			return @StatisticsDAL::$dataStatisticFactory->getAllNagiosStatistics();
		}
		catch(SoapFault $exception) {
			throw new DALException("An error occurred while connecting to the webservice.".$exception->getMessage());
		}
		catch(Exception $e) {
			throw new DALException("An unexpected error occurred.".$e->getMessage());
		}		
	}
	
    /**
     * gets counts for hosts broken down by hostgroup
     * Data is returned in the following format:
        Array
        (
            [UP] => 9
            [DOWN] => 6
            [PENDING] => 3
            [UNREACHABLE] => 0
        )
     * This is interpreted as 9 Hostgroups have hosts that are UP, 6 Hostgroups have hosts that are DOWN,
     * 3 Hostgroups have hosts that are PENDING, and 0 Hostgroups have hosts that are UNREACHABLE
     * 
     * @return statistics for hosts by hostgroup
     */
	public function getHostGroupHostStatisticData() {
		try {
			if (StatisticsDAL::$dataStatisticFactory == null) {
					StatisticsDAL::$dataStatisticFactory = @DAOFactory::getDAO($this->foundationURL, DAOFactory::FOUNDATIONWS_STATISTIC);
			}
			return @StatisticsDAL::$dataStatisticFactory->getHostGroupCountsByHost();
		}
		catch(SoapFault $exception) {
			throw new DALException("An error occurred while connecting to the foundation webservice.".$exception->getMessage());
		}
		catch(Exception $e) {
			throw new DALException("An unexpected error occurred.");
		}
	}
	
    /**
     * gets counts for services broken down by hostgroup
     * Data is returned in the following format:
        Array
        (
            [OK] => 9
            [UNKNOWN] => 6
            [CRITICAL] => 6
            [WARNING] => 3
            [PENDING] => 0
        )
     * This is interpreted as 9 Hostgroups have services that are OK, 6 Hostgroups have services that are UNKNOWN,
     * 6 Hostgroups have services that are CRITICAL, 3 Hostgroups have services that are WARNING, 
     * and 0 Hostgroups have hosts that are PENDING

     * @return statistics for services by hostgroup
     */
	public function getHostGroupServiceStatisticData() {
		try {
			if (StatisticsDAL::$dataStatisticFactory == null) {
					StatisticsDAL::$dataStatisticFactory = @DAOFactory::getDAO($this->foundationURL, DAOFactory::FOUNDATIONWS_STATISTIC);
			}
			return @StatisticsDAL::$dataStatisticFactory->getHostGroupCountsByService();
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
