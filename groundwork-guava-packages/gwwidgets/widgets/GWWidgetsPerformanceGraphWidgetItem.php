<?php
/*
Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
All rights reserved. Use is subject to GroundWork commercial license terms. 
*/
class GWWidgetsPerformanceGraphWidgetItem extends GuavaObject implements ActionListener {
	
	private $graphImage;
	private $rrdtoolpath;
	private $host;
	private $service;
	private $datatype;
	private $label;
	private $datasets;
	private $dataTypeInfo;
	private $colors;
	private $endtime;
	private $starttime;
	private $scrolltime;
	private $counter;
	
	public function __construct($host, $service, $label, $datatype_id) {
		global $foundationDB;
		global $guava;
		global $sv2;

		parent::__construct();
		$this->counter = 0;
		$this->rrdtoolpath = $guava->getpreference('sv2', 'rrdtoolpath');

		$this->height = 110;
		$this->width  = 450;

		$this->host = $host;
		$this->service = $service;
		$this->label = $label;
		$this->datatype = $datatype_id;
		$this->datasets = array();
		// Let's obtain our file from perfgraphs
		$this->dataTypeInfo = $sv2->obtainDataTypeInfo($this->datatype);
		
		$this->colors = array('#0000FF','#00FF00','#FF0000','#C05599','#E092E3','#6F76C4','#9BAEFF','#818181','#C0C0C0','#8BA016','#D3DB00','#64A2B8','#8DD9E0','#7E87B7','#F3B50F','#EB6232');
		
		// Obtain data sets
		switch($this->dataTypeInfo['type']) {
			case 'RRD':
				// Exec logic
				exec($this->rrdtoolpath . ' info ' . $this->dataTypeInfo['location'], $this->output, $returnVar);
				foreach($this->output as $outputline) {
					if(preg_match('/^ds\[(\S+)]/', $outputline, $regs)) {
						// We have a data set, obtain it and assign
						if(!in_array($regs[1], $this->datasets)) {
							$this->datasets[] = $regs[1];
						}
					}
				}
				break;
		}
		
		$this->scrolltime = 7200;
		
		$this->graphImage = new Image('');
		
		$this->update();
	}
	
	public function setStartDate($aDate) {
		$this->starttime = $aDate;
	}

	public function setEndDate($aDate) {
		$this->endtime = $aDate;
	}

	public function setTitle($aTitle) {
		$this->label = $aTitle;
	}

	public function setDimensions($aHeight, $aWidth) {
		$this->height = $aHeight;
		$this->width  = $aWidth;
	}

	public function unregister() {
		parent::unregister();
		$this->graphImage->unregister();

	}
	
	public function actionPerformed($event) {
	}
	
	function prepareExecString() {
		$numOfColors = count($this->colors);
		$this->execstring = $this->rrdtoolpath . " graph - --start " . $this->starttime . " --end " . $this->endtime;
		$this->execstring .= " --color CANVAS#ffffff --title '" . $this->label . "' --width " . $this->width . " --height " . $this->height;
		$counter = 0;
		foreach($this->datasets as $dataset) {
			$this->execstring .= " DEF:" . $dataset . "=" . $this->dataTypeInfo['location'] . ":" . $dataset . ":AVERAGE ";
			$this->execstring .= " LINE2:"  . $dataset . $this->colors[$counter++] . ":" . $dataset;
			$this->execstring .= " GPRINT:" . $dataset . ":MIN:'(min=%.2lf' ";
			$this->execstring .= " GPRINT:" . $dataset . ":AVERAGE:'ave=%.2lf' ";
			$this->execstring .= " GPRINT:" . $dataset . ":MAX:'max=%.2lf)' ";
			if($counter == $numOfColors) {
				$counter = 0;
			}
		}
		$this->targetData("execstring", $this->execstring);
	}
	
	function Draw() {
		?>
<!--
		<h1><?=$this->service;?> on <?=$this->host;?>: <?=$this->label;?></h1>
-->
		<div>
			<?=$this->graphImage->Draw();?>
		</div>
		<?php
	}
	
	public function rawoutput() {
		header('Content-type: image/png');
		//print($this->execstring);
		passthru($this->execstring);
	}
	
	public function update() {
		global $foundationDB;
 
		$this->prepareExecString();
		$this->graphImage->setSrc("rpc.php?object=" . $this->getIdentifier() . "&counter=" . $this->counter++);
		error_log("Perf graph widget item updating.");
		error_log("Perf graph widget item graph image id: " . $this->graphImage->getIdentifier() . ":" . $this->counter);
	}
}
?>
