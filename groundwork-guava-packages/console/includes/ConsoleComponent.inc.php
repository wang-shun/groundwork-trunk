<?php
/*
Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
All rights reserved. Use is subject to GroundWork commercial license terms. 
*/ 

/**
 * @deprecated 
 *
 */
class ConsoleHostGroupContainer extends Container {
	private $hostgroup_name;
	private $hostgroupInfo;
	
	public function createComponents($startRange, $endRange) {
		$this->removeChildren();	// Let's get rid of the children (forcefully)
 
		if($this->hostgroupInfo) {
			$tempComponent = new ConsoleComponent();
			$tempComponent->queryForHostGroupEvents($this->hostgroupInfo);
			$this->addChild($tempComponent);

		}
	}
 
	public function setParent($parentComponent) {
		global $foundationDB;
		$this->parentComponent = $parentComponent;
		
		// Okay, let's ask our service for our host and service name
		$this->hostgroup_name = $parentComponent->getConfigInfo("hostgroup_name");
				
		// Get host group object
		$tempQuery = new CollageHostGroupQuery($foundationDB);
		$this->hostgroupInfo = $tempQuery->getHostGroup($this->hostgroup_name);

	}	
	
	public function unregister() {
		parent::unregister();
		$this->hostgroup_name = null;
		$this->hostgroupInfo = null;
	}
}

/**
 * @deprecated 
 *
 */
class ConsoleHostContainer extends Container {
	private $host_name;
	private $hostInfo;
	
	public function createComponents($startRange, $endRange) {
		$this->removeChildren();	// Let's get rid of the children (forcefully)
 
		if($this->hostInfo) {
			$tempComponent = new ConsoleComponent();
			$tempComponent->queryForHostEvents($this->hostInfo);
			$this->addChild($tempComponent);

		}
	}
 
	public function setParent($parentComponent) {
		global $foundationDB;
		$this->parentComponent = $parentComponent;
		
		// Okay, let's ask our service for our host and service name
		$this->host_name = $parentComponent->getConfigInfo("host_name");
		// Get host object
		$tempQuery = new CollageHostQuery($foundationDB);
		$this->hostInfo = $tempQuery->getHost($this->host_name);

	}
	
	public function unregister() {
		parent::unregister();
		$this->host_name = null;
		$this->hostInfo = null;
	}
}

/**
 * @deprecated 
 *
 */
class ConsoleServiceContainer extends Container {
	private $service_name;
	private $host_name;
	private $serviceInfo;
	private $hostInfo;
	
	public function createComponents($startRange, $endRange) {
		$this->removeChildren();	// Let's get rid of the children (forcefully)
 
		if($this->serviceInfo) {
			$tempComponent = new ConsoleComponent();
			$tempComponent->queryForServiceEvents($this->serviceInfo, $this->hostInfo);
			$this->addChild($tempComponent);

		}
	}
 
	public function setParent($parentComponent) {
		global $foundationDB;
		$this->parentComponent = $parentComponent;
		
		// Okay, let's ask our service for our host and service name
		$this->service_name = $parentComponent->getConfigInfo("service_description");
		$this->host_name = $parentComponent->getConfigInfo("host_name");
				
		// Get host object
		$tempQuery = new CollageServiceQuery($foundationDB);
		$this->serviceInfo = $tempQuery->getService($this->service_name, $this->host_name);
		// Get host object
		$tempQuery = new CollageHostQuery($foundationDB);
		$this->hostInfo = $tempQuery->getHost($this->host_name);

	}	
	
	public function unregister() {
		parent::unregister();
		$this->service_name = null;
		$this->host_name = null;
		$this->serviceInfo = null;
	}
}

/**
 * @deprecated 
 *
 */
class ConsoleComponent extends Component {
	
	private $consoleObject;
	
	private $contextType = 'all';
	private $consoleContext = array();
	
	function __construct() {
		global $foundationDB;
		parent::__construct(true);				
		
		
		$this->consoleObject = new ConsoleObject();
		
		// turns Ajax refresh on
		$this->consoleObject->addRefreshButton();
		
		// default
		$this->consoleObject->queryForAllEvents();
		
	}
	
	private function getConsoleObject() {
		return $this->consoleObject;
	}
	
	public function titlebar() {
		?>
		Console Component
		<?php
	}
	
	public function queryForServiceEvents($serviceInfo, $hostInfo) {
		$this->getConsoleObject()->queryForServiceEvents($serviceInfo, $hostInfo, $this->getApplicationType());
		$this->contextType = 'service';
		$this->consoleContext['ServiceInfo'] = $serviceInfo;
		$this->consoleContext['HostInfo'] = $hostInfo;
	}
	
	public function queryForHostEvents($hostInfo) {
		$this->getConsoleObject()->queryForHostEvents($hostInfo['HostID'], $this->getApplicationType());
		$this->contextType = 'host';
		$this->consoleContext['HostInfo'] = $hostInfo;
	}
	
	public function queryForHostGroupEvents($hostgroupInfo) {
		$this->getConsoleObject()->queryForHostGroupEvents($hostgroupInfo['HostGroupID'], $this->getApplicationType());
		$this->contextType = 'hostgroup';
		$this->consoleContext['HostGroupInfo'] = $hostgroupInfo;
	}
	
	public function unregister() {
		// This gets called whenever someone moves away from your view (clicks on another tab)
		
		parent::unregister();
		$this->consoleObject->unregister();
	}

	public function refresh() {
		// This function gets called whenever the view is refreshed either through a browser refresh or 
		// through ajax requests.  But we don't want to do it if we're processing something from the url
	}
	
	
	public function expand() {
		// Stub
		
		$this->expanded = true;
	}
	

	public function display() {
		?><div style="width: 98%" align="left"><div style="overflow: scroll; width: 100%; height: 300px;">
		<? $this->consoleObject->Draw(); ?></div></div>
		<?php		
	}
	
	function componentClone() {
		$tempComponent = new ConsoleComponent();
		
		switch($this->contextType) {
			case 'all':
				$tempComponent->getConsoleObject()->queryForAllEvents();
				break;
			case 'service':
				$tempComponent->queryForServiceEvents($this->consoleContext['ServiceInfo'], $this->consoleContext['HostInfo']);
				break;
			case 'host':
				$tempComponent->queryForHostEvents($this->consoleContext['HostInfo']);
				break;
			case 'hostgroup':
				$tempComponent->queryForHostGroupEvents($this->consoleContext['HostGroupInfo']);
				break;
			default:
				$tempComponent->getConsoleObject()->queryForAllEvents();
				break;
		}
		
		return $tempComponent;
	}
}


?>