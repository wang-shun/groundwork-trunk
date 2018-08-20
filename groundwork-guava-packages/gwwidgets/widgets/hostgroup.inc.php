<?php
/*
Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
All rights reserved. Use is subject to GroundWork commercial license terms. 
*/

require_once('DAL/HostGroupDAL.inc.php');
require_once('DAL/StatisticsDAL.inc.php');

class GWWidgetsHostGroupListConfigureDialog extends GuavaWidgetConfigureDialog implements ActionListener  {
	
	private $hostgroupNames;
	
	private $hostgroupButtons;
	private $selectRefresh;
	private $inputHostGroup;
	private $addButton;
	
	public function __construct($source) {
		parent::__construct($source);
		
		$this->hostgroupNames = $source->getHostgroupList();
		
		
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
	
	
		
		$this->inputHostGroup =  new InputTextSuggestControl("HostGroup",$this); //InputText(50, 255);
		
		$this->addButton = new Button("Add Hostgroup");
		$this->addButton->addActionListener("click", $this);
	}
	
	public function unregister() {
		parent::unregister();
		$this->inputHostGroup->unregister();
		$this->addButton->unregister();
	}
	
	public function getHostgroupList() {
		return $this->hostgroupNames;
	}
	
	public function actionPerformed($event) {
		global $foundationModule;
		parent::actionPerformed($event);
		$hostGroup = strtolower($this->inputHostGroup->getValue());
		
		if($event->getSource() === $this->addButton) {
			// We want to add a hostgroup
			if(($hostGroup == NULL) || (strlen($hostGroup) == 0)) {
				$dialog = new ErrorDialog("Hostgroup Name cannot be blank.");
				$dialog->show();
				return;
			}
			if(in_array($hostGroup, $this->hostgroupNames)) {
				$dialog = new ErrorDialog("Hostgroup already exists in list.");
				$dialog->show();
				return;				
			}
			
			
			
        try {
    		// Let's first check to see if host exists
    		$hostGroupDAL = new HostGroupDAL($foundationModule->getWebServiceURL());
    		
    		$hostGroupInfo = $hostGroupDAL->getHostGroupByName($hostGroup);
        }
		catch (Exception $e)
		{
		    $dialog = new ErrorDialog("Unable to retrieve hostgroup ".$this->hostName);
		    $dialog->show();
		    if ($sv2->getErrorOccurred() == false)
    		  $sv2->setErrorOccurred(true);
		}
		
		if($hostGroupInfo == null || empty($hostGroupInfo)) {
			// Host does not exist
			$err = new ErrorDialog("HostGroup does not exist.");
	 		$err->show();
	 		return;
		}
			
			
			/**
			 * @todo We should check for hostgroup name validity here
			 */
			$this->hostgroupNames[] = $hostGroup;
			$this->rebuildListTarget();
			//reset input field to empty after hostgroup is added
			$this->inputHostGroup->setValue("");
		}
		else {
			if(count($this->hostgroupButtons)) {
				foreach($this->hostgroupButtons as $key => $button) {
					if($event->getSource() === $button) {
						$index = array_search($key, $this->hostgroupNames);
						unset($this->hostgroupNames[$index]);
						$button->unregister();
						unset($this->hostgroupButtons[$key]);
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
		foreach($this->hostgroupNames as $hostgroup) {
			if(isset($this->hostgroupButtons[$hostgroup])) {
				$this->hostgroupButtons[$hostgroup]->unregister();
			}
			$this->hostgroupButtons[$hostgroup] = new TextLink("Delete");
			$this->hostgroupButtons[$hostgroup]->addActionListener("click", $this);
			$buffer .= '<div style="background: #dddddd; height: 25px; border-width: 1px 0px 1px 0px; border-style: solid; border-bottom-color: black; border-left-color: grey; border-right-color: black; border-top-color: grey;"><table width="100%"><tr><td>'.$hostgroup . '</td><td align="right">' . $this->hostgroupButtons[$hostgroup]->toString() . '</td></tr></table></div>';

		}
		$this->targetData("hostgroupList", $buffer);
	}
	
	public function Draw() {
		?>
		<h1 align='center'>Host Group List Configuration</h1>
		<br/>
		<h1>Specify An Additional Hostgroup:</h1>
		<?php 
			$this->inputHostGroup->Draw();
		?><br> 
		<div id="search_suggest">
			</div> 
		<?=$this->addButton->Draw(); ?> <br />
		<br />
		<h1>Current Hostgroup List</h1>
		<div style="height: 200px; border: 1px solid grey; overflow: auto;">
		<?=$this->printTarget("hostgroupList");?><br />
		<br />
		</div>
		<h1>Set the Refresh Rate:</h1>
		<?php
		$this->selectRefresh->Draw();
		?>
		<?php
	}
	
}

class GWWidgetsHostGroupListWidgetItem extends GuavaObject implements ActionListener {
	
	private $hostgroupName;
	
	private $downHosts, $unreachableHosts;
	
	private $configInfo;
	
	private $notFound;
	
	private $hostgroupLink;
	
	public function __construct($hostgroupName) {
		global $foundationModule;
		global $sv2;
				
		parent::__construct();
		
		$this->hostgroupName = $hostgroupName;
		
		
		$this->setTemplate(GUAVA_FS_ROOT . 'packages/gwwidgets/templates/hostgrouplistitem.xml');
		
		$this->bind("self", $this);
		
		$this->hostgroupLink = new TextLink($this->hostgroupName);
		$this->hostgroupLink->addActionListener("click", $this);
		$this->bind("hostgroupLink", $this->hostgroupLink);
		
		try {
            $hostGroupDAL = new HostGroupDAL($foundationModule->getWebServiceURL());
            // do a deep retrieval to get host info back - no need to do another call 
            $hgInfo = $hostGroupDAL->getHostGroupByName($this->hostgroupName, true);
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
		
		if(empty($hgInfo)) {
			// Hostgroup does not exist
			$this->notFound = true;
		}
		else {
			$this->notFound = false;	
			
			$this->hostgroupName = $hgInfo['Name'];
			
    		$tempMemberList = $hgInfo['Hosts'];
    		if(count($tempMemberList)) {
    			foreach($tempMemberList as $member) {
    				$this->configInfo['members'][] = $member['Name'];
					$this->memberHostIDs[] = $member['HostID'];
    			}
    		}
		}
		$this->update();
	}
	
	public function unregister() {
		parent::unregister();
		$this->hostgroupLink->unregister();
	}
	
	public function actionPerformed($event) {
		global $sv2;
		global $guava;
		if(isset($sv2) && !$this->notFound) {
			$component = $sv2->createHostGroupComponent($this->hostgroupName);
			$component->expand();
			
			$component->setCloned(true);
			
			$guava->objectView($component);
		}
	}
	
	public function getHostgroup() {
		return $this->hostgroupName;
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
		if($this->downHosts || $this->unreachableHosts) {
			return "packages/gwwidgets/images/bg-red.gif";
		}
		else {
			return "packages/gwwidgets/images/bg-green.gif";
		}
	}
	
	public function getIconImage() {
		if($this->notFound) {
			return "packages/gwwidgets/images/hostGroup-gray.gif";
		}
		if($this->downHosts || $this->unreachableHosts) {
			return "packages/gwwidgets/images/hostGroup-red.gif";
		}
		else {
			return "packages/gwwidgets/images/hostGroup-green.gif";
		}
	}
	
	public function update() {
		global $foundationModule;
		global $sv2;
		
		if(!$this->notFound) {
		    try {		    
 		      $statisticDAL = new StatisticsDAL($foundationModule->getWebServiceURL());
		      $hostStatistics = $statisticDAL->getHostStatisticsByHostGroupName($this->hostgroupName);
		      if ($hostStatistics != null) {
    		      $counts = $hostStatistics;
    		      $this->downHosts = $counts['DOWN'];
    		      $this->unreachableHosts = $counts['UNREACHABLE'];
    			  $this->targetData("down", (string)$this->downHosts);
    			  $this->targetData("unreachable", (string)$this->unreachableHosts);
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
		    catch (Exception $e) {
		        $dlg = new ErrorDialog("An error occurred while updating HostGroup statistics.".$e->getMessage());
		        $dlg->show();
		    }
		}
		
	}
	
}


class GWWidgetsHostGroupListWidget extends GuavaWidget implements ActionListener   {
	
	private $items;	// Array

	private $hostgroups;
	 
	private $refreshRate;

	public function init() {
		$this->hostgroups = array();
		
		$this->update();
		
		$this->items = array();
		
		$this->setConfigClass("GWWidgetsHostGroupListConfigureDialog");
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
			if(count($this->items)) {
				foreach($this->items as $item) {
					$item->unregister();
				}
			}
			$this->items = array();
			$this->hostgroups = array();
			$tempList = $event->getSource()->getHostgroupList();
			if(count($tempList)) {
				foreach($tempList as $hostgroup) {
					$tempHostgroupItem = new GWWidgetsHostGroupListWidgetItem($hostgroup);
					$this->hostgroups[] = $hostgroup;
					$this->items[] = $tempHostgroupItem;
				}	
			}
		}
		$this->update();
	}
	
	public function getHostgroupList() {
		return $this->hostgroups;
	}
	
	public function getConfigObject() {
		return $this->hostgroups;
	}
	
	public function loadConfig($configObject) {
		// configObject should be an array of hostgroup names
		foreach($configObject as $hostgroup) {
			$tempHostgroupItem = new GWWidgetsHostGroupListWidgetItem($hostgroup);
			$this->hostgroups[] = $hostgroup;
			$this->items[] = $tempHostgroupItem;
		}
		$this->update();
	}
	
	public function update() {
		global $guava;
		$guava->console("Updating Hostgroup List Widget.");
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
