<?php
/*
Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
All rights reserved. Use is subject to GroundWork commercial license terms. 
*/

require_once(GUAVA_FS_ROOT . 'packages/widgets/support/guavawidget.inc.php');

class GWWidgetsDigitalClockPanel extends GuavaWidget  {
    private $_timeZone;
    private $_myTimer;
    private $_dateStr;
    private $_timeStr;
    private $_form;

    public function __construct($widgetProps, $configObject = null) {
	parent::__construct($widgetProps);

	$this->setTemplate(GUAVA_FS_ROOT . 'packages/widgets/templates/gdc_timepanel.xml');
	$this->targetData("timePanelContents", "");

	$this->_form   = new Form();
	$this->_select = new Select();

	$this->_select->addOption('GMT-8',  'Los Angeles');
	$this->_select->addOption('GMT-6',  'Phoenix');
	$this->_select->addOption('GMT-7',  'Houston/Chicago');
	$this->_select->addOption('GMT-5',  'New York');
	$this->_select->addOption('GMT+1',  'Paris');
	$this->_select->addClickListener('timeZoneChanger', $this, 'changeTimeZones', null);

	// default to pacific timezone
	$this->_timeZone = 'GMT-8';
	$this->update();

        $this->_myTimer = new GuavaTimer(time() + 5, 5, $this, 'updateTime', null);
    }

    public function changeTimeZones($guavaObject, $parameter = null) {
	$this->_timeZone = $this->_select->getValue();

	//global $guava;
	//$guava->console('The Time Zone is now: ' . var_dump($this->_timeZone));

	$this->updateTime($guavaObject, $parameter);
    }

    public function updateTime($guavaObject, $parameters = null) {
	$this->update();
	$this->generateTimePanelContents();
	$this->printTarget("alarmPanelContents");
    }

    public function activate() {
	$this->_myTimer->enable();
    }

    public function suspend() {
	$this->_myTimer->disable();
    }

    public function update() {
	date_default_timezone_set($this->_timeZone);

	$this->_dateStr = date('D. M. n, Y');
	$this->_timeStr = date('h:m:s A');
    }

    public function Draw() {
	$this->DrawOpen();
	$this->generateTimePanelContents();
	$this->printTarget("timePanelContents");
	$this->DrawClose();
    }

    public function generateTimePanelContents() {
	ob_start();

        ?>
	<div align="center" style="height: 4px;"></div>
	<div align="center" style="font-weight: bold; font-size: 26;"><?=$this->_timeStr;?></div>
	   <div align="center" style="height: 12px;"></div>
        <div align="center" style="font-weight: bold; font-size: 14;"><?=$this->_dateStr;?></div>
	<div align="center" style="height: 6px;"></div>
	<div align="center"><hr noshade="noshade" size="1"></div>
	<?$this->_form->Open();?>
        <div align="center" style="font-weight: bold; font-size: 14;">
	   <?=$this->_select->Draw();?>
        </div>
	<?$this->_form->Close();?>
	<?php

	 $buffer = ob_get_contents();
	 ob_end_clean();

	 $this->targetData("timePanelContents", $buffer);
    }

    public function Destroy() {
	$this->_myTimer->disable();

	$this->_form->Destroy();
	$this->_select->Destroy();
	$this->_myTimer->Destroy();

	unset($this->_form);
	unset($this->_select);
	unset($this->_myTimer);
    }
}