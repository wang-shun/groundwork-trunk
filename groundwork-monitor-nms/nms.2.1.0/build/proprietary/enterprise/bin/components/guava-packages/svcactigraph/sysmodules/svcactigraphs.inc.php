<?php
class SVCactiGraphsSystemModule extends SystemModule {
    // Database connection parameters
    private $dbServ;	// Either mysql or sqlite
    private $dbHost;
    private $dbUsername;
    private $dbPassword;
    private $dbDatabase;
    private $dbConnection = null;

    private $interval;	// seconds

    function __construct() {
	global $guava;
	$this->dbServ     = 'mysql';
	$this->dbHost = $guava->getpreference('svcactigraphs', 'address');
	$this->dbUsername = $guava->getpreference('svcactigraphs', 'username');
	$this->dbPassword = $guava->getpreference('svcactigraphs', 'password');
	$this->dbDatabase = $guava->getpreference('svcactigraphs', 'dbname');
	$this->interval = 2;
	parent::__construct("SVCactiGraphsSystemModule");
    }

    function __destruct() {
	if (isset($this->dbConnection)) {
	    $this->dbConnection->Close();
	}
    }

    function DBConnect() {
	$this->dbConnection = ADONewConnection($this->dbServ);
	@$this->dbConnection->Connect($this->dbHost, $this->dbUsername, $this->dbPassword, $this->dbDatabase);
	if(!$this->dbConnection->IsConnected()) {
	    $this->dbConnection = null;
	    throw new Exception ('Unable to connect to the Cacti database "' . $this->dbDatabase . '" on host "' .
		$this->dbHost . '".  Please have your system administrator check the configuration.');
	}
	$this->dbConnection->SetFetchMode(ADODB_FETCH_ASSOC);
    }

    function init() {
	global $svcactigraphs;
	global $sv2;
	$svcactigraphs = $this;

	// Okay, we need to now talk to the status viewer object and attempt to register our extensions
	if(isset($sv2)) {
	    $sv2->registerExtension('host', 'SVCactiGraphsContainer', 'Cacti Graphs');
	}
	else {
	    print("SVCactiGraphs: Failed to communicate to Status Viewer.");
	}
    }

    function restart() {
	global $svcactigraphs;
	// Restart the object
	if (isset($this->dbConnection)) {
	    $this->dbConnection->Close();
	}
	$this->dbConnection = null;
	$svcactigraphs = $this;	// Recreate the global link to ourself.
    }

    function reload() {
	$this->dbHost = $guava->getpreference('svcactigraphs', 'address');
	$this->dbUsername = $guava->getpreference('svcactigraphs', 'username');
	$this->dbPassword = $guava->getpreference('svcactigraphs', 'password');
	$this->dbDatabase = $guava->getpreference('svcactigraphs', 'dbname');
	if (isset($this->dbConnection)) {
	    $this->dbConnection->Close();
	}
	$this->dbConnection = null;
    }

    public function obtainGraphsForHost($host) {
	global $guava;
	$graphs = null;
	$query = "SELECT local_graph_id, title_cache from graph_templates_graph WHERE local_graph_id in (SELECT id FROM graph_local WHERE host_id in (SELECT id FROM host WHERE description = '$host'))";
	if (!isset($this->dbConnection)) {
	    try {
		$this->DBConnect();
	    } catch (Exception $e) {
		return $e->getMessage();
	    }
	}
	$result = $this->dbConnection->Execute($query);
	if($result != false) {
	    // Then we should have a result!
	    $graphs = array();
	    $guava->console("Number of graphs for host '$host': " . $result->RecordCount());
	    // We've got graphs!
	    while(!$result->EOF) {
		$tempGraph['local_id'] = $result->fields['local_graph_id'];
		$tempGraph['title']    = $result->fields['title_cache'];
		$graphs[] = $tempGraph;
		$result->MoveNext();
	    }
	    $guava->console("Count of graphs array: " . count($graphs));
	    return $graphs;
	}
	return 'Unable to query the Cacti database "' . $this->dbDatabase . '" on host "' .
	    $this->dbHost . '".  Please have your system administrator check the configuration.';
    }

}

?>
