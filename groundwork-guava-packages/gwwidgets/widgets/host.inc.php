<?php
/*
Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
All rights reserved. Use is subject to GroundWork commercial license terms. 
*/

require_once('DAL/HostDAL.inc.php');

class GWWidgetsHostListConfigureDialog extends GuavaWidgetConfigureDialog implements ActionListener  {
	
	private $HostNames;
	
	private $HostButtons;
	private $selectRefresh;
	private $inputHost;
	private $addButton;
	
	public function __construct($source) {
		parent::__construct($source);
		
		$this->HostNames = $source->getHostList();
		
		
		$this->rebuildListTarget();
		$this->selectRefresh = new InputSelect();
		$this->selectRefresh->addOption(  30, "Every 30 seconds");
		$this->selectRefresh->addOption(  60, "Every  1 minute");
		$this->selectRefresh->addOption( 300, "Every  5 minutes");
		$this->selectRefresh->addOption( 600, "Every 10 minutes");
		$this->selectRefresh->addOption( 900, "Every 15 minutes");
		$this->selectRefresh->addOption(1800, "Every 30 minutes");
		$this->selectRefresh->addOption(2700, "Every 45 minutes");
		$this->selectRefresh->addOption(3600, "Every hour");

		$this->inputHost = new InputTextSuggestControl("Host",$this); //InputText(50, 255);
		$this->addButton = new Button("Add Host");
		$this->addButton->addActionListener("click", $this);
	}
	
	public function unregister() {
		parent::unregister();
		$this->inputHost->unregister();
		$this->addButton->unregister();
	}
	
	public function getHostList() {
		return $this->HostNames;
	}
	
	public function actionPerformed($event) {
		global $foundationModule;
		parent::actionPerformed($event);
		$hostname = $this->inputHost->getValue();				
		if($event->getSource() === $this->addButton) {
			// We want to add a Host
			if($hostname == "") {
				$dialog = new ErrorDialog("Host Name cannot be blank.");
				$dialog->show();
				return;
			}
 	 foreach ($this->HostNames as $arrayHost){
    	if (strtolower($arrayHost) == strtolower($hostname)){  
    			$dialog = new ErrorDialog("Host already exists in list.");
				$dialog->show();
				return;				
			}
 	 	}			
			
        try {
    		// Let's first check to see if host exists
    		$hostDAL = new HostDAL($foundationModule->getWebServiceURL());
    		
    		$hostInfo = $hostDAL->getHostByHostName($hostname);
        }
		catch (Exception $e)
		{
		    $dialog = new ErrorDialog("Unable to retrieve host ".$this->hostName);
		    $dialog->show();
		    if ($sv2->getErrorOccurred() == false)
    		  $sv2->setErrorOccurred(true);
		}
		
		if($hostInfo == null || empty($hostInfo)) {
			// Host does not exist
			$err = new ErrorDialog("Host does not exist.");
	 		$err->show();
	 		return;
		}
			/**
			 * @todo We should check for Host name validity here
			 */
			$this->HostNames[] = $hostname;
			$this->rebuildListTarget();
			//reset input field to empty when host is added
			$this->inputHost->setValue("");
		}
		else {
			if(count($this->HostButtons)) {
				foreach($this->HostButtons as $key => $button) {
					if($event->getSource() === $button) {
						$index = array_search($key, $this->HostNames);
						unset($this->HostNames[$index]);
						$button->unregister();
						unset($this->HostButtons[$key]);
						
						
						
						$this->rebuildListTarget();
						return;
					}
				}
			}
			$this->hide();
			$this->unregister();
		}
	}
	
	private function rebuildListTarget() {
		foreach($this->HostNames as $Host) {
			if(isset($this->HostButtons[$Host])) {
				$this->HostButtons[$Host]->unregister();
			}
			$this->HostButtons[$Host] = new TextLink("Delete");
			$this->HostButtons[$Host]->addActionListener("click", $this);
			$buffer .= '<div style="background: #dddddd; height: 25px; border-width: 1px 0px 1px 0px; border-style: solid; border-bottom-color: black; border-left-color: grey; border-right-color: black; border-top-color: grey;"><table width="100%"><tr><td>'.$Host . '</td><td align="right">' . $this->HostButtons[$Host]->toString() . '</td></tr></table></div>';

		}
		$this->targetData("HostList", $buffer);
	}
	
