<?php
/*
Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
All rights reserved. Use is subject to GroundWork commercial license terms. 
*/ 

require_once('DAL/EventDAL.inc.php');
require_once('EventCheckBox.inc.php');
require_once('SortDialog.inc.php');

define('DEFAULT_SORTCOLUMN', 'reportDate');
define('DEFAULTEVENTVIEW', 'ALL OPEN EVENTS');
define('APPLICATIONEVENTVIEW', 'APPLICATION TYPE');
define('HOSTGROUPEVENTVIEW', 'HOSTGROUP');
define('HOSTEVENTVIEW', 'HOST');
define('SERVICEEVENTVIEW', 'SERVICE');

class ConsoleObject extends GuavaObject implements GuavaMessageHandler  
{
    private $eventDal;
    
	private $consolePager = 0;
	private $consoleNumPerPage = 20;	// default value
	
	private $mainForm;
	private $hostTextLink;
	private $msgCountTextLink;
	private $reportDtTextLink;
	private $monitorStatusTextLink;
	private $severityTextLink;
	private $appNameTextLink;
	private $lastInsertTextLink;
	private $firstInsertTextLink;
	
	private $nextLink;
	private $prevLink;
	private $isRefreshButtonEnabled = false;
	private $refreshOn = false;
	
	private $timer;
	
	private $eventView = DEFAULTEVENTVIEW;
	private $eventDescription = " ";
	
	private $appTypeId;
	private $hgId;
	private $hostId;
	private $serviceDescription;
	
	private $isAcceptEnabled = false;
	private $acceptAll;
	private $checkBoxSubmitButton;
	private $checkBoxes;
	private $serviceDAL;
	private $sortItems = array(DEFAULT_SORTCOLUMN => false);
	
	private $advancedSortButton;
	private $sortDialog;
	
	private $svloaded = false;
	
	private $preDefinedColumns = array("Select Column..." => "No Table", "Device" => "device.displayName", "Msg Count" => "msgCount", "Report Date" => "reportDate", "Status" => "monitorStatus.name", "Severity" => "severity.name", "Application Type" => "applicationType.name", "Last Inserted" => "lastInsertDate", "First Inserted" => "firstInsertDate");
	public function __construct() 
	{
		global $guava;
		global $foundationModule;
		global $sv2;
		
		parent::__construct("Console", true);					
		
		// Create console context (if it doesn't exist)
		$processor = $guava->getProcessor();
		$processor->addContext("console");
		$processor->addContextListener("console", $this);
		
		if (isset($sv2)) {
			$this->svLoaded = true;
		}
		
        try {
		  $this->eventDal = new EventDAL($foundationModule->getWebServiceURL());	
		}
		catch (DALException $ex) {
		    $guava->console("Error creating EventDAL - no webservice url provided");
		    $guava->console($ex->getMessage());
		    $dlg = new ErrorDialog("An Internal Error Occurred - please contact your system administrator for assistance.");
		    $dlg->show();
		}
		try{
		  $this->serviceDAL = new ServiceDAL($foundationModule->getWebServiceURL());
			}
		catch(DALException $ex){
			$dlg = new ErrorDialog("An Internal Error Occurred - please contact your system administrator for assistance.");
		    $dlg->show();
		} 
		
		$this->hostTextLink = new TextLink("Device");
		$this->hostTextLink->addClickListener('deviceSort', $this, "sortData", array('orderBy' => "device.displayName", 'sortOrder' => true));
		
		$this->msgCountTextLink = new TextLink("Msg Count");
		$this->msgCountTextLink->addClickListener('msgCountSort', $this, "sortData", array('orderBy' => "msgCount", 'sortOrder' => true));
		
		$this->reportDtTextLink = new TextLink("Report Date");
		$this->reportDtTextLink->addClickListener('reportDateSort', $this, "sortData", array('orderBy' => "reportDate", 'sortOrder' => false));
				
		$this->monitorStatusTextLink = new TextLink("Status");
		$this->monitorStatusTextLink->addClickListener('monStatSort', $this, "sortData", array('orderBy' => "monitorStatus.name", 'sortOrder' => true));
		
		$this->severityTextLink = new TextLink("Severity");
		$this->severityTextLink->addClickListener('sevSort', $this, "sortData", array('orderBy' => "severity.name", 'sortOrder' => true));
		


		
		$this->lastInsertTextLink = new TextLink("Last Inserted");
		$this->lastInsertTextLink->addClickListener('lastInsertSort', $this, "sortData", array('orderBy' => "lastInsertDate", 'sortOrder' => false));
		
		$this->firstInsertTextLink = new TextLink("First Inserted");
		$this->firstInsertTextLink->addClickListener('firstInsertSort', $this, "sortData", array('orderBy' => "firstInsertDate", 'sortOrder' => false));
		
		$this->nextLink = new TextLink("Next");
		$this->nextLink->addClickListener('nextPage', $this, "incrementconsolePager");
		
		$this->prevLink = new TextLink("Prev");
		$this->prevLink->addClickListener('prevPage', $this, "decrementconsolePager");		

		$this->mainForm = new Form();
		$this->mainForm->addListener('mainform', $this, 'acceptEvents');
		
		$this->timer = new GuavaTimer(0, 10, $this, "refresh");	
	}	
	
