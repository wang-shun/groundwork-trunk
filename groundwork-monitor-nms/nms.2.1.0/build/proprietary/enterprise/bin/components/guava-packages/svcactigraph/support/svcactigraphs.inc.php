<?php
class SVCactiGraphsContainer extends GuavaObject {

    private $host_name;

    private $graphs;

    private $parent;

    public function __construct() {
	$this->graphs = array();
    }

    public function setParent($parent) {
	global $svcactigraphs;
	global $guava;

	$this->parent    = $parent;
	$this->host_name = $parent->getName();

	foreach($this->graphs as $graph) {
	    $graph->unregister();
	}
	$this->graphs = array();

	// Let's talk to cactigraphs and get our list of graphs for this host
	$graphs = @$svcactigraphs->obtainGraphsForHost($this->host_name);
	if (is_string ($graphs)) {
	    $guava->console($graphs);
	    $this->graphs[] = new SVCactiGraphsMessage($graphs);
	    return;
	}
	else if (is_null ($graphs)) {
	    // If this message appears in the user interface, check to see whether the Cacti database is up,
	    // and whether the Status Viewer Cacti Graphs package is properly configured.
	    $this->graphs[] = new SVCactiGraphsMessage('The Cacti database is inaccessible; see your system administrator.');
	    return;
	}

	// $guava->console("TRYING TO OBTAIN GRAPHS FOR HOST: " . $this->host_name);

	if(count($graphs)) {
	    foreach($graphs as $graph) {
		preg_match('/\s* - ([^#]+)/', $graph['title'], $regs);
		$this->graphs[] = new SVCactiGraphsGraph($regs[1], $this->host_name, $graph['local_id']);
	    }
	}
	else {
	    $this->graphs[] = new SVCactiGraphsMessage('No Cacti graphs are available for host: '.$this->host_name);
	}
    }

    public function update() {
	foreach($this->graphs as $graph) {
	    $graph->update();
	}
    }

    public function unregister() {
	foreach($this->graphs as $graph) {
	    $graph->unregister();
	}
    }

    public function Draw() {
	foreach($this->graphs as $graph) {
	    $graph->Draw();
	}
    }
}

class SVCactiGraphsMessage extends GuavaObject {

    private $errormsg;

    function __construct($message) {
	parent::__construct(GUAVA_FS_ROOT . "packages/svcactigraph/templates/cactigrapherror.xml");
	$this->errormsg = '<b>' . $message . '</b>';
	$this->targetData("errormsg", $this->errormsg);
	$this->update();
    }

    public function update() {
    }
}

class SVCactiGraphsGraph extends GuavaObject implements ActionListener {

    private $title;
    private $hostname;
    private $local_id;

    private $cactiurl;

    private $dataTypeInfo;
    private $rrdInfo;
    private $output;

    private $datasets;

    private $starttime;
    private $endtime;

    private $scrolltime;

    private $graphImage;

    private $graphTypeSelect;

    private $myForm;
    private $startTimeInput;
    private $scrollTimeInput;
    private $endTimeInput;
    private $submitButton;

    function __construct($title, $hostname, $local_id) {
	global $svperfgraphs;
	global $guava;
	parent::__construct(GUAVA_FS_ROOT . "packages/svcactigraph/templates/cactigraph.xml");
	$this->title = $title;
	$this->hostname = $hostname;
	$this->local_id = $local_id;

	$this->cactiurl = $guava->getPreference("svcactigraphs", "cactiurl");

	$this->scrolltime = 7200;
	$this->graphTypeSelect = new Select();
	$this->graphTypeSelect->addOption("scroll", "Scrolling");
	$this->graphTypeSelect->addOption("grow", "Growing");
	$this->graphTypeSelect->addOption("range", "Static");
	$this->graphTypeSelect->setValue("scroll");
	$this->graphTypeSelect->addClickListener("controlchange", $this, "controlChangeHandler");

	$this->graphImage = new Image('');

	$this->targetData("graphImage", $this->graphImage);

	$this->myForm = new Form();
	$this->myForm->addListener("rangechoose", $this, "formHandler");

	$this->scrollTimeInput = new InputText(4, 8);
	$this->scrollTimeInput->setValue($this->scrolltime);
	$this->startTimeInput = new InputDateTime();
	$this->startTimeInput->setValue(date("m/j/Y h:i:s A", $this->starttime));
	$this->endTimeInput = new InputDateTime();
	$this->endTimeInput->setValue(date("m/j/Y h:i:s A", $this->endtime));
	$this->submitButton = new Button("Update Graph");
	$this->submitButton->addActionListener("click", $this);
	$this->targetData("errormsg", ' ');
	// Create initial target
	ob_start();
	?>
	<b>Graph Type:</b> <?php $this->graphTypeSelect->Draw();?> <?php $this->scrollTimeInput->Draw(); ?> <b>Seconds</b> <?php $this->submitButton->Draw(); ?>
	<?php
	$buffer = ob_get_contents();
	ob_end_clean();
	$this->targetData("graphcontrols", $buffer);
	$this->update();
    }

