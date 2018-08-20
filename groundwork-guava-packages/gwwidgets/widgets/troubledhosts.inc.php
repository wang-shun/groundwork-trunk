<?php
/*
Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
All rights reserved. Use is subject to GroundWork commercial license terms. 
*/

require_once('DAL/HostDAL.inc.php');

class GWWidgetsTroubledHostsListWidgetItem extends GuavaObject implements ActionListener {
	
	private $TroubledHost;
	
	private $downQuery, $unreachableQuery;
	
	private $downTroubledHosts, $unreachableTroubledHosts;
	
	private $configInfo;
	
	private $notFound;
	
	private $troubledHostLink;
	
	private $status;
	
	/**
	 * Data Access Layer Instances used in this class.
	 */
	private $hostDAL;
	
	public function __construct($TroubledHost) 
    {
        global $foundationModule;
        global $sv2;
		
		parent::__construct();
		
		$this->TroubledHost = $TroubledHost;		
		
		$this->setTemplate(GUAVA_FS_ROOT . 'packages/gwwidgets/templates/troubledhostslistitem.xml');
		
		$this->bind("self", $this);
								
		$this->troubledHostLink = new TextLink($this->TroubledHost);
		$this->troubledHostLink->addActionListener("click", $this);
		$this->bind("troubledHostLink", $this->troubledHostLink);
		
        try {
            //Initialize DAL's		
            $this->hostDAL = new HostDAL($foundationModule->getWebServiceURL());
		
            // Let's first check to see if TroubledHost exists
            $TroubledHostInfo = $this->hostDAL->getHostByHostName($TroubledHost);
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
    		    $dialog = new ErrorDialog("An error occurred retrieving host ".$TroubledHost);
    		    $dialog->show();
    		    $sv2->setErrorOccurred(true);
		    }
		}
		
		if($TroubledHostInfo == null) 
		{
			// TroubledHost does not exist
			$this->notFound = true;
		}
		else {
			$this->notFound = false;					
		}
		$this->update();
	}
	
	
	public function unregister() {
		parent::unregister();
		$this->troubledHostLink->unregister();
	}
	
	public function actionPerformed($event) {
		global $sv2;
		global $guava;
		if(isset($sv2) && !$this->notFound) {
			$component = $sv2->createHostComponent($this->TroubledHost);
			$component->expand();
			$component->setCloned(true);
			$guava->objectView($component);
		}
	}
	
	public function getTroubledHost() {
		return $this->TroubledHost;
	}
	
	public function isNotFound() {
		return $this->notFound;
	}
	
	public function getStyle() {
		return "background-color: #00ff00;";
	}
	
	public function getTroubledHostsDown() {
		return (string)$this->downTroubledHosts;
	}
	
	public function getTroubledHostsUnreachable() {
		return (string)$this->unreachableTroubledHosts;
	}
	
	public function getTroubledHostsOK() {
		return ((count($this->configInfo['members']) - ($this->downTroubledHosts + $this->unreachableTroubledHosts)));
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
				return "packages/gwwidgets/images/bg-yellow.gif";
				break;
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
				return "packages/gwwidgets/images/host-yellow.gif";
				break;
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
	
	public function update() 
    {
        global $foundationModule;
        global $sv2;
        
		if (!$this->notFound) 
		{
		    try {
    		    if (!isset($this->hostDAL))
    		      $this->hostDAL = new HostDAL($foundationModule->getWebServiceURL());
    		    $tempInfo = $this->hostDAL->getHostByHostName($this->TroubledHost);
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
        		    $dialog = new ErrorDialog("An error occurred retrieving troubled host ".$this->TroubledHost);
        		    $dialog->show();
        		    $sv2->setErrorOccurred(true);
    		    }
    		}
		    if ($tempInfo != null)
		    {
		        $this->status = $tempInfo['MonitorStatus']->Name;
		    }		    
		}
	}	
}


class GWWidgetsTroubledHostsListWidget extends GuavaWidget implements ActionListener   {
	
	private $items;	// Array

	private $troubledHosts;
	 
	
	private $numPerPage;
	/**
	 * This is the start of where we are in our service list.
	 *
	 * @var integer
	 */
	private $hostStartCounter;
	
	/**
	 * Our Image to handle our Next link for services.
	 *
	 * @var Image
	 */
	private $hostNext;
	/**
	 * Our Image to handle our Prev link for services.
	 *
	 * @var Image
	 */
	private $hostPrev;

	
	
