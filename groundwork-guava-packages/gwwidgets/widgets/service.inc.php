<?php
/*
Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
All rights reserved. Use is subject to GroundWork commercial license terms. 
*/

require_once('DAL/ServiceDAL.inc.php');
require_once('DAL/HostDAL.inc.php');

class GWWidgetsServiceListConfigureDialog extends GuavaWidgetConfigureDialog implements ActionListener  {
	
	private $services;
	
	private $currentHost;
	private $selectRefresh;
	private $serviceLinks;
	
	private $inputHost;
	private $findButton;
	private $inputService;
	private $addButton;
	
	public function __construct($source) 
	{
	    
		parent::__construct($source);
		
		$this->services = $source->getServiceList();

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
	
	
		$this->inputHost = new InputTextSuggestControl("Host",$this); 
		$this->inputService = new InputSelect();
		$this->inputService->addActionListener("click", $this);
		$this->findButton = new Button("Find Host");
		$this->findButton->addActionListener("click", $this);
		$this->addButton = new Button("Add Service");
		$this->addButton->addActionListener("click", $this);
		
		$this->targetData("serviceSelect", "Specify a host above to obtain a list of services to add.");
		
	}
	
	public function unregister() {
		parent::unregister();
		$this->inputHost->unregister();
		$this->inputService->unregister();
		$this->findButton->unregister();
		$this->addButton->unregister();
	}
	
	public function getServiceList() {
		return $this->services;
	}
	
