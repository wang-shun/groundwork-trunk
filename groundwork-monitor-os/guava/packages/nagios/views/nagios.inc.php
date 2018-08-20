<?php
/*
Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
All rights reserved. Use is subject to GroundWork commercial license terms. 
*/

class nagiosView extends GuavaApplication implements ActionListener  {
	private $iframe;
	private $baseURL;
	private $initialURL;
	
	private $select;
	
	function __construct() {
		global $guava;
		parent::__construct("nagios");
		
		// WHAT'S THE BASE URL
		$this->baseURL = "/nagios/cgi-bin/";
		
		// WHAT'S THE INITIAL URL TO VIEW
		$this->initialURL = "tac.cgi";
		
		$guava->SSO_createAuthTicket("nagios_auth_tkt", "/", 'nagiosadmin');

	}
	
	
	public function init() {
		$this->iframe = new IFrame($this->baseURL . $this->initialURL);

				
		$this->select = new Select();
		$this->select->addOption("tac.cgi", "Tactical");
		$this->select->addOption("status.cgi?hostgroup=all&style=hostdetail", "Hosts");
		$this->select->addOption("status.cgi?hostgroup=all", "Service Overview");
		$this->select->addOption("status.cgi?host=all", "Service Detail");
		$this->select->addOption("status.cgi?hostgroup=all&style=summary", "Service Summary");
		$this->select->addOption("status.cgi?hostgroup=all&style=grid", "Service Grid");
		$this->select->addOption("status.cgi?host=all&servicestatustypes=248", "Service Problems");
		$this->select->addOption("status.cgi?hostgroup=all&style=hostdetail&hoststatustypes=12", "Host Problems");
		$this->select->addOption("outages.cgi", "Net Outages");
		$this->select->addOption("extinfo.cgi?type=3", "Comments");
		$this->select->addOption("extinfo.cgi?type=6", "Downtime");
		$this->select->addOption("showlog.cgi", "Event Log");
		$this->select->addOption("config.cgi", "View Config");
		$this->select->addActionListener("click", $this);
		
		$this->targetData("select", $this->select);	
		
		
				$nagiosOpts = array( 	"/nagios/cgi-bin/tac.cgi" => "Tactical",
							 	"/nagios/cgi-bin/status.cgi?hostgroup=all&style=hostdetail"=>  "Hosts",
								"/nagios/cgi-bin/status.cgi?hostgroup=all"=>  "Service Overview",
								"/nagios/cgi-bin/status.cgi?host=all"=>  "Service Detail",
								"/nagios/cgi-bin/status.cgi?hostgroup=all&style=summary" =>  "Service Summary",
								"/nagios/cgi-bin/status.cgi?hostgroup=all&style=grid" =>  "Service Grid",
								"/nagios/cgi-bin/status.cgi?host=all&servicestatustypes=248" =>  "Service Problems",
								"/nagios/cgi-bin/status.cgi?hostgroup=all&style=hostdetail&hoststatustypes=12" =>  "Host Problems",
								"/nagios/cgi-bin/outages.cgi" =>  "Net Outages",
								"/nagios/cgi-bin/extinfo.cgi?type=3" =>  "Comments",
								"/nagios/cgi-bin/extinfo.cgi?type=6" =>  "Downtime",
								"/nagios/cgi-bin/showlog.cgi" =>  "Event Log",
								"/nagios/cgi-bin/config.cgi" =>  "View Config"); 
		
		$this->addDropMenu("Views",$nagiosOpts,$this);
		
		
		$this->addmenuItem("Map","/nagios/cgi-bin/statusmap.cgi?host=all");
	}
	
	public function actionPerformed($event) {
		$this->iframe->setSrc($this->baseURL . $this->select->getValue());
	}
	
	public function menuCommand($command) {
		if($command != "---"){
			$this->iframe->setSrc($command);
		}
	}
	
	public function close() {
		if(isset($this->iframe)) {
			$this->iframe->unregister();
			$this->iframe = null;
			$this->select->unregister();
			$this->select = null;
		}
	}


	function Draw() {
		?>
		<div dojoType="LayoutContainer" layoutChildPriority="top-bottom" style="width: 100%; height: 100%; overflow: hidden;">
		

			<div dojoType="ContentPane" layoutAlign="client" style="width: 100%; background: white;">
				<?php $this->iframe->Draw(); ?>
			</div>

		
		</div>
		
		<?php
	}
}
?>