	/**
	 * Data Access Layer Instances used in this class.
	 */
	private $hostDAL = null;
	
	public function init()
	{
		$this->setTemplate(GUAVA_FS_ROOT . "packages/gwwidgets/templates/troubledhosts.xml");
		$this->troubledHosts = array();
		
		$this->hostStartCounter = 0;
		$this->numPerPage = 50;
		
		$this->hostNext = new Image(GUAVA_WS_ROOT . 'packages/sv2/images/next_on.gif');
		$this->hostNext->addActionListener("click", $this);
		$this->hostPrev = new Image(GUAVA_WS_ROOT . 'packages/sv2/images/prev_on.gif');
		$this->hostPrev->addActionListener("click", $this);
		
		$this->update();
		
		$this->items = array();
		 
	}
	
	public function unregister() {
		parent::unregister(); 
		if(count($this->items)) {
		foreach($this->items as $item) {
			$item->Destroy();
		}
		}
		$this->items = array();
		
		$this->hostNext->unregister();
		$this->hostPrev->unregister();
		
        if ($this->hostDAL == null)
		  unset($this->hostDAL);
	}
	
		
	public function actionPerformed($event) {
		if($event->getSource() === $this->hostNext) {
			$this->hostStartCounter += $this->numPerPage;
		}
		if($event->getSource() === $this->hostPrev)
			$this->hostStartCounter -= $this->numPerPage;
		$this->update();
	}
	
	public function update() {
		global $guava;
		global $foundationModule;
		global $sv2;
				
		// Clear existing
		$this->troubledHosts = array();
		
		if(count($this->items)) {
			foreach($this->items as $item) {
				$item->unregister();
			}
		}
		$this->items = array();
/*		
		if($this->serviceStartCounter < 0) {
			$this->serviceStartCounter = 0;
		}*/
		if($this->hostStartCounter < 0) {
			$this->hostStartCounter = 0;
		}
		// NOTE:  We need to add pagination to this widget.  Returning all may be a performance issue.
        try {
    		if ($this->hostDAL == null)
    		    $this->hostDAL = new HostDAL($foundationModule->getWebServiceURL());
    		$results = $this->hostDAL->getTroubledHosts($this->hostStartCounter, $this->numPerPage);
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
    		    $dialog = new ErrorDialog("An error occurred retrieving troubled hosts ");
    		    $dialog->show();
    		    $sv2->setErrorOccurred(true);
		    }
		}
				
		if ($results != null)
		{
			$totalTroubledHosts = $results['Count'];
			$this->targetData("count", $results['Count']);

		    $hostList = $results['Hosts']; 
		    
			if(($this->hostStartCounter + $this->numPerPage) < $totalTroubledHosts) {
				$this->targetData("hostNext", $this->hostNext);
			}
			else {
				$this->targetData("hostNext", "");
			}
			if(($this->hostStartCounter - $this->numPerPage) >= 0) {
				$this->targetData("hostPrev", $this->hostPrev);
			}
			else {
				$this->targetData("hostPrev", "");
			}
		    
		    if($totalTroubledHosts > 0) {
				$buffer = "Showing " . ($this->hostStartCounter + 1) . " To ";
				
				if(($this->hostStartCounter + $this->numPerPage) >= $totalTroubledHosts) {
				    $buffer .= $totalTroubledHosts;
				}
				else {
					$buffer .= ($this->hostStartCounter + $this->numPerPage);
				}
				$buffer .= " of " . $totalTroubledHosts;		
		    }
		    else {
		    	$buffer = "There are no troubled hosts at this time.";
		    }
			$this->targetData("info", $buffer);
			
			// WE NEED TO TEST TO SEE IF WE ARE OUT OF BOUNDS
			if($this->hostStartCounter >= $totalTroubledHosts && $totalTroubledHosts != 0) {
			    for($this->hostStartCounter = 0; $this->hostStartCounter < $totalTroubledHosts; $this->hostStartCounter += $this->numPerPage) {}
				$this->hostStartCounter -= $this->numPerPage;
			}
			
			if($totalTroubledHosts == 0) {
				$this->hostStartCounter = 0;
			}

		    
		}
		
		if(count($hostList)) {
			foreach($hostList as $host) 
			{				
			    $hostName = $host['Name'];
				$this->troubledHosts[] = $hostName;
				$tempItem = new GWWidgetsTroubledHostsListWidgetItem($hostName);
				$this->items[] = $tempItem;
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
	
	
}

?>
