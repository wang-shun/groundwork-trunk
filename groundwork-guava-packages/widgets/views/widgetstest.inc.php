<?php
/*
Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
All rights reserved. Use is subject to GroundWork commercial license terms. 
*/

class WidgetsTestView extends View {
	
	private $widgets;
	
	function __construct() {
		parent::__construct("gwwidgetstest");
	
	}
	
	public function init() {
		global $widgetdaemon;
		$this->widgets = array();

		$widgetprops['name'] = "Widget Test";
		$widgetprops['zIndex'] = count($this->widgets) + 1;
		$widgetprops['top'] = "30";
		$widgetprops['left'] = "100";
		$widgetprops['width'] = "300";
		$widgetprops['height'] = "400";
		$widgetprops['frames'] = true;
		$widgetprops['movable'] = true;
		$widgetprops['resizable'] = true;
		
		// Create our widgets!
		$tempWidget = $widgetdaemon->createWidget("gwwidgets.console", $widgetprops);
		$this->widgets[] = $tempWidget;
	}
	
	public function close() {
		foreach($this->widgets as $widget) {
			$widget->Destroy();
		}
	}
	
	public function Draw() {
		// Really simple
		global $widgetdaemon;
		?>
		
		<h1>Widget Test</h1>
		Number Of Widgets: <?=count($this->widgets);?><br />
		Widget Daemon: <?=$widgetdaemon;?>
		
		<div style="background: yellow; width: 900px; height: 900px; position: absolute; top: 80px; left: 80px;">
		<?php
		
		foreach($this->widgets as $widget) {
			$widget->WidgetDraw();
		}
		?>		
		</div>
		<?php
	}

	public function reload() {
		foreach($this->widgets as $widget) {
			$widget->reload();
		}
	}
	
}


?>