	/** 
	 * Set the number of events that will be displayed in the console window
	 * 
	 * @param int $eventsPerPage
	 */
	public function setEventsPerPage($eventsPerPage) {
		if ($this->isAcceptEnabled && (count($this->checkBoxes) < $eventsPerPage)) {
			// add more check boxes if necessary
			for($counter = $this->consoleNumPerPage; $counter < $eventsPerPage; $counter++) {
				$this->checkBoxes[$counter] = new EventCheckBox("checkbox_" . $counter);
				$this->checkBoxes[$counter]->setValue(false);
			}
		}
		$this->consoleNumPerPage = $eventsPerPage;
	}
	
	/**
	 * Call this function to add the column to the console that allows events to be
	 * cleared from the console.
	*/
	public function enableAcceptButtons() {
		$this->isAcceptEnabled = true;
		
		$this->checkBoxSubmitButton = new SubmitButton('Submit');
		$this->acceptAll = new CheckBox("Accept All");
		$this->acceptAll->addClickListener('acceptall', $this, "acceptAll");
		
		// Create our checkboxes
		for($counter = 0; $counter < $this->consoleNumPerPage; $counter++) {
			$this->checkBoxes[$counter] = new EventCheckBox("checkbox_" . $counter);
			$this->checkBoxes[$counter]->setValue(false);
		}
		
	}
	
	/**
	 * Sets all check boxes on the current page to "checked"
	 *
	 * @param unknown_type $guavaObject
	 * @param unknown_type $parameter
	 */
	public function acceptAll($guavaObject, $parameter = null) {
		// Our listener for the accept all checkbox
		// Let's get our checkvalue
		$checked = $guavaObject->isChecked();
		if(count($this->checkBoxes)) {
			foreach($this->checkBoxes as $checkbox) {
				$checkbox->setValue($checked ? true : false);
			}
		}
	}
	
	/**
	 * Sends the information about the events that have been selected to be cleared ("accepted")
	 * from the Console to the foundation feeder.
	 *
	 */
	public function acceptEvents() {
		global $guava;
		global $foundationModule;
		
		if (count($this->checkBoxes)) {
			$feederURL = $foundationModule->getFeederURL();
			// url should be something like: tcp://192.168.2.164:4913
			if(!$foundationSocket = @stream_socket_client($feederURL, $errorNumber, $errorString, 5)) {
				$guava->console("Error Connection To Foundation Socket: " . $errorNumber . ": " . $errorString);
			}
			else {
				// We've got our socket!
				// Let's create our dom
				$foundationSession = microtime();
				$checkboxCount = count($this->checkBoxes);
				for ($i = 0; $i < $checkboxCount; $i++) {
					if ($this->checkBoxes[$i]->getIsChecked()) {
						/*
						Send XML to feeder to set operation status to ACCEPTED
						An example:
						<ADMIN SessionID="1234" Action="modify" Type="LogMessage"  LogMessageID="1212" OperationStatus="ACCEPTED" />
						*/
						$dom = new DOMDocument('1.1', 'iso-8859-1');
						$domRoot = $dom->appendChild($dom->createElement('admin'));
						$domRoot->setAttribute('SessionID', $foundationSession);
						$domRoot->setAttribute('Action', 'modify');
						$domRoot->setAttribute('Type', 'LogMessage');
						$domRoot->setAttribute('LogMessageID', $this->checkBoxes[$i]->getEventId());
						$domRoot->setAttribute('OperationStatus', 'ACCEPTED');
						$domOutput = $dom->saveXML($domRoot);
						fprintf($foundationSocket, $domOutput . "\n\n\r");
						$this->checkBoxes[$i]->setValue(false);
					}
				}
				fclose($foundationSocket);
				
				// force refresh back on to work around weird problem where there's a delay in the clearing
				// of messages.
				$this->refreshOn = true;
				$this->timer->enable();
				$this->refreshButton->setLabel('De-Activate Refresh');
				$this->getEvents($this->sortItems);				
			}
		}
	}
	
	/**
	 * Call this function to enable the advanced sorting options in the console.
	*/
	public function enableAdvancedSorting() {
		$this->isAdvancedSortingEnabled = true;
		
		$this->advancedSortButton = new Button('Sort Options');
		$this->advancedSortButton->addClickListener('sortsetup', $this, 'setupAdvancedSort');
				
	}
	
	/**
	 * Sets up and displays the dialog for advanced sorting.
	 *
	 */
	public function setupAdvancedSort() {
		$this->sortDialog = new SortDialog($this);
		
		// while the sort dialog is open, disable refresh 
		//so it won't slow things down
		if ($this->isRefreshButtonEnabled && $this->refreshOn) {
			if (isset($this->timer) && $this->timer->isEnabled())
				$this->timer->disable();
		}
		$this->sortDialog->show();
	}
	
