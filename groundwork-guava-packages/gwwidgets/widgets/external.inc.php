<?php
require_once('externalconfig.php');

class ExternalWidget extends GuavaWidget {
	
	/**
	 * We'll use an IFrame object to view our external resource.  IFrame 
	 * renders an iframe element into the browser, but it's also possible to 
	 * be resized and reference a new URL on the fly.
	 *
	 * @var IFrame
	 */
	private $iframe;
	/**
	 * The current location we are pointing to.
	 *
	 * @var string
	 */
	private $location;

	private $_timer;
	private $_updateFrequency;

	
	/**
	 * We'll call our setConfigClass which references the ClassName of the ConfigureDialog which 
	 * will be used to configure this widget.  We'll also set our default value of our location 
	 * which is a helper HTML file.  We'll also initialize our IFrame object to point to our 
	 * new location.
	 *
	 */
	public function init() {
		$this->setConfigClass("ExternalWidgetConfigureDialog");
		$this->location = 'http://groundworkconnect.com';
		//$this->_updateFrequency = "--never--";
		$this->iframe = new IFrame($this->location);
	
	}
	
	/**
	 * Perform normal cleanup.
	 *
	 */
	public function unregister() {
		parent::unregister();

		// Cleanup our objects
		if ($this->iframe) {
			$this->iframe->unregister();
			unset($this->iframe);
		}
	}
	
	/**
	 * We're not using a template for this widget, so we'll just draw the contents of 
	 * our IFrame object.
	 *
	 */
	public function Draw() {
		$this->iframe->Draw();
	}
	
	/**
	 * A helper method to return our location.  It's used by our Configure dialog.
	 *
	 * @return string
	 */
	public function getLocation() {
		return $this->location;
	}

	public function setLocation($loc){

		$this->location = $loc;
		$this->iframe->setSrc($this->location);
	}

	public function setTitle($title){
		$this->setName($title);
	}
	/**
	 * If this widget is to be configured, it must handle 'configured' ActionEvents 
	 * triggered from our configure dialog.  We get the dialog's new location and 
	 * set it as our IFrame's new location.
	 *
	 * @param ActionEvent $event
	 */
	public function actionPerformed($event) {
		if($event->getAction() == "configured") {
			$this->location = $event->getSource()->getLocation();
			$this->setName($event->getSource()->getWidgetTitle());
		//	$this->_updateFrequency = $event->getSource()->getUpdateFrequency();
			$this->iframe->setSrc($this->location);
		}
	}
	
	/**
	 * If we want to store our configuration so applications like dashboards can 
	 * pre-configure this widget, we must override the getConfigObject() to return 
	 * the data to configure this widget.  It can be any type of object, as it is 
	 * serialized upon saving and de-serialized and passed to our loadConfig() 
	 * method.
	 *
	 * @return string
	 */
	public function getConfigObject() {
		$configObj = array();
		$configObj['location']        = $this->location;
		$configObj['title']           = $this->getName();
		//$configObj['updateFrequency'] = $this->_updateFrequency;

		return $configObj;
	}
	
	/**
	 * loadConfig() is called when loading this widget with a pre-defined configuration.  
	 * We know it's a string, so we'll assign it to our location and our IFrame object.
	 *
	 * @param string $configObject
	 */
	public function loadConfig($configObject) {
		$this->location         = $configObject['location'];
		//$this->_updateFrequency = $configObject['updateFrequency'];

	//	if ($this->_updateFrequency != "--never--") {
	//		$this->_timer = new GuavaTimer(0, $this->_updateFrequency, $this, "update");
	//	}

		$this->iframe->setSrc($this->location);
		$this->setName($configObject['name']);
	}

	public function update() {
		if ($this->iframe) {
		//	$this->iframe->setSrc($this->location);
		}
	}
}

?>
