<?php
/*
Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
All rights reserved. Use is subject to GroundWork commercial license terms. 
*/

class nagiosmapView extends GuavaApplication {
	private $iframe;
	private $baseURL;
	private $initialURL;
	
	function __construct() {
		global $guava;
		parent::__construct("nagiosmap");
		
		// WHAT'S THE BASE URL
		$this->baseURL = "/nagios/cgi-bin/statusmap.cgi?host=all";
		
		// WHAT'S THE INITIAL URL TO VIEW
		$this->initialURL = "";
		
		

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
