<?php
/*
Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
All rights reserved. Use is subject to GroundWork commercial license terms. 
*/ 

class WidgetDaemon extends SystemModule {
    private $_widgetRegistry;

    function __construct() {
    	global $widgetdaemon;
    	parent::__construct("widgetdaemon");
		$this->_widgetRegistry = array();
    	$widgetdaemon = $this;
    }

    function init()     {
    	global $widgetdaemon;
    	global $guava;
    	$widgetdaemon = $this;
		$guava->registerScriptModulePath("gwwidgets", "../../packages/widgets/javascript/");
		$guava->registerScriptModule("gwwidgets.core.WidgetManager");
    }

    function restart()  {
    	global $widgetdaemon;
    	$widgetdaemon = $this;
    }

    /**
     * Returns array of widgets.
     *
     */
    function getWidgets() {
    	$returnArray = array();
    	foreach($this->_widgetRegistry as $name => $item) {
    		$returnArray[] = array('name' => $name, 'description' => $item['description']);
    	}
    	return $returnArray;
    }
    
    function register($aName, $aClassName, $description) {

		// only register widget/class mappings once
		if (!$this->alreadyRegistered($aName)) {
	
		    // make sure that class definition exists in the current 
		    // scope before proceeding
		    if (class_exists($aClassName)) {
				$this->_widgetRegistry[$aName] = array('class' => $aClassName, 'description' => $description);
				return true;
		    }
		    else {
		    	throw new GuavaException("Widget Class " . $aClassName . " Does Not Exist.");
		    }
		}

	return false;
    }

    function alreadyRegistered($aName) {
		return isset($this->_widgetRegistry[$aName]);
    }

    function unRegister($aName) {
	if (isset($this->_widgetRegistry[$aName])) { unset($this->_widgetRegistry[$aName]); }
    }

    function createWidget($registryName, $widgetProps,    $configObject = null) {

    	
		if (!$widgetClass = $this->_widgetRegistry[$registryName]['class']) {
			throw new GuavaException("Widget " . $registryName . 
						 " Does Not Exist In Widget Registry.");
		}
	
		$widget = new $widgetClass($widgetProps, $configObject);
		
        return $widget;
    }

    function __destruct() { unset ($this->_widgetRegistry); }
}

?>