	public function Draw() {
		?>
		<h1 align='center'>Host List Configuration</h1>
		<br/>
 		<h1>Specify An Additional Host:</h1>
		<?php 
//		if($this->inputHost->getValue() == "") {
			$this->inputHost->Draw();
			?><br>
			<div id="search_suggest">
			</div>
			<?php
						//$this->printTarget("contents"); 
	//	}else{
			//$this->printTarget("contents");
	//	} 
		?> <?=$this->addButton->Draw(); ?> <br />
		<br />
		<h1>Current Host List</h1>
		<div style="height: 200px; border: 1px solid grey; overflow: auto;">
		<?=$this->printTarget("HostList");?><br />
		<br />
		</div>
		<h1>Set the Refresh Rate:</h1>
		<?php
		$this->selectRefresh->Draw();
		?>
		
		<?php
	}
}

class GWWidgetsHostListWidgetItem extends GuavaObject implements ActionListener {
	
	private $hostName;
	
	private $downQuery, $unreachableQuery;
	
	private $downHosts, $unreachableHosts;
	
	private $configInfo;
	
	private $notFound;
	
	private $hostLink;
	
	private $status;
	
	public function __construct($hostName) {
		global $foundationModule;
		global $sv2;
		
		parent::__construct();
		
		$this->hostName = $hostName;
		
		
		$this->setTemplate(GUAVA_FS_ROOT . 'packages/gwwidgets/templates/hostlistitem.xml');
		
		$this->bind("self", $this);
		
		$this->hostLink = new TextLink($this->hostName);
		$this->hostLink->addActionListener("click", $this);
		$this->bind("hostLink", $this->hostLink);
		
        try {
    		// Let's first check to see if host exists
    		$hostDAL = new HostDAL($foundationModule->getWebServiceURL());
    		
    		$hostInfo = $hostDAL->getHostByHostName($this->hostName);
        }
		catch (DALException $dalEx)
		{
		    if ($sv2->getErrorOccurred() == false)
		    {
    			$dialog = new ErrorDialog("An error occurred when connecting to the foundation webservice.  Please contact your system administrator for further assistance.");
    			$dialog->show();
    			$sv2->setErrorOccurred(true);
		    }
		}
		catch (Exception $e)
		{
		    $dialog = new ErrorDialog("Unable to retrieve host ".$this->hostName);
		    $dialog->show();
		    if ($sv2->getErrorOccurred() == false)
    		  $sv2->setErrorOccurred(true);
		}
		
		if($hostInfo == null || empty($hostInfo)) {
			// Host does not exist
			$this->notFound = true;
	 
		}
		else {
			$this->notFound = false;
		 				
		}
		$this->update();
	}
	
	public function unregister() {
		parent::unregister();
		$this->hostLink->unregister();
	}
	
	public function actionPerformed($event) {
		global $sv2;
		global $guava;
		if(isset($sv2) && !$this->notFound) {
			$component = $sv2->createHostComponent($this->hostName);
			$component->expand();
			
			$component->setCloned(true);
			
			$guava->objectView($component);
		}
	}
	
	public function getHostName() {
		return $this->HostName;
	}
	
	public function isNotFound() {
		return $this->notFound;
	}
	
	public function getStyle() {
		return "background-color: #00ff00;";
	}
	
	public function getHostsDown() {
		return (string)$this->downHosts;
	}
	
	public function getHostsUnreachable() {
		return (string)$this->unreachableHosts;
	}
	
	public function getHostsOK() {
		return ((count($this->configInfo['members']) - ($this->downHosts + $this->unreachableHosts)));
	}
	
	public function getBackgroundImage() {
		if($this->notFound) {
			return "packages/gwwidgets/images/bg-gray.gif";
		}
		switch($this->status) {
			case 'UP':
				return "packages/gwwidgets/images/bg-green.gif";
				break;
			case 'UNREACHABLE':
			case 'DOWN':
				return "packages/gwwidgets/images/bg-red.gif";
				break;
			case 'PENDING':
				return "packages/gwwidgets/images/bg-yellow.gif";
				break;
		}
	}
	
	public function getIconImage() {
		if($this->notFound) {
			return "packages/gwwidgets/images/host-gray.gif";
		}
		switch($this->status) {
			case 'UP':
				return "packages/gwwidgets/images/host-green.gif";
				break;
			case 'UNREACHABLE':
			case 'DOWN':
				return "packages/gwwidgets/images/host-red.gif";
				break;
			case 'PENDING':
				return "packages/gwwidgets/images/host-yellow.gif";
				break;
		}
	}
	
	public function getStatus() {
		return $this->status;
	}
	
