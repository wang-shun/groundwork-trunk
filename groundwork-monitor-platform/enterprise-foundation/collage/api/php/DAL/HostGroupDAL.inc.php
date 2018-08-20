<?php
/**
 * @author Robin Dandridge
 * @package DAL
 * @version 1.0
 */

/**
 * include required files
 */
require_once('DAL/DAOFactory.inc.php');
require_once('DAL/DALException.inc.php');

/**
 * Provides access to HostGroups stored in the Foundation Database.  Both
 * shallow and deep retrieval are provided.  
 * Shallow retrieval returns data in the following format:
Array
(
    [Count] => 4
    [HostGroups] => Array
        (
            [0] => Array
                (
                    [HostGroupID] => 21
                    [Name] => Engineering
                    [Description] =>
                    [ApplicationTypeID] => 100
                    [ApplicationName] => NAGIOS
                )

            [1] => Array
                (
                    [HostGroupID] => 28
                    [Name] => google
                    [Description] =>
                    [ApplicationTypeID] => 100
                    [ApplicationName] => NAGIOS
                )

            [2] => Array
                (
                    [HostGroupID] => 29
                    [Name] => infrastructure
                    [Description] =>
                    [ApplicationTypeID] => 100
                    [ApplicationName] => NAGIOS
                )

            [3] => Array
                (
                    [HostGroupID] => 22
                    [Name] => Linux Servers
                    [Description] =>
                    [ApplicationTypeID] => 100
                    [ApplicationName] => NAGIOS
                )

        )

)
 * Deep retrieval returns data in the following format:
Array
(
    [Count] => 10  <=== this is the TOTAL count of hostgroups in foundation, not how many are being retrieved.
    [HostGroups] => Array
        (
            [0] => Array
                (
                    [HostGroupID] => 22
                    [Name] => Linux Servers
                    [Description] =>
                    [ApplicationTypeID] => 100
                    [ApplicationName] => NAGIOS
                    [Hosts] => Array
                        (
                            [0] => Array
                                (
                                    [HostID] => 1
                                    [Name] => localhost
                                    [ApplicationTypeID] => 100
                                    [Device] => Array
                                        (
                                            [DeviceID] => 1
                                            [Name] => 127.0.0.1
                                            [Identification] => 127.0.0.1
                                        )

                                    [MonitorStatus] => stdClass Object
                                        (
                                            [MonitorStatusID] => 7
                                            [Name] => UP
                                            [Description] => Status UP
                                        )

                                )

                        )

                )

            [1] => Array
                (
                    [HostGroupID] => 23
                    [Name] => Marketing
                    [Description] =>
                    [ApplicationTypeID] => 100
                    [ApplicationName] => NAGIOS
                    [Hosts] => Array
                        (
                            [0] => Array
                                (
                                    [HostID] => 228
                                    [ApplicationTypeID] => 100
                                    [Device] => Array
                                        (
                                            [DeviceID] => 229
                                            [Name] => sales-linux-fs02
                                            [Identification] => 172.28.114.200
                                        )

                                    [Name] => sales-linux-fs02
                                    [MonitorStatus] => stdClass Object
                                        (
                                            [MonitorStatusID] => 7
                                            [Name] => UP
                                            [Description] => Status UP
                                        )

                                )

                            [1] => Array
                                (
                                    [HostID] => 232
                                    [ApplicationTypeID] => 100
                                    [Device] => Array
                                        (
                                            [DeviceID] => 233
                                            [Name] => 172.28.113.147
                                            [Identification] => 172.28.113.147
                                        )

                                    [Name] => 172.28.113.147
                                    [MonitorStatus] => stdClass Object
                                        (
                                            [MonitorStatusID] => 8
                                            [Name] => PENDING
                                            [Description] => Status PENDING
                                        )

                                )
                        )

                )

        )

)


 * @package DAL
 */
class HostGroupDAL {
	static private $dataHostGroupFactory;
	static private $foundationURL;
		
    /**
     * create an instance of the HostGroupDAL
     *
     * @param string $foundationURL - path to foundation MUST be provided
     * 
     */
	public function __construct($foundationURL) 
	{
	    if ($foundationURL == null)
	       throw new DALException("A path to the Foundation Webservice must be provided");
		$this->foundationURL=$foundationURL;
	}
	
