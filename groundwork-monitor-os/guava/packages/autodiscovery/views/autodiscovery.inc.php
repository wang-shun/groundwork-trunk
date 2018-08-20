<?php
class AutoDiscoveryView extends GuavaApplication {
	private $iframe;
	private $baseURL;
	private $initialURL;
	
	function __construct() {
		global $guava;
		parent::__construct("AutoDiscovery");
		
		// WHAT'S THE BASE URL
		$this->baseURL = "/monarch/cgi-bin/monarch_auto.cgi?view=";
		
		// WHAT'S THE INITIAL URL TO VIEW
		$this->initialURL = "discover";
		
		$this->addMenuItem("Discovery", "discover");
		$this->addMenuItem("Automation", "automation");

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
