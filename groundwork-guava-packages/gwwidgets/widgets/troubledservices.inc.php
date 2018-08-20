<?php
/*
Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
All rights reserved. Use is subject to GroundWork commercial license terms. 
*/

require_once('DAL/ServiceDAL.inc.php');

// TODO: this class is used by both GWWidgetsTroubledServicesListWidget and GWWidgetsServiceListWidget
// do we need to separate classes?
class GWWidgetsTroubledServicesListWidgetItem extends GuavaObject implements ActionListener {
	
	private $serviceID;
		
	private $downTroubledServices, $unreachableTroubledServices;
	
	private $configInfo;
	
	private $notFound;
	
	private $hostLink;
	private $serviceLink;
	
	private $status;
	
	/**
	 * Data Access Layer Instances used in this class.
	 */
	private $serviceDAL;		
	
	public function __construct($serviceID) 
	{
	    global $foundationModule;
	    global $sv2;
	    
		parent::__construct();		

		$this->serviceID = $serviceID;		
		$this->setTemplate(GUAVA_FS_ROOT . 'packages/gwwidgets/templates/serviceitem.xml');
		
        try {
    		//Initialize DAL's		
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
    		    $dialog = new ErrorDialog("An error occurred retrieving service with ID: ".$serviceID);
    		    $dialog->show();
    		    $sv2->setErrorOccurred(true);
		    }
		}
		if ($serviceInfo == null)
		{
		    if ($sv2->getErrorOccurred() == false)
		    {
    			$dialog = new ErrorDialog('Unable to find service status, ID: ' . $serviceID);
    			$dialog->show();
    			$sv2->setErrorOccurred(true);
		    }
		}
		else {
		    $hostInfo = $serviceInfo['Host'];
    		$this->hostLink = new TextLink($hostInfo->Name);
    		$this->hostLink->addActionListener("click", $this);
    		$this->serviceLink = new TextLink($serviceInfo['Description']);
    		$this->serviceLink->addActionListener("click", $this);
    		$this->bind("hostLink", $this->hostLink);
    		$this->bind("serviceLink", $this->serviceLink);
		}

		$this->update();
	}
	
	
	public function unregister() {
		parent::unregister();
		if (isset($this->hostLink))
		  $this->hostLink->unregister();
		if (isset($this->serviceLink))
		  $this->serviceLink->unregister();
	}
	
	public function actionPerformed($event) {
		global $guava;
		global $foundationModule;
		global $sv2;

	    try {
		    if (!isset($this->serviceDAL))
		      $this->serviceDAL = new ServiceDAL($foundationModule->getWebServiceURL());
		    $serviceInfo = $this->serviceDAL->getServiceById($this->serviceID);
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
    		    $dialog = new ErrorDialog("An error occurred retrieving service with ID: ".$serviceID);
    		    $dialog->show();
    		    $sv2->setErrorOccurred(true);
		    }
		}
	    if ($serviceInfo == null)
		{
			$dialog = new ErrorDialog('Unable to find service status, ID: ' . $serviceID);
			$dialog->show();
		}
		else {
		    $hostInfo = $serviceInfo['Host'];
    		if($event->getSource() === $this->hostLink) 
    		{
    			$component = $sv2->createHostComponent($hostInfo->Name);
    		}
    		else {
    			$component = $sv2->createServiceComponent($this->serviceID);
    		}
    		$component->expand();
    		$component->setCloned(true);
    		$guava->objectView($component);
		}
		
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
				return "packages/gwwidgets/images/bg-yellow.gif";
				break;
			case 'PENDING':
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
				return "packages/gwwidgets/images/service-yellow.gif";
				break;
			case 'PENDING':
			case 'UNKNOWN':
				return "packages/gwwidgets/images/service-gray.gif";
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
	    
	    try {
		    if (!isset($this->serviceDAL))
		      $this->serviceDAL = new ServiceDAL($foundationModule->getWebServiceURL());
		    $serviceInfo = $this->serviceDAL->getServiceById($this->serviceID);
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
    		    $dialog = new ErrorDialog("An error occurred retrieving service with ID: ".$this->serviceID);
    		    $dialog->show();
    		    $sv2->setErrorOccurred(true);
		    }
		}
	    if ($serviceInfo == null)
		{
			$dialog = new ErrorDialog('Unable to find service status, ID: ' . $this->serviceID);
			$dialog->show();
		}
		else {		    
		    $this->status = $serviceInfo['MonitorStatus']->Name;
		}
	}	
}


class GWWidgetsTroubledServicesListWidget extends GuavaWidget implements ActionListener   {
	
	private $items;	// Array

	private $TroubledServices;
	
	private $serviceDAL;
	
	private $numPerPage;
	/**
	 * This is the start of where we are in our service list.
	 *
	 * @var integer
	 */
	private $serviceStartCounter;
	
	/**
	 * Our Image to handle our Next link for services.
	 *
	 * @var Image
	 */
	private $serviceNext;
	/**
	 * Our Image to handle our Prev link for services.
	 *
	 * @var Image
	 */
	private $servicePrev;
	
