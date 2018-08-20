<?php
/*
Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
All rights reserved. Use is subject to GroundWork commercial license terms. 
*/
class GWWidgetsPerformanceGraphWidget extends GuavaWidget implements ActionListener   {

	private $_perfGraphList;
	private $_perfGraphItems;
	private $_timer;

	private $_counter;

	public function init() {
		$this->setConfigClass("GWWidgetsPerformanceGraphConfigureDialog");

		$this->setTemplate('/usr/local/groundwork/core/guava/htdocs/guava/packages/gwwidgets/templates/perfgraph.xml');

		$this->_perfGraphList  = array();
		$this->_perfGraphItems = array();
		$this->_timer          = null;

		$this->update();
	}
	
	public function unregister() {
		parent::unregister();

		if (isset($this->_timer)) { $this->_timer->disable(); }

		if (count($this->_perfGraphItems)) {
			foreach($this->_perfGraphItems as $anItem) {
				$anItem->Destroy();
			}
		}

		$this->_perfGraphItems = array();
		$this->_perfGraphList  = array();
	}
	

	public function getPerformanceGraphsList() {
		return $this->_perfGraphList;
	}

	/**
	 * Enter description here...
	 *
	 * @param ActionEvent $event
	 */
	public function actionPerformed($event) {
		if ($event->getAction() == "configured") {
			if (count($this->_perfGraphItems)) {
				foreach($this->_perfGraphItems as $anItem) {
					$anItem->unregister();
				}
			}

			$this->_perfGraphItems = array();
			$this->_perfGraphList  = array();

			$this->_updateFrequency = $event->getSource()->getUpdateFrequency();

			$aGraphList = $event->getSource()->getPerformanceGraphsList();

			if (count($aGraphList)) {
				foreach ($aGraphList as $aGraph) {
					if (isset($aGraph['DeleteLink'])) { unset($aGraph['DeleteLink']); }

					$hostName           = $aGraph['HostName'];
					$serviceDescription = $aGraph['ServiceDescription'];
					$graphLabel         = $aGraph['graphLabel'];
					$dataTypeID         = $aGraph['datatype_id'];

					$aGraphItem = new GWWidgetsPerformanceGraphWidgetItem($hostName,
													  $serviceDescription,
													  $graphLabel,
													  $dataTypeID);

					$aGraphItem->setStartDate($aGraph['startDate']);
					$aGraphItem->setEndDate($aGraph['endDate']);
					$aGraphItem->setTitle($aGraph['title']);
					$aGraphItem->setDimensions($aGraph['height'], $aGraph['width']);

					$this->_perfGraphList[]  = $aGraph;
					$this->_perfGraphItems[] = $aGraphItem;
				}
			}
		}

		$this->update();
	}

	public function getConfigObject() {
		$configObject                    = array();
		$configObject['updateFrequency'] = $this->_updateFrequency;
		$configObject['perfGraphList']   = $this->_perfGraphList;

		return $configObject;
	}
	
	public function loadConfig($configObject) {
 
		// Clear all object references
		global $sv2;
		global $guava;
		$this->_updateFrequency = $configObject['updateFrequency'];
		$this->_timer           = new GuavaTimer(0, $this->_updateFrequency, $this, "update");



		$perfGraphs = $configObject['perfGraphList'];
 
		if (count($perfGraphs)) {
			foreach ($perfGraphs as $aGraph) {
				$hostName           = $aGraph['HostName'];
				$serviceDescription = $aGraph['ServiceDescription'];
				$graphLabel         = $aGraph['graphLabel'];
				//$dataTypeID         = $aGraph['datatype_id'];
				//patch here to reconstruct proper datatype_id after upgrade;
 				
				$rrdpath = "/usr/local/groundwork/rrd/" . $aGraph['HostName'] . "_" . $aGraph['ServiceDescription'] . ".rrd";
				$rrdpath = str_replace(" ","_",$rrdpath);

				$query = "select datatype_id from monarch.datatype where location = '" . $rrdpath . "'";
			 
								 
				$result = $sv2->dbConnection->Execute($query);
				$dataTypeID = $result->fields['datatype_id'];		
				 		
				$aGraphItem = new GWWidgetsPerformanceGraphWidgetItem($hostName,
												  $serviceDescription,
												  $graphLabel,
												  $dataTypeID);

				$aGraphItem->setStartDate($aGraph['startDate']);
				$aGraphItem->setEndDate($aGraph['endDate']);
				$aGraphItem->setTitle($aGraph['title']);
				$aGraphItem->setDimensions($aGraph['height'], $aGraph['width']);

				$this->_perfGraphList[]  = $aGraph;
				$this->_perfGraphItems[] = $aGraphItem;
			}
		} //end if perfGraphs - new object

		else{
			//old config object	
		 
			// configObject should be an array of Host names
			foreach($configObject as $service) {
				// We're going to do a quick sanity check to see if a required parameter is there.
				if(!isset($service['host'])) {
					continue;
				}
				// We need to obtain the service id

				$hostName           = $service['host'];
				$serviceDescription = $service['service'];
				$graphLabel         = $service['label'];
				//$dataTypeID         = $aGraph['datatype_id'];
				//patch here to reconstruct proper datatype_id after upgrade;

				$rrdpath = "/usr/local/groundwork/rrd/" . $hostName . "_" . $serviceDescription . ".rrd";
				$rrdpath = str_replace(" ","_",$rrdpath);

				$query = "select datatype_id from monarch.datatype where location = '" . $rrdpath . "'";

				$result = $sv2->dbConnection->Execute($query);
				$dataTypeID = $result->fields['datatype_id'];		

				$aGraphItem = new GWWidgetsPerformanceGraphWidgetItem($hostName,$serviceDescription,$graphLabel,$dataTypeID);
				$aGraphItem->setStartDate('-360');
				$aGraphItem->setEndDate('n');
				$aGraphItem->setTitle($graphLabel);

				$this->_perfGraphList[]  = $aGraph;
				$this->_perfGraphItems[] = $aGraphItem;


			} //end foreach graph
					
		}//end else

		$this->update();
	}
	
	public function update() {
		if (count($this->_perfGraphItems)) {
			foreach ($this->_perfGraphItems as $anItem) {
				$anItem->update();
			}
		}

		// Let's build our target
		ob_start();
		if (count($this->_perfGraphItems)) {
			foreach($this->_perfGraphItems as $anItem) {
				$anItem->Draw();
			}
		}
		$buffer = ob_get_contents();
		ob_end_clean();

		$this->targetData("graphContents", $buffer);
	}

	public function Draw() {
		$this->printTarget("graphContents");
	}


}
?>
