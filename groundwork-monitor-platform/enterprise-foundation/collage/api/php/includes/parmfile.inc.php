<?php
/*
	parmfile.inc.php
	Reads the Groundwork Parameter (db.parameters) file and assigns the values to proper session variables
*/

function parseDBPropertiesFile($filename) {
	$objectName = '';
	
	if ( ($fp = @fopen($filename, 'r')) == FALSE) {
	    print("Property File Error: Cannot open properties file: $filename");
	    die();
	}
	// If we get here, we've opened the file
	while ($line = fgets($fp)) {
	    if (preg_match('/^\s*(|#.*)$/', $line)) {	// Let's not process a comment
		continue;
	    }
	    if(preg_match('/\s*(\S+)\.(\S+)\s*=\s*(\S*)/', $line, $regs)) {
		    if(!isset($regs[3]))
		    	$regs[3] = '';
		    $_SESSION['groundworkConfig'][$regs[1]][$regs[2]] = $regs[3];
		    continue;
	    }
	}
	return true;
}
