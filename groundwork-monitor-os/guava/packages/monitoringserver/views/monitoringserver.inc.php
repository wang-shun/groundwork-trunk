<?php
/*
Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
All rights reserved. Use is subject to GroundWork commercial license terms. 
*/

class monitoringserverView extends GuavaApplication {
	private $iframe;
	private $baseURL;
	private $initialURL;
	
	function __construct() {
		global $guava;
		parent::__construct("monitoringserver");
		
		// WHAT'S THE BASE URL
		$this->baseURL = "/nagios/cgi-bin/";
		
		// WHAT'S THE INITIAL URL TO VIEW
		$this->initialURL = "status.cgi?host=localhost";
		
		$this->addMenuItem("Server Status", "status.cgi?host=localhost");
$this->addMenuItem("Process Info", "extinfo.cgi?type=0");
$this->addMenuItem("Performance Info", "extinfo.cgi?type=4");
$this->addMenuItem("Scheduling Queue", "extinfo.cgi?type=7");


		$guava->SSO_createAuthTicket("monitoringserver_auth_tkt", $this->baseURL, 'nagiosadmin');

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