	public function actionPerformed($event) 
    {
        global $foundationModule;
        global $sv2;
        
		parent::actionPerformed($event);
		
		if($event->getSource() === $this->addButton) {
			// We want to add a Service
			//need to check if service exists already
			if (in_array(array('host' => $this->currentHost, 'service' => $this->inputService->getValue()), $this->services)){
				//already exists, do nothing
			}else{
				//add service
				$this->services[] = array('host' => $this->currentHost, 'service' => $this->inputService->getValue());
			}
			
			
			$this->rebuildListTarget();
		}
		else if($event->getSource() === $this->findButton) 
		{
		
		
		
		
		    if ($this->inputHost->getValue() == null || $this->inputHost->getValue() == "")
			{
   				$dialog = new ErrorDialog("Host Name cannot be blank.");
   				$dialog->show();   	
   				return;			
			}
			 
			
        try {
    		// Let's first check to see if host exists
    		$hostDAL = new HostDAL($foundationModule->getWebServiceURL());
    		
    		$hostInfo = $hostDAL->getHostByHostName($this->inputHost->getValue());
        }
		catch (Exception $e)
		{
		    $dialog = new ErrorDialog("Unable to retrieve host ".$this->hostName);
		    $dialog->show();
		    if ($sv2->getErrorOccurred() == false)
    		  $sv2->setErrorOccurred(true);
    		  return;
		}
		
		if($hostInfo == null || empty($hostInfo)) {
			// Host does not exist
			$err = new ErrorDialog("Host does not exist.");
	 		$err->show();
	 		return;
		}	
			
			
			
			else {
			
                try {
    			    // First find a host!			
    				$hostDAL = new HostDAL($foundationModule->getWebServiceURL());
    				$tempHost = $hostDAL->getHostByHostName($this->inputHost->getValue());
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
        		    if ($sv2->getErrorOccurred() == false)
        		    {
            		    $dialog = new ErrorDialog("An error occurred retrieving host ".$this->inputHost->getValue());
            		    $dialog->show();
            		    $sv2->setErrorOccurred(true);
        		    }
        		}
        		
				if($tempHost == null) {
				    if ($sv2->getErrorOccurred() == false)
				    {
    					$dialog = new ErrorDialog("Host not found in system.");
    					$dialog->show();
    					$sv2->setErrorOccurred(true);
				    }
				}
				else {
					// we found the host!  Woohoo!
					// Empty the select
					$this->inputService->removeAll();
					try {
    					$serviceDAL = new ServiceDAL($foundationModule->getWebServiceURL());
    					$results = $serviceDAL->getServicesByHostName($this->inputHost->getValue());
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
            		    if ($sv2->getErrorOccurred() == false)
            		    {
                		    $dialog = new ErrorDialog("An error occurred retrieving host ".$this->inputHost->getValue());
                		    $dialog->show();
                		    $sv2->setErrorOccurred(true);
            		    }
            		}
					if ($results != null)
					{
					    $services = $results['Services'];
					}
					
					if($services == null) {
						$dialog = new ErrorDialog("No services found for host " . $this->inputHost->getValue());
						$dialog->show();
					}
					else 
					{
						$this->currentHost = $this->inputHost->getValue();
						
						// We actually have services
						foreach($services as $service) {
							$this->inputService->addOption($service['Description'], $service['Description']);
						}
						
						$this->targetData("serviceSelect", "<h1>Select Service To Add From " . $this->inputHost->getValue() . ":</h1>" . $this->inputService->toString() . " " . $this->addButton->toString());
					}
				}			
			}
		}
		else {
			if(count($this->serviceLinks)) {				
				for($counter = 0; $counter < count($this->serviceLinks); $counter++) {
					if($this->serviceLinks[$counter]['link'] === $event->getSource()) {
						array_splice($this->services, $counter, 1);					
						$this->serviceLinks[$counter]['link']->unregister();
						unset($this->serviceLinks[$counter]);
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
		// Empty our links
		if(count($this->serviceLinks)) {
			foreach($this->serviceLinks as $service) {
				$service['link']->unregister();
			}
		}
		$this->serviceLinks = array();			
		
		for($counter = 0; $counter < count($this->services); $counter++) {
			$this->serviceLinks[$counter] = array('link' => new TextLink("Delete"));			
			$this->serviceLinks[$counter]['link']->addActionListener("click", $this);
			$buffer .= '<div style="background: #dddddd; height: 25px; border-width: 1px 0px 1px 0px; border-style: solid; border-bottom-color: black; border-left-color: grey; border-right-color: black; border-top-color: grey;"><table width="100%"><tr><td>'.$this->services[$counter]['service'] . ' on ' . $this->services[$counter]['host'] . '</td><td align="right">' . $this->serviceLinks[$counter]['link']->toString() . '</td></tr></table></div>';

		}
		$this->targetData("ServiceList", $buffer);
	}
	
	public function Draw() {
		?>
		<h1 align='center'>Service List Configuration</h1>
		<br/>
		<h1>Specify A Host:</h1>		
		<?php $this->inputHost->Draw(); ?>
		<div id="search_suggest">
		</div>
		<?=$this->findButton->Draw(); ?> <br />
		<br />
		<?=$this->printTarget("serviceSelect");?>
		<br />
		<h1>Current Service List</h1>
		<div style="height: 200px; border: 1px solid grey; overflow: auto;">
		<?=$this->printTarget("ServiceList");?><br />
		<br />
		</div>
		<h1>Set the Refresh Rate:</h1>
		<?php
		$this->selectRefresh->Draw();
		?>
		<?php
	}
}

/*class GWWidgetsServiceListWidgetItem extends GuavaObject implements ActionListener {
	
	private $serviceName;
	
	private $downHosts, $unreachableHosts;
	
	private $configInfo;
	
	private $notFound;
	
	private $hostLink;
	
	private $serviceLink;
	
	private $status;

	/**
	 * Data Access Layer Instances used in this class.
	 */
/*	private $serviceDAL;
	
	public function __construct($serviceID) 
	{
	    global $foundationModule;
	    global $sv2;
		
		parent::__construct();
		
		$this->serviceName = $serviceName;
		
		//$this->setTemplate(GUAVA_FS_ROOT . 'packages/gwwidgets/templates/ServiceListitem.xml');
		$this->setTemplate(GUAVA_FS_ROOT . 'packages/gwwidgets/templates/serviceitem.xml');
		
		$this->bind("self", $this);
					
		$this->hostLink = new TextLink($this->hostName);
		$this->hostLink->addActionListener("click", $this);
		$this->bind("hostLink", $this->hostLink);
		
		$this->serviceLink = new TextLink($this->serviceName);
		$this->serviceLink->addActionListener("click", $this);
		$this->bind("serviceLink", $this->serviceLink);
		
        try {
    		$this->serviceDAL = new ServiceDAL($foundationModule->getWebServiceURL());
    				
    		$serviceInfo = $this->serviceDAL->getServiceById($serviceID);
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
		    if ($sv2->getErrorOccurred() == false)
		    {
    		    $dialog = new ErrorDialog("An error occurred retrieving host ".$this->host);
    		    $dialog->show();
    		    $sv2->setErrorOccurred(true);
		    }
		}
		
		if($serviceInfo == null) {
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
		$this->serviceLink->unregister();
	}
	
	public function actionPerformed($event) {
		global $sv;
		global $guava;
		
		if(isset($sv) && !$this->notFound) {
			$component = $sv->createServiceComponent($this->serviceName);
			$component->expand();
			$guava->objectView($component);
		}
	}
	
	public function getHost() {
		return $this->hostName;
	}
	
	public function getService() {
	    return $this->serviceName;
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
			case 'OK':
				return "packages/gwwidgets/images/bg-green.gif";
				break;
			case 'UNREACHABLE':
			case 'DOWN':
			case 'CRITICAL':
				return "packages/gwwidgets/images/bg-red.gif";
				break;
			case 'WARNING':
			case 'PENDING':
				return "packages/gwwidgets/images/bg-yellow.gif";
				break;
			case 'UNKNOWN':
				return "packages/gwwidgets/images/bg-gray.gif";
				break;
		}
	}
	
	public function getIconImage() {
		if($this->notFound) {
			return "packages/gwwidgets/images/service-gray.gif";
		}
		switch($this->status) {
			case 'OK':
				return "packages/gwwidgets/images/service-green.gif";
				break;
			case 'UNREACHABLE':
			case 'DOWN':
			case 'CRITICAL':
				return "packages/gwwidgets/images/service-red.gif";
				break;
			case 'WARNING':
			case 'PENDING':
				return "packages/gwwidgets/images/service-yellow.gif";
				break;
		}
	}
	
	public function getStatus() {
		return $this->status;
	}
	
	public function update() 
	{	
	    global $foundationModule;
	    global $sv2;
	    
		if(!$this->notFound) 
		{
		    try {
    		    if (!isset($this->hostDAL))
    		      $this->hostDAL = new HostDAL($foundationModule->getWebServiceURL());
		        $hostInfo = $this->hostDAL->getHostByHostName($this->hostName);
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
    		    if ($sv2->getErrorOccurred() == false)
    		    {
        		    $dialog = new ErrorDialog("An error occurred retrieving host ".$this->host);
        		    $dialog->show();
        		    $sv2->setErrorOccurred(true);
    		    }
    		}
		    if ($hostInfo != null)
		    {
		        $this->status = $hostInfo['MonitorStatus']->Name;
		    }
		}
	}	
}*/

class GWWidgetsServiceListWidget extends GuavaWidget implements ActionListener   {
	
	private $items;	// Array

	private $services;
	
	
	/**
	 * Data Access Layer Instances used in this class.
	 */
	private $serviceDAL;
	
	public function init() {
		$this->services = array();
		
		$this->update();
		
		$this->items = array();
		
		$this->setConfigClass("GWWidgetsServiceListConfigureDialog");
	}
	
	public function unregister() {
		parent::unregister();
		 
		foreach($this->items as $item) {
			$item->Destroy();
		}
		$this->items = array();
		
		unset($this->serviceDAL);
	}
	
	/**
	 * Enter description here...
	 *
	 * @param ActionEvent $event
	 */
	public function actionPerformed($event) 
	{	
	    global $foundationModule;
	    global $sv2;
				
		if($event->getAction() == "configured") {
			if(Count($this->items)) {
				foreach($this->items as $item) {
					$item->unregister();
				}
			}
			$this->items = array();
			$this->services = array();
			$tempList = $event->getSource()->getServiceList();
			if(count($tempList)) 
			{
			    try {
    		        if (!isset($this->serviceDAL))
    		            $this->serviceDAL = new ServiceDAL($foundationModule->getWebServiceURL());
    				foreach($tempList as $service) 
    			    {
    					// We need to obtain the service id
    					$serviceInfo = $this->serviceDAL->getService($service['service'], $service['host']);
    					
    					if ($serviceInfo != null)
    					{
    					    $tempServiceItem = new GWWidgetsTroubledServicesListWidgetItem($serviceInfo['ServiceStatusID']);
    					    $this->services[] = $service;
    					    $this->items[] = $tempServiceItem;
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
        		    if ($sv2->getErrorOccurred() == false)
        		    {
            		    $dialog = new ErrorDialog("An error occurred retrieving service ".$service['service']);
            		    $dialog->show();
            		    $sv2->setErrorOccurred(true);
        		    }
        		}
		     
			}
		}
		$this->update();
	}
	
	public function getServiceList() {
		return $this->services;
	}
	
	public function getConfigObject() {
		return $this->services;
	}
	
	public function loadConfig($configObject) 
	{
		global $foundationModule;
		global $sv2;
		
        try {
    		if ($this->serviceDAL == null)
               $this->serviceDAL = new ServiceDAL($foundationModule->getWebServiceURL());
    		            
    		// configObject should be an array of Host names
    		foreach($configObject as $service) 
    		{
    			// We need to obtain the service id
    			$serviceInfo = $this->serviceDAL->getService($service['service'], $service['host']);
    				
                if ($serviceInfo != null)
    			{
    				$tempServiceItem = new GWWidgetsTroubledServicesListWidgetItem($serviceInfo['ServiceStatusID']);
    				$this->services[] = $service;
    				$this->items[] = $tempServiceItem;
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
		    if ($sv2->getErrorOccurred() == false)
		    {
    		    $dialog = new ErrorDialog("An error occurred retrieving service " .$service['service']);
    		    $dialog->show();
    		    $sv2->setErrorOccurred(true);
		    }
		}
     
	   $this->update();
	}
	
	public function update() {
		global $guava;
		$guava->console("Updating Service List Widget.");
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
