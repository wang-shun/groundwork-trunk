<?php
/*
Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
All rights reserved. Use is subject to GroundWork commercial license terms. 
*/

class configurationezView extends GuavaApplication {
	private $iframe;
	private $baseURL;
	private $initialURL;
	
	function __construct() {
		global $guava;
		parent::__construct("configurationez");
		
		// WHAT'S THE BASE URL
		$this->baseURL = "/monarch/cgi-bin/monarch_ez.cgi?ez=1&top_menu=";
		
		// WHAT'S THE INITIAL URL TO VIEW
		$this->initialURL = "hosts";
		
		$this->addMenuItem("Hosts", "hosts");
		$this->addMenuItem("Host Groups", "host_groups");
		$this->addMenuItem("Profiles", "profiles");
		$this->addMenuItem("Notifications", "notifications");
		$this->addMenuItem("Commit", "commit");
		$this->addMenuItem("Setup", "setup");


		$guava->SSO_createAuthTicket("monarch_auth_tkt", "/monarch", $_SESSION['username']);

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
