<?php
/*
Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
All rights reserved. Use is subject to GroundWork commercial license terms. 
*/

class performanceView extends GuavaApplication {
	private $iframe;
	private $baseURL;
	private $initialURL;
	
	function __construct() {
		global $guava;
		parent::__construct("performance");
		
		// WHAT'S THE BASE URL
		$this->baseURL = "/performance/cgi-bin/perfchart.cgi?top_menu=";
		
		// WHAT'S THE INITIAL URL TO VIEW
		$this->initialURL = "common";
		
		$this->addMenuItem("Viewer", "performance");
		if($guava->getDefaultRole($_SESSION['user_id']) == 1){
			$this->addMenuItem("Configure","configureGraphs");
		}
		$guava->SSO_createAuthTicket("performance_auth_tkt", "/performance", 'nagiosadmin');

	}
	
	public function menuCommand($command) {
		$commandURL = "";
		switch($command){
			case "performance":
				$commandURL = "/performance/cgi-bin/perfchart.cgi?top_menu=performance";
				break;
				
			case "configureGraphs":
				$commandURL =  "/performance/cgi-bin/PerfConfigAdmin.pl?top_menu=common";
				break;
		}
 					
		$this->iframe->setSrc($commandURL);
		
		
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
