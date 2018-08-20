<?php

/*
**	Import Data Temple Into Cacti.
*/

include_once("/usr/local/groundwork/nms/applications/cacti/include/global.php");
include_once("/usr/local/groundwork/nms/applications/cacti/lib/import.php");

$fp = fopen("/usr/local/groundwork/enterprise/bin/components/cacti/cacti_data_query_snmp_interface_status.xml","r");
$xml_data = fread($fp,filesize("cacti_data_query_snmp_interface_status.xml"));
fclose($fp);
$debug_data = import_xml_data($xml_data, false);
?>