	/**
	 * gets the results from the advanced sort dialog and processes them.
	 *
	 */
	public function sort() {
	    
		$firstSortColumn = $this->sortDialog->getFirstSortOption();

		if (strcasecmp($firstSortColumn, "Select Column...") != 0) 
			$sortSelectionOne = $this->preDefinedColumns[$firstSortColumn];
		else 
			$sortSelectionOne = null;
		$secondSortColumn = $this->sortDialog->getSecondSortOption();
		if (strcasecmp($secondSortColumn, "Select Column...") != 0)
			$sortSelectionTwo = $this->preDefinedColumns[$secondSortColumn];
		else 
			$sortSelectionTwo = null;
			
		// handle the case where the user selects the same column for both.	
		if (strcasecmp($sortSelectionOne, $sortSelectionTwo) == 0)
			$sortSelectionTwo = null;

        if ($sortSelectionOne != null)
        {
    		  $this->sortItems = array($sortSelectionOne => true);
    		  if ($sortSelectionTwo != null)
    		  {
    		      $this->sortItems[$sortSelectionTwo] = true;
    		  }
        }
        else {
            if ($sortSelectionTwo != null)
    		  {
    		      $this->sortItems = array($sortSelectionTwo => true);
    		  }
        }
        
		// if we turned refresh off, turn it back on
		if ($this->isRefreshButtonEnabled && $this->refreshOn) {
			if (isset($this->timer) && (!$this->timer->isEnabled()))
				$this->timer->enable();
		}
				
        $this->getEvents($this->sortItems);
		$this->sortDialog->unregister();				
	}
	
	/**
	 * Provides access to the list of columns that are sortable
	 *
	 * @return List of column names
	 */
	public function getColumnNames() {
		$colNames = array();
		foreach($this->preDefinedColumns as $displayName => $tableName) {
			$colNames[] = $displayName;
		}
		return $colNames;
	}
	
	/**
	 * Call this function to add the Refresh on/off button to the console page.
	 */
	public function addRefreshButton() {
		
		$this->isRefreshButtonEnabled = true;
		$this->refreshOn = true;
		
		$this->refreshButton = new Button('De-Activate Refresh');
		$this->refreshButton->addClickListener('refresher', $this, "toggleRefresh");
		
	}

	/**
	 * Responds to a click on the "De-Activate Refresh" button.  Toggles the refresh
	 * and the text displayed on the button.
	 *
	 * @param unknown_type $guavaobject
	 * @param unknown_type $parameterList
	 */
	public function toggleRefresh($guavaobject, $parameterList = null) {
		if($this->refreshOn) {
			// Our refresh is activated
			$this->refreshOn = false;
			$this->timer->disable();
			$this->refreshButton->setLabel('Activate Refresh');
		}
		else {
			$this->refreshOn = true;
			$this->timer->enable();
			$this->refreshButton->setLabel('De-Activate Refresh');
		}
		$this->getEvents($this->sortItems);
	}
	
	/**
	 * Causes the screen to be refreshed by calling the updateData method
	 *
	 */
	public function refresh() {
		$this->getEvents($this->sortItems);
	}
	
