<?php
class weathermapeditorView extends GuavaApplication
{
	private $iframe;
	private $baseURL;
	private $initialURL;
	
	function __construct()
	{
		global $guava;
		parent::__construct("weathermapeditor");
		
		// Weathermap Editor doesn't require refresh.
                $this->disableRefresh();

		// WHAT'S THE BASE URL
		$this->baseURL = "http://" . $_SERVER['SERVER_NAME'] . ":81/";

		// WHAT'S THE INITIAL URL TO VIEW
		$this->initialURL = "/cacti/plugins/weathermap/editor.php";
		
		$guava->SSO_createAuthTicket("weathermap_auth_tkt", $this->initialURL, $_SESSION['username']);
	}
	
	public function menuCommand($command)
	{
		$this->iframe->setSrc($this->baseURL . $command);
	}
	
	public function init()
	{
		$this->iframe = new IFrame($this->baseURL . $this->initialURL);
		
	}
	
	public function close()
	{
		if(isset($this->iframe)) {
			$this->iframe->unregister();
			$this->iframe = null;
		}
	}
	
	function Draw()
	{
		$this->iframe->Draw();
	}
}
?>
