<?php
/*
Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
All rights reserved. Use is subject to GroundWork commercial license terms. 
*/
 class ConfigGraphDialog extends Dialog implements ActionListener {
	
	private $addButton;
	private $__cancelButton;
	private $__source;
	private $hostName;
	private $source;
	private $serviceClass;
	private $graphType;
	private $graphSize;
	private $timeSpan;

	public function __construct($source) {
		parent::__construct();

		$this->hostName = $source->inputHost->getValue();
		$this->addUIElements();
		$this->__source = $source;
		$this->setTemplate('/usr/local/groundwork/core/guava/htdocs/guava/packages/gwwidgets/templates/graphconfig.xml');
		$this->addActionListener('configured', $source);
	}

	private function addUIElements(){
		$this->addButton = new Button("Add This Graph");
		$this->addButton->addActionListener("click", $this);
		$this->__cancelButton = new Button("Cancel");
		$this->__cancelButton->addActionListener("click", $this);
		
		// Service Class
		$this->serviceClass = new Select();
		$this->serviceClass->addOption('--empty--','--Start Here--');
		$serviceList = $this->getServiceList();
		foreach ($serviceList as $service) {
			$this->serviceClass->addOption($service['Description'], $service['Description']);
		}
		$this->bind("serviceClass",$this->serviceClass);
		$this->serviceClass->addActionListener("click",$this);
		
		// Graph Type
		$this->graphType = new Select();
		$this->graphType->addOption('--empty--', '----------');
		$this->bind("graphType",$this->graphType);
		$this->graphType->addActionListener("click",$this);
		
		// Graph Title
		$this->graphTitle = new inputtext(45,64);
		$this->bind("graphTitle",$this->graphTitle);
		
		// Time Span
		$this->timeSpan = new Select();
		$this->timeSpan->addOption('n-1h','Last 1 hour');
        $this->timeSpan->addOption('n-12h','Last 12 hours');
		$this->timeSpan->addOption('n-1d','Last Day');
		$this->timeSpan->addOption('n-1w','Last Week');
        $this->timeSpan->addOption('n-1m','Last 1 month');
        $this->timeSpan->addOption('n-6m','Last 6 months');
        $this->timeSpan->addOption('n-1y','Last 1 year');
		$this->bind("timeSpan",$this->timeSpan);
		$this->timeSpan->addActionListener("click",$this);

		// Graph Size
		$this->graphSize = new Select();
		$this->graphSize->addOption('45x100','Thumbnail');
        $this->graphSize->addOption('70x300','Small');
        $this->graphSize->addOption('100x400','Medium(default)');
        $this->graphSize->addOption('150x500','Large');
		$this->graphSize->setValue('100x400');
		$this->graphSize->addActionListener("click",$this);	
		$this->bind("graphSize",$this->graphSize);
	
	}
	public function getSource() {
		return $this->__source;
	}
	
	public function unregister() {
		parent::unregister();
		$this->addButton->unregister();
		$this->__cancelButton->unregister();
	}
	
	public function actionPerformed($event) {
	$label = "";
			if(method_exists($source,"getLabel")){
			$label = @$source->getLabel();
			}
	
			if($label == "Delete"){
			$info = InfoDialog("Delete clicked CGD");
			$info->show();
			}
	if($event->getSource() === $this->__cancelButton) {
			$this->hide();
			$this->unregister();
		}
	 
		else if($event->getSource() === $this->serviceClass){
			
			if($this->serviceClass->getValue() != "--empty--") {	
				$this->addGraphTypeOptions();
			}
			list($crap,$title) = split('\^',$this->graphType->getValue()); 
			$this->graphTitle->setValue($this->hostName . ": " . $title);
		}
		else if($event->getSource() === $this->graphType){
			 $this->graphTitle->setValue($this->hostName . $this->graphType->getValue());
		}
		else if($event->getSource() === $this->addButton){
		 		$this->hide();
				$deleteLink = new TextLink("Delete");
				$deleteLink->addActionListener("click", $this->__source);

		  
				$graphInfo = $this->extractGraphInfo();

				$aGraph                       = array();
				$aGraph['HostName']           = $this->hostName;
				$aGraph['datatype_id']        = $graphInfo['DataTypeID']; 	
				$aGraph['graphLabel']         = $graphInfo['GraphLabel'];
				$aGraph['ServiceDescription'] = $this->serviceClass->getValue();
				$aGraph['startDate']          = $this->timeSpan->getValue();
				$aGraph['endDate']            = 'n';

				$aGraph['title']              = $this->graphTitle->getValue();

				$geom = $this->graphSize->getValue();
				list($height,$width) = split('x',$geom);
				$aGraph['height']             = $height;
				$aGraph['width']              = $width;

				$aGraph['DeleteLink']         = $deleteLink;

				$index = $deleteLink->getIdentifier(); 
	 
		 		
				  $this->__source->_perfGraphList[$index] = &$aGraph;

			 	$this->__source->renderPerfGraphList();
 			 	
		}
	}

protected function DialogDraw() {
			?>
		<div id="<?=$this->getIdentifier();?>.__dialog" dojoType="Dialog" bgColor="<?=$this->bgColor;?>" bgOpacity="<?=$this->bgOpacity;?>" toggle="<?=$this->toggle;?>" toggleDuration="<?=$this->toggleDuration;?>">
		
		<div style="padding: 10px; width: 350px;">
		<?php
		$this->Draw();
		?>
		<hr />
			<div align="right">
				<?=$this->addButton->Draw();?> <?=$this->__cancelButton->Draw();?>
			</div>
		</div>
		</div>
		<?php
	}
	
private function addGraphTypeOptions() {
		global $sv2;
	 	
	 	$listOfGraphs = $sv2->obtainGraphsForService($this->hostName,$this->serviceClass->getValue());

		if (count($listOfGraphs)) {
			$this->graphType->removeAll();
			foreach ($listOfGraphs as $aGraph) {
				$value = $aGraph['datatype_id'] . "^" . $aGraph['label'];
				$label = $aGraph['label'];
				$this->graphType->addOption($value, $label);
				 $this->graphType->setValue($value);
			}
			  
		   $this->hide();
		   $this->show();
		}
}
private function extractGraphInfo() {
		$fields = explode("^", $this->graphType->getValue());

		$resultArray               = array();
		$resultArray['DataTypeID'] = $fields[0];
		$resultArray['GraphLabel'] = $fields[1];

		return $resultArray;
	}

private function getServiceList() {
		global $foundationModule;
		global $sv;
		global $sv2;
	
 try {
    					$serviceDAL = new ServiceDAL($foundationModule->getWebServiceURL());
    					$results = $serviceDAL->getServicesByHostName($this->hostName);	            
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
                		    $dialog = new ErrorDialog("An error occurred retrieving services for host ".$this->hostName);
                		    $dialog->show();
                		    $sv2->setErrorOccurred(true);
            		    }
            		}
					if ($results != null)
					{
					    $services = $results['Services'];
					}
					if($services == null) {
						$dialog = new ErrorDialog("No services found for host " . $this->hostName);
						$dialog->show();
					}

            		
	return $services;

	 }
	
}
?>
