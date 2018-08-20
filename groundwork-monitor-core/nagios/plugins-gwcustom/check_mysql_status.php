#!/usr/local/groundwork/php/bin/php.bin
<?php

// check_mysql_status nagios plugin
// checks status of values from SHOW STATUS; command in MySQL
// takes various arguments such as what type of value to expect (range, exact, etc.)
// by Dan Fratus (dfratus@gmail.com)
// 12/15/06

define('OK', 0);
define('WARNING', 1);
define('CRITICAL', 2);
define('UNKNOWN', 3);

$mysql_host 	= "localhost";
$mysql_username = "nagios";
$mysql_password = "";

settype($first, 'float');
settype($last, 'float');

function usage(){
	
	echo ("
	check_mysql_status - 1.0 - 12/15/06
	by Dan Fratus (dfratus@gmail.com)
	
	This plugin allows you to check the various values that can 
	be returned by the function SHOW STATUS; in MySQL.
	
	[Usage]
	check_mysql_status -v <variable> -t <expected result>	
	Expected result examples: On, 1, 500-1000, gt1000, lt5000
	
	[Examples]
	check_mysql_status -v table_locks_immediate -t 1-1351354315
	check_mysql_status -v slow_queries
	check_mysql_status -v uptime -t lt5000000
	check_mysql_status -v innodb_rows_inserted
	
	[BTW..]
	* Must be in exactly this format, does not loosely accept variables
	* If you just want to return the number, exclude -t option.
	* By default, mysql_connect uses localhost, root, and no password.
	
");
}
 
$args = getopt("H:v:t:");
if(count($args)){
	$mysql_host=$args['H'];
	$sql = "show status where variable_name = '" . $args['v'] . "'";
	if($link = mysql_connect($mysql_host,$mysql_username,$mysql_password)){
		if(mysql_select_db('mysql', $link)){
			if($result = mysql_query($sql)){
				if(mysql_num_rows($result) > 0){
					$data = mysql_result($result, 0, 1);
					$perfdata = "|" . $args['v'] . "=$data\n";					
					if(isset($args['t']) && $args['t'] != ""){
						$v = $args['v']; $t = $args['t'];
						if(strpos($t, "-") !== FALSE){
							// if its a range
							$first = trim(substr($t,0,strpos($t,"-")));
							$last = trim(substr($t,strpos($t,"-")+1,strlen($t)));
							if($data >= $first && $data <= $last){
								echo "OK: $v = $data is between $first and $last" . $perfdata;
								die(OK);
							} else {
								echo "ERROR: $v = $data is not between $first and $last" . $perfdata;
								die(CRITICAL);	
							}					
						}  elseif(strpos($t, "lt") !== FALSE){
							// less than
							$compare = trim(str_replace("lt","",$t));
							if($data <= $compare){
								echo "OK: $v = $data is less than given value $compare" . $perfdata;
								die(OK);
							} else {
								echo "ERROR: $v = $data is greater than given value $compare" . $perfdata;
								die(CRITICAL);
							}
						} elseif(strpos($t, "gt") !== FALSE){
							// greater than
							$compare = trim(str_replace("gt","",$t));
							if($data >= $compare){
								echo "OK: $v = $data is greater than given value $compare" . $perfdata;
								die(OK);
							} else {
								echo "ERROR: $v = $data is less than given value $compare" . $perfdata;
								die(CRITICAL);
							}
						} else {
							// exact value, case sensitive too..
							// dont want to assume its a number or word, could be either
							if($data == $t){
								echo "$v = $data" . $perfdata;
								die(OK);
							} else {
								echo "ERROR: $v = $data not equal to given value $t" . $perfdata;
								die(CRITICAL);
							}
						} 					
					} else {
						// just echo the result.
						echo $data . $perfdata;
						die(OK);						
					}

				} else {
					echo "Incorrect status variable, no rows returned. Check command syntax.\n";
					die(WARNING);				
				}	
			} else {
				echo "Could not query DB, not too good...\n";
				die(WARNING);			
			}
		} else {
			echo "Could not select mysql DB, user nagios may not have permissions.\n";
			die(WARNING);		
		}
	} else {
		echo "Could not connect to database with user nagios.\n";
		die(CRITICAL);
	}
} else {
	// no params given, lets show the usage
	usage();
}

?>
