#!/usr/local/groundwork/nms/tools/php/bin/php -q
<?php
/*
 +-------------------------------------------------------------------------+
 | Copyright (C) 2004-2008 The Cacti Group                                 |
 |                                                                         |
 | This program is free software; you can redistribute it and/or           |
 | modify it under the terms of the GNU General Public License             |
 | as published by the Free Software Foundation; either version 2          |
 | of the License, or (at your option) any later version.                  |
 |                                                                         |
 | This program is distributed in the hope that it will be useful,         |
 | but WITHOUT ANY WARRANTY; without even the implied warranty of          |
 | MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the           |
 | GNU General Public License for more details.                            |
 +-------------------------------------------------------------------------+
 | Cacti: The Complete RRDTool-based Graphing Solution                     |
 +-------------------------------------------------------------------------+
 | This code is designed, written, and maintained by the Cacti Group. See  |
 | about.php and/or the AUTHORS file for specific developer information.   |
 +-------------------------------------------------------------------------+
 | http://www.cacti.net/                                                   |
 +-------------------------------------------------------------------------+
*/

/* do NOT run this script through a web browser */
if (!isset($_SERVER["argv"][0]) || isset($_SERVER['REQUEST_METHOD'])  || isset($_SERVER['REMOTE_ADDR'])) {
	die("<br><strong>This script is only meant to run at the command line.</strong>");
}

$no_http_headers = true;

include(dirname(__FILE__) . "/../include/global.php");
include_once($config["base_path"] . "/lib/snmp.php");
include_once($config["base_path"] . "/lib/ping.php");

/* process calling arguments */
$parms = $_SERVER["argv"];
array_shift($parms);

/* utility requires input parameters */
if (sizeof($parms) == 0) {
	print "ERROR: You must supply input parameters\n\n";
	display_help();
	exit;
}

$debug    = FALSE;
$hostid   = 0;
$hostname = "";
$timeout  = 500;
$retries  = 3;

foreach($parms as $parameter) {
	@list($arg, $value) = @explode("=", $parameter);

	switch ($arg) {
	case "--host-id":
		$hostid   = intval($value);
		break;
	case "--hostname":
		$hostname = trim($value);
		break;
	case "--retries":
		$retries  = intval($value);
		break;
	case "--timeout":
		$timeout  = intval($value);
		break;
	case "-d":
	case "--debug":
		$debug = TRUE;
		break;
	case "-h":
		display_help();
		exit;
	case "-v":
		display_help();
		exit;
	case "--version":
		display_help();
		exit;
	case "--help":
		display_help();
		exit;
	default:
		print "ERROR: Invalid Parameter " . $parameter . "\n\n";
		display_help();
		exit;
	}
}

if ($hostid > 0) {
	$host = db_fetch_row("SELECT * FROM host WHERE id='$hostid'");
}else if(strlen($hostname)) {
	$host = db_fetch_row("SELECT * FROM host WHERE hostname='$hostname'");
}else{
	echo "ERROR: You must provide either a hostname or a Cacti hostid for this to work\n";
	display_help();
	exit(1);
}

/* create new ping socket for host pinging */
$ping = new Net_Ping;

$ping->host = $host;

/* perform the appropriate ping check of the host */
if ($ping->ping(AVAIL_PING, PING_ICMP, $timeout, $retries)) {
	echo "up\t"   . $ping->ping_response . "\n";
}else{
	echo "down\t" . $ping->ping_response . "\n";
}
exit(0);

/*	display_help - displays the usage of the function */
function display_help () {
print "Cacti ICMP Pinger v1.0, Copyright 2008 - The Cacti Group\n";
	print "usage: host_icmp_ping.php [--host-id=[n] | --hostname='ip'] [--timeout=[n]] [--retries=[n]] [-d] [-h] [--help] [-v] [--version]\n\n";
	print "Required:\n";
	print "--host-id=n     - The host_id to have templates reapplied 'all' to do all hosts\n";
	print "--hostname='ip' - The hostname to be pinged\n\n";
	print "Optional:\n";
	print "--timeout=n     - The timeout in milliseconds\n";
	print "--retries=n     - The number of times to retry the ping\n";
	print "-d              - Display verbose output during execution\n";
	print "-v --version    - Display this help message\n";
	print "-h --help       - Display this help message\n\n";
}

function debug($message) {
	global $debug;

	if ($debug) {
		print("DEBUG: " . $message . "\n");
	}
}

?>
