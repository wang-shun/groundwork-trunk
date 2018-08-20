<?php
/*
Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
All rights reserved. Use is subject to GroundWork commercial license terms. 
*/

class WidgetsTestView_A extends View {
    private $widgets = null;

    function __construct() {
	parent::__construct("gwwidgetstest_a");
    }

    public function init() {
	global $widgetdaemon;

	$this->widgets = array();

	/*
	$this->widgets[] = $widgetdaemon->createWidget("widgets.TestWidgetA", 
						       "Test Widget", 
						       1, 50, 100, 300, 200, true, true, false);

	$this->widgets[] = $widgetdaemon->createWidget("widgets.TestWidgetB", 
						       "Test Template Widget", 
						       0, 200, 500, 400, 200, true, true, false);

	$this->widgets[] = $widgetdaemon->createWidget("widgets.ScrollerWidget", 
						       "Scroller Widget", 
						       5, 100, 200, 550, 100, true, true, false);

	*/
	$this->widgets[] = $widgetdaemon->createWidget("widgets.ActiveImage", 
						       "San Francisco", 
						       150, 100, 2, 100, 10, false, false, false);

    }

    public function close() {
	foreach($this->widgets as $widget) {
	    $widget->Destroy();
	}
    }

    public function Draw() {
	global $widgetdaemon;

	    ?>
	    <h1>Widget Test</h1>
		 Number Of Widgets: <?=count($this->widgets);?><br />
		 Widget Daemon: <?=$widgetdaemon;?>
		
	    <?php
		foreach($this->widgets as $widget) {
			$widget->DrawOpen();
			$widget->Draw();
			$widget->DrawClose();
		}
    }
}

?>