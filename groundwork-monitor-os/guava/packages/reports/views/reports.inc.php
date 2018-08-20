<?php
/*
Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
All rights reserved. Use is subject to GroundWork commercial license terms. 
*/

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
		$this->initialURL = "/reports/cgi-bin/nagios_alarms1.pl";

		// GROUNDWORK REPORTS (Formerly "Advanced Reports")
		$optArray = array(	"/reportserver/index.jsp" => "View Reports",
							"/reportserver/admin-fs.jsp" => "Publish a Report");
							
		if($guava->isPackageInstalled("Log Reporting",1,0)){
			$optArray = array(	"/reportserver/index.jsp" => "View Reports",
					"/reportserver/admin-fs.jsp" => "Publish a Report");
		}
							
		// Only show GroundWork Reports if Installed		
		if($guava->isPackageInstalled("Advanced Reporting",1,0)){
			$this->addDropMenu("GroundWork Reports",$optArray,$this);
			$this->initialURL = "/reportserver/index.jsp";
			
		}
		$guava->SSO_createAuthTicket("nagios_auth_tkt", "/", 'nagiosadmin');
		$guava->SSO_createAuthTicket("nagiosreports_auth_tkt", "/reports/cgi-bin/", 'nagiosadmin');

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
						 	"/nagios/cgi-bin/summary.cgi" => "Alert Summary",
						 	"/nagios/cgi-bin/notifications.cgi?contact=all" => "Notifications",
						 	"/nagios/cgi-bin/config.cgi" => "Configuration");
		$this->addDropMenu("Nagios Reports",$optArray,$this);

	
						 	
		//$guava->SSO_createAuthTicket("nagios_auth_tkt", "/nagios/", 'nagiosadmin');
		
		$ssouser = $guava->getpreference('nagiosreports', 'ssouser');
		$ssoenable = $guava->getpreference('nagiosreports', 'ssoenable');
		if($ssoenable) {
			// Create our auth ticket
			
			$guava->SSO_createAuthTicket("nagiosreports_auth_tkt", "/nagios", $ssouser);
			$guava->SSO_createAuthTicket("advanced_auth_tkt","/reportserver/",$ssouser);
		}


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
