<?php
/*
Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
All rights reserved. Use is subject to GroundWork commercial license terms. 
*/

class nagiosreportsView extends GuavaApplication {
	private $iframe;
	private $baseURL;
	private $initialURL;
	
	function __construct() {
		global $guava;
		parent::__construct("nagiosreports");
		
		// WHAT'S THE BASE URL
		$this->baseURL = "/nagios/cgi-bin/";
		
		// WHAT'S THE INITIAL URL TO VIEW
		$this->initialURL = "trends.cgi";
		
		$this->addMenuItem("Trends", "trends.cgi");
$this->addMenuItem("Availability", "avail.cgi");
$this->addMenuItem("Alert Histogram", "histogram.cgi");
$this->addMenuItem("Alert History", "history.cgi");
$this->addMenuItem("Alert Summary", "summary.cgi");
$this->addMenuItem("Notifications", "notifications.cgi?contact=all");
$this->addMenuItem("Configuration", "config.cgi");


		$guava->SSO_createAuthTicket("nagios_auth_tkt", "/nagios/", 'nagiosadmin');

	}
	
	public function menuCommand($command) {
		$this->iframe->setSrc($this->baseURL . $command);
	}
	
	public function init() {
		$this->iframe = new IFrame($this->baseURL . $this->initialURL);
		
	}
	
	public function close() {
		if(isset($this->iframe)) {
			$this->iframe->unregister();
			$this->iframe = null;
		}
	}
	
	function Draw() {
		$this->iframe->Draw();
	}
}
?>
