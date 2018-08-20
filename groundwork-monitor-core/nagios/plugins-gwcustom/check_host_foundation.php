#!/usr/local/groundwork/php/bin/php.bin
<?php
# This is a Nagios plugin that checks the status of all services on a host. 
# It is intended to be used to provide an erzatz host check in the case where 
# passive service checks are all that is available. 
# Return values are UP if at least one service is in OK state, 
# DOWN if all services are not OK. 
set_include_path('/usr/local/groundwork/core/foundation/api/php/');
require_once('DAL/ServiceDAL.inc.php');
# Would be nice to use this, but it's broken at the moment in PHP
#require_once('DAL/StatisticsDAL.inc.php');

# Handle Arguments 
$args = "";
$args .= "h:";
$args .= "d::";

$options = getopt($args);

if (!$options['h']) {
	print "Host name required! Use -h hostname\n";
	exit (1);
} else {
	$host2query = $options['h'];
}
if ($options['d']) {
	print "Debug set with -d. We will output any errors and all data. Do not use with Nagios in this mode.\n";
	$debug_exceptions = 1;
	$debug_service_results = 1;
} else {
	$debug_exceptions = 0;
        $debug_service_results = 0;
}

$webservicesURL = 'http://localhost:8080/foundation-webapp/services';

try {
#	$serviceDAL  = new StatisticsDAL($webservicesURL);
	 $serviceDAL  = new ServiceDAL($webservicesURL);
 $results = $serviceDAL->getServicesByHostName($host2query);      
#$results = $serviceDAL->getServiceStatisticsByHostName($host2query);
} catch (DALException $dalEx) {
    if ($debug_exceptions) {
        print "Caught exception:  ".$dalEx->getMessage()."\n";
    }
    print "ERROR:  Could not contact Foundation!\n";
    exit (1);
}
# Print debug data
if ($debug_service_results) {
    $returnArray = $results['Services'];
    if(count($returnArray)) {
        foreach ($returnArray as $service) {
	    $returnDesc = $service['Description'];
            $returnState = $service['MonitorStatus'];
            print "$returnDesc = " . $returnState->Name . "\n";
        }
    }
}
# Set up output
$returnArray = $results['Services'];
if(count($returnArray)) {
    foreach ($returnArray as $service) {
        $returnState = $service['MonitorStatus']->Name;
	$result = preg_match('/OK/', $returnState);
	if ($result == 1) { # Nothing more to do - host is UP
	    print "At least one service is OK. Host considered UP\n";
            exit (0);
        } 
    }
	print "All services in non-OK state! Host considered DOWN\n";
	exit (2);
} else { # No services to check
	print "This host has no services defined. Setting status to UP.\n";
	exit (0);
}
?>
