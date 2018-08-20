<?php
/*
 +-------------------------------------------------------------------------+
 | Copyright (C) 2007 The Cacti Group                                      |
 |                                                                         |
 | This program is free software; you can redistribute it and/or           |
 | modify it under the terms of the GNU General Public License             |
 | as published by the Free Software Foundation; either version 2          |
 | of the License, or (at your option) any later version.                  |
 |                                                                         |
 | This program is distributed in the hope that it will be useful,         |
 | but WITHOUT ANY WARRANTY; without even the implied warranty of          |
 | MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the           |
 | GNU General Public License for more details.                            |
 +-------------------------------------------------------------------------+
 | Cacti: The Complete RRDTool-based Graphing Solution                     |
 +-------------------------------------------------------------------------+
 | This code is designed, written, and maintained by the Cacti Group. See  |
 | about.php and/or the AUTHORS file for specific developer information.   |
 +-------------------------------------------------------------------------+
 | http://www.cacti.net/                                                   |
 +-------------------------------------------------------------------------+
*/

chdir('../../');
include("./include/auth.php");

if ( strlen( $_SESSION['GWRK_USER']) == 0 ) {
        print "<br><br><center><font color=red>You do not have permission to access this page. Please log in first</font></color>";
        exit;
}


discovery_setup_table();

$hosts = array();
$sql = "SELECT ip FROM plugin_discover_hosts order by hash";
$result = db_fetch_assoc($sql);
foreach ($result as $row) {
	$hosts[] = $row['ip'];
}

$os_arr = array('');
$sql = "SELECT DISTINCT os FROM plugin_discover_hosts";
$result = db_fetch_assoc($sql);
foreach ($result as $row) {
	if ($row['os'] != '')
	$os_arr[] = $row['os'];
}


$where = '';

$status_arr = array('', 'Down', 'Up');
$status = '';
if (isset($_POST['status']))
	$status = sql_sanitize($_POST['status']);
if ($status != '') {
	if ($status == 'Down')
		$where .= "where up=0";
	if ($status == 'Up')
		$where .= "where up=1";
}

$snmp = '';
if (isset($_POST['snmp']))
	$snmp = sql_sanitize($_POST['snmp']);
if ($snmp != '') {
	if ($where == '')
		$where = 'where ';
	else
		$where .= ' and ';

	if ($snmp == 'Down')
		$where .= "snmp=0";
	if ($snmp == 'Up')
		$where .= "snmp=1";
}

$os = '';
if (isset($_POST['os']))
	$os = sql_sanitize($_POST['os']);
if ($os != '' && in_array($os, $os_arr)) {
	if ($where == '')
		$where .= "where os='$os'";
	else
		$where .= " and os='$os'";
}

$hostf = '';
if (isset($_POST['host']))
	$hostf = sql_sanitize($_POST['host']);
if ($hostf != '') {
	if ($where == '')
		$where .= "where hostname like '%$hostf%'";
	else
		$where .= " and hostname like '%$hostf%'";
}

$ip = '';
if (isset($_POST['ip']))
	$ip = sql_sanitize($_POST['ip']);
if ($ip != '') {
	if ($where == '')
		$where .= "where ip like '%$ip%'";
	else
		$where .= " and ip like '%$ip%'";
}

if (isset($_POST['button_clear_x'])) {
	$where = '';
	$status = '';
	$snmp = '';
	$os = '';
	$hostf = '';
	$ip = '';
}

if (isset($_POST['button_export_x'])) {
	$sql = "SELECT * FROM plugin_discover_hosts $where order by hash";
	$result = db_fetch_assoc($sql);
	header("Content-type: text/plain");
	header("Content-Disposition: attachment; filename=discovery_results.log");
	print "Host,IP,Community Name,SNMP Name,Location,Contact,Description,OS,Uptime,SNMP,Status\n";

	foreach ($result as $host) {
		if ($host['sysUptime'] != 0) {
			$days = intval($host['sysUptime']/8640000);
			$hours = intval(($host['sysUptime'] - ($days * 8640000)) / 360000);
			$uptime = $days . ' days ' . $hours . ' hours';
		} else {
			$uptime = '';
		}
		foreach($host as $h=>$r) {
			$host['$h'] = str_replace(',','',$r);
		}
		print $host['hostname'] . ",";
		print $host['ip'] . ",";
		print $host['community'] . ",";
		print $host['sysName'] . ",";
		print $host['sysLocation'] . ",";
		print $host['sysContact'] . ",";
		print $host['sysDescr'] . ",";
		print $host['os'] . ",";
		print $uptime . ",";
		print $host['snmp'] . ",";
		print $host['up'] . "\n";
	}
	exit;
}

