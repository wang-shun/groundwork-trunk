<?php
/*
Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
All rights reserved. Use is subject to GroundWork commercial license terms. 
*/

class GWWidgetsHostGroupListWidgetItem extends GuavaObject {
	
	private $hostgroup;
	
	private $downQuery, $unreachableQuery;
	
	private $downHosts, $unreachableHosts;
	
	private $configInfo;
	
	public function __construct($hostgroup) {
		global $foundationDB;
		
		parent::__construct();
		
		$this->hostgroup = $hostgroup;
		
		
		$this->setTemplate(GUAVA_FS_ROOT . 'packages/widgets/templates/hostgrouplistitem.xml');
		
		$this->bind("self", $this);
		
		
		$tempQuery = new CollageHostQuery($foundationDB);
		$tempHostGroupQuery = new CollageHostGroupQuery($foundationDB);		
		$tempMemberList = $tempHostGroupQuery->getHostsForHostGroup($this->hostgroup);
		if(count($tempMemberList)) {
			foreach($tempMemberList as $member) {
				$tempHostInfo = $tempQuery->getHostByID($member['HostID']);
				$this->configInfo['members'][] = $tempHostInfo['HostName'];
			}
		}
		

		if(count($this->configInfo['members'])) {
			foreach($this->configInfo['members'] as $member) {
				$tempHostInfo = $tempQuery->getHost($member);
				$this->memberHostIDs[] = $tempHostInfo['HostID'];
				if(isset($this->memberSubQuery)) {
					$this->memberSubQuery .= "OR HostStatusID = ".$tempHostInfo['HostID'] ." ";
				}
				else {
					$this->memberSubQuery .= "HostStatusID = ".$tempHostInfo['HostID'] ." ";
				}
			}
			$this->downQuery = "SELECT count(*) FROM HostStatus WHERE (".$this->memberSubQuery .") AND MonitorStatusID = '2'";
			$this->unreachableQuery = "SELECT count(*) FROM HostStatus WHERE (".$this->memberSubQuery .") AND MonitorStatusID = '3'";
		}
		
		$this->update();
	}
	
	public function getHostgroup() {
		return $this->hostgroup;
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
	
	public function getBackgroundColor() {
		if($this->downHosts || $this->unreachableHosts) {
			return "#ff6666";
		}
		else {
			return "#66ff66";
		}
	}
	
	public function update() {
		global $foundationDB;
			
		$downResult = $foundationDB->selectQuery($this->downQuery);
		$unreachableResult = $foundationDB->selectQuery($this->unreachableQuery);

		$this->downHosts = $downResult[0]['count(*)'];
		
		$this->unreachableHosts = $unreachableResult[0]['count(*)'];

		$this->targetData("down", (string)$this->downHosts);
		$this->targetData("unreachable", (string)$this->unreachableHosts);
		
	}
	
}


class GWWidgetsHostGroupListWidget extends GuavaWidget  {
	
	private $items;	// Array
	
	private $timer;
	
	public function init() {
		$this->hostgroups = array();
		
		$this->update();
		
		$this->items = array();
		
		$this->timer = new GuavaTimer(0, 10, $this, "update");
	}
	
	public function Destroy() {
		$this->timer->disable();
		foreach($this->items as $item) {
			$item->Destroy();
		}
		$this->items = array();
	}
	
	public function loadConfig($configObject) {
		// configObject should be an array of hostgroup names
		foreach($configObject as $hostgroup) {
			$tempHostgroupItem = new GWWidgetsHostGroupListWidgetItem($hostgroup);
			$this->items[] = $tempHostgroupItem;
		}
		
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