	/**
	 * opens a status viewer object view in response to a click on a host/device name
	 *
	 * @param unknown_type $message
	 */
	public function processMessage($message) {
		global $sv2;
		global $guava;
		// Make sure this message is really meant for us
		if($message->parameterExists("id")) {
			if($message->getParameter("id")->getValue() == $this->getIdentifier()) {
				// Yay, it's for us.  Let's check the action
				if($message->parameterExists("action")) {
					if($message->getParameter("action")->getValue() == "host") {
						$hostName = $message->getParameter("host")->getValue();
						
						if(isset($sv2)) {
							// Status Viewer sysmodule is available
							$component = $sv2->createHostComponent($hostName);
							$component->setCloned(true);
							$component->expand();
							if($component) {
								$guava->objectView($component);
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Increments the page number in response to a click on the "Next" link
	 *
	 */
	public function incrementconsolePager() 
	{
		$this->consolePager++;
		$this->getEvents($this->sortItems);
	}
	
	/**
	 * Decrements the page number in response to a click on the "Prev" link
	 *
	 */
	public function decrementconsolePager() {
	    if ($this->consolePager > 0)
		  $this->consolePager--;
		$this->getEvents($this->sortItems);
	}
	
	/**
	 * 	perform sorting on a single column
	 */
	public function sortData($guavaobject, $parameterList = null) 
	{
		
	    $this->sortItems = array($parameterList['orderBy']=>$parameterList['sortOrder']);
		$this->consolePager = 0;
		$this->getEvents($this->sortItems);
	}

	private function getEvents($sortItems=null)
	{
		switch($this->eventView)
		{
		    case DEFAULTEVENTVIEW:
		        $this->getAllEvents($sortItems);
		        break;
		    case APPLICATIONEVENTVIEW:
		        $this->getApplicationEvents($this->appTypeId, $this->eventDescription, $sortItems);
		        break;
		    case HOSTGROUPEVENTVIEW:
		        $this->getHostGroupEvents($this->hgId, $this->eventDescription, $sortItems);
		        break;
		    case HOSTEVENTVIEW:
		        $this->getHostEvents($this->hostId, $this->eventDescription, $sortItems);
		        break;
		    case SERVICEEVENTVIEW:
		        $this->getServiceEvents($this->serviceDescription, $this->hostId, $sortItems);
		        break;
		    default:
		        $this->getAllEvents($sortItems);
		        break;
		}
	}
	
	/**
	 * Get all events
	 *
	 */
	public function getAllEvents($sortItems=null) 
	{
	    global $guava;

	    if (strcmp($this->eventView,DEFAULTEVENTVIEW)!=0)
	    {
	        $this->eventView = DEFAULTEVENTVIEW;
	        $this->consolePager = 0;
	    }
		$this->eventDescription = " ";
		
        if ($sortItems==null)
        {
            $this->sortItems = array(DEFAULT_SORTCOLUMN => false);
		    $sortItems = $this->sortItems;
        }
        else {
            $this->sortItems = $sortItems;
        }
		try {
            $events = $this->eventDal->getAllOpenEvents(null, $sortItems, $this->consolePager * $this->consoleNumPerPage, $this->consoleNumPerPage);
		    // make sure polling is on since webservice is available.
    		// if we turned refresh off, turn it back on
    		if ($this->isRefreshButtonEnabled && $this->refreshOn) {
    			if (isset($this->timer) && (!$this->timer->isEnabled()))
    				$this->timer->enable();
    		}
		}
		catch (DALException $ex) {
		    $guava->console("Error while getting events");
		    $guava->console($ex->getMessage());
		    // stop polling since webservice is unavailable.
    		if ($this->isRefreshButtonEnabled && isset($this->timer)) {
			     $this->timer->disable();
    		}
		    $dlg = new ErrorDialog("An error occurred when connecting to the foundation webservice.  Please contact your system administrator for further assistance.");
		    $dlg->show();
		    $events = array();
		}
        $this->createTargetData($events);
	}
	
	/**
	 * @deprecated 
	 *
	 */
	public function queryForAllEvents($sortItems=null)
	{
	    $this->getAllEvents($sortItems);
	}
	
	/**
	 * Gets all events for the specified application type
	 *
	 * @param unknown_type $applicationTypeId
	 * @param String $applicationTypeName - name of the application type.  Allowed to be null for now, 
	 * after 5.1 it will be required
	 */
	public function getApplicationEvents($applicationTypeId, $applicationTypeName=null, $sortItems=null) {
		global $guava;

		if ($applicationTypeName == null)
		{
		    // TODO: retrieve Application Name for id provided.
		}
		
		// if the "view" has changed, make sure to reset the page to 0.
	    if (strcmp($this->eventView,APPLICATIONEVENTVIEW)!=0)
	    {
	        $this->eventView = APPLICATIONEVENTVIEW;
	        $this->consolePager = 0;
	    }
	    // even if the "view" didn't change, a different application type
	    // might have been selected.
		if ($this->appTypeId != $applicationTypeId)
		  $this->consolePager = 0;
		  
		$this->eventDescription = $applicationTypeName;
		$this->appTypeId = $applicationTypeId;
        if ($sortItems==null)
        {
            $this->sortItems = array(DEFAULT_SORTCOLUMN => false);
		    $sortItems = $this->sortItems;
        }
        else 
        {
            $this->sortItems = $sortItems;
        }

		try {
            $events = $this->eventDal->getAllOpenEvents($applicationTypeId, $sortItems, $this->consolePager * $this->consoleNumPerPage, $this->consoleNumPerPage);
		    // make sure polling is on since webservice is available.
    		// if we turned refresh off, turn it back on
    		if ($this->isRefreshButtonEnabled && $this->refreshOn) {
    			if (isset($this->timer) && (!$this->timer->isEnabled()))
    				$this->timer->enable();
    		}
		}
		catch (DALException $ex) {
		    $guava->console("Error while getting events");
		    $guava->console($ex->getMessage());
		    // stop polling since webservice is unavailable.
    		if ($this->isRefreshButtonEnabled && isset($this->timer)) {
			     $this->timer->disable();
    		}
		    $dlg = new ErrorDialog("An error occurred when connecting to the foundation webservice.  Please contact your system administrator for further assistance.");
		    $dlg->show();
		    $events = array();
		}
        $this->createTargetData($events);
	}

	/**
	 * @deprecated 
	 *
	 */
	public function queryForApplicationEvents($applicationTypeId, $applicationTypeName= null, $sortItems=null)
	{
	    $this->getApplicationEvents($applicationTypeId, $applicationTypeName, $sortItems);
	}
	
    /**
     * Get all open events for the hostgroup specified.
     *
     * @param unknown_type $hostGroupId
     * @param string $hostGroupName - Allowed to be null for now, 
	 * after 5.1 it will be required
     * @param unknown_type $sortItems
     */
	public function getHostGroupEvents($hostGroupId, $hostGroupName=null, $sortItems=null)
	{
		global $guava;
		
		if ($hostGroupName == null)
		{
		    // TODO: query for hostgroupName using hostgroupId provided
		    $guava->console("Host Group Name is null");
		}
		
        // if we just changed "views", reset the page to 0
		if (strcmp($this->eventView,HOSTGROUPEVENTVIEW)!=0)
	    {
	        $this->eventView = HOSTGROUPEVENTVIEW;
	        $this->consolePager = 0;
	    }
		
		// even if the "view" is still the same, the hostgroups being
		// "viewed" may have changed.  In that case, reset the page to 0.
		if ($this->hgId != $hostGroupId)
		  $this->consolePager = 0;
		  
		$this->hgId = $hostGroupId;
		$this->eventDescription = $hostGroupName;
        if ($sortItems==null)
        {
            $this->sortItems = array(DEFAULT_SORTCOLUMN => false);
		    $sortItems = $this->sortItems;
        }
        else {
            $this->sortItems = $sortItems;
        }
		try {
		    $events = $this->eventDal->getOpenEventsByHostGroupId($hostGroupId, null, $sortItems, $this->consolePager * $this->consoleNumPerPage, $this->consoleNumPerPage);
		    // make sure polling is on since webservice is available.
    		// if we turned refresh off, turn it back on
    		if ($this->isRefreshButtonEnabled && $this->refreshOn) {
    			if (isset($this->timer) && (!$this->timer->isEnabled()))
    				$this->timer->enable();
    		}
		}
		catch (DALException $ex) {
		    $guava->console("Error while getting events");
		    $guava->console($ex->getMessage());
		    // stop polling since webservice is unavailable.
    		if ($this->isRefreshButtonEnabled && isset($this->timer)) {
			     $this->timer->disable();
    		}
		    $dlg = new ErrorDialog("An error occurred when connecting to the foundation webservice.  Please contact your system administrator for further assistance.");
		    $dlg->show();
		    $events = array();
		}
       $this->createTargetData($events);
	}
	
	/**
	 * @deprecated 
	 *
	 */
	public function queryForHostGroupEvents($hostGroupId, $hostGroupName=null, $sortItems=null)
	{
	    $this->getHostGroupEvents($hostGroupId, $hostGroupName, $sortItems);
	}
	
	public function getHostEvents($hostId, $hostName=null, $sortItems=null)
	{
		global $guava;
		
		if ($hostName == null)
		{
		    // TODO: query for hostName using hostId provided
		    $guava->console("Host Name is null");
		}
		
		$this->eventView = HOSTEVENTVIEW;
		$this->eventDescription = $hostName;
		$this->hostId = $hostId;
        if ($sortItems==null)
        {
            $this->sortItems = array(DEFAULT_SORTCOLUMN => false);
		    $sortItems = $this->sortItems;
        }
        else 
        {
            $this->sortItems = $sortItems;
        }

		try {
		    $events = $this->eventDal->getOpenEventsByHostId($hostId, null, $sortItems, $this->consolePager * $this->consoleNumPerPage, $this->consoleNumPerPage);
		    // make sure polling is on since webservice is available.
    		// if we turned refresh off, turn it back on
    		if ($this->isRefreshButtonEnabled && $this->refreshOn) {
    			if (isset($this->timer) && (!$this->timer->isEnabled()))
    				$this->timer->enable();
    		}
		}
		catch (DALException $ex) {
		    $guava->console("Error while getting events");
		    $guava->console($ex->getMessage());
		    // stop polling since webservice is unavailable.
    		if ($this->isRefreshButtonEnabled && isset($this->timer)) {
			     $this->timer->disable();
    		}
		    $dlg = new ErrorDialog("An error occurred when connecting to the foundation webservice.  Please contact your system administrator for further assistance.");
		    $dlg->show();
		    $events = array();
		}
       $this->createTargetData($events);
	}
	
	/**
	 * @deprecated 
	 *
	 */
	public function queryForHostEvents($hostId, $hostName=null, $sortItems=null)
	{
	    $this->getHostEvents($hostId, $hostName, $sortItems);
	}
	
	public function getServiceEvents($serviceDescription, $hostId, $sortItems=null)
	{
		global $guava;
		
	    $this->eventView = SERVICEEVENTVIEW;
		$this->hostId = $hostId;
		$this->serviceDescription = $serviceDescription;
		$this->eventDescription = $serviceDescription;
        if ($sortItems==null)
        {
            $this->sortItems = array(DEFAULT_SORTCOLUMN => false);
		    $sortItems = $this->sortItems;
        }
        else 
        {
            $this->sortItems = $sortItems;
        }
		
		try {
		    $events = $this->eventDal->getOpenEventsByServiceDescription($serviceDescription, $hostId, null, $sortItems, $this->consolePager * $this->consoleNumPerPage, $this->consoleNumPerPage);
		    // make sure polling is on since webservice is available.
    		// if we turned refresh off, turn it back on
    		if ($this->isRefreshButtonEnabled && $this->refreshOn) {
    			if (isset($this->timer) && (!$this->timer->isEnabled()))
    				$this->timer->enable();
    		}
		}
		catch (DALException $ex) {
		    $guava->console("Error while getting events");
		    $guava->console($ex->getMessage());
		    // stop polling since webservice is unavailable.
    		if ($this->isRefreshButtonEnabled && isset($this->timer)) {
			     $this->timer->disable();
    		}
		    $dlg = new ErrorDialog("An error occurred when connecting to the foundation webservice.  Please contact your system administrator for further assistance.");
		    $dlg->show();
		    $events = array();
		}
       $this->createTargetData($events);
	}
	
	/**
	 * @deprecated 
	 *
	 */
	public function queryForServiceEvents($service, $host, $applicationTypeName=null)
	{
	    $this->getServiceEvents($service, $host, $applicationTypeName=null);
	}
	
	private function createTargetData($events)
	{
	    global $guava;

	    if (count($events) && count($events['Messages']))
	    {
	        $dynamicColumns = array();
	        // get the names of the columns pertaining to the dynamic properties that will be displayed
	        $firstTime = true;
			foreach($events['Messages'] as $event) {
			    if ($firstTime == true) {
			        $guava->console("First event has Status of: ".$event['MonitorStatus']->Name);
			        $firstTime = false;
			    }
				if (strcmp($this->eventView, DEFAULTEVENTVIEW)!=0 && sizeof($event['Properties'])>0) {
        	        foreach ($event['Properties'] as $propName => $value)
        	        {
        	            $dynamicColumns[$propName] = $value;
        	        }
				}
			}
	    }
	    
		$counter = 0;
		ob_start();
		$this->mainForm->Open();
		?>
		<table width="100%" cellspacing="0" cellpadding="0" parsewidgets="false">
			<tr>
				<td colspan="7" width="100%">
				<table cellpadding="0" cellspacing="0" width="100%">
			<tr>
				<td nowrap="1" width="100%">
		<?php
		if($this->consolePager > 0) {
			?><< <?php $this->prevLink->Draw() ?>&nbsp;&nbsp;&nbsp;<?php
		}
		?><b>Showing <?php 
		if (count($events) && count($events['Messages']) == 0) { 
		?>
			0 to <?=(($this->consolePager * $this->consoleNumPerPage));?> of <?=$events['Count'];?> Total Events For: <?=$this->eventView." ".$this->eventDescription;?></b>
		<?php }
		else {			
			print((($this->consolePager * $this->consoleNumPerPage)+1));?> to <?=(($this->consolePager * $this->consoleNumPerPage) + count($events['Messages']));?> of <?=$events['Count'];?> Total Events For: <?=$this->eventView." ".$this->eventDescription;?></b>
			
		<?php
		}
		if(count($events) && (($this->consolePager * $this->consoleNumPerPage + $this->consoleNumPerPage) <= $events['Count'])) {
			?>&nbsp;<?php $this->nextLink->Draw() ?> >> <?php
		}
		?>
				</td>
			</tr>
		<tr>
		<?php
		if (count($events) && ($events['Count'] > 0) && $this->isAcceptEnabled && !$this->refreshOn) {
		?>
			<td><?php $this->checkBoxSubmitButton->Draw() ?></td>
		<?php
		}
		?>
		<td>		
		<?php if (count($events) && ($events['Count'] > 0) && $this->isRefreshButtonEnabled) $this->refreshButton->Draw(); ?>
		</td>
		<td>
			<?php if (count($events) && ($events['Count'] > 0) && $this->isAdvancedSortingEnabled) $this->advancedSortButton->Draw(); 
			?>
		</td>
		</tr>
		</table>
		</td>
		</tr>
		<?php
		if(count($events) && count($events['Messages']) > 0) {
			?>
				<tr class="altTop">
				<?php
				if ($this->refreshOn) {
				
					?>
					<td nowrap="1" id="AcceptAll"></td>
				<?php
				}
				else {
					if ($this->isAcceptEnabled) {
				?>
				 <td nowrap="1" id="AcceptAll"><?php $this->acceptAll->setValue(false); $this->acceptAll->Draw();?> Accept All</td> 
				<?php
					}
					else {
					?>
					<td nowrap="1" id="AcceptAll"></td>
				<?php
					}
				}
			?>
					<td width="5"><img src="images/dotclear.gif" width="5" /></td>
					<td align="left" nowrap="1"><?php $this->reportDtTextLink->Draw() ?></td>
					
 					<td align="left" nowrap="1"><?php $this->msgCountTextLink->Draw() ?></td>
					<td width="5"><img src="images/dotclear.gif" width="5" /></td>
					<td align="left" nowrap="1"><?php $this->hostTextLink->Draw() ?></td>
					<td width="5"><img src="images/dotclear.gif" width="5" /></td>
					<td align="left" nowrap="1"><?php $this->monitorStatusTextLink->Draw() ?></td>
					<td width="5"><img src="images/dotclear.gif" width="5" /></td>
					<td align="left" nowrap="1"><?php $this->severityTextLink->Draw() ?></td>
					<td width="5"><img src="images/dotclear.gif" width="5" /></td>
					 
					<td width="5"><img src="images/dotclear.gif" width="5" /></td>
					<td align="left" nowrap="1">Message</td>
					<?php
					if (sizeof($dynamicColumns)>0) {
					    foreach ($dynamicColumns as $name => $value) {
                    ?>
                        <td width="5"><img src="images/dotclear.gif" width="5" /></td>
				        <td align="left" nowrap="1"><?php
				         ?>
				        <?=$name?></td>  <?php
					    }
					}
					?>
					<td width="5"><img src="images/dotclear.gif" width="5" /></td>
					<td align="left" nowrap="1"><?php $this->lastInsertTextLink->Draw() ?></td>
					<td width="5"><img src="images/dotclear.gif" width="5" /></td>
					<td align="left" nowrap="1"><?php $this->firstInsertTextLink->Draw() ?></td>
				</tr>
			<?php
			     foreach($events['Messages'] as $event) {
    				$deviceName = $event['Device']->Name;
    				?>
    				<tr class="<?php 					
    				switch($event['MonitorStatus']->Name) {
    						case "OK":
    							print("console_ok");
    							break;
    						case "CRITICAL":
    							print("console_critical");
    							break;
    						case "WARNING":
    							print("console_warning");
    							break;
    						case "PENDING":
    							print("console_unknown");
    							break;
    						case "UNKNOWN":
    							print("console_unknown");
    							break;
    						case "DOWN":
    							print("console_critical");
    							break;
    						case "UNREACHABLE":
    							print("console_critical");
    							break;
    						default:
    							print("console_ok");
    							break;
    					}?>">
        				<?php
        				if ($this->isRefreshButtonEnabled && $this->refreshOn) {
        					?>
        					<td nowrap="nowrap" align="center" >
        				<?php
        				}
        				else {
        				?>
        					<td nowrap="nowrap" align="center" >
        				<?php
        					if ($this->isAcceptEnabled && !$this->refreshOn) {
        						if ($counter < $this->consoleNumPerPage)
        						{
        						    $this->checkBoxes[$counter]->setValue(false);
        							$this->checkBoxes[$counter]->Draw(); 
        							$this->checkBoxes[$counter]->setEventId($event['LogMessageID']);
        						}
        					}
        				}
        				?> </td>
    				    </td>
    					<td width="5"><img src="images/dotclear.gif" width="5" /></td>
    					<td nowrap="nowrap" align="left"><?=$this->stringTime($event['ReportDate']);?></td>

    					<td nowrap="nowrap" align="center"><?=$event['MessageCount'];?></td>
    					<td width="5"><img src="images/dotclear.gif" width="5" /></td>
    					<td nowrap="nowrap" align="left"><?php 
    					  
    					
						if ($this->svLoaded && ($event['Host']->Name != null && (strlen($event['Host']->Name) > 0))) {
							if($guava->getExecPerm($guava->getDefaultRole($_SESSION['user_id'])) == 1){
							?><a href="javascript:addMessage('console', 'console', [{name: 'id', type: 'string', value: '<?=$this->getIdentifier();?>'},{name: 'action', type: 'string', value: 'host'}, {name: 'host', type: 'string', value: '<?=$event['Host']->Name;?>'}]); sendMessageQueue();">
    							
    							
    				 <?php }} ?>
    				 <?=$deviceName;?>
    				   </a>
    				   
    				   </td>
    
    					<td width="5"><img src="images/dotclear.gif" width="5" /></td>
    					<td nowrap="nowrap" align="left" ><?=$event['MonitorStatus']->Name;?></td>
    					<td width="5"><img src="images/dotclear.gif" width="5" /></td>
    					<td nowrap="nowrap" align="left" ><?=$event['Severity']->Name;?></td>
    					<td width="5"><img src="images/dotclear.gif" width="5" /></td>
    					 
    					<td width="5"><img src="images/dotclear.gif" width="5" /></td>
    					<td nowrap="nowrap" align="left"><?=$event['TextMessage'];?></td>
    					<?php
    					if (sizeof($dynamicColumns)>0) {
    					    foreach ($dynamicColumns as $name => $value) {
    					        if (array_key_exists($name, $event['Properties'])) {
    					             list($myHost,$myService) = explode(":",$event['Properties'][$name]);
     					       		 if($myService != ""){
     					       		 $tempService = $this->serviceDAL->getService($myService,$myHost);
    					       		 ?>
    					        	 <td width="5"><img src="images/dotclear.gif" width="5" /></td>
    					             <td align="left" nowrap="1"><?php
						if($guava->getExecPerm($guava->getDefaultRole($_SESSION['user_id'])) == 1){
							?><a href="javascript:addMessage('sv2clone', 'service',[{name: 'id', type: 'string', value: '<?=$tempService['ServiceStatusID'];?>'}]); sendMessageQueue();">
    					 <?php }  ?>
    				 <?=$myService?>
    				   </a><?php
								}
    					       else{
                        ?>
                                    <td width="5"><img src="images/dotclear.gif" width="5" /></td>
    					           <td align="left" nowrap="1"><?=$event['Properties'][$name]?></td>  <?php
    					        	}
								}
    					        else {?>
                                    <td width="5"><img src="images/dotclear.gif" width="5" /></td>
    					            <td align="left" nowrap="1"><?=" "?></td>  <?php    
    					        }					        
    					    }
    					}
    			 	if(preg_match('/1969/',$this->stringTime($event['FirstInsertDate']))){
    						$event['FirstInsertDate'] = $event['LastInsertDate'];
    					 }
    					?>
    					<td width="5"><img src="images/dotclear.gif" width="5" /></td>
    					<td nowrap="nowrap" align="left"><?=$this->stringTime($event['LastInsertDate']);?></td>
    					<td width="5"><img src="images/dotclear.gif" width="5" /></td>
    					<td nowrap="nowrap" align="left"><?=$this->stringTime($event['FirstInsertDate']);?></td>
    				</tr>
    				<?php
    				$counter++;
    			}?>
			</table> <?php
			$this->mainForm->Close();		
			$targetData = ob_get_clean();
			ob_end_flush();
			$this->targetData("resultTable", $targetData);
        }
		else {  // no results
			?>
			<tr class="altTop">
				<td align="left" nowrap="1">Report Date</td>
				<td width="5"><img src="images/dotclear.gif" width="5" /></td>
				<td align="left" nowrap="1">Host</td>
				<td width="5"><img src="images/dotclear.gif" width="5" /></td>
				<td align="left" nowrap="1">Msg Count</td>
				<td width="5"><img src="images/dotclear.gif" width="5" /></td>
				<td align="left" nowrap="1">Monitor Status</td>
				<td width="5"><img src="images/dotclear.gif" width="5" /></td>
				<td align="left" nowrap="1">Application Type</td>
				<td width="5"><img src="images/dotclear.gif" width="5" /></td>
				<td align="left" nowrap="1">Message</td>
				<td width="5"><img src="images/dotclear.gif" width="5" /></td>
				<td align="left" nowrap="1">Last Inserted</td>
				<td width="5"><img src="images/dotclear.gif" width="5" /></td>
				<td align="left" nowrap="1">First Inserted</td>
			</tr>
			<tr class="altRow1">
				<td align="left" nowrap="1">No events found.</td>
				<td width="5"><img src="images/dotclear.gif" width="5" /></td>
				<td width="5"><img src="images/dotclear.gif" width="5" /></td>
				<td width="5"><img src="images/dotclear.gif" width="5" /></td>
				<td width="5"><img src="images/dotclear.gif" width="5" /></td>
				<td width="5"><img src="images/dotclear.gif" width="5" /></td>
				<td width="5"><img src="images/dotclear.gif" width="5" /></td>
				<td width="5"><img src="images/dotclear.gif" width="5" /></td>
				<td width="5"><img src="images/dotclear.gif" width="5" /></td>
				<td width="5"><img src="images/dotclear.gif" width="5" /></td>
				<td width="5"><img src="images/dotclear.gif" width="5" /></td>
				<td width="5"><img src="images/dotclear.gif" width="5" /></td>
				<td width="5"><img src="images/dotclear.gif" width="5" /></td>
				<td width="5"><img src="images/dotclear.gif" width="5" /></td>
				<td width="5"><img src="images/dotclear.gif" width="5" /></td>
				<td width="5"><img src="images/dotclear.gif" width="5" /></td>
				<td width="5"><img src="images/dotclear.gif" width="5" /></td>
			</tr>
		</table>
<?php	
			$this->mainForm->Close();		
			$targetData = ob_get_clean();
			ob_end_flush();
			$this->targetData("resultTable", $targetData);
		}
	    	    
	}
	
	public function Draw()
	{
		// Check for consolePager values over url
		if(isset($_GET['consolePager'])) {
			$this->consolePager = $_GET['consolePager'];
			$this->getEvents($this->sortItems);
		}

		print_window_header("GroundWork Network Console - " . date('l dS \of F Y h:i:s A'), "100%", "left");

		$this->printTarget("resultTable");
		print_window_footer();
	}
	
	public function unregister()
	{
		global $guava;
		
		parent::unregister();
		// This gets called whenever someone moves away from your view (clicks on another tab)
		
		if (isset($this->acceptAll))
			$this->acceptAll->unregister();
		
        if (isset($this->eventDal))
            $this->eventDal = null;
		if (isset($this->advancedSortButton))
			$this->advancedSortButton->unregister();
		if (isset($this->appNameTextLink))
			$this->appNameTextLink->unregister();
		if (isset($this->checkBoxSubmitButton))
			$this->checkBoxSubmitButton->unregister();
		if (isset($this->firstInsertTextLink))
			$this->firstInsertTextLink->unregister();
		if (isset($this->hostTextLink))
			$this->hostTextLink->unregister();
		if (isset($this->lastInsertTextLink))
			$this->lastInsertTextLink->unregister();
		if (isset($this->mainForm))
			$this->mainForm->unregister();
		if (isset($this->monitorStatusTextLink))
			$this->monitorStatusTextLink->unregister();
		if (isset($this->msgCountTextLink))
			$this->msgCountTextLink->unregister();
		if (isset($this->nextLink))
			$this->nextLink->unregister();
		if (isset($this->preDefinedColumns))
			$this->preDefinedColumns = null;
		if (isset($this->prevLink))
			$this->prevLink->unregister();
		if (isset($this->refreshButton))
			$this->refreshButton->unregister();
		if (isset($this->reportDtTextLink))
			$this->reportDtTextLink->unregister();
		if (isset($this->severityTextLink))
			$this->severityTextLink->unregister();
		if (isset($this->sortDialog))
			$this->sortDialog->unregister();
		
		if (count($this->checkBoxes)) {
			foreach ($this->checkBoxes as $checkBox) {
				$checkBox->unregister();
			}
		}
		
		if (isset($this->timer)) {
			$this->timer->disable();
			$this->timer = null;
		}
			
		$processor = $guava->getProcessor();
		$processor->removeContextListener('console', $this);
		
	    
	}
	
	private function stringTime($time) {
		$newTime = date("m/d/Y H:i:s", $time);
		
		return $newTime;
	}
}
?>