    public function unregister() {
	parent::unregister();
	$this->graphTypeSelect->unregister();
	$this->graphImage->unregister();
	$this->myForm->unregister();
	$this->scrollTimeInput->unregister();
	$this->startTimeInput->unregister();
	$this->endTimeInput->unregister();
	$this->submitButton->unregister();
    }

    public function controlChangeHandler($guavaObject, $parameter = null) {
	ob_start();
	switch($this->graphTypeSelect->getValue()) {
	    case 'scroll':
		?>
		<b>Graph Type:</b>
		<?php $this->graphTypeSelect->Draw();?>
		<?php $this->scrollTimeInput->Draw(); ?> <b>Seconds</b>
		<?php $this->submitButton->Draw();  ?>
		<?php
		break;
	    case 'grow':
		$this->startTimeInput->setValue(date("m/j/Y h:i:s A", time()));
		?>
		    <b>Graph Type:</b> <?php $this->graphTypeSelect->Draw();?>
		    <b>Start:</b> <?php $this->startTimeInput->Draw();?>
		    <?php $this->submitButton->Draw(); ?>
		<?php
		break;
	    case 'range':
		$this->startTimeInput->setValue(date("m/j/Y h:i:s A", time() - 60));
		$this->endTimeInput->setValue(date("m/j/Y h:i:s A", time()));
		?>
		    <b>Graph Type:</b> <?php $this->graphTypeSelect->Draw();?>
		    <b>Start:</b> <?php $this->startTimeInput->Draw();?>
		    <b>End:</b> <?php $this->endTimeInput->Draw();?>
		    <?php $this->submitButton->Draw(); ?>
		<?php
		break;
	}
	$buffer = ob_get_contents();
	ob_end_clean();
	$this->targetData("graphcontrols", $buffer);
    }

    public function actionPerformed($event) {
	switch($this->graphTypeSelect->getValue()) {
	    case 'scroll':
		if(!is_numeric($this->scrollTimeInput->getValue()) && $this->scrollTimeInput->getValue() > 0) {
		    $this->targetData("errormsg", "Error: Invalid Seconds Value.");
		}
		else {
		    $this->scrolltime = $this->scrollTimeInput->getValue();
		    // Update graph
		    $this->update();
		}
		break;
	    case 'grow':
		$tempstarttime = strtotime($this->startTimeInput->getValue());
		if($tempstarttime <= 0) {
		    $this->targetData("errormsg", "Error: Erroneous Date Range Chosen.");
		}
		else {
		    $this->starttime = $tempstarttime;

		    $this->targetData("errormsg", '');
		    $this->update();
		}
		break;
	    case 'range':
		$tempstarttime = strtotime($this->startTimeInput->getValue());
		if($tempstarttime <= 0) {
		    $this->targetData("errormsg", "Error: Erroneous Date Range Chosen.");
		}
		else {
		    $tempendtime = strtotime($this->endTimeInput->getValue());
		    if($tempendtime <= 0) {
			$this->targetData("errormsg", "Error: Erroneous Date Range Chosen.");
		    }
		    else {
			$this->targetData("errormsg", '');
			// We have a good date range.
			$this->starttime = $tempstarttime;
			$this->endtime = $tempendtime;
			$this->graphImage->setSrc($this->cactiurl . "graph_image.php?local_graph_id=". $this->local_id . "&rra_id=0&view_type=tree&graph_start=".$this->starttime . "&graph_end=" . $this->endtime);
		    }
		}
		break;
	}
    }

    function update() {
	switch($this->graphTypeSelect->getValue()) {
	    case 'scroll':
		$this->endtime = time();
		$this->starttime = $this->endtime - $this->scrolltime;
		break;
	    case 'grow':
		$this->endtime = time();
		break;

	    default:
		// No need to do anything
		break;
	}

	$this->graphImage->setSrc($this->cactiurl . "graph_image.php?local_graph_id=". $this->local_id . "&rra_id=0&view_type=tree&graph_start=".$this->starttime . "&graph_end=" . $this->endtime);
    }

    function graphGenerate($guavaObject, $parameter = null) {
	header('Content-type: image/png');
	passthru($this->execstring);
    }

    public function getTitle() {
	return $this->title;
    }
}

?>
