<?php
class ntopView extends GuavaApplication
{
	private $iframe;
	private $baseURL;
	private $initialURL;
	
	function __construct()
	{
		global $guava;
		parent::__construct("ntop");
		
                // NTOP doesn't require refresh.
                $this->disableRefresh();

		// WHAT'S THE BASE URL
		 $this->baseURL = "http://" . $_SERVER['SERVER_NAME'] . ":82/";
		
		// WHAT'S THE INITIAL URL TO VIEW
		$this->initialURL = "";

		$guava->SSO_createAuthTicket("ntop_auth_tkt", $this->initialURL, $_SESSION['username']);

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
		if(isset($this->iframe))
		{
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
