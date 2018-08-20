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
 * This is a utility class that can be used to retrieve what is generally
 * static data in foundation.  
 * 
 * @package DAL
 *
 */
class MetaDataDAL {
	static private $dataMetaDataFactory;
	static private $foundationURL;
		
    /**
     * Create a MetaDataDAL instance
     * @param string $foundationURL - can not be NULL
     */
	public function __construct($foundationURL) 
	{
	    if ($foundationURL == null)
	       throw new DALException("A path to the Foundation Webservice must be provided");
	    $this->foundationURL = $foundationURL;
	}
	
    /**
     * gets all of the application types currently defined.
     *
     * Data is returned in the following format:
        Array
        (
            [TotalCount] => 4
            [ApplicationTypes] => Array
                (
                    [SYSTEM] => Array
                        (
                            [Name] => SYSTEM
                            [AttributeID] => 1
                            [Description] => Properties that exist regardless of the Application being monitored
                        )
        
                    [NAGIOS] => Array
                        (
                            [Name] => NAGIOS
                            [AttributeID] => 100
                            [Description] => System monitored by Nagios
                        )
        
                    [SNMPTRAP] => Array
                        (
                            [Name] => SNMPTRAP
                            [AttributeID] => 101
                            [Description] => SNMP Trap application
                        )
        
                    [SYSLOG] => Array
                        (
                            [Name] => SYSLOG
                            [AttributeID] => 102
                            [Description] => SYSLOG application
                        )
        
                )
        
        )     
     * @return list of application type objects
     */
	public function getApplicationTypes()
	{
	    global $guava;
		try {
			if (MetaDataDAL::$dataMetaDataFactory == null) {
					MetaDataDAL::$dataMetaDataFactory = @DAOFactory::getDAO($this->foundationURL, DAOFactory::FOUNDATIONWS_METADATA);
			}
			return @MetaDataDAL::$dataMetaDataFactory->getApplicationTypes();
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