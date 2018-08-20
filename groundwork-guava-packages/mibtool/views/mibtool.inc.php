<?php
class mibtoolView extends GuavaApplication {
	private $iframe;
	private $baseURL;
	private $initialURL;
	
	function __construct() {
		global $guava;
		parent::__construct("mibtool");
		
		// WHAT'S THE BASE URL
		$this->baseURL = "/cgi-bin/snmp/mibtool/index.cgi";
		
		// WHAT'S THE INITIAL URL TO VIEW
		$this->initialURL = "";
		
		

		$guava->SSO_createAuthTicket("mibtool_auth_tkt", $this->baseURL, $_SESSION['username']);

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
