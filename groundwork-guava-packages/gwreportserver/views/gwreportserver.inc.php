<?php
/*
Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
All rights reserved. Use is subject to GroundWork commercial license terms. 
*/

class gwreportserverView extends GuavaApplication {
	private $iframe;
	private $baseURL;
	private $initialURL;
	
	function __construct() {
		global $guava;
		parent::__construct("gwreportserver");
		
		// WHAT'S THE BASE URL
		$this->baseURL = "/reportserver/";
		
		// WHAT'S THE INITIAL URL TO VIEW
		$this->initialURL = "";
		
		$this->addMenuItem("View Reports", "index.jsp");
		$this->addMenuItem("Report Admin", "admin-fs.jsp");		
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
