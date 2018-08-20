<?php
class gwNOC extends GuavaApplication {
	private $iframe;
	private $baseURL;
	private $initialURL;
	
	function __construct() {
		global $guava;
		parent::__construct("gwnoc");
		
		// GWMON-3237
		// The new console doesn't need the refresh since it's using AJAX push
		$this->disableRefresh();
		
		// WHAT'S THE BASE URL
		$this->baseURL = "http://" .  $_SERVER['SERVER_NAME'] . "/groundwork-console/";
		
		// WHAT'S THE INITIAL URL TO VIEW
		$ustring = "user=" . $_SESSION['username'];	
		$hexString = bin2hex($ustring);
		$this->initialURL = "Console.jsf" . "?sessionid=" . $hexString;	
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