	public function update() {
		global $foundationModule;
		global $sv2;
		
		if(!$this->notFound) {
            try {		    
    		  $hostDAL = new HostDAL($foundationModule->getWebServiceURL());
    		  $hostInfo = $hostDAL->getHostByHostName($this->hostName);
		
              if ($hostInfo != null && !empty($hostInfo)) {
        		  switch($hostInfo['MonitorStatus']->MonitorStatusID) {
    			      case '7':
    					$this->status = 'UP';
    					break;
    				  case '8':
    					$this->status = 'PENDING';
    					break;
    				  case '2':
    					$this->status = 'DOWN';
    					break;
    				  case '3':
    					$this->status = 'UNREACHABLE';
    					break;
    			 }
              }
		    }
    		catch (DALException $dalEx)
    		{
    		    if ($sv2->getErrorOccurred() == false)
    		    {
        			$dialog = new ErrorDialog("An error occurred when connecting to the foundation webservice.  Please contact your system administrator for further assistance.");
        			$dialog->show();
        			$sv2->setErrorOccurred(true);
    		    }
    		}
    		catch (Exception $e)
    		{
    		    $dialog = new ErrorDialog("An error occurred while updating Host statistics.".$e->getMessage());
    		    $dialog->show();
    		    if ($sv2->getErrorOccurred() == false)
        		  $sv2->setErrorOccurred(true);
    		}
		}
	}
	
}


class GWWidgetsHostListWidget extends GuavaWidget implements ActionListener   {
	
	private $items;	// Array

	private $hosts;

	private $defaultRefreshRate = 30; //default refresh rate in seconds

	private $refreshRate;
	
	public function init() {
		$this->hosts = array();
		
		$this->update();
		
		$this->items = array();
		$this->setConfigClass("GWWidgetsHostListConfigureDialog");
	}
	
	public function unregister() {
		parent::unregister();
		foreach($this->items as $item) {
			$item->Destroy();
		}
		$this->items = array();
	}
	
	/**
	 * Enter description here...
	 *
	 * @param ActionEvent $event
	 */
	public function actionPerformed($event) {
		if($event->getAction() == "configured") {
			if(Count($this->items)) {
				foreach($this->items as $item) {
					$item->unregister();
				}
			}
			$this->items = array();
			$this->hosts = array();
			$tempList = $event->getSource()->getHostList();
			if(count($tempList)) {
				foreach($tempList as $host) {
					
 					if($this->hostExists($host)){					
						$tempHostItem = new GWWidgetsHostListWidgetItem($host);
						$this->hosts[] = $host;
						$this->items[] = $tempHostItem;
 					}
				}	
			}
		}
		$this->update();
	}
	
	private function hostExists($hostName){
		global $foundationModule;
		global $sv2;
		$exists = true;
		 try {
    		// Let's first check to see if host exists
    		$hostDAL = new HostDAL($foundationModule->getWebServiceURL());
    		
    		$hostInfo = $hostDAL->getHostByHostName($hostName);
        }
		catch (DALException $dalEx)
		{
			$exists = false;
		    if ($sv2->getErrorOccurred() == false)
		    {
    			$dialog = new ErrorDialog("An error occurred when connecting to the foundation webservice.  Please contact your system administrator for further assistance.");
    			$dialog->show();
    			$sv2->setErrorOccurred(true);
		    }
		}
		catch (Exception $e)
		{
			$exists = false;
		    $dialog = new ErrorDialog("Unable to retrieve host ".$this->hostName);
		    $dialog->show();
		    if ($sv2->getErrorOccurred() == false)
    		  $sv2->setErrorOccurred(true);
		}
		
		if($hostInfo == null || empty($hostInfo)) {
			// Host does not exist
			$exists = false;
	 
		}
 
		return $exists;
		
	}
	public function getHostList() {
		return $this->hosts;
	}
	
	public function getConfigObject() {
		return $this->hosts;
	}
	
	public function loadConfig($configObject) {
		// configObject should be an array of Host names
		foreach($configObject as $host) {
			if($this->hostExists($host)){
				$tempHostItem = new GWWidgetsHostListWidgetItem($host);
				$this->hosts[] = $host;
				$this->items[] = $tempHostItem;
			}
		}
		$this->update();
	}
	
	public function update() {
		global $guava;
		$guava->console("Updating Host List Widget.");
		if(count($this->items)) {
			foreach($this->items as $item) {
				$item->update();
			}
		}
		// Let's build our target
		ob_start();
		if(count($this->items)) {
			foreach($this->items as $item) {
				$item->Draw();
			}
		}
		$buffer = ob_get_contents();
		ob_end_clean();
		$this->targetData("contents", $buffer);
	}
	
	public function Draw() {
		$this->printTarget("contents");
		
	}
	
}

?>
