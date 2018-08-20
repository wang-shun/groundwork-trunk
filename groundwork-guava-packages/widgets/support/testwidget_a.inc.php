<?php
/*
Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
All rights reserved. Use is subject to GroundWork commercial license terms. 
*/


require_once(GUAVA_FS_ROOT . 'packages/widgets/support/guavawidget.inc.php');

class TestWidgetA extends GuavaWidget  {

    public function Draw() {
        ?>
        My Frames Being Enabled? <?=(string)$this->hasFrames();?>
	<br />
	<div align="center">Test Widget Being Drawn with overwritten Draw()</div>
	<br />
        <?php
    }
}

?>