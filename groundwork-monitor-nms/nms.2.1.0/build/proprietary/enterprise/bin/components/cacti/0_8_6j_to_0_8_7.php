<?php
/*
 +-------------------------------------------------------------------------+
 | Copyright (C) 2004-2008 The Cacti Group                                 |
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

function upgrade_to_0_8_7() {
        $global_auth = "on";
	$global_auth_db = db_fetch_row("SELECT value FROM settings WHERE name = 'global_auth'");
	if (sizeof($global_auth_db)) {
		$global_auth = $global_auth_db["value"];

	}
        $ldap_enabled = "";
	$ldap_enabled_db = db_fetch_row("SELECT value FROM settings WHERE name = 'ldap_enabled'");
	if (sizeof($ldap_enabled_db)) {
		$ldap_enabled = $ldap_enable_db["value"];

	}
	if ($global_auth == "on") {
		if ($ldap_enabled == "on") {
			db_install_execute("0.8.7", "INSERT INTO settings VALUES ('auth_method','3')");
		}else{
			db_install_execute("0.8.7", "INSERT INTO settings VALUES ('auth_method','1')");
		}
	}else{
		db_install_execute("0.8.7", "INSERT INTO settings VALUES ('auth_method','0')");
	}
	db_install_execute("0.8.7", "UPDATE `settings` SET name = 'user_template' WHERE name = 'ldap_template'");
	db_install_execute("0.8.7", "DELETE FROM `settings` WHERE name = 'global_auth'");
	db_install_execute("0.8.7", "DELETE FROM `settings` WHERE name = 'ldap_enabled'");

	/* host settings for availability */
	$ping_method         = read_config_option("ping_method");
	$ping_retries        = read_config_option("ping_retries");
	$ping_timeout        = read_config_option("ping_timeout");
	$availability_method = read_config_option("availability_method");
	$hosts               = db_fetch_assoc("SELECT id, snmp_community, snmp_version FROM host");

	if (sizeof($hosts)) {
		foreach($hosts as $host) {
			if (strlen($host["snmp_community"] != 0)) {
				if ($host["snmp_version"] == "3") {
					if ($availability_method == AVAIL_SNMP) {
						db_install_execute("0.8.7", "UPDATE host SET snmp_priv_protocol='[None]', snmp_auth_protocol='MD5', availability_method=" . AVAIL_SNMP . ", ping_method=" . PING_UDP . ",ping_timeout=" . $ping_timeout . ", ping_retries=" . $ping_retries . " WHERE id=" . $host["id"]);
					}else if ($availability_method == AVAIL_SNMP_AND_PING) {
						if ($ping_method == PING_ICMP) {
							db_install_execute("0.8.7", "UPDATE host SET snmp_priv_protocol='[None]', availability_method=" . AVAIL_SNMP_AND_PING . ", ping_method=" . $ping_method . ", ping_timeout=" . $ping_timeout . ", ping_retries=" . $ping_retries . " WHERE id=" . $host["id"]);
						}else{
							db_install_execute("0.8.7", "UPDATE host SET snmp_priv_protocol='[None]', availability_method=" . AVAIL_SNMP_AND_PING . ", ping_method=" . $ping_method . ", ping_port=33439, ping_timeout=" . $ping_timeout . ", ping_retries=" . $ping_retries . " WHERE id=" . $host["id"]);
						}
					}else{
						if ($ping_method == PING_ICMP) {
							db_install_execute("0.8.7", "UPDATE host SET snmp_priv_protocol='[None]', availability_method=" . AVAIL_PING . ", ping_method=" . $ping_method . ", ping_timeout=" . $ping_timeout . ", ping_retries=" . $ping_retries . " WHERE id=" . $host["id"]);
						}else{
							db_install_execute("0.8.7", "UPDATE host SET snmp_priv_protocol='[None]', availability_method=" . AVAIL_PING . ", ping_method=" . $ping_method . ", ping_port=33439, ping_timeout=" . $ping_timeout . ", ping_retries=" . $ping_retries . " WHERE id=" . $host["id"]);
						}
					}
				}else{
					if ($availability_method == AVAIL_SNMP) {
						db_install_execute("0.8.7", "UPDATE host SET availability_method=" . AVAIL_NONE . ", ping_method=" . PING_UDP . ",ping_timeout=" . $ping_timeout . ", ping_retries=" . $ping_retries . " WHERE id=" . $host["id"]);
					}else if ($availability_method == AVAIL_SNMP_AND_PING) {
						if ($ping_method == PING_ICMP) {
							db_install_execute("0.8.7", "UPDATE host SET availability_method=" . AVAIL_SNMP_AND_PING . ", ping_method=" . $ping_method . ", ping_timeout=" . $ping_timeout . ", ping_retries=" . $ping_retries . " WHERE id=" . $host["id"]);
						}else{
							db_install_execute("0.8.7", "UPDATE host SET availability_method=" . AVAIL_SNMP_AND_PING . ", ping_method=" . $ping_method . ", ping_port=33439, ping_timeout=" . $ping_timeout . ", ping_retries=" . $ping_retries . " WHERE id=" . $host["id"]);
						}
					}else{
						if ($ping_method == PING_ICMP) {
							db_install_execute("0.8.7", "UPDATE host SET availability_method=" . AVAIL_PING . ", ping_method=" . $ping_method . ", ping_timeout=" . $ping_timeout . ", ping_retries=" . $ping_retries . " WHERE id=" . $host["id"]);
						}else{
							db_install_execute("0.8.7", "UPDATE host SET availability_method=" . AVAIL_PING . ", ping_method=" . $ping_method . ", ping_port=33439, ping_timeout=" . $ping_timeout . ", ping_retries=" . $ping_retries . " WHERE id=" . $host["id"]);
						}
					}
				}
			}else{
				if ($availability_method == AVAIL_SNMP) {
					db_install_execute("0.8.7", "UPDATE host SET availability_method=" . AVAIL_SNMP . ", ping_method=" . PING_UDP . ", ping_timeout = " . $ping_timeout . ", ping_retries=" . $ping_retries . " WHERE id=" . $host["id"]);
				}else if ($availability_method == AVAIL_SNMP_AND_PING) {
					if ($ping_method == PING_ICMP) {
						db_install_execute("0.8.7", "UPDATE host SET availability_method=" . AVAIL_SNMP_AND_PING . ", ping_method=" . $ping_method . ", ping_timeout=" . $ping_timeout . ", ping_retries=" . $ping_retries . " WHERE id=" . $host["id"]);
					}else{
						db_install_execute("0.8.7", "UPDATE host SET availability_method=" . AVAIL_SNMP_AND_PING . ", ping_method=" . $ping_method . ", ping_port=33439, ping_timeout=" . $ping_timeout . ", ping_retries=" . $ping_retries . " WHERE id=" . $host["id"]);
					}
				}else{
					if ($ping_method == PING_ICMP) {
						db_install_execute("0.8.7", "UPDATE host SET availability_method=" . AVAIL_PING . ", ping_method=" . $ping_method . ", ping_timeout=" . $ping_timeout . ", ping_retries=" . $ping_retries . " WHERE id=" . $host["id"]);
					}else{
						db_install_execute("0.8.7", "UPDATE host SET availability_method=" . AVAIL_PING . ", ping_method=" . $ping_method . ", ping_port=33439, ping_timeout=" . $ping_timeout . ", ping_retries=" . $ping_retries . " WHERE id=" . $host["id"]);
					}
				}
			}
		}
	}

	/* Add 1 min rra */
	db_install_execute("0.8.7", "INSERT INTO rra VALUES (DEFAULT,'283ea2bf1634d92ce081ec82a634f513','Hourly (1 Minute Average)',0.5,1,500,14400)");
	$rrd_id = mysql_insert_id();
	db_install_execute("0.8.7", "INSERT INTO `rra_cf` VALUES ($rrd_id,1), ($rrd_id,3)");
}
?>
