<?php
/*
Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
All rights reserved. Use is subject to GroundWork commercial license terms. 
*/
class GWWidgetsPerformanceGraphConfigureDialog extends GuavaWidgetConfigureDialog implements ActionListener  {
	private $_hostSelectField;
	private $_servicesSelectField;
	private $_graphSelectField;
	private $_graphTitleField;
	private $_startDateField;
	private $_defaultEndDate;
	private $_updateFrequencyField;
	private $_addButton;
	public $inputHost;
	public $_perfGraphList;
	private $searchButton;
 	public $actualWidget;
 	private $hostname;
	private $cgDialog;
	
	public function __construct($source) {
		global $guava;
		parent::__construct($source);
		$this->actualWidget = $source;
		
		$this->setTemplate('/usr/local/groundwork/core/guava/htdocs/guava/packages/gwwidgets/templates/perfgraphconfig.xml');
	    $this->_defaultEndDate = 'n';
 

		$aGraphList           =  $source->getPerformanceGraphsList();
		$this->_perfGraphList =& $this->addDeleteLinksToGraphList($aGraphList);

		$this->inputHost = new InputTextSuggestControl("Host",$this);
		$this->targetData("inputHost",$this->inputHost);	

		$this->searchButton = new Button("Add a Graph for this Host");
		$this->targetData("searchButton",$this->searchButton);
	    $this->searchButton->addActionListener("click", $this);

		$this->_updateFrequencyField = new Select();
		$this->_updateFrequencyField->addOption(  60, "Every  1 minute");
		$this->_updateFrequencyField->addOption( 300, "Every  5 minutes");
		$this->_updateFrequencyField->addOption( 600, "Every 10 minutes");
		$this->_updateFrequencyField->addOption( 900, "Every 15 minutes");
		$this->_updateFrequencyField->addOption(1800, "Every 30 minutes");
		$this->_updateFrequencyField->addOption(2700, "Every 45 minutes");
		$this->_updateFrequencyField->addOption(3600, "Every hour");
		$this->_updateFrequencyField->setValue(300);

		$this->_updateFrequencyField->addActionListener("click", $this);
		$this->targetData("updateFrequencyField", $this->_updateFrequencyField->toString());

		 
		$this->renderPerfGraphList();
	}
	

	public function unregister() {
		parent::unregister();
		$this->searchButton->unregister();
		$this->inputHost->unregister(); 
	}

	public function getPerformanceGraphsList() {
		return $this->_perfGraphList;
	}

	public function getUpdateFrequency() {
		return $this->_updateFrequencyField->getValue();
	}
	
	public function actionPerformed($event) {
		global $foundationModule;
		global $sv2;
		parent::actionPerformed($event);
		$source = $event->getSource();
		$label = "";
		if(method_exists($source,"getLabel")){
			$label = @$source->getLabel();
			}

		if ($source === $this->_updateFrequencyField) {
			$refreshValue = $source->getValue();			

			if($refreshValue == "---"){ 
				$this->actualWidget->disableRefresh();
			}
			else{
				$this->actualWidget->setRefreshRate($refreshValue);
			}
	
		}

		else if ($label == "Delete"){
			$index = $event->getSource()->getIdentifier();
 
			
			// Remove the corresponding graphAttributes from the perfGraphList
			unset($this->_perfGraphList[$index]);

			// Redraw the list
			$this->renderPerfGraphList();
		}
		
		else if ($label == "Add a Graph for this Host"){ 
			$hostname = $this->inputHost->getValue();
			if($hostname == "") {
					$dialog = new ErrorDialog("Host Name cannot be blank.");
					$dialog->show();
					return;
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
		
 			$this->cgDialog = new ConfigGraphDialog($this);
			$this->cgDialog->show();
			 
		}//END ADD A GRAPH FOR THIS HOST
	 
		else if (get_class($event->getSource()) == "TextLink") {
			// We've clicked on a Delete Link
			// Get the ID of the TextLink
			$index = $event->getSource()->getIdentifier();
			
			$info = new InfoDialog("Delete link clicked for id=" . $index);
			$info->show();
			
			// Remove the corresponding graphAttributes from the perfGraphList
			unset($this->_perfGraphList[$index]);

			// Redraw the list
			$this->renderPerfGraphList();
		}
		else {

 
			/**
			 * If something ELSE triggered the event (usually the OK or close
			 * button, then let's hide this dialog and call our unregister
			 * method.  You must always do this for a ConfigureDialog in your
			 * actionPerformed handler.
			 */

			$this->hide();
			$this->unregister();
			if(isset($this->cgDialog)){
				$this->cgDialog->hide();
				$this->cgDialog->unregister();
			}
		}
	}


	private function extractGraphInfo() {
		$fields = explode("^", $this->_graphSelectField->getValue());

		$resultArray               = array();
		$resultArray['DataTypeID'] = $fields[0];
		$resultArray['GraphLabel'] = $fields[1];

		return $resultArray;
	}

 
	public function renderPerfGraphList() {
		$outputBuffer = "";

		foreach ($this->_perfGraphList as $aGraph) {
			$outputBuffer .= '<div style="background: #dddddd; height: 35px; border-width: 1px 0px 1px 0px; border-style: solid; border-bottom-color: black; border-left-color: grey; border-right-color: black; border-top-color: grey;"><table width="100%"><tr><td>' . $aGraph['title'] . ' on ' . $aGraph['HostName'] . ': '. $aGraph['graphLabel'] . '</td>' . '<td align="right">' . $aGraph['DeleteLink']->toString() . '</td></tr></table></div>';
		}

		$this->targetData("performanceGraphsList", $outputBuffer);
	}

	public function &addDeleteLinksToGraphList(&$aGraphList) {
		$newPerfGraphList = array();

		if (count($aGraphList)) {
			foreach ($aGraphList as &$aGraph) {
				$deleteLink = new TextLink("Delete");
				$deleteLink->addActionListener("click", $this);

				$aGraph['DeleteLink'] = $deleteLink;
				$index = $deleteLink->getIdentifier();
				$newPerfGraphList[$index] = &$aGraph;
			}
		}

		return $newPerfGraphList;
	}
}

?>
