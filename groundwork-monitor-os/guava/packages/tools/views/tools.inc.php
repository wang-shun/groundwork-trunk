<?php
/*
Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
All rights reserved. Use is subject to GroundWork commercial license terms. 
* @author dpuertas
*/

class toolsView extends GuavaApplication {
	private $iframe;
	private $baseURL;
	private $initialURL;
	
	function __construct() {
		global $guava;
		parent::__construct("tools");
		
		// WHAT'S THE BASE URL
		$this->baseURL = "/";
 	
	 	$guava->SSO_createAuthTicket("profiletools_auth_tkt", "/profiles/cgi-bin/", 'nagiosadmin');
		$guava->SSO_createAuthTicket("nagios_auth_tkt", "/", 'nagiosadmin');
		
 		// WHAT'S THE INITIAL URL TO VIEW
		$this->initialURL = "/nagios/cgi-bin/status.cgi?host=localhost";

		//Only Show MIB Validator if installed && Role is Admin
		if( ($guava->isPackageInstalled("MIB Validator",1,0)) && ($guava->getDefaultRole($_SESSION['user_id']) == 1)){
	 		$guava->SSO_createAuthTicket("mibtool_auth_tkt", "/cgi-bin/snmp/mibtool/", $_SESSION['username']);	
			$this->addMenuItem("MIB Validator","mibValidator");
			$this->initialURL = "/cgi-bin/snmp/mibtool/index.cgi";
		} 
		
		
		if( $guava->isPackageInstalled("Profile Tools")  ){
			//Profile Tools Menu
			$menuOpts = array( 	"/profiles/cgi-bin/showprofiles.pl?top_menu=show"=>"Show",
								"/profiles/cgi-bin/importcfg.pl?top_menu=import"=>"Import");
			
			$this->addDropMenu("Profile Tools",$menuOpts,$this);
 
		}
				$msOptions = array(
					"/nagios/cgi-bin/status.cgi?host=localhost" => "Server Status",
					"/nagios/cgi-bin/extinfo.cgi?type=0" => "Process Info",
					"/nagios/cgi-bin/extinfo.cgi?type=4" => "Performance Info",
					"/nagios/cgi-bin/extinfo.cgi?type=7" =>  "Scheduling Queue"
			);
					
		$this->addDropMenu("--Monitoring Server--",$msOptions,$this);
//		//Nagios Menu	
//		$nagiosOpts = array( 	"/nagios/cgi-bin/tac.cgi" => "Tactical",
//							 	"/nagios/cgi-bin/status.cgi?hostgroup=all&style=hostdetail"=>  "Hosts",
//								"/nagios/cgi-bin/status.cgi?hostgroup=all"=>  "Service Overview",
//								"/nagios/cgi-bin/status.cgi?host=all"=>  "Service Detail",
//								"/nagios/cgi-bin/status.cgi?hostgroup=all&style=summary" =>  "Service Summary",
//								"/nagios/cgi-bin/status.cgi?hostgroup=all&style=grid" =>  "Service Grid",
//								"/nagios/cgi-bin/status.cgi?host=all&servicestatustypes=248" =>  "Service Problems",
//								"/nagios/cgi-bin/status.cgi?hostgroup=all&style=hostdetail&hoststatustypes=12" =>  "Host Problems",
//								"/nagios/cgi-bin/outages.cgi" =>  "Net Outages",
//								"/nagios/cgi-bin/extinfo.cgi?type=3" =>  "Comments",
//								"/nagios/cgi-bin/extinfo.cgi?type=6" =>  "Downtime",
//								"/nagios/cgi-bin/showlog.cgi" =>  "Event Log",
//								"/nagios/cgi-bin/config.cgi" =>  "View Config"); 
//		
//		$this->addDropMenu("Nagios",$nagiosOpts,$this);
		
		$this->iframe = new IFrame($this->initialURL);

 		
	}
	
	public function menuCommand($command) {
		$commandURL = "";
	 	global $guava;
		
		//if($command == "nagios"){
		//	$guava->SSO_createAuthTicket("nagios_auth_tkt", "/", 'nagiosadmin');
		//	$commandURL =  "/nagios/cgi-bin/tac.cgi";
		//	$this->iframe->setSrc($commandURL);
		//}
		
		//else 
		if($command == "mibValidator"){
			$guava->SSO_createAuthTicket("mibtool_auth_tkt", "/cgi-bin/snmp/mibtool/", $_SESSION['username']);	
			$guava->SSO_createAuthTicket("profiletools_auth_tkt", "/profiles/cgi-bin/", 'nagiosadmin');
			$commandURL = "/cgi-bin/snmp/mibtool/index.cgi";
			$this->iframe->setSrc($commandURL);
			
		}
		
		else if($command != "---"){
 
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
