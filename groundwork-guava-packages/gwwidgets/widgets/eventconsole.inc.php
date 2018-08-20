<?php
/*
Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
All rights reserved. Use is subject to GroundWork commercial license terms. 
*/

class GWWidgetsConsoleWidget extends GuavaWidget implements ActionListener   {
	
	private $console;

	
	public function init() {
		$this->console = new ConsoleObject();
		$this->console->getAllEvents();
		//$this->console->addRefreshButton();
	}
	
	public function unregister() {
		parent::unregister();
		if (isset($this->console))
		  $this->console->unregister();
		$this->console = null;
	}
	
	/**
	 * Enter description here...
	 *
	 * @param ActionEvent $event
	 */
	public function actionPerformed($event) {
		/*
		if($event->getAction() == "configured") {
			if(Count($this->items)) {
				foreach($this->items as $item) {
					$item->unregister();
				}
			}
			$this->items = array();
			$this->hosts = array();
			$tempList = $event->getSource()->getConsole();
			if(count($tempList)) {
				foreach($tempList as $host) {
					$tempHostItem = new GWWidgetsConsoleWidgetItem($host);
					$this->hosts[] = $host;
					$this->items[] = $tempHostItem;
				}	
			}
		}
		$this->update();
		*/
	}
	
	/*
	public function loadConfig($configObject) {
		// configObject should be an array of Host names
		foreach($configObject as $host) {
			$tempHostItem = new GWWidgetsConsoleWidgetItem($host);
			$this->hosts[] = $host;
			$this->items[] = $tempHostItem;
		}
		
	}
	*/
	
	
	public function Draw() {
		$this->console->Draw();
		
	}
	
}

?>
