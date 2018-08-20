<?php
/*
Copyright 2005 GroundWork Open Source Solutions, Inc. ("GroundWork")  
All rights reserved. Use is subject to GroundWork commercial license

	Version:	1.0

	Event Viewer : Sample Collage PHP API Application
	Author: Taylor Dondich (tdondich@itgroundwork.com)
	Description:
		Event Viewer uses the Collage PHP API to drill down from hostgroups, to hosts, to events 
		regarding hosts, and events regarding services to those hosts.
	Changelog:
		2005-07-25:	Include support for Groundwork Configuration File.
		2005-06-03:	Revised sample, includes log messages for host.
		2005-05-11:	First Version (With no API Error Checking)
*/
// Include the collegeapi.php, this assumes it's in your includes path, if not, specify the location
include_once('collageapi/collage.inc.php');
include_once('includes/output.php');	// Include application's output library
include_once('includes/parmfile.inc.php');	// Include routine to help read groundwork parameter file


// Configuration
$db_properties_file = "/usr/local/groundwork/config/db.properties";	// Path to our db.properties file



session_start();
session_register("groundworkConfig");
parseDBPropertiesFile($db_properties_file);
// If we got here, we've read the file, let's check for required variables
if(!isset($_SESSION['groundworkConfig']['collage']['username']) || 
	!isset($_SESSION['groundworkConfig']['collage']['password']) || 
	!isset($_SESSION['groundworkConfig']['collage']['database']) ||
	!isset($_SESSION['groundworkConfig']['collage']['dbhost'])) {
		print("Properties File Error: One or more of the required fields are not defined.");
		die();
}
// We got here!  Let's test connect to the server
$testDB = new CollageDB("mysql", $_SESSION['groundworkConfig']['collage']['dbhost'], 
				$_SESSION['groundworkConfig']['collage']['username'],
				$_SESSION['groundworkConfig']['collage']['password'],
				$_SESSION['groundworkConfig']['collage']['database'],'NAGIOS');
if(!$testDB->isConnected()) {
	print_r($_SESSION['groundworkConfig']['collage']);
	print("Collage DB Error: Database not connected.  Check your connection parameters.<br />");
	print($collageDB->get_error_msg());
	die();
}

if(!isset($_GET['step']))
	$_GET['step'] = 'hostgroups';
	
if($_GET['step'] == 'hostgroups') {
	$hostGroupQuery = new CollageHostGroupQuery($testDB);
	$hostGroups = $hostGroupQuery->getHostGroups();
	$numOfHostGroups = count($hostGroups);
}
elseif($_GET['step'] == 'hosts') {
	$hostGroupQuery = new CollageHostGroupQuery($testDB);
	$hostQuery = new CollageHostQuery($testDB);
	$tempHosts = $hostGroupQuery->getHostsForHostGroup($_GET['HostGroup']);
	$numOfHosts = count($tempHosts);
	for($counter = 0; $counter < $numOfHosts; $counter++) {
		$hosts[] = $hostQuery->getHostByID($tempHosts[$counter]['HostID']);
	}
}
elseif($_GET['step'] == 'services') {
	if(!isset($_GET['refreshinsec']))
		$_GET['refreshinsec'] = 10;
	$hostQuery = new CollageHostQuery($testDB);
	$hostStatus = $hostQuery->getHostStatusForHost($_GET['Host']);
	$hostInfo = $hostQuery->getHost($_GET['Host']);
	$hostMonitorStatus = $hostQuery->getMonitorStatus($hostStatus['MonitorStatusID']);
	$hostServices = $hostQuery->getServicesForHost($_GET['Host']);
	$numOfServices = count($hostServices);
	for($counter = 0; $counter < $numOfServices; $counter++) {
		$hostServicesStatus[$counter] = $hostQuery->getMonitorStatus($hostServices[$counter]['MonitorStatusID']);
	}
}
elseif($_GET['step'] == "logs") {
	$eventQuery = new CollageEventQuery($testDB);
	$tempEvents = $eventQuery->getEventsForHost($_GET['Host'], NULL, NULL, NULL, 20);
		// We don't have any dates we need, all we want is the latest 5
	if(count($tempEvents)) {
		foreach($tempEvents as $event) {
			$severityInfo = $eventQuery->getSeverity($event['SeverityID']);
			$tempEventsSeverityNames[] = $severityInfo[0]['Name'];
		}
	}
}
	
		

