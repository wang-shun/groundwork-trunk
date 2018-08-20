<?php
/*
Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
All rights reserved. Use is subject to GroundWork commercial license terms. 
*/

class configurationView extends GuavaApplication {
	private $iframe;
	private $baseURL;
	private $initialURL;
	
	function __construct() {
		global $guava;
		parent::__construct("configuration");
		
		// WHAT'S THE BASE URL
		$this->baseURL = "/monarch/cgi-bin/monarch.cgi?top_menu=";
		
		// WHAT'S THE INITIAL URL TO VIEW
		$this->initialURL = "hosts";
		
		$this->addMenuItem("Services", "services");
		$this->addMenuItem("Profiles", "profiles");
		$this->addMenuItem("Hosts", "hosts");
		$this->addMenuItem("Contacts", "contacts");
		$this->addMenuItem("Escalations", "escalations");
		$this->addMenuItem("Commands", "commands");
		$this->addMenuItem("Time Periods", "time_periods");
		$this->addMenuItem("Groups", "groups");
		$this->addMenuItem("Control", "control");
		$this->addMenuItem("Tools", "tools");


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