	public function init() 
	{
		$this->setTemplate(GUAVA_FS_ROOT . "packages/gwwidgets/templates/troubledservices.xml");
		$this->TroubledServices = array();
		
		$this->serviceStartCounter = 0;
		$this->numPerPage = 50;
		
		$this->serviceNext = new Image(GUAVA_WS_ROOT . 'packages/sv2/images/next_on.gif');
		$this->serviceNext->addActionListener("click", $this);
		$this->servicePrev = new Image(GUAVA_WS_ROOT . 'packages/sv2/images/prev_on.gif');
		$this->servicePrev->addActionListener("click", $this);
		
		$this->update();
		
		$this->items = array();
		 
	}
	
	public function unregister() 
	{
		parent::unregister(); 
		
		foreach($this->items as $item) {
			$item->Destroy();
		}
		
		$this->items = array();
		
		$this->serviceNext->unregister();
		$this->servicePrev->unregister();
		
		unset($this->serviceDAL);
	}
	
			
	public function actionPerformed($event) {
		if($event->getSource() === $this->serviceNext)
			$this->serviceStartCounter += $this->numPerPage;
		if($event->getSource() === $this->servicePrev)
			$this->serviceStartCounter -= $this->numPerPage;
		$this->update();
	}
	
	public function update() 
	{
		global $guava;
		global $foundationModule;
		global $sv2;
		
		// Clear existing
		$this->TroubledServices = array();
		
		if(count($this->items)) {
			foreach($this->items as $item) {
				$item->unregister();
			}
		}
		$this->items = array();
		
		if($this->serviceStartCounter < 0) {
			$this->serviceStartCounter = 0;
		}
		
	    try {
		    if (!isset($this->serviceDAL))
		      $this->serviceDAL = new ServiceDAL($foundationModule->getWebServiceURL());
    		// NOTE:  We need to add pagination to this widget.  Returning all may be a performance issue.
            $results = $this->serviceDAL->getTroubledServices($this->serviceStartCounter, $this->numPerPage);
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
    		    $dialog = new ErrorDialog("An error occurred retrieving troubled services");
    		    $dialog->show();
    		    $sv2->setErrorOccurred(true);
		    }
		}
		
		if ($results != null)
		{
			$totalTroubledServices = $results['Count'];
			$this->targetData("count", $results['Count']);
		    $serviceList = $results['Services']; 
		    
			if(($this->serviceStartCounter + $this->numPerPage) < $totalTroubledServices) {
				$this->targetData("serviceNext", $this->serviceNext);
			}
			else {
				$this->targetData("serviceNext", "");
			}
			if(($this->serviceStartCounter - $this->numPerPage) >= 0) {
				$this->targetData("servicePrev", $this->servicePrev);
			}
			else {
				$this->targetData("servicePrev", "");
			}
		    
		    if($totalTroubledServices > 0) {
				$buffer = "Showing " . ($this->serviceStartCounter + 1) . " To ";
				
				if(($this->serviceStartCounter + $this->numPerPage) >= $totalTroubledServices) {
				    $buffer .= $totalTroubledServices;
				}
				else {
					$buffer .= ($this->serviceStartCounter + $this->numPerPage);
				}
				$buffer .= " of " . $totalTroubledServices;		
		    }
		    else {
		    	$buffer = "There are no troubled services at this time.";
		    }
			$this->targetData("info", $buffer);
			
			// WE NEED TO TEST TO SEE IF WE ARE OUT OF BOUNDS
			if($this->serviceStartCounter >= $totalTroubledServices && $totalTroubledServices != 0) {
			    for($this->serviceStartCounter = 0; $this->serviceStartCounter < $totalTroubledServices; $this->serviceStartCounter += $this->numPerPage) {}
				$this->serviceStartCounter -= $this->numPerPage;
			}
			
			if($totalTroubledServices == 0) {
				$this->serviceStartCounter = 0;
			}
		    
		}


		if (count($serviceList))
		{
			
		    foreach ($serviceList as $service)
		    {
		        $this->TroubledServices[] = $service['ServiceStatusID'];
		        $tempItem = new GWWidgetsTroubledServicesListWidgetItem($service['ServiceStatusID']);
	    		$this->items[] = $tempItem;
		    }
		}

		// Let's build our target
		ob_start();
		if(count($this->items)) {
			
			// $temp = new InfoDialog("servicestartCounter".$this->serviceStartCounter.",totaltroubled".$totalTroubledServices.",numperpage".$this->numPerPage);
			// $temp->show();

			// for ($i = $this->serviceStartCounter; $i < $this->numPerPage;$i++){
			// 	$item = $this->items[$i];
			// 	$item->Draw();
			// }
			
			foreach($this->items as $item) {
				$item->Draw();
			}
		}
		$buffer = ob_get_contents();
		ob_end_clean();
		$this->targetData("contents", $buffer);
	}
	
	/*
	public function Draw() {
		$this->printTarget("contents");
		
	}
	*/
	
}

?>