if(isset($_GET['refreshinsec'])) {
	print_header("Host Status For ".$_GET['Host']." (Refresh Every ".$_GET['refreshinsec']." Seconds)", $_GET['refreshinsec']);
}
else {
	print_header("Status Viewer");
}
?>
<a href="sample.php">Start Over</a><br />
<br />
<?php
if($_GET['step'] == 'hostgroups') {
	print_window_header("Host Groups", "80%");
	if($numOfHostGroups) {
		?>
		<br />
		<table width="100%" align="center" cellspacing="0" cellpadding="0" border="0">
		<tr>
		<td colspan="2"><b>HostGroup Name</b></td>
		<td colspan="2"><b>Description</b></td>
		</tr>
		<tr>
			<td width="1" bgcolor="#aaaaaa"><img src="<?=$path_config['image_root'];?>dotclear.gif" height="1" width="1" /></td>
			<td colspan="2" height="1" bgcolor="#aaaaaa"><img src="<?=$path_config['image_root'];?>dotclear.gif" height="1" width="1" /></td>
			<td width="1" bgcolor="#aaaaaa"><img src="<?=$path_config['image_root'];?>dotclear.gif" height="1" width="1" /></td>
		</tr>
		<?php
		for($counter = 0; $counter < $numOfHostGroups; $counter++) {
			if($counter % 2) {
				?>
				<tr bgcolor="#cccccc">
				<?php
			}
			else {
				?>
				<tr bgcolor="#f0f0f0">
				<?php
			}
			?>
			<td width="1" bgcolor="#aaaaaa"><img src="<?=$path_config['image_root'];?>dotclear.gif" height="1" width="1" /></td>
			<td height="20">&nbsp;<a href="<?=$path_config['doc_root'];?>sample.php?step=hosts&HostGroup=<?=$hostGroups[$counter]['Name'];?>"><?=$hostGroups[$counter]['Name'];?></a></td>
			<td height="20">&nbsp;<?=$hostGroups[$counter]['Description'];?></td>
			<td width="1" bgcolor="#aaaaaa"><img src="<?=$path_config['image_root'];?>dotclear.gif" height="1" width="1" /></td>
			</tr>
			<tr>
				<td width="1" bgcolor="#aaaaaa"><img src="<?=$path_config['image_root'];?>dotclear.gif" height="1" width="1" /></td>
				<td colspan="2" height="1" bgcolor="#aaaaaa"><img src="<?=$path_config['image_root'];?>dotclear.gif" height="1" width="1" /></td>
				<td width="1" bgcolor="#aaaaaa"><img src="<?=$path_config['image_root'];?>dotclear.gif" height="1" width="1" /></td>
			</tr>
			<?php
		}
		?>
		</table>
		<?php
	
	}
	else {
		?>
		<br />
		<div class="statusmsg">No Host Groups Exist</div>
		<?php
	}
	print_window_footer();
}
else if($_GET['step'] == 'hosts') {
	print_window_header("Hosts in " . $_GET['HostGroup'], "80%");
	if($numOfHosts) {
		?>
		<br />
		<table width="100%" align="center" cellspacing="0" cellpadding="0" border="0">
		<tr>
		<td colspan="2"><b>Host Name</b></td>
		<td colspan="2"><b>Description</b></td>
		</tr>
		<tr>
			<td width="1" bgcolor="#aaaaaa"><img src="<?=$path_config['image_root'];?>dotclear.gif" height="1" width="1" /></td>
			<td colspan="2" height="1" bgcolor="#aaaaaa"><img src="<?=$path_config['image_root'];?>dotclear.gif" height="1" width="1" /></td>
			<td width="1" bgcolor="#aaaaaa"><img src="<?=$path_config['image_root'];?>dotclear.gif" height="1" width="1" /></td>
		</tr>
		<?php
		for($counter = 0; $counter < $numOfHosts; $counter++) {
			if($counter % 2) {
				?>
				<tr bgcolor="#cccccc">
				<?php
			}
			else {
				?>
				<tr bgcolor="#f0f0f0">
				<?php
			}
			?>
			<td width="1" bgcolor="#aaaaaa"><img src="<?=$path_config['image_root'];?>dotclear.gif" height="1" width="1" /></td>
			<td height="20">&nbsp;<a href="<?=$path_config['doc_root'];?>sample.php?step=services&Host=<?=$hosts[$counter]['HostName'];?>"><?=$hosts[$counter]['HostName'];?></a></td>
			<td height="20">&nbsp;<?=$hosts[$counter]['Description'];?></td>
			<td width="1" bgcolor="#aaaaaa"><img src="<?=$path_config['image_root'];?>dotclear.gif" height="1" width="1" /></td>
			</tr>
			<tr>
				<td width="1" bgcolor="#aaaaaa"><img src="<?=$path_config['image_root'];?>dotclear.gif" height="1" width="1" /></td>
				<td colspan="2" height="1" bgcolor="#aaaaaa"><img src="<?=$path_config['image_root'];?>dotclear.gif" height="1" width="1" /></td>
				<td width="1" bgcolor="#aaaaaa"><img src="<?=$path_config['image_root'];?>dotclear.gif" height="1" width="1" /></td>
			</tr>
			<?php
		}
		?>
		</table>
		<?php
	
	}
	else {
		?>
		<br />
		<div class="statusmsg">No Hosts Exist In That Hostgroup</div>
		<?php
	}
	print_window_footer();
}
else if($_GET['step'] == 'services') {
	?>
	<center>
	<form name="refresh_change" action="sample.php" method="get">
	<input type="hidden" name="Host" value="<?=$_GET['Host'];?>" />
	<input type="hidden" name="step" value="services" />
	Refresh Every 
	<select name="refreshinsec">
	<option value="5" <?php if($_GET['refreshinsec'] == 5) print("SELECTED");?>>5</option>
	<option value="10" <?php if($_GET['refreshinsec'] == 10) print("SELECTED");?>>10</option>
	<option value="15" <?php if($_GET['refreshinsec'] == 15) print("SELECTED");?>>15</option>
	<option value="30" <?php if($_GET['refreshinsec'] == 30) print("SELECTED");?>>30</option>
	</select> Seconds <input type="submit" value="Change" />
	</form>
	</center>
	<?php
	print_window_header("Host Status For " . $_GET['Host'], "80%");
		?>
		<b><?=$hostInfo['Description'];?><br />
		<table width="100%" border="0" />
		<tr>
		<td valign="top" width="20" align="center">
		<?php
		switch($hostMonitorStatus['Name']) {
			case 'UP':
				?>
				<img src="images/bullet_green.png" />
				<?php
				break;
			case 'DOWN':
				?>
				<img src="images/bullet_red.png" />
				<?php
				break;
			default:
				?>
				<img src="images/bullet_yellow.png" />
				<?php
				break;
		}
		?>
		</td>
		<td>	
		<?php if ($hostStatus == null) print("HostStatus is NULL");?>	
		<?=$hostMonitorStatus['Name'];?></b> As Of <?=$hostStatus['LastStateChange'];?> <br />
		<br />
		<?php
		if($hostMonitorStatus['Name'] != 'UP') {		// Houston, we have a problem
			?>
			Problem has <?php if(!$hostStatus['isAcknowledged']) print(" NOT ");?>been acknowledged.<br />
			<br />
			<?php
		}
		?>
		</td>
		</tr>
		</table>
		[ <a href="sample.php?step=logs&Host=<?=$_GET['Host'];?>">Retrieve Last 20 Log Messages For This Host</a> ]
		<?php
	print_window_footer();
	?>
	<br />
	<br />
	<?php
	print_window_header("Service Status For ". $_GET['Host'], "80%");
	if($numOfServices) {
		?>
		<br />
		<table width="100%" align="center" cellspacing="0" cellpadding="0" border="0">
		<tr>
			<td width="1" bgcolor="#aaaaaa"><img src="<?=$path_config['image_root'];?>dotclear.gif" height="1" width="1" /></td>
			<td colspan="1" height="1" bgcolor="#aaaaaa"><img src="<?=$path_config['image_root'];?>dotclear.gif" height="1" width="1" /></td>
			<td width="1" bgcolor="#aaaaaa"><img src="<?=$path_config['image_root'];?>dotclear.gif" height="1" width="1" /></td>
		</tr>
		<?php
		for($counter = 0; $counter < $numOfServices; $counter++) {
			if($counter % 2) {
				?>
				<tr bgcolor="#cccccc">
				<?php
			}
			else {
				?>
				<tr bgcolor="#f0f0f0">
				<?php
			}
			?>
			<td width="1" bgcolor="#aaaaaa"><img src="<?=$path_config['image_root'];?>dotclear.gif" height="1" width="1" /></td>
			<td>
				<b><?=$hostServices[$counter]['ServiceDescription'];?><br />
				<table width="100%" border="0" />
				<tr>
				<td valign="top" width="20" align="center">
				<?php
				switch($hostServicesStatus[$counter]['Name']) {
					case 'OK':
						?>
						<img src="images/bullet_green.png" />
						<?php
						break;
					case 'DOWN':
						?>
						<img src="images/bullet_red.png" />
						<?php
						break;
					default:
						?>
						<img src="images/bullet_yellow.png" />
						<?php
						break;
				}
				?>
				</td>
				<td>
				<b><?=$hostServicesStatus[$counter]['Name'];?> as of <?=$hostServices[$counter]['LastStateChange'];?><br />
				<?php
				if($hostServicesStatus[$counter]['Name'] != 'OK') {
					?>
					Problem has <?php if(!$hostServices[$counter]['isProblemAcknowledged']) print(" NOT ");?>been acknowledged.<br />
					<br />
					<?php
				}
				?>
				</td>
				</tr>
				</table>
			</td>
			<td width="1" bgcolor="#aaaaaa"><img src="<?=$path_config['image_root'];?>dotclear.gif" height="1" width="1" /></td>
			</tr>
			<tr>
				<td width="1" bgcolor="#aaaaaa"><img src="<?=$path_config['image_root'];?>dotclear.gif" height="1" width="1" /></td>
				<td colspan="1" height="1" bgcolor="#aaaaaa"><img src="<?=$path_config['image_root'];?>dotclear.gif" height="1" width="1" /></td>
				<td width="1" bgcolor="#aaaaaa"><img src="<?=$path_config['image_root'];?>dotclear.gif" height="1" width="1" /></td>
			</tr>
			<?php
		}
		?>
		</table>
		<?php
	}
	else {
		?>
		<br />
		<div class="statusmsg">No Service data exists for that host.</div>
		<?php
	}
		
	print_window_footer();
}
if($_GET['step'] == "logs") {
	$numOfEvents = count($tempEvents);	// Just in case there's less than 5 returned
	print_window_header("Latest Events For Host: " . $_GET['Host'], "80%");
	?>
	<br />
	<table width="100%" align="center" cellspacing="0" cellpadding="0" border="0">
	<tr>
		<td colspan="2">Report Date</td>
		<td>Severity</td>
		<td>Priority</td>
		<td>Logger</td>
		<td>Application Code</td>
		<td>Application Name</td>
		<td colspan="2">Message</td>
		
	<tr>
		<td width="1" bgcolor="#aaaaaa"><img src="<?=$path_config['image_root'];?>dotclear.gif" height="1" width="1" /></td>
		<td colspan="7" height="1" bgcolor="#aaaaaa"><img src="<?=$path_config['image_root'];?>dotclear.gif" height="1" width="1" /></td>
		<td width="1" bgcolor="#aaaaaa"><img src="<?=$path_config['image_root'];?>dotclear.gif" height="1" width="1" /></td>
	</tr>
	<?php
	for($counter = 0; $counter < $numOfEvents; $counter++) {	
		if($counter % 2) {
			?>
			<tr bgcolor="#cccccc">
			<?php
		}
		else {
			?>
			<tr bgcolor="#f0f0f0">
			<?php
		}
		?>
		<td width="1" bgcolor="#aaaaaa"><img src="<?=$path_config['image_root'];?>dotclear.gif" height="1" width="1" /></td>
		<td><?=$tempEvents[$counter]['ReportDate'];?></td>
		<td><?=$tempEventsSeverityNames[$counter];?></td>
		<td><?=$tempEvents[$counter]['PriorityID'];?></td>
		<td><?=$tempEvents[$counter]['LoggerName'];?></td>
		<td><?=$tempEvents[$counter]['ApplicationCode'];?></td>
		<td><?=$tempEvents[$counter]['ApplicationName'];?></td>
		<td><?=$tempEvents[$counter]['TextMessage'];?></td>
		<td width="1" bgcolor="#aaaaaa"><img src="<?=$path_config['image_root'];?>dotclear.gif" height="1" width="1" /></td>
		</tr>
		<tr>
			<td width="1" bgcolor="#aaaaaa"><img src="<?=$path_config['image_root'];?>dotclear.gif" height="1" width="1" /></td>
			<td colspan="7" height="1" bgcolor="#aaaaaa"><img src="<?=$path_config['image_root'];?>dotclear.gif" height="1" width="1" /></td>
			<td width="1" bgcolor="#aaaaaa"><img src="<?=$path_config['image_root'];?>dotclear.gif" height="1" width="1" /></td>
		</tr>
		<?php		
	}
	?>
	</table>
	<?php	
	print_window_footer();
}

print_footer();
?>
