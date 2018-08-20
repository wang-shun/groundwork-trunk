<?php
class gwfoundationAdminView extends GuavaApplication {
	private $iframe;
	private $baseURL;
	private $initialURL;
	
	function __construct() {
		global $guava;
		parent::__construct("gwfoundationadminpane");
		
		// WHAT'S THE BASE URL
		$this->baseURL = "/foundation-webapp/";
		
		// WHAT'S THE INITIAL URL TO VIEW
		$this->initialURL = "admin/manage-configuration.jsp";
		
		$this->addMenuItem("Manage configuration", "admin/manage-configuration.jsp");
		$this->addMenuItem("Manage Hostgroups", "admin/manage-hostgroups.jsp");	
		$this->addMenuItem("Manage properties", "admin/manage-properties.jsp");		
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
