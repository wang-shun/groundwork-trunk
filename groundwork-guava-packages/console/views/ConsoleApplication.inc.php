<?php
/*
Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
All rights reserved. Use is subject to GroundWork commercial license terms. 
*/ 

require_once(GUAVA_FS_ROOT . 'packages/console/includes/ConsoleObject.inc.php');
require_once(GUAVA_FS_ROOT . 'packages/console/includes/ConsoleComponent.inc.php');
require_once('DAL/HostGroupDAL.inc.php');
require_once('DAL/MetaDataDAL.inc.php');

/**
 * This class represents the Console Application as it appears in Groundwork Monitor Professional.
 * It uses the ConsoleObject class to control what will be displayed in the Application.
 *
 * @author Robin Dandridge
 */
class ConsoleApplication extends GuavaApplication 
{
	private $consoleObject;
	private $applicationTypes;
	private $hostGroups;
	
	public function __construct() {
		global $guava;
		
		parent::__construct();
				
		$this->sideNavEnable(true);	// Only call this if you want the sideNav panel
		$this->sideNavDestroyAll();
	}
	
	public function init() {
		global $guava;
		global $foundationModule;
		
		if (!isset($this->consoleObject))
			$this->consoleObject = new ConsoleObject();
			
		// This gets called if the person clicks on the tab to activate this view
		// If you called sideNavEnable, you can create the toplevel node with this method
		$topNavNode = new NavNode("All Open Events"); // NavNode is the object to represent a node in the navigation
		$topNavNode->addClickListener("top",$this,"executeSideNavLink","top");				
		$this->sideNavCreate("eventTree", $topNavNode);
		 
		// Create navigation nodes for events by application
		$appsNavNode = new NavNode("Applications");
		$this->sideNavCreate("applicationTree", $appsNavNode);
		$appsNavNode->addClickListener("apps", $this, "executeSideNavLink", "apps");
		 
		 // Query for ApplicationTypes to create tree
        try {
		     $metaDataDAL = new MetaDataDAL($foundationModule->getWebServiceURL());
		     $applicationTypes = $metaDataDAL->getApplicationTypes();
		     if ($applicationTypes['TotalCount']>1) {
		 	   foreach($applicationTypes['ApplicationTypes'] as $application) {
                    $appNode = new NavNode($application['Name']);
                    $appNode->addClickListener($application['Name'],$this,"executeSideNavLink","app_".$application['ApplicationTypeID']);
                    $appsNavNode->addNode($appNode);
                    $this->applicationTypes[$application['ApplicationTypeID']] = $application['Name'];
		 	   }
		 	} 		 	    
		}
	 	catch(Exception $ex) {
            $guava->console("Error getting app types ".$ex->getMessage());
		    $dlg = new ErrorDialog("An error occurred when connecting to the foundation webservice.  Please contact your system administrator for further assistance.");
		    $dlg->show();
		 }
		 
		 // create navigation nodes for events by hostgroup
		 $hgNavNode = new NavNode("HostGroups");
		 $this->sideNavCreate("hgTree",$hgNavNode);
		 $hgNavNode->addClickListener("hostgroups", $this, "executeSideNavLink", "hostgroups");
		 try {
		     $hgDAL = new HostGroupDAL($foundationModule->getWebServiceURL());
		     $hostGroups = $hgDAL->getHostGroups();
		     // only create the hostgroup tree if hostgroups exist.
		     if ($hostGroups['Count'] > 0) {
		 	    foreach($hostGroups['HostGroups'] as $hostGroup) {
		 		    $hgNode = new NavNode($hostGroup['Name']);
		 		    $hgNode->addClickListener($hostGroup['Name'],$this,"executeSideNavLink","hostgroup_".$hostGroup['HostGroupID']);
		 		    $hgNavNode->addNode($hgNode);
		 		    $this->hostGroups[$hostGroup['HostGroupID']] = $hostGroup['Name'];
		 	    }
		 	}
		 }
		 catch (Exception $ex) {
            $guava->console("Error getting hostgroups ".$ex->getMessage());
		    $dlg = new ErrorDialog("An error occurred when connecting to the foundation webservice.  Please contact your system administrator for further assistance.");
		    $dlg->show();
		 }
		 
		 // the full blown console application needs all the bells and whistles
		 $this->consoleObject->addRefreshButton();
		 $this->consoleObject->enableAcceptButtons();
		 $this->consoleObject->enableAdvancedSorting();
		 $this->consoleObject->setEventsPerPage(50);
		 
		 // initial display
		$this->consoleObject->getAllEvents();
	}
	
	public function executeSideNavLink($guavaObject, $command = null) {
	    global $guava;
	    
		if ($command == "top") {
			$this->consoleObject->getAllEvents();			
		}
		else if (stripos($command, "hostgroup_") === 0) {
			$hostGroupID = substr($command, 10);
			$this->consoleObject->getHostGroupEvents($hostGroupID, $this->hostGroups[$hostGroupID]);
		}
		else if (stripos($command, "hostgroups") === 0) {
			$hgNav = $this->getSideNav("hgTree");
			if ($hgNav->isExpanded()) 
				$hgNav->Collapse();
			else
				$hgNav->Expand();
		}
		else if (stripos($command,"app_") === 0) {
			// only care about everything after "app_"
			$appID = substr($command, 4);
			$this->consoleObject->getApplicationEvents($appID,$this->applicationTypes[$appID]);
		}
		else if (stripos($command, "apps") === 0) {
			$appNav = $this->getSideNav("applicationTree");
			if ($appNav->isExpanded()) 
				$appNav->Collapse();
			else
				$appNav->Expand();
		}
		
	}
	
	public function close() {
		if (isset($this->consoleObject)) {
			$this->consoleObject->unregister();
			$this->consoleObject = null;
		}
		$this->sideNavDestroyAll();
		$this->applicationTypes = null;
	}
	
	public function menuCommand($command) {
		// empty - no menu options for this application
	}
	
	public function Draw() {
		$this->consoleObject->Draw();		
	}
}

?>