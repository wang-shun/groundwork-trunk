<?php
/*
Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
All rights reserved. Use is subject to GroundWork commercial license terms. 
*/

class NagiosReportsMainView extends View {
	private $context;
	
	function __construct() {
		$this->addMenuItem("Trends", "trends");
		$this->addMenuItem("Availability", "availability");
		$this->addMenuItem("Alert Histogram", "alerthistogram");
		$this->addMenuItem("Alert History", "alerthistory");
		$this->addMenuItem("Alert Summary", "alertsummary");
		$this->addMenuItem("Notifications", "nagiosnotifications");
		$this->addMenuItem("Help", "help");
		$this->context = "common";
		parent::__construct("Reports");
	}
	
	public function menuCommand($command) {
		switch($command) {
			case 'common':
				$this->context = 'common';
				break;
			case 'trends':
				$this->context = 'trends';
				break;
			case 'availability':
				$this->context = 'availability';
				break;
			case 'alerthistogram':
				$this->context = 'alerthistogram';
				break;
			case 'alerthistory':
				$this->context = 'alerthistory';
				break;
			case 'alertsummary':
				$this->context = 'alertsummary';
				break;
			case 'nagiosnotifications':
				$this->context = 'nagiosnotifications';
				break;
			case 'help':
				$this->context = 'help';
				break;
			
		}
	}
	
	public function init() {
		global $guava;
		// Let's get our single sign-on configuration
		$ssouser = $guava->getpreference('nagiosreports', 'ssouser');
		$ssoenable = $guava->getpreference('nagiosreports', 'ssoenable');
		if($ssoenable) {
			// Create our auth ticket
			$guava->SSO_createAuthTicket("nagiosreports_auth_tkt", "/nagios", $ssouser);
		}
	}
	
	public function close() {
		// No need to do anything
	}
	
	function render() {
		switch($this->context) {
			case 'common':
			case 'trends':
				header("Location: /nagios/cgi-bin/trends.cgi");
				break;
			case 'availability':
				header("Location: /nagios/cgi-bin/avail.cgi");
				break;
			case 'alerthistogram':
				header("Location: /nagios/cgi-bin/histogram.cgi");
				break;
			case 'alerthistory':
				header("Location: /nagios/cgi-bin/history.cgi");
				break;
			case 'alertsummary':
				header("Location: /nagios/cgi-bin/summary.cgi");
				break;
			case 'nagiosnotifications':
				header("Location: /nagios/cgi-bin/notifications.cgi?contact=all");
				break;
			case 'help':
				header("Location: /reports/reports/doc/index.html");
				break;
		}
	}
	
	public function refresh() {}
	
	public function sideNavValue($command) {}
}
?>