$sql = "SELECT up FROM plugin_discover_hosts $where order by hash";
$result = mysql_query($sql) or die (mysql_error());
//$result = db_fetch_assoc($sql);

$page = 1;
if (isset($_GET['page']))
	$page = $_GET['page'];

$per_row = read_config_option("num_rows_device");
$total = mysql_num_rows($result);

if ($total != 0)
	$total_rows = intval($total / $per_row) +1;
else
	$total_rows = 1;
;
if (!isset($_SERVER['QUERY_STRING'])) {
	$_SERVER['QUERY_STRING'] = (isset($HTTP_SERVER_VARS['QUERY_STRING']) ? $HTTP_SERVER_VARS['QUERY_STRING'] : '');
	$_SERVER['PHP_SELF'] = (isset($HTTP_SERVER_VARS['PHP_SELF']) ? $HTTP_SERVER_VARS['PHP_SELF'] : '');
}

if (ereg("page=[0-9]+", basename($_SERVER["QUERY_STRING"]))) {
	$nav_url = str_replace("page=" . $_REQUEST["page"], "page=<PAGE>", basename($_SERVER["PHP_SELF"]) . "?" . $_SERVER["QUERY_STRING"]);
}else{
	$nav_url = basename($_SERVER["PHP_SELF"]) . "?" . $_SERVER["QUERY_STRING"] . "&page=<PAGE>";
}
$nav_url = ereg_replace("((\?|&)host_id=[0-9]+|(\?|&)filter=[a-zA-Z0-9]*)", "", $nav_url);


include($config['base_path'] . "/include/top_graph_header.php");

$start = ($page-1)*$per_row;
$sql = "SELECT * FROM plugin_discover_hosts $where order by hash LIMIT $start, $per_row";
$result = mysql_query($sql) or die (mysql_error());


// TOP DEVICE SELECTION
print '<br><table align="center" width="95%" cellpadding=1 cellspacing=0 border=0 bgcolor="#00438C"><tr><td>';
print "\n<center><table width='100%' cellspacing=0 bgcolor='#E5E5E5'>\n";
print "<tr bgcolor='#00438C'><td colspan=6><font color='#FFFFFF'><b>Filters</b></font></td></tr>";
print "<tr><form name='form_events' method=POST action='" . $config['url_path'] . "plugins/discovery/discover.php'>";

print "<td width=130>&nbsp;&nbsp;&nbsp;&nbsp;Status : ";
print '<select name="status" align=absmiddle>';
print "<option value='' selected>Any</option>\n";
foreach($status_arr as $st) {
	$s = '';
	if ($st == $status)
		$s = ' selected';
	if ($st != '')
		print "<option value='$st'$s>$st</option>";
}
print '</select></td>';

print '<td>&nbsp;&nbsp;&nbsp;&nbsp;OS : ';
print '<select name="os" align=absmiddle>';
print "<option value='' selected>Any</option>\n";
foreach($os_arr as $st) {
	$s = '';
	if ($st == $os)
		$s = ' selected';
	if ($st != '')
		print "<option value='$st'$s>$st</option>";
}
print '</select></td>';

print '<td width=130>&nbsp;&nbsp;&nbsp;&nbsp;SNMP : ';
print '<select name="snmp" align=absmiddle>';
print "<option value='' selected>Any</option>\n";
foreach($status_arr as $st) {
	$s = '';
	if ($st == $snmp)
		$s = ' selected';
	if ($st != '')
		print "<option value='$st'$s>$st</option>";
}
print '</select></td>';

print '<td>&nbsp;&nbsp;&nbsp;&nbsp;Host : ';
print '<input name=host type=text max=5 width=15' . ($hostf != '' ? " value='$hostf'" : '') . '></td>';

print '<td>&nbsp;&nbsp;&nbsp;&nbsp;IP : ';
print '<input name=ip type=text max=5 width=15' . ($ip != '' ? " value='$ip'" : '') . '></td>';

print "<td>&nbsp;&nbsp;&nbsp;&nbsp;<input type=image src='" . $config['url_path'] . "images/button_go.gif' align='absmiddle' border=0 action=submit>";
print "&nbsp;&nbsp;<input type='image' name='button_clear' src='" . $config['url_path'] . "images/button_clear.gif' alt='Reset fields to defaults' border='0' align='absmiddle' action='submit'>";
print "&nbsp;&nbsp;<input type='image' name='button_export' src='" . $config['url_path'] . "images/button_export.gif' alt='Export to a file' border='0' align='absmiddle' action='submit'>";

print '</td></form></tr>';
print '</table></center>';
print '</td></tr></table>';
print '<br>';



