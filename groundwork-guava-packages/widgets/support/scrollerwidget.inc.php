<?php
/*
Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
All rights reserved. Use is subject to GroundWork commercial license terms. 
*/

require_once(GUAVA_FS_ROOT . 'packages/widgets/support/guavawidget.inc.php');

class ScrollerWidget extends GuavaWidget {
    protected function init() {
	//$this->targetData("contents", );
	$this->setTemplate(GUAVA_FS_ROOT . 'packages/widgets/templates/scroller_widget_test.xml');
    }

    //public function Draw() {
    //print "<b>Just testing</b>";
    //}
}