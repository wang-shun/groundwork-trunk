<?php
/*
Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
All rights reserved. Use is subject to GroundWork commercial license terms. 
*/

require_once(GUAVA_FS_ROOT . 'packages/widgets/support/guavawidget.inc.php');

class TestWidgetB extends GuavaWidget {

	private $myButton;
	private $clickCounter;
	
	protected function init() {
		$this->clickCounter = 0;
		$this->targetData("counter", (string)$this->clickCounter);
		$this->myButton = new Button("Click Me");
		$this->myButton->addClickListener("click", $this, "clickHandler");
		$this->bind("myButton", $this->myButton);
		$this->setTemplate(GUAVA_FS_ROOT . 'packages/widgets/templates/test_widget_b.xml');
	}
	
	public function clickHandler($guavaObject, $parameter = null) {
		$this->targetData("counter", (string)++$this->clickCounter);
	}
}


?>