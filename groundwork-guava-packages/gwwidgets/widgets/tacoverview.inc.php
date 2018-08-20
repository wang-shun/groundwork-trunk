<?php
/*
Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
All rights reserved. Use is subject to GroundWork commercial license terms. 
*/

class GWWidgetsTacOverviewConfigureDialog extends GuavaWidgetConfigureDialog implements ActionListener  {
	
	private $overviewClassSelect;
	
	public function __construct($source) {
		global $sv;
		parent::__construct($source);
		
		$this->overviewClassSelect = new Select();
		$this->overviewClassSelect->addActionListener("click", $this);
		
		if(isset($sv)) {
			$extensions = $sv->getExtensions("overview");
			if(count($extensions)) {
				foreach($extensions as $extension) {
					$this->overviewClassSelect->addOption($extension['modname'], $extension['description']);
				}
			}
		}
		
		
	}
	
	public function getClassName() {
		return $this->overviewClassSelect->getValue();
	}
	
	public function unregister() {
		parent::unregister();
		$this->overviewClassSelect->unregister();
	}
	
	public function actionPerformed($event) {
		parent::actionPerformed($event);
		if($event->getSource() === $this->overviewClassSelect) {
			

		}
		else {
			$this->hide();
			$this->unregister();
		}
	}
	
	public function Draw() {
		?>
		<div style="width: 300px;">
		<h1>Specify Which Overview To View:</h1>
		<?php $this->overviewClassSelect->Draw(); ?><br />
		<br />
		</div>
		<?php
	}
}

class GWWidgetsTacOverviewWidget extends GuavaWidget implements ActionListener   {
	
	private $overviewObject;
	
	public function init() {
		$this->setConfigClass("GWWidgetsTacOverviewConfigureDialog");
		$this->overviewObject = null;
		$this->targetData("contents" , " No Overview Class loaded.");
	}
	
	public function unregister() {
		parent::unregister();
		$this->overviewObject = null;
	}
	
	/**
	 * Enter description here...
	 *
	 * @param ActionEvent $event
	 */
	public function actionPerformed($event) {
		if($event->getAction() == "configured") {
			$className = $event->getSource()->getClassName();
			if(class_exists($className)) {
				$tempObject = new $className();
				$this->overviewObject = $tempObject;
				//if(method_exists($tempObject, "setCloned")) {
					$tempObject->setCloned(true);
				//}
				$this->targetData("contents", $this->overviewObject);
			}	
		}
	}
	
	public function getConfigObject() {
		if(!isset($this->overviewObject)) {
			return null;
		}
		return get_class($this->overviewObject);
	}
	
	public function loadConfig($configObject) {
		// configObject should be a valid overview class
		if(class_exists($configObject)) {
			$tempObject = new $configObject();
			if(!$tempObject instanceof GuavaObject ) {
				return;
			}
			else {
				//if(method_exists($tempObject, "setCloned")) {
					$tempObject->setCloned(true);
				//}
				$this->overviewObject = $tempObject;
				$this->targetData("contents", $this->overviewObject);
			}
		}	
	}
	
	
	public function Draw() {
		$this->printTarget("contents");
		
	}
	
}

?>
