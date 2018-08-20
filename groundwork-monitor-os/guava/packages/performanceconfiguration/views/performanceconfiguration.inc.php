<?php
/*
Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
All rights reserved. Use is subject to GroundWork commercial license terms. 
*/

class performanceconfigurationView extends GuavaApplication {
	private $iframe;
	private $baseURL;
	private $initialURL;
	
	function __construct() {
		global $guava;
		parent::__construct("performanceconfiguration");
		
		// WHAT'S THE BASE URL
		$this->baseURL = "/performance/cgi-bin/PerfConfigAdmin.pl?top_menu=";
		
		// WHAT'S THE INITIAL URL TO VIEW
		$this->initialURL = "common";
		


		$guava->SSO_createAuthTicket("performance_auth_tkt", "/performance", 'nagiosadmin');

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
