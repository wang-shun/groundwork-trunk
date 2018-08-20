<?php
/*
Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
All rights reserved. Use is subject to GroundWork commercial license terms. 
*/

class ActiveImage extends GuavaWidget {
    private $_src          = null;
    private $_statusImages = null;
    private $_status       = null;
    private $_refreshTimer = null;

    protected function init() {
	$this->_statusImages = array();

	$this->_statusImages['statusUp']      = GUAVA_WS_ROOT . 'packages/widgets/images/statusUp.jpg';
	$this->_statusImages['statusDown']    = GUAVA_WS_ROOT . 'packages/widgets/images/statusDown.jpg';
	$this->_statusImages['statusNeutral'] = GUAVA_WS_ROOT . 'packages/widgets/images/statusNeutral.jpg';

	$this->_src = $this->_statusImages['statusNeutral'];

	$this->_refreshTimer = new GuavaTimer(0, 15, $this, "refresh");
    }

    public function refresh() {
	global $guava;
	$guava->console("view refresh");
    }

    public function close() {
	$this->_refreshTimer->disable();
	$this->_refreshTimer = null;
    }

    public function Draw() {
	?>
         <img align="<?=$this->__CSSalign;?>" src="<?=$this->src;?>" id="<?=$this->getIdentifier();?>" alt="" <?php
	 if(count($this->clickListeners)) {
			?>onclick="addMessage('framework', 'object', [{name: 'identifier', type: 'string', value: '<?=$this->getIdentifier();?>'}, {name: 'method', type: 'string', value: 'Invoke'}]); sendMessageQueue();" /><?php
	 }
	 else {
			?>onclick="null" /><?php
			      }
    }
}

?>