# EVENT TABLE
print '<br><table align="center" width="95%" cellpadding=1 cellspacing=0 border=0 bgcolor="#00438C"><tr><td>';
print "\n<center><table width='100%' cellpadding=2 cellspacing=0 bgcolor='#6d88ad'>\n";
print html_nav_bar('00438C', 11, $page, $per_row, $total, $nav_url);
print "<tr bgcolor='#6d88ad' >
	<td style='padding: 4px; margin: 4px;'><font color='#FFFFFF'><b>Host</b></font></td>
	<td><font color='#FFFFFF'><b>IP</b></font></td>
	<td><font color='#FFFFFF'><b>SNMP Name</b></font></td>
	<td><font color='#FFFFFF'><b>Location</b></font></td>
	<td><font color='#FFFFFF'><b>Contact</b></font></td>
	<td><font color='#FFFFFF'><b>Description</b></font></td>
	<td><font color='#FFFFFF'><b>OS</b></font></td>
	<td><font color='#FFFFFF'><b>Uptime</b></font></td>
	<td width=50><font color='#FFFFFF'><b>SNMP</b></font></td>
	<td width=50><font color='#FFFFFF'><b>Status</b></font></td>
	<td width=30>&nbsp;</td></tr>";

$snmp_version 	= read_config_option("snmp_ver");
$snmp_port	= read_config_option("snmp_port");
$snmp_timeout 	= read_config_option("snmp_timeout");
$snmp_username	= read_config_option("snmp_username");
$snmp_password	= read_config_option("snmp_password");
$max_oids       = read_config_option("max_get_size");
$ping_method    = read_config_option("ping_method");
$availability_method = read_config_option("availability_method"); 

$bg = "#E7E9F2";
$status = array('<font color=red>Down</font>','<font color=green>Up</font>');
while ($row = mysql_fetch_array($result, MYSQL_ASSOC)) {
	if ($bg == '#E7E9F2')
		$bg = '#F5F5F5';
	else
		$bg = '#E7E9F2';
	if ($row['sysUptime'] != 0) {
		$days = intval($row['sysUptime']/8640000);
		$hours = intval(($row['sysUptime'] - ($days * 8640000)) / 360000);
		$uptime = $days . ' days ' . $hours . ' hours';
	} else {
		$uptime = '';
	}
	print "<tr bgcolor='$bg'>
		<td style='padding: 4px; margin: 4px;'>" . $row['hostname'] . "</td>
		<td>" . $row['ip'] . '</td>
		<td>' . $row['sysName'] . '</td>
		<td>' . $row['sysLocation'] . '</td>
		<td>' . $row['sysContact'] . '</td>
		<td>' . $row['sysDescr'] . '</td>
		<td>' . $row['os'] . '</td>
		<td>' . $uptime . '</td>
		<td>' . $status[$row['snmp']] . '</td>
		<td>' . $status[$row['up']] . '</td>
		<td style=\'padding: 0px 0px 0px 0px; margin: 0px;\'>';
	print '<form method=POST action="../../host.php">
		<input type=hidden name=save_component_host value=1>
		<input type=hidden name=host_template_id value=0>
		<input type=hidden name=action value="save">
		<input type=hidden name=hostname value=\'' . $row['ip'] . "'>
		<input type=hidden name=id value=0>
		<input type=hidden name=description value=''>
		<input type=hidden name=snmp_community value='" . $row['community'] . "'>
		<input type=hidden name=snmp_version value='$snmp_version'>
		<input type=hidden name=snmp_username value='$snmp_username'>
		<input type=hidden name=snmp_password value='$snmp_password'>
		<input type=hidden name=snmp_port value=$snmp_port>
		<input type=hidden name=snmp_timeout value=$snmp_timeout>
		<input type=hidden name=snmp_password_confirm value=''>
		<input type=hidden name=availability_method value='$availability_method'> 
		<input type=hidden name=ping_method value='$ping_method'>
		<input type=hidden name=ping_port value=''>
		<input type=hidden name=ping_timeout value=''>
		<input type=hidden name=ping_retries value=''>
		<input type=hidden name=notes value=''>
		<input type=hidden name=snmp_auth_protocol value=''>
		<input type=hidden name=snmp_priv_passphrase value=''>
		<input type=hidden name=snmp_priv_protocol value=''>
		<input type=hidden name=snmp_context value=''>
		<input type=hidden name=max_oids value='$max_oids'>
		<input type=submit value=Add style='background-color: $bg; border-width: 0; font-size: 11px; text-decoration: none; border-style: outset; padding: 1px 0px 0px 0px;'>
		</form></td></tr>";
}
if ($total == 0)
	print "<tr bgcolor='$bg'><td style='padding: 4px; margin: 4px;' colspan=11><center>There are no Hosts to display!</center></td></tr>";

print html_nav_bar('00438C', 11, $page, $per_row, $total, $nav_url);

print "</table></center>";

print "</td></tr></table><br><br><br>";

print '</body></html>';

?>