    /**
     * Gets all the HostGroups.  Set $deep to true to do a deep retrieval of hostgroups that will include
     * detailed host information in the response.
     *
     * @param boolean $deep
     * @return a list of HostGroups
     */
	public function getHostGroups($deep=false) {
		try {
			if (HostGroupDAL::$dataHostGroupFactory == null) {
				 
					HostGroupDAL::$dataHostGroupFactory = @DAOFactory::getDAO($this->foundationURL, DAOFactory::FOUNDATIONWS_HOSTGROUP);
			}
		 
			return @HostGroupDAL::$dataHostGroupFactory->getHostGroups($deep);
		}
		catch(SoapFault $exception) {
		 
			throw new DALException("An error occurred while connecting to the foundation webservice.".$exception->getMessage());
		}
		catch(Exception $e) {
		 
			throw new DALException("An unexpected error occurred.");
		}
	}
	
    /**
     * gets the hostgroup corresponding to $hostGroupId.  Set $deep to true to do a deep retrieval of 
     * hostgroups that will include detailed host information in the response.
     *
     * @param integer $hostGroupId
     * @param boolean $deep
     * @return A hostgroup
     */
	public function getHostGroupById($hostGroupId,$deep=false) {
		try {
			if (HostGroupDAL::$dataHostGroupFactory == null) {
					HostGroupDAL::$dataHostGroupFactory = @DAOFactory::getDAO($this->foundationURL, DAOFactory::FOUNDATIONWS_HOSTGROUP);
			}
			return @HostGroupDAL::$dataHostGroupFactory->getHostGroupByID($hostGroupId,$deep);
		}
		catch(SoapFault $exception) {
			throw new DALException("An error occurred while connecting to the foundation webservice.".$exception->getMessage());
		}
		catch(Exception $e) {
			throw new DALException("An unexpected error occurred.");
		}
	}
	
    /**
     * gets the hostgroup corresponding to $hostGroupName.  Set $deep to true to do a deep retrieval of 
     * hostgroups that will include detailed host information in the response.
     *
     * @param string $hostGroupName
     * @param boolean $deep
     * @return a hostgroup
     */
	public function getHostGroupByName($hostGroupName,$deep=false) {
		try {
			if (HostGroupDAL::$dataHostGroupFactory == null) {
					HostGroupDAL::$dataHostGroupFactory = @DAOFactory::getDAO($this->foundationURL, DAOFactory::FOUNDATIONWS_HOSTGROUP);
			}
			return @HostGroupDAL::$dataHostGroupFactory->getHostGroupByName($hostGroupName,$deep);
		}
		catch(SoapFault $exception) {
			throw new DALException("An error occurred while connecting to the foundation webservice.".$exception->getMessage());
		}
		catch(Exception $e) {
			throw new DALException("An unexpected error occurred.");
		}
	}
	
    /**
     * gets the hostgroups according the the filter passed in, sorted according to the fields in $sort.  
     * Set $deep to true to do a deep retrieval of hostgroups that will include detailed host information 
     * in the response.  Use $firstResult and $maxResults to constrain the number of hostgroups returned.
     *
     * @param unknown_type $filter
     * @param list $sort - a list of Foundation database fields to sort on.
     * @param integer $firstResult Number of HostGroup to start with - use for pagination
     * @param integer $maxResults Total number of hostgroups to return, starting from $firstResult.
     * @param boolean $deep - deep retrieval or not?
     * @return list of hostgroups
     */
	public function getHostGroupsByFilter($filter=null, $sort=null, $firstResult=-1, $maxResults=-1, $deep=false)
	{
		try {
		    if (HostGroupDAL::$dataHostGroupFactory == null) {
		        HostGroupDAL::$dataHostGroupFactory = @DAOFactory::getDAO($this->foundationURL, DAOFactory::FOUNDATIONWS_HOSTGROUP);
			}
			return @HostGroupDAL::$dataHostGroupFactory->getHostGroupsByFilter($filter, $sort, $firstResult, $maxResults, $deep);
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
