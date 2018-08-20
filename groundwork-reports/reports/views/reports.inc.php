<?php
class reportsView extends GuavaApplication  {
	private $iframe;
	private $baseURL;
	private $initialURL;
	
	function __construct() {
		global $guava;
		parent::__construct("reports");
		
		// WHAT'S THE BASE URL
		$this->baseURL = "/reports/cgi-bin/";
		
		// WHAT'S THE INITIAL URL TO VIEW
		$this->initialURL = "/reportserver/index.jsp";

		// GROUNDWORK REPORTS (Formerly "Advanced Reports")
		$optArray = array(	"/reportserver/index.jsp" => "View Reports",
							"/reportserver/admin-fs.jsp" => "Publish a Report");
		$this->addDropMenu("GroundWork Reports",$optArray,$this);
		

		// INSIGHT REPORTS
		$optArray = array(	"/reports/cgi-bin/nagios_alarms1.pl" => "Alerts", 
							"/reports/cgi-bin/nagios_notifications1.pl" => "Notifications",
							"/reports/cgi-bin/nagios_outages1.pl" => "Outages");
		
		$this->addDropMenu("Insight Reports",$optArray,$this);


		// NAGIOS REPORTS
		$optArray = array(	"/nagios/cgi-bin/trends.cgi" => "Trends", 
						 	"/nagios/cgi-bin/avail.cgi" => "Availability", 
						 	"/nagios/cgi-bin/histogram.cgi" => "Alert Histogram", 
						 	"/nagios/cgi-bin/history.cgi" => "Alert History", 
						 	"/nagios/cgi-bin/summary.cgi" => "Alert Sumamry",
						 	"/nagios/cgi-bin/notifications.cgi?contact=all" => "Notifications",
						 	"/nagios/cgi-bin/config.cgi" => "Configuration");
		$this->addDropMenu("Nagios Reports",$optArray,$this);

	
						 	
		$guava->SSO_createAuthTicket("reports_auth_tkt", $this->baseURL, 'reports');

	}
	

	
	
	
	public function menuCommand($command) {
		if($command != "---"){
			$this->iframe->setSrc($command);
		}
	}
	
	public function init() {
		$this->iframe = new IFrame($this->initialURL);
		
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
