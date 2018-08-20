<?php

require_once('DAL/webservice/FoundationStatisticDAO.inc.php');
require_once('DAL/webservice/FoundationHostGroupDAO.inc.php');
require_once('DAL/webservice/FoundationHostDAO.inc.php');
require_once('DAL/webservice/FoundationServiceDAO.inc.php');
require_once('DAL/webservice/FoundationEventDAO.inc.php');
require_once('DAL/webservice/FoundationMetaDataDAO.inc.php');
class DAOFactory {
	
	// List of DAO types supported by the factory
	const FOUNDATIONWS_STATISTIC = 1;
	const FOUNDATIONWS_HOSTGROUP = 2;
	const FOUNDATIONWS_HOST = 3;
	const FOUNDATIONWS_SERVICE = 4;
	const FOUNDATIONWS_EVENT = 5;
	const FOUNDATIONWS_METADATA = 6;
	
	public static function getDAO($foundationURL, $type) {
		switch($type) {
			case DAOFactory::FOUNDATIONWS_STATISTIC:
				try {
					return @(new FoundationStatisticDAO($foundationURL));
				}
				catch(SoapFault $sfe) {
					throw $sfe;
				}
				catch(Exception $e) {
					throw $e;
				}
				break;
			case DAOFactory::FOUNDATIONWS_HOSTGROUP:
				try {
					return @(new FoundationHostGroupDAO($foundationURL));
				}
				catch(SoapFault $sfe) {
					throw $sfe;
				}
				catch(Exception $e) {
					throw $e;
				}
				break;
			case DAOFactory::FOUNDATIONWS_HOST:
				try {
					return @(new FoundationHostDAO($foundationURL));
				}
				catch(SoapFault $sfe) {
					throw $sfe;
				}
				catch(Exception $e) {
					throw $e;
				}
				break;
			case DAOFactory::FOUNDATIONWS_SERVICE:
				try {
					return @(new FoundationServiceDAO($foundationURL));
				}
				catch(SoapFault $sfe) {
					throw $sfe;
				}
				catch(Exception $e) {
					throw $e;
				}
				break;
			case DAOFactory::FOUNDATIONWS_EVENT:
				try {
					return @(new FoundationEventDAO($foundationURL));
				}
				catch(SoapFault $sfe) {
					throw $sfe;
				}
				catch(Exception $e) {
					throw $e;
				}
				break;
			case DAOFactory::FOUNDATIONWS_METADATA:
				try {
					return @(new FoundationMetaDataDAO($foundationURL));
				}
				catch(SoapFault $sfe) {
					throw $sfe;
				}
				catch(Exception $e) {
					throw $e;
				}
				break;
			default:
				return null;
		}
	}
	
}
 
?>