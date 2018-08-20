<?php
include_once('collageapi/collage.inc.php');
include_once('includes/output.php');	// Include application's output library
include_once('includes/parmfile.inc.php');	// Include routine to help read groundwork parameter file

// Configuration
$db_properties_file = "/usr/local/groundwork/config/db.properties";	// Path to our db.properties file



session_start();
session_register("groundworkConfig");
parseDBPropertiesFile($db_properties_file);
// If we got here, we've read the file, let's check for required variables
if(!isset($_SESSION['groundworkConfig']['collage']['username']) || 
	!isset($_SESSION['groundworkConfig']['collage']['password']) || 
	!isset($_SESSION['groundworkConfig']['collage']['database']) ||
	!isset($_SESSION['groundworkConfig']['collage']['dbhost'])) {
		print("Properties File Error: One or more of the required fields are not defined.");
		die();
}
// We got here!  Let's test connect to the server
$testDB = new CollageDB("mysql", $_SESSION['groundworkConfig']['collage']['dbhost'], 
				$_SESSION['groundworkConfig']['collage']['username'],
				$_SESSION['groundworkConfig']['collage']['password'],
				$_SESSION['groundworkConfig']['collage']['database'],'NAGIOS');
if(!$testDB->isConnected()) {
	print_r($_SESSION['groundworkConfig']['collage']);
	print("Collage DB Error: Database not connected.  Check your connection parameters.<br />");
	print($collageDB->get_error_msg());
	die();
}

print_header("Test Application");

?>
<br />
<br />
<?php
print_window_header("getHostsByFilter Test", "90%");
?>
Attempting to filter with Following Filter:<br />
//isAcknowledged = 1<br />
//isChecksEnabled = 1<br />
<br />
<?php

//$filter[] = array("key" => "isAcknowledged", "operator" => "=", "value" => "1");
//$filter[] = array("key" => "isChecksEnabled", "operator" => "=", "value" => "1");
		$filter = array();
		$filter[] = array("key" => "HostStatusID", "operator" => "=", "value" => "38");
		$filter[] = array("key" => "ServiceStatusID", "operator" => "=", "value" => "12");
		//$filter[] = array("key" => "ApplicationName", "operator" => "=", "value" => "notify-by-email");

//$tempQuery = new CollageHostQuery($testDB);

//$filterResults = $tempQuery->getHostsByFilter($filter);

$tempQuery = new CollageEventQuery($testDB);
$filterResults = $tempQuery->getEventsByFilter($filter);

//print("<b>Results for getHostsByFilter:</b><br />");
//print("<b>Results for getEventsByFilter:</b><br />");
//print_r($filterResults);

foreach ($filterResults as $result) {
	print("<br/>Result: ");
	print_r($result);
	$event = $tempQuery->getEventByID($result['LogMessageID']);
	print("<br/>Event: ");
	print_r($event);
}
//$serviceQuery = new CollageServiceQuery($testDB);
//$serviceResult = $serviceQuery->getService("nrpe_evalid_transaction_script_2","Application_2");

//print("<br />");
//print("<b>Results for getService:</b><br />");
//print_r($serviceResult);

//$serviceResult = $serviceQuery->getServiceByStatusID("6");
//print("<br />");
//print("<b>Results for getServiceByStatusID:</b><br />");
//print_r($serviceResult);

//$serviceResult = $serviceQuery->getServices();
//print("<br />");
//print("<b>Results for getServices:</b><br />");
//print_r($serviceResult);

//$hostGroupQuery = new CollageHostGroupQuery($testDB);
//$HGserviceResult = $hostGroupQuery->getServicesForHostGroup("monitoring-servers");
//print("<br />");
//print("<b>Results for getServicesForHostGroup:</b><br />");
//print_r($HGserviceResult);


print_window_footer();

print_footer();


?>